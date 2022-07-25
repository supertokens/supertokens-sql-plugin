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

package io.supertokens.storage.sql.domainobject.useridmapping;

/*
CREATE TABLE IF NOT EXISTS userid_mapping (
    supertokens_user_id CHAR(36) NOT NULL UNIQUE,
    external_user_id VARCHAR(128) NOT NULL UNIQUE,
    external_user_id_info TEXT,
    PRIMARY KEY (supertokens_user_id, external_user_id),
    FOREIGN KEY (supertokens_user_id) REFERENCES all_auth_recipe_users (user_id) ON DELETE CASCADE ON UPDATE CASCADE
);
*/

import io.supertokens.storage.sql.domainobject.PrimaryKeyFetchable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "userid_mapping")
public class UserIdMappingDO extends PrimaryKeyFetchable {

    @EmbeddedId
    private UserIdMappingPK pk;

    @Column(columnDefinition = "TEXT")
    private String external_user_id_info;

    @Override
    public boolean equals(Object other) {
        if (other instanceof UserIdMappingDO) {
            UserIdMappingDO otherKeyValue = (UserIdMappingDO) other;
            return otherKeyValue.getPk().equals(this.getPk());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getPk().hashCode();
    }

    @Override
    public Serializable getPrimaryKey() {
        return this.getPk();
    }
}
