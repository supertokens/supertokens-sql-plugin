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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
public class PasswordResetTokensPK implements Serializable {

    @Getter(value = AccessLevel.PRIVATE)
    @ManyToOne(fetch = FetchType.LAZY)
    // we use FetchType.LAZY so that when we fetch Password reset tokens, we don't also unnecessarily end up fetching
    // email password users table
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EmailPasswordUsersDO user;
    // TODO: sql-plugin -> when fetching this, hibernate doesn't actually set the user_id inside this object, but if
    // we call .getUser_id() function, it returns it correctly.. how?

    @Column(length = 128, nullable = false, unique = true)
    private String token;

    public String getUserId() {
        return user.getUser_id();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof PasswordResetTokensPK) {
            PasswordResetTokensPK otherKeyValue = (PasswordResetTokensPK) other;
            return otherKeyValue.getUser().equals(this.getUser()) && otherKeyValue.getToken().equals(getToken());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (getUser().getUser_id() + getToken()).hashCode();
    }
}
