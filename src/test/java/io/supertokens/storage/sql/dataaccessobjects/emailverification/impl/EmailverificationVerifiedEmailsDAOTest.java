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

import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationVerifiedEmailsDO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationVerifiedEmailsPKDO;
import io.supertokens.storage.sql.exceptions.UserAndEmailNotFoundException;
import io.supertokens.storage.sql.test.HibernateUtilTest;
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

    private static EmailverificationVerifiedEmailsDAO emailverificationVerifiedEmailsDAO;

    @BeforeClass
    public static void setUp() throws Exception {
        emailverificationVerifiedEmailsDAO = new EmailverificationVerifiedEmailsDAO(
                HibernateUtilTest.getSessionFactory());
    }

    @Before
    public void beforeTest() {
        emailverificationVerifiedEmailsDAO.removeAll();
    }

    @After
    public void afterTest() {
        emailverificationVerifiedEmailsDAO.removeAll();
    }

    @Test
    public void insertIntoTable() {

        EmailVerificationVerifiedEmailsPKDO pkdo = (EmailVerificationVerifiedEmailsPKDO) emailverificationVerifiedEmailsDAO
                .insertIntoTable(USER_ID, EMAIL);

        assertTrue(pkdo.getEmail().equals(EMAIL));
        assertTrue(pkdo.getUser_id().equals(USER_ID));

    }

    @Test
    public void insertIntoTableException() {

        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL);

        try {
            emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ConstraintViolationException);
            return;
        }
        fail();

    }

    @Test
    public void deleteFromTableWhereUserIdEqualsAndEmailEquals() throws Exception {

        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL);
        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL + "two");
        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID + "two", EMAIL);

        emailverificationVerifiedEmailsDAO.deleteFromTableWhereUserIdEqualsAndEmailEquals(USER_ID, EMAIL);

        assertTrue(emailverificationVerifiedEmailsDAO.getAll().size() == 2);
    }

    @Test
    public void deleteFromTableWhereUserIdEqualsAndEmailException() throws Exception {

        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL);
        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL + "two");
        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID + "two", EMAIL);

        try {
            emailverificationVerifiedEmailsDAO.deleteFromTableWhereUserIdEqualsAndEmailEquals(USER_ID + "three",
                    EMAIL + "two");
        } catch (UserAndEmailNotFoundException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing, failure case
        }
        fail();

    }

    @Test
    public void getWhereUserIdEqualsAndEmailEquals() {
        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL);

        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID + "two", EMAIL);

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

        try {
            EmailVerificationVerifiedEmailsDO emailVerificationVerifiedEmailsDO = emailverificationVerifiedEmailsDAO
                    .getWhereUserIdEqualsAndEmailEquals(USER_ID + "three", EMAIL);
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();
    }

    @Test
    public void deleteWhereUserIdEquals() throws Exception {
        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL);

        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID + "two", EMAIL);

        emailverificationVerifiedEmailsDAO.deleteWhereUserIdEquals(USER_ID + "two");
        assertTrue(emailverificationVerifiedEmailsDAO.getAll().size() == 1);
    }

    @Test
    public void deleteWhereUserIdEqualsException() throws Exception {
        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID, EMAIL);

        emailverificationVerifiedEmailsDAO.insertIntoTable(USER_ID + "two", EMAIL);

        try {
            emailverificationVerifiedEmailsDAO.deleteWhereUserIdEquals(USER_ID + "twoe");
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();
    }
}