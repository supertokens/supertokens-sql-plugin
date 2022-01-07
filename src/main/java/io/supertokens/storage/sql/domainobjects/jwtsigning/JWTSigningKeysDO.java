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

package io.supertokens.storage.sql.domainobjects.jwtsigning;

import io.supertokens.storage.sql.config.Config;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "jwt_signing_keys")
public class JWTSigningKeysDO {

//        return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getJWTSigningKeysTable() + " ("
//            + "key_id VARCHAR(255) NOT NULL," + "key_string TEXT NOT NULL," + "algorithm VARCHAR(10) NOT NULL,"
//            + "created_at BIGINT ," + "PRIMARY KEY(key_id));";

    @Id
    @Column(length = 255)
    private String key_id;

    @Column(nullable = false)
    @Type(type = "text")
    private String key_string;

    @Column(length = 10, nullable = false)
    private String algorithm;

    @Column
    BigInteger created_at;

}
