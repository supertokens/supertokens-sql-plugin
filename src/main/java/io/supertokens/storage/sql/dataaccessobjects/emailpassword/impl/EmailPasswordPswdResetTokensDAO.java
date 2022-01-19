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

package io.supertokens.storage.sql.dataaccessobjects.emailpassword.impl;

import io.supertokens.pluginInterface.emailpassword.exceptions.UnknownUserIdException;
import io.supertokens.storage.sql.dataaccessobjects.SessionFactoryDAO;
import io.supertokens.storage.sql.dataaccessobjects.emailpassword.EmailPasswordPswdResetTokensInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordPswdResetTokensDO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordPswdResetTokensPKDO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordUsersDO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jetbrains.annotations.TestOnly;

import javax.persistence.LockModeType;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.List;

public class EmailPasswordPswdResetTokensDAO extends SessionFactoryDAO
        implements EmailPasswordPswdResetTokensInterfaceDAO {

    public EmailPasswordPswdResetTokensDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Serializable create(EmailPasswordPswdResetTokensDO entity) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        Serializable id = session.save(entity);
        transaction.commit();
        session.close();
        return id;
    }

    @Override
    public EmailPasswordPswdResetTokensDO get(Object id) {
        Session session = sessionFactory.openSession();
        EmailPasswordPswdResetTokensDO emailPasswordPswdResetTokensDO = session
                .find(EmailPasswordPswdResetTokensDO.class, id);
        session.close();
        return emailPasswordPswdResetTokensDO;
    }

    @Override
    public List<EmailPasswordPswdResetTokensDO> getAll() {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailPasswordPswdResetTokensDO> criteriaQuery = criteriaBuilder
                .createQuery(EmailPasswordPswdResetTokensDO.class);
        Root<EmailPasswordPswdResetTokensDO> root = criteriaQuery.from(EmailPasswordPswdResetTokensDO.class);
        criteriaQuery.select(root);
        Query<EmailPasswordPswdResetTokensDO> query = session.createQuery(criteriaQuery);
        List<EmailPasswordPswdResetTokensDO> result = query.getResultList();
        session.close();
        return result;
    }

    @Override
    public void removeWhereUserIdEquals(Object id) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailPasswordPswdResetTokensDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailPasswordPswdResetTokensDO.class);
        Root<EmailPasswordPswdResetTokensDO> root = criteriaDelete.from(EmailPasswordPswdResetTokensDO.class);
        EmailPasswordPswdResetTokensPKDO dopk = (EmailPasswordPswdResetTokensPKDO) id;
        criteriaDelete.where(criteriaBuilder.equal(root.get("primaryKey"), dopk));
        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();
    }

    @TestOnly
    @Override
    public void removeAll() {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailPasswordPswdResetTokensDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailPasswordPswdResetTokensDO.class);
        Root<EmailPasswordPswdResetTokensDO> root = criteriaDelete.from(EmailPasswordPswdResetTokensDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("primaryKey")));
        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();
    }

    @Override
    public void deleteWhereTokenExpiryIsLessThan(long token_expiry) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailPasswordPswdResetTokensDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailPasswordPswdResetTokensDO.class);
        Root<EmailPasswordPswdResetTokensDO> root = criteriaDelete.from(EmailPasswordPswdResetTokensDO.class);
        criteriaDelete.where(criteriaBuilder.lessThan(root.get("token_expiry"), token_expiry));
        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();
    }

    @Override
    public void deleteAllWhereUserIdEquals(String user_id) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailPasswordPswdResetTokensDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailPasswordPswdResetTokensDO.class);
        Root<EmailPasswordPswdResetTokensDO> root = criteriaDelete.from(EmailPasswordPswdResetTokensDO.class);
        criteriaDelete.where(criteriaBuilder.equal(root.get("primaryKey").get("user_id").get("user_id"), user_id));
        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();
    }

    @Override
    public List<EmailPasswordPswdResetTokensDO> getAllPasswordResetTokenInfoForUser(String userId) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailPasswordPswdResetTokensDO> criteriaQuery = criteriaBuilder
                .createQuery(EmailPasswordPswdResetTokensDO.class);

        Root<EmailPasswordPswdResetTokensDO> root = criteriaQuery.from(EmailPasswordPswdResetTokensDO.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("primaryKey").get("user_id").get("user_id"), userId));
        Query<EmailPasswordPswdResetTokensDO> query = session.createQuery(criteriaQuery);
        List<EmailPasswordPswdResetTokensDO> result = query.getResultList();
        session.close();
        return result;
    }

    @Override
    public List<EmailPasswordPswdResetTokensDO> lockAndgetAllPasswordResetTokenInfoForUser(String userId) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailPasswordPswdResetTokensDO> criteriaQuery = criteriaBuilder
                .createQuery(EmailPasswordPswdResetTokensDO.class);

        Root<EmailPasswordPswdResetTokensDO> root = criteriaQuery.from(EmailPasswordPswdResetTokensDO.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("primaryKey").get("user_id").get("user_id"), userId));
        Query<EmailPasswordPswdResetTokensDO> query = session.createQuery(criteriaQuery)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE);
        Transaction transaction = session.beginTransaction();
        List<EmailPasswordPswdResetTokensDO> result = query.getResultList();
        transaction.commit();
        session.close();
        return result;
    }

    @Override
    public EmailPasswordPswdResetTokensDO getPasswordResetTokenInfo(String token) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailPasswordPswdResetTokensDO> criteriaQuery = criteriaBuilder
                .createQuery(EmailPasswordPswdResetTokensDO.class);
        Root<EmailPasswordPswdResetTokensDO> root = criteriaQuery.from(EmailPasswordPswdResetTokensDO.class);
        criteriaQuery.select(root);

        criteriaQuery.where(criteriaBuilder.equal(root.get("primaryKey").get("token"), token));
        Query<EmailPasswordPswdResetTokensDO> query = session.createQuery(criteriaQuery);
        EmailPasswordPswdResetTokensDO emailPasswordPswdResetTokensDO = query.getSingleResult();
        session.close();
        return emailPasswordPswdResetTokensDO;
    }

    @Override
    public EmailPasswordPswdResetTokensPKDO insertPasswordResetTokenInfo(String userId, String token, long tokenExpiry)
            throws UnknownUserIdException {

        Session session = sessionFactory.openSession();

        EmailPasswordUsersDO emailPasswordUsersDO = session.find(EmailPasswordUsersDO.class, userId);
        if (emailPasswordUsersDO == null)
            throw new UnknownUserIdException();

        EmailPasswordPswdResetTokensDO emailPasswordPswdResetTokensDO = new EmailPasswordPswdResetTokensDO(
                new EmailPasswordPswdResetTokensPKDO(emailPasswordUsersDO, token), tokenExpiry);
        Transaction transaction = session.beginTransaction();
        Serializable key = session.save(emailPasswordPswdResetTokensDO);
        transaction.commit();
        session.close();
        return (EmailPasswordPswdResetTokensPKDO) key;
    }

}
