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

import io.supertokens.storage.sql.HibernateUtil;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessDevicesDO;
import io.supertokens.storage.sql.test.HibernateUtilTest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.NoResultException;

import static io.supertokens.storage.sql.TestConstants.*;
import static org.junit.Assert.*;

public class PasswordlessDevicesDAOTest {

    static PasswordlessDevicesDAO passwordlessDevicesDAO;

    @BeforeClass
    public static void beforeClass() {
        passwordlessDevicesDAO = new PasswordlessDevicesDAO(HibernateUtilTest.getSessionFactory());
    }

    @Before
    public void before() {
        passwordlessDevicesDAO.removeAll();
    }

    @After
    public void after() {
        passwordlessDevicesDAO.removeAll();
    }

    @Test
    public void insertIntoTableValues() {

        assertTrue(passwordlessDevicesDAO.getAll().size() == 0);

        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH, EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);

        PasswordlessDevicesDO passwordlessDevicesDO = (PasswordlessDevicesDO) passwordlessDevicesDAO.getAll().get(0);

        assertTrue(passwordlessDevicesDO.getDevice_id_hash().equals(DEVICE_ID_HASH));
        assertTrue(passwordlessDevicesDO.getEmail().equals(EMAIL));
        assertTrue(passwordlessDevicesDO.getPhone_number().equals(PHONE_NUMBER));
        assertTrue(passwordlessDevicesDO.getLink_code_salt().equals(LINK_CODE_SALT));
        assertTrue(passwordlessDevicesDO.getFailed_attempts() == (FAILED_ATTEMPTS));

    }

    @Test
    public void getWhereDeviceIdHashEquals() {
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH, EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH + "Two", EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);

        PasswordlessDevicesDO passwordlessDevicesDO = passwordlessDevicesDAO
                .getWhereDeviceIdHashEquals(DEVICE_ID_HASH + "Two");

        assertTrue(passwordlessDevicesDO != null);
        assertTrue(passwordlessDevicesDO.getDevice_id_hash().equals(DEVICE_ID_HASH + "Two"));
        assertTrue(passwordlessDevicesDO.getEmail().equals(EMAIL));
        assertTrue(passwordlessDevicesDO.getPhone_number().equals(PHONE_NUMBER));
        assertTrue(passwordlessDevicesDO.getLink_code_salt().equals(LINK_CODE_SALT));
        assertTrue(passwordlessDevicesDO.getFailed_attempts() == (FAILED_ATTEMPTS));
    }

    @Test
    public void getWhereDeviceIdHashEqualsException() {
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH, EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH + "Two", EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);

        try {
            PasswordlessDevicesDO passwordlessDevicesDO = passwordlessDevicesDAO
                    .getWhereDeviceIdHashEquals(DEVICE_ID_HASH + "Three");
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();
    }

    @Test
    public void updateFailedAttemptsWhereDeviceIdHashEquals() {
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH, EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);

        passwordlessDevicesDAO.updateFailedAttemptsWhereDeviceIdHashEquals(DEVICE_ID_HASH);

        PasswordlessDevicesDO devicesDO = passwordlessDevicesDAO.getWhereDeviceIdHashEquals(DEVICE_ID_HASH);
        assertTrue(devicesDO.getFailed_attempts() == FAILED_ATTEMPTS + 1);
    }

    @Test
    public void deleteWhereDeviceIdHashEquals() {
        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH, EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);

        assertTrue(passwordlessDevicesDAO.getAll().size() == 1);
        passwordlessDevicesDAO.deleteWhereDeviceIdHashEquals(DEVICE_ID_HASH);
        assertTrue(passwordlessDevicesDAO.getAll().size() == 0);

    }

    @Test
    public void deleteWhereDeviceIdHashEqualsException() {

        try {
            passwordlessDevicesDAO.deleteWhereDeviceIdHashEquals(DEVICE_ID_HASH);
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case
        }
        fail();

    }
}