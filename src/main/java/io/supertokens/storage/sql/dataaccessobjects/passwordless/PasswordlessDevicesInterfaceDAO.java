/*
 *    Copyright (c) 2022, VRAI Labs and/or its affiliates. All rights reserved.
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

package io.supertokens.storage.sql.dataaccessobjects.passwordless;

import io.supertokens.storage.sql.dataaccessobjects.DAO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessCodesDO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessDevicesDO;

import java.util.List;

public interface PasswordlessDevicesInterfaceDAO extends DAO {

    /**
     * String QUERY = "INSERT INTO " + Config.getConfig(start).getPasswordlessDevicesTable()
     * + "(device_id_hash, email, phone_number, link_code_salt, failed_attempts)"
     * + " VALUES(:device_id_hash, :email, :phone_number, :link_code_salt, 0)";
     */
    public PasswordlessDevicesDO insertIntoTableValues(String deviceIdHash, String email, String phoneNumber,
            String linkCodeSalt, int failedAttempts, List<PasswordlessCodesDO> codes);

    /**
     * String QUERY = "SELECT device_id_hash, email, phone_number, link_code_salt, failed_attempts FROM "
     * + Config.getConfig(start).getPasswordlessDevicesTable() + " WHERE device_id_hash = ? FOR UPDATE";
     */
    public PasswordlessDevicesDO getWhereDeviceIdHashEquals_locked(String deviceIdHash);

    public PasswordlessDevicesDO getWhereDeviceIdHashEquals(String deviceIdHash);

    /**
     * String QUERY = "UPDATE " + Config.getConfig(start).getPasswordlessDevicesTable()
     * + " SET failed_attempts = failed_attempts + 1 WHERE device_id_hash = ?";
     */
    public void updateFailedAttemptsWhereDeviceIdHashEquals(String deviceIdHash);

    /**
     * String QUERY = "DELETE FROM " + Config.getConfig(start).getPasswordlessDevicesTable()
     * + " WHERE device_id_hash = ?";
     */
    public void deleteWhereDeviceIdHashEquals_transaction(String deviceIdHash);

    /**
     * String QUERY = "DELETE FROM " + Config.getConfig(start).getPasswordlessDevicesTable()
     * + " WHERE phone_number = ?";
     */
    public void deleteWherePhoneNumberEquals(String phoneNumber);

    /**
     * String QUERY = "DELETE FROM " + Config.getConfig(start).getPasswordlessDevicesTable() + " WHERE email
     * = ?";
     */
    public void deleteWhereEmailEquals_transaction(String email);

    /**
     * String QUERY = "SELECT device_id_hash, email, phone_number, link_code_salt, failed_attempts FROM "
     * + Config.getConfig(start).getPasswordlessDevicesTable() + " WHERE email = ?";
     */
    public List<PasswordlessDevicesDO> getDevicesWhereEmailEquals(String email);

    /**
     * String QUERY = "SELECT device_id_hash, email, phone_number, link_code_salt, failed_attempts FROM "
     * + Config.getConfig(start).getPasswordlessDevicesTable() + " WHERE email = ? FOR UPDATE";
     */
    public List<PasswordlessDevicesDO> getDevicesWhereEmailEquals_transaction(String email);

    /**
     * String QUERY = "SELECT device_id_hash, email, phone_number, link_code_salt, failed_attempts FROM "
     * + Config.getConfig(start).getPasswordlessDevicesTable() + " WHERE phone_number = ?";
     */
    public List<PasswordlessDevicesDO> getDevicesWherePhoneNumberEquals(String phoneNumber);

}
