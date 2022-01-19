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
import io.supertokens.storage.sql.dataaccessobjects.session.SessionInfoInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.session.SessionInfoDO;
import io.supertokens.storage.sql.exceptions.SessionHandleNotFoundException;
import io.supertokens.storage.sql.exceptions.UserIdNotFoundException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public class SessionInfoDAO extends SessionFactoryDAO implements SessionInfoInterfaceDAO {

    public SessionInfoDAO(SessionFactory sessionFactory) {
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
        CriteriaQuery<SessionInfoDO> criteria = criteriaBuilder.createQuery(SessionInfoDO.class);
        Root<SessionInfoDO> root = criteria.from(SessionInfoDO.class);
        criteria.select(root);
        Query<SessionInfoDO> query = session.createQuery(criteria);
        List<SessionInfoDO> results = query.getResultList();
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
        CriteriaDelete<SessionInfoDO> criteriaDelete = criteriaBuilder.createCriteriaDelete(SessionInfoDO.class);
        Root<SessionInfoDO> root = criteriaDelete.from(SessionInfoDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("session_handle")));
        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();
    }

    @Override
    public Serializable insertIntoTableValues(String sessionHandle, String userId, String refreshTokenHashTwo,
            String sessionData, long expiresAt, long createdAtTime, String jwtUserPayload) {

        SessionInfoDO sessionInfoDO = new SessionInfoDO(sessionHandle, userId, refreshTokenHashTwo, sessionData,
                expiresAt, createdAtTime, jwtUserPayload);

        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        String id = (String) session.save(sessionInfoDO);
        transaction.commit();
        return id;
    }

    @Override
    public SessionInfoDO getWhereSessionHandleEquals_locked(String sessionHandle) throws NoResultException {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(SessionInfoDO.class);
        Root<SessionInfoDO> root = criteriaQuery.from(SessionInfoDO.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("session_handle"), sessionHandle));
        Query<SessionInfoDO> query = session.createQuery(criteriaQuery);
        Transaction transaction = session.beginTransaction();
        SessionInfoDO sessionInfoDO = query.setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
        transaction.commit();
        session.close();
        return sessionInfoDO;
    }

    @Override
    public void updateRefreshTokenTwoAndExpiresAtWhereSessionHandleEquals(String refreshTokenHashTwo, long expiresAt,
            String sessionHandle) throws SessionHandleNotFoundException {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

        CriteriaUpdate<SessionInfoDO> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(SessionInfoDO.class);
        Root<SessionInfoDO> root = criteriaUpdate.from(SessionInfoDO.class);
        criteriaUpdate.set(root.get("refresh_token_hash_2"), refreshTokenHashTwo);
        criteriaUpdate.set(root.get("expires_at"), expiresAt);

        criteriaUpdate.where(criteriaBuilder.equal(root.get("session_handle"), sessionHandle));

        Transaction transaction = session.beginTransaction();
        int rowsUpdated = session.createQuery(criteriaUpdate).executeUpdate();
        transaction.commit();
        session.close();

        if (rowsUpdated == 0)
            throw new SessionHandleNotFoundException("Session handle not found");

    }

    @Override
    public void deleteWhereUserIdEquals(String userId) throws UserIdNotFoundException {

        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<SessionInfoDO> criteriaDelete = criteriaBuilder.createCriteriaDelete(SessionInfoDO.class);
        Root<SessionInfoDO> root = criteriaDelete.from(SessionInfoDO.class);

        criteriaDelete.where(criteriaBuilder.equal(root.get("user_id"), userId));

        Transaction transaction = session.beginTransaction();
        int rowsUpdated = session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();

        if (rowsUpdated == 0)
            throw new UserIdNotFoundException("user_id not found in session_info");

    }

    @Override
    public String[] getSessionHandlesWhereUserIdEquals(String userId) throws UserIdNotFoundException {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<SessionInfoDO> criteriaQuery = criteriaBuilder.createQuery(SessionInfoDO.class);
        Root<SessionInfoDO> root = criteriaQuery.from(SessionInfoDO.class);
        criteriaQuery.select(root.get("session_handle"));

        criteriaQuery.where(criteriaBuilder.equal(root.get("user_id"), userId));
        List resultList = session.createQuery(criteriaQuery).getResultList();

        if (resultList.size() == 0)
            throw new UserIdNotFoundException("user_id not found in session_info");

        String[] results = new String[resultList.size()];
        int counter = 0;

        Iterator iterator = resultList.iterator();
        while (iterator.hasNext()) {
            results[counter++] = iterator.next().toString();
        }
        return results;
    }

    @Override
    public void deleteWhereExpiresLessThan(long expires) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

        CriteriaDelete<SessionInfoDO> criteriaDelete = criteriaBuilder.createCriteriaDelete(SessionInfoDO.class);
        Root<SessionInfoDO> root = criteriaDelete.from(SessionInfoDO.class);

        criteriaDelete.where(criteriaBuilder.lessThan(root.get("expires_at"), expires));

        Transaction transaction = session.beginTransaction();
        int rowsUpdated = session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();

    }

    @Override
    public SessionInfoDO getWhereSessionHandleEquals(String sessionHandle) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(SessionInfoDO.class);
        Root<SessionInfoDO> root = criteriaQuery.from(SessionInfoDO.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("session_handle"), sessionHandle));
        Query<SessionInfoDO> query = session.createQuery(criteriaQuery);
        Transaction transaction = session.beginTransaction();
        SessionInfoDO sessionInfoDO = query.getSingleResult();
        transaction.commit();
        session.close();
        return sessionInfoDO;
    }
}
