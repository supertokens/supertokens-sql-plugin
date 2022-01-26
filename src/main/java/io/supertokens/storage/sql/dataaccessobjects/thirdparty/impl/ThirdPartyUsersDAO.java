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

package io.supertokens.storage.sql.dataaccessobjects.thirdparty.impl;

import io.supertokens.storage.sql.dataaccessobjects.SessionTransactionDAO;
import io.supertokens.storage.sql.dataaccessobjects.thirdparty.ThirdPartyUsersInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.thirdparty.ThirdPartyUsersDO;
import io.supertokens.storage.sql.domainobjects.thirdparty.ThirdPartyUsersPKDO;
import io.supertokens.storage.sql.enums.OrderEnum;
import io.supertokens.storage.sql.exceptions.InvalidOrderTypeException;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ThirdPartyUsersDAO extends SessionTransactionDAO implements ThirdPartyUsersInterfaceDAO {

    public ThirdPartyUsersDAO(Session sessionInstance) {
        super(sessionInstance);
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
        Session session = (Session) sessionInstance.getSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<ThirdPartyUsersDO> criteriaQuery = criteriaBuilder.createQuery(ThirdPartyUsersDO.class);
        Root<ThirdPartyUsersDO> root = criteriaQuery.from(ThirdPartyUsersDO.class);
        criteriaQuery.select(root);
        Query<ThirdPartyUsersDO> query = session.createQuery(criteriaQuery);
        List<ThirdPartyUsersDO> result = query.getResultList();
        return result;
    }

    @Override
    public int removeWhereUserIdEquals(Object id) throws Exception {
        return 0;
    }

    @Override
    public void removeAll() {
        Session session = (Session) sessionInstance.getSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<ThirdPartyUsersDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(ThirdPartyUsersDO.class);
        Root<ThirdPartyUsersDO> root = criteriaDelete.from(ThirdPartyUsersDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("primary_key")));
        session.createQuery(criteriaDelete).executeUpdate();
    }

    @Override
    public ThirdPartyUsersDO getWhereThirdPartyIDEqualsAndThirdPartyUserIdEquals(String thirdPartyId,
            String thirdPartyUserId) {
        Session session = (Session) sessionInstance.getSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<ThirdPartyUsersDO> criteriaQuery = criteriaBuilder.createQuery(ThirdPartyUsersDO.class);
        Root<ThirdPartyUsersDO> root = criteriaQuery.from(ThirdPartyUsersDO.class);

        Predicate predicateOne = criteriaBuilder.equal(root.get("primary_key").get("third_party_id"), thirdPartyId);
        Predicate predicateTwo = criteriaBuilder.equal(root.get("primary_key").get("third_party_user_id"),
                thirdPartyUserId);

        criteriaQuery.where(criteriaBuilder.and(predicateOne, predicateTwo));
        ThirdPartyUsersDO thirdPartyUsersDO = null;

        thirdPartyUsersDO = session.createQuery(criteriaQuery).getSingleResult();

        return thirdPartyUsersDO;
    }

    @Override
    public Serializable insertValues(String thirdPartyId, String thirdPartyUserId, String userId, String email,
            long timeJoined) {
        ThirdPartyUsersPKDO partyUsersPKDO = new ThirdPartyUsersPKDO(thirdPartyId, thirdPartyUserId);
        ThirdPartyUsersDO thirdPartyUsersDO = new ThirdPartyUsersDO(partyUsersPKDO, userId, email, timeJoined);

        Session session = (Session) sessionInstance.getSession();
        ThirdPartyUsersPKDO pkdo = (ThirdPartyUsersPKDO) session.save(thirdPartyUsersDO);

        return pkdo;
    }

    @Override
    public void updateEmailWhereThirdPartyIdEqualsAndThirdPartyUserIdEquals(String thirdPartyId,
            String thirdPartyUserId, String email) {

        Session session = (Session) sessionInstance.getSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaUpdate<ThirdPartyUsersDO> criteriaUpdate = criteriaBuilder
                .createCriteriaUpdate(ThirdPartyUsersDO.class);

        Root<ThirdPartyUsersDO> root = criteriaUpdate.from(ThirdPartyUsersDO.class);

        criteriaUpdate.set(root.get("email"), email);
        Predicate predicateOne = criteriaBuilder.equal(root.get("primary_key").get("third_party_id"), thirdPartyId);
        Predicate predicateTwo = criteriaBuilder.equal(root.get("primary_key").get("third_party_user_id"),
                thirdPartyUserId);

        criteriaUpdate.where(criteriaBuilder.and(predicateOne, predicateTwo));

        int rowsUpdated = session.createQuery(criteriaUpdate).executeUpdate();

        if (rowsUpdated == 0)
            throw new NoResultException();

    }

    @Override
    public ThirdPartyUsersDO getWhereThirdPartyIDEqualsAndThirdPartyUserIdEquals_locked(String thirdPartyId,
            String thirdPartyUserId) {
        Session session = (Session) sessionInstance.getSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<ThirdPartyUsersDO> criteriaQuery = criteriaBuilder.createQuery(ThirdPartyUsersDO.class);
        Root<ThirdPartyUsersDO> root = criteriaQuery.from(ThirdPartyUsersDO.class);

        Predicate predicateOne = criteriaBuilder.equal(root.get("primary_key").get("third_party_id"), thirdPartyId);
        Predicate predicateTwo = criteriaBuilder.equal(root.get("primary_key").get("third_party_user_id"),
                thirdPartyUserId);

        criteriaQuery.where(criteriaBuilder.and(predicateOne, predicateTwo));
        ThirdPartyUsersDO thirdPartyUsersDO = null;

            thirdPartyUsersDO = session.createQuery(criteriaQuery).setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getSingleResult();

        return thirdPartyUsersDO;
    }

    @Override
    public List<ThirdPartyUsersDO> getWhereEmailEquals(String email) {
        Session session = (Session) sessionInstance.getSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<ThirdPartyUsersDO> criteriaQuery = criteriaBuilder.createQuery(ThirdPartyUsersDO.class);
        Root<ThirdPartyUsersDO> root = criteriaQuery.from(ThirdPartyUsersDO.class);

        criteriaQuery.where(criteriaBuilder.equal(root.get("email"), email));
        List<ThirdPartyUsersDO> thirdPartyUsersDOs = null;

        thirdPartyUsersDOs = session.createQuery(criteriaQuery).getResultList();

        if (thirdPartyUsersDOs.size() == 0)
            throw new NoResultException();

        return thirdPartyUsersDOs;
    }

    @Override
    public List<ThirdPartyUsersDO> getByTimeJoinedOrderAndUserIdOrderAndLimit(String timeJoinedOrder,
            String userIdOrder, Integer limit) throws InvalidOrderTypeException {

        Session session = (Session) sessionInstance.getSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<ThirdPartyUsersDO> criteriaQuery = criteriaBuilder.createQuery(ThirdPartyUsersDO.class);

        Root<ThirdPartyUsersDO> root = criteriaQuery.from(ThirdPartyUsersDO.class);
        List<Order> ordersList = new ArrayList<>();
        if (timeJoinedOrder.equalsIgnoreCase(OrderEnum.DESC.name())) {
            ordersList.add(criteriaBuilder.desc(root.get("time_joined")));
        } else if (timeJoinedOrder.equalsIgnoreCase(OrderEnum.ASC.name())) {
            ordersList.add(criteriaBuilder.asc(root.get("time_joined")));
        } else {
            throw new InvalidOrderTypeException(timeJoinedOrder + " not defined");
        }

        if (userIdOrder.equalsIgnoreCase(OrderEnum.DESC.name())) {
            ordersList.add(criteriaBuilder.desc(root.get("user_id")));
        } else if (userIdOrder.equalsIgnoreCase(OrderEnum.ASC.name())) {
            ordersList.add(criteriaBuilder.asc(root.get("user_id")));
        } else {
            throw new InvalidOrderTypeException(userIdOrder + " not defined");
        }

        criteriaQuery.orderBy(ordersList);

        List<ThirdPartyUsersDO> resultList = session.createQuery(criteriaQuery).setFirstResult(0).setMaxResults(limit)
                .getResultList();

        if (resultList.size() == 0)
            throw new NoResultException();

        return resultList;

    }

    @Override
    public Long getCount() {
        Session session = (Session) sessionInstance.getSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        criteriaQuery.select(criteriaBuilder.count(criteriaQuery.from(ThirdPartyUsersDO.class)));
        Long rows = session.createQuery(criteriaQuery).getSingleResult();
        return rows;
    }
}
