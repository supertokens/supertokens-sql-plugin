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

package io.supertokens.storage.sql.dataaccessobjects.passwordless.impl;

import io.supertokens.pluginInterface.sqlStorage.SessionObject;
import io.supertokens.storage.sql.dataaccessobjects.SessionTransactionDAO;
import io.supertokens.storage.sql.dataaccessobjects.passwordless.UsersInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordUsersDO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationTokensDO;
import io.supertokens.storage.sql.domainobjects.general.UsersDO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.jetbrains.annotations.TestOnly;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

public class UsersDAO extends SessionTransactionDAO implements UsersInterfaceDAO {

    public UsersDAO(SessionObject sessionInstance) {
        super(sessionInstance);
    }

    @Override
    public Serializable create(Object entity) throws Exception {
        return null;
    }

    @Override
    public Object getWherePrimaryKeyEquals(Object id) throws Exception {
        return null;
    }

    @Override
    public List getAll() {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<UsersDO> criteria = criteriaBuilder.createQuery(UsersDO.class);
        Root<UsersDO> root = criteria.from(UsersDO.class);
        criteria.select(root);
        Query<UsersDO> query = session.createQuery(criteria);
        List<UsersDO> results = query.getResultList();
        return results;
    }

    @Override
    public int deleteWherePrimaryKeyEquals(Object id) throws Exception {
        return 0;
    }

    @TestOnly
    @Override
    public void removeAll() {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<UsersDO> criteriaDelete = criteriaBuilder.createCriteriaDelete(UsersDO.class);
        Root root = criteriaDelete.from(UsersDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("user_id")));
        session.createQuery(criteriaDelete).executeUpdate();
    }

    @Override
    public UsersDO insertIntoTableValues(String userId, String recipeId, long timeJoined) {
        UsersDO usersDO = new UsersDO(userId, recipeId, timeJoined);
        sessionInstance.save(usersDO);
        return usersDO;
    }

    @Override
    public int deleteWhereUserIdEqualsAndRecipeIdEquals(String userId, String recipeId) {
        CriteriaBuilder criteriaBuilder = sessionInstance.getCriteriaBuilder();
        CriteriaDelete criteriaDelete = criteriaBuilder.createCriteriaDelete(UsersDO.class);
        Root<UsersDO> root = criteriaDelete.from(UsersDO.class);
        criteriaDelete.where(criteriaBuilder.and(criteriaBuilder.equal(root.get("user_id"), userId),
                criteriaBuilder.equal(root.get("recipe_id"), recipeId)));
        return sessionInstance.createQuery(criteriaDelete).executeUpdate();
    }
}
