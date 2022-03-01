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

package io.supertokens.storage.sql;

import io.supertokens.storage.sql.config.SQLConfig;
import io.supertokens.storage.sql.singletons.ConfigObject;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * This naming strategy class is internally invoked by Hibernate to allow changing names
 * physically on the database
 */
public class CustomNamingStrategy implements PhysicalNamingStrategy {

    private final SQLConfig sqlConfig = ConfigObject.getSqlConfig();

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {

        String PREFIX = sqlConfig.getMysql_table_names_prefix();
        String legacyTableName = tableName(name.getText());

        if (legacyTableName != null) {
            return Identifier.toIdentifier(legacyTableName);
        }
        return Identifier.toIdentifier(PREFIX + name.getText());
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    /**
     * Following private methods help with legacy table renaming conventions
     */
    private String tableName(String name) {
        switch (name) {
        case "key_value":
            return sqlConfig.getKeyValueTable();
        case "session_info":
            return sqlConfig.getSessionInfoTable();
        case "emailpassword_users":
            return sqlConfig.getEmailPasswordUsersTable();
        case "emailpassword_pswd_reset_tokens":
            return sqlConfig.getPasswordResetTokensTable();
        case "emailverification_tokens":
            return sqlConfig.getEmailVerificationTokensTable();
        case "emailverification_verified_emails":
            return sqlConfig.getEmailVerificationTable();
        case "thirdparty_users":
            return sqlConfig.getThirdPartyUsersTable();
        default:
            return null;

        }
    }
}
