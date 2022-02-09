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
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordUsersDO;

import java.io.Serializable;
import java.util.List;

public interface EmailPasswordUsersInterfaceDAO extends DAO<EmailPasswordUsersDO> {
    /**
     * String QUERY = "UPDATE " + Config.getConfig(start).getEmailPasswordUsersTable()
     * + " SET password_hash = ? WHERE user_id = ?";
     */
    public void updatePasswordHashWhereUserId(String user_id, String password_hash);

    /**
     * String QUERY = "UPDATE " + Config.getConfig(start).getEmailPasswordUsersTable()
     * + " SET email = ? WHERE user_id = ?";
     */
    public int updateEmailWhereUserId(String user_id, String email) throws UnknownUserIdException;

    /**
     * String QUERY = "INSERT INTO " + Config.getConfig(start).getEmailPasswordUsersTable()
     * + "(user_id, email, password_hash, time_joined)" + " VALUES(?, ?, ?, ?)";
     */
    public Serializable insert(String userId, String email, String passwordHash, long timeJoined);

    /**
     * String QUERY = "SELECT user_id, email, password_hash, time_joined FROM "
     * + Config.getConfig(start).getEmailPasswordUsersTable() + " WHERE user_id = ? FOR UPDATE";
     */
    public EmailPasswordUsersDO getWhereUserIdEquals_locked(String userId);

    /**
     * String QUERY = "SELECT user_id, email, password_hash, time_joined FROM "
     * + Config.getConfig(start).getEmailPasswordUsersTable() + " WHERE email = ?";
     */
    public EmailPasswordUsersDO getWhereEmailEquals(String email);

    /**
     * String QUERY = "SELECT user_id, email, password_hash, time_joined FROM "
     * + Config.getConfig(start).getEmailPasswordUsersTable() + " ORDER BY time_joined " +
     * timeJoinedOrder
     * + ", user_id DESC LIMIT ?";
     */
    public List<EmailPasswordUsersDO> getLimitedOrderByTimeJoinedAndUserId(String timeJoinedOrder, String userIdOrder,
            int limit);

    /**
     * String QUERY = "SELECT user_id, email, password_hash, time_joined FROM "
     * + Config.getConfig(start).getEmailPasswordUsersTable() + " WHERE time_joined " +
     * timeJoinedOrderSymbol
     * + " ? OR (time_joined = ? AND user_id <= ?) ORDER BY time_joined " + timeJoinedOrder
     * + ", user_id DESC LIMIT ?";
     */
    public List<EmailPasswordUsersDO> getLimitedUsersInfo(String timeJoinedOrder, Long timeJoined, String userIdOrder,
            String userId, int limit);

    /**
     * String QUERY = "SELECT COUNT(*) as total FROM " + Config.getConfig(start).getEmailPasswordUsersTable();
     *
     * @return
     */
    public Long getCount();
}