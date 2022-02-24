/*
 *    Copyright (c) 2021, VRAI Labs and/or its affiliates. All rights reserved.
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
 */

package io.supertokens.storage.sql;

import io.supertokens.pluginInterface.exceptions.QuitProgramFromPluginException;
import io.supertokens.storage.sql.config.Config;
import io.supertokens.storage.sql.config.SQLConfig;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordPswdResetTokensDO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordPswdResetTokensPKDO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordUsersDO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationTokensDO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationTokensPKDO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationVerifiedEmailsDO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationVerifiedEmailsPKDO;
import io.supertokens.storage.sql.domainobjects.general.KeyValueDO;
import io.supertokens.storage.sql.domainobjects.general.UsersDO;
import io.supertokens.storage.sql.domainobjects.jwtsigning.JWTSigningKeysDO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessCodesDO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessDevicesDO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessUsersDO;
import io.supertokens.storage.sql.domainobjects.session.SessionAccessTokenSigningKeysDO;
import io.supertokens.storage.sql.domainobjects.session.SessionInfoDO;
import io.supertokens.storage.sql.domainobjects.thirdparty.ThirdPartyUsersDO;
import io.supertokens.storage.sql.domainobjects.thirdparty.ThirdPartyUsersPKDO;
import io.supertokens.storage.sql.output.Logging;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.service.spi.ServiceException;
import org.hibernate.tool.schema.Action;
import org.mariadb.jdbc.Driver;
import org.hibernate.dialect.MariaDBDialect;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HibernateSessionPool extends ResourceDistributor.SingletonResource {

    private static final String RESOURCE_KEY = "io.supertokens.storage.sql.HibernateSessionPool";

    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

    private static String errorMessage = "Error connecting to SQL instance. Please make sure that SQL is running and that "
            + "you have" + " specified the correct values for ('host' and 'port') or for 'connection_uri'";

    public static synchronized SessionFactory getSessionFactory(Start start) throws InterruptedException {
        return getSessionFactory(start, false);
    }

    /**
     * Load postgres
     *
     */
    public static HashMap<String, Object> getSettings_postgres(Start start) {
        HashMap<String, Object> settings = new HashMap<>();

        settings.put(Environment.DRIVER, "org.postgresql.Driver");
        settings.put(Environment.URL, "jdbc:postgresql://localhost:5432/supertokens");
        settings.put(Environment.USER, "root");
        settings.put(Environment.PASS, "root");
        settings.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        settings.put(Environment.HBM2DDL_AUTO, Action.CREATE_DROP);
        settings.put(Environment.SHOW_SQL, true);

        // settings.put("hibernate.physical_naming_strategy", "io.supertokens.storage.sql.CustomNamingStrategy");
        // HikariCP settings

//        settings.put("hibernate.hikari.connectionTimeout", "20000");
//        settings.put("hibernate.hikari.minimumIdle", "10");
//        settings.put("hibernate.hikari.maximumPoolSize", "16");
//        settings.put("hibernate.hikari.idleTimeout", "300000");
//        settings.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");

        // settings.put("hibernate.hikari.cachePrepStmts", "true");
        // settings.put("hibernate.hikari.prepStmtCacheSize", "250");
        // settings.put("hibernate.hikari.prepStmtCacheSqlLimit", "2048");
        // settings.put("hibernate.hikari.poolName", "SuperTokens");

        return settings;
    }

    /**
     * Load cockroachDB
     */
    public static HashMap<String, Object> getSettings_cockroachDB(Start start) {
        HashMap<String, Object> settings = new HashMap<>();

        settings.put(Environment.DRIVER, "org.postgresql.Driver");
        settings.put(Environment.URL, "jdbc:postgresql://localhost:26257/supertokens?sslmode=disable");
        settings.put(Environment.USER, "root");
        settings.put(Environment.PASS, "root");
        settings.put("hibernate.dialect", "org.hibernate.dialect.CockroachDB201Dialect");
        settings.put(Environment.HBM2DDL_AUTO, Action.CREATE_DROP);
        settings.put(Environment.SHOW_SQL, true);

        // settings.put("hibernate.physical_naming_strategy", "io.supertokens.storage.sql.CustomNamingStrategy");
        // HikariCP settings

//        settings.put("hibernate.hikari.connectionTimeout", "20000");
//        settings.put("hibernate.hikari.minimumIdle", "10");
//        settings.put("hibernate.hikari.maximumPoolSize", "16");
//        settings.put("hibernate.hikari.idleTimeout", "300000");
//        settings.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");

        // settings.put("hibernate.hikari.cachePrepStmts", "true");
        // settings.put("hibernate.hikari.prepStmtCacheSize", "250");
        // settings.put("hibernate.hikari.prepStmtCacheSqlLimit", "2048");
        // settings.put("hibernate.hikari.poolName", "SuperTokens");

        return settings;
    }

    /**
     * Load non in-memory settings
     * 
     * @param start
     * @return
     */
    public static HashMap<String, Object> getSettings(Start start) {
        HashMap<String, Object> settings = new HashMap<>();

        SQLConfig userConfig = Config.getConfig(start);
        String connectionUrl = null;

        if (userConfig.getConnectionURI() != null) {

            connectionUrl = userConfig.getConnectionURI();

        } else {

            // build connection url
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

            connectionUrl = "jdbc:" + scheme + "://" + hostName + port + "/" + databaseName + attributes;

        }

        settings.put(Environment.URL, connectionUrl);

        if (userConfig.getUser() != null) {
            settings.put(Environment.USER, userConfig.getUser());

        }

        if (userConfig.getPassword() != null && !userConfig.getPassword().equals("")) {
            settings.put(Environment.PASS, userConfig.getPassword());
        }
        settings.put(Environment.HBM2DDL_AUTO, Action.CREATE_DROP);
        settings.put(Environment.SHOW_SQL, true);
        settings.put(Environment.DRIVER, userConfig.getSql_driver());

        // settings.put("hibernate.physical_naming_strategy", "io.supertokens.storage.sql.CustomNamingStrategy");
        // HikariCP settings

        settings.put("hibernate.hikari.connectionTimeout", "20000");
        settings.put("hibernate.hikari.minimumIdle", "10");
        settings.put("hibernate.hikari.maximumPoolSize", String.valueOf(userConfig.getConnectionPoolSize()));
        settings.put("hibernate.hikari.idleTimeout", "300000");
        settings.put("hibernate.dialect", userConfig.getSql_driver());

        // settings.put("hibernate.hikari.cachePrepStmts", "true");
        // settings.put("hibernate.hikari.prepStmtCacheSize", "250");
        // settings.put("hibernate.hikari.prepStmtCacheSqlLimit", "2048");
        // settings.put("hibernate.hikari.poolName", "SuperTokens");

        return settings;
    }

    /**
     * Load in memory settings
     * 
     * @return
     */

    public static HashMap<String, Object> getInMemorySettings() {
        HashMap<String, Object> settings = new HashMap<>();

        settings.put(Environment.DRIVER, "org.hsqldb.jdbcDriver");
        settings.put(Environment.URL, "jdbc:hsqldb:mem:supertokens");
        settings.put(Environment.USER, "sa");
        settings.put(Environment.PASS, "");

        settings.put(Environment.HBM2DDL_AUTO, Action.CREATE_DROP);
        settings.put(Environment.SHOW_SQL, true);

        settings.put("hibernate.physical_naming_strategy", "io.supertokens.storage.sql.CustomNamingStrategy");
        // HikariCP settings

//        settings.put("hibernate.hikari.connectionTimeout", "20000");
//        settings.put("hibernate.hikari.minimumIdle", "10");
//        settings.put("hibernate.hikari.maximumPoolSize", "5");
//        settings.put("hibernate.hikari.idleTimeout", "300000");
        settings.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");

        return settings;
    }

    /**
     * Verifying if the start object is configured correctly
     * 
     * @param start
     */
    private static void verifyStartEnvironment(Start start) {
        if (!start.enabled) {
            throw new RuntimeException("Connection refused");
        }
    }

    /**
     * set time to wait for the connection to be estabilished
     * 
     * @param start
     * @return
     */
    private static int getTimeToWaitToInit(Start start) {
        int actualValue = 3600 * 1000;
        if (Start.isTesting) {
            Integer testValue = ConnectionPoolTestContent.getInstance(start)
                    .getValue(ConnectionPoolTestContent.TIME_TO_WAIT_TO_INIT);
            return Objects.requireNonNullElse(testValue, actualValue);
        }
        return actualValue;
    }

    public static SessionFactory getSessionFactory(Start start, boolean inMemory) throws InterruptedException {

        verifyStartEnvironment(start);

        // Sanity check on the session factor object
        if (sessionFactory == null || sessionFactory.isClosed()) {

            Logging.info(start, "Setting up SQL connection pool.");

            long maxTryTime = System.currentTimeMillis() + getTimeToWaitToInit(start);
            boolean longMessagePrinted = false;
            try {
                while (true) {
                    try {
                        StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();

                        HashMap<String, Object> settings = null;
                        if (inMemory) {
                            settings = getInMemorySettings();
                        } else {
                            // settings = getSettings(start);
                            // settings = getSettings_postgres(start);
                            settings = getSettings_cockroachDB(start);
                        }

                        registryBuilder.applySettings(settings);

                        registry = registryBuilder.build();
                        MetadataSources sources = new MetadataSources(registry)
                                .addAnnotatedClass(EmailPasswordPswdResetTokensDO.class)
                                .addAnnotatedClass(EmailPasswordPswdResetTokensPKDO.class)
                                .addAnnotatedClass(EmailPasswordUsersDO.class)
                                .addAnnotatedClass(EmailVerificationTokensDO.class)
                                .addAnnotatedClass(EmailVerificationTokensPKDO.class)
                                .addAnnotatedClass(EmailVerificationVerifiedEmailsDO.class)
                                .addAnnotatedClass(EmailVerificationVerifiedEmailsPKDO.class)

                                .addAnnotatedClass(PasswordlessCodesDO.class)
                                .addAnnotatedClass(PasswordlessDevicesDO.class)
                                .addAnnotatedClass(PasswordlessUsersDO.class)

                                .addAnnotatedClass(UsersDO.class).addAnnotatedClass(KeyValueDO.class)

                                .addAnnotatedClass(JWTSigningKeysDO.class)

                                .addAnnotatedClass(SessionAccessTokenSigningKeysDO.class)
                                .addAnnotatedClass(SessionInfoDO.class)

                                .addAnnotatedClass(ThirdPartyUsersDO.class)
                                .addAnnotatedClass(ThirdPartyUsersPKDO.class);

                        Metadata metadata = sources.getMetadataBuilder().build();
                        sessionFactory = metadata.getSessionFactoryBuilder().build();
                        break;
                    } catch (Exception e) {

                        if (e.getMessage().contains("Connection refused")) {
                            retryRefusedConnection(start, maxTryTime, longMessagePrinted);
                        } else {
                            throw e;
                        }

                    }
                }
            } finally {
                start.removeShutdownHook();
            }
        }
        return sessionFactory;
    }

    /**
     * retry logic when connection to db has been refused
     * 
     * @param start
     * @param maxTryTime
     * @param longMessagePrinted
     */
    public static void retryRefusedConnection(Start start, long maxTryTime, boolean longMessagePrinted) {
        start.handleKillSignalForWhenItHappens();

        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
        sessionFactory = null;

        if (System.currentTimeMillis() > maxTryTime) {
            throw new QuitProgramFromPluginException(errorMessage);
        }

        if (!longMessagePrinted) {
            longMessagePrinted = true;
            Logging.info(start, errorMessage);
        }

        double minsRemaining = (maxTryTime - System.currentTimeMillis()) / (1000.0 * 60);
        NumberFormat formatter = new DecimalFormat("#0.0");
        Logging.info(start, "Trying again in a few seconds for " + formatter.format(minsRemaining) + " mins...");
        try {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            Thread.sleep(getRetryIntervalIfInitFails(start));
        } catch (InterruptedException ex) {
            throw new QuitProgramFromPluginException(errorMessage);
        }
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

    public static void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
