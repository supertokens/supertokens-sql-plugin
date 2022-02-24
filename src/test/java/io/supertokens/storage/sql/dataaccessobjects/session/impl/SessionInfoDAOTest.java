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

import io.supertokens.pluginInterface.sqlStorage.SessionObject;
import io.supertokens.storage.sql.domainobjects.session.SessionInfoDO;
import io.supertokens.storage.sql.test.HibernateUtilTest;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.NoResultException;

import static io.supertokens.storage.sql.TestConstants.*;
import static org.junit.Assert.*;

public class SessionInfoDAOTest {

    Session session;
    SessionObject sessionObject;
    SessionInfoDAO sessionInfoDAO;

    @Before
    public void before() throws Exception {
        session = HibernateUtilTest.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        sessionObject = new SessionObject(session);
        sessionInfoDAO = new SessionInfoDAO(sessionObject);
        sessionInfoDAO.removeAll();
        transaction.commit();
    }

    @After
    public void after() throws Exception {
        Transaction transaction = session.beginTransaction();

        sessionInfoDAO.removeAll();
        transaction.commit();
        session.close();
    }

    @Test
    public void insertIntoTableValues() throws Exception {

        Transaction transaction = session.beginTransaction();

        String id = (String) sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO,
                SESSION_DATA, EXPIRES_AT, CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();

        assertTrue(sessionInfoDAO.getAll().size() == 1);
        assertTrue(id.equals(SESSION_HANDLE));

    }

    @Test
    public void insertIntoTableValuesException() throws Exception {

        Transaction transaction = session.beginTransaction();
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();
        try {
            transaction = session.beginTransaction();
            sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID + "two", REFRESH_TOKEN_HASH_TWO, SESSION_DATA,
                    EXPIRES_AT, CREATED_AT, JWT_USER_PAYLOAD);
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
    public void getWhereSessionHandle_lockedEquals() throws InterruptedException {

        Transaction transaction = session.beginTransaction();
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();

        transaction = session.beginTransaction();
        SessionInfoDO sessionInfoDO = sessionInfoDAO.getWhereSessionHandleEquals_locked(SESSION_HANDLE);
        transaction.commit();

        assertTrue(sessionInfoDO != null);
        assertTrue(sessionInfoDO.getSession_handle().equals(SESSION_HANDLE));
        assertTrue(sessionInfoDO.getUser_id().equals(USER_ID));
        assertTrue(sessionInfoDO.getRefresh_token_hash_2().equals(REFRESH_TOKEN_HASH_TWO));
        assertTrue(sessionInfoDO.getSession_data().equals(SESSION_DATA));
        assertTrue(sessionInfoDO.getExpires_at() == EXPIRES_AT);
        assertTrue(sessionInfoDO.getCreated_at_time() == CREATED_AT);
        assertTrue(sessionInfoDO.getJwt_user_payload().equals(JWT_USER_PAYLOAD));

    }

    @Test
    public void getWhereSessionHandleEquals_lockedException() throws InterruptedException {

        Transaction transaction = session.beginTransaction();

        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();
        transaction = session.beginTransaction();
        SessionInfoDO sessionInfoDO = sessionInfoDAO.getWhereSessionHandleEquals_locked(SESSION_HANDLE + "Two");
        transaction.commit();

        assertTrue(sessionInfoDO == null);

    }

    @Test
    public void updateRefreshTokenTwoAndExpiresAtWhereSessionHandleEquals() throws InterruptedException {

        Transaction transaction = session.beginTransaction();

        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();

        transaction = session.beginTransaction();
        sessionInfoDAO.updateRefreshTokenTwoAndExpiresAtWhereSessionHandleEquals(REFRESH_TOKEN_HASH_TWO + "UPDATED",
                EXPIRES_AT + 30l, SESSION_HANDLE);
        transaction.commit();

        session.clear();

        transaction = session.beginTransaction();
        SessionInfoDO sessionInfoDO = sessionInfoDAO.getWhereSessionHandleEquals_locked(SESSION_HANDLE);
        transaction.commit();

        assertTrue(sessionInfoDO.getExpires_at() == EXPIRES_AT + 30l);
        assertTrue(sessionInfoDO.getRefresh_token_hash_2().equals(REFRESH_TOKEN_HASH_TWO + "UPDATED"));

    }

    @Test
    public void updateRefreshTokenTwoAndExpiresAtWhereSessionHandleEqualsException() throws InterruptedException {

        Transaction transaction = session.beginTransaction();
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();

        transaction = session.beginTransaction();
        int rows = sessionInfoDAO.updateRefreshTokenTwoAndExpiresAtWhereSessionHandleEquals(
                REFRESH_TOKEN_HASH_TWO + "UPDATED", EXPIRES_AT + 30l, SESSION_HANDLE + "TWO");
        transaction.commit();
        assertTrue(rows == 0);
    }

    @Test
    public void deleteWhereUserIdEquals() throws Exception {

        Transaction transaction = session.beginTransaction();

        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);

        transaction.commit();

        assertTrue(sessionInfoDAO.getAll().size() == 1);

        transaction = session.beginTransaction();
        sessionInfoDAO.deleteWhereUserIdEquals(USER_ID);
        transaction.commit();
        assertTrue(sessionInfoDAO.getAll().size() == 0);

    }

    @Test
    public void deleteWhereUserIdEqualsException() throws Exception {

        Transaction transaction = session.beginTransaction();

        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();

        assertTrue(sessionInfoDAO.getAll().size() == 1);

        transaction = session.beginTransaction();
        int rows = sessionInfoDAO.deleteWhereUserIdEquals(USER_ID + "two");
        transaction.commit();

        assertTrue(rows == 0);

    }

    @Test
    public void getSessionHandlesWhereUserIdEquals() throws InterruptedException {

        Transaction transaction = session.beginTransaction();
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE + "two", USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA,
                EXPIRES_AT, CREATED_AT, JWT_USER_PAYLOAD);
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE + "three", USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA,
                EXPIRES_AT, CREATED_AT, JWT_USER_PAYLOAD);
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE + "four", USER_ID + "three", REFRESH_TOKEN_HASH_TWO,
                SESSION_DATA, EXPIRES_AT, CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();

        String[] sessionHandles = sessionInfoDAO.getSessionHandlesWhereUserIdEquals(USER_ID);

        assertTrue(sessionHandles.length == 3);

    }

    @Test
    public void getSessionHandlesWhereUserIdEqualsException() throws InterruptedException {

        Transaction transaction = session.beginTransaction();
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();

        String[] results = sessionInfoDAO.getSessionHandlesWhereUserIdEquals(USER_ID + "two");
        assertTrue(results.length == 0);

    }

    @Test
    public void getWhereSessionHandleEquals() throws InterruptedException {

        Transaction transaction = session.beginTransaction();
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();
        SessionInfoDO sessionInfoDO = sessionInfoDAO.getWhereSessionHandleEquals(SESSION_HANDLE);
        assertTrue(sessionInfoDO != null);
        assertTrue(sessionInfoDO.getSession_handle().equals(SESSION_HANDLE));
        assertTrue(sessionInfoDO.getUser_id().equals(USER_ID));
        assertTrue(sessionInfoDO.getRefresh_token_hash_2().equals(REFRESH_TOKEN_HASH_TWO));
        assertTrue(sessionInfoDO.getSession_data().equals(SESSION_DATA));
        assertTrue(sessionInfoDO.getExpires_at() == EXPIRES_AT);
        assertTrue(sessionInfoDO.getCreated_at_time() == CREATED_AT);
        assertTrue(sessionInfoDO.getJwt_user_payload().equals(JWT_USER_PAYLOAD));

    }

    @Test
    public void getWhereSessionHandleEqualsException() throws InterruptedException {

        Transaction transaction = session.beginTransaction();
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();
        SessionInfoDO sessionInfoDO = sessionInfoDAO.getWhereSessionHandleEquals(SESSION_HANDLE + "Two");
        assertNull(sessionInfoDO);

    }
}