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

package io.supertokens.storage.sql.queries;

import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.storage.sql.ConnectionPool;
import io.supertokens.storage.sql.Start;
import io.supertokens.storage.sql.domainobject.userroles.*;
import io.supertokens.storage.sql.hibernate.CustomQueryWrapper;
import io.supertokens.storage.sql.hibernate.CustomSessionWrapper;
import io.supertokens.storage.sql.utils.Utils;

import javax.persistence.LockModeType;
import java.sql.SQLException;

import static io.supertokens.storage.sql.config.Config.getConfig;

public class UserRolesQueries {
    public static String getQueryToCreateRolesTable(Start start) {
        String schema = getConfig(start).getTableSchema();
        String tableName = getConfig(start).getRolesTable();
        // @formatter:off
        return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "role VARCHAR(255) NOT NULL,"
                + "CONSTRAINT " + Utils.getConstraintName(schema, tableName, null, "pkey") + " PRIMARY KEY(role)" +
                " );";

        // @formatter:on
    }

    public static String getQueryToCreateRolePermissionsTable(Start start) {
        String tableName = getConfig(start).getUserRolesPermissionsTable();
        String schema = getConfig(start).getTableSchema();
        // @formatter:off
        return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "role VARCHAR(255) NOT NULL,"
                + "permission VARCHAR(255) NOT NULL,"
                + "CONSTRAINT " + Utils.getConstraintName(schema, tableName, null, "pkey") +
                " PRIMARY KEY(role, permission),"
                + "CONSTRAINT " + Utils.getConstraintName(schema, tableName, "role", "fkey") + " FOREIGN KEY(role)"
                + " REFERENCES " + getConfig(start).getRolesTable()
                + "(role) ON DELETE CASCADE );";

        // @formatter:on
    }

    static String getQueryToCreateRolePermissionsPermissionIndex(Start start) {
        return "CREATE INDEX role_permissions_permission_index ON " + getConfig(start).getUserRolesPermissionsTable()
                + "(permission);";
    }

    public static String getQueryToCreateUserRolesTable(Start start) {
        String schema = getConfig(start).getTableSchema();
        String tableName = getConfig(start).getUserRolesTable();
        // @formatter:off
        return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "user_id VARCHAR(128) NOT NULL,"
                + "role VARCHAR(255) NOT NULL,"
                + "CONSTRAINT " + Utils.getConstraintName(schema, tableName, null, "pkey") +
                " PRIMARY KEY(user_id, role),"
                + "CONSTRAINT " + Utils.getConstraintName(schema, tableName, "role", "fkey") + " FOREIGN KEY(role)"
                + " REFERENCES " + getConfig(start).getRolesTable()
                + "(role) ON DELETE CASCADE );";

        // @formatter:on
    }

    public static String getQueryToCreateUserRolesRoleIndex(Start start) {
        return "CREATE INDEX user_roles_role_index ON " + getConfig(start).getUserRolesTable() + "(role);";
    }

    public static boolean createNewRoleOrDoNothingIfExists_Transaction(CustomSessionWrapper session, String role)
            throws SQLException {

        RolesDO toInsertOrUpdate = session.get(RolesDO.class, role);
        if (toInsertOrUpdate == null) {
            toInsertOrUpdate = new RolesDO(role);

            session.save(UserRolePermissionsDO.class, role, toInsertOrUpdate);

            return true;
        }

        return false;
    }

    public static void addPermissionToRoleOrDoNothingIfExists_Transaction(CustomSessionWrapper session, String role,
            String permission) throws SQLException {

        final UserRolePermissionsPK pk = new UserRolePermissionsPK(new RolesDO(role), permission);
        UserRolePermissionsDO toInsertOrUpdate = session.get(UserRolePermissionsDO.class, pk);
        if (toInsertOrUpdate == null) {
            toInsertOrUpdate = new UserRolePermissionsDO(pk);
            session.save(UserRolePermissionsDO.class, pk, toInsertOrUpdate);
        }
    }

    public static boolean deleteRole(Start start, String role) throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "DELETE FROM RolesDO WHERE role = :role";
            final CustomQueryWrapper query = session.createQuery(QUERY);
            query.setParameter("role", role);

            return query.executeUpdate() == 1;
        }, true);
    }

    public static boolean doesRoleExist(Start start, String role) throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT 1 FROM RolesDO entity WHERE entity.role = :role";

            final CustomQueryWrapper<Integer> query = session.createQuery(QUERY, Integer.class);
            query.setParameter("role", role);

            return !query.list().isEmpty();
        }, false);
    }

    public static String[] getPermissionsForRole(Start start, String role) throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity.pk.permission FROM UserRolePermissionsDO entity"
                    + " WHERE entity.pk.userRole.role = :role";

            final CustomQueryWrapper<String> query = session.createQuery(QUERY, String.class);
            query.setParameter("role", role);

            return query.list().toArray(String[]::new);
        }, false);
    }

    public static String[] getRoles(Start start) throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity.role FROM RolesDO entity";

            final CustomQueryWrapper<String> query = session.createQuery(QUERY, String.class);

            return query.list().toArray(String[]::new);
        }, false);
    }

    public static void addRoleToUser(Start start, String userId, String role)
            throws SQLException, StorageQueryException {
        ConnectionPool.withSession(start, (session, con) -> {
            // might need to find role before commit
            final UserRolesPK pk = new UserRolesPK(new RolesDO(role), userId);
            final UserRolesDO userRolesDO = new UserRolesDO(pk);

            session.save(UserRolesDO.class, pk, userRolesDO);

            return null;
        }, true);
    }

    public static String[] getRolesForUser(Start start, String userId) throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity.pk.userRole.role FROM UserRolesDO entity WHERE entity.pk.user_id = :user_id";

            final CustomQueryWrapper<String> query = session.createQuery(QUERY, String.class);
            query.setParameter("user_id", userId);

            return query.list().toArray(String[]::new);
        }, false);
    }

    public static boolean deleteRoleForUser_Transaction(CustomSessionWrapper session, String userId, String role)
            throws SQLException {
        String QUERY = "DELETE FROM UserRolesDO entity WHERE entity.pk.user_id = :user_id "
                + "AND entity.pk.userRole.role = :role";

        final CustomQueryWrapper query = session.createQuery(QUERY);
        query.setParameter("user_id", userId);
        query.setParameter("role", role);

        return query.executeUpdate() > 0;
    }

    public static boolean doesRoleExist_transaction(CustomSessionWrapper session, String role) throws SQLException {
        String QUERY = "SELECT 1 FROM RolesDO entity WHERE entity.role = :role";

        final CustomQueryWrapper<Integer> query = session.createQuery(QUERY, Integer.class);
        query.setParameter("role", role);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);

        return !query.list().isEmpty();
    }

    public static String[] getUsersForRole(Start start, String role) throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity.pk.user_id FROM UserRolesDO entity WHERE entity.pk.userRole.role = :role";

            final CustomQueryWrapper<String> query = session.createQuery(QUERY, String.class);
            query.setParameter("role", role);

            return query.list().toArray(String[]::new);
        }, false);
    }

    public static boolean deletePermissionForRole_Transaction(CustomSessionWrapper session, String role,
            String permission) throws SQLException {
        String QUERY = "DELETE FROM UserRolePermissionsDO entity WHERE entity.pk.userRole.role = :role AND entity.pk"
                + ".permission = :permission";

        final CustomQueryWrapper query = session.createQuery(QUERY);
        query.setParameter("role", role);
        query.setParameter("permission", permission);

        return query.executeUpdate() > 0;
    }

    public static int deleteAllPermissionsForRole_Transaction(CustomSessionWrapper session, String role)
            throws SQLException {
        String QUERY = "DELETE FROM UserRolePermissionsDO entity WHERE entity.pk.userRole.role = :role";

        final CustomQueryWrapper query = session.createQuery(QUERY);
        query.setParameter("role", role);

        return query.executeUpdate();
    }

    public static String[] getRolesThatHavePermission(Start start, String permission)
            throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "SELECT entity.pk.userRole.role FROM UserRolePermissionsDO entity WHERE entity.pk"
                    + ".permission = :permission";

            final CustomQueryWrapper<String> query = session.createQuery(QUERY, String.class);
            query.setParameter("permission", permission);

            return query.list().toArray(String[]::new);
        }, false);
    }

    public static int deleteAllRolesForUser(Start start, String userId) throws SQLException, StorageQueryException {
        return ConnectionPool.withSession(start, (session, con) -> {
            String QUERY = "DELETE FROM UserRolesDO entity WHERE entity.pk.user_id = :user_id";

            final CustomQueryWrapper query = session.createQuery(QUERY);
            query.setParameter("user_id", userId);

            return query.executeUpdate();
        }, true);
    }

}
