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

package io.supertokens.storage.sql.domainobject.useridmapping;

import io.supertokens.storage.sql.domainobject.emailpassword.PasswordResetTokensPK;
import io.supertokens.storage.sql.domainobject.general.AllAuthRecipeUsersDO;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
public class UserIdMappingPK implements Serializable {

    @Serial
    private static final long serialVersionUID = 7136543407035119598L;

    @Getter(value = AccessLevel.PRIVATE)
    @ManyToOne(fetch = FetchType.LAZY)
    // we use FetchType.LAZY so that when we fetch userId mapping, we don't also unnecessarily end up fetching
    // all auth users table
    @JoinColumn(name = "supertokens_user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AllAuthRecipeUsersDO user;

    @Column(length = 128, nullable = false, unique = true)
    private String external_user_id;

    public String getSuperTokensUserId() {
        return user.getUser_id();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof UserIdMappingPK) {
            UserIdMappingPK otherKeyValue = (UserIdMappingPK) other;
            /*
             * We do not use getUser().equals here because if we do that, Hibernate will run an extra query to get
             * all user info from AllAuthRecipeUsersDO table
             */
            return otherKeyValue.getSuperTokensUserId().equals(this.getSuperTokensUserId())
                    && otherKeyValue.getExternal_user_id().equals(this.getExternal_user_id());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (getSuperTokensUserId() + getExternal_user_id()).hashCode();
    }
}