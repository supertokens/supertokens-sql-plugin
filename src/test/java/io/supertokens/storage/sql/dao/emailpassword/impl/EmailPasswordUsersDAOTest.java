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

package io.supertokens.storage.sql.dao.emailpassword.impl;

import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordPswdResetTokensDO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordPswdResetTokensDOPK;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordUsersDO;
import io.supertokens.storage.sql.test.HibernateUtilTest;
import io.supertokens.storage.sql.test.TestUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.PersistenceException;
import java.io.Serializable;
import java.util.List;

import static io.supertokens.storage.sql.TestConstants.*;
import static org.junit.Assert.*;

public class EmailPasswordUsersDAOTest {

    private static EmailPasswordUsersDAO emailPasswordUsersDAO;
    private static EmailPasswordPswdResetTokensDAO emailPasswordPswdResetTokensDAO;

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
    public void createOnce() {
        EmailPasswordUsersDO emailPasswordUsersDO = createEmailPasswordUsersDO();
        Serializable savedId = emailPasswordUsersDAO.create(emailPasswordUsersDO);

        assertNotNull(savedId);
        assertEquals(savedId.toString(), emailPasswordUsersDO.getUser_id());
    }

    @Test
    public void createDuplicate() {

        try {
            this.createOnce();
            this.createOnce();
        } catch (PersistenceException p) {

            assertNotNull(p);
            assertTrue(p instanceof PersistenceException);
            assertTrue(p.getCause() instanceof ConstraintViolationException);
            return;

        }

        fail();
    }

    @Test
    public void get() {
        this.createOnce();

        EmailPasswordUsersDO emailPasswordUsersDO1 = emailPasswordUsersDAO.get(USER_ID);
        assertNotNull(emailPasswordUsersDO1);
        assertEquals(emailPasswordUsersDO1.getUser_id(), USER_ID);
        assertEquals(emailPasswordUsersDO1.getTime_joined(), CREATED_AT);
        assertEquals(emailPasswordUsersDO1.getPassword_hash(), PASS_HASH);
        assertEquals(emailPasswordUsersDO1.getEmail(), EMAIL);
    }

    @Test
    public void getAll() {
        this.createOnce();
        List<EmailPasswordUsersDO> results = emailPasswordUsersDAO.getAll();
        assertNotNull(results);
        assertEquals(results.size(), 1);
    }

    @Test
    public void remove() {
        this.createOnce();
        emailPasswordUsersDAO.remove(USER_ID);
        List<EmailPasswordUsersDO> results = emailPasswordUsersDAO.getAll();
        assertTrue(results.size() == 0);
    }

    @Test
    public void removeAll() {
        this.createOnce();
        emailPasswordUsersDAO.removeAll();
        List<EmailPasswordUsersDO> results = emailPasswordUsersDAO.getAll();
        assertTrue(results.size() == 0);
    }

    @Test
    public void testDeleteCascade() {
        EmailPasswordUsersDO emailPasswordUsersDO = createEmailPasswordUsersDO();
        emailPasswordUsersDAO.create(emailPasswordUsersDO);

        // CREATE CHILD ENTRY
        EmailPasswordPswdResetTokensDOPK primaryKey = new EmailPasswordPswdResetTokensDOPK();

        primaryKey.setToken(TOKEN);
        primaryKey.setUser_id(emailPasswordUsersDO);

        EmailPasswordPswdResetTokensDO emailPasswordPswdResetTokensDO = new EmailPasswordPswdResetTokensDO();
        emailPasswordPswdResetTokensDO.setPrimaryKey(primaryKey);
        emailPasswordPswdResetTokensDO.setToken_expiry(TOKEN_EXPIRY);
        Serializable id = emailPasswordPswdResetTokensDAO.create(emailPasswordPswdResetTokensDO);

        assertTrue(id != null);
        assertTrue(emailPasswordPswdResetTokensDAO.getAll().size() == 1);
        assertTrue(emailPasswordUsersDAO.getAll().size() == 1);

        emailPasswordUsersDAO.remove(emailPasswordUsersDO.getUser_id());
        assertTrue(emailPasswordPswdResetTokensDAO.getAll().size() == 0);
        assertTrue(emailPasswordUsersDAO.getAll().size() == 0);

    }

    /**
     * Utility method
     * 
     * @return
     */

    private EmailPasswordUsersDO createEmailPasswordUsersDO() {
        EmailPasswordUsersDO emailPasswordUsersDO = new EmailPasswordUsersDO();
        emailPasswordUsersDO.setEmail(EMAIL);
        emailPasswordUsersDO.setPassword_hash(PASS_HASH);
        emailPasswordUsersDO.setTime_joined(CREATED_AT);
        emailPasswordUsersDO.setUser_id(USER_ID);
        return emailPasswordUsersDO;
    }
}