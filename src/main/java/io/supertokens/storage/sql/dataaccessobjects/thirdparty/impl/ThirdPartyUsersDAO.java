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

import io.supertokens.storage.sql.dataaccessobjects.SessionFactoryDAO;
import io.supertokens.storage.sql.dataaccessobjects.thirdparty.ThirdPartyUsersInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordPswdResetTokensDO;
import io.supertokens.storage.sql.domainobjects.thirdparty.ThirdPartyUsersDO;
import io.supertokens.storage.sql.domainobjects.thirdparty.ThirdPartyUsersPKDO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.List;

public class ThirdPartyUsersDAO extends SessionFactoryDAO implements ThirdPartyUsersInterfaceDAO {

    public ThirdPartyUsersDAO(SessionFactory sessionFactory) {
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
        CriteriaQuery<ThirdPartyUsersDO> criteriaQuery = criteriaBuilder
                .createQuery(ThirdPartyUsersDO.class);
        Root<ThirdPartyUsersDO> root = criteriaQuery.from(ThirdPartyUsersDO.class);
        criteriaQuery.select(root);
        Query<ThirdPartyUsersDO> query = session.createQuery(criteriaQuery);
        List<ThirdPartyUsersDO> result = query.getResultList();
        session.close();
        return result;
    }

    @Override
    public void removeWhereUserIdEquals(Object id) throws Exception {

    }

    @Override
    public void removeAll() {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<ThirdPartyUsersDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(ThirdPartyUsersDO.class);
        Root<ThirdPartyUsersDO> root = criteriaDelete.from(ThirdPartyUsersDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("primary_key")));
        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();
    }

    @Override
    public ThirdPartyUsersDO getWhereThirdPartyIDEqualsAndThirdPartyUserIdEquals(String thirdPartyId,
                                                                                 String thirdPartyUserId) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<ThirdPartyUsersDO> criteriaQuery = criteriaBuilder.createQuery(ThirdPartyUsersDO.class);
        Root<ThirdPartyUsersDO> root = criteriaQuery.from(ThirdPartyUsersDO.class);

        Predicate predicateOne = criteriaBuilder.equal(root.get("primary_key").get("third_party_id"), thirdPartyId);
        Predicate predicateTwo = criteriaBuilder.equal(root.get("primary_key").get("third_party_user_id"), thirdPartyUserId);

        criteriaQuery.where(
            criteriaBuilder.and(
                    predicateOne, predicateTwo
            )
        );
        ThirdPartyUsersDO thirdPartyUsersDO = null;

        Transaction transaction = session.beginTransaction();
        try {
            thirdPartyUsersDO =
                    session.createQuery(criteriaQuery).setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
        } catch (NoResultException e) {
            if (transaction != null)
                transaction.rollback();

            throw new NoResultException();
        }
        transaction.commit();
        session.close();

        return thirdPartyUsersDO;
    }

    @Override
    public Serializable insertValues(String thirdPartyId, String thirdPartyUserId, String userId, String email,
                                     long timeJoined) {
        ThirdPartyUsersPKDO partyUsersPKDO = new ThirdPartyUsersPKDO(
                thirdPartyId, thirdPartyUserId
                );
        ThirdPartyUsersDO thirdPartyUsersDO = new ThirdPartyUsersDO(
                partyUsersPKDO, userId, email, timeJoined
        );

        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        ThirdPartyUsersPKDO pkdo = (ThirdPartyUsersPKDO) session.save(thirdPartyUsersDO);
        transaction.commit();
        session.close();

        return pkdo;
    }
}
