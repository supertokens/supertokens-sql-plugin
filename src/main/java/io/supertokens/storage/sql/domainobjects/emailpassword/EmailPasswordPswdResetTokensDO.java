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

package io.supertokens.storage.sql.domainobjects.emailpassword;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "emailpassword_pswd_reset_tokens", indexes = { @Index(columnList = "token_expiry") })
public class EmailPasswordPswdResetTokensDO {

//    return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getPasswordResetTokensTable() + " ("
//            + "user_id CHAR(36) NOT NULL," + "token VARCHAR(128) NOT NULL UNIQUE,"
//            + "token_expiry BIGINT  NOT NULL," + "PRIMARY KEY (user_id, token),"
//            + "FOREIGN KEY (user_id) REFERENCES " + Config.getConfig(start).getEmailPasswordUsersTable()
//                + "(user_id) ON DELETE CASCADE ON UPDATE CASCADE);";

//    return "CREATE INDEX emailpassword_password_reset_token_expiry_index ON "
//            + Config.getConfig(start).getPasswordResetTokensTable() + "(token_expiry);";
//

    @EmbeddedId
    private EmailPasswordPswdResetTokensPKDO primaryKey;

    @Column(nullable = false)
    private long token_expiry;

}
