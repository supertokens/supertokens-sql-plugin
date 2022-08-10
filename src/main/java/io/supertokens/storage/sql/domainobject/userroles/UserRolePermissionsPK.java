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

package io.supertokens.storage.sql.domainobject.userroles;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRolePermissionsPK implements Serializable {

    @Getter(value = AccessLevel.PRIVATE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private RolesDO userRole;

    @Column(length = 255, nullable = false)
    private String permission;

    public String getRole() {
        return userRole.getRole();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof UserRolePermissionsPK) {
            UserRolePermissionsPK otherDO = (UserRolePermissionsPK) other;

            return otherDO.getRole().equals(this.getRole()) && otherDO.getPermission().equals(getPermission());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (getRole() + getPermission()).hashCode();
    }

}
