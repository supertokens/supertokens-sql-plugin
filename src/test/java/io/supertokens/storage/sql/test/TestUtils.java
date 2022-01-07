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

import io.supertokens.storage.sql.CustomNamingStrategy;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;

public class TestUtils {

    public static void truncate(SessionFactory sessionFactory, String tableName) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.createSQLQuery("truncate table " + tableName).executeUpdate();
        transaction.commit();
        session.close();
    }

    public static String getTableName(SessionFactory sessionFactory, Class tableClass) {
        ClassMetadata hibernateMetadata = sessionFactory.getClassMetadata(tableClass);

        if (hibernateMetadata != null && hibernateMetadata instanceof AbstractEntityPersister) {
            AbstractEntityPersister persister = (AbstractEntityPersister) hibernateMetadata;
            return persister.getTableName();
        }
        return null;
    }
}
