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

package io.supertokens.storage.sql.dataaccessobjects.passwordless;

import io.supertokens.pluginInterface.emailpassword.exceptions.UnknownUserIdException;
import io.supertokens.storage.sql.dataaccessobjects.DAO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessUsersDO;

public interface PasswordlessUsersInterfaceDAO extends DAO {

    /**
     * String QUERY = "UPDATE " + Config.getConfig(start).getPasswordlessUsersTable()
     * + " SET email = ? WHERE user_id = ?";
     */
    public void updateEmailWhereUserIdEquals(String userId, String email) throws UnknownUserIdException;

    /**
     * insert values into table
     */
    public String insertValuesIntoTable(String userId, String emailId, String phoneNumber, long timeJoined);

    /**
     * get where user id equals
     */
    public PasswordlessUsersDO getWhereUserIdEquals(String userId);

    /**
     * String QUERY = "UPDATE " + Config.getConfig(start).getPasswordlessUsersTable()
     * + " SET phone_number = ? WHERE user_id = ?";
     */
    public void updatePhoneNumberWhereUserIdEquals(String userId, String phoneNumber) throws UnknownUserIdException;

    /**
     * String QUERY = "SELECT user_id, email, phone_number, time_joined FROM "
     * + Config.getConfig(start).getPasswordlessUsersTable() + " WHERE email = ?";
     */
    public PasswordlessUsersDO getUserWhereEmailEquals(String email);

    /**
     * String QUERY = "SELECT user_id, email, phone_number, time_joined FROM "
     * + Config.getConfig(start).getPasswordlessUsersTable() + " WHERE phone_number = ?";
     */
    public PasswordlessUsersDO getUserWherePhoneNumberEquals(String phoneNumber);
}
