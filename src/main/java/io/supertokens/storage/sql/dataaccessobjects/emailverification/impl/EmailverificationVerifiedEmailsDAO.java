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
import io.supertokens.storage.sql.dataaccessobjects.emailverification.EmailverificationVerifiedEmailsInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationVerifiedEmailsDO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationVerifiedEmailsPKDO;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jetbrains.annotations.TestOnly;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.List;

public class EmailverificationVerifiedEmailsDAO extends SessionTransactionDAO
        implements EmailverificationVerifiedEmailsInterfaceDAO {

    public EmailverificationVerifiedEmailsDAO(SessionObject sessionFactory) {
        super(sessionFactory);
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
        CriteriaQuery<EmailVerificationVerifiedEmailsDO> criteriaQuery = criteriaBuilder
                .createQuery(EmailVerificationVerifiedEmailsDO.class);
        Root<EmailVerificationVerifiedEmailsDO> root = criteriaQuery.from(EmailVerificationVerifiedEmailsDO.class);
        criteriaQuery.select(root);
        Query<EmailVerificationVerifiedEmailsDO> query = session.createQuery(criteriaQuery);
        List<EmailVerificationVerifiedEmailsDO> result = query.getResultList();
        return result;
    }

    @Override
    public int deleteWherePrimaryKeyEquals(Object id) throws PersistenceException {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailVerificationVerifiedEmailsDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailVerificationVerifiedEmailsDO.class);
        Root root = criteriaDelete.from(EmailVerificationVerifiedEmailsDO.class);
        criteriaDelete.where(criteriaBuilder.equal(root.get("primary_key").get("user_id"), id.toString()));
        return session.createQuery(criteriaDelete).executeUpdate();
    }

    @TestOnly
    @Override
    public void removeAll() {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailVerificationVerifiedEmailsDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailVerificationVerifiedEmailsDO.class);
        Root root = criteriaDelete.from(EmailVerificationVerifiedEmailsDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("primary_key")));
        session.createQuery(criteriaDelete).executeUpdate();
    }

    @Override
    public Serializable insertIntoTable(String userId, String email) {

        EmailVerificationVerifiedEmailsDO emailVerificationTokensDO = new EmailVerificationVerifiedEmailsDO(
                new EmailVerificationVerifiedEmailsPKDO(userId, email));

        Session session = (Session) sessionInstance;
        EmailVerificationVerifiedEmailsPKDO emailsPKDO = (EmailVerificationVerifiedEmailsPKDO) session
                .save(emailVerificationTokensDO);
        return emailsPKDO;
    }

    @Override
    public int deleteFromTableWhereUserIdEqualsAndEmailEquals(String userId, String email) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailVerificationVerifiedEmailsDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailVerificationVerifiedEmailsDO.class);
        Root<EmailVerificationVerifiedEmailsDO> root = criteriaDelete.from(EmailVerificationVerifiedEmailsDO.class);
        Predicate predicateUserID = criteriaBuilder.equal(root.get("primary_key").get("user_id"), userId);
        Predicate predicateEmail = criteriaBuilder.equal(root.get("primary_key").get("email"), email);
        criteriaDelete.where(criteriaBuilder.and(predicateUserID, predicateEmail));
        return session.createQuery(criteriaDelete).executeUpdate();
    }

    @Override
    public EmailVerificationVerifiedEmailsDO getWhereUserIdEqualsAndEmailEquals(String userId, String email)
            throws NoResultException {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailVerificationVerifiedEmailsDO> criteriaQuery = criteriaBuilder
                .createQuery(EmailVerificationVerifiedEmailsDO.class);
        Root<EmailVerificationVerifiedEmailsDO> root = criteriaQuery.from(EmailVerificationVerifiedEmailsDO.class);
        criteriaQuery.select(root);
        Predicate predicateOne = criteriaBuilder.equal(root.get("primary_key").get("user_id"), userId);
        Predicate predicateTwo = criteriaBuilder.equal(root.get("primary_key").get("email"), email);
        criteriaQuery.where(criteriaBuilder.and(predicateOne, predicateTwo));
        Query<EmailVerificationVerifiedEmailsDO> query = session.createQuery(criteriaQuery);
        EmailVerificationVerifiedEmailsDO result = null;
        try {
            result = query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
        return result;
    }

    @Override
    public int deleteWhereUserIdEquals(String userId) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailVerificationVerifiedEmailsDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailVerificationVerifiedEmailsDO.class);
        Root<EmailVerificationVerifiedEmailsDO> root = criteriaDelete.from(EmailVerificationVerifiedEmailsDO.class);
        criteriaDelete.where(criteriaBuilder.equal(root.get("primary_key").get("user_id"), userId));

        return session.createQuery(criteriaDelete).executeUpdate();

    }
}
