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

package io.supertokens.storage.sql.dataaccessobjects.emailverification;

import io.supertokens.storage.sql.dataaccessobjects.DAO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationTokensDO;
import io.supertokens.storage.sql.exceptions.UserAndEmailNotFoundException;

import javax.persistence.NoResultException;
import java.io.Serializable;
import java.util.List;

public interface EmailVerificationTokensInterfaceDAO extends DAO {

    /**
     * String QUERY = "DELETE FROM " + Config.getConfig(start).getEmailVerificationTokensTable()
     * + " WHERE token_expiry < ?";
     */
    public void deleteFromTableWhereTokenExpiryIsLessThan(long tokenExpiry);

    /**
     * insert row query
     */
    public Serializable insertIntoTable(String userId, String email, String token, long tokenExpiry);

    /**
     * String QUERY = "DELETE FROM " + Config.getConfig(start).getEmailVerificationTokensTable()
     * + " WHERE user_id = ? AND email = ?";
     */
    public void deleteFromTableWhereUserIdEqualsAndEmailEquals(String userId, String email)
            throws UserAndEmailNotFoundException;

    /**
     * tring QUERY = "SELECT user_id, token, token_expiry, email FROM "
     * + Config.getConfig(start).getEmailVerificationTokensTable() + " WHERE token = ?";
     */
    public EmailVerificationTokensDO getEmailVerificationTokenWhereTokenEquals(String token) throws NoResultException;

    /**
     * String QUERY = "SELECT user_id, token, token_expiry, email FROM "
     * + Config.getConfig(start).getEmailVerificationTokensTable()
     * + " WHERE user_id = ? AND email = ? FOR UPDATE";
     */
    public List<EmailVerificationTokensDO> getLockedEmailVerificationTokenWhereUserIdEqualsAndEmailEquals(String userId,
            String email);

    /**
     * String QUERY = "SELECT user_id, token, token_expiry, email FROM "
     * + Config.getConfig(start).getEmailVerificationTokensTable() + " WHERE user_id = ? AND email = ?";
     */
    public List<EmailVerificationTokensDO> getEmailVerificationTokenWhereUserIdEqualsAndEmailEquals(String userId,
            String email);

    /**
     * String QUERY = "DELETE FROM " + Config.getConfig(start).getEmailVerificationTokensTable()
     * + " WHERE user_id = ? AND email = ?";
     */
    public void deleteWhereUserIdEqualsAndEmailEquals(String userId, String email);
}
