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

import io.supertokens.pluginInterface.sqlStorage.SessionObject;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessDevicesDO;
import io.supertokens.storage.sql.test.HibernateUtilTest;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.NoResultException;

import static io.supertokens.storage.sql.TestConstants.*;
import static org.junit.Assert.*;

public class PasswordlessDevicesDAOTest {

    PasswordlessDevicesDAO passwordlessDevicesDAO;
    Session session;
    SessionObject sessionObject;

    @Before
    public void before() throws InterruptedException {
        session = HibernateUtilTest.getSessionFactory().openSession();
        sessionObject = new SessionObject(session);
        passwordlessDevicesDAO = new PasswordlessDevicesDAO(sessionObject);
        Transaction transaction = session.beginTransaction();
        passwordlessDevicesDAO.removeAll();
        transaction.commit();
    }

    @After
    public void after() {
        Transaction transaction = session.beginTransaction();
        passwordlessDevicesDAO.removeAll();
        transaction.commit();
        session.close();
    }

    @Test
    public void insertIntoTableValues() {

        assertTrue(passwordlessDevicesDAO.getAll().size() == 0);

        Transaction transaction = session.beginTransaction();
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH, EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        transaction.commit();
        session.clear();

        PasswordlessDevicesDO passwordlessDevicesDO = (PasswordlessDevicesDO) passwordlessDevicesDAO.getAll().get(0);

        assertTrue(passwordlessDevicesDO.getDevice_id_hash().equals(DEVICE_ID_HASH));
        assertTrue(passwordlessDevicesDO.getEmail().equals(EMAIL));
        assertTrue(passwordlessDevicesDO.getPhone_number().equals(PHONE_NUMBER));
        assertTrue(passwordlessDevicesDO.getLink_code_salt().equals(LINK_CODE_SALT));
        assertTrue(passwordlessDevicesDO.getFailed_attempts() == (FAILED_ATTEMPTS));

    }

    @Test
    public void getWhereDeviceIdHashEquals() {
        Transaction transaction = session.beginTransaction();

        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH, EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH + "Two", EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        transaction.commit();
        session.clear();

        transaction = session.beginTransaction();
        PasswordlessDevicesDO passwordlessDevicesDO = passwordlessDevicesDAO
                .getWhereDeviceIdHashEquals_locked(DEVICE_ID_HASH + "Two");
        transaction.commit();

        assertTrue(passwordlessDevicesDO != null);
        assertTrue(passwordlessDevicesDO.getDevice_id_hash().equals(DEVICE_ID_HASH + "Two"));
        assertTrue(passwordlessDevicesDO.getEmail().equals(EMAIL));
        assertTrue(passwordlessDevicesDO.getPhone_number().equals(PHONE_NUMBER));
        assertTrue(passwordlessDevicesDO.getLink_code_salt().equals(LINK_CODE_SALT));
        assertTrue(passwordlessDevicesDO.getFailed_attempts() == (FAILED_ATTEMPTS));
    }

    @Test
    public void getWhereDeviceIdHashEqualsException() {
        Transaction transaction = session.beginTransaction();

        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH, EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH + "Two", EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        transaction.commit();
        session.clear();

        try {
            transaction = session.beginTransaction();
            PasswordlessDevicesDO passwordlessDevicesDO = passwordlessDevicesDAO
                    .getWhereDeviceIdHashEquals_locked(DEVICE_ID_HASH + "Three");
            transaction.commit();
        } catch (NoResultException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();
    }

    @Test
    public void updateFailedAttemptsWhereDeviceIdHashEquals() {
        Transaction transaction = session.beginTransaction();

        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH, EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        transaction.commit();
        session.clear();

        transaction = session.beginTransaction();
        passwordlessDevicesDAO.updateFailedAttemptsWhereDeviceIdHashEquals(DEVICE_ID_HASH);
        transaction.commit();
        session.clear();

        transaction = session.beginTransaction();
        PasswordlessDevicesDO devicesDO = passwordlessDevicesDAO.getWhereDeviceIdHashEquals_locked(DEVICE_ID_HASH);
        transaction.commit();
        assertTrue(devicesDO.getFailed_attempts() == FAILED_ATTEMPTS + 1);
    }

    @Test
    public void deleteWhereDeviceIdHashEquals() {
        Transaction transaction = session.beginTransaction();

        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH, EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        transaction.commit();

        assertTrue(passwordlessDevicesDAO.getAll().size() == 1);

        transaction = session.beginTransaction();
        passwordlessDevicesDAO.deleteWhereDeviceIdHashEquals(DEVICE_ID_HASH);
        transaction.commit();
        session.clear();

        assertTrue(passwordlessDevicesDAO.getAll().size() == 0);

    }

    @Test
    public void deleteWhereDeviceIdHashEqualsException() {
        Transaction transaction = session.beginTransaction();

        try {
            passwordlessDevicesDAO.deleteWhereDeviceIdHashEquals(DEVICE_ID_HASH);
            transaction.commit();
        } catch (NoResultException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case
        }
        fail();

    }

    @Test
    public void deleteWherePhoneNumberEquals() {
        Transaction transaction = session.beginTransaction();
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH, EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        transaction.commit();
        assertTrue(passwordlessDevicesDAO.getAll().size() == 1);

        transaction = session.beginTransaction();
        passwordlessDevicesDAO.deleteWherePhoneNumberEquals(PHONE_NUMBER);
        transaction.commit();
        session.clear();

        assertTrue(passwordlessDevicesDAO.getAll().size() == 0);
    }

    @Test
    public void deleteWherePhoneNumberEqualsException() {
        Transaction transaction = session.beginTransaction();

        try {
            passwordlessDevicesDAO.deleteWherePhoneNumberEquals(PHONE_NUMBER);
            transaction.commit();
        } catch (NoResultException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case
        }
        fail();

    }

    @Test
    public void deleteWhereEmailEquals() {
        Transaction transaction = session.beginTransaction();

        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH, EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        transaction.commit();
        assertTrue(passwordlessDevicesDAO.getAll().size() == 1);

        transaction = session.beginTransaction();
        passwordlessDevicesDAO.deleteWhereEmailEquals(EMAIL);
        transaction.commit();
        session.clear();

        assertTrue(passwordlessDevicesDAO.getAll().size() == 0);
    }

    @Test
    public void deleteWhereEmailEqualsException() {
        Transaction transaction = session.beginTransaction();

        try {
            passwordlessDevicesDAO.deleteWhereEmailEquals(EMAIL);
            transaction.commit();
        } catch (NoResultException e) {

            if (transaction != null) {
                transaction.rollback();
            }
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case
        }
        fail();

    }

    @Test
    public void getDevicesWhereEmailEquails() {
        Transaction transaction = session.beginTransaction();

        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH + "1", EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH + "2", EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH + "3", EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH + "4", EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH + "5", EMAIL + ".com", PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        transaction.commit();

        assertTrue(passwordlessDevicesDAO.getDevicesWhereEmailEquals(EMAIL).size() == 4);
    }

    @Test
    public void getDevicesWhereEmailEquailsException() {

        try {
            passwordlessDevicesDAO.getDevicesWhereEmailEquals(EMAIL);
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing, failure case scenario
        }
        fail();
    }

    @Test
    public void getDevicesWherePhoneNumberEquals() {
        Transaction transaction = session.beginTransaction();

        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH + "1", EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH + "2", EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH + "3", EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH + "4", EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH + "5", EMAIL, PHONE_NUMBER + "1", LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        transaction.commit();

        assertTrue(passwordlessDevicesDAO.getDevicesWherePhoneNumberEquals(PHONE_NUMBER).size() == 4);
    }

    @Test
    public void getDevicesWherePhoneNumberEqualsException() {
        try {
            passwordlessDevicesDAO.getDevicesWherePhoneNumberEquals(PHONE_NUMBER);
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing, failure case scenario
        }
        fail();
    }
}