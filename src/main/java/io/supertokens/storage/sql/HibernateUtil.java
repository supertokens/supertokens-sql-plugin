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

import io.supertokens.storage.sql.config.Config;
import io.supertokens.storage.sql.config.SQLConfig;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessCodesDO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessDevicesDO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessUsersDO;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.service.spi.ServiceException;

import java.util.HashMap;
import java.util.Map;

public class HibernateUtil {

    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory(Start start) {
        if (sessionFactory == null || sessionFactory.isClosed()) {

            if (!start.enabled) {
                throw new RuntimeException("Connection refused"); // emulates exception thrown by Hikari
            }

            try {

                // build connection url
                SQLConfig userConfig = Config.getConfig(start);
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

                String connectionUrl = "jdbc:" + scheme + "://" + hostName + port + "/" + databaseName + attributes;

                StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();

                Map<String, Object> settings = new HashMap<>();
                settings.put(Environment.DRIVER, "org.mariadb.jdbc.Driver");
                settings.put(Environment.URL, connectionUrl);

                if (userConfig.getUser() != null) {
                    settings.put(Environment.USER, userConfig.getUser());

                }

                if (userConfig.getPassword() != null && !userConfig.getPassword().equals("")) {
                    settings.put(Environment.PASS, userConfig.getPassword());
                }
                settings.put(Environment.HBM2DDL_AUTO, "none");
                settings.put(Environment.SHOW_SQL, true);

                settings.put("hibernate.physical_naming_strategy", "io.supertokens.storage.sql.CustomNamingStrategy");
                // HikariCP settings

                settings.put("hibernate.hikari.connectionTimeout", "20000");
                settings.put("hibernate.hikari.minimumIdle", "10");
                settings.put("hibernate.hikari.maximumPoolSize", String.valueOf(userConfig.getConnectionPoolSize()));
                settings.put("hibernate.hikari.idleTimeout", "300000");
                settings.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
                // settings.put("hibernate.hikari.cachePrepStmts", "true");
                // settings.put("hibernate.hikari.prepStmtCacheSize", "250");
                // settings.put("hibernate.hikari.prepStmtCacheSqlLimit", "2048");
                // settings.put("hibernate.hikari.poolName", "SuperTokens");

                while (true) {
                    try {
                        registryBuilder.applySettings(settings);

                        registry = registryBuilder.build();
                        MetadataSources sources = new MetadataSources(registry)
                                .addAnnotatedClass(PasswordlessCodesDO.class)
                                .addAnnotatedClass(PasswordlessDevicesDO.class)
                                .addAnnotatedClass(PasswordlessUsersDO.class);

                        Metadata metadata = sources.getMetadataBuilder().build();
                        sessionFactory = metadata.getSessionFactoryBuilder().build();
                        break;
                    } catch (IllegalStateException | ServiceException s) {
                        Thread.sleep(2000);
                    }
                }

            } catch (Exception e) {
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
                e.printStackTrace();
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
