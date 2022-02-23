/*
 *    Copyright (c) 2022, VRAI Labs and/or its affiliates. All rights reserved.
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

package io.supertokens.storage.sql.test;

import io.supertokens.pluginInterface.exceptions.QuitProgramFromPluginException;
import io.supertokens.storage.sql.ConnectionPoolTestContent;
import io.supertokens.storage.sql.Start;
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
import org.hibernate.tool.schema.Action;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HibernateUtilTest {
    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() throws InterruptedException {
        if (sessionFactory == null) {
            int trial_attempts = 5;
            long waitTime = 5000;
            while (true) {
                try {
                    Map<String, Object> settings = new HashMap<>();

                    settings.put(Environment.DRIVER, "org.postgresql.Driver");
                    settings.put(Environment.URL, "jdbc:postgresql://localhost:26257/supertokens?sslmode=disable");
                    settings.put(Environment.USER, "root");
                    settings.put(Environment.PASS, "root");
                    settings.put("hibernate.dialect", "org.hibernate.dialect.CockroachDB201Dialect");
                    settings.put(Environment.HBM2DDL_AUTO, Action.CREATE_DROP);
                    settings.put(Environment.SHOW_SQL, true);

                    // TODO: to be changed later to an in memory version
//                    settings.put(Environment.DRIVER, "org.mariadb.jdbc.Driver");
//                    settings.put(Environment.URL, "jdbc:mysql://localhost:3306/supertokens");
//                    settings.put(Environment.USER, "root");
//                    settings.put(Environment.PASS, "root");
//                    settings.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
//
//                    settings.put(Environment.HBM2DDL_AUTO, Action.CREATE_DROP);
//                    settings.put(Environment.SHOW_SQL, true);
//
//                    settings.put("hibernate.physical_naming_strategy",
//                            "io.supertokens.storage.sql.CustomNamingStrategy");
//                    // HikariCP settings
//
//                    settings.put("hibernate.hikari.connectionTimeout", "20000");
//                    settings.put("hibernate.hikari.minimumIdle", "10");
//                    settings.put("hibernate.hikari.maximumPoolSize", "5");
//                    settings.put("hibernate.hikari.idleTimeout", "300000");

                    // Create registry
                    StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
                    registryBuilder.applySettings(settings);

                    registry = registryBuilder.build();

                    // Create MetadataSources
                    MetadataSources sources = new MetadataSources(registry)
                            .addAnnotatedClass(EmailPasswordPswdResetTokensDO.class)
                            .addAnnotatedClass(EmailPasswordPswdResetTokensPKDO.class)
                            .addAnnotatedClass(EmailPasswordUsersDO.class)
                            .addAnnotatedClass(EmailVerificationTokensDO.class)
                            .addAnnotatedClass(EmailVerificationTokensPKDO.class)
                            .addAnnotatedClass(EmailVerificationVerifiedEmailsDO.class)
                            .addAnnotatedClass(EmailVerificationVerifiedEmailsPKDO.class)

                            .addAnnotatedClass(PasswordlessCodesDO.class).addAnnotatedClass(PasswordlessDevicesDO.class)
                            .addAnnotatedClass(PasswordlessUsersDO.class)

                            .addAnnotatedClass(UsersDO.class).addAnnotatedClass(KeyValueDO.class)

                            .addAnnotatedClass(JWTSigningKeysDO.class)

                            .addAnnotatedClass(SessionAccessTokenSigningKeysDO.class)
                            .addAnnotatedClass(SessionInfoDO.class)

                            .addAnnotatedClass(ThirdPartyUsersDO.class).addAnnotatedClass(ThirdPartyUsersPKDO.class);

                    // Create Metadata
                    Metadata metadata = sources.getMetadataBuilder().build();

                    // Create SessionFactory
                    sessionFactory = metadata.getSessionFactoryBuilder().build();

                    break;
                } catch (Exception e) {
                    if (registry != null) {
                        StandardServiceRegistryBuilder.destroy(registry);
                    }

                    if (e.getMessage().contains("Connection refused")) {
                        if (trial_attempts == 0) {
                            throw e;
                        }

                        System.out.println("Connection refused, retrying....");
                        trial_attempts--;
                        Thread.sleep(waitTime);
                    } else {
                        throw e;
                    }
                }
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}

/**
 * Possible configurations
 */

//<!--        <property name="connection.url">jdbc:mysql://localhost:3306/supertokens</property>-->
//<!--        <property name="dialect">org.hibernate.dialect.MySQL8Dialect</property>-->
//<!--        <property name="connection.username">root</property>-->
//<!--        <property name="connection.password">root</property>-->
//<!--        <property name="hibernate.physical_naming_strategy">io.supertokens.storage.sql.CustomNamingStrategy</property>-->
//
//<!-- JDBC Database connection settings -->
//<!--        <property name="connection.driver_class">org.hsqldb.jdbcDriver</property>-->
//<!--        <property name="connection.url">jdbc:hsqldb:mem:supertokens</property>-->
//<!--        <property name="connection.username">sa</property>-->
//<!--        <property name="connection.password"></property>-->
//<!--        &lt;!&ndash; JDBC connection pool settings ... using built-in test pool &ndash;&gt;-->
//<!--        <property name="connection.pool_size">1</property>-->
//<!--        &lt;!&ndash; Select our SQL dialect &ndash;&gt;-->
//<!--        <property name="dialect">org.hibernate.dialect.HSQLDialect</property>-->
//<!-- Echo the SQL to stdout -->

//<property name="show_sql">true</property>
//<!-- Set the current session context -->
//<property name="current_session_context_class">thread</property>
//<!-- Drop and re-create the database schema on startup -->
//<property name="hibernate.hbm2ddl.auto">create-drop</property>
//<!-- dbcp connection pool configuration -->
//<property name="hibernate.dbcp.initialSize">5</property>
//<property name="hibernate.dbcp.maxTotal">20</property>
//<property name="hibernate.dbcp.maxIdle">10</property>
//<property name="hibernate.dbcp.minIdle">5</property>
//<property name="hibernate.dbcp.maxWaitMillis">-1</property>
