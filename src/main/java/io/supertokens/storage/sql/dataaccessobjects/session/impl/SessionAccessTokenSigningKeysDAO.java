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

package io.supertokens.storage.sql.dataaccessobjects.session.impl;

import io.supertokens.storage.sql.dataaccessobjects.SessionTransactionDAO;
import io.supertokens.storage.sql.dataaccessobjects.session.SessionAccessTokenSigningKeysInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.session.SessionAccessTokenSigningKeysDO;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

public class SessionAccessTokenSigningKeysDAO extends SessionTransactionDAO
        implements SessionAccessTokenSigningKeysInterfaceDAO {

    public SessionAccessTokenSigningKeysDAO(Session sessionInstance) {
        super(sessionInstance);
    }

    @Override
    public Serializable create(Object entity) throws Exception {
        return null;
    }

    @Override
    public Object get(Object id) throws Exception {
        return null;
    }

    @Override
    public List getAll() {

        Session session = (Session) sessionInstance.getSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<SessionAccessTokenSigningKeysDO> criteria = criteriaBuilder
                .createQuery(SessionAccessTokenSigningKeysDO.class);
        Root<SessionAccessTokenSigningKeysDO> root = criteria.from(SessionAccessTokenSigningKeysDO.class);
        criteria.select(root);
        Query<SessionAccessTokenSigningKeysDO> query = session.createQuery(criteria);
        List<SessionAccessTokenSigningKeysDO> results = query.getResultList();
        return results;

    }

    @Override
    public int removeWhereUserIdEquals(Object id) throws Exception {
        return 0;
    }

    @Override
    public void removeAll() {
        Session session = (Session) sessionInstance.getSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<SessionAccessTokenSigningKeysDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(SessionAccessTokenSigningKeysDO.class);
        Root<SessionAccessTokenSigningKeysDO> root = criteriaDelete.from(SessionAccessTokenSigningKeysDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("created_at_time")));
        session.createQuery(criteriaDelete).executeUpdate();
    }

    @Override
    public long insertIntoTableValues(long createdAtTime, String value) {
        Session session = (Session) sessionInstance.getSession();

        SessionAccessTokenSigningKeysDO keysDO = new SessionAccessTokenSigningKeysDO(createdAtTime, value);

        long createdAt = (long) session.save(keysDO);

        return createdAt;

    }

    @Override
    public void deleteWhereCreatedAtTimeLessThan(long createdAtTime) {
        Session session = (Session) sessionInstance.getSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

        CriteriaDelete<SessionAccessTokenSigningKeysDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(SessionAccessTokenSigningKeysDO.class);
        Root<SessionAccessTokenSigningKeysDO> root = criteriaDelete.from(SessionAccessTokenSigningKeysDO.class);

        criteriaDelete.where(criteriaBuilder.lessThan(root.get("created_at_time"), createdAtTime));

        session.createQuery(criteriaDelete).executeUpdate();

    }
}
