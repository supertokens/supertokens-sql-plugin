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

package io.supertokens.storage.sql.dataaccessobjects.emailpassword.impl;

import io.supertokens.pluginInterface.emailpassword.exceptions.UnknownUserIdException;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordPswdResetTokensDO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordPswdResetTokensPKDO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordUsersDO;
import io.supertokens.storage.sql.enums.OrderEnum;
import io.supertokens.storage.sql.test.HibernateUtilTest;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.NoResultException;
import java.io.Serializable;
import java.util.List;

import static io.supertokens.storage.sql.TestConstants.*;
import static org.junit.Assert.*;

public class EmailPasswordUsersDAOTest {

    EmailPasswordUsersDAO emailPasswordUsersDAO;
    EmailPasswordPswdResetTokensDAO emailPasswordPswdResetTokensDAO;
    Session session;

    @Before
    public void beforeTest() throws InterruptedException {
        session = HibernateUtilTest.getSessionFactory().openSession();
        emailPasswordUsersDAO = new EmailPasswordUsersDAO(session);
        emailPasswordPswdResetTokensDAO = new EmailPasswordPswdResetTokensDAO(session);
        Transaction transaction = session.beginTransaction();
        emailPasswordUsersDAO.removeAll();
        transaction.commit();
    }

    @After
    public void afterTest() {
        Transaction transaction = session.beginTransaction();
        emailPasswordUsersDAO.removeAll();
        transaction.commit();
        session.close();
    }

    /**
     * Test entity creation
     */
    @Test
    public void createOnce() throws Exception {
        Transaction transaction = session.beginTransaction();
        EmailPasswordUsersDO emailPasswordUsersDO = createEmailPasswordUsersDO();
        Serializable savedId = emailPasswordUsersDAO.create(emailPasswordUsersDO);
        transaction.commit();

        assertNotNull(savedId);
        assertEquals(savedId.toString(), emailPasswordUsersDO.getUser_id());
    }

    @Test
    public void createDuplicate() throws Exception {

        this.createOnce();
        Transaction transaction = session.beginTransaction();

        try {
            EmailPasswordUsersDO emailPasswordUsersDO = createEmailPasswordUsersDO(EMAIL + ".com", PASS_HASH,
                    CREATED_AT, USER_ID);
            Serializable savedId = emailPasswordUsersDAO.create(emailPasswordUsersDO);
            transaction.commit();
        } catch (Exception p) {

            if (transaction != null) {
                transaction.rollback();
            }

            assertTrue(p instanceof NonUniqueObjectException);
            return;

        }

        fail();
    }

    @Test
    public void get() throws Exception {
        this.createOnce();

        EmailPasswordUsersDO emailPasswordUsersDO1 = emailPasswordUsersDAO.get(USER_ID);
        assertNotNull(emailPasswordUsersDO1);
        assertEquals(emailPasswordUsersDO1.getUser_id(), USER_ID);
        assertEquals(emailPasswordUsersDO1.getTime_joined(), CREATED_AT);
        assertEquals(emailPasswordUsersDO1.getPassword_hash(), PASS_HASH);
        assertEquals(emailPasswordUsersDO1.getEmail(), EMAIL);
    }

    @Test
    public void getAll() throws Exception {
        this.createOnce();
        List<EmailPasswordUsersDO> results = emailPasswordUsersDAO.getAll();
        assertNotNull(results);
        assertEquals(results.size(), 1);
    }

    @Test
    public void remove() throws Exception {
        this.createOnce();
        Transaction transaction = session.beginTransaction();
        emailPasswordUsersDAO.removeWhereUserIdEquals(USER_ID);
        transaction.commit();

        List<EmailPasswordUsersDO> results = emailPasswordUsersDAO.getAll();
        assertTrue(results.size() == 0);
    }

    @Test
    public void removeException() throws Exception {
        this.createOnce();
        Transaction transaction = session.beginTransaction();
        try {
            int rows = emailPasswordUsersDAO.removeWhereUserIdEquals(USER_ID + "unknown");
            if (rows == 0) {
                throw new UnknownUserIdException();
            }
            transaction.commit();
        } catch (UnknownUserIdException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            assertTrue(true);
            return;
        } catch (Exception e) {
            // do nothing, failure case
        }
        fail();
    }

    @Test
    public void removeAll() throws Exception {
        this.createOnce();
        Transaction transaction = session.beginTransaction();
        emailPasswordUsersDAO.removeAll();
        transaction.commit();
        List<EmailPasswordUsersDO> results = emailPasswordUsersDAO.getAll();
        assertTrue(results.size() == 0);
    }

    @Test
    public void testDeleteCascade() throws Exception {
        Transaction transaction = session.beginTransaction();

        EmailPasswordUsersDO emailPasswordUsersDO = createEmailPasswordUsersDO();
        emailPasswordUsersDAO.create(emailPasswordUsersDO);

        // CREATE CHILD ENTRY
        EmailPasswordPswdResetTokensPKDO primaryKey = new EmailPasswordPswdResetTokensPKDO();

        primaryKey.setToken(TOKEN);
        primaryKey.setUser_id(emailPasswordUsersDO);

        EmailPasswordPswdResetTokensDO emailPasswordPswdResetTokensDO = new EmailPasswordPswdResetTokensDO();
        emailPasswordPswdResetTokensDO.setPrimaryKey(primaryKey);
        emailPasswordPswdResetTokensDO.setToken_expiry(TOKEN_EXPIRY);
        Serializable id = emailPasswordPswdResetTokensDAO.create(emailPasswordPswdResetTokensDO);

        assertTrue(id != null);
        assertTrue(emailPasswordPswdResetTokensDAO.getAll().size() == 1);
        assertTrue(emailPasswordUsersDAO.getAll().size() == 1);

        emailPasswordUsersDAO.removeWhereUserIdEquals(emailPasswordUsersDO.getUser_id());
        transaction.commit();

        assertTrue(emailPasswordPswdResetTokensDAO.getAll().size() == 0);
        assertTrue(emailPasswordUsersDAO.getAll().size() == 0);
    }

    @Test
    public void updatePasswordHashWhereUserId() throws Exception {
        createOnce();
        Transaction transaction = session.beginTransaction();
        String UPDATED_PASS_HASH = PASS_HASH + "Updated";
        emailPasswordUsersDAO.updatePasswordHashWhereUserId(USER_ID, UPDATED_PASS_HASH);
        transaction.commit();
        session.clear();
        assertTrue(emailPasswordUsersDAO.get(USER_ID).getPassword_hash().equals(UPDATED_PASS_HASH));
    }

    @Test
    public void updateEmailWhereUserId() throws Exception {
        createOnce();
        Transaction transaction = session.beginTransaction();
        String UPDATED_EMAIL = EMAIL + "Updated";
        emailPasswordUsersDAO.updateEmailWhereUserId(USER_ID, UPDATED_EMAIL);
        transaction.commit();
        session.clear();
        assertTrue(emailPasswordUsersDAO.get(USER_ID).getEmail().equals(UPDATED_EMAIL));
    }

    @Test
    public void updateEmailWhereUserIdFail() throws Exception {
        createOnce();
        Transaction transaction = session.beginTransaction();
        String UPDATED_EMAIL = EMAIL + "Updated";
        try {
            int rows = emailPasswordUsersDAO.updateEmailWhereUserId(USER_ID + "unknown", UPDATED_EMAIL);
            if (rows == 0) {
                throw new Exception();
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            assertTrue(e instanceof Exception);
            return;
        }
        fail();
    }

    @Test
    public void insert() {

        Transaction transaction = session.beginTransaction();
        String id = emailPasswordUsersDAO.insert(USER_ID, EMAIL, PASS_HASH, CREATED_AT);
        transaction.commit();
        assertTrue(id != null && id.equals(USER_ID));
        assertTrue(emailPasswordUsersDAO.getAll().size() == 1);

    }

    @Test
    public void insertException() {
        Transaction transaction = session.beginTransaction();
        emailPasswordUsersDAO.insert(USER_ID, EMAIL, PASS_HASH, CREATED_AT);

        try {
            emailPasswordUsersDAO.insert(USER_ID, EMAIL + "change", PASS_HASH, CREATED_AT);
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
    public void getWhereUserIdEquals() {
        Transaction transaction = session.beginTransaction();

        emailPasswordUsersDAO.insert(USER_ID, EMAIL, PASS_HASH, CREATED_AT);

        emailPasswordUsersDAO.insert(USER_ID + "two", EMAIL + "two", PASS_HASH + "two", CREATED_AT);

        EmailPasswordUsersDO emailPasswordUsersDO = emailPasswordUsersDAO.getWhereUserIdEquals_locked(USER_ID);
        transaction.commit();

        assertTrue(emailPasswordUsersDO != null);
        assertTrue(emailPasswordUsersDO.getUser_id().equals(USER_ID));
        assertTrue(emailPasswordUsersDO.getPassword_hash().equals(PASS_HASH));
        assertTrue(emailPasswordUsersDO.getEmail().equals(EMAIL));
        assertTrue(emailPasswordUsersDO.getTime_joined() == CREATED_AT);

    }

    @Test
    public void getWhereUserIdEqualsException() {
        Transaction transaction = session.beginTransaction();

        emailPasswordUsersDAO.insert(USER_ID, EMAIL, PASS_HASH, CREATED_AT);

        emailPasswordUsersDAO.insert(USER_ID + "two", EMAIL + "two", PASS_HASH + "two", CREATED_AT);

        try {
            emailPasswordUsersDAO.getWhereUserIdEquals_locked(USER_ID + "three");
            transaction.commit();

        } catch (Exception n) {

            if (transaction != null) {
                transaction.rollback();
            }
            assertTrue(n instanceof NoResultException);
            return;

        }

        fail();

    }

    @Test
    public void getWhereEmailEquals() {
        Transaction transaction = session.beginTransaction();

        emailPasswordUsersDAO.insert(USER_ID, EMAIL, PASS_HASH, CREATED_AT);

        emailPasswordUsersDAO.insert(USER_ID + "two", EMAIL + "two", PASS_HASH + "two", CREATED_AT);
        transaction.commit();

        assertTrue(emailPasswordUsersDAO.getWhereEmailEquals(EMAIL) != null);
        assertTrue(emailPasswordUsersDAO.getWhereEmailEquals(EMAIL + "two") != null);

    }

    @Test
    public void getWhereEmailEqualsNull() {
        Transaction transaction = session.beginTransaction();

        emailPasswordUsersDAO.insert(USER_ID, EMAIL, PASS_HASH, CREATED_AT);

        emailPasswordUsersDAO.insert(USER_ID + "two", EMAIL + "two", PASS_HASH + "two", CREATED_AT);
        transaction.commit();
        try {
            emailPasswordUsersDAO.getWhereEmailEquals(EMAIL + "three");
        } catch (Exception n) {
            assertTrue(n instanceof NoResultException);
            return;
        }

        fail();

    }

    /**
     * Utility method
     *
     * @return
     */
    private EmailPasswordUsersDO createEmailPasswordUsersDO() {
        return createEmailPasswordUsersDO(EMAIL, PASS_HASH, CREATED_AT, USER_ID);
    }

    private EmailPasswordUsersDO createEmailPasswordUsersDO(String email, String passHash, long createdAt,
            String userId) {
        EmailPasswordUsersDO emailPasswordUsersDO = new EmailPasswordUsersDO();
        emailPasswordUsersDO.setEmail(email);
        emailPasswordUsersDO.setPassword_hash(passHash);
        emailPasswordUsersDO.setTime_joined(createdAt);
        emailPasswordUsersDO.setUser_id(userId);
        return emailPasswordUsersDO;
    }

    @Test
    public void getAllOrderByTimeJoinedAndUserId() {

        Transaction transaction = session.beginTransaction();

        emailPasswordUsersDAO.insert(USER_ID, EMAIL, PASS_HASH, CREATED_AT);

        emailPasswordUsersDAO.insert(USER_ID + "two", EMAIL + "two", PASS_HASH + "two", CREATED_AT + 10);
        emailPasswordUsersDAO.insert(USER_ID + "three", EMAIL + "three", PASS_HASH + "three", CREATED_AT + 20);
        emailPasswordUsersDAO.insert(USER_ID + "four", EMAIL + "four", PASS_HASH + "four", CREATED_AT + 30);
        emailPasswordUsersDAO.insert(USER_ID + "five", EMAIL + "five", PASS_HASH + "five", CREATED_AT + 40);

        transaction.commit();

        List<EmailPasswordUsersDO> list = emailPasswordUsersDAO
                .getLimitedOrderByTimeJoinedAndUserId(OrderEnum.DESC.name(), OrderEnum.DESC.name(), 3);

        assertEquals(list.size(), 3);
        assertEquals(list.get(0).getTime_joined(), TIME_JOINED + 30l);
        assertEquals(list.get(1).getTime_joined(), TIME_JOINED + 20l);
        assertEquals(list.get(2).getTime_joined(), TIME_JOINED + 10l);
    }

    @Test
    public void getLimitedUsersInfo() {
        fail();
    }
}