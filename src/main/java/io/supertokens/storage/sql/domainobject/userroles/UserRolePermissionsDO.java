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

package io.supertokens.storage.sql.domainobject.userroles;

import io.supertokens.storage.sql.domainobject.PrimaryKeyFetchable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

//CREATE TABLE IF NOT EXISTS role_permissions (role VARCHAR(255) NOT NULL,
//permission VARCHAR(255) NOT NULL,CONSTRAINT role_permissions_pkey PRIMARY KEY(role, permission),
//CONSTRAINT role_permissions_role_fkey FOREIGN KEY(role) REFERENCES roles(role) ON DELETE CASCADE)

@Entity
@Table(name = "role_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRolePermissionsDO extends PrimaryKeyFetchable {

    @EmbeddedId
    private UserRolePermissionsPK pk;

    @Override
    public int hashCode() {
        return getPk().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof UserRolePermissionsDO) {
            UserRolePermissionsDO otherDO = (UserRolePermissionsDO) other;
            return otherDO.getPk().equals(this.getPk());
        }
        return false;
    }

    @Override
    public Serializable getPrimaryKey() {
        return pk;
    }
}
