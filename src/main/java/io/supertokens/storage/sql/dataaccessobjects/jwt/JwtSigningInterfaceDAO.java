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

package io.supertokens.storage.sql.dataaccessobjects.jwt;

import io.supertokens.storage.sql.dataaccessobjects.DAO;
import io.supertokens.storage.sql.domainobjects.jwtsigning.JWTSigningKeysDO;

import java.io.Serializable;
import java.util.List;

public interface JwtSigningInterfaceDAO extends DAO {

    /**
     * String QUERY = "INSERT INTO " + Config.getConfig(start).getJWTSigningKeysTable()
     *                 + "(key_id, key_string, created_at, algorithm) VALUES(?, ?, ?, ?)";
     */
    public Serializable insert(String keyId, String keyString, String algorithm, long createdAt);

    /**
     * String QUERY = "SELECT * FROM " + Config.getConfig(start).getJWTSigningKeysTable()
     *                 + " ORDER BY created_at DESC FOR UPDATE";
     */
    public List<JWTSigningKeysDO> getAllOrderByCreatedAtDesc_locked();
}
