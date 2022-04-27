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

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
public class PasswordResetTokensPK implements Serializable {

    @ManyToOne // TODO: sql-plugin -> should we us @ManyToOne(fetch = FetchType.LAZY) instead?
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EmailPasswordUsersDO user;

    @Column(length = 128, nullable = false, unique = true)
    private String token;

    @Override
    public boolean equals(Object other) {
        if (other instanceof PasswordResetTokensPK) {
            PasswordResetTokensPK otherKeyValue = (PasswordResetTokensPK) other;
            return otherKeyValue.user.equals(this.user) && otherKeyValue.token.equals(token);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (user.getUser_id() + token).hashCode();
    }
}
