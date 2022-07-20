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
import io.supertokens.storage.sql.hibernate.CustomQueryWrapper;
import io.supertokens.storage.sql.hibernate.CustomSessionWrapper;
import io.supertokens.storage.sql.utils.Utils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.persistence.LockModeType;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.supertokens.pluginInterface.RECIPE_ID.PASSWORDLESS;
import static io.supertokens.storage.sql.QueryExecutorTemplate.update;
import static io.supertokens.storage.sql.config.Config.getConfig;

public class PasswordlessQueries {
    public static String getQueryToCreateUsersTable(Start start) {
        String schema = Config.getConfig(start).getTableSchema();
        String usersTable = Config.getConfig(start).getPasswordlessUsersTable();
        // @formatter:off
        return "CREATE TABLE IF NOT EXISTS " + usersTable
                + " (" + "user_id CHAR(36) NOT NULL,"
                + "email VARCHAR(256) CONSTRAINT " + Utils.getConstraintName(schema, usersTable, "email", "key")
                + " UNIQUE,"
                + "phone_number VARCHAR(256) CONSTRAINT " +
                Utils.getConstraintName(schema, usersTable, "phone_number", "key")
                + " UNIQUE,"
                + "time_joined BIGINT NOT NULL, "
                + "CONSTRAINT " + Utils.getConstraintName(schema, usersTable, null, "pkey")
                + " PRIMARY KEY (user_id)" + ");";
        // @formatter:on
    }

    public static String getQueryToCreateDevicesTable(Start start) {
        String schema = Config.getConfig(start).getTableSchema();
        String devicesTable = Config.getConfig(start).getPasswordlessDevicesTable();
        // @formatter:off
        return "CREATE TABLE IF NOT EXISTS " + devicesTable
                + " (" + "device_id_hash CHAR(44) NOT NULL,"
                + "email VARCHAR(256), "
                + "phone_number VARCHAR(256),"
                + "link_code_salt CHAR(44) NOT NULL,"
                + "failed_attempts INT NOT NULL,"
                + "CONSTRAINT "
                + Utils.getConstraintName(schema, devicesTable, null, "pkey")
                + " PRIMARY KEY (device_id_hash));";
        // @formatter:on
    }

    public static String getQueryToCreateCodesTable(Start start) {
        String schema = Config.getConfig(start).getTableSchema();
        String codesTable = Config.getConfig(start).getPasswordlessCodesTable();
        // @formatter:off
        return "CREATE TABLE IF NOT EXISTS " + codesTable
                + " (" + "code_id CHAR(36) NOT NULL,"
                + "device_id_hash CHAR(44) NOT NULL,"
                + "link_code_hash CHAR(44) NOT NULL CONSTRAINT " +
                Utils.getConstraintName(schema, codesTable, "link_code_hash", "key")
                + " UNIQUE,"
                + "created_at BIGINT NOT NULL,"
                + "CONSTRAINT " + Utils.getConstraintName(schema, codesTable, null, "pkey")
                + " PRIMARY KEY (code_id),"
                + "CONSTRAINT " + Utils.getConstraintName(schema, codesTable, "device_id_hash", "fkey") +
                " FOREIGN KEY (device_id_hash) "
                + "REFERENCES " + Config.getConfig(start).getPasswordlessDevicesTable()
                + "(device_id_hash) ON DELETE CASCADE ON UPDATE CASCADE);";
        // @formatter:on
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
            PasswordlessCode code) throws StorageTransactionLogicException, StorageQueryException {
        start.startTransaction(con -> {
            Connection sqlCon = (Connection) con.getConnection();
            final CustomSessionWrapper session = (CustomSessionWrapper) con.getSession();
            try {
//                final PasswordlessDevicesDO passwordlessDevicesDO = new PasswordlessDevicesDO(code.deviceIdHash, email,
//                        phoneNumber, linkCodeSalt, 0);
//                session.save(PasswordlessDevicesDO.class, code.deviceIdHash, passwordlessDevicesDO);

                String QUERY = "INSERT INTO " + getConfig(start).getPasswordlessDevicesTable()
                        + "(device_id_hash, email, phone_number, link_code_salt, failed_attempts)"
                        + " VALUES(?, ?, ?, ?, 0)";
                update(sqlCon, QUERY, pst -> {
                    pst.setString(1, code.deviceIdHash);
                    pst.setString(2, email);
                    pst.setString(3, phoneNumber);
                    pst.setString(4, linkCodeSalt);
                });

                createCode_Transaction(start, (Connection) con.getConnection(), code);

                sqlCon.commit();
            } catch (SQLException throwables) {
                throw new StorageTransactionLogicException(throwables);
            }
            return null;
        }, TransactionIsolationLevel.REPEATABLE_READ);
    }

    public static PasswordlessDevice getDevice_Transaction(CustomSessionWrapper session, String deviceIdHash)
            throws SQLException {

        String QUERY = "SELECT entity FROM PasswordlessDevicesDO entity WHERE entity.device_id_hash = :device_id_hash";
        final CustomQueryWrapper<PasswordlessDevicesDO> query = session.createQuery(QUERY, PasswordlessDevicesDO.class);
        query.setParameter("device_id_hash", deviceIdHash);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);

        return query.list().stream().findFirst().map(PasswordlessQueries::entityToPasswordlessDevice).orElse(null);
    }

    public static void incrementDeviceFailedAttemptCount_Transaction(CustomSessionWrapper session, String deviceIdHash)
            throws SQLException {
        String QUERY = "UPDATE PasswordlessDevicesDO entity SET entity.failed_attempts = entity.failed_attempts + 1 "
                + "WHERE entity.device_id_hash = :device_id_hash";

        final CustomQueryWrapper query = session.createQuery(QUERY);
        query.setParameter("device_id_hash", deviceIdHash);

        query.executeUpdate();
    }

    public static void deleteDevice_Transaction(CustomSessionWrapper session, String deviceIdHash) throws SQLException {
        String QUERY = "DELETE FROM PasswordlessDevicesDO entity WHERE entity.device_id_hash = :device_id_hash";

        final CustomQueryWrapper query = session.createQuery(QUERY);
        query.setParameter("device_id_hash", deviceIdHash);

        query.executeUpdate();
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

//    private static void createCode_Transaction(CustomSessionWrapper session, PasswordlessCode code)
//            throws SQLException {
//
//
//        String hql = "INSERT INTO Student (firstName, lastName, email) " +
//                "SELECT firstName, lastName, email FROM Student";
//        Query query = session.createQuery(hql);
//
//        final PasswordlessDevicesDO passwordlessDevice = new PasswordlessDevicesDO();
//        passwordlessDevice.setDevice_id_hash(code.deviceIdHash);
//        final PasswordlessCodesDO toInsert = new PasswordlessCodesDO(code.id, passwordlessDevice, code.linkCodeHash,
//                code.createdAt);
//        session.save(PasswordlessCodesDO.class, code.id, toInsert);
//    }

    private static void createCode_Transaction(Start start, Connection con, PasswordlessCode code)
            throws SQLException, StorageQueryException {
        String QUERY = "INSERT INTO " + getConfig(start).getPasswordlessCodesTable()
                + "(code_id, device_id_hash, link_code_hash, created_at)" + " VALUES(?, ?, ?, ?)";
        update(con, QUERY, pst -> {
            pst.setString(1, code.id);
            pst.setString(2, code.deviceIdHash);
            pst.setString(3, code.linkCodeHash);
            pst.setLong(4, code.createdAt);
        });
    }

    public static void createCode(Start start, PasswordlessCode code)
            throws StorageTransactionLogicException, StorageQueryException {
        start.startTransaction(con -> {
            CustomSessionWrapper session = (CustomSessionWrapper) con.getSession();

            try {
                PasswordlessQueries.createCode_Transaction(start, (Connection) con.getConnection(), code);
            } catch (SQLException e) {
                throw new StorageTransactionLogicException(e);
            }
            return null;
        });
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

    public static void createUser(Start start, UserInfo user)
            throws StorageTransactionLogicException, StorageQueryException {
        start.startTransaction(con -> {
            Connection sqlCon = (Connection) con.getConnection();
            CustomSessionWrapper session = (CustomSessionWrapper) con.getSession();
            try {
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
                sqlCon.commit();
            } catch (SQLException throwables) {
                throw new StorageTransactionLogicException(throwables);
            }
            return null;
        });
    }

    public static void deleteUser(Start start, String userId)
            throws StorageQueryException, StorageTransactionLogicException {
        start.startTransaction(con -> {
            Connection sqlCon = (Connection) con.getConnection();
            CustomSessionWrapper session = (CustomSessionWrapper) con.getSession();
            try {
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
                ;
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

                sqlCon.commit();
            } catch (SQLException throwables) {
                throw new StorageTransactionLogicException(throwables);
            }
            return null;
        });
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
            String QUERY = "SELECT entity FROM PasswordlessDevicesDO entity WHERE entity.device_id_hash = :device_id_hash";
            final CustomQueryWrapper<PasswordlessDevicesDO> query = session.createQuery(QUERY,
                    PasswordlessDevicesDO.class);
            query.setParameter("device_id_hash", deviceIdHash);

            return query.list().stream().findFirst().map(PasswordlessQueries::entityToPasswordlessDevice).orElse(null);
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
            String QUERY = "SELECT entity FROM PasswordlessCodesDO entity WHERE entity.code_id = :code_id";
            final CustomQueryWrapper<PasswordlessCodesDO> query = session.createQuery(QUERY, PasswordlessCodesDO.class);

            query.setParameter("code_id", codeId);

            return query.list().stream().findFirst().map(PasswordlessQueries::entityToPasswordlessCode).orElse(null);
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
