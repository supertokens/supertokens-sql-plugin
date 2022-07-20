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

import io.supertokens.storage.sql.Start;
import io.supertokens.storage.sql.config.DatabaseConfig;
import io.supertokens.storage.sql.domainobject.emailpassword.EmailPasswordUsersDO;
import io.supertokens.storage.sql.domainobject.emailpassword.PasswordResetTokensDO;
import io.supertokens.storage.sql.domainobject.general.AllAuthRecipeUsersDO;
import io.supertokens.storage.sql.domainobject.general.KeyValueDO;
import io.supertokens.storage.sql.domainobject.thirdparty.ThirdPartyUsersDO;
import io.supertokens.storage.sql.domainobject.useridmapping.UserIdMappingDO;
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
import java.util.Properties;

public class HibernateUtils {

    public static SessionFactory initSessionFactory(DatabaseConfig databaseConfig, DataSource dataSource) {
        final StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(properties(databaseConfig, dataSource)).build();

        final MetadataSources metadataSources = new MetadataSources(serviceRegistry);

        for (Class<?> entity : entities()) {
            metadataSources.addAnnotatedClass(entity);
        }

        final Metadata metadata = metadataSources.getMetadataBuilder().enableNewIdentifierGeneratorSupport(true)
                .applyPhysicalNamingStrategy(physicalNamingStrategy(databaseConfig)).build();

        final SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();

        SessionFactory sessionFactory = sessionFactoryBuilder.build();

        return sessionFactory;
    }

    private static Class<?>[] entities() {
        return new Class[] { KeyValueDO.class, AllAuthRecipeUsersDO.class, EmailPasswordUsersDO.class,
                PasswordResetTokensDO.class, ThirdPartyUsersDO.class, UserIdMappingDO.class };
    }

    @NotNull
    private static PhysicalNamingStrategy physicalNamingStrategy(DatabaseConfig databaseConfig) {
        return new CustomNamingStrategy(databaseConfig);
    }

    private static Properties properties(DatabaseConfig databaseConfig, DataSource dataSource) {
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
        properties.put(Environment.DATASOURCE, dataSource);

        // Uncomment the below if you want to see SQL queries
        if (Start.printSQL) {
            // is true during certain tests.
            properties.put(Environment.SHOW_SQL, true);
        }
//        properties.put(Environment.SHOW_SQL, true);
//        properties.put(Environment.FORMAT_SQL, true);

        return properties;
    }

}
