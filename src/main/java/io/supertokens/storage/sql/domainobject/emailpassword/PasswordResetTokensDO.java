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

import io.supertokens.storage.sql.domainobject.PrimaryKeyFetchable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/*
CREATE TABLE IF NOT EXISTS emailpassword_pswd_reset_tokens (
    user_id CHAR(36) NOT NULL,
    token VARCHAR(128) NOT NULL CONSTRAINT UNIQUE,
    token_expiry BIGINT NOT NULL,
    PRIMARY KEY (user_id, token),
    FOREIGN KEY (user_id) REFERENCES emailpassword_users(user_id),
    ON DELETE CASCADE ON UPDATE CASCADE);
);
*/

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "emailpassword_pswd_reset_tokens")
public class PasswordResetTokensDO extends PrimaryKeyFetchable {

    @EmbeddedId
    private PasswordResetTokensPK pk;

    @Column(nullable = false)
    private long token_expiry;

    @Override
    public boolean equals(Object other) {
        if (other instanceof PasswordResetTokensDO) {
            PasswordResetTokensDO otherKeyValue = (PasswordResetTokensDO) other;
            return otherKeyValue.getPk().equals(this.getPk());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getPk().hashCode();
    }

    @Override
    public Serializable getPrimaryKey() {
        return this.getPk();
    }
}
