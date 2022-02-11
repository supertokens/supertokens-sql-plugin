/*
 *    Copyright (c) 2020, VRAI Labs and/or its affiliates. All rights reserved.
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

package io.supertokens.storage.sql.queries;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.supertokens.pluginInterface.KeyValueInfo;
import io.supertokens.pluginInterface.RowMapper;
import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.pluginInterface.session.SessionInfo;

import io.supertokens.pluginInterface.sqlStorage.SessionObject;
import io.supertokens.storage.sql.Start;
import io.supertokens.storage.sql.config.Config;
import io.supertokens.storage.sql.dataaccessobjects.session.impl.SessionAccessTokenSigningKeysDAO;
import io.supertokens.storage.sql.dataaccessobjects.session.impl.SessionInfoDAO;
import io.supertokens.storage.sql.domainobjects.session.SessionAccessTokenSigningKeysDO;
import io.supertokens.storage.sql.domainobjects.session.SessionInfoDO;
import io.supertokens.storage.sql.exceptions.SessionHandleNotFoundException;
import io.supertokens.storage.sql.exceptions.UserIdNotFoundException;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import javax.annotation.Nullable;
import javax.persistence.NoResultException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class SessionQueries {
    static String getQueryToCreateSessionInfoTable(Start start) {
        return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getSessionInfoTable() + " ("
                + "session_handle VARCHAR(255) NOT NULL," + "user_id VARCHAR(128) NOT NULL,"
                + "refresh_token_hash_2 VARCHAR(128) NOT NULL," + "session_data TEXT," + "expires_at BIGINT  NOT NULL,"
                + "created_at_time BIGINT  NOT NULL," + "jwt_user_payload TEXT," + "PRIMARY KEY(session_handle)"
                + " );";
    }

    static String getQueryToCreateAccessTokenSigningKeysTable(Start start) {
        return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getAccessTokenSigningKeysTable() + " ("
                + "created_at_time BIGINT  NOT NULL," + "value TEXT," + "PRIMARY KEY(created_at_time)" + " );";
    }

    public static void createNewSession(Start start, SessionObject sessionObject, String sessionHandle, String userId,
            String refreshTokenHash2, JsonObject userDataInDatabase, long expiry, JsonObject userDataInJWT,
            long createdAtTime) throws SQLException {

        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(sessionObject);
        sessionInfoDAO.insertIntoTableValues(sessionHandle, userId, refreshTokenHash2, userDataInDatabase.toString(),
                expiry, createdAtTime, userDataInJWT.toString());
    }

    public static SessionInfo getSessionInfo_Transaction(Start start, SessionObject sessionObject, String sessionHandle)
            throws NoResultException {

        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(sessionObject);

        SessionInfoDO sessionInfoDO = sessionInfoDAO.getWhereSessionHandleEquals_locked(sessionHandle);

        JsonParser jp = new JsonParser();

        return new SessionInfo(sessionInfoDO.getSession_handle(), sessionInfoDO.getUser_id(),
                sessionInfoDO.getRefresh_token_hash_2(), jp.parse(sessionInfoDO.getSessions_data()).getAsJsonObject(),
                sessionInfoDO.getExpires_at(), jp.parse(sessionInfoDO.getJwt_user_payload()).getAsJsonObject(),
                sessionInfoDO.getCreated_at_time());
    }

    public static void updateSessionInfo_Transaction(Start start, SessionObject sessionObject, String sessionHandle,
            String refreshTokenHash2, long expiry) throws SQLException, SessionHandleNotFoundException {

        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(sessionObject);
        sessionInfoDAO.updateRefreshTokenTwoAndExpiresAtWhereSessionHandleEquals(refreshTokenHash2, expiry,
                sessionHandle);
    }

    public static int getNumberOfSessions(Start start, SessionObject sessionObject) throws SQLException {
        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(sessionObject);
        // TODO: shouldn't this return long? number rows might not be in int range
        return Math.toIntExact(sessionInfoDAO.getCount());
    }

    public static int deleteSession(Start start, SessionObject sessionObject, String[] sessionHandles)
            throws SQLException {
        if (sessionHandles.length == 0) {
            return 0;
        }
        StringBuilder QUERY = new StringBuilder(
                "DELETE FROM " + Config.getConfig(start).getSessionInfoTable() + " WHERE session_handle IN (");
        for (int i = 0; i < sessionHandles.length; i++) {
            if (i == sessionHandles.length - 1) {
                QUERY.append("?)");
            } else {
                QUERY.append("?, ");
            }
        }
        Session session = (Session) sessionObject.getSession();
        NativeQuery nativeQuery = session.createNativeQuery(QUERY.toString());
        for (int i = 0; i < sessionHandles.length; i++) {
            nativeQuery.setParameter(i + 1, sessionHandles[i]);
        }
        return nativeQuery.executeUpdate();

    }

    public static void deleteSessionsOfUser(Start start, SessionObject sessionObject, String userId)
            throws SQLException, UserIdNotFoundException {
        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(sessionObject);

        sessionInfoDAO.deleteWhereUserIdEquals(userId);
    }

    public static String[] getAllSessionHandlesForUser(Start start, SessionObject sessionObject, String userId)
            throws SQLException, UserIdNotFoundException {

        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(sessionObject);

        return sessionInfoDAO.getSessionHandlesWhereUserIdEquals(userId);

    }

    public static void deleteAllExpiredSessions(Start start, SessionObject sessionObject) throws SQLException {
        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(sessionObject);

        sessionInfoDAO.deleteWhereExpiresLessThan(System.currentTimeMillis());
    }

    public static SessionInfo getSession(Start start, SessionObject sessionObject, String sessionHandle)
            throws SQLException, StorageQueryException {

        SessionInfoDAO sessionInfoDAO = new SessionInfoDAO(sessionObject);

        SessionInfoDO sessionInfoDO = sessionInfoDAO.getWhereSessionHandleEquals_locked(sessionHandle);

        JsonParser jp = new JsonParser();

        return new SessionInfo(sessionInfoDO.getSession_handle(), sessionInfoDO.getUser_id(),
                sessionInfoDO.getRefresh_token_hash_2(), jp.parse(sessionInfoDO.getSessions_data()).getAsJsonObject(),
                sessionInfoDO.getExpires_at(), jp.parse(sessionInfoDO.getJwt_user_payload()).getAsJsonObject(),
                sessionInfoDO.getCreated_at_time());
    }

    public static int updateSession(Start start, SessionObject sessionObject, String sessionHandle,
            @Nullable JsonObject sessionData, @Nullable JsonObject jwtPayload) throws SQLException {

        if (sessionData == null && jwtPayload == null) {
            throw new SQLException("sessionData and jwtPayload are null when updating session info");
        }

        String QUERY = "UPDATE " + Config.getConfig(start).getSessionInfoTable() + " SET";
        boolean somethingBefore = false;
        if (sessionData != null) {
            QUERY += " session_data = ?";
            somethingBefore = true;
        }
        if (jwtPayload != null) {
            QUERY += (somethingBefore ? "," : "") + " jwt_user_payload = ?";
        }
        QUERY += " WHERE session_handle = ?";

        int currIndex = 1;
        Session session = (Session) sessionObject.getSession();
        NativeQuery nativeQuery = session.createNativeQuery(QUERY.toString());

        if (sessionData != null) {
            nativeQuery.setParameter(currIndex, sessionData.toString());
            currIndex++;
        }
        if (jwtPayload != null) {
            nativeQuery.setParameter(currIndex, jwtPayload.toString());
            currIndex++;
        }
        nativeQuery.setParameter(currIndex, sessionHandle);

        return nativeQuery.executeUpdate();

    }

    public static void addAccessTokenSigningKey_Transaction(Start start, SessionObject sessionObject,
            long createdAtTime, String value) {
        SessionAccessTokenSigningKeysDAO signingKeysDAO = new SessionAccessTokenSigningKeysDAO(sessionObject);

        signingKeysDAO.insertIntoTableValues(createdAtTime, value);
    }

    public static KeyValueInfo[] getAccessTokenSigningKeys_Transaction(Start start, SessionObject sessionObject) {

        SessionAccessTokenSigningKeysDAO keysDAO = new SessionAccessTokenSigningKeysDAO(sessionObject);

        List<SessionAccessTokenSigningKeysDO> results = keysDAO.getAll();

        if (results.size() == 0)
            throw new NoResultException();

        KeyValueInfo[] keyValueInfos = new KeyValueInfo[results.size()];
        Iterator<SessionAccessTokenSigningKeysDO> iterator = results.iterator();
        int counter = 0;

        while (iterator.hasNext()) {
            SessionAccessTokenSigningKeysDO keysDO = iterator.next();
            keyValueInfos[counter++] = new KeyValueInfo(keysDO.getValue(), keysDO.getCreated_at_time());
        }

        return keyValueInfos;
    }

    public static void removeAccessTokenSigningKeysBefore(Start start, SessionObject sessionObject, long time)
            throws SQLException {
        SessionAccessTokenSigningKeysDAO keysDAO = new SessionAccessTokenSigningKeysDAO(sessionObject);

        keysDAO.deleteWhereCreatedAtTimeLessThan(time);
    }

    private static class SessionInfoRowMapper implements RowMapper<SessionInfo, ResultSet> {
        private static final SessionInfoRowMapper INSTANCE = new SessionInfoRowMapper();

        private SessionInfoRowMapper() {
        }

        private static SessionInfoRowMapper getInstance() {
            return INSTANCE;
        }

        @Override
        public SessionInfo map(ResultSet result) throws Exception {
            JsonParser jp = new JsonParser();
            return new SessionInfo(result.getString("session_handle"), result.getString("user_id"),
                    result.getString("refresh_token_hash_2"),
                    jp.parse(result.getString("session_data")).getAsJsonObject(), result.getLong("expires_at"),
                    jp.parse(result.getString("jwt_user_payload")).getAsJsonObject(),
                    result.getLong("created_at_time"));
        }
    }

    private static class AccessTokenSigningKeyRowMapper implements RowMapper<KeyValueInfo, ResultSet> {
        private static final AccessTokenSigningKeyRowMapper INSTANCE = new AccessTokenSigningKeyRowMapper();

        private AccessTokenSigningKeyRowMapper() {
        }

        private static AccessTokenSigningKeyRowMapper getInstance() {
            return INSTANCE;
        }

        @Override
        public KeyValueInfo map(ResultSet result) throws Exception {
            return new KeyValueInfo(result.getString("value"), result.getLong("created_at_time"));
        }
    }
}
