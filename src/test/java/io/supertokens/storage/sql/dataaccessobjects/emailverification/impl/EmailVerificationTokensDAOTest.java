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

import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationTokensDO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationTokensPKDO;
import io.supertokens.storage.sql.exceptions.UserAndEmailNotFoundException;
import io.supertokens.storage.sql.test.HibernateUtilTest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.NoResultException;

import java.util.List;

import static io.supertokens.storage.sql.TestConstants.*;
import static org.junit.Assert.*;

public class EmailVerificationTokensDAOTest {

    private static EmailVerificationTokensDAO emailVerificationTokensDAO;

    @BeforeClass
    public static void setUp() throws Exception {

        emailVerificationTokensDAO = new EmailVerificationTokensDAO(HibernateUtilTest.getSessionFactory());
    }

    @Before
    public void beforeTest() {
        emailVerificationTokensDAO.removeAll();
    }

    @After
    public void afterTest() {
        emailVerificationTokensDAO.removeAll();
    }

    @Test
    public void deleteFromTableWhereTokenExpiryIsLessThan() throws Exception {
        emailVerificationTokensDAO.insertIntoTable(
                USER_ID,
                EMAIL,
                TOKEN,
                TOKEN_EXPIRY
        );
        emailVerificationTokensDAO.insertIntoTable(
                USER_ID + "three",
                EMAIL + "three",
                TOKEN,
                TOKEN_EXPIRY + 10
        );
        emailVerificationTokensDAO.insertIntoTable(
                USER_ID + "two",
                EMAIL + "two",
                TOKEN,
                TOKEN_EXPIRY + 20
        );

        emailVerificationTokensDAO.deleteFromTableWhereTokenExpiryIsLessThan(TOKEN_EXPIRY + 10);
        assertTrue(emailVerificationTokensDAO.getAll().size() == 2);

    }

    @Test
    public void insertIntoTable() throws Exception {

        EmailVerificationTokensPKDO entity = (EmailVerificationTokensPKDO) emailVerificationTokensDAO.insertIntoTable(
                USER_ID,
                EMAIL,
                TOKEN,
                TOKEN_EXPIRY
        );

        assertTrue(entity.getUser_id().equals(USER_ID));
        assertTrue(entity.getToken().equals(TOKEN));
        assertTrue(entity.getEmail().equals(EMAIL));


        assertTrue(emailVerificationTokensDAO.getAll().size() == 1);

    }

    @Test
    public void insertIntoTableException() throws Exception {

        emailVerificationTokensDAO.insertIntoTable(
                USER_ID,
                EMAIL,
                TOKEN,
                TOKEN_EXPIRY
        );

        try {
            emailVerificationTokensDAO.insertIntoTable(
                    USER_ID,
                    EMAIL,
                    TOKEN,
                    TOKEN_EXPIRY
            );
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ConstraintViolationException);
            return;
        }

        fail();
    }

    @Test
    public void create() {
    }

    @Test
    public void get() {
    }

    @Test
    public void getAll() {
    }

    @Test
    public void removeWhereUserIdEquals() {
    }

    @Test
    public void removeAll() {
    }

    @Test
    public void deleteFromTableWhereUserIdEqualsAndEmailEquals() throws Exception {
        emailVerificationTokensDAO.insertIntoTable(
                USER_ID, EMAIL ,TOKEN, TOKEN_EXPIRY
        );

        emailVerificationTokensDAO.insertIntoTable(
                USER_ID + "two", EMAIL ,TOKEN, TOKEN_EXPIRY
        );

        emailVerificationTokensDAO.deleteFromTableWhereUserIdEqualsAndEmailEquals(
                USER_ID, EMAIL
        );

        assertTrue(emailVerificationTokensDAO.getAll().size() == 1);
    }

    @Test
    public void deleteFromTableWhereUserIdEqualsAndEmailEqualsException() throws Exception {
        emailVerificationTokensDAO.insertIntoTable(
                USER_ID, EMAIL ,TOKEN, TOKEN_EXPIRY
        );

        emailVerificationTokensDAO.insertIntoTable(
                USER_ID + "two", EMAIL ,TOKEN, TOKEN_EXPIRY
        );
        try {
            emailVerificationTokensDAO.deleteFromTableWhereUserIdEqualsAndEmailEquals(
                    USER_ID + "three", EMAIL
            );
        } catch (Exception e) {
            assertTrue(e instanceof UserAndEmailNotFoundException);
            return;
        }
        fail();
    }

    @Test
    public void getEmailVerificationTokenWhereTokenEquals() throws NoResultException {
        emailVerificationTokensDAO.insertIntoTable(
                USER_ID, EMAIL ,TOKEN, TOKEN_EXPIRY
        );

        EmailVerificationTokensDO emailVerificationTokensDO =
                emailVerificationTokensDAO.getEmailVerificationTokenWhereTokenEquals(TOKEN);

        assertTrue(emailVerificationTokensDO != null);
        assertTrue(emailVerificationTokensDO.getToken_expiry() == TOKEN_EXPIRY);
        assertTrue(emailVerificationTokensDO.getPrimary_key().getToken().equals(TOKEN));
    }

    @Test
    public void getEmailVerificationTokenWhereTokenEqualsException() {
        emailVerificationTokensDAO.insertIntoTable(
                USER_ID, EMAIL ,TOKEN, TOKEN_EXPIRY
        );

        try {
            EmailVerificationTokensDO emailVerificationTokensDO =
                    emailVerificationTokensDAO.getEmailVerificationTokenWhereTokenEquals(TOKEN + "@");
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            //do nothing failure case
        }
        fail();

    }

    @Test
    public void getEmailVerificationTokenWhereUserIdEqualsAndEmailEquals() {
        emailVerificationTokensDAO.insertIntoTable(
                USER_ID, EMAIL ,TOKEN, TOKEN_EXPIRY
        );
        emailVerificationTokensDAO.insertIntoTable(
                USER_ID + "two", EMAIL ,TOKEN, TOKEN_EXPIRY
        );

        List<EmailVerificationTokensDO> emailVerificationTokensDOList =
                emailVerificationTokensDAO.getLockedEmailVerificationTokenWhereUserIdEqualsAndEmailEquals
                        (
                                USER_ID+"two", EMAIL
                        );

        assertTrue(emailVerificationTokensDOList.size() == 1);

        EmailVerificationTokensDO emailVerificationTokensDO = emailVerificationTokensDOList.get(0);
        assertTrue(emailVerificationTokensDO != null);
        assertTrue(emailVerificationTokensDO.getPrimary_key().getUser_id().equals(USER_ID+"two"));
        assertTrue(emailVerificationTokensDO.getPrimary_key().getEmail().equals(EMAIL));
        assertTrue(emailVerificationTokensDO.getPrimary_key().getToken().equals(TOKEN));
        assertTrue(emailVerificationTokensDO.getToken_expiry() == TOKEN_EXPIRY);


    }


    @Test
    public void getEmailVerificationTokenWhereUserIdEqualsAndEmailEqualsException() {
        emailVerificationTokensDAO.insertIntoTable(
                USER_ID, EMAIL ,TOKEN, TOKEN_EXPIRY
        );
        emailVerificationTokensDAO.insertIntoTable(
                USER_ID + "two", EMAIL ,TOKEN, TOKEN_EXPIRY
        );

        try {
            List<EmailVerificationTokensDO> emailVerificationTokensDO =
                    emailVerificationTokensDAO.getLockedEmailVerificationTokenWhereUserIdEqualsAndEmailEquals
                            (
                                    USER_ID + "three", EMAIL
                            );
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();

    }

    @Test
    public void testGetEmailVerificationTokenWhereUserIdEqualsAndEmailEquals() {
        emailVerificationTokensDAO.insertIntoTable(
                USER_ID, EMAIL ,TOKEN, TOKEN_EXPIRY
        );
        emailVerificationTokensDAO.insertIntoTable(
                USER_ID + "two", EMAIL ,TOKEN, TOKEN_EXPIRY
        );

        List<EmailVerificationTokensDO> emailVerificationTokensDOList =
                emailVerificationTokensDAO.getEmailVerificationTokenWhereUserIdEqualsAndEmailEquals
                        (
                                USER_ID+"two", EMAIL
                        );

        assertTrue(emailVerificationTokensDOList.size() == 1);

        EmailVerificationTokensDO emailVerificationTokensDO = emailVerificationTokensDOList.get(0);
        assertTrue(emailVerificationTokensDO != null);
        assertTrue(emailVerificationTokensDO.getPrimary_key().getUser_id().equals(USER_ID+"two"));
        assertTrue(emailVerificationTokensDO.getPrimary_key().getEmail().equals(EMAIL));
        assertTrue(emailVerificationTokensDO.getPrimary_key().getToken().equals(TOKEN));
        assertTrue(emailVerificationTokensDO.getToken_expiry() == TOKEN_EXPIRY);
    }
    @Test
    public void testGetEmailVerificationTokenWhereUserIdEqualsAndEmailEqualsException() {
        emailVerificationTokensDAO.insertIntoTable(
                USER_ID, EMAIL ,TOKEN, TOKEN_EXPIRY
        );
        emailVerificationTokensDAO.insertIntoTable(
                USER_ID + "two", EMAIL ,TOKEN, TOKEN_EXPIRY
        );

        try {
            List<EmailVerificationTokensDO> emailVerificationTokensDO =
                    emailVerificationTokensDAO.getEmailVerificationTokenWhereUserIdEqualsAndEmailEquals
                            (
                                    USER_ID + "three", EMAIL
                            );
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();
    }

    @Test
    public void testDeleteFromTableWhereUserIdEqualsAndEmailEquals() throws Exception {
        emailVerificationTokensDAO.insertIntoTable(
                USER_ID, EMAIL ,TOKEN, TOKEN_EXPIRY
        );

        emailVerificationTokensDAO.deleteFromTableWhereUserIdEqualsAndEmailEquals(USER_ID, EMAIL);
        assertTrue(emailVerificationTokensDAO.getAll().size() == 0);
    }

    @Test
    public void testDeleteFromTableWhereUserIdEqualsAndEmailEqualsException() throws Exception {
        emailVerificationTokensDAO.insertIntoTable(
                USER_ID, EMAIL ,TOKEN, TOKEN_EXPIRY
        );

        try {
            emailVerificationTokensDAO.deleteFromTableWhereUserIdEqualsAndEmailEquals(USER_ID + "two", EMAIL);
        } catch (UserAndEmailNotFoundException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            //do nothing failure case scenario
        }
        fail();
    }
}