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
import io.supertokens.storage.sql.ConnectionPool;
import io.supertokens.storage.sql.Start;
import io.supertokens.storage.sql.config.Config;
import io.supertokens.storage.sql.domainobject.emailverification.EmailVerificationDO;
import io.supertokens.storage.sql.domainobject.emailverification.EmailVerificationTokensDO;
import io.supertokens.storage.sql.domainobject.emailverification.EmailVerificationTokensPK;
import io.supertokens.storage.sql.domainobject.emailverification.EmailVerificationUsersPK;
import io.supertokens.storage.sql.hibernate.CustomQueryWrapper;
import io.supertokens.storage.sql.hibernate.CustomSessionWrapper;
import io.supertokens.storage.sql.utils.Utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static io.supertokens.storage.sql.QueryExecutorTemplate.execute;
import static io.supertokens.storage.sql.QueryExecutorTemplate.update;
import static io.supertokens.storage.sql.config.Config.getConfig;
import static java.lang.System.currentTimeMillis;

@interface Done {
}

public class EmailVerificationQueries {

    static String getQueryToCreateEmailVerificationTable(Start start) {
        String schema = Config.getConfig(start).getTableSchema();
        String emailVerificationTable = Config.getConfig(start).getEmailVerificationTable();
        // @formatter:off
        return "CREATE TABLE IF NOT EXISTS " + emailVerificationTable + " ("
                + "user_id VARCHAR(128) NOT NULL,"
                + "email VARCHAR(256) NOT NULL,"
                + "CONSTRAINT " + Utils.getConstraintName(schema, emailVerificationTable, null, "pkey") +
                " PRIMARY KEY (user_id, email));";
        // @formatter:on
    }

    static String getQueryToCreateEmailVerificationTokensTable(Start start) {
        String schema = Config.getConfig(start).getTableSchema();
        String emailVerificationTokensTable = Config.getConfig(start).getEmailVerificationTokensTable();
        // @formatter:off
        return "CREATE TABLE IF NOT EXISTS " + emailVerificationTokensTable + " ("
                + "user_id VARCHAR(128) NOT NULL,"
                + "email VARCHAR(256) NOT NULL,"
                + "token VARCHAR(128) NOT NULL CONSTRAINT " +
                Utils.getConstraintName(schema, emailVerificationTokensTable, "token", "key") + " UNIQUE,"
                + "token_expiry BIGINT NOT NULL,"
                + "CONSTRAINT " + Utils.getConstraintName(schema, emailVerificationTokensTable, null, "pkey") +
                " PRIMARY KEY (user_id, email, token))";
        // @formatter:on
    }

    static String getQueryToCreateEmailVerificationTokenExpiryIndex(Start start) {
        return "CREATE INDEX emailverification_tokens_index ON "
                + Config.getConfig(start).getEmailVerificationTokensTable() + "(token_expiry);";
    }

    public static void deleteExpiredEmailVerificationTokens(Start start) throws SQLException, StorageQueryException {
        ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "DELETE FROM EmailVerificationTokensDO where token_expiry < :expiry";
            CustomQueryWrapper q = session.createQuery(QUERY);
            q.setParameter("expiry", currentTimeMillis());
            q.executeUpdate();
            return null;
        }, true);
    }

    public static void updateUsersIsEmailVerified_Transaction(CustomSessionWrapper session, String userId, String email,
            boolean isEmailVerified) throws SQLException {
        if (isEmailVerified) {
            final EmailVerificationUsersPK pk = new EmailVerificationUsersPK(userId, email);
            final EmailVerificationDO toInsert = new EmailVerificationDO(pk);

            session.save(EmailVerificationUsersPK.class, pk, toInsert);
        } else {
            String QUERY = "DELETE FROM EmailVerificationDO entity "
                    + "WHERE entity.pk.user_id = :user_id AND entity.pk.email = :email";

            CustomQueryWrapper q = session.createQuery(QUERY);
            q.setParameter("user_id", userId);
            q.setParameter("email", email);
            q.executeUpdate();
        }
    }

    public static void deleteAllEmailVerificationTokensForUser_Transaction(CustomSessionWrapper session, String userId,
            String email) throws SQLException {
        String QUERY = "DELETE FROM EmailVerificationTokensDO entity "
                + "WHERE entity.pk.user_id = :user_id AND entity.pk.email = :email";

        CustomQueryWrapper q = session.createQuery(QUERY);
        q.setParameter("user_id", userId);
        q.setParameter("email", email);
        q.executeUpdate();
    }

    public static EmailVerificationTokenInfo getEmailVerificationTokenInfo(Start start, String token)
            throws SQLException, StorageQueryException {
        String QUERY = "SELECT user_id, token, token_expiry, email FROM "
                + getConfig(start).getEmailVerificationTokensTable() + " WHERE token = ?";
        return execute(start, QUERY, pst -> pst.setString(1, token), result -> {
            if (result.next()) {
                return EmailVerificationTokenInfoRowMapper.getInstance().mapOrThrow(result);
            }
            return null;
        });
    }

    public static void addEmailVerificationToken(Start start, String userId, String tokenHash, long expiry,
            String email) throws SQLException, StorageQueryException {

        ConnectionPool.withSession(start, (session, con) -> {
            final EmailVerificationTokensPK pk1 = new EmailVerificationTokensPK(userId, email, tokenHash);
            final EmailVerificationTokensDO toInsert = new EmailVerificationTokensDO(pk1, expiry);
            session.save(EmailVerificationUsersPK.class, pk1, toInsert);

            return null;
        }, true);
    }

    public static EmailVerificationTokenInfo[] getAllEmailVerificationTokenInfoForUser_Transaction(Start start,
            Connection con, String userId, String email) throws SQLException, StorageQueryException {

        String QUERY = "SELECT user_id, token, token_expiry, email FROM "
                + getConfig(start).getEmailVerificationTokensTable() + " WHERE user_id = ? AND email = ? FOR UPDATE";

        return execute(con, QUERY, pst -> {
            pst.setString(1, userId);
            pst.setString(2, email);
        }, result -> {
            List<EmailVerificationTokenInfo> temp = new ArrayList<>();
            while (result.next()) {
                temp.add(EmailVerificationTokenInfoRowMapper.getInstance().mapOrThrow(result));
            }
            EmailVerificationTokenInfo[] finalResult = new EmailVerificationTokenInfo[temp.size()];
            for (int i = 0; i < temp.size(); i++) {
                finalResult[i] = temp.get(i);
            }
            return finalResult;
        });
    }

    public static EmailVerificationTokenInfo[] getAllEmailVerificationTokenInfoForUser(Start start, String userId,
            String email) throws SQLException, StorageQueryException {
        String QUERY = "SELECT user_id, token, token_expiry, email FROM "
                + getConfig(start).getEmailVerificationTokensTable() + " WHERE user_id = ? AND email = ?";

        return execute(start, QUERY, pst -> {
            pst.setString(1, userId);
            pst.setString(2, email);
        }, result -> {
            List<EmailVerificationTokenInfo> temp = new ArrayList<>();
            while (result.next()) {
                temp.add(EmailVerificationTokenInfoRowMapper.getInstance().mapOrThrow(result));
            }
            EmailVerificationTokenInfo[] finalResult = new EmailVerificationTokenInfo[temp.size()];
            for (int i = 0; i < temp.size(); i++) {
                finalResult[i] = temp.get(i);
            }
            return finalResult;
        });
    }

    public static boolean isEmailVerified(Start start, String userId, String email)
            throws SQLException, StorageQueryException {
        String QUERY = "SELECT * FROM " + getConfig(start).getEmailVerificationTable()
                + " WHERE user_id = ? AND email = ?";

        return execute(start, QUERY, pst -> {
            pst.setString(1, userId);
            pst.setString(2, email);
        }, result -> result.next());
    }

    public static void deleteUserInfo(Start start, String userId)
            throws StorageQueryException, StorageTransactionLogicException {
        start.startTransaction(con -> {
            Connection sqlCon = (Connection) con.getConnection();
            try {
                {
                    String QUERY = "DELETE FROM " + getConfig(start).getEmailVerificationTable() + " WHERE user_id = ?";
                    update(sqlCon, QUERY, pst -> pst.setString(1, userId));
                }

                {
                    String QUERY = "DELETE FROM " + getConfig(start).getEmailVerificationTokensTable()
                            + " WHERE user_id = ?";

                    update(sqlCon, QUERY, pst -> pst.setString(1, userId));
                }

                sqlCon.commit();
            } catch (SQLException throwables) {
                throw new StorageTransactionLogicException(throwables);
            }
            return null;
        });
    }

    public static void unverifyEmail(Start start, String userId, String email)
            throws SQLException, StorageQueryException {
        ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "DELETE FROM EmailVerificationDO entity WHERE entity.pk.user_id = :user_id AND entity.pk"
                    + ".email = :email";
            CustomQueryWrapper q = session.createQuery(QUERY);
            q.setParameter("user_id", userId);
            q.setParameter("email", email);
            q.executeUpdate();
            return null;
        }, true);
    }

    public static void revokeAllTokens(Start start, String userId, String email)
            throws SQLException, StorageQueryException {
        ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "DELETE FROM EmailVerificationTokensDO entity WHERE entity.pk.user_id = :user_id AND "
                    + "entity.pk.email = :email";
            CustomQueryWrapper q = session.createQuery(QUERY);
            q.setParameter("user_id", userId);
            q.setParameter("email", email);
            q.executeUpdate();
            return null;
        }, true);
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