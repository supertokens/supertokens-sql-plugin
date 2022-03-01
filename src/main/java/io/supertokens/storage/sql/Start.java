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
 *
 */

package io.supertokens.storage.sql;

import ch.qos.logback.classic.Logger;
import com.google.gson.JsonObject;
import io.supertokens.pluginInterface.KeyValueInfo;
import io.supertokens.pluginInterface.RECIPE_ID;
import io.supertokens.pluginInterface.STORAGE_TYPE;
import io.supertokens.pluginInterface.authRecipe.AuthRecipeUserInfo;
import io.supertokens.pluginInterface.emailpassword.PasswordResetTokenInfo;
import io.supertokens.pluginInterface.emailpassword.UserInfo;
import io.supertokens.pluginInterface.emailpassword.exceptions.DuplicateEmailException;
import io.supertokens.pluginInterface.emailpassword.exceptions.DuplicatePasswordResetTokenException;
import io.supertokens.pluginInterface.emailpassword.exceptions.DuplicateUserIdException;
import io.supertokens.pluginInterface.emailpassword.exceptions.UnknownUserIdException;
import io.supertokens.pluginInterface.emailpassword.sqlStorage.EmailPasswordSQLStorage;
import io.supertokens.pluginInterface.emailverification.EmailVerificationTokenInfo;
import io.supertokens.pluginInterface.emailverification.exception.DuplicateEmailVerificationTokenException;
import io.supertokens.pluginInterface.emailverification.sqlStorage.EmailVerificationSQLStorage;
import io.supertokens.pluginInterface.exceptions.QuitProgramFromPluginException;
import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.pluginInterface.exceptions.StorageTransactionLogicException;
import io.supertokens.pluginInterface.jwt.JWTSigningKeyInfo;
import io.supertokens.pluginInterface.jwt.exceptions.DuplicateKeyIdException;
import io.supertokens.pluginInterface.jwt.sqlstorage.JWTRecipeSQLStorage;
import io.supertokens.pluginInterface.passwordless.PasswordlessCode;
import io.supertokens.pluginInterface.passwordless.PasswordlessDevice;
import io.supertokens.pluginInterface.passwordless.exception.*;
import io.supertokens.pluginInterface.passwordless.sqlStorage.PasswordlessSQLStorage;
import io.supertokens.pluginInterface.session.SessionInfo;
import io.supertokens.pluginInterface.session.sqlStorage.SessionSQLStorage;
import io.supertokens.pluginInterface.sqlStorage.SessionObject;
import io.supertokens.pluginInterface.thirdparty.exception.DuplicateThirdPartyUserException;
import io.supertokens.pluginInterface.thirdparty.sqlStorage.ThirdPartySQLStorage;
import io.supertokens.storage.sql.config.Config;
import io.supertokens.storage.sql.constants.ConstraintNameConstants;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessCodesDO;
import io.supertokens.storage.sql.output.Logging;
import io.supertokens.storage.sql.queries.*;
import io.supertokens.storage.sql.singletons.ConfigObject;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.jdbc.Work;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLTransactionRollbackException;
import java.util.List;

import static io.supertokens.storage.sql.constants.ErrorMessageConstants.DUPLICATE_ENTRY;
import static io.supertokens.storage.sql.constants.ErrorMessageConstants.FOREIGN_KEY;

public class Start implements SessionSQLStorage, EmailPasswordSQLStorage, EmailVerificationSQLStorage,
        ThirdPartySQLStorage, JWTRecipeSQLStorage, PasswordlessSQLStorage {

    private static final Object appenderLock = new Object();
    public static boolean silent = false;
    private ResourceDistributor resourceDistributor = new ResourceDistributor();
    private String processId;
    private HikariLoggingAppender appender = new HikariLoggingAppender(this);
    private static final String APP_ID_KEY_NAME = "app_id";
    private static final String ACCESS_TOKEN_SIGNING_KEY_NAME = "access_token_signing_key";
    private static final String REFRESH_TOKEN_KEY_NAME = "refresh_token_key";
    public static boolean isTesting = false;
    boolean enabled = true;
    Thread mainThread = Thread.currentThread();
    private Thread shutdownHook;

    public ResourceDistributor getResourceDistributor() {
        return resourceDistributor;
    }

    public String getProcessId() {
        return this.processId;
    }

    @Override
    public void constructor(String processId, boolean silent) {
        this.processId = processId;
        Start.silent = silent;
    }

    @Override
    public STORAGE_TYPE getType() {
        return STORAGE_TYPE.SQL;
    }

    @Override
    public void loadConfig(String configFilePath) {
        Config.loadConfig(this, configFilePath);
        ConfigObject.setSqlConfig(this);
    }

    @Override
    public void initFileLogging(String infoLogPath, String errorLogPath) {
        Logging.initFileLogging(this, infoLogPath, errorLogPath);

        /*
         * NOTE: The log this produces is only accurate in production or development.
         *
         * For testing, it may happen that multiple processes are running at the same
         * time which can lead to one of them being the winner and its start instance
         * being attached to logger class. This would yield inaccurate processIds during
         * logging.
         *
         * Finally, during testing, the winner's logger might be removed, in which case
         * nothing will be handling logging and hikari's logs would not be outputed
         * anywhere.
         */
        synchronized (appenderLock) {
            final Logger infoLog = (Logger) LoggerFactory.getLogger("com.zaxxer.hikari");
            if (infoLog.getAppender(HikariLoggingAppender.NAME) == null) {
                infoLog.setAdditive(false);
                infoLog.addAppender(appender);
            }
        }

    }

    @Override
    public void stopLogging() {
        Logging.stopLogging(this);

        synchronized (appenderLock) {
            final Logger infoLog = (Logger) LoggerFactory.getLogger("com.zaxxer.hikari");
            if (infoLog.getAppender(HikariLoggingAppender.NAME) != null) {
                infoLog.detachAppender(HikariLoggingAppender.NAME);
            }
        }
    }

    @Override
    public void initStorage() {
        try {
            // TODO: check if in memory db is required and load accordingly
            HibernateSessionPool.getSessionFactory(this);
            // TODO: initiate via liquibase restore later, currently being handled by hibernate
            // GeneralQueries.createTablesIfNotExists(this);
        } catch (InterruptedException e) {
            throw new QuitProgramFromPluginException(e);
        }
    }

    @Override
    public <T> T startSimpleTransactionHibernate(SimpleTransactionLogicHibernate<T> logic)
            throws StorageQueryException {
        Session session = null;
        Transaction transaction = null;

        try {

            session = HibernateSessionPool.getSessionFactory(this).openSession();
            transaction = session.beginTransaction();
            T t = logic.mainLogic(new SessionObject(session));
            transaction.commit();
            return t;

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw new StorageQueryException(e);
        } finally {

            if (session != null) {
                session.close();
            }

        }
    }

    @Override
    public <T> T startTransactionHibernate(TransactionLogicHibernate<T> logic)
            throws PersistenceException, StorageQueryException, StorageTransactionLogicException {
        int tries = 0;
        while (true) {
            tries++;
            try {
                return startTransactionHelper(logic);
                // TODO: fix this exception handling for hibernate related transactions
            } catch (Exception e) {
                // check according to: https://github.com/supertokens/supertokens-mysql-plugin/pull/2
                if ((e.getCause() instanceof SQLTransactionRollbackException
                        || e.getMessage().toLowerCase().contains("deadlock") || e instanceof OptimisticLockException
                        || e.getCause() instanceof OptimisticLockException) && tries < 3) {
                    try {
                        Thread.sleep((long) (10 + (Math.random() * 20)));
                    } catch (InterruptedException ignored) {
                    }
                    ProcessState.getInstance(this).addState(ProcessState.PROCESS_STATE.DEADLOCK_FOUND, e);
                    continue; // this because deadlocks are not necessarily a result of faulty logic. They can happen
                }
                if (e instanceof StorageTransactionLogicException) {
                    throw (StorageTransactionLogicException) e;
                } else if (e instanceof PersistenceException) {
                    throw (PersistenceException) e;
                } else if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new StorageQueryException(e);
            }
        }
    }

    private <T> T startTransactionHelper(TransactionLogicHibernate<T> logic) throws Exception {

        Session session = null;
        Transaction transaction = null;
        final Integer[] defaultTransactionIsolation = { null };

        try {
            session = HibernateSessionPool.getSessionFactory(this).openSession();
            session.doWork(new Work() {
                @Override
                public void execute(Connection connection) throws SQLException {
                    defaultTransactionIsolation[0] = connection.getTransactionIsolation();
                    connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                }
            });

            transaction = session.beginTransaction();
            T t = logic.mainLogicAndCommit(new SessionObject(session));
            return t;

        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {

                transaction.rollback();

            }
            throw e;

        } finally {

            if (transaction != null && transaction.isActive()) {
                transaction.commit();
            }

            session.doWork(new Work() {
                @Override
                public void execute(Connection connection) throws SQLException {
                    connection.setTransactionIsolation(defaultTransactionIsolation[0]);
                }
            });

            if (session != null)
                session.close();
        }
    }

    @Override
    public void commitTransaction(SessionObject sessionInstance) throws Exception {
        Transaction transaction = null;
        try {
            Session session = (Session) sessionInstance.getSession();
            transaction = (Transaction) session.getTransaction();
            if (transaction.isActive()) {
                transaction.commit();
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }

    }

    @Override
    public KeyValueInfo getLegacyAccessTokenSigningKey_Transaction(SessionObject sessionInstance)
            throws StorageQueryException {
        try {
            return GeneralQueries.getKeyValue_Transaction(this, sessionInstance, ACCESS_TOKEN_SIGNING_KEY_NAME);
        } catch (Exception e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void removeLegacyAccessTokenSigningKey_Transaction(SessionObject sessionInstance)
            throws StorageQueryException {
        try {
            GeneralQueries.deleteKeyValue_Transaction(this, sessionInstance, ACCESS_TOKEN_SIGNING_KEY_NAME);
        } catch (PersistenceException | InterruptedException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public KeyValueInfo[] getAccessTokenSigningKeys_Transaction(SessionObject sessionInstance)
            throws StorageQueryException {
        try {
            return SessionQueries.getAccessTokenSigningKeys_Transaction(this, sessionInstance);
        } catch (PersistenceException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void addAccessTokenSigningKey_Transaction(SessionObject sessionInstance, KeyValueInfo info)
            throws StorageQueryException {
        try {
            SessionQueries.addAccessTokenSigningKey_Transaction(this, sessionInstance, info.createdAtTime, info.value);
        } catch (PersistenceException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void removeAccessTokenSigningKeysBefore(long time) throws StorageQueryException {
        startSimpleTransactionHibernate(session -> {
            SessionQueries.removeAccessTokenSigningKeysBefore(this, session, time);
            return null;
        });
    }

    @Override
    public KeyValueInfo getRefreshTokenSigningKey_Transaction(SessionObject sessionInstance)
            throws StorageQueryException {
        try {
            return GeneralQueries.getKeyValue_Transaction(this, sessionInstance, REFRESH_TOKEN_KEY_NAME);
        } catch (PersistenceException | InterruptedException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void setRefreshTokenSigningKey_Transaction(SessionObject sessionInstance, KeyValueInfo info)
            throws StorageQueryException {
        try {
            GeneralQueries.setKeyValue_Transaction(this, sessionInstance, REFRESH_TOKEN_KEY_NAME, info);
        } catch (PersistenceException | InterruptedException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    @TestOnly
    public void deleteAllInformation() throws StorageQueryException {
        try {
            GeneralQueries.deleteAllTables(this);
        } catch (PersistenceException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void close() {
        HibernateSessionPool.shutdown();
    }

    @Override
    public void createNewSession(String sessionHandle, String userId, String refreshTokenHash2,
            JsonObject userDataInDatabase, long expiry, JsonObject userDataInJWT, long createdAtTime)
            throws StorageQueryException {

        startSimpleTransactionHibernate(session -> {
            SessionQueries.createNewSession(this, session, sessionHandle, userId, refreshTokenHash2, userDataInDatabase,
                    expiry, userDataInJWT, createdAtTime);
            return null;
        });

    }

    @Override
    public void deleteSessionsOfUser(String userId) throws StorageQueryException {

        startSimpleTransactionHibernate(session -> {
            SessionQueries.deleteSessionsOfUser(this, session, userId);
            return null;
        });

    }

    @Override
    public int getNumberOfSessions() throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return SessionQueries.getNumberOfSessions(this, session);
        });
    }

    @Override
    public int deleteSession(String[] sessionHandles) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return SessionQueries.deleteSession(this, session, sessionHandles);
        });
    }

    @Override
    public String[] getAllSessionHandlesForUser(String userId) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return SessionQueries.getAllSessionHandlesForUser(this, session, userId);
        });
    }

    @Override
    public void deleteAllExpiredSessions() throws StorageQueryException {
        startSimpleTransactionHibernate(session -> {
            SessionQueries.deleteAllExpiredSessions(this, session);
            return null;
        });
    }

    @Override
    public KeyValueInfo getKeyValue(String key) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return GeneralQueries.getKeyValue(this, session, key);
        });
    }

    @Override
    public void setKeyValue(String key, KeyValueInfo info) throws StorageQueryException {
        startSimpleTransactionHibernate(session -> {
            GeneralQueries.setKeyValue(this, session, key, info);
            return null;
        });
    }

    @Override
    public void setStorageLayerEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public SessionInfo getSession(String sessionHandle) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return SessionQueries.getSession(this, session, sessionHandle);
        });
    }

    @Override
    public int updateSession(String sessionHandle, @Nullable JsonObject sessionData, @Nullable JsonObject jwtPayload)
            throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return SessionQueries.updateSession(this, session, sessionHandle, sessionData, jwtPayload);
        });
    }

    @Override
    public SessionInfo getSessionInfo_Transaction(SessionObject sessionInstance, String sessionHandle)
            throws StorageQueryException {
        try {
            return SessionQueries.getSessionInfo_Transaction(this, sessionInstance, sessionHandle);
        } catch (NoResultException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void updateSessionInfo_Transaction(SessionObject sessionInstance, String sessionHandle,
            String refreshTokenHash2, long expiry) throws StorageQueryException {
        try {
            SessionQueries.updateSessionInfo_Transaction(this, sessionInstance, sessionHandle, refreshTokenHash2,
                    expiry);
        } catch (SQLException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void setKeyValue_Transaction(SessionObject sessionInstance, String key, KeyValueInfo info)
            throws StorageQueryException {
        try {
            GeneralQueries.setKeyValue_Transaction(this, sessionInstance, key, info);
        } catch (PersistenceException | InterruptedException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public KeyValueInfo getKeyValue_Transaction(SessionObject sessionInstance, String key)
            throws StorageQueryException {
        try {
            return GeneralQueries.getKeyValue_Transaction(this, sessionInstance, key);
        } catch (PersistenceException | InterruptedException e) {
            throw new StorageQueryException(e);
        }
    }

//    @Override
//    public <T> T startTransaction(TransactionLogic<T> logic)
//            throws StorageQueryException, StorageTransactionLogicException {
//        return null;
//    }

    void removeShutdownHook() {
        if (shutdownHook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
                shutdownHook = null;
            } catch (IllegalStateException ignored) {
            }
        }
    }

    void handleKillSignalForWhenItHappens() {
        if (shutdownHook != null) {
            return;
        }
        shutdownHook = new Thread(() -> {
            HibernateSessionPool.shutdown();
            mainThread.interrupt();
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    @Override
    public boolean canBeUsed(String configFilePath) {
        return Config.canBeUsed(configFilePath);
    }

    @Override
    public void signUp(UserInfo userInfo)
            throws StorageQueryException, DuplicateUserIdException, DuplicateEmailException {

        Session session = null;
        Transaction transaction = null;
        try {

            EmailPasswordQueries.signUp(this, userInfo.id, userInfo.email, userInfo.passwordHash, userInfo.timeJoined);

        } catch (PersistenceException | StorageTransactionLogicException | StorageQueryException e) { // TODO: add
                                                                                                      // exception type

            if (transaction != null) {
                transaction.rollback();
            }

            String message = null;
            String constraint_name = null;
            if (e instanceof StorageQueryException) {
                PersistenceException persistenceException = (PersistenceException) ((StorageQueryException) e)
                        .getCause();
                message = ((ConstraintViolationException) persistenceException.getCause()).getSQLException()
                        .getMessage();
            } else if (e instanceof PersistenceException) {
                message = ((ConstraintViolationException) ((PersistenceException) e).getCause()).getSQLException()
                        .getMessage();
                constraint_name = ((ConstraintViolationException) ((PersistenceException) e).getCause())
                        .getConstraintName();
            } else {
                message = e.getMessage();
            }

            // match directly with constraints
            if (ConstraintNameConstants.EmailPasswordUsersDO_email_constraint.equalsIgnoreCase(constraint_name)) {
                throw new DuplicateEmailException();
            }
            message = message.replaceAll("\n", "");
            if (message.matches(DUPLICATE_ENTRY) && message.contains(userInfo.id)) {

                throw new DuplicateUserIdException();

            }
            throw new StorageQueryException(e);
//
//
//            if (message.matches(DUPLICATE_ENTRY)
//                    && (message.endsWith("'" + Config.getConfig(this).getEmailPasswordUsersTable() + ".email'")
//                            || message.endsWith("'email'"))) {
//            } else if (message.contains("Duplicate entry")
//                    && (message.endsWith("'" + Config.getConfig(this).getEmailPasswordUsersTable() + ".PRIMARY'")
//                            || message.endsWith("'" + Config.getConfig(this).getUsersTable() + ".PRIMARY'")
//                            || message.endsWith("'PRIMARY'"))) {
//            }

        } finally {

            if (session != null) {
                session.close();
            }

        }
    }

    @Override
    public void deleteEmailPasswordUser(String userId) throws StorageQueryException {
        try {
            EmailPasswordQueries.deleteUser(this, userId);
        } catch (StorageTransactionLogicException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public UserInfo getUserInfoUsingId(String id) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return EmailPasswordQueries.getUserInfoUsingId(this, session, id);
        });
    }

    @Override
    public UserInfo getUserInfoUsingEmail(String email) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return EmailPasswordQueries.getUserInfoUsingEmail(this, session, email);
        });
    }

    @Override
    public void addPasswordResetToken(PasswordResetTokenInfo passwordResetTokenInfo)
            throws StorageQueryException, UnknownUserIdException, DuplicatePasswordResetTokenException {

        Session session = null;
        Transaction transaction = null;

        try {
            session = HibernateSessionPool.getSessionFactory(this).openSession();
            SessionObject sessionObject = new SessionObject(session);

            transaction = session.beginTransaction();
            EmailPasswordQueries.addPasswordResetToken(this, sessionObject, passwordResetTokenInfo.userId,
                    passwordResetTokenInfo.token, passwordResetTokenInfo.tokenExpiry);
            transaction.commit();
        } catch (SQLException | InterruptedException | PersistenceException e) {

            if (transaction != null) {
                transaction.rollback();
            }

            String message = null;

            if (e instanceof PersistenceException) {
                message = ((ConstraintViolationException) ((PersistenceException) e).getCause()).getSQLException()
                        .getMessage();
            } else {
                message = e.getMessage();
            }
            message = message.replaceAll("\n", "");

            if (message.matches(DUPLICATE_ENTRY) && (message.contains(passwordResetTokenInfo.token))
                    && (message.contains(passwordResetTokenInfo.userId))) {
                throw new DuplicatePasswordResetTokenException();
            } else if (message.matches(FOREIGN_KEY) && message.contains("user_id")) {
                throw new UnknownUserIdException();
            }
            throw new StorageQueryException(e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public PasswordResetTokenInfo getPasswordResetTokenInfo(String token) throws StorageQueryException {

        return startSimpleTransactionHibernate(session -> {
            return EmailPasswordQueries.getPasswordResetTokenInfo(this, session, token);
        });
    }

    @Override
    public PasswordResetTokenInfo[] getAllPasswordResetTokenInfoForUser(String userId) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return EmailPasswordQueries.getAllPasswordResetTokenInfoForUser(this, session, userId);
        });
    }

    @Override
    public PasswordResetTokenInfo[] getAllPasswordResetTokenInfoForUser_Transaction(SessionObject sessionInstance,
            String userId) throws StorageQueryException {
        try {
            return EmailPasswordQueries.getAllPasswordResetTokenInfoForUser_Transaction(this, sessionInstance, userId);
        } catch (SQLException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void deleteAllPasswordResetTokensForUser_Transaction(SessionObject sessionInstance, String userId)
            throws StorageQueryException {
        try {
            EmailPasswordQueries.deleteAllPasswordResetTokensForUser_Transaction(this, sessionInstance, userId);
        } catch (SQLException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void updateUsersPassword_Transaction(SessionObject sessionInstance, String userId, String newPassword)
            throws StorageQueryException {
        try {
            EmailPasswordQueries.updateUsersPassword_Transaction(this, sessionInstance, userId, newPassword);
        } catch (SQLException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void updateUsersEmail_Transaction(SessionObject sessionTransactionn, String userId, String email)
            throws StorageQueryException, DuplicateEmailException {
        try {
            EmailPasswordQueries.updateUsersEmail_Transaction(this, sessionTransactionn, userId, email);
        } catch (PersistenceException | SQLException | UnknownUserIdException e) {
            String message = null;
            if (e instanceof PersistenceException) {
                message = ((ConstraintViolationException) ((PersistenceException) e).getCause()).getSQLException()
                        .getMessage();
            } else {
                message = e.getMessage();
            }
            message = message.replaceAll("\n", "");

            if (message.matches(DUPLICATE_ENTRY)
                    && (message.contains(Config.getConfig(this).getEmailPasswordUsersTable())
                            && message.contains(email))) {
                throw new DuplicateEmailException();
            }
            throw new StorageQueryException(e);
        }
    }

    @Override
    public UserInfo getUserInfoUsingId_Transaction(SessionObject sessionInstance, String userId)
            throws StorageQueryException {
        try {
            return EmailPasswordQueries.getUserInfoUsingId_Transaction(this, sessionInstance, userId);
        } catch (SQLException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    @Deprecated
    public UserInfo[] getUsers(@Nonnull String userId, @Nonnull Long timeJoined, @Nonnull Integer limit,
            @Nonnull String timeJoinedOrder) throws StorageQueryException {

        return startSimpleTransactionHibernate(session -> {
            return EmailPasswordQueries.getUsersInfo(this, session, userId, timeJoined, limit, timeJoinedOrder);
        });
    }

    @Override
    @Deprecated
    public UserInfo[] getUsers(@Nonnull Integer limit, @Nonnull String timeJoinedOrder) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return EmailPasswordQueries.getUsersInfo(this, session, limit, timeJoinedOrder);
        });
    }

    @Override
    @Deprecated
    public long getUsersCount() throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return EmailPasswordQueries.getUsersCount(this, session);
        });
    }

    @Override
    public void deleteExpiredPasswordResetTokens() throws StorageQueryException {
        startSimpleTransactionHibernate(session -> {
            EmailPasswordQueries.deleteExpiredPasswordResetTokens(this, session);
            return null;
        });
    }

    @Override
    public void deleteExpiredEmailVerificationTokens() throws StorageQueryException {
        startSimpleTransactionHibernate(session -> {
            EmailVerificationQueries.deleteExpiredEmailVerificationTokens(this, session);
            return null;
        });
    }

    @Override
    public EmailVerificationTokenInfo[] getAllEmailVerificationTokenInfoForUser_Transaction(
            SessionObject sessionInstance, String userId, String email) throws StorageQueryException {
        try {
            return EmailVerificationQueries.getAllEmailVerificationTokenInfoForUser_Transaction(this, sessionInstance,
                    userId, email);
        } catch (SQLException | NoResultException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void deleteAllEmailVerificationTokensForUser_Transaction(SessionObject sessionInstance, String userId,
            String email) throws StorageQueryException {
        try {
            EmailVerificationQueries.deleteAllEmailVerificationTokensForUser_Transaction(this, sessionInstance, userId,
                    email);
        } catch (SQLException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void updateIsEmailVerified_Transaction(SessionObject sessionInstance, String userId, String email,
            boolean isEmailVerified) throws StorageQueryException {
        try {
            EmailVerificationQueries.updateUsersIsEmailVerified_Transaction(this, sessionInstance, userId, email,
                    isEmailVerified);
        } catch (SQLException e) {
            if (!isEmailVerified || !(e.getMessage().matches(DUPLICATE_ENTRY)
                    && (e.getMessage().endsWith("'" + Config.getConfig(this).getEmailVerificationTable() + ".PRIMARY'")
                            || e.getMessage().endsWith("'PRIMARY'")))) {
                throw new StorageQueryException(e);
            }
            // we do not throw an error since the email is already verified
        }
    }

    @Override
    public void addEmailVerificationToken(EmailVerificationTokenInfo emailVerificationInfo)
            throws StorageQueryException, DuplicateEmailVerificationTokenException {
        startSimpleTransactionHibernate(session -> {
            EmailVerificationQueries.deleteExpiredEmailVerificationTokens(this, session);
            return null;
        });

        Session session = null;
        Transaction transaction = null;

        try {

            session = HibernateSessionPool.getSessionFactory(this).openSession();
            SessionObject sessionObject = new SessionObject(session);
            transaction = session.beginTransaction();

            EmailVerificationQueries.addEmailVerificationToken(this, sessionObject, emailVerificationInfo.userId,
                    emailVerificationInfo.token, emailVerificationInfo.tokenExpiry, emailVerificationInfo.email);
            transaction.commit();

        } catch (SQLException | InterruptedException | PersistenceException e) {

            if (transaction != null) {
                transaction.rollback();
            }

            String message = null;

            if (e instanceof PersistenceException) {
                message = ((PersistenceException) e).getCause().getCause().getMessage();
            } else {
                message = e.getMessage();
            }
            message = message.replaceAll("\n", "");

            if (message.matches(DUPLICATE_ENTRY)
                    && (message.endsWith("'" + Config.getConfig(this).getEmailVerificationTokensTable() + ".PRIMARY'")
                            || message.endsWith("'PRIMARY'"))) {
                throw new DuplicateEmailVerificationTokenException();
            }
            throw new StorageQueryException(e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public EmailVerificationTokenInfo getEmailVerificationTokenInfo(String token) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return EmailVerificationQueries.getEmailVerificationTokenInfo(this, session, token);
        });
    }

    @Override
    public void deleteEmailVerificationUserInfo(String userId) throws StorageQueryException {
        try {
            EmailVerificationQueries.deleteUserInfo(this, userId);
        } catch (StorageTransactionLogicException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void revokeAllTokens(String userId, String email) throws StorageQueryException {
        startSimpleTransactionHibernate(session -> {
            EmailVerificationQueries.revokeAllTokens(this, session, userId, email);
            return null;
        });
    }

    @Override
    public void unverifyEmail(String userId, String email) throws StorageQueryException {
        startSimpleTransactionHibernate(session -> {
            EmailVerificationQueries.unverifyEmail(this, session, userId, email);
            return null;
        });
    }

    @Override
    public EmailVerificationTokenInfo[] getAllEmailVerificationTokenInfoForUser(String userId, String email)
            throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return EmailVerificationQueries.getAllEmailVerificationTokenInfoForUser(this, session, userId, email);
        });
    }

    @Override
    public boolean isEmailVerified(String userId, String email) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return EmailVerificationQueries.isEmailVerified(this, session, userId, email);
        });
    }

    @Override
    public io.supertokens.pluginInterface.thirdparty.UserInfo getUserInfoUsingId_Transaction(
            SessionObject sessionInstance, String thirdPartyId, String thirdPartyUserId) throws StorageQueryException {
        try {
            return ThirdPartyQueries.getUserInfoUsingId_Transaction(this, sessionInstance, thirdPartyId,
                    thirdPartyUserId);
        } catch (SQLException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void updateUserEmail_Transaction(SessionObject sessionInstance, String thirdPartyId, String thirdPartyUserId,
            String newEmail) throws StorageQueryException {
        try {
            ThirdPartyQueries.updateUserEmail_Transaction(this, sessionInstance, thirdPartyId, thirdPartyUserId,
                    newEmail);
        } catch (SQLException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void signUp(io.supertokens.pluginInterface.thirdparty.UserInfo userInfo)
            throws StorageQueryException, io.supertokens.pluginInterface.thirdparty.exception.DuplicateUserIdException,
            DuplicateThirdPartyUserException {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateSessionPool.getSessionFactory(this).openSession();
            transaction = session.beginTransaction();
            ThirdPartyQueries.signUp(this, userInfo);
            transaction.commit();
        } catch (PersistenceException | StorageTransactionLogicException | InterruptedException eTemp) {

            if (transaction != null) {
                transaction.rollback();
            }

            String message = null;
            Exception e = null;

            if (eTemp instanceof StorageTransactionLogicException) {
                e = ((StorageTransactionLogicException) eTemp).actualException;
                message = e.getMessage();
            } else if (eTemp instanceof PersistenceException) {
                message = eTemp.getCause().getCause().getMessage();
            } else if (eTemp instanceof InterruptedException) {
                throw new StorageQueryException(e);
            }

            message = message.replaceAll("\n", "");

            if (message.matches(DUPLICATE_ENTRY)) {

                if (message.contains(userInfo.thirdParty.userId) && message.contains(userInfo.thirdParty.id)
                        && message.contains(Config.getConfig(this).getThirdPartyUsersTable())) {

                    throw new DuplicateThirdPartyUserException();

                } else if (message.contains(userInfo.id) && message.contains(Config.getConfig(this).getUsersTable())) {
                    // TODO: fix this
                    throw new io.supertokens.pluginInterface.thirdparty.exception.DuplicateUserIdException();
                } else if (message.contains(Config.getConfig(this).getThirdPartyUsersTable())
                        && message.contains(userInfo.id)) {
                    throw new io.supertokens.pluginInterface.thirdparty.exception.DuplicateUserIdException();
                }

            }

//            else if (
//                    && ((message.endsWith("'" + Config.getConfig(this).getThirdPartyUsersTable() + ".user_id'")
//                            || message.endsWith("'user_id'"))
//                            || (message.endsWith("'" + Config.getConfig(this).getUsersTable() + ".PRIMARY'")
//                                    || message.endsWith("'PRIMARY'")))) {
//                throw new io.supertokens.pluginInterface.thirdparty.exception.DuplicateUserIdException();
//            }
            if (e != null) {
                throw new StorageQueryException(e);
            } else {
                throw new StorageQueryException(eTemp);
            }
        } finally {

            if (session != null) {
                session.close();
            }

        }
    }

    @Override
    public void deleteThirdPartyUser(String userId) throws StorageQueryException {
        try {
            ThirdPartyQueries.deleteUser(this, userId);
        } catch (StorageTransactionLogicException e) {
            throw new StorageQueryException(e.actualException);
        }
    }

    @Override
    public io.supertokens.pluginInterface.thirdparty.UserInfo getThirdPartyUserInfoUsingId(String thirdPartyId,
            String thirdPartyUserId) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return ThirdPartyQueries.getThirdPartyUserInfoUsingId(this, session, thirdPartyId, thirdPartyUserId);
        });
    }

    @Override
    public io.supertokens.pluginInterface.thirdparty.UserInfo getThirdPartyUserInfoUsingId(String id)
            throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return ThirdPartyQueries.getThirdPartyUserInfoUsingId(this, session, id);
        });
    }

    @Override
    @Deprecated
    public io.supertokens.pluginInterface.thirdparty.UserInfo[] getThirdPartyUsers(@NotNull String userId,
            @NotNull Long timeJoined, @NotNull Integer limit, @NotNull String timeJoinedOrder)
            throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return ThirdPartyQueries.getThirdPartyUsers(this, session, userId, timeJoined, limit, timeJoinedOrder);
        });
    }

    @Override
    @Deprecated
    public io.supertokens.pluginInterface.thirdparty.UserInfo[] getThirdPartyUsers(@NotNull Integer limit,
            @NotNull String timeJoinedOrder) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return ThirdPartyQueries.getThirdPartyUsers(this, session, limit, timeJoinedOrder);
        });
    }

    @Override
    @Deprecated
    public long getThirdPartyUsersCount() throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return ThirdPartyQueries.getUsersCount(this, session);
        });
    }

    @Override
    public io.supertokens.pluginInterface.thirdparty.UserInfo[] getThirdPartyUsersByEmail(@NotNull String email)
            throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return ThirdPartyQueries.getThirdPartyUsersByEmail(this, session, email);
        });
    }

    @Override
    public long getUsersCount(RECIPE_ID[] includeRecipeIds) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return GeneralQueries.getUsersCount(this, session, includeRecipeIds);
        });
    }

    @Override
    public AuthRecipeUserInfo[] getUsers(@NotNull Integer limit, @NotNull String timeJoinedOrder,
            @Nullable RECIPE_ID[] includeRecipeIds, @Nullable String userId, @Nullable Long timeJoined)
            throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return GeneralQueries.getUsers(this, session, limit, timeJoinedOrder, includeRecipeIds, userId, timeJoined);
        });
    }

    @Override
    public List<JWTSigningKeyInfo> getJWTSigningKeys_Transaction(SessionObject sessionInstance)
            throws StorageQueryException {
        try {
            return JWTSigningQueries.getJWTSigningKeys_Transaction(this, sessionInstance);
        } catch (SQLException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void setJWTSigningKey_Transaction(SessionObject sessionInstance, JWTSigningKeyInfo info)
            throws StorageQueryException, DuplicateKeyIdException {
        try {
            JWTSigningQueries.setJWTSigningKeyInfo_Transaction(this, sessionInstance, info);
            // ((Session) sessionInstance.getSession()).flush();
        } catch (PersistenceException | SQLException e) {

            String message = null;

            if (e instanceof PersistenceException) {
                message = ((PersistenceException) e).getCause().getCause().getMessage();
            }
            message = message.replaceAll("\n", "");

            if (message.matches(DUPLICATE_ENTRY) && message.contains(info.keyId)
                    && message.contains(Config.getConfig(this).getJWTSigningKeysTable())) {
                throw new DuplicateKeyIdException();
            }

            throw new StorageQueryException(e);
        }
    }

    /**
     * Passwordless impl begin here
     */

    @Override
    public PasswordlessDevice getDevice(String deviceIdHash) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return PasswordlessQueries.getDevice(this, session, deviceIdHash);
        });
    }

    @Override
    public PasswordlessDevice[] getDevicesByEmail(String email) throws StorageQueryException {
        SessionObject sessionObject = null;

        try {
            sessionObject = new SessionObject(HibernateSessionPool.getSessionFactory(this).openSession());
            return PasswordlessQueries.getDevicesByEmail(this, sessionObject, email);

        } catch (InterruptedException | SQLException e) {
            return null;
        } finally {
            if (sessionObject != null) {
                ((Session) sessionObject.getSession()).close();
            }
        }
    }

    @Override
    public PasswordlessDevice[] getDevicesByPhoneNumber(String phoneNumber) throws StorageQueryException {

        SessionObject sessionObject = null;

        try {
            sessionObject = new SessionObject(HibernateSessionPool.getSessionFactory(this).openSession());
            return PasswordlessQueries.getDevicesByPhoneNumber(this, sessionObject, phoneNumber);

        } catch (InterruptedException | SQLException e) {
            return null;
        } finally {
            if (sessionObject != null) {
                ((Session) sessionObject.getSession()).close();
            }
        }
    }

    @Override
    public PasswordlessCode[] getCodesOfDevice(String deviceIdHash) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return PasswordlessQueries.getCodesOfDevice(this, session, deviceIdHash);
        });
    }

    @Override
    public PasswordlessCode[] getCodesBefore(long time) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return PasswordlessQueries.getCodesBefore(this, session, time);
        });
    }

    @Override
    public PasswordlessCode getCode(String codeId) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return PasswordlessQueries.getCode(this, session, codeId);
        });
    }

    @Override
    public PasswordlessCode getCodeByLinkCodeHash(String linkCodeHash) throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return PasswordlessQueries.getCodeByLinkCodeHash(this, session, linkCodeHash);
        });
    }

    @Override
    public io.supertokens.pluginInterface.passwordless.UserInfo getUserById(String userId)
            throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return PasswordlessQueries.getUserById(this, session, userId);
        });
    }

    @Override
    public io.supertokens.pluginInterface.passwordless.UserInfo getUserByEmail(String email)
            throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return PasswordlessQueries.getUserByEmail(this, session, email);
        });
    }

    @Override
    public io.supertokens.pluginInterface.passwordless.UserInfo getUserByPhoneNumber(String phoneNumber)
            throws StorageQueryException {
        return startSimpleTransactionHibernate(session -> {
            return PasswordlessQueries.getUserByPhoneNumber(this, session, phoneNumber);
        });
    }

    @Override
    public void createDeviceWithCode(@Nullable String email, @Nullable String phoneNumber, @NotNull String linkCodeSalt,
            PasswordlessCode code) throws StorageQueryException, DuplicateDeviceIdHashException,
            DuplicateCodeIdException, DuplicateLinkCodeHashException {
        if (email == null && phoneNumber == null) {
            throw new IllegalArgumentException("Both email and phoneNumber can't be null");
        }
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateSessionPool.getSessionFactory(this).openSession();
            SessionObject sessionObject = new SessionObject(session);
            transaction = session.beginTransaction();
            PasswordlessQueries.createDeviceWithCode(this, sessionObject, email, phoneNumber, linkCodeSalt, code);
            transaction.commit();
        } catch (PersistenceException | StorageTransactionLogicException | InterruptedException
                | UnknownDeviceIdHash e) {

            if (transaction != null) {
                transaction.rollback();
            }

            String message = null;

            if (e instanceof PersistenceException) {
                message = e.getCause().getCause().getMessage();
            } else {
                message = e.getMessage();
            }
            message = message.replaceAll("\n", "");

            if (message.matches(DUPLICATE_ENTRY)) {
                if (message.contains(Config.getConfig(this).getPasswordlessDevicesTable())
                        && message.contains(code.deviceIdHash)) {
                    throw new DuplicateDeviceIdHashException();
                }
                if (message.contains(Config.getConfig(this).getPasswordlessCodesTable()) && message.contains(code.id)) {
                    throw new DuplicateCodeIdException();
                }

                if (message.contains(Config.getConfig(this).getPasswordlessCodesTable())
                        && message.contains(code.linkCodeHash)) {
                    throw new DuplicateLinkCodeHashException();
                }
            }

            throw new StorageQueryException(e);
        } finally {

            if (session != null) {
                session.close();
            }

        }
    }

    @Override
    public PasswordlessDevice getDevice_Transaction(SessionObject sessionInstance, String deviceIdHash)
            throws StorageQueryException {
        try {
            return PasswordlessQueries.getDevice_Transaction(this, sessionInstance, deviceIdHash);
        } catch (SQLException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void incrementDeviceFailedAttemptCount_Transaction(SessionObject sessionInstance, String deviceIdHash)
            throws StorageQueryException {
        try {
            PasswordlessQueries.incrementDeviceFailedAttemptCount_Transaction(this, sessionInstance, deviceIdHash);
        } catch (SQLException e) {
            throw new StorageQueryException(e);
        }

    }

    @Override
    public PasswordlessCode[] getCodesOfDevice_Transaction(SessionObject sessionInstance, String deviceIdHash)
            throws StorageQueryException {
        try {
            return PasswordlessQueries.getCodesOfDevice_Transaction(this, sessionInstance, deviceIdHash);
        } catch (SQLException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void deleteDevice_Transaction(SessionObject sessionInstance, String deviceIdHash)
            throws StorageQueryException {
        try {
            PasswordlessQueries.deleteDevice_Transaction(this, sessionInstance, deviceIdHash);
        } catch (SQLException e) {
            throw new StorageQueryException(e);
        }

    }

    @Override
    public void deleteDevicesByPhoneNumber_Transaction(SessionObject sessionInstance, @Nonnull String phoneNumber)
            throws StorageQueryException {
        try {
            PasswordlessQueries.deleteDevicesByPhoneNumber_Transaction(this, sessionInstance, phoneNumber);
        } catch (SQLException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void deleteDevicesByEmail_Transaction(SessionObject sessionInstance, @Nonnull String email)
            throws StorageQueryException {
        try {
            PasswordlessQueries.deleteDevicesByEmail_Transaction(this, sessionInstance, email);
        } catch (SQLException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void createCode(PasswordlessCode code) throws StorageQueryException, UnknownDeviceIdHash,
            DuplicateCodeIdException, DuplicateLinkCodeHashException {

        Session session = null;
        Transaction transaction = null;

        try {

            session = HibernateSessionPool.getSessionFactory(this).openSession();
            transaction = session.beginTransaction();

            PasswordlessQueries.createCode(this, new SessionObject(session), code);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            if (e instanceof UnknownDeviceIdHash) {

                throw new UnknownDeviceIdHash();

            } else if (e instanceof NonUniqueObjectException) {
                String message = e.getMessage();

                if (message.contains(PasswordlessCodesDO.class.getName())) {
                    if (message.contains(code.id)) {
                        throw new DuplicateCodeIdException();
                    } else if (message.contains(code.linkCodeHash)) {
                        throw new DuplicateLinkCodeHashException();
                    }
                }

            } else if (e instanceof PersistenceException) {

                String message = e.getCause().getCause().getMessage();
                message = message.replaceAll("\n", "");

                if (message.matches(DUPLICATE_ENTRY)
                        && message.contains(Config.getConfig(this).getPasswordlessCodesTable())) {

                    if (message.contains(code.id)) {
                        throw new DuplicateCodeIdException();
                    }

                    if (message.contains(code.linkCodeHash)) {
                        throw new DuplicateLinkCodeHashException();
                    }

                }
            }
            throw new StorageQueryException(e);

        } finally {

            if (session != null) {
                session.close();
            }

        }
    }

    @Override
    public PasswordlessCode getCodeByLinkCodeHash_Transaction(SessionObject sessionInstance, String linkCodeHash)
            throws StorageQueryException {
        try {
            return PasswordlessQueries.getCodeByLinkCodeHash_Transaction(this, sessionInstance, linkCodeHash);
        } catch (SQLException | PersistenceException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void deleteCode_Transaction(SessionObject sessionInstance, String deviceIdHash)
            throws StorageQueryException {
        try {
            PasswordlessQueries.deleteCode_Transaction(this, sessionInstance, deviceIdHash);
        } catch (SQLException e) {
            throw new StorageQueryException(e);
        }
    }

    @Override
    public void createUser(io.supertokens.pluginInterface.passwordless.UserInfo user) throws StorageQueryException,
            DuplicateEmailException, DuplicatePhoneNumberException, DuplicateUserIdException {

        try {
            PasswordlessQueries.createUser(this, user);
        } catch (Exception e) {

            String message = null;
            if (e instanceof StorageQueryException) {

                message = ((ConstraintViolationException) e.getCause().getCause()).getSQLException().getMessage();

            } else if (e instanceof StorageTransactionLogicException) {

                Exception actualException = ((StorageTransactionLogicException) e).actualException;
                message = actualException.getMessage();
            } else if (e instanceof PersistenceException) {
                message = ((ConstraintViolationException) e.getCause()).getSQLException().getMessage();
            }
            message = message.replaceAll("\n", "");

            if (message.matches(DUPLICATE_ENTRY)) {
                if (message.contains(user.id)) {
                    if (message.contains(Config.getConfig(this).getPasswordlessUsersTable())
                            || message.contains(Config.getConfig(this).getUsersTable())) {
                        throw new DuplicateUserIdException();
                    }
                } else if (user.email != null && message.contains(Config.getConfig(this).getPasswordlessUsersTable())
                        && message.contains(user.email)) {
                    throw new DuplicateEmailException();
                } else if (user.phoneNumber != null
                        && message.contains(Config.getConfig(this).getPasswordlessUsersTable())
                        && message.contains(user.phoneNumber)) {
                    throw new DuplicatePhoneNumberException();
                }

            }
            throw new StorageQueryException(e);

        }
    }

    @Override
    public void deletePasswordlessUser(String userId) throws StorageQueryException {
        try {
            PasswordlessQueries.deleteUser(this, userId);
        } catch (StorageTransactionLogicException e) {
            throw new StorageQueryException(e.actualException);
        }
    }

    @Override
    public void updateUserEmail_Transaction(SessionObject sessionInstance, String userId, String email)
            throws StorageQueryException, UnknownUserIdException, DuplicateEmailException {

        Session session = null;
        Transaction transaction = null;
        try {
            session = (Session) sessionInstance.getSession();
            PasswordlessQueries.updateUserEmail_Transaction(this, sessionInstance, userId, email);
        } catch (PersistenceException e) {

            if (e.getCause() != null && e.getCause().getCause() != null) {
                String message = e.getCause().getCause().getMessage();
                message = message.replaceAll("\n", "");

                if (message.matches(DUPLICATE_ENTRY)
                        && message.contains(Config.getConfig(this).getPasswordlessUsersTable())
                        && message.contains(email)) {
                    throw new DuplicateEmailException();
                }
            }
            throw new StorageQueryException(e);

        }
    }

    @Override
    public void updateUserPhoneNumber_Transaction(SessionObject sessionInstance, String userId, String phoneNumber)
            throws StorageQueryException, UnknownUserIdException, DuplicatePhoneNumberException {
        try {

            PasswordlessQueries.updateUserPhoneNumber_Transaction(this, sessionInstance, userId, phoneNumber);

        } catch (PersistenceException e) {

            if (e.getCause() != null && e.getCause().getCause() != null) {
                String message = e.getCause().getCause().getMessage();
                message = message.replaceAll("\n", "");

                if (message.matches(DUPLICATE_ENTRY)
                        && message.contains(Config.getConfig(this).getPasswordlessUsersTable())
                        && message.contains(phoneNumber)) {
                    throw new DuplicatePhoneNumberException();
                }
            }

            throw new StorageQueryException(e);
        }
    }

}
