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

public interface PasswordlessCodesInterfaceDAO extends DAO {

    /**
     * String QUERY = "INSERT INTO " + Config.getConfig(start).getPasswordlessCodesTable()
     * + "(code_id, device_id_hash, link_code_hash, created_at)" + " VALUES(?, ?, ?, ?)";
     */
    public String insertIntoTableValues(String codeId, PasswordlessDevicesDO deviceId, String linkCodeHash,
            long createdAt);

    /**
     * String QUERY = "SELECT code_id, device_id_hash, link_code_hash, created_at FROM "
     * + Config.getConfig(start).getPasswordlessCodesTable() + " WHERE device_id_hash = ?";
     */
    public List<PasswordlessCodesDO> getCodesWhereDeviceIdHashEquals(PasswordlessDevicesDO deviceDO);

    /**
     * String QUERY = "SELECT code_id, device_id_hash, link_code_hash, created_at FROM "
     * + Config.getConfig(start).getPasswordlessCodesTable() + " WHERE link_code_hash = ?";
     */
    public PasswordlessCodesDO getWhereLinkCodeHashEquals(String linkCodeHash);

    /**
     * String QUERY = "DELETE FROM " + Config.getConfig(start).getPasswordlessCodesTable() + " WHERE code_id
     * = ?";
     */
    public int deleteWhereCodeIdEquals(String codeId);

    /**
     * String QUERY = "SELECT code_id, device_id_hash, link_code_hash, created_at FROM "
     * + Config.getConfig(start).getPasswordlessCodesTable() + " WHERE created_at < ?";
     */
    public List<PasswordlessCodesDO> getCodesWhereCreatedAtLessThan(long createdAt);

    /**
     * String QUERY = "SELECT code_id, device_id_hash, link_code_hash, created_at FROM "
     * + Config.getConfig(start).getPasswordlessCodesTable() + " WHERE code_id = ?";
     */
    public PasswordlessCodesDO getCodeWhereCodeIdEquals(String codeId);

}
