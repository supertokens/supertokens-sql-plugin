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

package io.supertokens.storage.sql.domainobject.session;

import io.supertokens.storage.sql.domainobject.PrimaryKeyFetchable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

//CREATE TABLE IF NOT EXISTS session_info (session_handle VARCHAR(255) NOT NULL,
//user_id VARCHAR(128) NOT NULL,refresh_token_hash_2 VARCHAR(128) NOT NULL,
//session_data TEXT,expires_at BIGINT NOT NULL,created_at_time BIGINT NOT NULL,
//jwt_user_payload TEXT,CONSTRAINT session_info_pkey PRIMARY KEY(session_handle) );

@Entity
@Table(name = "session_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfoDO extends PrimaryKeyFetchable {

    @Id
    private String session_handle;

    @Column(length = 128, nullable = false)
    private String user_id;

    @Column(length = 128, nullable = false)
    private String refresh_token_hash_2;

    @Column(columnDefinition = "TEXT")
    private String session_data;

    @Column(columnDefinition = "TEXT")
    private String jwt_user_payload;

    @Column(nullable = false)
    private long expires_at;

    @Column(nullable = false)
    private long created_at_time;

    @Override
    public boolean equals(Object other) {
        if (other instanceof SessionInfoDO) {
            SessionInfoDO otherDO = (SessionInfoDO) other;
            return otherDO.getSession_handle().equals(this.getSession_handle());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getSession_handle().hashCode();
    }

    @Override
    public Serializable getPrimaryKey() {
        return session_handle;
    }

}
