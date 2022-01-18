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
import io.supertokens.storage.sql.test.HibernateUtilTest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import java.io.Serializable;
import java.util.List;

import static io.supertokens.storage.sql.TestConstants.*;
import static org.junit.Assert.*;

public class EmailPasswordUsersDAOTest {

    private static EmailPasswordUsersDAO emailPasswordUsersDAO;
    private static EmailPasswordPswdResetTokensDAO emailPasswordPswdResetTokensDAO;

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

    // TODO: fix this to boot database from external props
    @BeforeClass
    public static void setUp() throws Exception {

        emailPasswordUsersDAO = new EmailPasswordUsersDAO(HibernateUtilTest.getSessionFactory());
        emailPasswordPswdResetTokensDAO = new EmailPasswordPswdResetTokensDAO(HibernateUtilTest.getSessionFactory());
    }

    @Before
    public void beforeTest() {
        emailPasswordUsersDAO.removeAll();
    }

    @After
    public void afterTest() {
        emailPasswordUsersDAO.removeAll();
    }

    /**
     * Test entity creation
     */
    @Test
    public void createOnce() throws Exception {
        EmailPasswordUsersDO emailPasswordUsersDO = createEmailPasswordUsersDO();
        Serializable savedId = emailPasswordUsersDAO.create(emailPasswordUsersDO);

        assertNotNull(savedId);
        assertEquals(savedId.toString(), emailPasswordUsersDO.getUser_id());
    }

    @Test
    public void createDuplicate() throws Exception {

        try {
            this.createOnce();
            EmailPasswordUsersDO emailPasswordUsersDO = createEmailPasswordUsersDO(
                    EMAIL + ".com", PASS_HASH, CREATED_AT, USER_ID
            );
            Serializable savedId = emailPasswordUsersDAO.create(emailPasswordUsersDO);
        } catch (PersistenceException p) {

            assertNotNull(p);
            assertTrue(p instanceof PersistenceException);
            assertTrue(p.getCause() instanceof ConstraintViolationException);
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
        emailPasswordUsersDAO.removeWhereUserIdEquals(USER_ID);
        List<EmailPasswordUsersDO> results = emailPasswordUsersDAO.getAll();
        assertTrue(results.size() == 0);
    }

    @Test
    public void removeException() throws Exception {
        this.createOnce();
        try {
            emailPasswordUsersDAO.removeWhereUserIdEquals(USER_ID + "unknown");
        } catch (UnknownUserIdException e) {
            assertTrue(true);
            return;
        } catch (Exception e) {
            //do nothing, failure case
        }
        fail();
    }

    @Test
    public void removeAll() throws Exception {
        this.createOnce();
        emailPasswordUsersDAO.removeAll();
        List<EmailPasswordUsersDO> results = emailPasswordUsersDAO.getAll();
        assertTrue(results.size() == 0);
    }

    @Test
    public void testDeleteCascade() throws Exception {
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
        assertTrue(emailPasswordPswdResetTokensDAO.getAll().size() == 0);
        assertTrue(emailPasswordUsersDAO.getAll().size() == 0);

    }

    @Test
    public void updatePasswordHashWhereUserId() throws Exception {
        createOnce();
        String UPDATED_PASS_HASH = PASS_HASH + "Updated";
        emailPasswordUsersDAO.updatePasswordHashWhereUserId(USER_ID, UPDATED_PASS_HASH);
        assertTrue(emailPasswordUsersDAO.get(USER_ID).getPassword_hash().equals(UPDATED_PASS_HASH));
    }

    @Test
    public void updateEmailWhereUserId() throws Exception {
        createOnce();
        String UPDATED_EMAIL = EMAIL + "Updated";
        emailPasswordUsersDAO.updateEmailWhereUserId(USER_ID, UPDATED_EMAIL);
        assertTrue(emailPasswordUsersDAO.get(USER_ID).getEmail().equals(UPDATED_EMAIL));
    }

    @Test
    public void updateEmailWhereUserIdFail() throws Exception {
        createOnce();
        String UPDATED_EMAIL = EMAIL + "Updated";
        try {
            emailPasswordUsersDAO.updateEmailWhereUserId(USER_ID + "unknown", UPDATED_EMAIL);
        } catch (Exception e) {
            assertTrue(e instanceof UnknownUserIdException);
            return;
        }
        fail();
    }

    @Test
    public void insert() {

        String id = emailPasswordUsersDAO.insert(
                USER_ID,
                EMAIL,
                PASS_HASH,
                CREATED_AT
        );

        assertTrue(id != null && id.equals(USER_ID));
        assertTrue(emailPasswordUsersDAO.getAll().size() == 1);

    }

    @Test
    public void insertException() {

        emailPasswordUsersDAO.insert(
                USER_ID,
                EMAIL,
                PASS_HASH,
                CREATED_AT
        );

        try {
            emailPasswordUsersDAO.insert(
                    USER_ID,
                    EMAIL + "change",
                    PASS_HASH,
                    CREATED_AT
            );
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ConstraintViolationException);
            return;
        }
        fail();

    }

    @Test
    public void getWhereUserIdEquals() {
        emailPasswordUsersDAO.insert(
                USER_ID,
                EMAIL,
                PASS_HASH,
                CREATED_AT
        );

        emailPasswordUsersDAO.insert(
                USER_ID + "two",
                EMAIL + "two",
                PASS_HASH + "two",
                CREATED_AT
        );

        EmailPasswordUsersDO emailPasswordUsersDO = emailPasswordUsersDAO.getWhereUserIdEquals(USER_ID);

        assertTrue(emailPasswordUsersDO != null);
        assertTrue(emailPasswordUsersDO.getUser_id().equals(USER_ID));
        assertTrue(emailPasswordUsersDO.getPassword_hash().equals(PASS_HASH));
        assertTrue(emailPasswordUsersDO.getEmail().equals(EMAIL));
        assertTrue(emailPasswordUsersDO.getTime_joined() == CREATED_AT);

    }

    @Test
    public void getWhereUserIdEqualsException() {
        emailPasswordUsersDAO.insert(
                USER_ID,
                EMAIL,
                PASS_HASH,
                CREATED_AT
        );

        emailPasswordUsersDAO.insert(
                USER_ID + "two",
                EMAIL + "two",
                PASS_HASH + "two",
                CREATED_AT
        );
        try {
            emailPasswordUsersDAO.getWhereUserIdEquals(USER_ID + "three");
        } catch (Exception n) {
            assertTrue(n instanceof NoResultException);
            return;
        }

        fail();

    }

    @Test
    public void getWhereEmailEquals() {
        emailPasswordUsersDAO.insert(
                USER_ID,
                EMAIL,
                PASS_HASH,
                CREATED_AT
        );

        emailPasswordUsersDAO.insert(
                USER_ID + "two",
                EMAIL + "two",
                PASS_HASH + "two",
                CREATED_AT
        );

        assertTrue(emailPasswordUsersDAO.getWhereEmailEquals(EMAIL) != null);
        assertTrue(emailPasswordUsersDAO.getWhereEmailEquals(EMAIL+"two") != null);

    }

    @Test
    public void getWhereEmailEqualsNull() {
        emailPasswordUsersDAO.insert(
                USER_ID,
                EMAIL,
                PASS_HASH,
                CREATED_AT
        );

        emailPasswordUsersDAO.insert(
                USER_ID + "two",
                EMAIL + "two",
                PASS_HASH + "two",
                CREATED_AT
        );

        try {
            emailPasswordUsersDAO.getWhereEmailEquals(EMAIL + "three");
        } catch (Exception n) {
            assertTrue(n instanceof NoResultException);
            return;
        }

        fail();

    }
}