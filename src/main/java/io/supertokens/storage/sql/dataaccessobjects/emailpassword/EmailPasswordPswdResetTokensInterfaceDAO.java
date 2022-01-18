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

package io.supertokens.storage.sql.dataaccessobjects.emailpassword;

import io.supertokens.pluginInterface.emailpassword.exceptions.UnknownUserIdException;
import io.supertokens.storage.sql.dataaccessobjects.DAO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordPswdResetTokensDO;

import java.io.Serializable;
import java.util.List;

public interface EmailPasswordPswdResetTokensInterfaceDAO extends DAO<EmailPasswordPswdResetTokensDO> {

    /**
     * String QUERY = "DELETE FROM " + Config.getConfig(start).getPasswordResetTokensTable()
     * + " WHERE token_expiry < ?";
     */
    public void deleteWhereTokenExpiryIsLessThan(long tokenExpiry);

    /**
     * String QUERY = "DELETE FROM " + Config.getConfig(start).getPasswordResetTokensTable() + " WHERE
     * user_id = ?";
     */
    public void deleteAllWhereUserIdEquals(String userId);

    /**
     * String QUERY = "SELECT user_id, token, token_expiry FROM "
     * + Config.getConfig(start).getPasswordResetTokensTable() + " WHERE user_id = ?";
     */
    public List<EmailPasswordPswdResetTokensDO> getAllPasswordResetTokenInfoForUser(String userId);

    /**
     * String QUERY = "SELECT user_id, token, token_expiry FROM "
     * + Config.getConfig(start).getPasswordResetTokensTable() + " WHERE user_id = ? FOR UPDATE";
     */
    public List<EmailPasswordPswdResetTokensDO> lockAndgetAllPasswordResetTokenInfoForUser(String userId);

    /**
     * String QUERY = "SELECT user_id, token, token_expiry FROM "
     * + Config.getConfig(start).getPasswordResetTokensTable() + " WHERE token = ?";
     */
    public EmailPasswordPswdResetTokensDO getPasswordResetTokenInfo(String token);

    /**
     * String QUERY = "INSERT INTO " + Config.getConfig(start).getPasswordResetTokensTable()
     * + "(user_id, token, token_expiry)" + " VALUES(?, ?, ?)";
     */
    public Serializable insertPasswordResetTokenInfo(String userId, String token, long tokenExpiry)
            throws UnknownUserIdException;
}
