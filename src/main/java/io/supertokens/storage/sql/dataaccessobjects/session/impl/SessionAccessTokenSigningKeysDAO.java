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

import io.supertokens.storage.sql.dataaccessobjects.SessionFactoryDAO;
import io.supertokens.storage.sql.dataaccessobjects.session.SessionAccessTokenSigningKeysInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordPswdResetTokensDO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordUsersDO;
import io.supertokens.storage.sql.domainobjects.session.SessionAccessTokenSigningKeysDO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

public class SessionAccessTokenSigningKeysDAO extends SessionFactoryDAO
        implements SessionAccessTokenSigningKeysInterfaceDAO {

    public SessionAccessTokenSigningKeysDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
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

        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<SessionAccessTokenSigningKeysDO> criteria = criteriaBuilder
                .createQuery(SessionAccessTokenSigningKeysDO.class);
        Root<SessionAccessTokenSigningKeysDO> root = criteria.from(SessionAccessTokenSigningKeysDO.class);
        criteria.select(root);
        Query<SessionAccessTokenSigningKeysDO> query = session.createQuery(criteria);
        List<SessionAccessTokenSigningKeysDO> results = query.getResultList();
        session.close();
        return results;

    }

    @Override
    public void removeWhereUserIdEquals(Object id) throws Exception {

    }

    @Override
    public void removeAll() {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<SessionAccessTokenSigningKeysDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(SessionAccessTokenSigningKeysDO.class);
        Root<SessionAccessTokenSigningKeysDO> root = criteriaDelete.from(SessionAccessTokenSigningKeysDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("created_at_time")));
        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();
    }

    @Override
    public long insertIntoTableValues(long createdAtTime, String value) {
        Session session = sessionFactory.openSession();

        SessionAccessTokenSigningKeysDO keysDO = new SessionAccessTokenSigningKeysDO(createdAtTime, value);

        Transaction transaction = session.beginTransaction();
        long createdAt = (long) session.save(keysDO);
        transaction.commit();
        session.close();

        return createdAt;

    }

    @Override
    public void deleteWhereCreatedAtTimeLessThan(long createdAtTime) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

        CriteriaDelete<SessionAccessTokenSigningKeysDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(SessionAccessTokenSigningKeysDO.class);
        Root<SessionAccessTokenSigningKeysDO> root = criteriaDelete.from(SessionAccessTokenSigningKeysDO.class);

        criteriaDelete.where(criteriaBuilder.lessThan(root.get("created_at_time"), createdAtTime));

        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();

    }
}
