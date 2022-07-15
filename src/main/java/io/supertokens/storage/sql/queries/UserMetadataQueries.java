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
import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.storage.sql.ConnectionPool;
import io.supertokens.storage.sql.Start;
import io.supertokens.storage.sql.domainobject.usermetadata.UserMetadataDO;
import io.supertokens.storage.sql.hibernate.CustomQueryWrapper;
import io.supertokens.storage.sql.hibernate.CustomSessionWrapper;
import io.supertokens.storage.sql.utils.Utils;

import javax.persistence.LockModeType;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static io.supertokens.storage.sql.QueryExecutorTemplate.update;
import static io.supertokens.storage.sql.config.Config.getConfig;

public class UserMetadataQueries {

    public static String getQueryToCreateUserMetadataTable(Start start) {
        String schema = getConfig(start).getTableSchema();
        String tableName = getConfig(start).getUserMetadataTable();
        // @formatter:off
        return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "user_id VARCHAR(128) NOT NULL,"
                + "user_metadata TEXT NOT NULL,"
                + "CONSTRAINT " + Utils.getConstraintName(schema, tableName, null, "pkey") + " PRIMARY KEY(user_id)" +
                " );";
        // @formatter:on

    }

    public static int deleteUserMetadata(Start start, String userId) throws SQLException, StorageQueryException {

        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "DELETE FROM UserMetadataDO entity WHERE entity.user_id = :user_id";
            return session.createQuery(QUERY).setParameter("user_id", userId).executeUpdate();
        }, true);
    }

    public static int setUserMetadata_Transaction(Start start, Connection con, String userId, JsonObject metadata)
            throws SQLException, StorageQueryException {

        String QUERY = "INSERT INTO " + getConfig(start).getUserMetadataTable()
                + "(user_id, user_metadata) VALUES(?, ?) "
                + "ON CONFLICT(user_id) DO UPDATE SET user_metadata=excluded.user_metadata;";

        return update(con, QUERY, pst -> {
            pst.setString(1, userId);
            pst.setString(2, metadata.toString());
        });
    }

    public static JsonObject getUserMetadata_Transaction(CustomSessionWrapper session, String userId)
            throws SQLException, StorageQueryException {

        String QUERY = "SELECT entity FROM UserMetadataDO entity WHERE entity.user_id = :user_id";

        CustomQueryWrapper<UserMetadataDO> q = session.createQuery(QUERY, UserMetadataDO.class);
        q.setParameter("user_id", userId);
        q.setLockMode(LockModeType.PESSIMISTIC_WRITE);

        final List<UserMetadataDO> result = q.list();
        if (result.size() == 0) {
            return null;
        }
        JsonParser jp = new JsonParser();
        return jp.parse(result.get(0).getUser_metadata()).getAsJsonObject();
    }

    public static JsonObject getUserMetadata(Start start, String userId) throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity FROM UserMetadataDO entity WHERE entity.user_id = :user_id";

            CustomQueryWrapper<UserMetadataDO> q = session.createQuery(QUERY, UserMetadataDO.class);
            q.setParameter("user_id", userId);

            final List<UserMetadataDO> result = q.list();
            if (result.size() == 0) {
                return null;
            }
            JsonParser jp = new JsonParser();
            return jp.parse(result.get(0).getUser_metadata()).getAsJsonObject();
        }, true);
    }
}