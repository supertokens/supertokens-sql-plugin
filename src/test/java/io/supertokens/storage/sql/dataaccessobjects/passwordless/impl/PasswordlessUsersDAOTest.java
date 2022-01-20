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

package io.supertokens.storage.sql.dataaccessobjects.passwordless.impl;

import io.supertokens.pluginInterface.emailpassword.exceptions.UnknownUserIdException;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessUsersDO;
import io.supertokens.storage.sql.test.HibernateUtilTest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import static io.supertokens.storage.sql.TestConstants.*;
import static org.junit.Assert.*;

public class PasswordlessUsersDAOTest {

    static PasswordlessUsersDAO passwordlessUsersDAO;

    @BeforeClass
    public static void beforeClass() {
        passwordlessUsersDAO = new PasswordlessUsersDAO(HibernateUtilTest.getSessionFactory());
    }

    @Before
    public void before() {
        passwordlessUsersDAO.removeAll();
    }

    @After
    public void after() {
        passwordlessUsersDAO.removeAll();
    }

    @Test
    public void updateEmailWhereUserIdEquals() throws UnknownUserIdException {

        passwordlessUsersDAO.insertValuesIntoTable(USER_ID, EMAIL, PHONE_NUMBER, TIME_JOINED);

        passwordlessUsersDAO.insertValuesIntoTable(USER_ID + "Two", EMAIL + "Two", PHONE_NUMBER + "Two", TIME_JOINED);

        passwordlessUsersDAO.updateEmailWhereUserIdEquals(USER_ID, EMAIL + ".com");

        PasswordlessUsersDO usersDO = passwordlessUsersDAO.getWhereUserIdEquals(USER_ID);
        assertTrue(usersDO.getEmail().equals(EMAIL + ".com"));
    }

    @Test
    public void updateEmailWhereUserIdEqualsException() {

        passwordlessUsersDAO.insertValuesIntoTable(USER_ID, EMAIL, PHONE_NUMBER, TIME_JOINED);

        try {
            passwordlessUsersDAO.updateEmailWhereUserIdEquals(USER_ID + "Two", EMAIL + ".com");
        } catch (UnknownUserIdException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();
    }

    @Test
    public void updateEmailWhereUserIdEqualsDuplicateException() {

        passwordlessUsersDAO.insertValuesIntoTable(USER_ID, EMAIL, PHONE_NUMBER, TIME_JOINED);

        passwordlessUsersDAO.insertValuesIntoTable(USER_ID + "two", EMAIL + ".com", PHONE_NUMBER + "22", TIME_JOINED);

        try {
            passwordlessUsersDAO.updateEmailWhereUserIdEquals(USER_ID + "Two", EMAIL);
        } catch (PersistenceException e) {
            assertTrue(e.getCause() instanceof ConstraintViolationException);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();
    }

    @Test
    public void insertValuesIntoTable() {
        assertTrue(passwordlessUsersDAO.getAll().size() == 0);
        passwordlessUsersDAO.insertValuesIntoTable(USER_ID, EMAIL, PHONE_NUMBER, TIME_JOINED);
        assertTrue(passwordlessUsersDAO.getAll().size() == 1);
    }

    @Test
    public void insertValuesIntoTableException() {
        passwordlessUsersDAO.insertValuesIntoTable(USER_ID, EMAIL, PHONE_NUMBER, TIME_JOINED);

        try {
            passwordlessUsersDAO.insertValuesIntoTable(USER_ID, EMAIL + "two", PHONE_NUMBER, TIME_JOINED);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ConstraintViolationException);
            return;
        }
        fail();
    }

    @Test
    public void getWhereUserIdEquals() {
        passwordlessUsersDAO.insertValuesIntoTable(USER_ID, EMAIL, PHONE_NUMBER, TIME_JOINED);
        PasswordlessUsersDO usersDO = passwordlessUsersDAO.getWhereUserIdEquals(USER_ID);

        assertTrue(usersDO.getUser_id().equals(USER_ID));
        assertTrue(usersDO.getEmail().equals(EMAIL));
        assertTrue(usersDO.getPhone_number().equals(PHONE_NUMBER));
        assertTrue(usersDO.getTime_joined() == TIME_JOINED);

    }

    @Test
    public void getWhereUserIdEqualsException() {
        try {
            PasswordlessUsersDO usersDO = passwordlessUsersDAO.getWhereUserIdEquals(USER_ID);
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();
    }

    @Test
    public void updatePhoneNumberWhereUserIdEquals() throws UnknownUserIdException {

        passwordlessUsersDAO.insertValuesIntoTable(USER_ID, EMAIL, PHONE_NUMBER, TIME_JOINED);

        passwordlessUsersDAO.insertValuesIntoTable(USER_ID + "Two", EMAIL + "Two", PHONE_NUMBER + "Two", TIME_JOINED);

        passwordlessUsersDAO.updatePhoneNumberWhereUserIdEquals(USER_ID, PHONE_NUMBER + "22");

        PasswordlessUsersDO usersDO = passwordlessUsersDAO.getWhereUserIdEquals(USER_ID);
        assertTrue(usersDO.getPhone_number().equals(PHONE_NUMBER + "22"));
    }

    @Test
    public void updatePhoneNumberWhereUserIdEqualsException() {

        passwordlessUsersDAO.insertValuesIntoTable(USER_ID, EMAIL, PHONE_NUMBER, TIME_JOINED);

        try {
            passwordlessUsersDAO.updatePhoneNumberWhereUserIdEquals(USER_ID + "Two", PHONE_NUMBER + "22");
        } catch (UnknownUserIdException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();
    }

    @Test
    public void updatePhoneNumberWhereUserIdEqualsDuplicateException() {

        passwordlessUsersDAO.insertValuesIntoTable(USER_ID, EMAIL, PHONE_NUMBER, TIME_JOINED);

        passwordlessUsersDAO.insertValuesIntoTable(USER_ID + "two", EMAIL + ".com", PHONE_NUMBER + "22", TIME_JOINED);

        try {
            passwordlessUsersDAO.updatePhoneNumberWhereUserIdEquals(USER_ID + "Two", PHONE_NUMBER);
        } catch (PersistenceException e) {
            assertTrue(e.getCause() instanceof ConstraintViolationException);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();
    }

    @Test
    public void getWhereEmailEquals() {
        passwordlessUsersDAO.insertValuesIntoTable(USER_ID, EMAIL, PHONE_NUMBER, TIME_JOINED);
        PasswordlessUsersDO usersDO = passwordlessUsersDAO.getUserWhereEmailEquals(EMAIL);

        assertTrue(usersDO.getUser_id().equals(USER_ID));
        assertTrue(usersDO.getEmail().equals(EMAIL));
        assertTrue(usersDO.getPhone_number().equals(PHONE_NUMBER));
        assertTrue(usersDO.getTime_joined() == TIME_JOINED);

    }

    @Test
    public void getWhereEmailEqualsException() {
        try {
            PasswordlessUsersDO usersDO = passwordlessUsersDAO.getUserWhereEmailEquals(EMAIL);
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();
    }
}