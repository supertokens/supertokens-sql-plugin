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

package io.supertokens.storage.sql.dataaccessobjects.emailverification.impl;

import io.supertokens.pluginInterface.sqlStorage.SessionObject;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationVerifiedEmailsDO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationVerifiedEmailsPKDO;
import io.supertokens.storage.sql.test.HibernateUtilTest;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.NoResultException;

import static io.supertokens.storage.sql.TestConstants.EMAIL;
import static io.supertokens.storage.sql.TestConstants.USER_ID;
import static org.junit.Assert.*;

public class EmailverificationVerifiedEmailsDAOTest {

    Session session;
    SessionObject sessionObject;
    EmailverificationVerifiedEmailsDAO emailverificationVerifiedEmailsDAO;

    @Before
    public void beforeTest() throws InterruptedException {
        session = HibernateUtilTest.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        sessionObject = new SessionObject(session);
        emailverificationVerifiedEmailsDAO = new EmailverificationVerifiedEmailsDAO(sessionObject);
        emailverificationVerifiedEmailsDAO.removeAll();
        transaction.commit();
    }

    @After
    public void afterTest() throws InterruptedException {
        Transaction transaction = session.beginTransaction();
        emailverificationVerifiedEmailsDAO.removeAll();
        transaction.commit();
        session.close();
    }

    @Test
    public void insertIntoTable() {

        Transaction transaction = session.beginTransaction();

        EmailVerificationVerifiedEmailsPKDO pkdo = (EmailVerificationVerifiedEmailsPKDO) emailverificationVerifiedEmailsDAO
                .insertIntoTable(USER_ID, EMAIL);
        transaction.commit();

        assertTrue(pkdo.getEmail().equals(EMAIL));
        assertTrue(pkdo.getUser_id().equals(USER_ID));

    }

    @Test
    public void insertIntoTableException() {

        Transaction transaction = session.beginTransaction();

        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL);

        try {
            emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            assertTrue(e instanceof NonUniqueObjectException);
            return;
        }
        fail();

    }

    @Test
    public void deleteFromTableWhereUserIdEqualsAndEmailEquals() throws Exception {
        Transaction transaction = session.beginTransaction();

        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL);
        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL + "two");
        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID + "two", EMAIL);

        emailverificationVerifiedEmailsDAO.deleteFromTableWhereUserIdEqualsAndEmailEquals(USER_ID, EMAIL);
        assertTrue(emailverificationVerifiedEmailsDAO.getAll().size() == 2);
        transaction.commit();

    }

    @Test
    public void deleteFromTableWhereUserIdEqualsAndEmailException() throws Exception {

        Transaction transaction = session.beginTransaction();
        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL);
        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL + "two");
        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID + "two", EMAIL);

        assertEquals(emailverificationVerifiedEmailsDAO
                .deleteFromTableWhereUserIdEqualsAndEmailEquals(USER_ID + "three", EMAIL + "two"), 0);
        transaction.commit();

    }

    @Test
    public void getWhereUserIdEqualsAndEmailEquals() {
        Transaction transaction = session.beginTransaction();
        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL);

        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID + "two", EMAIL);
        transaction.commit();

        EmailVerificationVerifiedEmailsDO emailVerificationVerifiedEmailsDO = emailverificationVerifiedEmailsDAO
                .getWhereUserIdEqualsAndEmailEquals(USER_ID, EMAIL);

        assertTrue(emailVerificationVerifiedEmailsDO != null);
        assertTrue(emailVerificationVerifiedEmailsDO.getPrimary_key().getEmail().equals(EMAIL));
        assertTrue(emailVerificationVerifiedEmailsDO.getPrimary_key().getUser_id().equals(USER_ID));

    }

    @Test
    public void getWhereUserIdEqualsAndEmailEqualsException() {
        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL);

        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID + "two", EMAIL);

        EmailVerificationVerifiedEmailsDO emailVerificationVerifiedEmailsDO = emailverificationVerifiedEmailsDAO
                .getWhereUserIdEqualsAndEmailEquals(USER_ID + "three", EMAIL);

        assertEquals(emailVerificationVerifiedEmailsDO, null);

    }

    @Test
    public void deleteWhereUserIdEquals() throws Exception {
        Transaction transaction = session.beginTransaction();
        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL);

        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID + "two", EMAIL);

        emailverificationVerifiedEmailsDAO.deleteWhereUserIdEquals(USER_ID + "two");
        assertTrue(emailverificationVerifiedEmailsDAO.getAll().size() == 1);

        transaction.commit();
    }

    @Test
    public void deleteWhereUserIdEqualsException() throws Exception {
        Transaction transaction = session.beginTransaction();
        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL);

        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID + "two", EMAIL);

        assertEquals(emailverificationVerifiedEmailsDAO.deleteWhereUserIdEquals(USER_ID + "twoe"), 0);

        transaction.commit();
    }
}