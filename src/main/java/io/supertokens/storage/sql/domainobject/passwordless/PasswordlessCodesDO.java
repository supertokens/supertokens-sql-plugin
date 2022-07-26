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
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;

//CREATE TABLE IF NOT EXISTS passwordless_codes (code_id CHAR(36) NOT NULL,device_id_hash CHAR(44) NOT NULL,
// link_code_hash CHAR(44) NOT NULL CONSTRAINT passwordless_codes_link_code_hash_key UNIQUE,created_at BIGINT NOT
// NULL,CONSTRAINT passwordless_codes_pkey PRIMARY KEY (code_id),CONSTRAINT passwordless_codes_device_id_hash_fkey
// FOREIGN KEY (device_id_hash) REFERENCES passwordless_devices(device_id_hash) ON DELETE CASCADE ON UPDATE CASCADE);

@Entity
@Table(name = "passwordless_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordlessCodesDO extends PrimaryKeyFetchable {

    @Id
    @Column(length = 36)
    private String code_id;

    @Getter(AccessLevel.PRIVATE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id_hash", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PasswordlessDevicesDO passwordlessDevice;

    @Column(length = 44, nullable = false, unique = true)
    private String link_code_hash;

    @Column(nullable = false)
    private long created_at;

    public String getDevice_id_hash() {
        return passwordlessDevice.getDevice_id_hash();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof PasswordlessCodesDO) {
            PasswordlessCodesDO otherDO = (PasswordlessCodesDO) other;
            return otherDO.getCode_id().equals(this.getCode_id());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getCode_id().hashCode();
    }

    @Override
    public Serializable getPrimaryKey() {
        return code_id;
    }
}
