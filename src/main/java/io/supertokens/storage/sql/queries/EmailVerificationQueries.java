/*
 *    Copyright (c) 2021, VRAI Labs and/or its affiliates. All rights reserved.
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

import io.supertokens.pluginInterface.RowMapper;
import io.supertokens.pluginInterface.emailverification.EmailVerificationTokenInfo;
import io.supertokens.pluginInterface.exceptions.StorageQueryException;

import io.supertokens.pluginInterface.exceptions.StorageTransactionLogicException;
import io.supertokens.pluginInterface.sqlStorage.SessionObject;
import io.supertokens.storage.sql.Start;
import io.supertokens.storage.sql.config.Config;
import io.supertokens.storage.sql.dataaccessobjects.emailverification.impl.EmailVerificationTokensDAO;
import io.supertokens.storage.sql.dataaccessobjects.emailverification.impl.EmailverificationVerifiedEmailsDAO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationTokensDO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationVerifiedEmailsDO;
import io.supertokens.storage.sql.exceptions.UserAndEmailNotFoundException;
import org.hibernate.Session;

import javax.persistence.NoResultException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EmailVerificationQueries {

    static String getQueryToCreateEmailVerificationTable(Start start) {
        return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getEmailVerificationTable() + " ("
                + "user_id VARCHAR(128) NOT NULL," + "email VARCHAR(256) NOT NULL," + "PRIMARY KEY (user_id, email));";
    }

    static String getQueryToCreateEmailVerificationTokensTable(Start start) {
        return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getEmailVerificationTokensTable() + " ("
                + "user_id VARCHAR(128) NOT NULL," + "email VARCHAR(256) NOT NULL,"
                + "token VARCHAR(128) NOT NULL UNIQUE," + "token_expiry BIGINT  NOT NULL,"
                + "PRIMARY KEY (user_id, email, token))";
    }

    static String getQueryToCreateEmailVerificationTokenExpiryIndex(Start start) {
        return "CREATE INDEX emailverification_tokens_index ON "
                + Config.getConfig(start).getEmailVerificationTokensTable() + "(token_expiry);";
    }

    public static void deleteExpiredEmailVerificationTokens(Start start, SessionObject sessionObject)
            throws SQLException {
        EmailVerificationTokensDAO emailVerificationTokensDAO = new EmailVerificationTokensDAO(sessionObject);

        emailVerificationTokensDAO.deleteFromTableWhereTokenExpiryIsLessThan(System.currentTimeMillis());
    }

    public static void updateUsersIsEmailVerified_Transaction(Start start, SessionObject sessionObject, String userId,
            String email, boolean isEmailVerified) throws SQLException {

        EmailverificationVerifiedEmailsDAO emailverificationVerifiedEmailsDAO = new EmailverificationVerifiedEmailsDAO(
                sessionObject);

        if (isEmailVerified) {
            emailverificationVerifiedEmailsDAO.insertIntoTable(userId, email);
        } else {
            try {
                emailverificationVerifiedEmailsDAO.deleteFromTableWhereUserIdEqualsAndEmailEquals(userId, email);
            } catch (UserAndEmailNotFoundException u) {
                // do nothing for now, figure out error handling chain
            }
        }
    }

    public static void deleteAllEmailVerificationTokensForUser_Transaction(Start start, SessionObject sessionObject,
            String userId, String email) throws SQLException {

        EmailVerificationTokensDAO emailVerificationTokensDAO = new EmailVerificationTokensDAO(sessionObject);
        try {
            emailVerificationTokensDAO.deleteFromTableWhereUserIdEqualsAndEmailEquals(userId, email);
        } catch (UserAndEmailNotFoundException u) {
            // do nothing for now
        }
    }

    public static EmailVerificationTokenInfo getEmailVerificationTokenInfo(Start start, SessionObject sessionObject,
            String token) throws NoResultException {

        EmailVerificationTokensDAO emailVerificationTokensDAO = new EmailVerificationTokensDAO(sessionObject);

        EmailVerificationTokensDO emailVerificationTokensDO = emailVerificationTokensDAO
                .getEmailVerificationTokenWhereTokenEquals(token);

        return new EmailVerificationTokenInfo(emailVerificationTokensDO.getPrimary_key().getUser_id(),
                emailVerificationTokensDO.getPrimary_key().getToken(), emailVerificationTokensDO.getToken_expiry(),
                emailVerificationTokensDO.getPrimary_key().getEmail());
    }

    public static void addEmailVerificationToken(Start start, SessionObject sessionObject, String userId,
            String tokenHash, long expiry, String email) throws SQLException {

        EmailVerificationTokensDAO emailVerificationTokensDAO = new EmailVerificationTokensDAO(sessionObject);
        emailVerificationTokensDAO.insertIntoTable(userId, email, tokenHash, expiry);
    }

    public static EmailVerificationTokenInfo[] getAllEmailVerificationTokenInfoForUser_Transaction(Start start,
            SessionObject sessionObject, String userId, String email) throws SQLException, StorageQueryException {

        EmailVerificationTokensDAO emailVerificationTokensDAO = new EmailVerificationTokensDAO(sessionObject);

        List<EmailVerificationTokensDO> list = emailVerificationTokensDAO
                .getEmailVerificationTokenWhereUserIdEqualsAndEmailEquals_locked(userId, email);

        EmailVerificationTokenInfo[] finalResult = new EmailVerificationTokenInfo[list.size()];
        for (int i = 0; i < list.size(); i++) {
            EmailVerificationTokensDO tokensDO = list.get(i);
            finalResult[i] = new EmailVerificationTokenInfo(tokensDO.getPrimary_key().getUser_id(),
                    tokensDO.getPrimary_key().getToken(), tokensDO.getToken_expiry(),
                    tokensDO.getPrimary_key().getEmail());
        }
        return finalResult;

    }

    public static EmailVerificationTokenInfo[] getAllEmailVerificationTokenInfoForUser(Start start,
            SessionObject sessionObject, String userId, String email) throws NoResultException {

        EmailVerificationTokensDAO emailVerificationTokensDAO = new EmailVerificationTokensDAO(sessionObject);

        List<EmailVerificationTokensDO> list = emailVerificationTokensDAO
                .getEmailVerificationTokenWhereUserIdEqualsAndEmailEquals_locked(userId, email);

        EmailVerificationTokenInfo[] finalResult = new EmailVerificationTokenInfo[list.size()];
        for (int i = 0; i < list.size(); i++) {
            EmailVerificationTokensDO tokensDO = list.get(i);
            finalResult[i] = new EmailVerificationTokenInfo(tokensDO.getPrimary_key().getUser_id(),
                    tokensDO.getPrimary_key().getToken(), tokensDO.getToken_expiry(),
                    tokensDO.getPrimary_key().getEmail());
        }
        return finalResult;

    }

    public static boolean isEmailVerified(Start start, SessionObject sessionObject, String userId, String email)
            throws NoResultException {

        EmailverificationVerifiedEmailsDAO emailverificationVerifiedEmailsDAO = new EmailverificationVerifiedEmailsDAO(
                sessionObject);

        EmailVerificationVerifiedEmailsDO emailsDO = emailverificationVerifiedEmailsDAO
                .getWhereUserIdEqualsAndEmailEquals(userId, email);

        if (emailsDO != null)
            return true;

        return false;
    }

    public static void deleteUserInfo(Start start, String userId)
            throws StorageQueryException, StorageTransactionLogicException {
        start.startTransactionHibernate(session -> {
            {
                EmailverificationVerifiedEmailsDAO emailverificationVerifiedEmailsDAO = new EmailverificationVerifiedEmailsDAO(
                        session);
                emailverificationVerifiedEmailsDAO.deleteWherePrimaryKeyEquals(userId);
            }

            {
                EmailVerificationTokensDAO emailVerificationTokensDAO = new EmailVerificationTokensDAO(session);
                emailVerificationTokensDAO.deleteWherePrimaryKeyEquals(userId);

            }
            return null;
        });
    }

    public static void unverifyEmail(Start start, SessionObject sessionObject, String userId, String email)
            throws SQLException, UserAndEmailNotFoundException {
        EmailverificationVerifiedEmailsDAO emailverificationVerifiedEmailsDAO = new EmailverificationVerifiedEmailsDAO(
                sessionObject);

        emailverificationVerifiedEmailsDAO.deleteFromTableWhereUserIdEqualsAndEmailEquals(userId, email);
    }

    public static void revokeAllTokens(Start start, SessionObject sessionObject, String userId, String email)
            throws UserAndEmailNotFoundException {
        EmailverificationVerifiedEmailsDAO emailverificationVerifiedEmailsDAO = new EmailverificationVerifiedEmailsDAO(
                sessionObject);
        emailverificationVerifiedEmailsDAO.deleteFromTableWhereUserIdEqualsAndEmailEquals(userId, email);
    }

    private static class EmailVerificationTokenInfoRowMapper
            implements RowMapper<EmailVerificationTokenInfo, ResultSet> {
        private static final EmailVerificationTokenInfoRowMapper INSTANCE = new EmailVerificationTokenInfoRowMapper();

        private EmailVerificationTokenInfoRowMapper() {
        }

        private static EmailVerificationTokenInfoRowMapper getInstance() {
            return INSTANCE;
        }

        @Override
        public EmailVerificationTokenInfo map(ResultSet result) throws Exception {
            return new EmailVerificationTokenInfo(result.getString("user_id"), result.getString("token"),
                    result.getLong("token_expiry"), result.getString("email"));
        }
    }
}
