/*
 *    Copyright (c) 2020, VRAI Labs and/or its affiliates. All rights reserved.
 *
 *    This software is licensed under the Apache License, Version 2.0 (the
 *    "License") as published by the Apache Software Foundation.
 *
 *    You may not use this file except in compliance with the License. You may
 *    obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 *
 */

package io.supertokens.storage.sql;

import io.supertokens.pluginInterface.exceptions.QuitProgramFromPluginException;
import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.pluginInterface.exceptions.StorageTransactionLogicException;
import io.supertokens.pluginInterface.sqlStorage.SQLStorage;
import io.supertokens.storage.sql.config.Config;
import io.supertokens.storage.sql.config.PostgreSQLConfig;
import io.supertokens.storage.sql.hibernate.HibernateUtils;
import io.supertokens.storage.sql.output.Logging;
import io.supertokens.storage.sql.utils.Utils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.internal.SessionImpl;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;

public class ConnectionPool extends ResourceDistributor.SingletonResource {

    private static final String RESOURCE_KEY = "io.supertokens.storage.sql.ConnectionPool";
    private static SessionFactory sessionFactory = null;

    private ConnectionPool(Start start) {
        if (!start.enabled) {
            throw new RuntimeException(new ConnectException("Connection to refused")); // emulates exception thrown by
            // Hikari
        }

        if (ConnectionPool.sessionFactory != null) {
            // This implies that it was already created before and that
            // there is no need to create sessionFactory again.

            // If ConnectionPool.sessionFactory == null, it implies that
            // either the config file had changed somehow (which means the plugin JAR was reloaded, resulting in static
            // variables to be set to null), or it means that this is the first time we are trying to connect to a db
            // (applicable only for testing).
            return;
        }

        final PostgreSQLConfig config = Config.getConfig(start);
        sessionFactory = HibernateUtils.sessionFactory(config);
    }

    private static int getTimeToWaitToInit(Start start) {
        int actualValue = 3600 * 1000;
        if (Start.isTesting) {
            Integer testValue = ConnectionPoolTestContent.getInstance(start)
                    .getValue(ConnectionPoolTestContent.TIME_TO_WAIT_TO_INIT);
            return Objects.requireNonNullElse(testValue, actualValue);
        }
        return actualValue;
    }

    private static int getRetryIntervalIfInitFails(Start start) {
        int actualValue = 10 * 1000;
        if (Start.isTesting) {
            Integer testValue = ConnectionPoolTestContent.getInstance(start)
                    .getValue(ConnectionPoolTestContent.RETRY_INTERVAL_IF_INIT_FAILS);
            return Objects.requireNonNullElse(testValue, actualValue);
        }
        return actualValue;
    }

    private static ConnectionPool getInstance(Start start) {
        return (ConnectionPool) start.getResourceDistributor().getResource(RESOURCE_KEY);
    }

    static void initPool(Start start) {
        if (getInstance(start) != null) {
            return;
        }
        if (Thread.currentThread() != start.mainThread) {
            throw new QuitProgramFromPluginException("Should not come here");
        }
        Logging.info(start, "Setting up PostgreSQL connection pool.");
        boolean longMessagePrinted = false;
        long maxTryTime = System.currentTimeMillis() + getTimeToWaitToInit(start);
        // TODO: sql-plugin -> Form error message based on db being configured..
        String errorMessage = "Error connecting to PostgreSQL instance. Please make sure that PostgreSQL is running and that "
                + "you have" + " specified the correct values for ('postgresql_host' and 'postgresql_port') or for "
                + "'postgresql_connection_uri'";
        try {
            while (true) {
                try {
                    start.getResourceDistributor().setResource(RESOURCE_KEY, new ConnectionPool(start));
                    break;
                } catch (Exception e) {
                    if (hibernateFailedToConnect(e)) {
                        start.handleKillSignalForWhenItHappens();
                        if (System.currentTimeMillis() > maxTryTime) {
                            throw new QuitProgramFromPluginException(errorMessage);
                        }
                        if (!longMessagePrinted) {
                            longMessagePrinted = true;
                            Logging.info(start, errorMessage);
                        }
                        double minsRemaining = (maxTryTime - System.currentTimeMillis()) / (1000.0 * 60);
                        NumberFormat formatter = new DecimalFormat("#0.0");
                        Logging.info(start,
                                "Trying again in a few seconds for " + formatter.format(minsRemaining) + " mins...");
                        try {
                            if (Thread.interrupted()) {
                                throw new InterruptedException();
                            }
                            Thread.sleep(getRetryIntervalIfInitFails(start));
                        } catch (InterruptedException ex) {
                            throw new QuitProgramFromPluginException(errorMessage);
                        }
                    } else {
                        throw e;
                    }
                }
            }
        } finally {
            start.removeShutdownHook();
        }
    }

    static void close(Start start) {
        if (getInstance(start) == null) {
            return;
        }
        HibernateUtils.close();
        ConnectionPool.sessionFactory = null;
    }

    private static boolean hibernateFailedToConnect(Exception e) {
        return Utils.isExceptionCause(ConnectException.class, e);
    }

    public interface WithSession<T> {
        T op(Session session, Connection con) throws SQLException, StorageQueryException;
    }

    public interface WithSessionForComplexTransaction<T> {
        T op(Session session, Connection con)
                throws SQLException, StorageQueryException, StorageTransactionLogicException;

    }

    public static <T> T withSession(Start start, WithSession<T> func, boolean beginTransaction)
            throws SQLException, StorageQueryException {
        if (getInstance(start) == null) {
            throw new QuitProgramFromPluginException("Please call initPool before getConnection");
        }
        if (!start.enabled) {
            throw new SQLException("Should never come here");
        }

        if (beginTransaction) {
            // for non-SELECT query
            try {
                return withSessionForComplexTransaction(start, null, func::op);
            } catch (StorageTransactionLogicException e) {
                throw new SQLException("Should never come here");
            }
        } else {
            // for SELECT queries
            SessionFactory sessionFactory = ConnectionPool.sessionFactory;
            try (Session session = sessionFactory.openSession()) {
                Connection con = ((SessionImpl) session).connection();
                return func.op(session, con);
            }
        }
    }

    public static <T> T withSessionForComplexTransaction(Start start,
            SQLStorage.TransactionIsolationLevel isolationLevel, WithSessionForComplexTransaction<T> func)
            throws SQLException, StorageQueryException, StorageTransactionLogicException {
        if (getInstance(start) == null) {
            throw new QuitProgramFromPluginException("Please call initPool before getConnection");
        }
        if (!start.enabled) {
            throw new SQLException("Storage layer disabled");
        }

        SessionFactory sessionFactory = ConnectionPool.sessionFactory;
        try (Session session = sessionFactory.openSession()) {
            // we assume that these queries will always have a non-SELECT part in them
            // so that's why we always begin a transaction.
            Transaction tx = null;

            // we do not use try-with resource for Connection below cause we close
            // the entire Session itself.
            Connection con = ((SessionImpl) session).connection();

            if (isolationLevel != null) {
                int libIsolationLevel = Connection.TRANSACTION_SERIALIZABLE;
                switch (isolationLevel) {
                case SERIALIZABLE:
                    break;
                case REPEATABLE_READ:
                    libIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ;
                    break;
                case READ_COMMITTED:
                    libIsolationLevel = Connection.TRANSACTION_READ_COMMITTED;
                    break;
                case READ_UNCOMMITTED:
                    libIsolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;
                    break;
                case NONE:
                    libIsolationLevel = Connection.TRANSACTION_NONE;
                    break;
                }
                // TODO: sql-plugin -> Previously we used to store the defualt isolation level and then restore
                // it
                // in the connection. But I think that's not needed. Is this correct?
                con.setTransactionIsolation(libIsolationLevel);
            }
            try {
                tx = session.beginTransaction();
                T result = func.op(session, con);
                if (tx.isActive()) {
                    // maybe the user has already commited the transaction manually.
                    tx.commit();
                }
                return result;
            } catch (SQLException | StorageQueryException | StorageTransactionLogicException e) {
                if (tx != null) {
                    // the user can't rollback a transaction. So we can do this
                    // without checking for tx.isActive()
                    tx.rollback();
                }
                throw e;
            }
        }
    }

}
