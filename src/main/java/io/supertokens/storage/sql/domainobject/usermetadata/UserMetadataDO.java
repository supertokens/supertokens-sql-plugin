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

package io.supertokens.storage.sql.domainobject.usermetadata;

import io.supertokens.storage.sql.domainobject.PrimaryKeyFetchable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_metadata")
public class UserMetadataDO extends PrimaryKeyFetchable {

    @Id
    @Column(length = 128, nullable = false)
    private String user_id;

    @Column(columnDefinition = "TEXT")
    private String user_metadata;

    @Override
    public Serializable getPrimaryKey() {
        return getUser_id();
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof UserMetadataDO) {
            UserMetadataDO otherKeyValue = (UserMetadataDO) other;
            return otherKeyValue.getUser_id().equals(this.getUser_id());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getUser_id().hashCode();
    }
}
