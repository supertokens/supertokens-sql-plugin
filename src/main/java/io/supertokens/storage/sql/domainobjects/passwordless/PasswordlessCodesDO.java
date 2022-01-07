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

package io.supertokens.storage.sql.domainobjects.passwordless;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.math.BigInteger;

//name = "passwordless_codes" ,
@Getter
@Setter
@Entity
@Table(indexes = { @Index(columnList = "created_at") })
public class PasswordlessCodesDO {

    @Id
    @Column(length = 36)
    String code_id;

    @ManyToOne(fetch = FetchType.EAGER)
    @Cascade({ CascadeType.SAVE_UPDATE, CascadeType.DELETE })
    @JoinColumn(name = "device_id_hash")
    PasswordlessDevicesDO device;

    @Column(length = 44, unique = true, nullable = false)
    String link_code_hash;

    @Column(columnDefinition = "BIGINT  NOT NULL")
    BigInteger created_at;

}
