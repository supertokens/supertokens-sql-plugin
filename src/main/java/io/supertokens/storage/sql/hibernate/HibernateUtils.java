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

package io.supertokens.storage.sql.hibernate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.supertokens.storage.sql.config.DatabaseConfig;
import io.supertokens.storage.sql.domainobject.general.KeyValueDO;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class HibernateUtils {

    private static final List<Closeable> CLOSEABLES = new ArrayList<>();

    public static SessionFactory sessionFactory(DatabaseConfig databaseConfig) {
        return newSessionFactory(databaseConfig);
    }

    private static Class<?>[] entities() {
        return new Class[] { KeyValueDO.class };
    }

    private static SessionFactory newSessionFactory(DatabaseConfig databaseConfig) {

        final StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(properties(databaseConfig)).build();

        final MetadataSources metadataSources = new MetadataSources(serviceRegistry);

        for (Class<?> entity : entities()) {
            metadataSources.addAnnotatedClass(entity);
        }

        final Metadata metadata = metadataSources.getMetadataBuilder().enableNewIdentifierGeneratorSupport(true)
                .applyPhysicalNamingStrategy(physicalNamingStrategy(databaseConfig)).build();

        final SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();

        SessionFactory sessionFactory = sessionFactoryBuilder.build();

        CLOSEABLES.add(sessionFactory);

        return sessionFactory;
    }

    @NotNull
    private static PhysicalNamingStrategy physicalNamingStrategy(DatabaseConfig databaseConfig) {
        return new CustomNamingStrategy(databaseConfig);
    }

    private static Properties properties(DatabaseConfig databaseConfig) {
        Properties properties = new Properties();
        // TODO: sql-plugin: take from config or let hibernate figure out what driver is needed
        // TODO: sql-plugin -> even if I give MySQLDialect when using postgres, tests still pass. Is this done

        // it has a mechanism to choose driver based on an open connection
        if (databaseConfig.getDialect() != null) {
            // TODO: sql-plugin: do we need a validate function to check if the dialect passed exists or not
            // like invoke a Class.forName() and if it throws an exception we do a quit program exception,
            // or do we let hibernate handle if a dialect does not exist ?
            // this basically might be a measure against spelling mistakes or such
            properties.put(Environment.DIALECT, databaseConfig.getDialect());
        }

        // data source settings
        DataSource dataSource = dataSource(databaseConfig);
        properties.put(Environment.DATASOURCE, dataSource);

        return properties;
    }

    private static DataSource dataSource(DatabaseConfig databaseConfig) {
        HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig(databaseConfig));

        CLOSEABLES.add(hikariDataSource);

        return hikariDataSource;
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

    public static void close() {
        for (Closeable CLOSEABLE : CLOSEABLES) {
            try {
                CLOSEABLE.close();
            } catch (IOException ignored) {
                // as of now the two closable we have (SessionFactory and HikariDataSource)
                // do not throw IOException in their implementations
            }
        }
    }
}
