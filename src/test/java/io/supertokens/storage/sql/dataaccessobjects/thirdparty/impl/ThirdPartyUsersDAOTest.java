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

import io.supertokens.pluginInterface.sqlStorage.SessionObject;
import io.supertokens.storage.sql.domainobjects.thirdparty.ThirdPartyUsersDO;
import io.supertokens.storage.sql.exceptions.InvalidOrderTypeException;
import io.supertokens.storage.sql.test.HibernateUtilTest;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import java.util.List;

import static io.supertokens.storage.sql.TestConstants.*;
import static org.junit.Assert.*;

public class ThirdPartyUsersDAOTest {

    ThirdPartyUsersDAO thirdPartyUsersDAO;
    Session session;
    SessionObject sessionObject;

    @Before
    public void before() throws InterruptedException {
        session = HibernateUtilTest.getSessionFactory().openSession();
        sessionObject = new SessionObject(session);
        thirdPartyUsersDAO = new ThirdPartyUsersDAO(sessionObject);
        Transaction transaction = session.beginTransaction();
        thirdPartyUsersDAO.removeAll();
        transaction.commit();
    }

    @After
    public void after() {
        Transaction transaction = session.beginTransaction();
        thirdPartyUsersDAO.removeAll();
        transaction.commit();
        session.close();
    }

    @Test
    public void getWhereThirdPartyIDEqualsAndThirdPartyUserIdEquals() {
        Transaction transaction = session.beginTransaction();
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID, THIRD_PARTY_USER_ID, USER_ID, EMAIL, TIME_JOINED);
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "Two", THIRD_PARTY_USER_ID, USER_ID + "Two", EMAIL,
                TIME_JOINED);
        transaction.commit();

        ThirdPartyUsersDO thirdPartyUsersDO = thirdPartyUsersDAO
                .getWhereThirdPartyIDEqualsAndThirdPartyUserIdEquals(THIRD_PARTY_ID + "Two", THIRD_PARTY_USER_ID);

        assertTrue(thirdPartyUsersDO != null);
        assertTrue(thirdPartyUsersDO.getEmail().equals(EMAIL));
        assertTrue(thirdPartyUsersDO.getUser_id().equals(USER_ID + "Two"));
        assertTrue(thirdPartyUsersDO.getPrimary_key().getThird_party_id().equals(THIRD_PARTY_ID + "Two"));
        assertTrue(thirdPartyUsersDO.getPrimary_key().getThird_party_user_id().equals(THIRD_PARTY_USER_ID));

    }

    @Test
    public void getWhereThirdPartyIDEqualsAndThirdPartyUserIdEqualsException() {
        Transaction transaction = session.beginTransaction();
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID, THIRD_PARTY_USER_ID, USER_ID, EMAIL, TIME_JOINED);
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "Two", THIRD_PARTY_USER_ID, USER_ID + "Two", EMAIL,
                TIME_JOINED);

        try {
            ThirdPartyUsersDO thirdPartyUsersDO = thirdPartyUsersDAO
                    .getWhereThirdPartyIDEqualsAndThirdPartyUserIdEquals(THIRD_PARTY_ID + "Three", THIRD_PARTY_USER_ID);
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
    public void insertValues() {
        Transaction transaction = session.beginTransaction();
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID, THIRD_PARTY_USER_ID, USER_ID, EMAIL, TIME_JOINED);
        transaction.commit();
        assertTrue(thirdPartyUsersDAO.getAll().size() == 1);

    }

    @Test
    public void insertValuesException() {
        Transaction transaction = session.beginTransaction();
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID, THIRD_PARTY_USER_ID, USER_ID, EMAIL, TIME_JOINED);
        transaction.commit();
        try {
            transaction = session.beginTransaction();
            thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID, THIRD_PARTY_USER_ID, USER_ID, EMAIL, TIME_JOINED);
            transaction.commit();
        } catch (PersistenceException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            assertTrue(e instanceof NonUniqueObjectException);
            return;
        }
        fail();

    }

    @Test
    public void updateEmailWhereThirdPartyIdEqualsAndThirdPartyUserIdEquals() {
        Transaction transaction = session.beginTransaction();
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID, THIRD_PARTY_USER_ID, USER_ID, EMAIL, TIME_JOINED);

        thirdPartyUsersDAO.updateEmailWhereThirdPartyIdEqualsAndThirdPartyUserIdEquals(THIRD_PARTY_ID,
                THIRD_PARTY_USER_ID, EMAIL + "Hello");
        transaction.commit();

        session.clear();

        ThirdPartyUsersDO usersDO = thirdPartyUsersDAO
                .getWhereThirdPartyIDEqualsAndThirdPartyUserIdEquals(THIRD_PARTY_ID, THIRD_PARTY_USER_ID);

        assertTrue(usersDO.getEmail().equals(EMAIL + "Hello"));
    }

    @Test
    public void updateEmailWhereThirdPartyIdEqualsAndThirdPartyUserIdEqualsException() {
        Transaction transaction = session.beginTransaction();
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID, THIRD_PARTY_USER_ID, USER_ID, EMAIL, TIME_JOINED);
        transaction.commit();
        session.clear();
        try {
            transaction = session.beginTransaction();
            thirdPartyUsersDAO.updateEmailWhereThirdPartyIdEqualsAndThirdPartyUserIdEquals(THIRD_PARTY_ID,
                    THIRD_PARTY_USER_ID + "B", EMAIL + ".com");
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
    public void getWhereThirdPartyIDEqualsAndThirdPartyUserIdEquals_locked() {
        Transaction transaction = session.beginTransaction();
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID, THIRD_PARTY_USER_ID, USER_ID, EMAIL, TIME_JOINED);
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "Two", THIRD_PARTY_USER_ID, USER_ID + "Two", EMAIL,
                TIME_JOINED);
        transaction.commit();
        session.clear();

        transaction = session.beginTransaction();
        ThirdPartyUsersDO thirdPartyUsersDO = thirdPartyUsersDAO
                .getWhereThirdPartyIDEqualsAndThirdPartyUserIdEquals_locked(THIRD_PARTY_ID + "Two",
                        THIRD_PARTY_USER_ID);
        transaction.commit();

        assertTrue(thirdPartyUsersDO != null);
        assertTrue(thirdPartyUsersDO.getEmail().equals(EMAIL));
        assertTrue(thirdPartyUsersDO.getUser_id().equals(USER_ID + "Two"));
        assertTrue(thirdPartyUsersDO.getPrimary_key().getThird_party_id().equals(THIRD_PARTY_ID + "Two"));
        assertTrue(thirdPartyUsersDO.getPrimary_key().getThird_party_user_id().equals(THIRD_PARTY_USER_ID));

    }

    @Test
    public void getWhereThirdPartyIDEqualsAndThirdPartyUserIdEqualsException_locked() {
        Transaction transaction = session.beginTransaction();
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID, THIRD_PARTY_USER_ID, USER_ID, EMAIL, TIME_JOINED);
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "Two", THIRD_PARTY_USER_ID, USER_ID + "Two", EMAIL,
                TIME_JOINED);
        transaction.commit();
        session.clear();
        try {
            transaction = session.beginTransaction();
            ThirdPartyUsersDO thirdPartyUsersDO = thirdPartyUsersDAO
                    .getWhereThirdPartyIDEqualsAndThirdPartyUserIdEquals_locked(THIRD_PARTY_ID + "Three",
                            THIRD_PARTY_USER_ID);
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
    public void getWhereEmailEquals() {
        Transaction transaction = session.beginTransaction();
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID, THIRD_PARTY_USER_ID, USER_ID, EMAIL, TIME_JOINED);
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "Two", THIRD_PARTY_USER_ID, USER_ID + "Two", EMAIL,
                TIME_JOINED);
        transaction.commit();
        session.clear();
        assertTrue(thirdPartyUsersDAO.getWhereEmailEquals(EMAIL).size() == 2);
    }

    @Test
    public void getWhereEmailEqualsEmpty() {
        Transaction transaction = session.beginTransaction();

        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID, THIRD_PARTY_USER_ID, USER_ID, EMAIL, TIME_JOINED);
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "Two", THIRD_PARTY_USER_ID, USER_ID + "Two", EMAIL,
                TIME_JOINED);
        transaction.commit();
        session.clear();
        try {
            thirdPartyUsersDAO.getWhereEmailEquals(EMAIL + ".com");
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();
    }

    @Test
    public void getByTimeJoinedOrderAndUserIdOrderAndLimit() throws InvalidOrderTypeException {
        Transaction transaction = session.beginTransaction();

        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "One", THIRD_PARTY_USER_ID, USER_ID + "One", EMAIL,
                TIME_JOINED);
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "Two", THIRD_PARTY_USER_ID, USER_ID + "Two", EMAIL,
                TIME_JOINED);
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "Three", THIRD_PARTY_USER_ID, USER_ID + "Three", EMAIL,
                TIME_JOINED + 10l);
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "Four", THIRD_PARTY_USER_ID, USER_ID + "Four", EMAIL,
                TIME_JOINED - 20l);
        transaction.commit();
        session.clear();
        List<ThirdPartyUsersDO> resultList = thirdPartyUsersDAO.getByTimeJoinedOrderAndUserIdOrderAndLimit("DESC",
                "DESC", 3);
        assertTrue(resultList.size() == 3);
        assertTrue(resultList.get(0).getTime_joined() == 30l);
        assertTrue(resultList.get(1).getTime_joined() == 20l);
        assertTrue(resultList.get(1).getUser_id().equals(USER_ID + "Two"));

        assertTrue(resultList.get(2).getTime_joined() == 20l);
        assertTrue(resultList.get(2).getUser_id().equals(USER_ID + "One"));

    }

    @Test
    public void getByTimeJoinedOrderAndUserIdOrderAndLimitExceptionOrderType() throws InvalidOrderTypeException {
        Transaction transaction = session.beginTransaction();

        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "One", THIRD_PARTY_USER_ID, USER_ID + "One", EMAIL,
                TIME_JOINED);
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "Two", THIRD_PARTY_USER_ID, USER_ID + "Two", EMAIL,
                TIME_JOINED);
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "Three", THIRD_PARTY_USER_ID, USER_ID + "Three", EMAIL,
                TIME_JOINED + 10l);
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "Four", THIRD_PARTY_USER_ID, USER_ID + "Four", EMAIL,
                TIME_JOINED - 20l);
        transaction.commit();
        session.clear();

        try {
            List<ThirdPartyUsersDO> resultList = thirdPartyUsersDAO.getByTimeJoinedOrderAndUserIdOrderAndLimit("DESCC",
                    "DESC", 3);
        } catch (InvalidOrderTypeException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }

        try {
            List<ThirdPartyUsersDO> resultList = thirdPartyUsersDAO.getByTimeJoinedOrderAndUserIdOrderAndLimit("DESC",
                    "DESCC", 3);
        } catch (InvalidOrderTypeException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case scenario
        }
        fail();

    }

    @Test
    public void getByTimeJoinedOrderAndUserIdOrderAndLimitExceptionNoResult() throws InvalidOrderTypeException {
        try {
            List<ThirdPartyUsersDO> resultList = thirdPartyUsersDAO.getByTimeJoinedOrderAndUserIdOrderAndLimit("DESC",
                    "DESC", 3);
        } catch (NoResultException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing failure case
        }
        fail();

    }

    @Test
    public void getCount() {
        Transaction transaction = session.beginTransaction();

        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "One", THIRD_PARTY_USER_ID, USER_ID + "One", EMAIL,
                TIME_JOINED);
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "Two", THIRD_PARTY_USER_ID, USER_ID + "Two", EMAIL,
                TIME_JOINED);
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "Three", THIRD_PARTY_USER_ID, USER_ID + "Three", EMAIL,
                TIME_JOINED + 10l);
        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "Four", THIRD_PARTY_USER_ID, USER_ID + "Four", EMAIL,
                TIME_JOINED - 20l);
        transaction.commit();
        session.clear();

        assertTrue(thirdPartyUsersDAO.getCount() == 4l);

    }

    @Test
    public void getCountZero() {

        assertTrue(thirdPartyUsersDAO.getCount() == 0l);

    }

    @Test
    public void deleteWhereUserIdEquals() {
        Transaction transaction = session.beginTransaction();

        thirdPartyUsersDAO.insertValues(THIRD_PARTY_ID + "One", THIRD_PARTY_USER_ID, USER_ID + "One", EMAIL,
                TIME_JOINED);
        transaction.commit();

        assertTrue(thirdPartyUsersDAO.getCount() == 1);

        transaction = session.beginTransaction();
        thirdPartyUsersDAO.deleteWhereUserIdEquals(USER_ID + "One");
        transaction.commit();
        assertTrue(thirdPartyUsersDAO.getCount() == 0);

    }
}