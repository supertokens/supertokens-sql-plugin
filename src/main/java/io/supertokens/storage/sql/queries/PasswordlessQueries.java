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

import io.supertokens.pluginInterface.sqlStorage.SessionObject;
import io.supertokens.storage.sql.Start;
import io.supertokens.storage.sql.config.Config;
import io.supertokens.pluginInterface.RECIPE_ID;
import io.supertokens.pluginInterface.RowMapper;
import io.supertokens.pluginInterface.emailpassword.exceptions.UnknownUserIdException;
import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.pluginInterface.exceptions.StorageTransactionLogicException;
import io.supertokens.pluginInterface.passwordless.PasswordlessCode;
import io.supertokens.pluginInterface.passwordless.PasswordlessDevice;
import io.supertokens.pluginInterface.passwordless.UserInfo;
import io.supertokens.storage.sql.dataaccessobjects.passwordless.impl.PasswordlessCodesDAO;
import io.supertokens.storage.sql.dataaccessobjects.passwordless.impl.PasswordlessDevicesDAO;
import io.supertokens.storage.sql.dataaccessobjects.passwordless.impl.PasswordlessUsersDAO;
import io.supertokens.storage.sql.dataaccessobjects.passwordless.impl.UsersDAO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessCodesDO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessDevicesDO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessUsersDO;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import javax.annotation.Nonnull;
import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PasswordlessQueries {
    public static String getQueryToCreateUsersTable(Start start) {
        return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getPasswordlessUsersTable() + " ("
                + "user_id CHAR(36) NOT NULL," + "email VARCHAR(256) UNIQUE," + "phone_number VARCHAR(256) UNIQUE,"
                + "time_joined BIGINT UNSIGNED NOT NULL," + "PRIMARY KEY (user_id));";
    }

    public static String getQueryToCreateDevicesTable(Start start) {
        return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getPasswordlessDevicesTable() + " ("
                + "device_id_hash CHAR(44) NOT NULL," + "email VARCHAR(256)," + "phone_number VARCHAR(256),"
                + "link_code_salt CHAR(44) NOT NULL," + "failed_attempts INT UNSIGNED NOT NULL,"
                + "PRIMARY KEY (device_id_hash));";
    }

    public static String getQueryToCreateCodesTable(Start start) {
        return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getPasswordlessCodesTable() + " ("
                + "code_id CHAR(36) NOT NULL," + "device_id_hash CHAR(44) NOT NULL,"
                + "link_code_hash CHAR(44) NOT NULL UNIQUE," + "created_at BIGINT UNSIGNED NOT NULL,"
                + "PRIMARY KEY (code_id)," + "FOREIGN KEY (device_id_hash) REFERENCES "
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

    public static String getQueryToCreateCodeCreatedAtIndex(Start start) {
        return "CREATE INDEX passwordless_codes_created_at_index ON "
                + Config.getConfig(start).getPasswordlessCodesTable() + "(created_at);";
    }

    public static void createDeviceWithCode(Start start, SessionObject sessionObject, String email, String phoneNumber,
            String linkCodeSalt, PasswordlessCode code) throws StorageTransactionLogicException, StorageQueryException {

        PasswordlessDevicesDAO passwordlessDevicesDAO = new PasswordlessDevicesDAO(sessionObject);
        passwordlessDevicesDAO.insertIntoTableValues(code.deviceIdHash, email, phoneNumber, linkCodeSalt, 0, null);
        PasswordlessQueries.createCode_Transaction(start, sessionObject, code);
    }

    public static PasswordlessDevice getDevice_Transaction(Start start, SessionObject sessionObject,
            String deviceIdHash) throws StorageQueryException, SQLException {

        PasswordlessDevicesDAO devicesDAO = new PasswordlessDevicesDAO(sessionObject);

        PasswordlessDevicesDO passwordlessDevicesDO = devicesDAO.getWhereDeviceIdHashEquals_locked(deviceIdHash);
        return PasswordlessDeviceRowMapper.getInstance().mapOrThrow(passwordlessDevicesDO);

    }

    public static void incrementDeviceFailedAttemptCount_Transaction(Start start, SessionObject sessionObject,
            String deviceIdHash) throws SQLException {

        PasswordlessDevicesDAO passwordlessDevicesDAO = new PasswordlessDevicesDAO(sessionObject);
        passwordlessDevicesDAO.updateFailedAttemptsWhereDeviceIdHashEquals(deviceIdHash);
    }

    public static void deleteDevice_Transaction(Start start, SessionObject sessionObject, String deviceIdHash)
            throws SQLException {
        PasswordlessDevicesDAO passwordlessDevicesDAO = new PasswordlessDevicesDAO(sessionObject);

        passwordlessDevicesDAO.deleteWhereDeviceIdHashEquals(deviceIdHash);
    }

    public static void deleteDevicesByPhoneNumber_Transaction(Start start, SessionObject sessionObject,
            @Nonnull String phoneNumber) throws SQLException {

        PasswordlessDevicesDAO devicesDAO = new PasswordlessDevicesDAO(sessionObject);
        devicesDAO.deleteWherePhoneNumberEquals(phoneNumber);
    }

    public static void deleteDevicesByEmail_Transaction(Start start, SessionObject sessionObject, @Nonnull String email)
            throws SQLException, StorageQueryException {

        PasswordlessDevicesDAO devicesDAO = new PasswordlessDevicesDAO(sessionObject);

        devicesDAO.deleteWhereEmailEquals(email);
    }

    // TODO: optimize later
    private static void createCode_Transaction(Start start, SessionObject sessionObject, PasswordlessCode code) {

        PasswordlessCodesDAO passwordlessCodesDAO = new PasswordlessCodesDAO(sessionObject);
        PasswordlessDevicesDAO passwordlessDevicesDAO = new PasswordlessDevicesDAO(sessionObject);
        PasswordlessDevicesDO devicesDO = passwordlessDevicesDAO.getWhereDeviceIdHashEquals(code.deviceIdHash);

        passwordlessCodesDAO.insertIntoTableValues(code.id, devicesDO, code.linkCodeHash, code.createdAt);
    }

    public static void createCode(Start start, SessionObject sessionObject, PasswordlessCode code)
            throws HibernateException {
        PasswordlessQueries.createCode_Transaction(start, sessionObject, code);
    }

    // TODO: optimize later
    public static PasswordlessCode[] getCodesOfDevice_Transaction(Start start, SessionObject sessionObject,
            String deviceIdHash) throws StorageQueryException, SQLException {
        // We do not lock here, since the device is already locked earlier in the transaction.

        PasswordlessCodesDAO codesDAO = new PasswordlessCodesDAO(sessionObject);

        PasswordlessDevicesDAO devicesDAO = new PasswordlessDevicesDAO(sessionObject);

        PasswordlessDevicesDO devicesDO = devicesDAO.getWhereDeviceIdHashEquals_locked(deviceIdHash);

        List<PasswordlessCodesDO> codesDOList = codesDAO.getCodesWhereDeviceIdHashEquals(devicesDO);

        Iterator<PasswordlessCodesDO> iterator = codesDOList.iterator();
        PasswordlessCode[] finalResult = new PasswordlessCode[codesDOList.size()];
        int counter = 0;

        while (iterator.hasNext()) {
            PasswordlessCodesDO passwordlessCodesDO = iterator.next();
            finalResult[counter++] = PasswordlessCodeRowMapper.getInstance().mapOrThrow(passwordlessCodesDO);
        }

        return finalResult;

    }

    public static PasswordlessCode getCodeByLinkCodeHash_Transaction(Start start, SessionObject sessionObject,
            String linkCodeHash) throws StorageQueryException, SQLException {
        // We do not lock here, since the device is already locked earlier in the transaction.

        PasswordlessCodesDAO passwordlessCodesDO = new PasswordlessCodesDAO(sessionObject);

        PasswordlessCodesDO codesDO = passwordlessCodesDO.getWhereLinkCodeHashEquals(linkCodeHash);

        return PasswordlessCodeRowMapper.getInstance().mapOrThrow(codesDO);

    }

    public static void deleteCode_Transaction(Start start, SessionObject sessionObject, String codeId)
            throws SQLException {
        PasswordlessCodesDAO codesDAO = new PasswordlessCodesDAO(sessionObject);

        codesDAO.deleteWhereCodeIdEquals(codeId);
    }

    public static void createUser(Start start, UserInfo user)
            throws StorageTransactionLogicException, StorageQueryException {
        start.startTransactionHibernate(session -> {
            try {
                {
                    UsersDAO usersDAO = new UsersDAO(session);
                    usersDAO.insertIntoTableValues(user.id, RECIPE_ID.PASSWORDLESS.toString(), user.timeJoined);
                }

                {
                    PasswordlessUsersDAO passwordlessUsersDAO = new PasswordlessUsersDAO(session);
                    passwordlessUsersDAO.insertValuesIntoTable(user.id, user.email, user.phoneNumber, user.timeJoined);
                }
                start.commitTransaction(session);
            } catch (PersistenceException throwables) {
                throw new StorageTransactionLogicException(throwables);
            }
            return null;
        });
    }

    public static void deleteUser(Start start, String userId)
            throws StorageTransactionLogicException, StorageQueryException {
        start.startTransactionHibernate(session -> {
            try {
                {
                    UsersDAO usersDAO = new UsersDAO(session);
                    usersDAO.deleteWhereUserIdEqualsAndRecipeIdEquals(userId, RECIPE_ID.PASSWORDLESS.toString());
                }

                // Even if the user is changed after we read it here (which is unlikely),
                // we'd only leave devices that will be cleaned up later automatically when they expire.
                UserInfo user = getUserById(start, session, userId);
                {
                    PasswordlessUsersDAO passwordlessUsersDAO = new PasswordlessUsersDAO(session);
                    passwordlessUsersDAO.deleteWhereUserIdEquals(userId);
                }

                if (user != null) {
                    if (user.email != null) {
                        deleteDevicesByEmail_Transaction(start, session, user.email);
                    }
                    if (user.phoneNumber != null) {
                        deleteDevicesByPhoneNumber_Transaction(start, session, user.phoneNumber);
                    }
                }

            } catch (SQLException throwables) {
                throw new StorageTransactionLogicException(throwables);
            }
            return null;
        });
    }

    public static void updateUserEmail_Transaction(Start start, SessionObject sessionObject, String userId,
            String email) throws UnknownUserIdException {

        PasswordlessUsersDAO usersDAO = new PasswordlessUsersDAO(sessionObject);

        usersDAO.updateEmailWhereUserIdEquals(userId, email);
    }

    public static void updateUserPhoneNumber_Transaction(Start start, SessionObject sessionObject, String userId,
            String phoneNumber) throws SQLException, UnknownUserIdException {
        PasswordlessUsersDAO usersDAO = new PasswordlessUsersDAO(sessionObject);

        usersDAO.updatePhoneNumberWhereUserIdEquals(userId, phoneNumber);
    }

    public static PasswordlessDevice getDevice(Start start, SessionObject sessionObject, String deviceIdHash)
            throws StorageQueryException, SQLException {

        PasswordlessDevicesDAO passwordlessDevicesDAO = new PasswordlessDevicesDAO(sessionObject);
        PasswordlessDevicesDO devicesDO = passwordlessDevicesDAO.getWhereDeviceIdHashEquals(deviceIdHash);

        return PasswordlessDeviceRowMapper.getInstance().mapOrThrow(devicesDO);
    }

    public static PasswordlessDevice[] getDevicesByEmail(Start start, SessionObject sessionObject,
            @Nonnull String email) throws StorageQueryException, SQLException {

        PasswordlessDevicesDAO devicesDAO = new PasswordlessDevicesDAO(sessionObject);

        List<PasswordlessDevicesDO> results = devicesDAO.getDevicesWhereEmailEquals(email);
        Iterator<PasswordlessDevicesDO> iterator = results.iterator();
        PasswordlessDevice[] finalResult = new PasswordlessDevice[results.size()];
        int counter = 0;
        while (iterator.hasNext()) {
            PasswordlessDevicesDO devicesDO = iterator.next();
            finalResult[counter++] = PasswordlessDeviceRowMapper.getInstance().mapOrThrow(devicesDO);
        }

        return finalResult;
    }

    public static PasswordlessDevice[] getDevicesByPhoneNumber(Start start, SessionObject sessionObject,
            @Nonnull String phoneNumber) throws StorageQueryException, SQLException {
        PasswordlessDevicesDAO devicesDAO = new PasswordlessDevicesDAO(sessionObject);

        List<PasswordlessDevicesDO> results = devicesDAO.getDevicesWherePhoneNumberEquals(phoneNumber);
        Iterator<PasswordlessDevicesDO> iterator = results.iterator();
        PasswordlessDevice[] finalResult = new PasswordlessDevice[results.size()];
        int counter = 0;
        while (iterator.hasNext()) {
            PasswordlessDevicesDO devicesDO = iterator.next();
            finalResult[counter++] = PasswordlessDeviceRowMapper.getInstance().mapOrThrow(devicesDO);
        }

        return finalResult;
    }

    public static PasswordlessCode[] getCodesOfDevice(Start start, SessionObject sessionObject, String deviceIdHash)
            throws StorageQueryException, SQLException, InterruptedException {

        return PasswordlessQueries.getCodesOfDevice_Transaction(start, sessionObject, deviceIdHash);

    }

    public static PasswordlessCode[] getCodesBefore(Start start, SessionObject sessionObject, long time)
            throws StorageQueryException, SQLException {

        PasswordlessCodesDAO codesDAO = new PasswordlessCodesDAO(sessionObject);

        List<PasswordlessCodesDO> codesDOList = codesDAO.getCodesWhereCreatedAtLessThan(time);
        PasswordlessCode[] finalResult = new PasswordlessCode[codesDOList.size()];
        Iterator<PasswordlessCodesDO> iterator = codesDOList.iterator();
        int counter = 0;

        while (iterator.hasNext()) {
            PasswordlessCodesDO codeDo = iterator.next();
            finalResult[counter++] = PasswordlessCodeRowMapper.getInstance().mapOrThrow(codeDo);
        }
        return finalResult;
    }

    public static PasswordlessCode getCode(Start start, SessionObject sessionObject, String codeId)
            throws StorageQueryException, SQLException {

        PasswordlessCodesDAO codesDAO = new PasswordlessCodesDAO(sessionObject);

        PasswordlessCodesDO codesDO = codesDAO.getCodeWhereCodeIdEquals(codeId);
        return PasswordlessCodeRowMapper.getInstance().mapOrThrow(codesDO);
    }

    public static PasswordlessCode getCodeByLinkCodeHash(Start start, SessionObject sessionObject, String linkCodeHash)
            throws StorageQueryException, SQLException, InterruptedException {

        return PasswordlessQueries.getCodeByLinkCodeHash_Transaction(start, sessionObject, linkCodeHash);

    }

    public static List<UserInfo> getUsersByIdList(Start start, SessionObject sessionObject, List<String> ids)
            throws SQLException, StorageQueryException {
        List<UserInfo> finalResult = new ArrayList<>();
        if (ids.size() > 0) {
            StringBuilder QUERY = new StringBuilder("SELECT user_id, email, phone_number, time_joined FROM "
                    + Config.getConfig(start).getPasswordlessUsersTable());
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
                finalResult.add(UserInfoRowMapperNativeQuery.getInstance().mapOrThrow(iterator.next()));
            }
        }
        return finalResult;
    }

    public static UserInfo getUserById(Start start, SessionObject sessionObject, String userId)
            throws StorageQueryException, SQLException {
        List<String> input = new ArrayList<>();
        input.add(userId);

        List<UserInfo> result = getUsersByIdList(start, sessionObject, input);
        if (result.size() == 1) {
            return result.get(0);
        }
        return null;
    }

    public static UserInfo getUserByEmail(Start start, SessionObject sessionObject, @Nonnull String email)
            throws StorageQueryException, SQLException {

        PasswordlessUsersDAO usersDAO = new PasswordlessUsersDAO(sessionObject);

        PasswordlessUsersDO usersDO = usersDAO.getUserWhereEmailEquals(email);
        return UserInfoRowMapper.getInstance().mapOrThrow(usersDO);

    }

    public static UserInfo getUserByPhoneNumber(Start start, SessionObject sessionObject, @Nonnull String phoneNumber)
            throws StorageQueryException, SQLException {
        PasswordlessUsersDAO usersDAO = new PasswordlessUsersDAO(sessionObject);

        PasswordlessUsersDO usersDO = usersDAO.getUserWherePhoneNumberEquals(phoneNumber);
        return UserInfoRowMapper.getInstance().mapOrThrow(usersDO);
    }

    private static class PasswordlessDeviceRowMapper implements RowMapper<PasswordlessDevice, PasswordlessDevicesDO> {
        private static final PasswordlessDeviceRowMapper INSTANCE = new PasswordlessDeviceRowMapper();

        private PasswordlessDeviceRowMapper() {
        }

        private static PasswordlessDeviceRowMapper getInstance() {
            return INSTANCE;
        }

        @Override
        public PasswordlessDevice map(PasswordlessDevicesDO result) throws Exception {
            if (result == null) {
                return null;
            }
            return new PasswordlessDevice(result.getDevice_id_hash(), result.getEmail(), result.getPhone_number(),
                    result.getLink_code_salt(), result.getFailed_attempts());
        }
    }

    private static class PasswordlessCodeRowMapper implements RowMapper<PasswordlessCode, PasswordlessCodesDO> {
        private static final PasswordlessCodeRowMapper INSTANCE = new PasswordlessCodeRowMapper();

        private PasswordlessCodeRowMapper() {
        }

        private static PasswordlessCodeRowMapper getInstance() {
            return INSTANCE;
        }

        @Override
        public PasswordlessCode map(PasswordlessCodesDO result) throws Exception {
            if (result == null) {
                return null;
            }

            return new PasswordlessCode(result.getCode_id(), result.getDevice().getDevice_id_hash(),
                    result.getLink_code_hash(), result.getCreated_at());
        }
    }

    private static class UserInfoRowMapperNativeQuery implements RowMapper<UserInfo, Object[]> {
        private static final UserInfoRowMapperNativeQuery INSTANCE = new UserInfoRowMapperNativeQuery();

        private UserInfoRowMapperNativeQuery() {
        }

        private static UserInfoRowMapperNativeQuery getInstance() {
            return INSTANCE;
        }

        @Override
        public UserInfo map(Object[] result) throws Exception {
            if (result == null) {
                return null;
            }
            return new UserInfo(result[0] != null ? result[0].toString() : "",
                    result[1] != null ? result[1].toString() : null, result[2] != null ? result[2].toString() : null,
                    Long.valueOf(result[3].toString()));
        }
    }

    private static class UserInfoRowMapper implements RowMapper<UserInfo, PasswordlessUsersDO> {
        private static final UserInfoRowMapper INSTANCE = new UserInfoRowMapper();

        private UserInfoRowMapper() {
        }

        private static UserInfoRowMapper getInstance() {
            return INSTANCE;
        }

        @Override
        public UserInfo map(PasswordlessUsersDO result) throws Exception {
            if (result == null) {
                return null;
            }
            return new UserInfo(result.getUser_id(), result.getEmail(), result.getPhone_number(),
                    result.getTime_joined());
        }
    }
}
