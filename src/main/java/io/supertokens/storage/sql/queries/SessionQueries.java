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
import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.pluginInterface.session.SessionInfo;
import io.supertokens.storage.sql.ConnectionPool;
import io.supertokens.storage.sql.Start;
import io.supertokens.storage.sql.config.Config;
import io.supertokens.storage.sql.domainobject.session.SessionAccessTokenSigningKeysDO;
import io.supertokens.storage.sql.domainobject.session.SessionInfoDO;
import io.supertokens.storage.sql.hibernate.CustomQueryWrapper;
import io.supertokens.storage.sql.hibernate.CustomSessionWrapper;
import io.supertokens.storage.sql.utils.Utils;

import javax.annotation.Nullable;
import javax.persistence.LockModeType;
import java.sql.SQLException;
import java.util.List;

import static java.lang.System.currentTimeMillis;

public class SessionQueries {

    public static String getQueryToCreateSessionInfoTable(Start start) {
        String schema = Config.getConfig(start).getTableSchema();
        String sessionInfoTable = Config.getConfig(start).getSessionInfoTable();
        // @formatter:off
        return "CREATE TABLE IF NOT EXISTS " + sessionInfoTable + " ("
                + "session_handle VARCHAR(255) NOT NULL,"
                + "user_id VARCHAR(128) NOT NULL,"
                + "refresh_token_hash_2 VARCHAR(128) NOT NULL,"
                + "session_data TEXT,"
                + "expires_at BIGINT NOT NULL,"
                + "created_at_time BIGINT NOT NULL,"
                + "jwt_user_payload TEXT,"
                + "CONSTRAINT " + Utils.getConstraintName(schema, sessionInfoTable, null, "pkey") +
                " PRIMARY KEY(session_handle)" + " );";
        // @formatter:on

    }

    static String getQueryToCreateAccessTokenSigningKeysTable(Start start) {
        String schema = Config.getConfig(start).getTableSchema();
        String accessTokenSigningKeysTable = Config.getConfig(start).getAccessTokenSigningKeysTable();
        // @formatter:off
        return "CREATE TABLE IF NOT EXISTS " + accessTokenSigningKeysTable + " ("
                + "created_at_time BIGINT NOT NULL,"
                + "value TEXT,"
                + "CONSTRAINT " + Utils.getConstraintName(schema, accessTokenSigningKeysTable, null, "pkey") +
                " PRIMARY KEY(created_at_time)" + " );";
        // @formatter:on
    }

    public static void createNewSession(Start start, String sessionHandle, String userId, String refreshTokenHash2,
            JsonObject userDataInDatabase, long expiry, JsonObject userDataInJWT, long createdAtTime)
            throws SQLException, StorageQueryException {

        ConnectionPool.withSession(start, (session, con) -> {
            final SessionInfoDO sessionInfoDO = new SessionInfoDO(sessionHandle, userId, refreshTokenHash2,
                    userDataInDatabase.toString(), userDataInJWT.toString(), expiry, createdAtTime);

            session.save(SessionInfoDO.class, sessionHandle, sessionInfoDO);
            return null;
        }, true);
    }

    static boolean isSessionBlacklisted(Start start, String sessionHandle) throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity.session_handle FROM SessionInfoDO entity WHERE entity.session_handle = "
                    + ":session_handle";
            final CustomQueryWrapper<String> query = session.createQuery(QUERY, String.class);
            query.setParameter("session_handle", sessionHandle);

            final List<String> result = query.list();
            return !result.iterator().hasNext();
        }, false);
    }

    public static SessionInfo getSessionInfo_Transaction(CustomSessionWrapper session, String sessionHandle)
            throws SQLException, StorageQueryException {

        String QUERY = "SELECT entity FROM SessionInfoDO entity WHERE entity.session_handle = " + ":session_handle";
        final CustomQueryWrapper<SessionInfoDO> query = session.createQuery(QUERY, SessionInfoDO.class);
        query.setParameter("session_handle", sessionHandle);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);

        final List<SessionInfoDO> result = query.list();
        if (result.isEmpty()) {
            return null;
        }
        final SessionInfoDO sessionInfoDO = result.get(0);
        final JsonParser jsonParser = new JsonParser();
        return new SessionInfo(sessionInfoDO.getSession_handle(), sessionInfoDO.getUser_id(),
                sessionInfoDO.getRefresh_token_hash_2(),
                jsonParser.parse(sessionInfoDO.getJwt_user_payload()).getAsJsonObject(), sessionInfoDO.getExpires_at(),
                jsonParser.parse(sessionInfoDO.getJwt_user_payload()).getAsJsonObject(),
                sessionInfoDO.getCreated_at_time());
    }

    public static void updateSessionInfo_Transaction(CustomSessionWrapper session, String sessionHandle,
            String refreshTokenHash2, long expiry) throws SQLException, StorageQueryException {
        String QUERY = "UPDATE SessionInfoDO entity"
                + " SET entity.refresh_token_hash_2 = :refresh_token_hash_2, entity.expires_at = :expires_at"
                + " WHERE entity.session_handle = :session_handle";

        CustomQueryWrapper q = session.createQuery(QUERY);
        q.setParameter("refresh_token_hash_2", refreshTokenHash2);
        q.setParameter("expires_at", expiry);
        q.setParameter("session_handle", sessionHandle);
        q.executeUpdate();
    }

    public static int getNumberOfSessions(Start start) throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            CustomQueryWrapper<Integer> q = session.createQuery("SELECT COUNT(*) as num FROM SessionInfoDO",
                    Integer.class);
            List<Integer> result = q.list();
            return result.get(0);
        }, false);
    }

    public static int deleteSession(Start start, String[] sessionHandles) throws SQLException, StorageQueryException {
        if (sessionHandles.length == 0) {
            return 0;
        }

        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "DELETE FROM SessionInfoDO entity where entity.session_handle in (:session_handles)";
            final CustomQueryWrapper query = session.createQuery(QUERY);
            query.setParameterList("session_handles", sessionHandles);

            return query.executeUpdate();
        }, true);
    }

    public static void deleteSessionsOfUser(Start start, String userId) throws SQLException, StorageQueryException {
        ConnectionPool.withSession(start, (session, con) -> {
            CustomQueryWrapper q = session
                    .createQuery("DELETE  FROM SessionInfoDO entity WHERE entity.user_id = :user_id");
            q.setParameter("user_id", userId).executeUpdate();
            return null;
        }, true);

    }

    public static String[] getAllNonExpiredSessionHandlesForUser(Start start, String userId)
            throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity.session_handle FROM SessionInfoDO entity"
                    + " WHERE entity.user_id = :user_id AND entity.expires_at >= :expires_at";
            CustomQueryWrapper<String> q = session.createQuery(QUERY, String.class);
            q.setParameter("user_id", userId);
            q.setParameter("expires_at", currentTimeMillis());

            return q.list().toArray(String[]::new);
        }, false);
    }

    public static void deleteAllExpiredSessions(Start start) throws SQLException, StorageQueryException {
        ConnectionPool.withSession(start, (session, con) -> {
            CustomQueryWrapper q = session
                    .createQuery("DELETE  FROM SessionInfoDO entity WHERE entity.expires_at <= :expires_at");
            q.setParameter("expires_at", currentTimeMillis()).executeUpdate();
            return null;
        }, true);
    }

    public static int updateSession(Start start, String sessionHandle, @Nullable JsonObject sessionData,
            @Nullable JsonObject jwtPayload) throws SQLException, StorageQueryException {

        if (sessionData == null && jwtPayload == null) {
            throw new SQLException("sessionData and jwtPayload are null when updating session info");
        }

        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "UPDATE SessionInfoDO entity SET";
            boolean somethingBefore = false;
            if (sessionData != null) {
                QUERY += " entity.session_data = :session_data";
                somethingBefore = true;
            }
            if (jwtPayload != null) {
                QUERY += (somethingBefore ? "," : "") + " entity.jwt_user_payload = :jwt_user_payload";
            }
            QUERY += " WHERE entity.session_handle = :session_handle";

            final CustomQueryWrapper query = session.createQuery(QUERY);

            if (sessionData != null) {
                query.setParameter("session_data", sessionData.toString());
            }
            if (jwtPayload != null) {
                query.setParameter("jwt_user_payload", jwtPayload.toString());
            }
            query.setParameter("session_handle", sessionHandle);

            return query.executeUpdate();
        }, true);
    }

    public static SessionInfo getSession(Start start, String sessionHandle) throws SQLException, StorageQueryException {

        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity FROM SessionInfoDO entity WHERE entity.session_handle = :session_handle";

            CustomQueryWrapper<SessionInfoDO> q = session.createQuery(QUERY, SessionInfoDO.class);
            q.setParameter("session_handle", sessionHandle);

            final List<SessionInfoDO> result = q.list();
            if (result.isEmpty()) {
                return null;
            }
            final SessionInfoDO sessionInfoDO = result.get(0);
            final JsonParser jsonParser = new JsonParser();
            return new SessionInfo(sessionInfoDO.getSession_handle(), sessionInfoDO.getUser_id(),
                    sessionInfoDO.getRefresh_token_hash_2(),
                    jsonParser.parse(sessionInfoDO.getJwt_user_payload()).getAsJsonObject(),
                    sessionInfoDO.getExpires_at(),
                    jsonParser.parse(sessionInfoDO.getJwt_user_payload()).getAsJsonObject(),
                    sessionInfoDO.getCreated_at_time());
        }, false);
    }

    public static void addAccessTokenSigningKey_Transaction(CustomSessionWrapper session, long createdAtTime,
            String value) throws SQLException, StorageQueryException {

        final SessionAccessTokenSigningKeysDO sessionAccessTokenSigningKeysDO = new SessionAccessTokenSigningKeysDO(
                createdAtTime, value);

        session.save(SessionAccessTokenSigningKeysDO.class, createdAtTime, sessionAccessTokenSigningKeysDO);
    }

    public static KeyValueInfo[] getAccessTokenSigningKeys_Transaction(CustomSessionWrapper session)
            throws SQLException, StorageQueryException {

        String QUERY = "SELECT entity FROM SessionAccessTokenSigningKeysDO entity";
        final CustomQueryWrapper<SessionAccessTokenSigningKeysDO> query = session.createQuery(QUERY,
                SessionAccessTokenSigningKeysDO.class);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);

        return query.list().stream()
                .map(sessionAccessTokenSigningKeysDO -> new KeyValueInfo(sessionAccessTokenSigningKeysDO.getValue(),
                        sessionAccessTokenSigningKeysDO.getCreated_at_time()))
                .toArray(KeyValueInfo[]::new);

    }

    public static void removeAccessTokenSigningKeysBefore(Start start, long time)
            throws SQLException, StorageQueryException {
        ConnectionPool.withSession(start, ((session, con) -> {
            String QUERY = "DELETE FROM SessionAccessTokenSigningKeysDO entity "
                    + "WHERE entity.created_at_time < :created_at_time";

            final CustomQueryWrapper query = session.createQuery(QUERY);
            query.setParameter("created_at_time", time).executeUpdate();
            return null;
        }), true);
    }

}
