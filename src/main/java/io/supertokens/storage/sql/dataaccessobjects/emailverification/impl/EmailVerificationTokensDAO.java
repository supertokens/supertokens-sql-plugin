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

import io.supertokens.storage.sql.dataaccessobjects.SessionFactoryDAO;
import io.supertokens.storage.sql.dataaccessobjects.emailverification.EmailVerificationTokensInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationTokensDO;
import io.supertokens.storage.sql.domainobjects.emailverification.EmailVerificationTokensPKDO;
import io.supertokens.storage.sql.exceptions.UserAndEmailNotFoundException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jetbrains.annotations.TestOnly;

import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.List;

public class EmailVerificationTokensDAO extends SessionFactoryDAO implements EmailVerificationTokensInterfaceDAO {

    public EmailVerificationTokensDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public void deleteFromTableWhereTokenExpiryIsLessThan(long tokenExpiry) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailVerificationTokensDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailVerificationTokensDO.class);
        Root<EmailVerificationTokensDO> root = criteriaDelete.from(EmailVerificationTokensDO.class);
        criteriaDelete.where(criteriaBuilder.lessThan(root.get("token_expiry"), tokenExpiry));
        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();
    }

    @Override
    public Serializable insertIntoTable(String userId, String email, String token, long tokenExpiry) {
        EmailVerificationTokensDO emailVerificationTokensDO = new EmailVerificationTokensDO(
                new EmailVerificationTokensPKDO(userId, email, token),
                tokenExpiry
        );

        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        EmailVerificationTokensPKDO savedEntity = (EmailVerificationTokensPKDO) session.save(emailVerificationTokensDO);
        transaction.commit();
        session.close();
        return savedEntity;

    }

    @Override
    public void deleteFromTableWhereUserIdEqualsAndEmailEquals(String userId, String email)
            throws UserAndEmailNotFoundException {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailVerificationTokensDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailVerificationTokensDO.class);
        Root<EmailVerificationTokensDO> root = criteriaDelete.from(EmailVerificationTokensDO.class);
        Predicate predicateUserId = criteriaBuilder.equal(root.get("primary_key").get("user_id"), userId);
        Predicate predicateUserEmail = criteriaBuilder.equal(root.get("primary_key").get("email"), email);

        criteriaDelete.where(
                criteriaBuilder.and(
                        predicateUserId, predicateUserEmail
                )
        );
        Transaction transaction = session.beginTransaction();
        int rowsUpdated = session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();

        if (rowsUpdated == 0)
            throw new UserAndEmailNotFoundException();
    }

    @Override
    public EmailVerificationTokensDO getEmailVerificationTokenWhereTokenEquals(String token)
            throws NoResultException {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailVerificationTokensDO> criteriaQuery = criteriaBuilder
                .createQuery(EmailVerificationTokensDO.class);
        Root<EmailVerificationTokensDO> root = criteriaQuery.from(EmailVerificationTokensDO.class);
        criteriaQuery.where(criteriaBuilder.equal(root.get("primary_key").get("token"), token));
        Transaction transaction = session.beginTransaction();
        EmailVerificationTokensDO emailVerificationTokensDO = session.createQuery(criteriaQuery).getSingleResult();
        transaction.commit();
        session.close();
        return emailVerificationTokensDO;
    }

    @Override
    public List<EmailVerificationTokensDO> getLockedEmailVerificationTokenWhereUserIdEqualsAndEmailEquals(String userId,
                                                                                                          String email)
    throws NoResultException {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailVerificationTokensDO> criteriaQuery = criteriaBuilder
                .createQuery(EmailVerificationTokensDO.class);
        Root<EmailVerificationTokensDO> root = criteriaQuery.from(EmailVerificationTokensDO.class);
        Predicate predicateOne = criteriaBuilder.equal(root.get("primary_key").get("user_id"), userId);
        Predicate predicateTwo = criteriaBuilder.equal(root.get("primary_key").get("email"), email);
        criteriaQuery.where(
                criteriaBuilder.and( predicateOne, predicateTwo )
        );
        Transaction transaction = session.beginTransaction();
        List<EmailVerificationTokensDO> emailVerificationTokensDOList = session.createQuery(criteriaQuery)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
        transaction.commit();
        session.close();

        if (emailVerificationTokensDOList.size() == 0)
            throw new NoResultException();

        return emailVerificationTokensDOList;
    }

    @Override
    public List<EmailVerificationTokensDO> getEmailVerificationTokenWhereUserIdEqualsAndEmailEquals(String userId,
                                                                                                    String email)
    throws NoResultException{
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailVerificationTokensDO> criteriaQuery = criteriaBuilder
                .createQuery(EmailVerificationTokensDO.class);
        Root<EmailVerificationTokensDO> root = criteriaQuery.from(EmailVerificationTokensDO.class);
        Predicate predicateOne = criteriaBuilder.equal(root.get("primary_key").get("user_id"), userId);
        Predicate predicateTwo = criteriaBuilder.equal(root.get("primary_key").get("email"), email);
        criteriaQuery.where(
                criteriaBuilder.and( predicateOne, predicateTwo )
        );
        Transaction transaction = session.beginTransaction();
        List<EmailVerificationTokensDO> emailVerificationTokensDOList = session.createQuery(criteriaQuery).getResultList();
        transaction.commit();
        session.close();

        if (emailVerificationTokensDOList.size() == 0)
            throw new NoResultException();

        return emailVerificationTokensDOList;
    }

    @Override
    public void deleteWhereUserIdEqualsAndEmailEquals(String userId, String email) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailVerificationTokensDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailVerificationTokensDO.class);
        Root<EmailVerificationTokensDO> root = criteriaDelete.from(EmailVerificationTokensDO.class);
        Predicate predicateOne = criteriaBuilder.equal(root.get("primary_key").get("user_id"), userId);
        Predicate predicateTwo = criteriaBuilder.equal(root.get("primary_key").get("email"), email);

        criteriaDelete.where(
                criteriaBuilder.and(
                        predicateOne, predicateTwo
                )
        );

        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();
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
        CriteriaQuery<EmailVerificationTokensDO> criteria = criteriaBuilder.createQuery(EmailVerificationTokensDO.class);
        Root<EmailVerificationTokensDO> root = criteria.from(EmailVerificationTokensDO.class);
        criteria.select(root);
        Query<EmailVerificationTokensDO> query = session.createQuery(criteria);
        List<EmailVerificationTokensDO> results = query.getResultList();
        session.close();
        return results;
    }

    @Override
    public void removeWhereUserIdEquals(Object id) throws Exception {

    }

    @TestOnly
    @Override
    public void removeAll() {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailVerificationTokensDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailVerificationTokensDO.class);
        Root<EmailVerificationTokensDO> root = criteriaDelete.from(EmailVerificationTokensDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("primary_key").get("user_id")));
        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();
    }
}
