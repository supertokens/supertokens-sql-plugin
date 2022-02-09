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

package io.supertokens.storage.sql.dataaccessobjects.session.impl;

import io.supertokens.storage.sql.HibernateUtil;
import io.supertokens.storage.sql.dataaccessobjects.jwt.impl.JwtSigningDAO;
import io.supertokens.storage.sql.domainobjects.session.SessionAccessTokenSigningKeysDO;
import io.supertokens.storage.sql.test.HibernateUtilTest;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.supertokens.storage.sql.TestConstants.CREATED_AT;
import static io.supertokens.storage.sql.TestConstants.VALUE;
import static org.junit.Assert.*;

public class SessionAccessTokenSigningKeysDAOTest {

    Session session;

    @Before
    public void before() throws InterruptedException {
        session = HibernateUtilTest.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        SessionAccessTokenSigningKeysDAO sessionAccessTokenSigningKeysDAO = new SessionAccessTokenSigningKeysDAO(
                session);
        sessionAccessTokenSigningKeysDAO.removeAll();
        transaction.commit();
    }

    @After
    public void after() throws Exception {
        Transaction transaction = session.beginTransaction();
        SessionAccessTokenSigningKeysDAO sessionAccessTokenSigningKeysDAO = new SessionAccessTokenSigningKeysDAO(
                session);
        sessionAccessTokenSigningKeysDAO.removeAll();
        transaction.commit();
        session.close();
    }

    @Test
    public void insertIntoTableValues() throws Exception {

        SessionAccessTokenSigningKeysDAO sessionAccessTokenSigningKeysDAO = new SessionAccessTokenSigningKeysDAO(
                session);
        Transaction transaction = session.beginTransaction();
        sessionAccessTokenSigningKeysDAO.insertIntoTableValues(CREATED_AT, VALUE);
        transaction.commit();
        assertTrue(sessionAccessTokenSigningKeysDAO.getAll().size() == 1);

    }

    @Test
    public void deleteWhereCreatedAtTimeLessThan() throws Exception {

        SessionAccessTokenSigningKeysDAO sessionAccessTokenSigningKeysDAO = new SessionAccessTokenSigningKeysDAO(
                session);
        Transaction transaction = session.beginTransaction();

        sessionAccessTokenSigningKeysDAO.insertIntoTableValues(CREATED_AT, VALUE);
        sessionAccessTokenSigningKeysDAO.insertIntoTableValues(CREATED_AT + 10l, VALUE);
        sessionAccessTokenSigningKeysDAO.insertIntoTableValues(CREATED_AT + 20l, VALUE);
        transaction.commit();
        session.clear();

        transaction = session.beginTransaction();
        sessionAccessTokenSigningKeysDAO.deleteWhereCreatedAtTimeLessThan(CREATED_AT + 10l);
        transaction.commit();

        assertTrue(sessionAccessTokenSigningKeysDAO.getAll().size() == 2);
    }
}