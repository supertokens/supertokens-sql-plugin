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

package io.supertokens.storage.sql.domainobject.emailpassword;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/*
CREATE TABLE IF NOT EXISTS emailpassword_users(
    user_id CHAR(36) NOT NULL,
    email VARCHAR(256) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    time_joined BIGINT NOT NULL,
    PRIMARY KEY (user_id)
);
*/

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "emailpassword_users")
public class EmailPasswordUsersDO {

    @Id
    @Column(length = 36, nullable = false)
    private String user_id;

    @Column(length = 256, nullable = false, unique = true)
    private String email;

    @Column(length = 128, nullable = false)
    private String password_hash;

    @Column(nullable = false)
    private long time_joined;

    @Override
    public boolean equals(Object other) {
        if (other instanceof EmailPasswordUsersDO) {
            EmailPasswordUsersDO otherKeyValue = (EmailPasswordUsersDO) other;
            return otherKeyValue.user_id.equals(this.user_id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return user_id.hashCode();
    }
}
