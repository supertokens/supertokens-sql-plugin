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
import io.supertokens.storage.sql.output.Logging;
import io.supertokens.storage.sql.utils.Utils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.internal.SessionImpl;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;

public class ConnectionPool extends ResourceDistributor.SingletonResource {

    private static final String RESOURCE_KEY = "io.supertokens.storage.sql.ConnectionPool";
    private final SessionFactory sessionFactory;

    private ConnectionPool(Start start) {
        if (!start.enabled) {
            throw new RuntimeException("Connection to refused"); // emulates exception thrown by Hikari
        }

        PostgreSQLConfig userConfig = Config.getConfig(start);

        String scheme = userConfig.getConnectionScheme();

        String hostName = userConfig.getHostName();

        String port = userConfig.getPort() + "";
        if (!port.equals("-1")) {
            port = ":" + port;
        } else {
            port = "";
        }

        String databaseName = userConfig.getDatabaseName();

        String attributes = userConfig.getConnectionAttributes();
        if (!attributes.equals("")) {
            attributes = "?" + attributes;
        }

        String connectionURI = "jdbc:" + scheme + "://" + hostName + port + "/" + databaseName + attributes;

        ///////////////////////////////////////////////////////////////////
        // Creating Hibernate connection pool..
        // TODO: sql-plugin -> there is a way to use hikari with Hibarnate. Should we use that?
        StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();

        // TODO: sql-plugin -> choose the right driver based on actual config
        registryBuilder.applySetting(Environment.DRIVER, "org.postgresql.Driver");

        // TODO: sql-plugin -> chose the right dialect based on the db.
        registryBuilder.applySetting(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");

        // TODO: sql-plugin -> is this the right type to give? If this is not there, then getSession throws an error
        registryBuilder.applySetting(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");

        registryBuilder.applySetting(Environment.URL, connectionURI);
        if (userConfig.getUser() != null) {
            registryBuilder.applySetting(Environment.USER, userConfig.getUser());
        }
        if (userConfig.getPassword() != null && !userConfig.getPassword().equals("")) {
            registryBuilder.applySetting(Environment.PASS, userConfig.getPassword());
        }

        registryBuilder.applySetting(Environment.POOL_SIZE, userConfig.getConnectionPoolSize());
        // TODO: sql-plugin -> add connection timeout somehow

        sessionFactory = new MetadataSources(registryBuilder.build()).buildMetadata().buildSessionFactory();
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

    private static boolean hibernateFailedToConnect(Exception e) {
        return Utils.isExceptionCause(ConnectException.class, e);
    }

    public interface WithConnection<T> {
        T op(Connection con) throws SQLException, StorageQueryException;
    }

    public interface WithConnectionForComplexTransaction<T> {
        T op(Connection con) throws SQLException, StorageQueryException, StorageTransactionLogicException;
    }

    public static <T> T withConnection(Start start, WithConnection<T> func) throws SQLException, StorageQueryException {
        try {
            return withConnectionForComplexTransaction(start, null, func::op);
        } catch (StorageTransactionLogicException e) {
            throw new SQLException("Should never come here");
        }
    }

    public static <T> T withConnectionForComplexTransaction(Start start,
            SQLStorage.TransactionIsolationLevel isolationLevel, WithConnectionForComplexTransaction<T> func)
            throws SQLException, StorageTransactionLogicException, StorageQueryException {
        if (getInstance(start) == null) {
            throw new QuitProgramFromPluginException("Please call initPool before getConnection");
        }
        if (!start.enabled) {
            throw new SQLException("Storage layer disabled");
        }

        SessionFactory sessionFactory = getInstance(start).sessionFactory;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();

                // we do not use try-with resource for Connection below cause we close
                // the entire Session itself.
                Connection con = ((SessionImpl) session.getSession()).connection();

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
                    // TODO: sql-plugin -> Previously we used to store the defualt isolation level and then restore it
                    // in the connection. But I think that's not needed. Is this correct?
                    con.setTransactionIsolation(libIsolationLevel);
                }
                T result = func.op(con);
                tx.commit();
                return result;
            } catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }

    static void close(Start start) {
        if (getInstance(start) == null) {
            return;
        }
        getInstance(start).sessionFactory.close();
    }
}
