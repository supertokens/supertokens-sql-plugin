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

package io.supertokens.storage.sql.dataaccessobjects.general;

import io.supertokens.storage.sql.dataaccessobjects.DAO;
import io.supertokens.storage.sql.domainobjects.general.KeyValueDO;

import javax.persistence.PersistenceException;

public interface KeyValueInterfaceDAO extends DAO {

    /**
     * String QUERY = "SELECT value, created_at_time FROM " + Config.getConfig(start).getKeyValueTable()
     * + " WHERE name = ?";
     */
    public String insertIntoValues(String name, String value, long created_at_time);

    /**
     * String QUERY = "SELECT value, created_at_time FROM " + Config.getConfig(start).getKeyValueTable()
     * + " WHERE name = ? FOR UPDATE";
     */
    public KeyValueDO getWhereNameEquals_transaction(String name);

    public void updateWhereNameEquals_transaction(String name, String value, long createdAtTime)
            throws PersistenceException;
}
