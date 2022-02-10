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
import io.supertokens.storage.sql.dataaccessobjects.SessionTransactionDAO;
import io.supertokens.storage.sql.dataaccessobjects.passwordless.PasswordlessUsersInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessUsersDO;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.List;

public class PasswordlessUsersDAO extends SessionTransactionDAO implements PasswordlessUsersInterfaceDAO {

    public PasswordlessUsersDAO(Session sessionInstance) {
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
        CriteriaQuery<PasswordlessUsersDO> criteria = criteriaBuilder.createQuery(PasswordlessUsersDO.class);
        Root<PasswordlessUsersDO> root = criteria.from(PasswordlessUsersDO.class);
        criteria.select(root);
        Query<PasswordlessUsersDO> query = session.createQuery(criteria);
        List<PasswordlessUsersDO> results = query.getResultList();
        return results;
    }

    @Override
    public int deleteWherePrimaryKeyEquals(Object id) throws Exception {
        return 0;
    }

    @Override
    public void removeAll() {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<PasswordlessUsersDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(PasswordlessUsersDO.class);
        Root<PasswordlessUsersDO> root = criteriaDelete.from(PasswordlessUsersDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("user_id")));
        session.createQuery(criteriaDelete).executeUpdate();
    }

    @Override
    public void updateEmailWhereUserIdEquals(String userId, String email) throws UnknownUserIdException {

        Session session = (Session) sessionInstance;

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaUpdate<PasswordlessUsersDO> criteriaUpdate = criteriaBuilder
                .createCriteriaUpdate(PasswordlessUsersDO.class);

        Root<PasswordlessUsersDO> root = criteriaUpdate.from(PasswordlessUsersDO.class);
        criteriaUpdate.set(root.get("email"), email);
        criteriaUpdate.where(criteriaBuilder.equal(root.get("user_id"), userId));
        int rowsUpdated = 0;
        rowsUpdated = session.createQuery(criteriaUpdate).executeUpdate();

        if (rowsUpdated == 0)
            throw new UnknownUserIdException();

    }

    @Override
    public String insertValuesIntoTable(String userId, String emailId, String phoneNumber, long timeJoined) {
        PasswordlessUsersDO passwordlessUsersDO = new PasswordlessUsersDO(userId, emailId, phoneNumber, timeJoined);

        Session session = (Session) sessionInstance;
        String id = (String) session.save(passwordlessUsersDO);

        return id;
    }

    @Override
    public PasswordlessUsersDO getWhereUserIdEquals(String userId) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PasswordlessUsersDO> criteria = criteriaBuilder.createQuery(PasswordlessUsersDO.class);
        Root<PasswordlessUsersDO> root = criteria.from(PasswordlessUsersDO.class);
        criteria.select(root);
        criteria.where(criteriaBuilder.equal(root.get("user_id"), userId));
        Query<PasswordlessUsersDO> query = session.createQuery(criteria);
        PasswordlessUsersDO result = query.getSingleResult();
        return result;
    }

    @Override
    public void updatePhoneNumberWhereUserIdEquals(String userId, String phoneNumber) throws UnknownUserIdException {
        Session session = (Session) sessionInstance;

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaUpdate<PasswordlessUsersDO> criteriaUpdate = criteriaBuilder
                .createCriteriaUpdate(PasswordlessUsersDO.class);

        Root<PasswordlessUsersDO> root = criteriaUpdate.from(PasswordlessUsersDO.class);
        criteriaUpdate.set(root.get("phone_number"), phoneNumber);
        criteriaUpdate.where(criteriaBuilder.equal(root.get("user_id"), userId));

        int rowsUpdated = 0;

        rowsUpdated = session.createQuery(criteriaUpdate).executeUpdate();

        if (rowsUpdated == 0)
            throw new UnknownUserIdException();
    }

    @Override
    public PasswordlessUsersDO getUserWhereEmailEquals(String email) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PasswordlessUsersDO> criteria = criteriaBuilder.createQuery(PasswordlessUsersDO.class);
        Root<PasswordlessUsersDO> root = criteria.from(PasswordlessUsersDO.class);
        criteria.select(root);
        criteria.where(criteriaBuilder.equal(root.get("email"), email));
        Query<PasswordlessUsersDO> query = session.createQuery(criteria);
        PasswordlessUsersDO result = query.getSingleResult();
        return result;
    }

    @Override
    public PasswordlessUsersDO getUserWherePhoneNumberEquals(String phoneNumber) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PasswordlessUsersDO> criteria = criteriaBuilder.createQuery(PasswordlessUsersDO.class);
        Root<PasswordlessUsersDO> root = criteria.from(PasswordlessUsersDO.class);
        criteria.select(root);
        criteria.where(criteriaBuilder.equal(root.get("phone_number"), phoneNumber));
        Query<PasswordlessUsersDO> query = session.createQuery(criteria);
        PasswordlessUsersDO result = query.getSingleResult();
        return result;
    }

    @Override
    public int deleteWhereUserIdEquals(String userId) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<PasswordlessUsersDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(PasswordlessUsersDO.class);
        Root<PasswordlessUsersDO> root = criteriaDelete.from(PasswordlessUsersDO.class);
        criteriaDelete.where(criteriaBuilder.equal(root.get("user_id"), userId));
        return session.createQuery(criteriaDelete).executeUpdate();
    }
}
