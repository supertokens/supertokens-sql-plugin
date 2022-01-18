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

package io.supertokens.storage.sql.dataaccessobjects.session;

import io.supertokens.storage.sql.dataaccessobjects.DAO;
import io.supertokens.storage.sql.domainobjects.session.SessionInfoDO;
import io.supertokens.storage.sql.exceptions.SessionHandleNotFoundException;
import io.supertokens.storage.sql.exceptions.UserIdNotFoundException;

import javax.persistence.NoResultException;
import java.io.Serializable;

public interface SessionInfoInterfaceDAO extends DAO {

    /**
     * String QUERY = "INSERT INTO " + Config.getConfig(start).getSessionInfoTable()
     *                 + "(session_handle, user_id, refresh_token_hash_2, session_data, expires_at, jwt_user_payload, "
     *                 + "created_at_time)" + " VALUES(?, ?, ?, ?, ?, ?, ?)";
     */

    public Serializable insertIntoTableValues(String sessionHandle, String userId, String refreshTokenHashTwo,
                                              String sessionData, long expiresAt, long createdAtTime,
                                              String jwtUserPayload);

    /**
     * String QUERY = "SELECT session_handle, user_id, refresh_token_hash_2, session_data, expires_at, "
     *                 + "created_at_time, jwt_user_payload FROM " + Config.getConfig(start).getSessionInfoTable()
     *                 + " WHERE session_handle = ? FOR UPDATE";
     */
    public SessionInfoDO getWhereSessionHandleEquals_locked(String sessionHandle) throws NoResultException;

    /**
     * String QUERY = "UPDATE " + Config.getConfig(start).getSessionInfoTable()
     *                 + " SET refresh_token_hash_2 = ?, expires_at = ?" + " WHERE session_handle = ?";
     */
    public void updateRefreshTokenTwoAndExpiresAtWhereSessionHandleEquals(String refreshTokenHashTwo,
                                                                            long expiresAt, String sessionHandle)
            throws SessionHandleNotFoundException;

    /**
     *  String QUERY = "DELETE FROM " + Config.getConfig(start).getSessionInfoTable() + " WHERE user_id = ?";
     */
    public void deleteWhereUserIdEquals(String userId) throws UserIdNotFoundException;

    /**
     * String QUERY = "SELECT session_handle FROM " + Config.getConfig(start).getSessionInfoTable()
     *                 + " WHERE user_id = ?";
     */
    public String[] getSessionHandlesWhereUserIdEquals(String userId) throws UserIdNotFoundException;

    /**
     * String QUERY = "DELETE FROM " + Config.getConfig(start).getSessionInfoTable() + " WHERE expires_at <= ?";
     */
    public void deleteWhereExpiresLessThan(long expires);

    /**
     * String QUERY = "SELECT session_handle, user_id, refresh_token_hash_2, session_data, expires_at, "
     *                 + "created_at_time, jwt_user_payload FROM " + Config.getConfig(start).getSessionInfoTable()
     *                 + " WHERE session_handle = ?";
     */
    public SessionInfoDO getWhereSessionHandleEquals(String sessionHandle);

}
