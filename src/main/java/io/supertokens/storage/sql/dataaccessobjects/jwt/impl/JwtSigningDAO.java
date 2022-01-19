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

package io.supertokens.storage.sql.dataaccessobjects.jwt.impl;

import io.supertokens.storage.sql.dataaccessobjects.SessionFactoryDAO;
import io.supertokens.storage.sql.dataaccessobjects.jwt.JwtSigningInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.jwtsigning.JWTSigningKeysDO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.LockModeType;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JwtSigningDAO extends SessionFactoryDAO implements JwtSigningInterfaceDAO {

    public JwtSigningDAO(SessionFactory sessionFactory) {
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
        CriteriaQuery<JWTSigningKeysDO> criteria = criteriaBuilder.createQuery(JWTSigningKeysDO.class);
        Root<JWTSigningKeysDO> root = criteria.from(JWTSigningKeysDO.class);
        criteria.select(root);
        Query<JWTSigningKeysDO> query = session.createQuery(criteria);
        List<JWTSigningKeysDO> results = query.getResultList();
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
        CriteriaDelete<JWTSigningKeysDO> criteriaDelete = criteriaBuilder.createCriteriaDelete(JWTSigningKeysDO.class);
        Root<JWTSigningKeysDO> root = criteriaDelete.from(JWTSigningKeysDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("key_id")));
        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();

    }

    @Override
    public Serializable insert(String keyId, String keyString, String algorithm, long createdAt) {

        JWTSigningKeysDO jwtSigningKeysDO = new JWTSigningKeysDO(keyId, keyString, algorithm, createdAt);

        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        String id = (String) session.save(jwtSigningKeysDO);
        transaction.commit();
        session.close();

        return id;

    }

    @Override
    public List<JWTSigningKeysDO> getAllOrderByCreatedAtDesc_locked() {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<JWTSigningKeysDO> criteria = criteriaBuilder.createQuery(JWTSigningKeysDO.class);
        Root<JWTSigningKeysDO> root = criteria.from(JWTSigningKeysDO.class);
        criteria.select(root);

        List<Order> orderList = new ArrayList();
        orderList.add(criteriaBuilder.desc(root.get("created_at")));
        criteria.orderBy(orderList);

        Query<JWTSigningKeysDO> query = session.createQuery(criteria);
        Transaction transaction = session.beginTransaction();
        List<JWTSigningKeysDO> results = query.setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
        transaction.commit();
        session.close();
        return results;
    }
}
