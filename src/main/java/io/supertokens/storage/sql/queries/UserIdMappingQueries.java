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

package io.supertokens.storage.sql.queries;

import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.pluginInterface.useridmapping.UserIdMapping;
import io.supertokens.storage.sql.ConnectionPool;
import io.supertokens.storage.sql.Start;
import io.supertokens.storage.sql.config.Config;
import io.supertokens.storage.sql.domainobject.general.AllAuthRecipeUsersDO;
import io.supertokens.storage.sql.domainobject.useridmapping.UserIdMappingDO;
import io.supertokens.storage.sql.domainobject.useridmapping.UserIdMappingPK;
import io.supertokens.storage.sql.hibernate.CustomQueryWrapper;
import io.supertokens.storage.sql.utils.Utils;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserIdMappingQueries {

    public static String getQueryToCreateUserIdMappingTable(Start start) {
        String schema = Config.getConfig(start).getTableSchema();
        String userIdMappingTable = Config.getConfig(start).getUserIdMappingTable();
        // @formatter:off

        return "CREATE TABLE IF NOT EXISTS " + userIdMappingTable + " ("
                + "supertokens_user_id CHAR(36) NOT NULL "
                + "CONSTRAINT " + Utils.getConstraintName(schema, userIdMappingTable, "supertokens_user_id", "key") + " UNIQUE,"
                + "external_user_id VARCHAR(128) NOT NULL"
                + " CONSTRAINT " + Utils.getConstraintName(schema, userIdMappingTable, "external_user_id", "key") + " UNIQUE,"
                + "external_user_id_info TEXT,"
                + " CONSTRAINT " + Utils.getConstraintName(schema, userIdMappingTable, null, "pkey") +
                " PRIMARY KEY(supertokens_user_id, external_user_id),"
                + ("CONSTRAINT " + Utils.getConstraintName(schema, userIdMappingTable, "supertokens_user_id", "fkey") +
                " FOREIGN KEY (supertokens_user_id)"
                + " REFERENCES " + Config.getConfig(start).getUsersTable() + "(user_id)"
                + " ON DELETE CASCADE);");
        // @formatter:on
    }

    public static void createUserIdMapping(Start start, String superTokensUserId, String externalUserId,
            String externalUserIdInfo) throws SQLException, StorageQueryException {
        ConnectionPool.withSession(start, (session, con) -> {
            UserIdMappingDO toInsert = new UserIdMappingDO();
            toInsert.setExternal_user_id_info(externalUserIdInfo);
            UserIdMappingPK pk = new UserIdMappingPK();
            pk.setExternal_user_id(externalUserId);
            AllAuthRecipeUsersDO allAuthUser = new AllAuthRecipeUsersDO();
            allAuthUser.setUser_id(superTokensUserId);
            pk.setUser(allAuthUser);
            toInsert.setPk(pk);
            session.save(UserIdMappingDO.class, pk, toInsert);
            return null;
        }, true);
    }

    public static UserIdMapping getUserIdMappingWithSuperTokensUserId(Start start, String userId)
            throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {

            String QUERY = "SELECT entity FROM UserIdMappingDO entity WHERE entity.pk.user.user_id = :userId";
            CustomQueryWrapper<UserIdMappingDO> q = session.createQuery(QUERY, UserIdMappingDO.class);
            q.setParameter("userId", userId);
            List<UserIdMappingDO> result = q.list();
            if (result.size() == 0) {
                return null;

            }

            return new UserIdMapping(result.get(0).getPk().getSuperTokensUserId(),
                    result.get(0).getPk().getExternal_user_id(), result.get(0).getExternal_user_id_info());
        }, false);
    }

    public static UserIdMapping getUserIdMappingQueryWithExternalUserId(Start start, String userId)
            throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {

            String QUERY = "SELECT entity FROM UserIdMappingDO entity WHERE entity.pk.external_user_id = :userId";
            CustomQueryWrapper<UserIdMappingDO> q = session.createQuery(QUERY, UserIdMappingDO.class);
            q.setParameter("userId", userId);
            List<UserIdMappingDO> result = q.list();
            if (result.size() == 0) {
                return null;
            }

            return new UserIdMapping(result.get(0).getPk().getSuperTokensUserId(),
                    result.get(0).getPk().getExternal_user_id(), result.get(0).getExternal_user_id_info());
        }, false);
    }

    public static UserIdMapping[] getUserIdMappingWithSuperTokensUserIdOrExternalUserId(Start start, String userId)
            throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity FROM UserIdMappingDO entity WHERE entity.pk.user.user_id = :userId OR entity.pk.external_user_id = :userId";
            CustomQueryWrapper<UserIdMappingDO> q = session.createQuery(QUERY, UserIdMappingDO.class);
            q.setParameter("userId", userId);
            List<UserIdMappingDO> result = q.list();
            UserIdMapping[] userIdMapping = new UserIdMapping[result.size()];

            for (int i = 0; i < result.size(); i++) {
                UserIdMappingDO current = result.get(i);
                userIdMapping[i] = new UserIdMapping(current.getPk().getSuperTokensUserId(),
                        current.getPk().getExternal_user_id(), current.getExternal_user_id_info());
            }
            return userIdMapping;
        }, false);

    }

    public static HashMap<String, String> getUserIdMappingWithUserIds(Start start, ArrayList<String> userIds)
            throws SQLException, StorageQueryException {

        if (userIds.size() == 0) {
            return new HashMap<>();
        }

        List<UserIdMappingDO> mappingsFromQuery = ConnectionPool.withSession(start, (session, con) -> {
            CustomQueryWrapper<UserIdMappingDO> q;
            StringBuilder userIdCondition = new StringBuilder();

            userIdCondition.append("entity.pk.user.user_id IN (:user_ids)");

            String userIdConditionAsString = userIdCondition.toString();

            String QUERY = "SELECT entity FROM UserIdMappingDO entity WHERE " + userIdConditionAsString;

            String[] userIdsToIncludeStr = new String[userIds.size()];
            for (int i = 0; i < userIds.size(); i++) {
                userIdsToIncludeStr[i] = userIds.get(i).toString();
            }

            q = session.createQuery(QUERY, UserIdMappingDO.class);
            q.setParameterList("user_ids", userIdsToIncludeStr);
            return q.list();
        }, false);

        HashMap<String, String> userIdMappings = new HashMap<>();
        for (int i = 0; i < mappingsFromQuery.size(); i++) {
            userIdMappings.put(mappingsFromQuery.get(i).getPk().getSuperTokensUserId(),
                    mappingsFromQuery.get(i).getPk().getExternal_user_id());
        }

        return userIdMappings;
    }

    public static boolean deleteUserIdMappingWithSuperTokensUserId(Start start, String userId)
            throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "DELETE FROM UserIdMappingDO entity WHERE entity.pk.user.user_id = :userId";
            int rowUpdatedCount = session.createQuery(QUERY).setParameter("userId", userId).executeUpdate();

            return rowUpdatedCount > 0;
        }, true);
    }

    public static boolean deleteUserIdMappingWithExternalUserId(Start start, String userId)
            throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "DELETE FROM UserIdMappingDO entity WHERE entity.pk.external_user_id = :userId";
            int rowUpdatedCount = session.createQuery(QUERY).setParameter("userId", userId).executeUpdate();

            return rowUpdatedCount > 0;
        }, true);
    }

    public static boolean updateOrDeleteExternalUserIdInfoWithSuperTokensUserId(Start start, String userId,
            @Nullable String externalUserIdInfo) throws SQLException, StorageQueryException {

        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "UPDATE UserIdMappingDO entity SET entity.external_user_id_info = :externalUserIdInfo WHERE entity.pk.user.user_id = :userId";
            CustomQueryWrapper q = session.createQuery(QUERY);
            q.setParameter("userId", userId);
            q.setParameter("externalUserIdInfo", externalUserIdInfo);
            int rowUpdatedCount = q.executeUpdate();
            return rowUpdatedCount > 0;
        }, true);
    }

    public static boolean updateOrDeleteExternalUserIdInfoWithExternalUserId(Start start, String userId,
            @Nullable String externalUserIdInfo) throws SQLException, StorageQueryException {

        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "UPDATE UserIdMappingDO entity SET entity.external_user_id_info = :externalUserIdInfo WHERE entity.pk.external_user_id = :userId";
            CustomQueryWrapper q = session.createQuery(QUERY);
            q.setParameter("userId", userId);
            q.setParameter("externalUserIdInfo", externalUserIdInfo);
            int rowUpdatedCount = q.executeUpdate();
            return rowUpdatedCount > 0;
        }, true);
    }
}
