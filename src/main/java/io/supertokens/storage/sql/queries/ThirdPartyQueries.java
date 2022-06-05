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

import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.pluginInterface.exceptions.StorageTransactionLogicException;
import io.supertokens.pluginInterface.thirdparty.UserInfo;
import io.supertokens.storage.sql.ConnectionPool;
import io.supertokens.storage.sql.Start;
import io.supertokens.storage.sql.config.Config;
import io.supertokens.storage.sql.domainobject.general.AllAuthRecipeUsersDO;
import io.supertokens.storage.sql.domainobject.thirdparty.ThirdPartyUsersDO;
import io.supertokens.storage.sql.domainobject.thirdparty.ThirdPartyUsersPK;
import io.supertokens.storage.sql.hibernate.CustomQueryWrapper;
import io.supertokens.storage.sql.hibernate.CustomSessionWrapper;
import io.supertokens.storage.sql.utils.Utils;
import org.hibernate.LockMode;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.supertokens.pluginInterface.RECIPE_ID.THIRD_PARTY;

public class ThirdPartyQueries {

    static String getQueryToCreateUsersTable(Start start) {
        String schema = Config.getConfig(start).getTableSchema();
        String thirdPartyUsersTable = Config.getConfig(start).getThirdPartyUsersTable();
        // @formatter:off
        return "CREATE TABLE IF NOT EXISTS " + thirdPartyUsersTable + " ("
                + "third_party_id VARCHAR(28) NOT NULL,"
                + "third_party_user_id VARCHAR(128) NOT NULL,"
                + "user_id CHAR(36) NOT NULL CONSTRAINT " +
                Utils.getConstraintName(schema, thirdPartyUsersTable, "user_id", "key") + " UNIQUE,"
                + "email VARCHAR(256) NOT NULL,"
                + "time_joined BIGINT NOT NULL,"
                + "CONSTRAINT " + Utils.getConstraintName(schema, thirdPartyUsersTable, null, "pkey") +
                " PRIMARY KEY (third_party_id, third_party_user_id));";
        // @formatter:on
    }

    public static void signUp(Start start, io.supertokens.pluginInterface.thirdparty.UserInfo userInfo)
            throws StorageQueryException, StorageTransactionLogicException, SQLException {
        ConnectionPool.withSession(start, (session, con) -> {
            AllAuthRecipeUsersDO allUsersRow = new AllAuthRecipeUsersDO();
            allUsersRow.setUser_id(userInfo.id);
            allUsersRow.setRecipe_id(THIRD_PARTY.toString());
            allUsersRow.setTime_joined(userInfo.timeJoined);
            session.save(AllAuthRecipeUsersDO.class, userInfo.id, allUsersRow);

            ThirdPartyUsersDO tpRow = new ThirdPartyUsersDO();
            ThirdPartyUsersPK pk = new ThirdPartyUsersPK();
            pk.setThird_party_id(userInfo.thirdParty.id);
            pk.setThird_party_user_id(userInfo.thirdParty.userId);
            tpRow.setPk(pk);
            tpRow.setUser_id(userInfo.id);
            tpRow.setEmail(userInfo.email);
            tpRow.setTime_joined(userInfo.timeJoined);
            session.save(ThirdPartyUsersDO.class, pk, tpRow);
            return null;
        }, true);
    }

    public static void deleteUser(Start start, String userId)
            throws StorageQueryException, StorageTransactionLogicException, SQLException {
        ConnectionPool.withSession(start, (session, con) -> {
            {
                String QUERY = "DELETE FROM AllAuthRecipeUsersDO entity WHERE entity.user_id = :userid";
                session.createQuery(QUERY).setParameter("userid", userId).executeUpdate();
            }
            {
                String QUERY = "DELETE FROM ThirdPartyUsersDO entity WHERE entity.user_id = :userid";
                session.createQuery(QUERY).setParameter("userid", userId).executeUpdate();
            }
            return null;
        }, true);
    }

    public static UserInfo getThirdPartyUserInfoUsingId(Start start, String userId)
            throws SQLException, StorageQueryException {
        List<String> input = new ArrayList<>();
        input.add(userId);
        List<UserInfo> result = getUsersInfoUsingIdList(start, input);
        if (result.size() == 1) {
            return result.get(0);
        }
        return null;
    }

    public static List<UserInfo> getUsersInfoUsingIdList(Start start, List<String> ids)
            throws SQLException, StorageQueryException {
        if (ids.size() > 0) {
            return ConnectionPool.withSession(start, (session, con) -> {
                String QUERY = "SELECT entity FROM ThirdPartyUsersDO entity WHERE entity.user_id IN (:useridlist)";
                CustomQueryWrapper<ThirdPartyUsersDO> q = session.createQuery(QUERY, ThirdPartyUsersDO.class);
                q.setParameterList("useridlist", ids);
                List<ThirdPartyUsersDO> result = q.list();
                List<UserInfo> finalResult = new ArrayList<>();
                for (ThirdPartyUsersDO user : result) {
                    finalResult.add(new UserInfo(user.getUser_id(), user.getEmail(),
                            new UserInfo.ThirdParty(user.getPk().getThird_party_id(),
                                    user.getPk().getThird_party_user_id()),
                            user.getTime_joined()));
                }
                return finalResult;
            }, false);
        }
        return Collections.emptyList();
    }

    public static UserInfo getThirdPartyUserInfoUsingId(Start start, String thirdPartyId, String thirdPartyUserId)
            throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            ThirdPartyUsersPK pk = new ThirdPartyUsersPK();
            pk.setThird_party_user_id(thirdPartyUserId);
            pk.setThird_party_id(thirdPartyId);
            ThirdPartyUsersDO user = session.get(ThirdPartyUsersDO.class, pk);
            if (user == null) {
                return null;
            }
            return new UserInfo(user.getUser_id(), user.getEmail(),
                    new UserInfo.ThirdParty(user.getPk().getThird_party_id(), user.getPk().getThird_party_user_id()),
                    user.getTime_joined());
        }, false);
    }

    public static void updateUserEmail_Transaction(CustomSessionWrapper session, String thirdPartyId,
            String thirdPartyUserId, String newEmail) throws SQLException {
        String QUERY = "UPDATE ThirdPartyUsersDO entity SET entity.email = :email WHERE entity.pk.third_party_id = :tpid AND"
                + " entity.pk.third_party_user_id = :tpuid";
        CustomQueryWrapper q = session.createQuery(QUERY);
        q.setParameter("email", newEmail);
        q.setParameter("tpid", thirdPartyId);
        q.setParameter("tpuid", thirdPartyUserId);
        q.executeUpdate();
    }

    public static UserInfo getUserInfoUsingId_Transaction(CustomSessionWrapper session, String thirdPartyId,
            String thirdPartyUserId) throws SQLException {

        ThirdPartyUsersPK pk = new ThirdPartyUsersPK();
        pk.setThird_party_user_id(thirdPartyUserId);
        pk.setThird_party_id(thirdPartyId);
        ThirdPartyUsersDO user = session.get(ThirdPartyUsersDO.class, pk, LockMode.PESSIMISTIC_WRITE);
        if (user == null) {
            return null;
        }
        return new UserInfo(user.getUser_id(), user.getEmail(),
                new UserInfo.ThirdParty(user.getPk().getThird_party_id(), user.getPk().getThird_party_user_id()),
                user.getTime_joined());
    }

    @Deprecated
    public static UserInfo[] getThirdPartyUsers(Start start, @NotNull Integer limit, @NotNull String timeJoinedOrder)
            throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity FROM ThirdPartyUsersDO entity ORDER BY entity.time_joined " + timeJoinedOrder
                    + ", user_id DESC";
            CustomQueryWrapper<ThirdPartyUsersDO> q = session.createQuery(QUERY, ThirdPartyUsersDO.class);
            q.setMaxResults(limit);
            List<ThirdPartyUsersDO> result = q.list();
            UserInfo[] finalResult = new UserInfo[result.size()];
            for (int i = 0; i < result.size(); i++) {
                ThirdPartyUsersDO curr = result.get(i);
                finalResult[i] = new UserInfo(curr.getUser_id(), curr.getEmail(),
                        new UserInfo.ThirdParty(curr.getPk().getThird_party_id(),
                                curr.getPk().getThird_party_user_id()),
                        curr.getTime_joined());
            }
            return finalResult;
        }, false);
    }

    @Deprecated
    public static UserInfo[] getThirdPartyUsers(Start start, @NotNull String userId, @NotNull Long timeJoined,
            @NotNull Integer limit, @NotNull String timeJoinedOrder) throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String timeJoinedOrderSymbol = timeJoinedOrder.equals("ASC") ? ">" : "<";
            String QUERY = "SELECT entity FROM ThirdPartyUsersDO entity WHERE entity.time_joined "
                    + timeJoinedOrderSymbol
                    + " :tj1 OR (entity.time_joined = :tj2 AND entity.user_id <= :userid) ORDER BY entity"
                    + ".time_joined " + timeJoinedOrder + ", user_id DESC";
            CustomQueryWrapper<ThirdPartyUsersDO> q = session.createQuery(QUERY, ThirdPartyUsersDO.class);
            q.setMaxResults(limit);
            q.setParameter("tj1", timeJoined);
            q.setParameter("tj2", timeJoined);
            q.setParameter("userid", userId);
            List<ThirdPartyUsersDO> result = q.list();
            UserInfo[] finalResult = new UserInfo[result.size()];
            for (int i = 0; i < result.size(); i++) {
                ThirdPartyUsersDO curr = result.get(i);
                finalResult[i] = new UserInfo(curr.getUser_id(), curr.getEmail(),
                        new UserInfo.ThirdParty(curr.getPk().getThird_party_id(),
                                curr.getPk().getThird_party_user_id()),
                        curr.getTime_joined());
            }
            return finalResult;
        }, false);
    }

    @Deprecated
    public static long getUsersCount(Start start) throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            CustomQueryWrapper<Long> q = session.createQuery("SELECT COUNT(*) FROM ThirdPartyUsersDO ", Long.class);
            List<Long> result = q.list();
            return result.get(0);
        }, false);
    }

    public static UserInfo[] getThirdPartyUsersByEmail(Start start, @NotNull String email)
            throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity FROM ThirdPartyUsersDO entity where entity.email = :email";
            CustomQueryWrapper<ThirdPartyUsersDO> q = session.createQuery(QUERY, ThirdPartyUsersDO.class);
            q.setParameter("email", email);
            List<ThirdPartyUsersDO> result = q.list();
            List<UserInfo> users = new ArrayList<>();
            for (ThirdPartyUsersDO curr : result) {
                users.add(new UserInfo(curr.getUser_id(), curr.getEmail(),
                        new UserInfo.ThirdParty(curr.getPk().getThird_party_id(),
                                curr.getPk().getThird_party_user_id()),
                        curr.getTime_joined()));
            }
            return users.toArray(UserInfo[]::new);
        }, false);
    }
}
