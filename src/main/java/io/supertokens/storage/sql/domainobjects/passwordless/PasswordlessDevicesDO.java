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

package io.supertokens.storage.sql.domainobjects.passwordless;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@AllArgsConstructor
@Table(name = "passwordless_devices", indexes = { @Index(columnList = "email"), @Index(columnList = "phone_number") })
@org.hibernate.annotations.NamedQuery(name = "PasswordlessDevicesDO_deleteWhereEmailEquals", query = "DELETE FROM PasswordlessDevicesDO WHERE email = :email")
public class PasswordlessDevicesDO {

    @Deprecated
    public PasswordlessDevicesDO() {
        // do nothing, required by hibernate as part of entity class
    }

    @Id
    @Column(length = 44)
    String device_id_hash;

    @Column(length = 256)
    String email;

    @Column(length = 256)
    String phone_number;

    @Column(nullable = false, length = 44)
    String link_code_salt;

    @Column(columnDefinition = "INT  NOT NULL")
    int failed_attempts;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "device")
    @Cascade({ org.hibernate.annotations.CascadeType.DELETE, org.hibernate.annotations.CascadeType.SAVE_UPDATE })
    List<PasswordlessCodesDO> codes;
}
