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

import io.supertokens.pluginInterface.passwordless.PasswordlessCode;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessCodesDO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessDevicesDO;
import io.supertokens.storage.sql.test.HibernateUtilTest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.NoResultException;
import java.util.List;

import static io.supertokens.storage.sql.TestConstants.*;
import static org.junit.Assert.*;

public class PasswordlessCodesDAOTest {

    static PasswordlessCodesDAO passwordlessCodesDAO;
    static PasswordlessDevicesDAO passwordlessDevicesDAO;

    @BeforeClass
    public static void beforeClass() {
        passwordlessCodesDAO = new PasswordlessCodesDAO(HibernateUtilTest.getSessionFactory());
        passwordlessDevicesDAO = new PasswordlessDevicesDAO(HibernateUtilTest.getSessionFactory());
    }

    @Before
    public void before() {
        passwordlessCodesDAO.removeAll();
        passwordlessDevicesDAO.removeAll();
    }

    @After
    public void after() {
        passwordlessCodesDAO.removeAll();
        passwordlessDevicesDAO.removeAll();
    }

    @Test
    public void insertIntoTableValues() {

        assertTrue(passwordlessCodesDAO.getAll().size() == 0);

        passwordlessCodesDAO.insertIntoTableValues(CODE_ID, null, LINKED_CODE_HASH, CREATED_AT);

        assertTrue(passwordlessCodesDAO.getAll().size() == 1);

    }

    @Test
    public void getCodesWhereDeviceIdHashEquals() {

        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH, EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);

        PasswordlessDevicesDO passwordlessDevicesDO = passwordlessDevicesDAO.getWhereDeviceIdHashEquals(DEVICE_ID_HASH);

        passwordlessCodesDAO.insertIntoTableValues(CODE_ID, passwordlessDevicesDO, LINKED_CODE_HASH, CREATED_AT);

        List<PasswordlessCodesDO> codesDO = passwordlessCodesDAO.getCodesWhereDeviceIdHashEquals(passwordlessDevicesDO);

        assertTrue(codesDO.size() == 1);
    }

    @Test
    public void getCodesWhereDeviceIdHashEqualsException() {

        passwordlessDevicesDAO.insertIntoTableValues(DEVICE_ID_HASH, EMAIL, PHONE_NUMBER, LINK_CODE_SALT,
                FAILED_ATTEMPTS, null);

        PasswordlessDevicesDO passwordlessDevicesDO = passwordlessDevicesDAO.getWhereDeviceIdHashEquals(DEVICE_ID_HASH);

        passwordlessCodesDAO.insertIntoTableValues(CODE_ID, null, LINKED_CODE_HASH, CREATED_AT);

        try {
            List<PasswordlessCodesDO> codesDO = passwordlessCodesDAO
                    .getCodesWhereDeviceIdHashEquals(passwordlessDevicesDO);
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();
    }

    @Test
    public void getWhereLinkCodeHashEquals() {
        passwordlessCodesDAO.insertIntoTableValues(CODE_ID, null, LINKED_CODE_HASH, CREATED_AT);
        passwordlessCodesDAO.insertIntoTableValues(CODE_ID + "two", null, LINKED_CODE_HASH + "two", CREATED_AT);

        PasswordlessCodesDO codesDO = passwordlessCodesDAO.getWhereLinkCodeHashEquals(LINKED_CODE_HASH + "Two");

        assertTrue(codesDO.getCode_id().equals(CODE_ID + "two"));
        assertTrue(codesDO.getLink_code_hash().equals(LINKED_CODE_HASH + "two"));
        assertTrue(codesDO.getCreated_at() == CREATED_AT);

    }

    @Test
    public void getWhereLinkCodeHashEqualsException() {
        passwordlessCodesDAO.insertIntoTableValues(CODE_ID, null, LINKED_CODE_HASH, CREATED_AT);

        try {
            PasswordlessCodesDO codesDO = passwordlessCodesDAO.getWhereLinkCodeHashEquals(LINKED_CODE_HASH + "Two");
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();

    }

    @Test
    public void deleteWhereCodeIdEquals() {

        assertTrue(passwordlessCodesDAO.getAll().size() == 0);

        passwordlessCodesDAO.insertIntoTableValues(CODE_ID, null, LINKED_CODE_HASH, CREATED_AT);

        assertTrue(passwordlessCodesDAO.getAll().size() == 1);

        passwordlessCodesDAO.deleteWhereCodeIdEquals(CODE_ID);

        assertTrue(passwordlessCodesDAO.getAll().size() == 0);
    }

    @Test
    public void deleteWhereCodeIdEqualsException() {

        assertTrue(passwordlessCodesDAO.getAll().size() == 0);

        passwordlessCodesDAO.insertIntoTableValues(CODE_ID, null, LINKED_CODE_HASH, CREATED_AT);

        assertTrue(passwordlessCodesDAO.getAll().size() == 1);

        try {
            passwordlessCodesDAO.deleteWhereCodeIdEquals(CODE_ID + "two");
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();
    }

    @Test
    public void getCodesWhereCreatedAtLessThan() {
        passwordlessCodesDAO.insertIntoTableValues(CODE_ID + "1", null, LINKED_CODE_HASH + "1", CREATED_AT);
        passwordlessCodesDAO.insertIntoTableValues(CODE_ID + "2", null, LINKED_CODE_HASH + "2", CREATED_AT + 20l);
        passwordlessCodesDAO.insertIntoTableValues(CODE_ID + "3", null, LINKED_CODE_HASH + "3", CREATED_AT + 30l);
        passwordlessCodesDAO.insertIntoTableValues(CODE_ID + "4", null, LINKED_CODE_HASH + "4", CREATED_AT + 40l);
        passwordlessCodesDAO.insertIntoTableValues(CODE_ID + "5", null, LINKED_CODE_HASH + "5", CREATED_AT + 50l);

        assertTrue(passwordlessCodesDAO.getCodesWhereCreatedAtLessThan(CREATED_AT + 45l).size() == 4);
    }

    @Test
    public void getCodesWhereCreatedAtLessThanException() {
        try {
            passwordlessCodesDAO.getCodesWhereCreatedAtLessThan(CREATED_AT + 45l);
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case
        }
        fail();
    }

    @Test
    public void getCodeWhereCodeIdEquals() {
        passwordlessCodesDAO.insertIntoTableValues(CODE_ID + "1", null, LINKED_CODE_HASH + "1", CREATED_AT);
        passwordlessCodesDAO.insertIntoTableValues(CODE_ID + "2", null, LINKED_CODE_HASH + "2", CREATED_AT + 20l);

        PasswordlessCodesDO codesDO = passwordlessCodesDAO.getCodeWhereCodeIdEquals(CODE_ID + "1");
        assertTrue(codesDO.getCreated_at() == CREATED_AT);
        assertTrue(codesDO.getCode_id().equals(CODE_ID + "1"));
        assertTrue(codesDO.getLink_code_hash().equals(LINKED_CODE_HASH + "1"));

    }

    @Test
    public void getCodeWhereCodeIdEqualsException() {
        passwordlessCodesDAO.insertIntoTableValues(CODE_ID + "1", null, LINKED_CODE_HASH + "1", CREATED_AT);
        passwordlessCodesDAO.insertIntoTableValues(CODE_ID + "2", null, LINKED_CODE_HASH + "2", CREATED_AT + 20l);

        try {
            passwordlessCodesDAO.getCodeWhereCodeIdEquals(CODE_ID + "3");
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure
        }
        fail();
    }
}