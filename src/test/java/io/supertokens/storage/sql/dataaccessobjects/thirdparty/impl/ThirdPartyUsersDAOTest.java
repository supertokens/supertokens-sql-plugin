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

package io.supertokens.storage.sql.dataaccessobjects.thirdparty.impl;

import io.supertokens.storage.sql.domainobjects.thirdparty.ThirdPartyUsersDO;
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

public class ThirdPartyUsersDAOTest {

    static ThirdPartyUsersDAO thirdPartyUsersDAO;

    @BeforeClass
    public static void beforeClass() {
        thirdPartyUsersDAO = new ThirdPartyUsersDAO(
                HibernateUtilTest.getSessionFactory()
        );
    }

    @Before
    public void before() {
        thirdPartyUsersDAO.removeAll();
    }

    @After
    public void after() {
        thirdPartyUsersDAO.removeAll();
    }

    @Test
    public void getWhereThirdPartyIDEqualsAndThirdPartyUserIdEquals() {
        thirdPartyUsersDAO.insertValues(
                THIRD_PARTY_ID, THIRD_PARTY_USER_ID, USER_ID, EMAIL, TIME_JOINED
        );
        thirdPartyUsersDAO.insertValues(
                THIRD_PARTY_ID + "Two", THIRD_PARTY_USER_ID, USER_ID + "Two", EMAIL, TIME_JOINED
        );

        ThirdPartyUsersDO thirdPartyUsersDO = thirdPartyUsersDAO.getWhereThirdPartyIDEqualsAndThirdPartyUserIdEquals(
                THIRD_PARTY_ID + "Two", THIRD_PARTY_USER_ID
        );

        assertTrue(thirdPartyUsersDO != null);
        assertTrue(thirdPartyUsersDO.getEmail().equals(EMAIL));
        assertTrue(thirdPartyUsersDO.getUser_id().equals(USER_ID + "Two"));
        assertTrue(thirdPartyUsersDO.getPrimary_key().getThird_party_id().equals(THIRD_PARTY_ID + "Two"));
        assertTrue(thirdPartyUsersDO.getPrimary_key().getThird_party_user_id().equals(THIRD_PARTY_USER_ID));

    }

    @Test
    public void getWhereThirdPartyIDEqualsAndThirdPartyUserIdEqualsException() {
        thirdPartyUsersDAO.insertValues(
                THIRD_PARTY_ID, THIRD_PARTY_USER_ID, USER_ID, EMAIL, TIME_JOINED
        );
        thirdPartyUsersDAO.insertValues(
                THIRD_PARTY_ID + "Two", THIRD_PARTY_USER_ID, USER_ID + "Two", EMAIL, TIME_JOINED
        );

        try {
            ThirdPartyUsersDO thirdPartyUsersDO = thirdPartyUsersDAO
                    .getWhereThirdPartyIDEqualsAndThirdPartyUserIdEquals(
                            THIRD_PARTY_ID + "Three", THIRD_PARTY_USER_ID
                    );
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            //do nothing failure case scenario
        }
        fail();
    }

    @Test
    public void insertValues() {

        thirdPartyUsersDAO.insertValues(
                THIRD_PARTY_ID, THIRD_PARTY_USER_ID, USER_ID, EMAIL, TIME_JOINED
        );

        assertTrue(thirdPartyUsersDAO.getAll().size() == 1);

    }

    @Test
    public void insertValuesException() {

        thirdPartyUsersDAO.insertValues(
                THIRD_PARTY_ID, THIRD_PARTY_USER_ID, USER_ID, EMAIL, TIME_JOINED
        );

        try {
            thirdPartyUsersDAO.insertValues(
                    THIRD_PARTY_ID, THIRD_PARTY_USER_ID, USER_ID, EMAIL, TIME_JOINED
            );
        } catch (PersistenceException e) {
            assertTrue(e.getCause() instanceof ConstraintViolationException);
            return;
        }
        fail();

    }
}