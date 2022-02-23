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

package io.supertokens.storage.sql.dataaccessobjects.general;

import io.supertokens.pluginInterface.sqlStorage.SessionObject;
import io.supertokens.storage.sql.dataaccessobjects.jwt.impl.JwtSigningDAO;
import io.supertokens.storage.sql.test.HibernateUtilTest;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class KeyValueDAOTest {

    KeyValueDAO keyValueDAO;
    Session session;
    SessionObject sessionObject;

    @Before
    public void before() throws Exception {
        session = HibernateUtilTest.getSessionFactory().openSession();
        sessionObject = new SessionObject(session);
        keyValueDAO = new KeyValueDAO(sessionObject);
        Transaction transaction = session.beginTransaction();
        keyValueDAO.removeAll();
        transaction.commit();
    }

    @After
    public void after() throws Exception {
        Transaction transaction = session.beginTransaction();
        keyValueDAO.removeAll();
        transaction.commit();
        session.close();
    }

    @Test
    public void insertIntoValues() {

        assertTrue(keyValueDAO.getAll().size() == 0);
        Transaction transaction = session.beginTransaction();
        keyValueDAO.insertIntoValues("name", "Value", 1l);
        transaction.commit();

        assertTrue(keyValueDAO.getAll().size() == 1);

    }

    @Test
    public void getWherePrimaryKeyEquals() {

        assertTrue(keyValueDAO.getAll().size() == 0);
        Transaction transaction = session.beginTransaction();
        keyValueDAO.insertIntoValues("name", "Value", 1l);
        transaction.commit();

        assertTrue(keyValueDAO.getAll().size() == 1);

        Transaction transaction1 = session.beginTransaction();
        assertNotNull(keyValueDAO.getWhereNameEquals_transaction("name"));
        transaction.commit();
    }
}