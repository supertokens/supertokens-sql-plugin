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

import io.supertokens.storage.sql.domainobjects.session.SessionInfoDO;
import io.supertokens.storage.sql.exceptions.SessionHandleNotFoundException;
import io.supertokens.storage.sql.exceptions.UserIdNotFoundException;
import io.supertokens.storage.sql.test.HibernateUtilTest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.NoResultException;

import static io.supertokens.storage.sql.TestConstants.*;
import static org.junit.Assert.*;

public class SessionInfoDAOTest {

    static SessionInfoDAO sessionInfoDAO;

    @BeforeClass
    public static void beforeClass() {
        sessionInfoDAO = new SessionInfoDAO(HibernateUtilTest.getSessionFactory());
    }

    @Before
    public void before() throws Exception {
        sessionInfoDAO.removeAll();
    }

    @After
    public void after() throws Exception {
        sessionInfoDAO.removeAll();
    }

    @Test
    public void insertIntoTableValues() throws Exception {

        String id = (String) sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO,
                SESSION_DATA, EXPIRES_AT, CREATED_AT, JWT_USER_PAYLOAD);

        assertTrue(sessionInfoDAO.getAll().size() == 1);
        assertTrue(id.equals(SESSION_HANDLE));

    }

    @Test
    public void insertIntoTableValuesException() throws Exception {

        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);

        try {
            sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID + "two", REFRESH_TOKEN_HASH_TWO, SESSION_DATA,
                    EXPIRES_AT, CREATED_AT, JWT_USER_PAYLOAD);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ConstraintViolationException);
            return;
        }
        fail();

    }

    @Test
    public void getWhereSessionHandle_lockedEquals() {
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);

        SessionInfoDO sessionInfoDO = sessionInfoDAO.getWhereSessionHandleEquals_locked(SESSION_HANDLE);
        assertTrue(sessionInfoDO != null);
        assertTrue(sessionInfoDO.getSession_handle().equals(SESSION_HANDLE));
        assertTrue(sessionInfoDO.getUser_id().equals(USER_ID));
        assertTrue(sessionInfoDO.getRefresh_token_hash_2().equals(REFRESH_TOKEN_HASH_TWO));
        assertTrue(sessionInfoDO.getSessions_data().equals(SESSION_DATA));
        assertTrue(sessionInfoDO.getExpires_at() == EXPIRES_AT);
        assertTrue(sessionInfoDO.getCreated_at_time() == CREATED_AT);
        assertTrue(sessionInfoDO.getJwt_user_payload().equals(JWT_USER_PAYLOAD));

    }

    @Test
    public void getWhereSessionHandleEquals_lockedException() {
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);

        try {
            SessionInfoDO sessionInfoDO = sessionInfoDAO.getWhereSessionHandleEquals_locked(SESSION_HANDLE + "Two");
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();

    }

    @Test
    public void updateRefreshTokenTwoAndExpiresAtWhereSessionHandleEquals() throws SessionHandleNotFoundException {
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);

        sessionInfoDAO.updateRefreshTokenTwoAndExpiresAtWhereSessionHandleEquals(REFRESH_TOKEN_HASH_TWO + "UPDATED",
                EXPIRES_AT + 30l, SESSION_HANDLE);

        SessionInfoDO sessionInfoDO = sessionInfoDAO.getWhereSessionHandleEquals_locked(SESSION_HANDLE);

        assertTrue(sessionInfoDO.getExpires_at() == EXPIRES_AT + 30l);
        assertTrue(sessionInfoDO.getRefresh_token_hash_2().equals(REFRESH_TOKEN_HASH_TWO + "UPDATED"));
    }

    @Test
    public void updateRefreshTokenTwoAndExpiresAtWhereSessionHandleEqualsException()
            throws SessionHandleNotFoundException {
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);

        try {
            sessionInfoDAO.updateRefreshTokenTwoAndExpiresAtWhereSessionHandleEquals(REFRESH_TOKEN_HASH_TWO + "UPDATED",
                    EXPIRES_AT + 30l, SESSION_HANDLE + "TWO");
        } catch (SessionHandleNotFoundException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();
    }

    @Test
    public void deleteWhereUserIdEquals() throws Exception {

        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        assertTrue(sessionInfoDAO.getAll().size() == 1);

        sessionInfoDAO.deleteWhereUserIdEquals(USER_ID);
        assertTrue(sessionInfoDAO.getAll().size() == 0);
    }

    @Test
    public void deleteWhereUserIdEqualsException() throws Exception {

        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        assertTrue(sessionInfoDAO.getAll().size() == 1);

        try {
            sessionInfoDAO.deleteWhereUserIdEquals(USER_ID + "two");
        } catch (UserIdNotFoundException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();
    }

    @Test
    public void getSessionHandlesWhereUserIdEquals() throws UserIdNotFoundException {
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE + "two", USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA,
                EXPIRES_AT, CREATED_AT, JWT_USER_PAYLOAD);
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE + "three", USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA,
                EXPIRES_AT, CREATED_AT, JWT_USER_PAYLOAD);
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE + "four", USER_ID + "three", REFRESH_TOKEN_HASH_TWO,
                SESSION_DATA, EXPIRES_AT, CREATED_AT, JWT_USER_PAYLOAD);

        String[] sessionHandles = sessionInfoDAO.getSessionHandlesWhereUserIdEquals(USER_ID);

        assertTrue(sessionHandles.length == 3);
    }

    @Test
    public void getSessionHandlesWhereUserIdEqualsException() {
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);

        try {
            sessionInfoDAO.getSessionHandlesWhereUserIdEquals(USER_ID + "two");
        } catch (UserIdNotFoundException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();
    }

    @Test
    public void getWhereSessionHandleEquals() {
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);

        SessionInfoDO sessionInfoDO = sessionInfoDAO.getWhereSessionHandleEquals(SESSION_HANDLE);
        assertTrue(sessionInfoDO != null);
        assertTrue(sessionInfoDO.getSession_handle().equals(SESSION_HANDLE));
        assertTrue(sessionInfoDO.getUser_id().equals(USER_ID));
        assertTrue(sessionInfoDO.getRefresh_token_hash_2().equals(REFRESH_TOKEN_HASH_TWO));
        assertTrue(sessionInfoDO.getSessions_data().equals(SESSION_DATA));
        assertTrue(sessionInfoDO.getExpires_at() == EXPIRES_AT);
        assertTrue(sessionInfoDO.getCreated_at_time() == CREATED_AT);
        assertTrue(sessionInfoDO.getJwt_user_payload().equals(JWT_USER_PAYLOAD));

    }

    @Test
    public void getWhereSessionHandleEqualsException() {
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);

        try {
            SessionInfoDO sessionInfoDO = sessionInfoDAO.getWhereSessionHandleEquals(SESSION_HANDLE + "Two");
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();

    }
}