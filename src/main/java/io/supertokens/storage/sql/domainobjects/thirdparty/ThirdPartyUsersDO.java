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

package io.supertokens.storage.sql.domainobjects.thirdparty;

import io.supertokens.storage.sql.config.Config;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "thirdparty_users")
public class ThirdPartyUsersDO {

//     return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getThirdPartyUsersTable() + " ("
//            + "third_party_id VARCHAR(28) NOT NULL," + "third_party_user_id VARCHAR(128) NOT NULL,"
//            + "user_id CHAR(36) NOT NULL UNIQUE," + "email VARCHAR(256) NOT NULL,"
//            + "time_joined BIGINT  NOT NULL," + "PRIMARY KEY (third_party_id, third_party_user_id));";

    @Id
    @Column(length = 28)
    private String third_party_id;

    @Id
    @Column(length = 128)
    private String third_party_user_id;

    @Column(length = 36, unique = true, nullable = false)
    private String user_id;

    @Column(length = 256, nullable = false)
    private String email;

    @Column(nullable = false)
    private BigInteger time_joined;

}
