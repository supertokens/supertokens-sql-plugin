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
import io.supertokens.storage.sql.domainobjects.general.UsersDO;
import io.supertokens.storage.sql.test.HibernateUtilTest;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.supertokens.storage.sql.TestConstants.*;
import static org.junit.Assert.*;

public class UsersDAOTest {

    UsersDAO usersDAO;
    Session session;
    SessionObject sessionObject;

    @Before
    public void before() throws InterruptedException {
        session = HibernateUtilTest.getSessionFactory().openSession();
        sessionObject = new SessionObject(session);
        usersDAO = new UsersDAO(sessionObject);
        Transaction transaction = session.beginTransaction();
        usersDAO.removeAll();
        transaction.commit();
    }

    @After
    public void after() {
        Transaction transaction = session.beginTransaction();
        usersDAO.removeAll();
        transaction.commit();
        session.close();
    }

    @Test
    public void insertIntoTableValues() {
        assertTrue(usersDAO.getAll().size() == 0);
        Transaction transaction = session.beginTransaction();
        UsersDO usersDO = usersDAO.insertIntoTableValues(USER_ID, RECIPE_ID, TIME_JOINED);
        transaction.commit();
        assertTrue(usersDAO.getAll().size() == 1);
        assertTrue(usersDO.equals(usersDAO.getAll().get(0)));
    }

    @Test
    public void deleteWhereUserIdEqualsAndRecipeIdEquals() {
        assertTrue(usersDAO.getAll().size() == 0);
        Transaction transaction = session.beginTransaction();
        UsersDO usersDO = usersDAO.insertIntoTableValues(USER_ID, RECIPE_ID, TIME_JOINED);
        usersDAO.insertIntoTableValues(USER_ID + "two", RECIPE_ID, TIME_JOINED);
        transaction.commit();
        assertTrue(usersDAO.getAll().size() == 2);
        transaction = session.beginTransaction();
        usersDAO.deleteWhereUserIdEqualsAndRecipeIdEquals(USER_ID, RECIPE_ID);
        transaction.commit();
        assertTrue(usersDAO.getAll().size() == 1);
    }
}