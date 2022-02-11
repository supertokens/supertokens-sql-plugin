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
import io.supertokens.pluginInterface.emailpassword.PasswordResetTokenInfo;
import io.supertokens.pluginInterface.emailpassword.UserInfo;
import io.supertokens.pluginInterface.emailpassword.exceptions.UnknownUserIdException;
import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.pluginInterface.sqlStorage.SessionObject;
import io.supertokens.storage.sql.Start;
import io.supertokens.storage.sql.config.Config;
import io.supertokens.storage.sql.dataaccessobjects.emailpassword.impl.EmailPasswordPswdResetTokensDAO;
import io.supertokens.storage.sql.dataaccessobjects.emailpassword.impl.EmailPasswordUsersDAO;
import io.supertokens.storage.sql.dataaccessobjects.passwordless.impl.UsersDAO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordPswdResetTokensDO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordUsersDO;
import io.supertokens.storage.sql.enums.OrderEnum;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import javax.persistence.NoResultException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EmailPasswordQueries {

    // TODO: move these to liquibase
    static String getQueryToCreateUsersTable(Start start) {
        return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getEmailPasswordUsersTable() + " ("
                + "user_id CHAR(36) NOT NULL," + "email VARCHAR(256) NOT NULL UNIQUE,"
                + "password_hash VARCHAR(128) NOT NULL," + "time_joined BIGINT  NOT NULL," + "PRIMARY KEY (user_id));";
    }

    static String getQueryToCreatePasswordResetTokensTable(Start start) {
        return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getPasswordResetTokensTable() + " ("
                + "user_id CHAR(36) NOT NULL," + "token VARCHAR(128) NOT NULL UNIQUE,"
                + "token_expiry BIGINT  NOT NULL," + "PRIMARY KEY (user_id, token),"
                + "FOREIGN KEY (user_id) REFERENCES " + Config.getConfig(start).getEmailPasswordUsersTable()
                + "(user_id) ON DELETE CASCADE ON UPDATE CASCADE);";
    }

    static String getQueryToCreatePasswordResetTokenExpiryIndex(Start start) {
        return "CREATE INDEX emailpassword_password_reset_token_expiry_index ON "
                + Config.getConfig(start).getPasswordResetTokensTable() + "(token_expiry);";
    }

    public static void deleteExpiredPasswordResetTokens(Start start, SessionObject sessionObject) throws SQLException {
        EmailPasswordPswdResetTokensDAO emailPasswordPswdResetTokensDAO = new EmailPasswordPswdResetTokensDAO(
                sessionObject);

        emailPasswordPswdResetTokensDAO.deleteWhereTokenExpiryIsLessThan(System.currentTimeMillis());
    }

    public static void updateUsersPassword_Transaction(Start start, SessionObject sessionObject, String userId,
            String newPassword) throws SQLException {

        EmailPasswordUsersDAO emailPasswordUsersDAO = new EmailPasswordUsersDAO(sessionObject);
        emailPasswordUsersDAO.updatePasswordHashWhereUserId(userId, newPassword);

    }

    public static void updateUsersEmail_Transaction(Start start, SessionObject sessionObject, String userId,
            String newEmail) throws SQLException, UnknownUserIdException {

        EmailPasswordUsersDAO emailPasswordUsersDAO = new EmailPasswordUsersDAO(sessionObject);
        emailPasswordUsersDAO.updateEmailWhereUserId(userId, newEmail);
    }

    public static void deleteAllPasswordResetTokensForUser_Transaction(Start start, SessionObject sessionObject,
            String userId) throws SQLException {
        EmailPasswordPswdResetTokensDAO emailPasswordPswdResetTokensDAO = new EmailPasswordPswdResetTokensDAO(
                sessionObject);
        emailPasswordPswdResetTokensDAO.deleteAllWhereUserIdEquals(userId);
    }

    public static PasswordResetTokenInfo[] getAllPasswordResetTokenInfoForUser(Start start, SessionObject sessionObject,
            String userId) throws SQLException, StorageQueryException {

        EmailPasswordPswdResetTokensDAO emailPasswordPswdResetTokensDAO = new EmailPasswordPswdResetTokensDAO(
                sessionObject);
        List<EmailPasswordPswdResetTokensDO> results = emailPasswordPswdResetTokensDAO
                .getAllPasswordResetTokenInfoForUser(userId);

        PasswordResetTokenInfo[] finalResult = new PasswordResetTokenInfo[results.size()];
        for (int i = 0; i < results.size(); i++) {
            EmailPasswordPswdResetTokensDO tokensDO = results.get(i);
            finalResult[i] = new PasswordResetTokenInfo(tokensDO.getPrimaryKey().getUser_id().getUser_id(),
                    tokensDO.getPrimaryKey().getToken(), tokensDO.getToken_expiry());
        }
        return finalResult;
    }

    public static PasswordResetTokenInfo[] getAllPasswordResetTokenInfoForUser_Transaction(Start start,
            SessionObject sessionObject, String userId) throws SQLException, StorageQueryException {

        EmailPasswordPswdResetTokensDAO emailPasswordPswdResetTokensDAO = new EmailPasswordPswdResetTokensDAO(
                sessionObject);
        List<EmailPasswordPswdResetTokensDO> results = emailPasswordPswdResetTokensDAO
                .getAllPasswordResetTokenInfoForUser_locked(userId);

        PasswordResetTokenInfo[] finalResult = new PasswordResetTokenInfo[results.size()];
        for (int i = 0; i < results.size(); i++) {
            EmailPasswordPswdResetTokensDO tokensDO = results.get(i);
            finalResult[i] = new PasswordResetTokenInfo(tokensDO.getPrimaryKey().getUser_id().getUser_id(),
                    tokensDO.getPrimaryKey().getToken(), tokensDO.getToken_expiry());
        }
        return finalResult;
    }

    public static PasswordResetTokenInfo getPasswordResetTokenInfo(Start start, SessionObject sessionObject,
            String token) throws SQLException, StorageQueryException {

        EmailPasswordPswdResetTokensDAO emailPasswordPswdResetTokensDAO = new EmailPasswordPswdResetTokensDAO(
                sessionObject);
        EmailPasswordPswdResetTokensDO emailPasswordPswdResetTokensDO = emailPasswordPswdResetTokensDAO
                .getPasswordResetTokenInfo(token);

        return new PasswordResetTokenInfo(emailPasswordPswdResetTokensDO.getPrimaryKey().getUser_id().getUser_id(),
                emailPasswordPswdResetTokensDO.getPrimaryKey().getToken(),
                emailPasswordPswdResetTokensDO.getToken_expiry());
    }

    public static void addPasswordResetToken(Start start, SessionObject sessionObject, String userId, String tokenHash,
            long expiry) throws SQLException, UnknownUserIdException {
        EmailPasswordPswdResetTokensDAO emailPasswordPswdResetTokensDAO = new EmailPasswordPswdResetTokensDAO(
                sessionObject);
        emailPasswordPswdResetTokensDAO.insertPasswordResetTokenInfo(userId, tokenHash, expiry);
    }

    public static void signUp(Start start, String userId, String email, String passwordHash, long timeJoined)
            throws StorageQueryException {
        start.startTransactionHibernate(session -> {

            {
                UsersDAO usersDAO = new UsersDAO(session);
                usersDAO.insertIntoTableValues(userId, RECIPE_ID.EMAIL_PASSWORD.toString(), timeJoined);
            }

            {
                EmailPasswordUsersDAO emailPasswordUsersDAO = new EmailPasswordUsersDAO(session);
                emailPasswordUsersDAO.insert(userId, email, passwordHash, timeJoined);
            }

            return null;
        });
    }

    public static void deleteUser(Start start, String userId) throws StorageQueryException {
        start.startTransactionHibernate(session -> {
            UsersDAO usersDAO = new UsersDAO(session);
            usersDAO.deleteWhereUserIdEqualsAndRecipeIdEquals(userId, RECIPE_ID.EMAIL_PASSWORD.toString());
            EmailPasswordUsersDAO emailPasswordUsersDAO = new EmailPasswordUsersDAO(session);
            emailPasswordUsersDAO.deleteWherePrimaryKeyEquals(userId);
            return null;
        });
    }

    public static UserInfo getUserInfoUsingId(Start start, SessionObject sessionObject, String id)
            throws SQLException, StorageQueryException {
        List<String> input = new ArrayList<>();
        input.add(id);
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
            StringBuilder QUERY = new StringBuilder("SELECT user_id, email, password_hash, time_joined FROM "
                    + Config.getConfig(start).getEmailPasswordUsersTable());
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

            List<EmailPasswordUsersDO> list = nativeQuery.getResultList();

            Iterator<EmailPasswordUsersDO> iterator = list.iterator();
            while (iterator.hasNext()) {
                finalResult.add(UserInfoRowMapper.getInstance().mapOrThrow(iterator.next()));
            }
        }

        return finalResult;

    }

    public static UserInfo getUserInfoUsingId_Transaction(Start start, SessionObject sessionObject, String id)
            throws SQLException, StorageQueryException {

        EmailPasswordUsersDAO emailPasswordUsersDAO = new EmailPasswordUsersDAO(sessionObject);
        try {
            EmailPasswordUsersDO emailPasswordUsersDO = emailPasswordUsersDAO.getWhereUserIdEquals_locked(id);
            return new UserInfo(emailPasswordUsersDO.getUser_id(), emailPasswordUsersDO.getEmail(),
                    emailPasswordUsersDO.getPassword_hash(), emailPasswordUsersDO.getTime_joined());
        } catch (NoResultException noResultException) {
            return null;
        }
    }

    public static UserInfo getUserInfoUsingEmail(Start start, SessionObject sessionObject, String email)
            throws SQLException, StorageQueryException {

        EmailPasswordUsersDAO emailPasswordUsersDAO = new EmailPasswordUsersDAO(sessionObject);
        try {
            EmailPasswordUsersDO emailPasswordUsersDO = emailPasswordUsersDAO.getWhereEmailEquals(email);

            return new UserInfo(emailPasswordUsersDO.getUser_id(), emailPasswordUsersDO.getEmail(),
                    emailPasswordUsersDO.getPassword_hash(), emailPasswordUsersDO.getTime_joined());
        } catch (NoResultException noResultException) {
            return null;
        }

    }

    @Deprecated
    public static UserInfo[] getUsersInfo(Start start, SessionObject sessionObject, Integer limit,
            String timeJoinedOrder) throws SQLException, StorageQueryException {
        EmailPasswordUsersDAO emailPasswordUsersDAO = new EmailPasswordUsersDAO(sessionObject);
        List<EmailPasswordUsersDO> list = emailPasswordUsersDAO.getLimitedOrderByTimeJoinedAndUserId(timeJoinedOrder,
                OrderEnum.DESC.name(), limit);
        int size = list.size();
        UserInfo[] finalResult = new UserInfo[size];
        for (int i = 0; i < size; i++) {
            finalResult[i] = UserInfoRowMapper.getInstance().mapOrThrow(list.get(i));
        }

        return finalResult;
    }

    @Deprecated
    public static UserInfo[] getUsersInfo(Start start, SessionObject sessionObject, String userId, Long timeJoined,
            Integer limit, String timeJoinedOrder) throws SQLException, StorageQueryException {

        EmailPasswordUsersDAO emailPasswordUsersDAO = new EmailPasswordUsersDAO(sessionObject);
        List<EmailPasswordUsersDO> list = emailPasswordUsersDAO.getLimitedUsersInfo(timeJoinedOrder, timeJoined,
                OrderEnum.DESC.name(), userId, limit);
        int size = list.size();
        UserInfo[] finalResult = new UserInfo[size];
        for (int i = 0; i < size; i++) {
            finalResult[i] = UserInfoRowMapper.getInstance().mapOrThrow(list.get(i));
        }

        return finalResult;

    }

    @Deprecated
    public static long getUsersCount(Start start, SessionObject sessionObject) throws SQLException {
        EmailPasswordUsersDAO emailPasswordUsersDAO = new EmailPasswordUsersDAO(sessionObject);
        return emailPasswordUsersDAO.getCount();
    }

    private static class PasswordResetTokenInfoRowMapper implements RowMapper<PasswordResetTokenInfo, ResultSet> {
        private static final PasswordResetTokenInfoRowMapper INSTANCE = new PasswordResetTokenInfoRowMapper();

        private PasswordResetTokenInfoRowMapper() {
        }

        private static PasswordResetTokenInfoRowMapper getInstance() {
            return INSTANCE;
        }

        @Override
        public PasswordResetTokenInfo map(ResultSet result) throws Exception {
            return new PasswordResetTokenInfo(result.getString("user_id"), result.getString("token"),
                    result.getLong("token_expiry"));
        }
    }

    private static class UserInfoRowMapper implements RowMapper<UserInfo, EmailPasswordUsersDO> {
        private static final UserInfoRowMapper INSTANCE = new UserInfoRowMapper();

        private UserInfoRowMapper() {
        }

        private static UserInfoRowMapper getInstance() {
            return INSTANCE;
        }

        @Override
        public UserInfo map(EmailPasswordUsersDO result) throws Exception {
            return new UserInfo(result.getUser_id(), result.getEmail(), result.getPassword_hash(),
                    result.getTime_joined());
        }
    }
}
