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

package io.supertokens.storage.sql.domainobjects.session;

import io.supertokens.storage.sql.config.Config;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "session_info")
public class SessionInfoDO {

//     return "CREATE TABLE IF NOT EXISTS " + Config.getConfig(start).getSessionInfoTable() + " ("
//            + "session_handle VARCHAR(255) NOT NULL," + "user_id VARCHAR(128) NOT NULL,"
//            + "refresh_token_hash_2 VARCHAR(128) NOT NULL," + "session_data TEXT,"
//            + "expires_at BIGINT  NOT NULL," + "created_at_time BIGINT  NOT NULL,"
//            + "jwt_user_payload TEXT," + "PRIMARY KEY(session_handle)" + " );";

    @Id
    @Column(length = 255, nullable = false)
    private String session_handle;

    @Column(length = 128, nullable = false)
    private String user_id;

    @Column(length = 128, nullable = false)
    private String refresh_token_hash_2;

    @Column
    @Type(type = "text")
    private String sessions_data;

    @Column(nullable = false)
    private BigInteger expires_at;

    @Column(nullable = false)
    private BigInteger created_at_time;

    @Column
    @Type(type = "text")
    private String jwt_user_payload;
}
