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
import io.supertokens.storage.sql.dataaccessobjects.passwordless.impl.UsersDAO;
import io.supertokens.storage.sql.domainobjects.general.UsersDO;

public interface UsersInterfaceDAO extends DAO {

    /*
     * String QUERY = "INSERT INTO " + Config.getConfig(start).getUsersTable()
     * + "(user_id, recipe_id, time_joined)" + " VALUES(?, ?, ?)";
     * try (PreparedStatement pst = sqlCon.prepareStatement(QUERY)) {
     * pst.setString(1, user.id);
     * pst.setString(2, RECIPE_ID.PASSWORDLESS.toString());
     * pst.setLong(3, user.timeJoined);
     * pst.executeUpdate();
     * }
     */

    public UsersDO insertIntoTableValues(String userId, String recipeId, long timeJoined);

    /**
     * String QUERY = "DELETE FROM " + Config.getConfig(start).getUsersTable()
     * + " WHERE user_id = ? AND recipe_id = ?";
     */
    public int deleteWhereUserIdEqualsAndRecipeIdEquals(String userId, String recipeId);
}
