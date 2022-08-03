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

import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.pluginInterface.exceptions.StorageTransactionLogicException;
import io.supertokens.pluginInterface.passwordless.PasswordlessCode;
import io.supertokens.pluginInterface.passwordless.PasswordlessDevice;
import io.supertokens.pluginInterface.passwordless.UserInfo;
import io.supertokens.pluginInterface.sqlStorage.SQLStorage.TransactionIsolationLevel;
import io.supertokens.storage.sql.ConnectionPool;
import io.supertokens.storage.sql.Start;
import io.supertokens.storage.sql.config.Config;
import io.supertokens.storage.sql.domainobject.general.AllAuthRecipeUsersDO;
import io.supertokens.storage.sql.domainobject.passwordless.PasswordlessCodesDO;
import io.supertokens.storage.sql.domainobject.passwordless.PasswordlessDevicesDO;
import io.supertokens.storage.sql.domainobject.passwordless.PasswordlessUsersDO;
import io.supertokens.storage.sql.exceptions.ForeignKeyConstraintNotMetException;
import io.supertokens.storage.sql.hibernate.CustomQueryWrapper;
import io.supertokens.storage.sql.hibernate.CustomSessionWrapper;
import io.supertokens.storage.sql.utils.Utils;
import org.hibernate.LockMode;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.supertokens.pluginInterface.RECIPE_ID.PASSWORDLESS;

public class PasswordlessQueries {
    public static String getQueryToCreateUsersTable(Start start) {
        String schema = Config.getConfig(start).getTableSchema();
        String usersTable = Config.getConfig(start).getPasswordlessUsersTable();

        return "CREATE TABLE IF NOT EXISTS " + usersTable + " (" + "user_id CHAR(36) NOT NULL,"
                + "email VARCHAR(256) CONSTRAINT " + Utils.getConstraintName(schema, usersTable, "email", "key")
                + " UNIQUE," + "phone_number VARCHAR(256) CONSTRAINT "
                + Utils.getConstraintName(schema, usersTable, "phone_number", "key") + " UNIQUE,"
                + "time_joined BIGINT NOT NULL, " + "CONSTRAINT "
                + Utils.getConstraintName(schema, usersTable, null, "pkey") + " PRIMARY KEY (user_id)" + ");";
    }

    public static String getQueryToCreateDevicesTable(Start start) {
        String schema = Config.getConfig(start).getTableSchema();
        String devicesTable = Config.getConfig(start).getPasswordlessDevicesTable();

        return "CREATE TABLE IF NOT EXISTS " + devicesTable + " (" + "device_id_hash CHAR(44) NOT NULL,"
                + "email VARCHAR(256), " + "phone_number VARCHAR(256)," + "link_code_salt CHAR(44) NOT NULL,"
                + "failed_attempts INT NOT NULL," + "CONSTRAINT "
                + Utils.getConstraintName(schema, devicesTable, null, "pkey") + " PRIMARY KEY (device_id_hash));";
    }

    public static String getQueryToCreateCodesTable(Start start) {
        String schema = Config.getConfig(start).getTableSchema();
        String codesTable = Config.getConfig(start).getPasswordlessCodesTable();

        return "CREATE TABLE IF NOT EXISTS " + codesTable + " (" + "code_id CHAR(36) NOT NULL,"
                + "device_id_hash CHAR(44) NOT NULL," + "link_code_hash CHAR(44) NOT NULL CONSTRAINT "
                + Utils.getConstraintName(schema, codesTable, "link_code_hash", "key") + " UNIQUE,"
                + "created_at BIGINT NOT NULL," + "CONSTRAINT "
                + Utils.getConstraintName(schema, codesTable, null, "pkey") + " PRIMARY KEY (code_id)," + "CONSTRAINT "
                + Utils.getConstraintName(schema, codesTable, "device_id_hash", "fkey")
                + " FOREIGN KEY (device_id_hash) " + "REFERENCES "
                + Config.getConfig(start).getPasswordlessDevicesTable()
                + "(device_id_hash) ON DELETE CASCADE ON UPDATE CASCADE);";
    }

    public static String getQueryToCreateDeviceEmailIndex(Start start) {
        return "CREATE INDEX passwordless_devices_email_index ON "
                + Config.getConfig(start).getPasswordlessDevicesTable() + " (email);"; // USING hash
    }

    public static String getQueryToCreateDevicePhoneNumberIndex(Start start) {
        return "CREATE INDEX passwordless_devices_phone_number_index ON "
                + Config.getConfig(start).getPasswordlessDevicesTable() + " (phone_number);"; // USING hash
    }

    public static String getQueryToCreateCodeDeviceIdHashIndex(Start start) {
        return "CREATE INDEX IF NOT EXISTS passwordless_codes_device_id_hash_index ON "
                + Config.getConfig(start).getPasswordlessCodesTable() + "(device_id_hash);";
    }

    public static String getQueryToCreateCodeCreatedAtIndex(Start start) {
        return "CREATE INDEX passwordless_codes_created_at_index ON "
                + Config.getConfig(start).getPasswordlessCodesTable() + "(created_at);";
    }

    public static void createDeviceWithCode(Start start, String email, String phoneNumber, String linkCodeSalt,
            PasswordlessCode code) throws StorageTransactionLogicException, StorageQueryException, SQLException {

        ConnectionPool.withSessionForComplexTransaction(start, TransactionIsolationLevel.REPEATABLE_READ,
                (session, con) -> {
                    final PasswordlessDevicesDO passwordlessDevicesDO = new PasswordlessDevicesDO(code.deviceIdHash,
                            email, phoneNumber, linkCodeSalt, 0);
                    session.save(PasswordlessDevicesDO.class, code.deviceIdHash, passwordlessDevicesDO);

                    final PasswordlessCodesDO toInsert = new PasswordlessCodesDO(code.id, passwordlessDevicesDO,
                            code.linkCodeHash, code.createdAt);
                    session.save(PasswordlessCodesDO.class, code.id, toInsert);
                    return null;
                });
    }

    public static void incrementDeviceFailedAttemptCount_Transaction(CustomSessionWrapper session, String deviceIdHash)
            throws SQLException {
        final PasswordlessDevicesDO passwordlessDevicesDO = session.get(PasswordlessDevicesDO.class, deviceIdHash);
        if (passwordlessDevicesDO == null) {
            return;
        }
        passwordlessDevicesDO.setFailed_attempts(passwordlessDevicesDO.getFailed_attempts() + 1);

        session.update(PasswordlessDevicesDO.class, deviceIdHash, passwordlessDevicesDO);
    }

    public static PasswordlessDevice getDevice_Transaction(CustomSessionWrapper session, String deviceIdHash)
            throws SQLException {
        final PasswordlessDevicesDO passwordlessDevicesDO = session.get(PasswordlessDevicesDO.class, deviceIdHash,
                LockMode.PESSIMISTIC_WRITE);
        if (passwordlessDevicesDO == null) {
            return null;
        }

        return entityToPasswordlessDevice(passwordlessDevicesDO);
    }

    public static void deleteDevice_Transaction(CustomSessionWrapper session, String deviceIdHash) throws SQLException {
        final PasswordlessDevicesDO passwordlessDevicesDO = session.get(PasswordlessDevicesDO.class, deviceIdHash);
        if (passwordlessDevicesDO == null) {
            return;
        }
        session.delete(PasswordlessDevicesDO.class, deviceIdHash, passwordlessDevicesDO);
    }

    public static void deleteDevicesByPhoneNumber_Transaction(CustomSessionWrapper session, @Nonnull String phoneNumber)
            throws SQLException {
        String QUERY = "DELETE FROM PasswordlessDevicesDO entity WHERE entity.phone_number = :phone_number";

        final CustomQueryWrapper query = session.createQuery(QUERY);
        query.setParameter("phone_number", phoneNumber);

        query.executeUpdate();
    }

    public static void deleteDevicesByEmail_Transaction(CustomSessionWrapper session, @Nonnull String email)
            throws SQLException {
        String QUERY = "DELETE FROM PasswordlessDevicesDO entity WHERE entity.email = :email";

        final CustomQueryWrapper query = session.createQuery(QUERY);
        query.setParameter("email", email);

        query.executeUpdate();
    }

    public static void createCode(Start start, PasswordlessCode code)
            throws StorageTransactionLogicException, StorageQueryException, SQLException {
        ConnectionPool.withSession(start, (session, con) -> {
            /*
             * Here we do a explicit session.get
             * since we do not have a reference to th DB object in the Hibernate session
             * if we do not explicitly call a session.get here,
             * hibernate will make a get call internally while doing the session.save and will throw an exception
             * if the passwordless device does not exist
             */
            final PasswordlessDevicesDO passwordlessDevicesDO = session.get(PasswordlessDevicesDO.class,
                    code.deviceIdHash);
            if (passwordlessDevicesDO == null) {
                throw new PersistenceException(new ForeignKeyConstraintNotMetException());
            }
            final PasswordlessCodesDO toInsert = new PasswordlessCodesDO(code.id, passwordlessDevicesDO,
                    code.linkCodeHash, code.createdAt);
            session.save(PasswordlessCodesDO.class, code.id, toInsert);
            return null;
        }, true);
    }

    public static PasswordlessCode[] getCodesOfDevice_Transaction(CustomSessionWrapper session, String deviceIdHash)
            throws SQLException {
        // We do not lock here, since the device is already locked earlier in the transaction.
        String QUERY = "SELECT entity FROM PasswordlessCodesDO entity WHERE entity.passwordlessDevice.device_id_hash "
                + "= :device_id_hash";
        final CustomQueryWrapper<PasswordlessCodesDO> query = session.createQuery(QUERY, PasswordlessCodesDO.class);

        query.setParameter("device_id_hash", deviceIdHash);

        return query.list().stream().map(PasswordlessQueries::entityToPasswordlessCode)
                .toArray(PasswordlessCode[]::new);
    }

    public static void deleteCode_Transaction(CustomSessionWrapper session, String codeId) throws SQLException {
        String QUERY = "DELETE FROM PasswordlessCodesDO entity WHERE entity.code_id = :code_id";

        final CustomQueryWrapper query = session.createQuery(QUERY);
        query.setParameter("code_id", codeId);

        query.executeUpdate();
    }

    public static void createUser(Start start, UserInfo user) throws SQLException, StorageQueryException {

        ConnectionPool.withSession(start, (session, con) -> {
            {
                final AllAuthRecipeUsersDO usersDO = new AllAuthRecipeUsersDO();
                usersDO.setUser_id(user.id);
                usersDO.setRecipe_id(PASSWORDLESS.toString());
                usersDO.setTime_joined(user.timeJoined);

                session.save(AllAuthRecipeUsersDO.class, user.id, usersDO);
            }

            {
                final PasswordlessUsersDO passwordlessUsersDO = new PasswordlessUsersDO(user.id, user.email,
                        user.phoneNumber, user.timeJoined);

                session.save(PasswordlessUsersDO.class, user.id, passwordlessUsersDO);
            }

            return null;
        }, true);
    }

    public static void deleteUser(Start start, String userId) throws SQLException, StorageQueryException {

        ConnectionPool.withSession(start, (session, con) -> {
            {
                String QUERY = "DELETE FROM AllAuthRecipeUsersDO entity "
                        + "WHERE entity.user_id = :user_id AND entity.recipe_id = :recipe_id";

                final CustomQueryWrapper query = session.createQuery(QUERY);
                query.setParameter("user_id", userId);
                query.setParameter("recipe_id", PASSWORDLESS.toString());

                query.executeUpdate();
            }

            // Even if the user is changed after we read it here (which is unlikely),
            // we'd only leave devices that will be cleaned up later automatically when they expire.
            UserInfo user = getUserById(start, userId);
            {
                String QUERY = "DELETE FROM PasswordlessUsersDO entity WHERE entity.user_id = :user_id";
                final CustomQueryWrapper query = session.createQuery(QUERY);

                query.setParameter("user_id", userId);

                query.executeUpdate();
            }

            if (user != null) {
                if (user.email != null) {
                    deleteDevicesByEmail_Transaction(session, user.email);
                }
                if (user.phoneNumber != null) {
                    deleteDevicesByPhoneNumber_Transaction(session, user.phoneNumber);
                }
            }

            return null;
        }, true);
    }

    public static int updateUserEmail_Transaction(CustomSessionWrapper session, String userId, String email)
            throws SQLException {
        String QUERY = "UPDATE PasswordlessUsersDO entity SET entity.email = :email WHERE entity.user_id = :user_id";

        final CustomQueryWrapper query = session.createQuery(QUERY);
        query.setParameter("email", email);
        query.setParameter("user_id", userId);

        return query.executeUpdate();
    }

    public static int updateUserPhoneNumber_Transaction(CustomSessionWrapper session, String userId, String phoneNumber)
            throws SQLException {
        String QUERY = "UPDATE PasswordlessUsersDO entity SET entity.phone_number = :phone_number WHERE entity"
                + ".user_id = :user_id";

        final CustomQueryWrapper query = session.createQuery(QUERY);
        query.setParameter("phone_number", phoneNumber);
        query.setParameter("user_id", userId);

        return query.executeUpdate();
    }

    public static PasswordlessDevice getDevice(Start start, String deviceIdHash)
            throws StorageQueryException, SQLException {
        return ConnectionPool.withSession(start, (session, con) -> {
            final PasswordlessDevicesDO passwordlessDevicesDO = session.get(PasswordlessDevicesDO.class, deviceIdHash);
            if (passwordlessDevicesDO == null) {
                return null;
            }
            return entityToPasswordlessDevice(passwordlessDevicesDO);
        }, false);
    }

    @NotNull
    private static PasswordlessDevice entityToPasswordlessDevice(PasswordlessDevicesDO result) {
        return new PasswordlessDevice(result.getDevice_id_hash(), result.getEmail(), result.getPhone_number(),
                result.getLink_code_salt(), result.getFailed_attempts());
    }

    public static PasswordlessDevice[] getDevicesByEmail(Start start, @Nonnull String email)
            throws StorageQueryException, SQLException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity FROM PasswordlessDevicesDO entity WHERE entity.email = :email";
            final CustomQueryWrapper<PasswordlessDevicesDO> query = session.createQuery(QUERY,
                    PasswordlessDevicesDO.class);
            query.setParameter("email", email);

            return query.list().stream().map(PasswordlessQueries::entityToPasswordlessDevice)
                    .toArray(PasswordlessDevice[]::new);
        }, false);
    }

    public static PasswordlessDevice[] getDevicesByPhoneNumber(Start start, @Nonnull String phoneNumber)
            throws StorageQueryException, SQLException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity FROM PasswordlessDevicesDO entity WHERE entity.phone_number = :phone_number";
            final CustomQueryWrapper<PasswordlessDevicesDO> query = session.createQuery(QUERY,
                    PasswordlessDevicesDO.class);
            query.setParameter("phone_number", phoneNumber);

            return query.list().stream().map(PasswordlessQueries::entityToPasswordlessDevice)
                    .toArray(PasswordlessDevice[]::new);
        }, false);
    }

    public static PasswordlessCode[] getCodesOfDevice(Start start, String deviceIdHash)
            throws StorageQueryException, SQLException {
        return ConnectionPool.withSession(start,
                (session, con) -> PasswordlessQueries.getCodesOfDevice_Transaction(session, deviceIdHash), true);
    }

    public static PasswordlessCode[] getCodesBefore(Start start, long time) throws StorageQueryException, SQLException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity FROM PasswordlessCodesDO entity WHERE entity.created_at < :created_at";
            final CustomQueryWrapper<PasswordlessCodesDO> query = session.createQuery(QUERY, PasswordlessCodesDO.class);

            query.setParameter("created_at", time);

            return query.list().stream().map(PasswordlessQueries::entityToPasswordlessCode)
                    .toArray(PasswordlessCode[]::new);
        }, false);
    }

    @NotNull
    private static PasswordlessCode entityToPasswordlessCode(PasswordlessCodesDO result) {
        return new PasswordlessCode(result.getCode_id(), result.getDevice_id_hash(), result.getLink_code_hash(),
                result.getCreated_at());
    }

    public static PasswordlessCode getCode(Start start, String codeId) throws StorageQueryException, SQLException {
        return ConnectionPool.withSession(start, (session, con) -> {
            final PasswordlessCodesDO passwordlessCodesDO = session.get(PasswordlessCodesDO.class, codeId);
            if (passwordlessCodesDO == null) {
                return null;
            }
            return entityToPasswordlessCode(passwordlessCodesDO);
        }, false);
    }

    public static PasswordlessCode getCodeByLinkCodeHash(Start start, String linkCodeHash)
            throws StorageQueryException, SQLException {
        return ConnectionPool.withSession(start,
                (session, con) -> PasswordlessQueries.getCodeByLinkCodeHash_Transaction(session, linkCodeHash), true);
    }

    public static PasswordlessCode getCodeByLinkCodeHash_Transaction(CustomSessionWrapper session, String linkCodeHash)
            throws SQLException {
        // We do not lock here, since the device is already locked earlier in the transaction.
        String QUERY = "SELECT entity FROM PasswordlessCodesDO entity WHERE entity.link_code_hash = :link_code_hash";
        final CustomQueryWrapper<PasswordlessCodesDO> query = session.createQuery(QUERY, PasswordlessCodesDO.class);

        query.setParameter("link_code_hash", linkCodeHash);

        return query.list().stream().findFirst().map(PasswordlessQueries::entityToPasswordlessCode).orElse(null);
    }

    public static List<UserInfo> getUsersByIdList(Start start, List<String> ids)
            throws SQLException, StorageQueryException {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity FROM PasswordlessUsersDO entity WHERE entity.user_id in (:user_ids)";
            final CustomQueryWrapper<PasswordlessUsersDO> query = session.createQuery(QUERY, PasswordlessUsersDO.class);

            query.setParameterList("user_ids", ids);

            return query.list().stream().map(result -> new UserInfo(result.getUser_id(), result.getEmail(),
                    result.getPhone_number(), result.getTime_joined())).collect(Collectors.toList());

        }, false);
    }

    public static UserInfo getUserById(Start start, String userId) throws StorageQueryException, SQLException {
        List<String> input = new ArrayList<>();
        input.add(userId);
        List<UserInfo> result = getUsersByIdList(start, input);
        if (result.size() == 1) {
            return result.get(0);
        }
        return null;
    }

    public static UserInfo getUserByEmail(Start start, @Nonnull String email)
            throws StorageQueryException, SQLException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity FROM PasswordlessUsersDO entity WHERE entity.email = :email";
            final CustomQueryWrapper<PasswordlessUsersDO> query = session.createQuery(QUERY, PasswordlessUsersDO.class);

            query.setParameter("email", email);

            return query.list().stream().findFirst().map(result -> new UserInfo(result.getUser_id(), result.getEmail(),
                    result.getPhone_number(), result.getTime_joined())).orElse(null);

        }, false);
    }

    public static UserInfo getUserByPhoneNumber(Start start, @Nonnull String phoneNumber)
            throws StorageQueryException, SQLException {

        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity FROM PasswordlessUsersDO entity WHERE entity.phone_number = :phone_number";
            final CustomQueryWrapper<PasswordlessUsersDO> query = session.createQuery(QUERY, PasswordlessUsersDO.class);

            query.setParameter("phone_number", phoneNumber);

            return query.list().stream().findFirst().map(result -> new UserInfo(result.getUser_id(), result.getEmail(),
                    result.getPhone_number(), result.getTime_joined())).orElse(null);

        }, false);
    }

}
