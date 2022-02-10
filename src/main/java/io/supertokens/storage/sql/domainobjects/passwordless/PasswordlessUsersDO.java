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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigInteger;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "passwordless_users")
public class PasswordlessUsersDO {

    /**
     * return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getPasswordlessUsersTable() + " ("
     * + "user_id CHAR(36) NOT NULL," + "email VARCHAR(256) UNIQUE," + "phone_number VARCHAR(256)
     * UNIQUE,"
     * + "time_joined BIGINT UNSIGNED NOT NULL," + "PRIMARY KEY (user_id));";
     */

    @Deprecated
    public PasswordlessUsersDO() {
        // do nothing, required by hibernate as part of entity class
    }

    @Id
    @Column(length = 36)
    String user_id;

    @Column(unique = true, length = 256)
    String email;

    @Column(unique = true, length = 256)
    String phone_number;

    @Column(columnDefinition = "BIGINT  NOT NULL")
    long time_joined;
}
