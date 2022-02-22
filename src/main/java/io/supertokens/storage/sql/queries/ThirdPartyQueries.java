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

import io.supertokens.pluginInterface.RECIPE_ID;
import io.supertokens.pluginInterface.RowMapper;
import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.pluginInterface.exceptions.StorageTransactionLogicException;

import io.supertokens.pluginInterface.sqlStorage.SessionObject;
import io.supertokens.pluginInterface.thirdparty.UserInfo;
import io.supertokens.storage.sql.Start;
import io.supertokens.storage.sql.config.Config;
import io.supertokens.storage.sql.dataaccessobjects.passwordless.impl.UsersDAO;
import io.supertokens.storage.sql.dataaccessobjects.thirdparty.impl.ThirdPartyUsersDAO;
import io.supertokens.storage.sql.domainobjects.thirdparty.ThirdPartyUsersDO;
import io.supertokens.storage.sql.enums.OrderEnum;
import io.supertokens.storage.sql.exceptions.InvalidOrderTypeException;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.jetbrains.annotations.NotNull;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ThirdPartyQueries {

    static String getQueryToCreateUsersTable(Start start) {
        return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getThirdPartyUsersTable() + " ("
                + "third_party_id VARCHAR(28) NOT NULL," + "third_party_user_id VARCHAR(128) NOT NULL,"
                + "user_id CHAR(36) NOT NULL UNIQUE," + "email VARCHAR(256) NOT NULL," + "time_joined BIGINT  NOT NULL,"
                + "PRIMARY KEY (third_party_id, third_party_user_id));";
    }

    public static void signUp(Start start, io.supertokens.pluginInterface.thirdparty.UserInfo userInfo)
            throws StorageQueryException, StorageTransactionLogicException {
        start.startTransactionHibernate(session -> {
            try {
                {
                    UsersDAO usersDAO = new UsersDAO(session);
                    usersDAO.insertIntoTableValues(userInfo.id, RECIPE_ID.THIRD_PARTY.toString(), userInfo.timeJoined);
                }

                {
                    ThirdPartyUsersDAO thirdPartyUsersDAO = new ThirdPartyUsersDAO(session);
                    thirdPartyUsersDAO.insertValues(userInfo.thirdParty.id, userInfo.thirdParty.userId, userInfo.id,
                            userInfo.email, userInfo.timeJoined);
                }

                start.commitTransaction(session);
            } catch (PersistenceException exception) {
                throw exception;
            } catch (Exception e) {
                throw new StorageTransactionLogicException(e);
            }
            return null;
        });
    }

    public static void deleteUser(Start start, String userId)
            throws StorageQueryException, StorageTransactionLogicException {
        start.startTransactionHibernate(session -> {
            try {
                {
                    UsersDAO usersDAO = new UsersDAO(session);
                    usersDAO.deleteWhereUserIdEqualsAndRecipeIdEquals(userId, RECIPE_ID.THIRD_PARTY.toString());
                }

                {
                    ThirdPartyUsersDAO thirdPartyUsersDAO = new ThirdPartyUsersDAO(session);
                    thirdPartyUsersDAO.deleteWhereUserIdEquals(userId);
                }
            } catch (PersistenceException throwables) {
                throw new StorageTransactionLogicException(throwables);
            }
            return null;
        });
    }

    public static UserInfo getThirdPartyUserInfoUsingId(Start start, SessionObject sessionObject, String userId)
            throws SQLException, StorageQueryException {
        List<String> input = new ArrayList<>();
        input.add(userId);
        List<UserInfo> result = getUsersInfoUsingIdList(start, sessionObject, input);
        if (result.size() == 1) {
            return result.get(0);
        }
        return null;
    }

    public static List<UserInfo> getUsersInfoUsingIdList(Start start, SessionObject sessionObject, List<String> ids)
            throws SQLException, StorageQueryException {
        List<UserInfo> finalResult = new ArrayList<>();
        if (ids.size() > 0) {
            StringBuilder QUERY = new StringBuilder(
                    "SELECT user_id, third_party_id, third_party_user_id, email, time_joined FROM "
                            + Config.getConfig(start).getThirdPartyUsersTable());
            QUERY.append(" WHERE user_id IN (");
            for (int i = 0; i < ids.size(); i++) {

                QUERY.append("?");
                if (i != ids.size() - 1) {
                    // not the last element
                    QUERY.append(",");
                }
            }
            QUERY.append(")");
            Session session = (Session) sessionObject.getSession();
            NativeQuery nativeQuery = session.createNativeQuery(QUERY.toString());
            for (int i = 0; i < ids.size(); i++) {
                // i+1 cause this starts with 1 and not 0
                nativeQuery.setParameter(i + 1, ids.get(i));
            }

            List<Object[]> list = nativeQuery.getResultList();
            Iterator<Object[]> iterator = list.iterator();
            while (iterator.hasNext()) {
                finalResult.add(UserInfoObjectMapper.getInstance().mapOrThrow(iterator.next()));
            }
        }
        return finalResult;
    }

    public static UserInfo getThirdPartyUserInfoUsingId(Start start, SessionObject sessionObject, String thirdPartyId,
            String thirdPartyUserId) throws SQLException, StorageQueryException {

        ThirdPartyUsersDAO thirdPartyUsersDAO = new ThirdPartyUsersDAO(sessionObject);

        ThirdPartyUsersDO thirdPartyUsersDO = thirdPartyUsersDAO
                .getWhereThirdPartyIDEqualsAndThirdPartyUserIdEquals(thirdPartyId, thirdPartyUserId);

        return UserInfoRowMapper.getInstance().mapOrThrow(thirdPartyUsersDO);

    }

    public static void updateUserEmail_Transaction(Start start, SessionObject sessionObject, String thirdPartyId,
            String thirdPartyUserId, String newEmail) throws SQLException {

        ThirdPartyUsersDAO thirdPartyUsersDAO = new ThirdPartyUsersDAO(sessionObject);

        thirdPartyUsersDAO.updateEmailWhereThirdPartyIdEqualsAndThirdPartyUserIdEquals(thirdPartyId, thirdPartyUserId,
                newEmail);

    }

    public static UserInfo getUserInfoUsingId_Transaction(Start start, SessionObject sessionObject, String thirdPartyId,
            String thirdPartyUserId) throws SQLException, StorageQueryException {

        ThirdPartyUsersDAO thirdPartyUsersDAO = new ThirdPartyUsersDAO(sessionObject);

        ThirdPartyUsersDO thirdPartyUsersDO = thirdPartyUsersDAO
                .getWhereThirdPartyIDEqualsAndThirdPartyUserIdEquals_locked(thirdPartyId, thirdPartyUserId);

        return new UserInfo(thirdPartyUsersDO.getUser_id(), thirdPartyUsersDO.getEmail(),
                new UserInfo.ThirdParty(thirdPartyUsersDO.getPrimary_key().getThird_party_id(),
                        thirdPartyUsersDO.getPrimary_key().getThird_party_user_id()),
                thirdPartyUsersDO.getTime_joined());

    }

    public static UserInfo[] getThirdPartyUsersByEmail(Start start, SessionObject sessionObject, @NotNull String email)
            throws SQLException, StorageQueryException {

        ThirdPartyUsersDAO thirdPartyUsersDAO = new ThirdPartyUsersDAO(sessionObject);

        List<ThirdPartyUsersDO> doList = thirdPartyUsersDAO.getWhereEmailEquals(email);
        return getUsersFromResult(doList);

    }

    private static UserInfo[] getUsersFromResult(List<ThirdPartyUsersDO> doList)
            throws SQLException, StorageQueryException {
        List<UserInfo> users = new ArrayList<>();

        Iterator<ThirdPartyUsersDO> doIterator = doList.listIterator();

        while (doIterator.hasNext()) {
            ThirdPartyUsersDO partyUsersDO = doIterator.next();
            users.add(UserInfoRowMapper.getInstance().mapOrThrow(partyUsersDO));

        }
        return users.toArray(UserInfo[]::new);
    }

    @Deprecated
    public static UserInfo[] getThirdPartyUsers(Start start, SessionObject sessionObject, @NotNull Integer limit,
            @NotNull String timeJoinedOrder) throws SQLException, StorageQueryException, InvalidOrderTypeException {

        ThirdPartyUsersDAO thirdPartyUsersDAO = new ThirdPartyUsersDAO(sessionObject);

        List<ThirdPartyUsersDO> thirdPartyUsersDOS = thirdPartyUsersDAO
                .getByTimeJoinedOrderAndUserIdOrderAndLimit(timeJoinedOrder, OrderEnum.DESC.name(), limit);
        return getUsersFromResult(thirdPartyUsersDOS);
    }

    @Deprecated
    public static UserInfo[] getThirdPartyUsers(Start start, SessionObject sessionObject, @NotNull String userId,
            @NotNull Long timeJoined, @NotNull Integer limit, @NotNull String timeJoinedOrder)
            throws SQLException, StorageQueryException {
        String timeJoinedOrderSymbol = timeJoinedOrder.equals("ASC") ? ">" : "<";
        String QUERY = "SELECT user_id, third_party_id, third_party_user_id, email, time_joined FROM "
                + Config.getConfig(start).getThirdPartyUsersTable() + " WHERE time_joined " + timeJoinedOrderSymbol
                + " ? OR (time_joined = ? AND user_id <= ?) ORDER BY time_joined " + timeJoinedOrder
                + ", user_id DESC LIMIT ?";

        Session session = (Session) sessionObject.getSession();

        NativeQuery nativeQuery = session.createNativeQuery(QUERY.toString());
        nativeQuery.setParameter(1, timeJoined);
        nativeQuery.setParameter(2, timeJoined);
        nativeQuery.setParameter(3, userId);
        nativeQuery.setParameter(4, limit);

        List<Object[]> list = nativeQuery.getResultList();
        Iterator<Object[]> iterator = list.iterator();
        List<UserInfo> temp = new ArrayList<>();

        while (iterator.hasNext()) {
            temp.add(UserInfoObjectMapper.getInstance().mapOrThrow(iterator.next()));
        }

        UserInfo[] finalResult = new UserInfo[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            finalResult[i] = temp.get(i);
        }
        return finalResult;
    }

    @Deprecated
    public static long getUsersCount(Start start, SessionObject sessionObject) throws SQLException {
        ThirdPartyUsersDAO thirdPartyUsersDAO = new ThirdPartyUsersDAO(sessionObject);

        return thirdPartyUsersDAO.getCount();
    }

    private static class UserInfoObjectMapper implements RowMapper<UserInfo, Object[]> {
        private static final UserInfoObjectMapper INSTANCE = new UserInfoObjectMapper();

        private UserInfoObjectMapper() {
        }

        private static UserInfoObjectMapper getInstance() {
            return INSTANCE;
        }

        @Override
        public UserInfo map(Object[] result) throws Exception {

            if (result == null) {
                return null;
            }

            return new UserInfo(result[0] != null ? result[0].toString() : "",
                    result[3] != null ? result[3].toString() : "",
                    new UserInfo.ThirdParty(result[1] != null ? result[1].toString() : "",
                            result[2] != null ? result[2].toString() : ""),
                    result[4] != null ? Long.valueOf(result[4].toString()) : 0l);

        }
    }

    private static class UserInfoRowMapper implements RowMapper<UserInfo, ThirdPartyUsersDO> {
        private static final UserInfoRowMapper INSTANCE = new UserInfoRowMapper();

        private UserInfoRowMapper() {
        }

        private static UserInfoRowMapper getInstance() {
            return INSTANCE;
        }

        @Override
        public UserInfo map(ThirdPartyUsersDO result) throws Exception {
            if (result == null) {
                return null;
            }
            return new UserInfo(result.getUser_id(), result.getEmail(),
                    new UserInfo.ThirdParty(result.getPrimary_key().getThird_party_id(),
                            result.getPrimary_key().getThird_party_user_id()),
                    result.getTime_joined());
        }
    }
}
