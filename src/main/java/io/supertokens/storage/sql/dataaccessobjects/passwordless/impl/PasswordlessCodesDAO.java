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

import io.supertokens.storage.sql.dataaccessobjects.SessionFactoryDAO;
import io.supertokens.storage.sql.dataaccessobjects.passwordless.PasswordlessCodesInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationTokensDO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessCodesDO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessDevicesDO;
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

public class PasswordlessCodesDAO extends SessionFactoryDAO implements PasswordlessCodesInterfaceDAO {

    public PasswordlessCodesDAO(SessionFactory sessionFactory) {
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
        CriteriaQuery<PasswordlessCodesDO> criteria = criteriaBuilder.createQuery(PasswordlessCodesDO.class);
        Root<PasswordlessCodesDO> root = criteria.from(PasswordlessCodesDO.class);
        criteria.select(root);
        Query<PasswordlessCodesDO> query = session.createQuery(criteria);
        List<PasswordlessCodesDO> results = query.getResultList();
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
        CriteriaDelete<PasswordlessCodesDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(PasswordlessCodesDO.class);
        Root<PasswordlessCodesDO> root = criteriaDelete.from(PasswordlessCodesDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("code_id")));
        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();
    }

    @Override
    public String insertIntoTableValues(String codeId, PasswordlessDevicesDO deviceId, String linkCodeHash,
            long createdAt) {
        PasswordlessCodesDO codesDO = new PasswordlessCodesDO(codeId, deviceId, linkCodeHash, createdAt);

        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        String id = (String) session.save(codesDO);
        transaction.commit();
        session.close();
        return id;
    }

    @Override
    public List<PasswordlessCodesDO> getCodesWhereDeviceIdHashEquals(PasswordlessDevicesDO devicesDO) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PasswordlessCodesDO> criteria = criteriaBuilder.createQuery(PasswordlessCodesDO.class);
        Root<PasswordlessCodesDO> root = criteria.from(PasswordlessCodesDO.class);
        criteria.select(root);
        criteria.where(criteriaBuilder.equal(root.get("device"), devicesDO));
        Query<PasswordlessCodesDO> query = session.createQuery(criteria);
        List<PasswordlessCodesDO> results = query.getResultList();
        session.close();

        if (results.size() == 0)
            throw new NoResultException();

        return results;
    }

    @Override
    public PasswordlessCodesDO getWhereLinkCodeHashEquals(String linkCodeHash) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PasswordlessCodesDO> criteria = criteriaBuilder.createQuery(PasswordlessCodesDO.class);
        Root<PasswordlessCodesDO> root = criteria.from(PasswordlessCodesDO.class);
        criteria.select(root);
        criteria.where(criteriaBuilder.equal(root.get("link_code_hash"), linkCodeHash));
        Query<PasswordlessCodesDO> query = session.createQuery(criteria);
        PasswordlessCodesDO result = query.getSingleResult();
        session.close();

        return result;
    }

    @Override
    public void deleteWhereCodeIdEquals(String codeId) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<PasswordlessCodesDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(PasswordlessCodesDO.class);
        Root<PasswordlessCodesDO> root = criteriaDelete.from(PasswordlessCodesDO.class);
        criteriaDelete.where(criteriaBuilder.equal(root.get("code_id"), codeId));
        Transaction transaction = session.beginTransaction();
        int rowsUpdated = session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();

        if (rowsUpdated == 0)
            throw new NoResultException();
    }

    @Override
    public List<PasswordlessCodesDO> getCodesWhereCreatedAtLessThan(long createdAt) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PasswordlessCodesDO> criteria = criteriaBuilder.createQuery(PasswordlessCodesDO.class);
        Root<PasswordlessCodesDO> root = criteria.from(PasswordlessCodesDO.class);
        criteria.select(root);
        criteria.where(criteriaBuilder.lessThan(root.get("created_at"), createdAt));
        Query<PasswordlessCodesDO> query = session.createQuery(criteria);
        List<PasswordlessCodesDO> result = query.getResultList();
        session.close();

        if (result.size() == 0)
            throw new NoResultException();

        return result;
    }

    @Override
    public PasswordlessCodesDO getCodeWhereCodeIdEquals(String codeId) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PasswordlessCodesDO> criteria = criteriaBuilder.createQuery(PasswordlessCodesDO.class);
        Root<PasswordlessCodesDO> root = criteria.from(PasswordlessCodesDO.class);
        criteria.select(root);
        criteria.where(criteriaBuilder.equal(root.get("code_id"), codeId));
        Query<PasswordlessCodesDO> query = session.createQuery(criteria);
        PasswordlessCodesDO result = query.getSingleResult();
        session.close();

        return result;
    }
}
