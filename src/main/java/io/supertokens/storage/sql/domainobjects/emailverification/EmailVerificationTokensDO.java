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

package io.supertokens.storage.sql.domainobjects.emailverification;

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
@Table(name = "emailverification_tokens", indexes = { @Index(columnList = "token_expiry") })
public class EmailVerificationTokensDO {

//    return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getEmailVerificationTokensTable() + " ("
//            + "user_id VARCHAR(128) NOT NULL," + "email VARCHAR(256) NOT NULL,"
//            + "token VARCHAR(128) NOT NULL UNIQUE," + "token_expiry BIGINT  NOT NULL,"
//            + "PRIMARY KEY (user_id, email, token))";

//    return "CREATE INDEX emailverification_tokens_index ON "
//            + Config.getConfig(start).getEmailVerificationTokensTable() + "(token_expiry);";

    @Id
    @Column(length = 128)
    private String user_id;

    @Id
    @Column(length = 256)
    private String email;

    @Id
    @Column(length = 128, unique = true)
    private String token;

    @Column(nullable = false)
    private BigInteger token_expiry;
}
