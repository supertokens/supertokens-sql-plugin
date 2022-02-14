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

import io.supertokens.pluginInterface.KeyValueInfo;
import io.supertokens.pluginInterface.RECIPE_ID;
import io.supertokens.pluginInterface.authRecipe.AuthRecipeUserInfo;
import io.supertokens.pluginInterface.exceptions.StorageQueryException;

import io.supertokens.pluginInterface.sqlStorage.SessionObject;
import io.supertokens.storage.sql.HibernateSessionPool;
import io.supertokens.storage.sql.ProcessState;
import io.supertokens.storage.sql.Start;
import io.supertokens.storage.sql.config.Config;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.*;

public class GeneralQueries {

    private static void executeUpdateQuery(Start start, String query) throws StorageQueryException {

        try (Session session = HibernateSessionPool.getSessionFactory(start).openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createNativeQuery(query).executeUpdate();
            transaction.commit();
        } catch (PersistenceException | InterruptedException e) {
            throw new StorageQueryException(e);
        }

    }

    private static boolean doesTableExists(Start start, String tableName) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateSessionPool.getSessionFactory(start).openSession();
            transaction = session.getTransaction();
            transaction.begin();

            String query = "SELECT 1 FROM " + tableName + " LIMIT 1";
            NativeQuery nativeQuery = session.createNativeQuery(query);

            List<Object> result = nativeQuery.list();

            transaction.commit();
            session.close();

            if (result != null || result.size() >= 0) {
                return true;
            }

        } catch (Exception e) {
            session.close();
        }

        return false;

    }

    static String getQueryToCreateUsersTable(Start start) {
        return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getUsersTable() + " ("
                + "user_id CHAR(36) NOT NULL," + "recipe_id VARCHAR(128) NOT NULL," + "time_joined BIGINT NOT NULL,"
                + "PRIMARY KEY (user_id));";
    }

    static String getQueryToCreateUserPaginationIndex(Start start) {
        return "CREATE INDEX all_auth_recipe_users_pagination_index ON " + Config.getConfig(start).getUsersTable()
                + "(time_joined DESC, user_id " + "DESC);";
    }

    private static String getQueryToCreateKeyValueTable(Start start) {
        return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getKeyValueTable() + " (" + "name VARCHAR(128),"
                + "value TEXT," + "created_at_time BIGINT ," + "PRIMARY KEY(name)" + " );";
    }

    public static void createTablesIfNotExists(Start start) throws StorageQueryException {
        if (!doesTableExists(start, Config.getConfig(start).getKeyValueTable())) {
            ProcessState.getInstance(start).addState(ProcessState.PROCESS_STATE.CREATING_NEW_TABLE, null);
            // create table
            executeUpdateQuery(start, getQueryToCreateKeyValueTable(start));
        }

        if (!doesTableExists(start, Config.getConfig(start).getUsersTable())) {
            ProcessState.getInstance(start).addState(ProcessState.PROCESS_STATE.CREATING_NEW_TABLE, null);
            // create table
            executeUpdateQuery(start, getQueryToCreateUsersTable(start));

            // create index
            executeUpdateQuery(start, getQueryToCreateUserPaginationIndex(start));

        }

        if (!doesTableExists(start, Config.getConfig(start).getAccessTokenSigningKeysTable())) {
            ProcessState.getInstance(start).addState(ProcessState.PROCESS_STATE.CREATING_NEW_TABLE, null);

            // create table
            executeUpdateQuery(start, SessionQueries.getQueryToCreateAccessTokenSigningKeysTable(start));

        }

        if (!doesTableExists(start, Config.getConfig(start).getSessionInfoTable())) {
            ProcessState.getInstance(start).addState(ProcessState.PROCESS_STATE.CREATING_NEW_TABLE, null);

            // create table
            executeUpdateQuery(start, SessionQueries.getQueryToCreateSessionInfoTable(start));

        }

        if (!doesTableExists(start, Config.getConfig(start).getEmailPasswordUsersTable())) {
            ProcessState.getInstance(start).addState(ProcessState.PROCESS_STATE.CREATING_NEW_TABLE, null);

            // create table
            executeUpdateQuery(start, EmailPasswordQueries.getQueryToCreateUsersTable(start));

        }

        if (!doesTableExists(start, Config.getConfig(start).getPasswordResetTokensTable())) {
            ProcessState.getInstance(start).addState(ProcessState.PROCESS_STATE.CREATING_NEW_TABLE, null);
            // create table
            executeUpdateQuery(start, EmailPasswordQueries.getQueryToCreatePasswordResetTokensTable(start));

            // index
            executeUpdateQuery(start, EmailPasswordQueries.getQueryToCreatePasswordResetTokenExpiryIndex(start));

        }

        if (!doesTableExists(start, Config.getConfig(start).getEmailVerificationTable())) {
            ProcessState.getInstance(start).addState(ProcessState.PROCESS_STATE.CREATING_NEW_TABLE, null);

            // create table
            executeUpdateQuery(start, EmailVerificationQueries.getQueryToCreateEmailVerificationTable(start));

        }

        if (!doesTableExists(start, Config.getConfig(start).getEmailVerificationTokensTable())) {
            ProcessState.getInstance(start).addState(ProcessState.PROCESS_STATE.CREATING_NEW_TABLE, null);
            // create table
            executeUpdateQuery(start, EmailVerificationQueries.getQueryToCreateEmailVerificationTokensTable(start));

            // index
            executeUpdateQuery(start,
                    EmailVerificationQueries.getQueryToCreateEmailVerificationTokenExpiryIndex(start));

        }

        if (!doesTableExists(start, Config.getConfig(start).getThirdPartyUsersTable())) {
            ProcessState.getInstance(start).addState(ProcessState.PROCESS_STATE.CREATING_NEW_TABLE, null);
            // create table
            executeUpdateQuery(start, ThirdPartyQueries.getQueryToCreateUsersTable(start));

        }

        if (!doesTableExists(start, Config.getConfig(start).getJWTSigningKeysTable())) {
            ProcessState.getInstance(start).addState(ProcessState.PROCESS_STATE.CREATING_NEW_TABLE, null);
            // create table
            executeUpdateQuery(start, JWTSigningQueries.getQueryToCreateJWTSigningTable(start));

        }

        if (!doesTableExists(start, Config.getConfig(start).getPasswordlessUsersTable())) {
            ProcessState.getInstance(start).addState(ProcessState.PROCESS_STATE.CREATING_NEW_TABLE, null);
            // create table
            executeUpdateQuery(start, PasswordlessQueries.getQueryToCreateUsersTable(start));

        }

        if (!doesTableExists(start, Config.getConfig(start).getPasswordlessDevicesTable())) {
            ProcessState.getInstance(start).addState(ProcessState.PROCESS_STATE.CREATING_NEW_TABLE, null);
            // create table
            executeUpdateQuery(start, PasswordlessQueries.getQueryToCreateDevicesTable(start));

            // index
            executeUpdateQuery(start, PasswordlessQueries.getQueryToCreateDeviceEmailIndex(start));
            executeUpdateQuery(start, PasswordlessQueries.getQueryToCreateDevicePhoneNumberIndex(start));

        }

        if (!doesTableExists(start, Config.getConfig(start).getPasswordlessCodesTable())) {
            ProcessState.getInstance(start).addState(ProcessState.PROCESS_STATE.CREATING_NEW_TABLE, null);
            // create table
            executeUpdateQuery(start, PasswordlessQueries.getQueryToCreateCodesTable(start));

            // index
            executeUpdateQuery(start, PasswordlessQueries.getQueryToCreateCodeCreatedAtIndex(start));

        }

    }

    public static void setKeyValue_Transaction(Start start, SessionObject sessionObject, String key, KeyValueInfo info)
            throws InterruptedException {
        String QUERY = "INSERT INTO " + Config.getConfig(start).getKeyValueTable()
                + "(name, value, created_at_time) VALUES(?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE value = ?, created_at_time = ?";

        Session session = (Session) sessionObject.getSession();
        NativeQuery nativeQuery = session.createNativeQuery(QUERY);
        nativeQuery.setParameter(1, key);
        nativeQuery.setParameter(2, info.value);
        nativeQuery.setParameter(3, info.createdAtTime);
        nativeQuery.setParameter(4, info.value);
        nativeQuery.setParameter(5, info.createdAtTime);

        nativeQuery.executeUpdate();
    }

    public static void setKeyValue(Start start, SessionObject sessionObject, String key, KeyValueInfo info)
            throws InterruptedException {
        setKeyValue_Transaction(start, sessionObject, key, info);
    }

    public static KeyValueInfo getKeyValue(Start start, SessionObject sessionObject, String key)
            throws InterruptedException {
        String QUERY = "SELECT value, created_at_time FROM " + Config.getConfig(start).getKeyValueTable()
                + " WHERE name = :name";

        Session session = (Session) sessionObject.getSession();
        NativeQuery nativeQuery = session.createNativeQuery(QUERY);
        nativeQuery.setParameter("name", key);

        List<KeyValueInfo> result = nativeQuery.list();

        if (result == null)
            return null;

        return result.get(0);

    }

    public static KeyValueInfo getKeyValue_Transaction(Start start, SessionObject sessionObject, String key)
            throws InterruptedException {
        String QUERY = "SELECT value, created_at_time FROM " + Config.getConfig(start).getKeyValueTable()
                + " WHERE name = ? FOR UPDATE";

        Session session = (Session) sessionObject.getSession();
        NativeQuery nativeQuery = session.createNativeQuery(QUERY);
        nativeQuery.setParameter(1, key);

        // TODO: check if this works
        List<KeyValueInfo> result = nativeQuery.list();

        if (result.size() == 0)
            return null;

        return result.get(0);
    }

    public static void deleteKeyValue_Transaction(Start start, SessionObject sessionObject, String key)
            throws InterruptedException {
        String QUERY = "DELETE FROM " + Config.getConfig(start).getKeyValueTable() + " WHERE name = ?";

        Session session = (Session) sessionObject.getSession();
        NativeQuery nativeQuery = session.createNativeQuery(QUERY);
        nativeQuery.setParameter(1, key);

        nativeQuery.executeUpdate();
    }

    @TestOnly
    public static void deleteAllTables(Start start) throws StorageQueryException {
//        {
//            String DROP_QUERY = "DROP INDEX IF EXISTS emailpassword_password_reset_token_expiry_index";
//            executeUpdateQuery(start, DROP_QUERY);
//        }
//        {
//            String DROP_QUERY = "DROP INDEX IF EXISTS emailverification_tokens_index";
//            executeUpdateQuery(start, DROP_QUERY);
//        }
//        {
//            String DROP_QUERY = "DROP INDEX IF EXISTS all_auth_recipe_users_pagination_index";
//            executeUpdateQuery(start, DROP_QUERY);
//        }
        {
            String DROP_QUERY = "DROP TABLE IF EXISTS " + Config.getConfig(start).getKeyValueTable() + ","
                    + Config.getConfig(start).getUsersTable() + ","
                    + Config.getConfig(start).getAccessTokenSigningKeysTable() + ","
                    + Config.getConfig(start).getSessionInfoTable() + ","
                    + Config.getConfig(start).getEmailPasswordUsersTable() + ","
                    + Config.getConfig(start).getPasswordResetTokensTable() + ","
                    + Config.getConfig(start).getEmailVerificationTokensTable() + ","
                    + Config.getConfig(start).getEmailVerificationTable() + ","
                    + Config.getConfig(start).getThirdPartyUsersTable() + ","
                    + Config.getConfig(start).getJWTSigningKeysTable() + ","
                    + Config.getConfig(start).getPasswordlessCodesTable() + ","
                    + Config.getConfig(start).getPasswordlessDevicesTable() + ","
                    + Config.getConfig(start).getPasswordlessUsersTable();
            executeUpdateQuery(start, DROP_QUERY);
        }

//        String DROP_QUERY = "DROP DATABASE " + Config.getConfig(start).getDatabaseName();
//        String CREATE_QUERY = "CREATE DATABASE " + Config.getConfig(start).getDatabaseName();
//        executeUpdateQuery(start, DROP_QUERY);
//        executeUpdateQuery(start, CREATE_QUERY);
    }

    public static long getUsersCount(Start start, SessionObject sessionObject, RECIPE_ID[] includeRecipeIds)
            throws SQLException, InterruptedException {
        StringBuilder QUERY = new StringBuilder(
                "SELECT COUNT(*) as total FROM " + Config.getConfig(start).getUsersTable());
        if (includeRecipeIds != null && includeRecipeIds.length > 0) {
            QUERY.append(" WHERE recipe_id IN (");
            for (int i = 0; i < includeRecipeIds.length; i++) {
                String recipeId = includeRecipeIds[i].toString();
                QUERY.append("'").append(recipeId).append("'");
                if (i != includeRecipeIds.length - 1) {
                    // not the last element
                    QUERY.append(",");
                }
            }
            QUERY.append(")");
        }

        Session session = (Session) sessionObject.getSession();
        NativeQuery nativeQuery = session.createNativeQuery(QUERY.toString());

        // TODO: check if this works ( earlier checking column total )
        List<Long> result = nativeQuery.list();

        if (result == null)
            return 0;

        return result.get(0);
    }

    public static AuthRecipeUserInfo[] getUsers(Start start, SessionObject sessionObject, @NotNull Integer limit,
            @NotNull String timeJoinedOrder, @Nullable RECIPE_ID[] includeRecipeIds, @Nullable String userId,
            @Nullable Long timeJoined) throws SQLException, StorageQueryException {

        // This list will be used to keep track of the result's order from the db
        List<UserInfoPaginationResultHolder> usersFromQuery = new ArrayList<>();

        {
            StringBuilder RECIPE_ID_CONDITION = new StringBuilder();
            if (includeRecipeIds != null && includeRecipeIds.length > 0) {
                RECIPE_ID_CONDITION.append("recipe_id IN (");
                for (int i = 0; i < includeRecipeIds.length; i++) {
                    String recipeId = includeRecipeIds[i].toString();
                    RECIPE_ID_CONDITION.append("'").append(recipeId).append("'");
                    if (i != includeRecipeIds.length - 1) {
                        // not the last element
                        RECIPE_ID_CONDITION.append(",");
                    }
                }
                RECIPE_ID_CONDITION.append(")");
            }

            if (timeJoined != null && userId != null) {
                String recipeIdCondition = RECIPE_ID_CONDITION.toString();
                if (!recipeIdCondition.equals("")) {
                    recipeIdCondition = recipeIdCondition + " AND";
                }
                String timeJoinedOrderSymbol = timeJoinedOrder.equals("ASC") ? ">" : "<";
                String QUERY = "SELECT user_id, recipe_id FROM " + Config.getConfig(start).getUsersTable() + " WHERE "
                        + recipeIdCondition + " (time_joined " + timeJoinedOrderSymbol
                        + " ? OR (time_joined = ? AND user_id <= ?)) ORDER BY time_joined " + timeJoinedOrder
                        + ", user_id DESC LIMIT ?";

                Session session = (Session) sessionObject.getSession();

                NativeQuery nativeQuery = session.createNativeQuery(QUERY.toString());
                nativeQuery.setParameter(1, timeJoined);
                nativeQuery.setParameter(2, timeJoined);
                nativeQuery.setParameter(3, userId);
                nativeQuery.setParameter(4, limit);

                // TODO: check if this works ( earlier checking column total )
                List<Object> result = nativeQuery.list();

                // TODO: fix this
                if (result != null) {
                    Iterator<Object> iterator = result.iterator();
                    while (iterator.hasNext()) {
                        usersFromQuery.add(new UserInfoPaginationResultHolder(
//                                result.getString("user_id"),
//                                result.getString("recipe_id"))
                                "a", "b"));
                    }
                }

            } else {
                String recipeIdCondition = RECIPE_ID_CONDITION.toString();
                if (!recipeIdCondition.equals("")) {
                    recipeIdCondition = " WHERE " + recipeIdCondition;
                }
                String QUERY = "SELECT user_id, recipe_id FROM " + Config.getConfig(start).getUsersTable()
                        + recipeIdCondition + " ORDER BY time_joined " + timeJoinedOrder + ", user_id DESC LIMIT ?";

                Session session = (Session) sessionObject.getSession();
                NativeQuery nativeQuery = session.createNativeQuery(QUERY.toString());
                nativeQuery.setParameter(1, limit);

                // TODO: check if this works ( earlier checking column total )
                List<Object> result = nativeQuery.list();

                // TODO: fix this
                if (result != null) {
                    Iterator<Object> iterator = result.iterator();
                    while (iterator.hasNext()) {

                        usersFromQuery.add(new UserInfoPaginationResultHolder(
//                                result.getString("user_id"),
//                                result.getString("recipe_id"))
                                "a", "b"));

                    }
                }
            }
        }

        // we create a map from recipe ID -> userId[]
        Map<RECIPE_ID, List<String>> recipeIdToUserIdListMap = new HashMap<>();
        for (UserInfoPaginationResultHolder user : usersFromQuery) {
            RECIPE_ID recipeId = RECIPE_ID.getEnumFromString(user.recipeId);
            if (recipeId == null) {
                throw new SQLException("Unrecognised recipe ID in database: " + user.recipeId);
            }
            List<String> userIdList = recipeIdToUserIdListMap.get(recipeId);
            if (userIdList == null) {
                userIdList = new ArrayList<>();
            }
            userIdList.add(user.userId);
            recipeIdToUserIdListMap.put(recipeId, userIdList);
        }

        AuthRecipeUserInfo[] finalResult = new AuthRecipeUserInfo[usersFromQuery.size()];

        // we give the userId[] for each recipe to fetch all those user's details
        for (RECIPE_ID recipeId : recipeIdToUserIdListMap.keySet()) {
            List<? extends AuthRecipeUserInfo> users = getUserInfoForRecipeIdFromUserIds(start, sessionObject, recipeId,
                    recipeIdToUserIdListMap.get(recipeId));

            // we fill in all the slots in finalResult based on their position in usersFromQuery
            Map<String, AuthRecipeUserInfo> userIdToInfoMap = new HashMap<>();
            for (AuthRecipeUserInfo user : users) {
                userIdToInfoMap.put(user.id, user);
            }
            for (int i = 0; i < usersFromQuery.size(); i++) {
                if (finalResult[i] == null) {
                    finalResult[i] = userIdToInfoMap.get(usersFromQuery.get(i).userId);
                }
            }
        }

        return finalResult;
    }

    private static List<? extends AuthRecipeUserInfo> getUserInfoForRecipeIdFromUserIds(Start start,
            SessionObject sessionObject, RECIPE_ID recipeId, List<String> userIds)
            throws StorageQueryException, SQLException {

        if (recipeId == RECIPE_ID.EMAIL_PASSWORD) {
            return EmailPasswordQueries.getUsersInfoUsingIdList(start, sessionObject, userIds);
        } else if (recipeId == RECIPE_ID.THIRD_PARTY) {
            return ThirdPartyQueries.getUsersInfoUsingIdList(start, sessionObject, userIds);
        } else {
            throw new IllegalArgumentException("No implementation of get users for recipe: " + recipeId.toString());
        }
    }

    private static class UserInfoPaginationResultHolder {
        String userId;
        String recipeId;

        UserInfoPaginationResultHolder(String userId, String recipeId) {
            this.userId = userId;
            this.recipeId = recipeId;
        }
    }

//    private static class KeyValueInfoRowMapper implements RowMapper<KeyValueInfo, ResultSet> {
//        private static final KeyValueInfoRowMapper INSTANCE = new KeyValueInfoRowMapper();
//
//        private KeyValueInfoRowMapper() {
//        }
//
//        private static KeyValueInfoRowMapper getInstance() {
//            return INSTANCE;
//        }
//
//        @Override
//        public KeyValueInfo map(ResultSet result) throws Exception {
//            return new KeyValueInfo(result.getString("value"), result.getLong("created_at_time"));
//        }
//    }
}
