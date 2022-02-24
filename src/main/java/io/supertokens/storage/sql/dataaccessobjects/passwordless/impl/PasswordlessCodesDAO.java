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
import io.supertokens.storage.sql.dataaccessobjects.passwordless.PasswordlessCodesInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessCodesDO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessDevicesDO;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jetbrains.annotations.TestOnly;

import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

public class PasswordlessCodesDAO extends SessionTransactionDAO implements PasswordlessCodesInterfaceDAO {

    public PasswordlessCodesDAO(SessionObject sessionInstance) {
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
        CriteriaQuery<PasswordlessCodesDO> criteria = criteriaBuilder.createQuery(PasswordlessCodesDO.class);
        Root<PasswordlessCodesDO> root = criteria.from(PasswordlessCodesDO.class);
        criteria.select(root);
        Query<PasswordlessCodesDO> query = session.createQuery(criteria);
        List<PasswordlessCodesDO> results = query.getResultList();
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
        CriteriaDelete<PasswordlessCodesDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(PasswordlessCodesDO.class);
        Root<PasswordlessCodesDO> root = criteriaDelete.from(PasswordlessCodesDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("code_id")));
        session.createQuery(criteriaDelete).executeUpdate();
    }

    @Override
    public String insertIntoTableValues(String codeId, PasswordlessDevicesDO deviceId, String linkCodeHash,
            long createdAt) {

        PasswordlessCodesDO codesDO = new PasswordlessCodesDO(codeId, deviceId, linkCodeHash, createdAt);

        Session session = (Session) sessionInstance;
        String id = (String) session.save(codesDO);
        return id;
    }

    @Override
    public List<PasswordlessCodesDO> getCodesWhereDeviceIdHashEquals(PasswordlessDevicesDO devicesDO) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PasswordlessCodesDO> criteria = criteriaBuilder.createQuery(PasswordlessCodesDO.class);
        Root<PasswordlessCodesDO> root = criteria.from(PasswordlessCodesDO.class);
        criteria.select(root);
        criteria.where(criteriaBuilder.equal(root.get("device"), devicesDO));
        Query<PasswordlessCodesDO> query = session.createQuery(criteria);
        List<PasswordlessCodesDO> results = query.getResultList();

        return results;
    }

    @Override
    public PasswordlessCodesDO getWhereLinkCodeHashEquals(String linkCodeHash) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PasswordlessCodesDO> criteria = criteriaBuilder.createQuery(PasswordlessCodesDO.class);
        Root<PasswordlessCodesDO> root = criteria.from(PasswordlessCodesDO.class);
        criteria.select(root);
        criteria.where(criteriaBuilder.equal(root.get("link_code_hash"), linkCodeHash));
        Query<PasswordlessCodesDO> query = session.createQuery(criteria);
        PasswordlessCodesDO result = null;
        try {
            result = query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
        return result;
    }

    @Override
    public int deleteWhereCodeIdEquals(String codeId) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<PasswordlessCodesDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(PasswordlessCodesDO.class);
        Root<PasswordlessCodesDO> root = criteriaDelete.from(PasswordlessCodesDO.class);
        criteriaDelete.where(criteriaBuilder.equal(root.get("code_id"), codeId));
        return session.createQuery(criteriaDelete).executeUpdate();

    }

    @Override
    public List<PasswordlessCodesDO> getCodesWhereCreatedAtLessThan(long createdAt) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PasswordlessCodesDO> criteria = criteriaBuilder.createQuery(PasswordlessCodesDO.class);
        Root<PasswordlessCodesDO> root = criteria.from(PasswordlessCodesDO.class);
        criteria.select(root);
        criteria.where(criteriaBuilder.lessThan(root.get("created_at"), createdAt));
        Query<PasswordlessCodesDO> query = session.createQuery(criteria);
        List<PasswordlessCodesDO> result = query.getResultList();

        return result;
    }

    @Override
    public PasswordlessCodesDO getCodeWhereCodeIdEquals(String codeId) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PasswordlessCodesDO> criteria = criteriaBuilder.createQuery(PasswordlessCodesDO.class);
        Root<PasswordlessCodesDO> root = criteria.from(PasswordlessCodesDO.class);
        criteria.select(root);
        criteria.where(criteriaBuilder.equal(root.get("code_id"), codeId));
        Query<PasswordlessCodesDO> query = session.createQuery(criteria);
        PasswordlessCodesDO result = null;
        try {
            result = query.getSingleResult();
        } catch (NoResultException | EntityNotFoundException e) {
            return null;
        }
        return result;
    }
}
