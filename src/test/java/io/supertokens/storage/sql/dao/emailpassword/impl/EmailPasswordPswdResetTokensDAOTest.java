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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class EmailPasswordPswdResetTokensDAOTest {

    private static EmailPasswordPswdResetTokensDAO emailPasswordPswdResetTokensDAO;
    private static EmailPasswordUsersDAO emailPasswordUsersDAO;

    @BeforeClass
    public static void beforeClass() throws Exception {
        emailPasswordPswdResetTokensDAO = new EmailPasswordPswdResetTokensDAO(HibernateUtilTest.getSessionFactory());
        emailPasswordUsersDAO = new EmailPasswordUsersDAO(HibernateUtilTest.getSessionFactory());
    }

    @Before
    public void setUp() throws Exception {
        emailPasswordUsersDAO.removeAll();
    }

    @After
    public void tearDown() throws Exception {
        emailPasswordUsersDAO.removeAll();
    }

    @Test
    public void createFailParentKey() {
        EmailPasswordPswdResetTokensDO emailPasswordPswdResetTokensDO = new EmailPasswordPswdResetTokensDO();
        EmailPasswordPswdResetTokensDOPK primaryKey = new EmailPasswordPswdResetTokensDOPK();

        EmailPasswordUsersDO emailPasswordUsersDO = new EmailPasswordUsersDO();
        emailPasswordUsersDO.setEmail(EMAIL);
        emailPasswordUsersDO.setPassword_hash(PASS_HASH);
        emailPasswordUsersDO.setTime_joined(CREATED_AT);
        emailPasswordUsersDO.setUser_id(USER_ID);

        primaryKey.setToken(TOKEN);
        primaryKey.setUser_id(emailPasswordUsersDO);

        emailPasswordPswdResetTokensDO.setPrimaryKey(primaryKey);
        emailPasswordPswdResetTokensDO.setToken_expiry(TOKEN_EXPIRY);

        try {
            Serializable id = emailPasswordPswdResetTokensDAO.create(emailPasswordPswdResetTokensDO);
        } catch (PersistenceException p) {
            assertTrue(p.getCause() instanceof ConstraintViolationException);
            return;
        }

        fail();
    }

    @Test
    public void createSuccess() {

        EmailPasswordPswdResetTokensDO emailPasswordPswdResetTokensDO = new EmailPasswordPswdResetTokensDO();
        EmailPasswordPswdResetTokensDOPK primaryKey = new EmailPasswordPswdResetTokensDOPK();

        primaryKey.setToken(TOKEN);
        primaryKey.setUser_id(createParentEntry());

        emailPasswordPswdResetTokensDO.setPrimaryKey(primaryKey);
        emailPasswordPswdResetTokensDO.setToken_expiry(TOKEN_EXPIRY);
        Serializable id = emailPasswordPswdResetTokensDAO.create(emailPasswordPswdResetTokensDO);
        assertTrue(id != null);
        assertTrue(((EmailPasswordPswdResetTokensDOPK) id).getToken().equals(TOKEN));
        assertTrue(((EmailPasswordPswdResetTokensDOPK) id).getUser_id().getUser_id().equals(USER_ID));

    }

    @Test
    public void get() {
        this.createSuccess();
        EmailPasswordUsersDO emailPasswordUsersDO = emailPasswordUsersDAO.get(USER_ID);
        EmailPasswordPswdResetTokensDO emailPasswordPswdResetTokensDO = emailPasswordPswdResetTokensDAO
                .get(new EmailPasswordPswdResetTokensDOPK(emailPasswordUsersDO, TOKEN));

        assertTrue(emailPasswordPswdResetTokensDO != null);
        assertTrue(emailPasswordPswdResetTokensDO.getToken_expiry() == TOKEN_EXPIRY);
        assertTrue(emailPasswordPswdResetTokensDO.getPrimaryKey().getToken() == TOKEN);
        assertTrue(emailPasswordPswdResetTokensDO.getPrimaryKey().getUser_id() == emailPasswordUsersDO);

    }

    @Test
    public void getAll() {
        this.createSuccess();
        List<EmailPasswordPswdResetTokensDO> results = emailPasswordPswdResetTokensDAO.getAll();
        assertTrue(results != null);
        assertTrue(results.size() == 1);
    }

    @Test
    public void remove() {
        this.createSuccess();
        EmailPasswordUsersDO emailPasswordUsersDO = emailPasswordUsersDAO.get(USER_ID);

        emailPasswordPswdResetTokensDAO.remove(new EmailPasswordPswdResetTokensDOPK(emailPasswordUsersDO, TOKEN));

        List<EmailPasswordPswdResetTokensDO> results = emailPasswordPswdResetTokensDAO.getAll();
        assertTrue(results.size() == 0);

    }

    private EmailPasswordUsersDO createParentEntry() {
        EmailPasswordUsersDO emailPasswordUsersDO = new EmailPasswordUsersDO();
        emailPasswordUsersDO.setEmail(EMAIL);
        emailPasswordUsersDO.setPassword_hash(PASS_HASH);
        emailPasswordUsersDO.setTime_joined(CREATED_AT);
        emailPasswordUsersDO.setUser_id(USER_ID);
        emailPasswordUsersDAO.create(emailPasswordUsersDO);
        return emailPasswordUsersDO;
    }

    @Test
    public void removeAll() {
        this.createSuccess();
        EmailPasswordUsersDO emailPasswordUsersDO = emailPasswordUsersDAO.get(USER_ID);

        emailPasswordPswdResetTokensDAO.removeAll();

        List<EmailPasswordPswdResetTokensDO> results = emailPasswordPswdResetTokensDAO.getAll();
        assertTrue(results.size() == 0);

    }
}