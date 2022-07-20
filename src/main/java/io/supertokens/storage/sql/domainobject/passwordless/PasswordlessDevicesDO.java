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

package io.supertokens.storage.sql.domainobject.passwordless;

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

@Entity
@Table(name = "passwordless_devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordlessDevicesDO extends PrimaryKeyFetchable {

    @Id
    @Column(length = 44)
    private String device_id_hash;

    @Column(length = 256)
    private String email;

    @Column(length = 256)
    private String phone_number;

    @Column(length = 44, nullable = false)
    private String link_code_salt;

    @Column(nullable = false)
    private int failed_attempts;

    @Override
    public boolean equals(Object other) {
        if (other instanceof PasswordlessDevicesDO) {
            PasswordlessDevicesDO otherDO = (PasswordlessDevicesDO) other;
            return otherDO.getDevice_id_hash().equals(this.getDevice_id_hash());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getDevice_id_hash().hashCode();
    }


    @Override
    public Serializable getPrimaryKey() {
        return device_id_hash;
    }
}
