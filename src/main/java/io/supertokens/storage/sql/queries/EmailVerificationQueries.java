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

import javax.persistence.LockModeType;
import java.sql.SQLException;
import java.util.List;

import static java.lang.System.currentTimeMillis;

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
            session.flush();
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

    public static void addEmailVerificationToken(Start start, String userId, String tokenHash, long expiry,
            String email) throws SQLException, StorageQueryException {

        ConnectionPool.withSession(start, (session, con) -> {
            final EmailVerificationTokensPK pk = new EmailVerificationTokensPK(userId, email, tokenHash);
            final EmailVerificationTokensDO toInsert = new EmailVerificationTokensDO(pk, expiry);
            session.save(EmailVerificationUsersPK.class, pk, toInsert);

            return null;
        }, true);
    }

    public static EmailVerificationTokenInfo getEmailVerificationTokenInfo(Start start, String token)
            throws SQLException, StorageQueryException {

        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity FROM EmailVerificationTokensDO entity " + "WHERE entity.pk.token = :token";

            CustomQueryWrapper<EmailVerificationTokensDO> q = session.createQuery(QUERY,
                    EmailVerificationTokensDO.class);
            q.setParameter("token", token);

            final List<EmailVerificationTokensDO> result = q.list();
            if (result.size() == 0) {
                return null;
            }
            return new EmailVerificationTokenInfo(result.get(0).getPk().getUser_id(), result.get(0).getPk().getToken(),
                    result.get(0).getToken_expiry(), result.get(0).getPk().getEmail());
        }, false);
    }

    public static EmailVerificationTokenInfo[] getAllEmailVerificationTokenInfoForUser_Transaction(
            CustomSessionWrapper session, String userId, String email) throws SQLException {

        String QUERY = "SELECT entity FROM EmailVerificationTokensDO entity "
                + "WHERE entity.pk.user_id = :user_id AND entity.pk.email = :email";

        CustomQueryWrapper<EmailVerificationTokensDO> q = session.createQuery(QUERY, EmailVerificationTokensDO.class);
        q.setParameter("user_id", userId);
        q.setParameter("email", email);
        q.setLockMode(LockModeType.PESSIMISTIC_WRITE);

        return entityToEmailVerificationTokenInfos(q.list());
    }

    public static EmailVerificationTokenInfo[] getAllEmailVerificationTokenInfoForUser(Start start, String userId,
            String email) throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity FROM EmailVerificationTokensDO entity "
                    + "WHERE entity.pk.user_id = :user_id AND entity.pk.email = :email";

            CustomQueryWrapper<EmailVerificationTokensDO> q = session.createQuery(QUERY,
                    EmailVerificationTokensDO.class);
            q.setParameter("user_id", userId);
            q.setParameter("email", email);

            return entityToEmailVerificationTokenInfos(q.list());
        }, false);
    }

    private static EmailVerificationTokenInfo[] entityToEmailVerificationTokenInfos(
            List<EmailVerificationTokensDO> result) {

        EmailVerificationTokenInfo[] finalResult = new EmailVerificationTokenInfo[result.size()];
        for (int i = 0; i < result.size(); i++) {
            EmailVerificationTokensDO curr = result.get(i);
            finalResult[i] = new EmailVerificationTokenInfo(curr.getPk().getUser_id(), curr.getPk().getToken(),
                    curr.getToken_expiry(), curr.getPk().getEmail());
        }
        return finalResult;
    }

    public static boolean isEmailVerified(Start start, String userId, String email)
            throws SQLException, StorageQueryException {

        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity FROM EmailVerificationDO entity WHERE entity.pk.user_id = :user_id AND "
                    + "entity.pk.email = :email";
            CustomQueryWrapper<EmailVerificationDO> q = session.createQuery(QUERY, EmailVerificationDO.class);
            q.setParameter("user_id", userId);
            q.setParameter("email", email);

            return q.list().size() > 0;
        }, false);
    }

    public static void deleteUserInfo(Start start, String userId)
            throws StorageQueryException, StorageTransactionLogicException, SQLException {
        ConnectionPool.withSession(start, (session, con) -> {
            {
                String QUERY = "DELETE FROM EmailVerificationDO entity WHERE entity.pk.user_id = :userid";
                session.createQuery(QUERY).setParameter("userid", userId).executeUpdate();
            }
            {
                String QUERY = "DELETE FROM EmailVerificationTokensDO entity WHERE entity.pk.user_id = :userid";
                session.createQuery(QUERY).setParameter("userid", userId).executeUpdate();
            }
            return null;
        }, true);
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

}