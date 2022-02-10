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
import io.supertokens.storage.sql.dataaccessobjects.SessionTransactionDAO;
import io.supertokens.storage.sql.dataaccessobjects.emailpassword.EmailPasswordPswdResetTokensInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordPswdResetTokensDO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordPswdResetTokensPKDO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordUsersDO;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jetbrains.annotations.TestOnly;

import javax.persistence.LockModeType;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.List;

public class EmailPasswordPswdResetTokensDAO extends SessionTransactionDAO
        implements EmailPasswordPswdResetTokensInterfaceDAO {

    public EmailPasswordPswdResetTokensDAO(Session sessionInstance) {
        super(sessionInstance);
    }

    @Override
    public Serializable create(EmailPasswordPswdResetTokensDO entity) {
        Session session = (Session) sessionInstance;
        Serializable id = session.save(entity);
        return id;
    }

    @Override
    public EmailPasswordPswdResetTokensDO getWherePrimaryKeyEquals(Object id) {
        Session session = (Session) sessionInstance;
        EmailPasswordPswdResetTokensDO emailPasswordPswdResetTokensDO = session
                .find(EmailPasswordPswdResetTokensDO.class, id);
        return emailPasswordPswdResetTokensDO;
    }

    @Override
    public List<EmailPasswordPswdResetTokensDO> getAll() {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailPasswordPswdResetTokensDO> criteriaQuery = criteriaBuilder
                .createQuery(EmailPasswordPswdResetTokensDO.class);
        Root<EmailPasswordPswdResetTokensDO> root = criteriaQuery.from(EmailPasswordPswdResetTokensDO.class);
        criteriaQuery.select(root);
        Query<EmailPasswordPswdResetTokensDO> query = session.createQuery(criteriaQuery);
        List<EmailPasswordPswdResetTokensDO> result = query.getResultList();
        return result;
    }

    @Override
    public int deleteWherePrimaryKeyEquals(Object id) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailPasswordPswdResetTokensDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailPasswordPswdResetTokensDO.class);
        Root<EmailPasswordPswdResetTokensDO> root = criteriaDelete.from(EmailPasswordPswdResetTokensDO.class);
        EmailPasswordPswdResetTokensPKDO dopk = (EmailPasswordPswdResetTokensPKDO) id;
        criteriaDelete.where(criteriaBuilder.equal(root.get("primaryKey"), dopk));
        return session.createQuery(criteriaDelete).executeUpdate();
    }

    @TestOnly
    @Override
    public void removeAll() {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailPasswordPswdResetTokensDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailPasswordPswdResetTokensDO.class);
        Root<EmailPasswordPswdResetTokensDO> root = criteriaDelete.from(EmailPasswordPswdResetTokensDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("primaryKey")));
        session.createQuery(criteriaDelete).executeUpdate();
    }

    @Override
    public void deleteWhereTokenExpiryIsLessThan(long token_expiry) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailPasswordPswdResetTokensDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailPasswordPswdResetTokensDO.class);
        Root<EmailPasswordPswdResetTokensDO> root = criteriaDelete.from(EmailPasswordPswdResetTokensDO.class);
        criteriaDelete.where(criteriaBuilder.lessThan(root.get("token_expiry"), token_expiry));
        session.createQuery(criteriaDelete).executeUpdate();
    }

    @Override
    public void deleteAllWhereUserIdEquals(String user_id) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailPasswordPswdResetTokensDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailPasswordPswdResetTokensDO.class);
        Root<EmailPasswordPswdResetTokensDO> root = criteriaDelete.from(EmailPasswordPswdResetTokensDO.class);
        criteriaDelete.where(criteriaBuilder.equal(root.get("primaryKey").get("user_id").get("user_id"), user_id));
        session.createQuery(criteriaDelete).executeUpdate();
    }

    @Override
    public List<EmailPasswordPswdResetTokensDO> getAllPasswordResetTokenInfoForUser(String userId) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailPasswordPswdResetTokensDO> criteriaQuery = criteriaBuilder
                .createQuery(EmailPasswordPswdResetTokensDO.class);

        Root<EmailPasswordPswdResetTokensDO> root = criteriaQuery.from(EmailPasswordPswdResetTokensDO.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("primaryKey").get("user_id").get("user_id"), userId));
        Query<EmailPasswordPswdResetTokensDO> query = session.createQuery(criteriaQuery);
        List<EmailPasswordPswdResetTokensDO> result = query.getResultList();
        return result;
    }

    @Override
    public List<EmailPasswordPswdResetTokensDO> getAllPasswordResetTokenInfoForUser_locked(String userId) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailPasswordPswdResetTokensDO> criteriaQuery = criteriaBuilder
                .createQuery(EmailPasswordPswdResetTokensDO.class);

        Root<EmailPasswordPswdResetTokensDO> root = criteriaQuery.from(EmailPasswordPswdResetTokensDO.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("primaryKey").get("user_id").get("user_id"), userId));
        Query<EmailPasswordPswdResetTokensDO> query = session.createQuery(criteriaQuery)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE);
        List<EmailPasswordPswdResetTokensDO> result = query.getResultList();
        return result;
    }

    @Override
    public EmailPasswordPswdResetTokensDO getPasswordResetTokenInfo(String token) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailPasswordPswdResetTokensDO> criteriaQuery = criteriaBuilder
                .createQuery(EmailPasswordPswdResetTokensDO.class);
        Root<EmailPasswordPswdResetTokensDO> root = criteriaQuery.from(EmailPasswordPswdResetTokensDO.class);
        criteriaQuery.select(root);

        criteriaQuery.where(criteriaBuilder.equal(root.get("primaryKey").get("token"), token));
        Query<EmailPasswordPswdResetTokensDO> query = session.createQuery(criteriaQuery);
        EmailPasswordPswdResetTokensDO emailPasswordPswdResetTokensDO = query.getSingleResult();
        return emailPasswordPswdResetTokensDO;
    }

    @Override
    public EmailPasswordPswdResetTokensPKDO insertPasswordResetTokenInfo(String userId, String token, long tokenExpiry)
            throws UnknownUserIdException {

        Session session = (Session) sessionInstance;

        EmailPasswordUsersDO emailPasswordUsersDO = session.find(EmailPasswordUsersDO.class, userId);
        if (emailPasswordUsersDO == null)
            throw new UnknownUserIdException();

        EmailPasswordPswdResetTokensDO emailPasswordPswdResetTokensDO = new EmailPasswordPswdResetTokensDO(
                new EmailPasswordPswdResetTokensPKDO(emailPasswordUsersDO, token), tokenExpiry);
        Serializable key = session.save(emailPasswordPswdResetTokensDO);
        return (EmailPasswordPswdResetTokensPKDO) key;
    }

}
