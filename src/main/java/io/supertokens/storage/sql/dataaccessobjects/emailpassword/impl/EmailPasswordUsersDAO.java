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
import io.supertokens.storage.sql.dataaccessobjects.emailpassword.EmailPasswordUsersInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordUsersDO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.List;

/**
 * This DAO acts as the CRUD layer for interaction with emailpassword_users table
 */
public class EmailPasswordUsersDAO extends SessionFactoryDAO implements EmailPasswordUsersInterfaceDAO {

    public EmailPasswordUsersDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    /**
     * create EmailPasswordUsers
     *
     * @param entity
     * @return
     */
    @Override
    public Serializable create(EmailPasswordUsersDO entity) throws Exception {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        Serializable savedId = null;
        Exception exception = null;
        try {

            transaction = session.beginTransaction();
            savedId = session.save(entity);
            transaction.commit();

        } catch (Exception e) {

            if (transaction != null)
                transaction.rollback();

            exception = e;

        } finally {
            session.close();
        }

        if (savedId != null)
            return savedId;

        throw exception;
    }

    @Override
    public EmailPasswordUsersDO get(Object id) {
        Session session = sessionFactory.openSession();
        EmailPasswordUsersDO emailPasswordUsersDO = session.find(EmailPasswordUsersDO.class, id.toString());
        session.close();
        return emailPasswordUsersDO;
    }

    @Override
    public List<EmailPasswordUsersDO> getAll() {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailPasswordUsersDO> criteria = criteriaBuilder.createQuery(EmailPasswordUsersDO.class);
        Root<EmailPasswordUsersDO> root = criteria.from(EmailPasswordUsersDO.class);
        criteria.select(root);
        Query<EmailPasswordUsersDO> query = session.createQuery(criteria);
        List<EmailPasswordUsersDO> results = query.getResultList();
        session.close();
        return results;
    }

    @Override
    public void removeWhereUserIdEquals(Object entity) throws PersistenceException, UnknownUserIdException {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailPasswordUsersDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailPasswordUsersDO.class);
        Root root = criteriaDelete.from(EmailPasswordUsersDO.class);
        criteriaDelete.where(criteriaBuilder.equal(root.get("user_id"), entity.toString()));
        Transaction transaction = null;
        PersistenceException exception = null;
        int rowsUpdated = 0;

        try {

            transaction = session.beginTransaction();
            rowsUpdated = session.createQuery(criteriaDelete).executeUpdate();
            transaction.commit();

        } catch (PersistenceException e) {

            if (transaction != null)
                transaction.rollback();
            exception = e;

        } finally {
            session.close();
        }

        if (exception != null)
            throw exception;

        if (rowsUpdated == 0)
            throw new UnknownUserIdException();
    }

    @Override
    public void removeAll() {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailPasswordUsersDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailPasswordUsersDO.class);
        Root root = criteriaDelete.from(EmailPasswordUsersDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("user_id")));
        Transaction transaction = null;
        Exception exception = null;

        try {

            transaction = session.beginTransaction();
            session.createQuery(criteriaDelete).executeUpdate();
            transaction.commit();

        } catch (Exception e) {

            if (transaction != null)
                transaction.rollback();

            exception = e;

        } finally {
            session.close();
        }
    }

    @Override
    public void updatePasswordHashWhereUserId(String user_id, String password_hash) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaUpdate<EmailPasswordUsersDO> criteriaUpdate = criteriaBuilder
                .createCriteriaUpdate(EmailPasswordUsersDO.class);
        Root<EmailPasswordUsersDO> root = criteriaUpdate.from(EmailPasswordUsersDO.class);
        criteriaUpdate.set("password_hash", password_hash);
        criteriaUpdate.where(criteriaBuilder.equal(root.get("user_id"), user_id));
        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaUpdate).executeUpdate();
        transaction.commit();
        session.close();
    }

    @Override
    public void updateEmailWhereUserId(String user_id, String email) throws UnknownUserIdException {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaUpdate<EmailPasswordUsersDO> criteriaUpdate = criteriaBuilder
                .createCriteriaUpdate(EmailPasswordUsersDO.class);
        Root<EmailPasswordUsersDO> root = criteriaUpdate.from(EmailPasswordUsersDO.class);
        criteriaUpdate.set("email", email);
        criteriaUpdate.where(criteriaBuilder.equal(root.get("user_id"), user_id));
        Transaction transaction = session.beginTransaction();
        int rowsUpdated = session.createQuery(criteriaUpdate).executeUpdate();
        transaction.commit();
        session.close();
        if (rowsUpdated == 0)
            throw new UnknownUserIdException();
    }

    @Override
    public String insert(String userId, String email, String passwordHash, long timeJoined) {
        EmailPasswordUsersDO emailPasswordUsersDO = new EmailPasswordUsersDO(userId, email, passwordHash, timeJoined);
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        String userIdSaved = (String) session.save(emailPasswordUsersDO);
        transaction.commit();
        session.close();
        return userIdSaved;
    }

    @Override
    public EmailPasswordUsersDO getWhereUserIdEquals(String userId) throws NoResultException {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailPasswordUsersDO> criteriaQuery = criteriaBuilder.createQuery(EmailPasswordUsersDO.class);

        Root<EmailPasswordUsersDO> root = criteriaQuery.from(EmailPasswordUsersDO.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("user_id"), userId));
        Query<EmailPasswordUsersDO> query = session.createQuery(criteriaQuery)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE);
        Transaction transaction = session.beginTransaction();
        EmailPasswordUsersDO result = query.getSingleResult();
        transaction.commit();
        session.close();
        return result;
    }

    @Override
    public EmailPasswordUsersDO getWhereEmailEquals(String email) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailPasswordUsersDO> criteriaQuery = criteriaBuilder.createQuery(EmailPasswordUsersDO.class);

        Root<EmailPasswordUsersDO> root = criteriaQuery.from(EmailPasswordUsersDO.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("email"), email));
        Query<EmailPasswordUsersDO> query = session.createQuery(criteriaQuery);
        Transaction transaction = session.beginTransaction();
        EmailPasswordUsersDO result = query.getSingleResult();
        transaction.commit();
        session.close();
        return result;
    }

}
