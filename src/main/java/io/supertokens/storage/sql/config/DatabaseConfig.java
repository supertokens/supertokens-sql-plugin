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

package io.supertokens.storage.sql.config;

public interface DatabaseConfig {

    int getConnectionPoolSize();

    String getConnectionScheme();

    String getConnectionAttributes();

    String getHostName();

    int getPort();

    String getUser();

    String getPassword();

    String getDatabaseName();

    String getConnectionURI();

    String getTableSchema();

    String getUsersTable();

    String getKeyValueTable();

    String getAccessTokenSigningKeysTable();

    String getSessionInfoTable();

    String getEmailPasswordUsersTable();

    String getPasswordResetTokensTable();

    String getEmailVerificationTokensTable();

    String getEmailVerificationTable();

    String getThirdPartyUsersTable();

    String getPasswordlessUsersTable();

    String getPasswordlessDevicesTable();

    String getPasswordlessCodesTable();

    String getJWTSigningKeysTable();

    String getUserMetadataTable();

    String addSchemaAndPrefixToTableName(String tableName);

    void validateAndInitialise();

    String getDialect();

    String getDriverClassName();

}
