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

package io.supertokens.storage.sql.dataaccessobjects.jwt.impl;

import io.supertokens.pluginInterface.sqlStorage.SessionObject;
import io.supertokens.storage.sql.domainobjects.jwtsigning.JWTSigningKeysDO;
import io.supertokens.storage.sql.test.HibernateUtilTest;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.PersistenceException;
import java.util.List;

import static io.supertokens.storage.sql.TestConstants.KEY;
import static io.supertokens.storage.sql.TestConstants.KEY_STRING;
import static io.supertokens.storage.sql.TestConstants.ALGORITHM;
import static io.supertokens.storage.sql.TestConstants.CREATED_AT;
import static org.junit.Assert.*;

public class JwtSigningDAOTest {

    JwtSigningDAO jwtSigningDAO;
    Session session;
    SessionObject sessionObject;

    @Before
    public void before() throws Exception {
        session = HibernateUtilTest.getSessionFactory().openSession();
        sessionObject = new SessionObject(session);
        jwtSigningDAO = new JwtSigningDAO(sessionObject);
        Transaction transaction = session.beginTransaction();
        jwtSigningDAO.removeAll();
        transaction.commit();
    }

    @After
    public void after() throws Exception {
        Transaction transaction = session.beginTransaction();
        jwtSigningDAO.removeAll();
        transaction.commit();
        session.close();
    }

    @Test
    public void insert() throws Exception {
        Transaction transaction = session.beginTransaction();
        jwtSigningDAO.insert(KEY, KEY_STRING, ALGORITHM, CREATED_AT);
        transaction.commit();
        assertTrue(jwtSigningDAO.getAll().size() == 1);

    }

    @Test
    public void insertException() throws Exception {
        Transaction transaction = session.beginTransaction();

        jwtSigningDAO.insert(KEY, KEY_STRING, ALGORITHM, CREATED_AT);

        try {
            jwtSigningDAO.insert(KEY, KEY_STRING, ALGORITHM, CREATED_AT);
            transaction.commit();
        } catch (PersistenceException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            assertTrue(e instanceof NonUniqueObjectException);
            return;
        } catch (Exception e) {
            // do nothing, failure case scenario
        }
        fail();

    }

    @Test
    public void getAllOrderByCreatedAtDesc_locked() {
        Transaction transaction = session.beginTransaction();

        jwtSigningDAO.insert(KEY, KEY_STRING, ALGORITHM, CREATED_AT);
        jwtSigningDAO.insert(KEY + "1", KEY_STRING, ALGORITHM, CREATED_AT + 10l);
        jwtSigningDAO.insert(KEY + "2", KEY_STRING, ALGORITHM, CREATED_AT + 20l);
        jwtSigningDAO.insert(KEY + "3", KEY_STRING, ALGORITHM, CREATED_AT + 30l);
        transaction.commit();

        transaction = session.beginTransaction();
        List<JWTSigningKeysDO> list = jwtSigningDAO.getAllOrderByCreatedAtDesc_locked();
        assertTrue(list.size() == 4);
        assertTrue(list.get(0).getCreated_at() == 40l);
        assertTrue(list.get(1).getCreated_at() == 30l);
        assertTrue(list.get(2).getCreated_at() == 20l);
        assertTrue(list.get(3).getCreated_at() == 10l);
        transaction.commit();

    }
}