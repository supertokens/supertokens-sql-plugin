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

package io.supertokens.storage.sql.dataaccessobjects.thirdparty;

import io.supertokens.storage.sql.dataaccessobjects.DAO;
import io.supertokens.storage.sql.domainobjects.thirdparty.ThirdPartyUsersDO;

import java.io.Serializable;

public interface ThirdPartyUsersInterfaceDAO extends DAO {
    /**
     * String QUERY = "SELECT user_id, third_party_id, third_party_user_id, email, time_joined FROM "
     *                 + Config.getConfig(start).getThirdPartyUsersTable()
     *                 + " WHERE third_party_id = ? AND third_party_user_id = ?";
     */
    public ThirdPartyUsersDO getWhereThirdPartyIDEqualsAndThirdPartyUserIdEquals(String thirdPartyId,
                                                                                 String thirdPartyUserId);


    /**
     * insert values into table
     */
    public Serializable insertValues(String thirdPartyId, String thirdPartyUserId, String userId,
                                     String email, long timeJoined);
}
