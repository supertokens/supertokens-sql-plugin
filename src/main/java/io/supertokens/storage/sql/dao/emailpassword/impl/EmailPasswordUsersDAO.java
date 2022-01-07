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

package io.supertokens.storage.sql.dao.emailpassword.impl;

import io.supertokens.storage.sql.dao.SessionFactoryDAO;
import io.supertokens.storage.sql.dao.emailpassword.EmailPasswordUsersInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordUsersDO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
    public Serializable create(EmailPasswordUsersDO entity) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        Serializable savedId = session.save(entity);
        transaction.commit();
        session.close();
        return savedId;
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
    public void remove(Object entity) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailPasswordUsersDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailPasswordUsersDO.class);
        Root root = criteriaDelete.from(EmailPasswordUsersDO.class);
        criteriaDelete.where(criteriaBuilder.equal(root.get("user_id"), entity.toString()));
        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();
    }

    @Override
    public void removeAll() {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailPasswordUsersDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailPasswordUsersDO.class);
        Root root = criteriaDelete.from(EmailPasswordUsersDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("user_id")));
        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();
    }

}
