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

package io.supertokens.storage.sql.dataaccessobjects.emailverification.impl;

import io.supertokens.pluginInterface.sqlStorage.SessionObject;
import io.supertokens.storage.sql.dataaccessobjects.SessionTransactionDAO;
import io.supertokens.storage.sql.dataaccessobjects.emailverification.EmailVerificationTokensInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationTokensDO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationTokensPKDO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationVerifiedEmailsDO;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jetbrains.annotations.TestOnly;

import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.List;

public class EmailVerificationTokensDAO extends SessionTransactionDAO implements EmailVerificationTokensInterfaceDAO {

    public EmailVerificationTokensDAO(SessionObject sessionInstance) {
        super(sessionInstance);
    }

    @Override
    public void deleteFromTableWhereTokenExpiryIsLessThan(long tokenExpiry) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailVerificationTokensDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailVerificationTokensDO.class);
        Root<EmailVerificationTokensDO> root = criteriaDelete.from(EmailVerificationTokensDO.class);
        criteriaDelete.where(criteriaBuilder.lessThan(root.get("token_expiry"), tokenExpiry));
        session.createQuery(criteriaDelete).executeUpdate();
    }

    @Override
    public Serializable insertIntoTable(String userId, String email, String token, long tokenExpiry) {
        EmailVerificationTokensDO emailVerificationTokensDO = new EmailVerificationTokensDO(
                new EmailVerificationTokensPKDO(userId, email, token), tokenExpiry);

        Session session = (Session) sessionInstance;
        EmailVerificationTokensPKDO savedEntity = (EmailVerificationTokensPKDO) session.save(emailVerificationTokensDO);
        return savedEntity;

    }

    @Override
    public int deleteFromTableWhereUserIdEqualsAndEmailEquals(String userId, String email) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailVerificationTokensDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailVerificationTokensDO.class);
        Root<EmailVerificationTokensDO> root = criteriaDelete.from(EmailVerificationTokensDO.class);
        Predicate predicateUserId = criteriaBuilder.equal(root.get("primary_key").get("user_id"), userId);
        Predicate predicateUserEmail = criteriaBuilder.equal(root.get("primary_key").get("email"), email);

        criteriaDelete.where(criteriaBuilder.and(predicateUserId, predicateUserEmail));
        return session.createQuery(criteriaDelete).executeUpdate();
    }

    @Override
    public EmailVerificationTokensDO getEmailVerificationTokenWhereTokenEquals(String token) throws NoResultException {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailVerificationTokensDO> criteriaQuery = criteriaBuilder
                .createQuery(EmailVerificationTokensDO.class);
        Root<EmailVerificationTokensDO> root = criteriaQuery.from(EmailVerificationTokensDO.class);
        criteriaQuery.where(criteriaBuilder.equal(root.get("primary_key").get("token"), token));
        try {
            EmailVerificationTokensDO emailVerificationTokensDO = session.createQuery(criteriaQuery).getSingleResult();
            return emailVerificationTokensDO;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<EmailVerificationTokensDO> getEmailVerificationTokenWhereUserIdEqualsAndEmailEquals_locked(
            String userId, String email) throws NoResultException {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailVerificationTokensDO> criteriaQuery = criteriaBuilder
                .createQuery(EmailVerificationTokensDO.class);
        Root<EmailVerificationTokensDO> root = criteriaQuery.from(EmailVerificationTokensDO.class);
        Predicate predicateOne = criteriaBuilder.equal(root.get("primary_key").get("user_id"), userId);
        Predicate predicateTwo = criteriaBuilder.equal(root.get("primary_key").get("email"), email);
        criteriaQuery.where(criteriaBuilder.and(predicateOne, predicateTwo));
        List<EmailVerificationTokensDO> emailVerificationTokensDOList = session.createQuery(criteriaQuery)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();

        return emailVerificationTokensDOList;
    }

    @Override
    public List<EmailVerificationTokensDO> getEmailVerificationTokenWhereUserIdEqualsAndEmailEquals(String userId,
            String email) throws NoResultException {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailVerificationTokensDO> criteriaQuery = criteriaBuilder
                .createQuery(EmailVerificationTokensDO.class);
        Root<EmailVerificationTokensDO> root = criteriaQuery.from(EmailVerificationTokensDO.class);
        Predicate predicateOne = criteriaBuilder.equal(root.get("primary_key").get("user_id"), userId);
        Predicate predicateTwo = criteriaBuilder.equal(root.get("primary_key").get("email"), email);
        criteriaQuery.where(criteriaBuilder.and(predicateOne, predicateTwo));
        List<EmailVerificationTokensDO> emailVerificationTokensDOList = session.createQuery(criteriaQuery)
                .getResultList();

        return emailVerificationTokensDOList;
    }

    @Override
    public void deleteWhereUserIdEqualsAndEmailEquals(String userId, String email) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailVerificationTokensDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailVerificationTokensDO.class);
        Root<EmailVerificationTokensDO> root = criteriaDelete.from(EmailVerificationTokensDO.class);
        Predicate predicateOne = criteriaBuilder.equal(root.get("primary_key").get("user_id"), userId);
        Predicate predicateTwo = criteriaBuilder.equal(root.get("primary_key").get("email"), email);

        criteriaDelete.where(criteriaBuilder.and(predicateOne, predicateTwo));
        session.createQuery(criteriaDelete).executeUpdate();

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
        CriteriaQuery<EmailVerificationTokensDO> criteria = criteriaBuilder
                .createQuery(EmailVerificationTokensDO.class);
        Root<EmailVerificationTokensDO> root = criteria.from(EmailVerificationTokensDO.class);
        criteria.select(root);
        Query<EmailVerificationTokensDO> query = session.createQuery(criteria);
        List<EmailVerificationTokensDO> results = query.getResultList();
        return results;
    }

    @Override
    public int deleteWherePrimaryKeyEquals(Object id) throws PersistenceException {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailVerificationTokensDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailVerificationTokensDO.class);
        Root root = criteriaDelete.from(EmailVerificationTokensDO.class);
        criteriaDelete.where(criteriaBuilder.equal(root.get("primary_key").get("user_id"), id.toString()));
        return session.createQuery(criteriaDelete).executeUpdate();
    }

    @TestOnly
    @Override
    public void removeAll() {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailVerificationTokensDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailVerificationTokensDO.class);
        Root<EmailVerificationTokensDO> root = criteriaDelete.from(EmailVerificationTokensDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("primary_key").get("user_id")));
        session.createQuery(criteriaDelete).executeUpdate();
    }
}
