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
import io.supertokens.pluginInterface.sqlStorage.SessionObject;
import io.supertokens.storage.sql.dataaccessobjects.SessionTransactionDAO;
import io.supertokens.storage.sql.dataaccessobjects.emailpassword.EmailPasswordUsersInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordUsersDO;
import io.supertokens.storage.sql.domainobjects.thirdparty.ThirdPartyUsersDO;
import io.supertokens.storage.sql.enums.OrderEnum;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jetbrains.annotations.TestOnly;

import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This DAO acts as the CRUD layer for interaction with emailpassword_users table
 */
public class EmailPasswordUsersDAO extends SessionTransactionDAO implements EmailPasswordUsersInterfaceDAO {

    public EmailPasswordUsersDAO(SessionObject sessionInstance) {
        super(sessionInstance);
    }

    /**
     * create EmailPasswordUsers
     *
     * @param entity
     * @return
     */
    @Override
    public String create(EmailPasswordUsersDO entity) throws Exception {
        Session session = (Session) sessionInstance;
        return (String) session.save(entity);
    }

    @Override
    public EmailPasswordUsersDO getWherePrimaryKeyEquals(Object id) {
        Session session = (Session) sessionInstance;
        EmailPasswordUsersDO emailPasswordUsersDO = session.find(EmailPasswordUsersDO.class, id.toString());
        return emailPasswordUsersDO;
    }

    @Override
    public List<EmailPasswordUsersDO> getAll() {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailPasswordUsersDO> criteria = criteriaBuilder.createQuery(EmailPasswordUsersDO.class);
        Root<EmailPasswordUsersDO> root = criteria.from(EmailPasswordUsersDO.class);
        criteria.select(root);
        Query<EmailPasswordUsersDO> query = session.createQuery(criteria);
        List<EmailPasswordUsersDO> results = query.getResultList();
        return results;
    }

    @Override
    public int deleteWherePrimaryKeyEquals(Object entity) throws PersistenceException, UnknownUserIdException {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailPasswordUsersDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailPasswordUsersDO.class);
        Root root = criteriaDelete.from(EmailPasswordUsersDO.class);
        criteriaDelete.where(criteriaBuilder.equal(root.get("user_id"), entity.toString()));

        return session.createQuery(criteriaDelete).executeUpdate();
    }

    @TestOnly
    @Override
    public void removeAll() {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<EmailPasswordUsersDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(EmailPasswordUsersDO.class);
        Root root = criteriaDelete.from(EmailPasswordUsersDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("user_id")));
        session.createQuery(criteriaDelete).executeUpdate();
    }

    @Override
    public void updatePasswordHashWhereUserId(String user_id, String password_hash) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaUpdate<EmailPasswordUsersDO> criteriaUpdate = criteriaBuilder
                .createCriteriaUpdate(EmailPasswordUsersDO.class);
        Root<EmailPasswordUsersDO> root = criteriaUpdate.from(EmailPasswordUsersDO.class);
        criteriaUpdate.set("password_hash", password_hash);
        criteriaUpdate.where(criteriaBuilder.equal(root.get("user_id"), user_id));
        session.createQuery(criteriaUpdate).executeUpdate();
    }

    @Override
    public int updateEmailWhereUserId(String user_id, String email) throws UnknownUserIdException {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaUpdate<EmailPasswordUsersDO> criteriaUpdate = criteriaBuilder
                .createCriteriaUpdate(EmailPasswordUsersDO.class);
        Root<EmailPasswordUsersDO> root = criteriaUpdate.from(EmailPasswordUsersDO.class);
        criteriaUpdate.set("email", email);
        criteriaUpdate.where(criteriaBuilder.equal(root.get("user_id"), user_id));
        return session.createQuery(criteriaUpdate).executeUpdate();
    }

    @Override
    public String insert(String userId, String email, String passwordHash, long timeJoined) {
        EmailPasswordUsersDO emailPasswordUsersDO = new EmailPasswordUsersDO(userId, email, passwordHash, timeJoined);
        Session session = (Session) sessionInstance;
        String userIdSaved = (String) session.save(emailPasswordUsersDO);
        return userIdSaved;
    }

    @Override
    public EmailPasswordUsersDO getWhereUserIdEquals_locked(String userId) throws NoResultException {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailPasswordUsersDO> criteriaQuery = criteriaBuilder.createQuery(EmailPasswordUsersDO.class);

        Root<EmailPasswordUsersDO> root = criteriaQuery.from(EmailPasswordUsersDO.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("user_id"), userId));
        Query<EmailPasswordUsersDO> query = session.createQuery(criteriaQuery)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE);
        EmailPasswordUsersDO result = query.getSingleResult();
        return result;
    }

    @Override
    public EmailPasswordUsersDO getWhereEmailEquals(String email) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailPasswordUsersDO> criteriaQuery = criteriaBuilder.createQuery(EmailPasswordUsersDO.class);

        Root<EmailPasswordUsersDO> root = criteriaQuery.from(EmailPasswordUsersDO.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("email"), email));
        Query<EmailPasswordUsersDO> query = session.createQuery(criteriaQuery);
        EmailPasswordUsersDO result = query.getSingleResult();
        return result;
    }

    @Override
    public List<EmailPasswordUsersDO> getLimitedOrderByTimeJoinedAndUserId(String timeJoinedOrder, String userIdOrder,
            int limit) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailPasswordUsersDO> criteria = criteriaBuilder.createQuery(EmailPasswordUsersDO.class);
        Root<EmailPasswordUsersDO> root = criteria.from(EmailPasswordUsersDO.class);
        criteria.select(root);

        List<Order> orderList = new ArrayList();

        if (timeJoinedOrder.equalsIgnoreCase(OrderEnum.DESC.name())) {
            orderList.add(criteriaBuilder.desc(root.get("time_joined")));
        } else if (timeJoinedOrder.equalsIgnoreCase(OrderEnum.ASC.name())) {
            orderList.add(criteriaBuilder.asc(root.get("time_joined")));
        }

        if (userIdOrder.equalsIgnoreCase(OrderEnum.DESC.name())) {
            orderList.add(criteriaBuilder.desc(root.get("user_id")));
        } else if (userIdOrder.equalsIgnoreCase(OrderEnum.ASC.name())) {
            orderList.add(criteriaBuilder.asc(root.get("user_id")));
        }

        criteria.orderBy(orderList);

        Query<EmailPasswordUsersDO> query = session.createQuery(criteria).setMaxResults(limit);
        List<EmailPasswordUsersDO> results = query.getResultList();
        return results;
    }

    @Override
    public List<EmailPasswordUsersDO> getLimitedUsersInfo(String timeJoinedOrder, Long timeJoined, String userIdOrder,
            String userId, int limit) {
        String timeJoinedOrderSymbol = timeJoinedOrder.equals("ASC") ? ">" : "<";

        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<EmailPasswordUsersDO> criteria = criteriaBuilder.createQuery(EmailPasswordUsersDO.class);
        Root<EmailPasswordUsersDO> root = criteria.from(EmailPasswordUsersDO.class);
        criteria.select(root);

        Predicate predicateOne = null;
        if (timeJoinedOrderSymbol.equalsIgnoreCase("<")) {
            predicateOne = criteriaBuilder.lessThan(root.get("time_joined"), timeJoined);
        } else if (timeJoinedOrderSymbol.equalsIgnoreCase(">")) {
            predicateOne = criteriaBuilder.greaterThan(root.get("time_joined"), timeJoined);
        } else {
            throw new PersistenceException("Invalid timeJoined order");
        }

        Predicate predicateTwo = criteriaBuilder.and(criteriaBuilder.equal(root.get("time_joined"), timeJoined),
                criteriaBuilder.equal(root.get("user_id"), userId));

        Predicate predicateThree = criteriaBuilder.or(predicateOne, predicateTwo);

        criteria.where(predicateThree);

        List<Order> orderList = new ArrayList();

        if (timeJoinedOrder.equalsIgnoreCase(OrderEnum.DESC.name())) {
            orderList.add(criteriaBuilder.desc(root.get("time_joined")));
        } else if (timeJoinedOrder.equalsIgnoreCase(OrderEnum.ASC.name())) {
            orderList.add(criteriaBuilder.asc(root.get("time_joined")));
        }

        if (userIdOrder.equalsIgnoreCase(OrderEnum.DESC.name())) {
            orderList.add(criteriaBuilder.desc(root.get("user_id")));
        } else if (userIdOrder.equalsIgnoreCase(OrderEnum.ASC.name())) {
            orderList.add(criteriaBuilder.asc(root.get("user_id")));
        }

        criteria.orderBy(orderList);

        Query<EmailPasswordUsersDO> query = session.createQuery(criteria).setMaxResults(limit);
        List<EmailPasswordUsersDO> results = query.getResultList();
        return results;
    }

    @Override
    public Long getCount() {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        criteriaQuery.select(criteriaBuilder.count(criteriaQuery.from(EmailPasswordUsersDO.class)));
        Long rows = session.createQuery(criteriaQuery).getSingleResult();
        return rows;
    }

}
