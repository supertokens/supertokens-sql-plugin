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

import io.supertokens.storage.sql.HibernateUtil;
import io.supertokens.storage.sql.domainobjects.session.SessionInfoDO;
import io.supertokens.storage.sql.exceptions.SessionHandleNotFoundException;
import io.supertokens.storage.sql.exceptions.UserIdNotFoundException;
import io.supertokens.storage.sql.test.HibernateUtilTest;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.query.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static io.supertokens.storage.sql.TestConstants.*;
import static org.junit.Assert.*;

public class SessionInfoDAOTest {

    @Before
    public void before() throws Exception {
        Session session = HibernateUtilTest.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(session);
        sessionInfoDAO.removeAll();
        transaction.commit();
        session.close();
    }

    @After
    public void after() throws Exception {
        before();
    }

    @Test
    public void insertIntoTableValues() throws Exception {
        Session session = HibernateUtilTest.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(session);

        String id = (String) sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO,
                SESSION_DATA, EXPIRES_AT, CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();

        assertTrue(sessionInfoDAO.getAll().size() == 1);
        assertTrue(id.equals(SESSION_HANDLE));

        session.close();
    }

    @Test
    public void insertIntoTableValuesException() throws Exception {

        Session session = HibernateUtilTest.getSessionFactory().openSession();
        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(session);

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

        } finally {
            if (session != null) {
                session.close();
            }
        }
        fail();

    }

    @Test
    public void getWhereSessionHandle_lockedEquals() {
        Session session = HibernateUtilTest.getSessionFactory().openSession();
        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(session);

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
        assertTrue(sessionInfoDO.getSessions_data().equals(SESSION_DATA));
        assertTrue(sessionInfoDO.getExpires_at() == EXPIRES_AT);
        assertTrue(sessionInfoDO.getCreated_at_time() == CREATED_AT);
        assertTrue(sessionInfoDO.getJwt_user_payload().equals(JWT_USER_PAYLOAD));

        if (session != null) {
            session.close();
        }

    }

    @Test
    public void getWhereSessionHandleEquals_lockedException() {
        Session session = HibernateUtilTest.getSessionFactory().openSession();
        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(session);

        Transaction transaction = session.beginTransaction();

        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();
        try {
            transaction = session.beginTransaction();
            SessionInfoDO sessionInfoDO = sessionInfoDAO.getWhereSessionHandleEquals_locked(SESSION_HANDLE + "Two");
            transaction.commit();
        } catch (NoResultException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        } finally {
            if (session != null) {
                session.close();
            }
        }
        fail();

    }

    @Test
    public void updateRefreshTokenTwoAndExpiresAtWhereSessionHandleEquals() throws SessionHandleNotFoundException {
        Session session = HibernateUtilTest.getSessionFactory().openSession();
        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(session);

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

        session.close();
    }

    @Test
    public void updateRefreshTokenTwoAndExpiresAtWhereSessionHandleEqualsException()
            throws SessionHandleNotFoundException {
        Session session = HibernateUtilTest.getSessionFactory().openSession();
        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(session);

        Transaction transaction = session.beginTransaction();
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();

        try {
            transaction = session.beginTransaction();
            sessionInfoDAO.updateRefreshTokenTwoAndExpiresAtWhereSessionHandleEquals(REFRESH_TOKEN_HASH_TWO + "UPDATED",
                    EXPIRES_AT + 30l, SESSION_HANDLE + "TWO");
            transaction.commit();
        } catch (SessionHandleNotFoundException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        } finally {
            if (session != null) {
                session.close();
            }
        }
        fail();
    }

    @Test
    public void deleteWhereUserIdEquals() throws Exception {
        Session session = HibernateUtilTest.getSessionFactory().openSession();
        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(session);

        Transaction transaction = session.beginTransaction();

        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);

        transaction.commit();

        assertTrue(sessionInfoDAO.getAll().size() == 1);

        transaction = session.beginTransaction();
        sessionInfoDAO.deleteWhereUserIdEquals(USER_ID);
        transaction.commit();
        assertTrue(sessionInfoDAO.getAll().size() == 0);

        session.close();
    }

    @Test
    public void deleteWhereUserIdEqualsException() throws Exception {
        Session session = HibernateUtilTest.getSessionFactory().openSession();
        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(session);

        Transaction transaction = session.beginTransaction();

        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();

        assertTrue(sessionInfoDAO.getAll().size() == 1);

        try {
            transaction = session.beginTransaction();
            sessionInfoDAO.deleteWhereUserIdEquals(USER_ID + "two");
            transaction.commit();
        } catch (UserIdNotFoundException e) {

            if (transaction != null) {
                transaction.rollback();
            }
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        } finally {
            if (session != null) {
                session.close();
            }
        }
        fail();
    }

    @Test
    public void getSessionHandlesWhereUserIdEquals() throws UserIdNotFoundException {
        Session session = HibernateUtilTest.getSessionFactory().openSession();
        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(session);

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
        session.close();
        assertTrue(sessionHandles.length == 3);

    }

    @Test
    public void getSessionHandlesWhereUserIdEqualsException() {
        Session session = HibernateUtilTest.getSessionFactory().openSession();
        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(session);

        Transaction transaction = session.beginTransaction();
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();

        try {
            sessionInfoDAO.getSessionHandlesWhereUserIdEquals(USER_ID + "two");
        } catch (UserIdNotFoundException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        } finally {
            if (session != null) {
                session.close();
            }
        }
        fail();
    }

    @Test
    public void getWhereSessionHandleEquals() {
        Session session = HibernateUtilTest.getSessionFactory().openSession();
        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(session);

        Transaction transaction = session.beginTransaction();
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();
        SessionInfoDO sessionInfoDO = sessionInfoDAO.getWhereSessionHandleEquals(SESSION_HANDLE);
        assertTrue(sessionInfoDO != null);
        assertTrue(sessionInfoDO.getSession_handle().equals(SESSION_HANDLE));
        assertTrue(sessionInfoDO.getUser_id().equals(USER_ID));
        assertTrue(sessionInfoDO.getRefresh_token_hash_2().equals(REFRESH_TOKEN_HASH_TWO));
        assertTrue(sessionInfoDO.getSessions_data().equals(SESSION_DATA));
        assertTrue(sessionInfoDO.getExpires_at() == EXPIRES_AT);
        assertTrue(sessionInfoDO.getCreated_at_time() == CREATED_AT);
        assertTrue(sessionInfoDO.getJwt_user_payload().equals(JWT_USER_PAYLOAD));
        session.close();
    }

    @Test
    public void getWhereSessionHandleEqualsException() {
        Session session = HibernateUtilTest.getSessionFactory().openSession();
        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(session);

        Transaction transaction = session.beginTransaction();
        sessionInfoDAO.insertIntoTableValues(SESSION_HANDLE, USER_ID, REFRESH_TOKEN_HASH_TWO, SESSION_DATA, EXPIRES_AT,
                CREATED_AT, JWT_USER_PAYLOAD);
        transaction.commit();

        try {
            SessionInfoDO sessionInfoDO = sessionInfoDAO.getWhereSessionHandleEquals(SESSION_HANDLE + "Two");
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        } finally {
            if (session != null) {
                session.close();
            }
        }
        fail();

    }
}