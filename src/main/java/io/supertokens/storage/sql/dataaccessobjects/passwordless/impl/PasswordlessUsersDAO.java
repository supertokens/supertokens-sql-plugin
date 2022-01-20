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

import io.supertokens.pluginInterface.emailpassword.exceptions.UnknownUserIdException;
import io.supertokens.storage.sql.dataaccessobjects.SessionFactoryDAO;
import io.supertokens.storage.sql.dataaccessobjects.passwordless.PasswordlessUsersInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessCodesDO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessUsersDO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.List;

public class PasswordlessUsersDAO extends SessionFactoryDAO implements PasswordlessUsersInterfaceDAO {

    public PasswordlessUsersDAO(SessionFactory sessionFactory) {
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
        CriteriaQuery<PasswordlessUsersDO> criteria = criteriaBuilder.createQuery(PasswordlessUsersDO.class);
        Root<PasswordlessUsersDO> root = criteria.from(PasswordlessUsersDO.class);
        criteria.select(root);
        Query<PasswordlessUsersDO> query = session.createQuery(criteria);
        List<PasswordlessUsersDO> results = query.getResultList();
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
        CriteriaDelete<PasswordlessUsersDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(PasswordlessUsersDO.class);
        Root<PasswordlessUsersDO> root = criteriaDelete.from(PasswordlessUsersDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("user_id")));
        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();
    }

    @Override
    public void updateEmailWhereUserIdEquals(String userId, String email) throws UnknownUserIdException {

        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaUpdate<PasswordlessUsersDO> criteriaUpdate = criteriaBuilder
                .createCriteriaUpdate(PasswordlessUsersDO.class);

        Root<PasswordlessUsersDO> root = criteriaUpdate.from(PasswordlessUsersDO.class);
        criteriaUpdate.set(root.get("email"), email);
        criteriaUpdate.where(criteriaBuilder.equal(root.get("user_id"), userId));
        Transaction transaction = session.beginTransaction();
        int rowsUpdated = 0;
        try {

            rowsUpdated = session.createQuery(criteriaUpdate).executeUpdate();
            transaction.commit();
            session.close();

        } catch (Exception e) {

            if (transaction != null)
                transaction.rollback();
            if (session != null)
                session.close();

            throw e;

        }
        if (rowsUpdated == 0)
            throw new UnknownUserIdException();

    }

    @Override
    public String insertValuesIntoTable(String userId, String emailId, String phoneNumber, long timeJoined) {
        PasswordlessUsersDO passwordlessUsersDO = new PasswordlessUsersDO(userId, emailId, phoneNumber, timeJoined);

        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        String id = (String) session.save(passwordlessUsersDO);
        transaction.commit();
        session.close();

        return id;
    }

    @Override
    public PasswordlessUsersDO getWhereUserIdEquals(String userId) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PasswordlessUsersDO> criteria = criteriaBuilder.createQuery(PasswordlessUsersDO.class);
        Root<PasswordlessUsersDO> root = criteria.from(PasswordlessUsersDO.class);
        criteria.select(root);
        criteria.where(criteriaBuilder.equal(root.get("user_id"), userId));
        Query<PasswordlessUsersDO> query = session.createQuery(criteria);
        PasswordlessUsersDO result = query.getSingleResult();
        session.close();
        return result;
    }

    @Override
    public void updatePhoneNumberWhereUserIdEquals(String userId, String phoneNumber) throws UnknownUserIdException {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaUpdate<PasswordlessUsersDO> criteriaUpdate = criteriaBuilder
                .createCriteriaUpdate(PasswordlessUsersDO.class);

        Root<PasswordlessUsersDO> root = criteriaUpdate.from(PasswordlessUsersDO.class);
        criteriaUpdate.set(root.get("phone_number"), phoneNumber);
        criteriaUpdate.where(criteriaBuilder.equal(root.get("user_id"), userId));

        Transaction transaction = session.beginTransaction();
        int rowsUpdated = 0;

        try {

            rowsUpdated = session.createQuery(criteriaUpdate).executeUpdate();
            transaction.commit();
            session.close();

        } catch (PersistenceException e) {
            if (transaction != null)
                transaction.rollback();
            if (session != null)
                session.close();
            throw e;
        }

        if (rowsUpdated == 0)
            throw new UnknownUserIdException();
    }

    @Override
    public PasswordlessUsersDO getUserWhereEmailEquals(String email) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PasswordlessUsersDO> criteria = criteriaBuilder.createQuery(PasswordlessUsersDO.class);
        Root<PasswordlessUsersDO> root = criteria.from(PasswordlessUsersDO.class);
        criteria.select(root);
        criteria.where(criteriaBuilder.equal(root.get("email"), email));
        Query<PasswordlessUsersDO> query = session.createQuery(criteria);
        PasswordlessUsersDO result = query.getSingleResult();
        session.close();
        return result;
    }

    @Override
    public PasswordlessUsersDO getUserWherePhoneNumberEquals(String phoneNumber) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PasswordlessUsersDO> criteria = criteriaBuilder.createQuery(PasswordlessUsersDO.class);
        Root<PasswordlessUsersDO> root = criteria.from(PasswordlessUsersDO.class);
        criteria.select(root);
        criteria.where(criteriaBuilder.equal(root.get("phone_number"), phoneNumber));
        Query<PasswordlessUsersDO> query = session.createQuery(criteria);
        PasswordlessUsersDO result = query.getSingleResult();
        session.close();
        return result;
    }
}
