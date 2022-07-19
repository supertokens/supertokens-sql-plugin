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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.supertokens.pluginInterface.exceptions.QuitProgramFromPluginException;
import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.pluginInterface.exceptions.StorageTransactionLogicException;
import io.supertokens.pluginInterface.sqlStorage.SQLStorage;
import io.supertokens.storage.sql.config.Config;
import io.supertokens.storage.sql.config.DatabaseConfig;
import io.supertokens.storage.sql.hibernate.CustomSessionWrapper;
import io.supertokens.storage.sql.hibernate.HibernateUtils;
import io.supertokens.storage.sql.output.Logging;
import io.supertokens.storage.sql.utils.Utils;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;

public class ConnectionPool extends ResourceDistributor.SingletonResource {

    private static final String RESOURCE_KEY = "io.supertokens.storage.sql.ConnectionPool";
    private static SessionFactory sessionFactory = null;
    private static HikariDataSource dataSource = null;

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

        final DatabaseConfig config = Config.getConfig(start);
        dataSource = (HikariDataSource) dataSource(config);
        sessionFactory = HibernateUtils.initSessionFactory(config, dataSource);
    }

    private static DataSource dataSource(DatabaseConfig databaseConfig) {
        return new HikariDataSource(hikariConfig(databaseConfig));
    }

    @NotNull
    private static HikariConfig hikariConfig(DatabaseConfig databaseConfig) {

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getJdbcUrl(databaseConfig));

        // TODO: sql-plugin -> choose the right driver based on actual config
        // TODO: sql-plugin: do we need a validate function to check if the dialect passed exists or not
        // like invoke a Class.forName() and if it throws an exception we do a quit program exception,
        // or do we let hibernate handle if a dialect does not exist ?
        // this basically might be a measure against spelling mistakes or such
        hikariConfig.setDriverClassName(databaseConfig.getDriverClassName());

        if (databaseConfig.getUser() != null) {
            hikariConfig.setUsername(databaseConfig.getUser());
        }

        if (databaseConfig.getPassword() != null && !databaseConfig.getPassword().equals("")) {
            hikariConfig.setPassword(databaseConfig.getPassword());
        }
        hikariConfig.setMaximumPoolSize(databaseConfig.getConnectionPoolSize());
        hikariConfig.setConnectionTimeout(5000);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        // TODO: set maxLifetimeValue to lesser than 10 mins so that the following error doesnt happen:
        // io.supertokens.storage.postgresql.HikariLoggingAppender.doAppend(HikariLoggingAppender.java:117) |
        // SuperTokens
        // - Failed to validate connection org.mariadb.jdbc.MariaDbConnection@79af83ae (Connection.setNetworkTimeout
        // cannot be called on a closed connection). Possibly consider using a shorter maxLifetime value.
        hikariConfig.setPoolName("SuperTokens");

        return hikariConfig;
    }

    @NotNull
    private static String getJdbcUrl(DatabaseConfig userConfig) {
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

        return "jdbc:" + scheme + "://" + hostName + port + "/" + databaseName + attributes;
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
        ConnectionPool.sessionFactory.close();
        ConnectionPool.sessionFactory = null;
        ConnectionPool.dataSource.close();
        ConnectionPool.dataSource = null;
    }

    private static boolean hibernateFailedToConnect(Exception e) {
        return Utils.isExceptionCause(ConnectException.class, e);
    }

    public interface WithSession<T> {
        T op(CustomSessionWrapper session, Connection con) throws SQLException, StorageQueryException;
    }

    public interface WithSessionForComplexTransaction<T> {
        T op(CustomSessionWrapper session, Connection con)
                throws SQLException, StorageQueryException, StorageTransactionLogicException;

    }

    public static <T> T withSession(Start start, WithSession<T> func, boolean isNonSelectQuery)
            throws SQLException, StorageQueryException {
        if (getInstance(start) == null) {
            throw new QuitProgramFromPluginException("Please call initPool before getConnection");
        }
        if (!start.enabled) {
            throw new SQLException("Should never come here");
        }

        if (isNonSelectQuery) {
            try {
                return withSessionForComplexTransaction(start, null, func::op);
            } catch (StorageTransactionLogicException e) {
                throw new SQLException("Should never come here");
            }
        } else {
            // for SELECT queries
            SessionFactory sessionFactory = ConnectionPool.sessionFactory;
            try (CustomSessionWrapper session = new CustomSessionWrapper(sessionFactory.openSession())) {
                Connection con = session.getSessionImpl().connection();
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
        try (CustomSessionWrapper session = new CustomSessionWrapper(sessionFactory.openSession())) {
            // we assume that these queries will always have a non-SELECT part in them
            // so that's why we always begin a transaction.
            Transaction tx = null;

            // we do not use try-with resource for Connection below cause we close
            // the entire Session itself.
            Connection con = session.getSessionImpl().connection();
            try {
                tx = session.beginTransaction(isolationLevel);
                T result = func.op(session, con);
                if (tx.isActive()) {
                    // maybe the user has already commited the transaction manually.
                    tx.commit();
                }
                return result;
                // when using hibernate
                // exceptions are thrown when we do a tx.commit()
                // hence the addition of PersistenceException in the catch block
            } catch (PersistenceException | SQLException | StorageQueryException | StorageTransactionLogicException e) {
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
