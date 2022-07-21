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

package io.supertokens.storage.sql.domainobject.emailverification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EmailVerificationUsersPK implements Serializable {

    @Column(length = 128, nullable = false)
    private String user_id;

    @Column(length = 256, nullable = false)
    private String email;

    @Override
    public boolean equals(Object other) {
        if (other instanceof EmailVerificationUsersPK) {
            EmailVerificationUsersPK otherKeyValue = (EmailVerificationUsersPK) other;
            return otherKeyValue.getUser_id().equals(this.getUser_id()) && otherKeyValue.getEmail().equals(getEmail());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (getEmail() + getUser_id()).hashCode();
    }
}
