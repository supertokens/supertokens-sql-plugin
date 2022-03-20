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

package io.supertokens.storage.sql.hibernate;

import io.supertokens.storage.sql.config.DatabaseConfig;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class CustomNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    private final DatabaseConfig databaseConfig;

    public CustomNamingStrategy(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        String legacyTableName = legacyTableName(name.getText());

        if (legacyTableName != null) {
            return Identifier.toIdentifier(legacyTableName);
        }

        final String PREFIX = databaseConfig.getTableNamePrefix();
        return Identifier.toIdentifier(PREFIX + name.getText());
    }

    /**
     * Following private methods help with legacy table renaming conventions
     */
    private String legacyTableName(String name) {
        return switch (name) {
        case "key_value" -> databaseConfig.getKeyValueTable();
        case "session_info" -> databaseConfig.getSessionInfoTable();
        case "emailpassword_users" -> databaseConfig.getEmailPasswordUsersTable();
        case "emailpassword_pswd_reset_tokens" -> databaseConfig.getPasswordResetTokensTable();
        case "emailverification_tokens" -> databaseConfig.getEmailVerificationTokensTable();
        case "emailverification_verified_emails" -> databaseConfig.getEmailVerificationTable();
        case "thirdparty_users" -> databaseConfig.getThirdPartyUsersTable();
        default -> null;
        };
    }
}
