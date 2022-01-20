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

import io.supertokens.storage.sql.dataaccessobjects.SessionFactoryDAO;
import io.supertokens.storage.sql.dataaccessobjects.passwordless.PasswordlessDevicesInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessCodesDO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessDevicesDO;
import io.supertokens.storage.sql.domainobjects.thirdparty.ThirdPartyUsersDO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.List;

public class PasswordlessDevicesDAO extends SessionFactoryDAO implements PasswordlessDevicesInterfaceDAO {

    public PasswordlessDevicesDAO(SessionFactory sessionFactory) {
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
        CriteriaQuery<PasswordlessDevicesDO> criteriaQuery = criteriaBuilder.createQuery(PasswordlessDevicesDO.class);
        Root<PasswordlessDevicesDO> root = criteriaQuery.from(PasswordlessDevicesDO.class);
        criteriaQuery.select(root);
        Query<PasswordlessDevicesDO> query = session.createQuery(criteriaQuery);
        List<PasswordlessDevicesDO> result = query.getResultList();
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
        CriteriaDelete<PasswordlessDevicesDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(PasswordlessDevicesDO.class);
        Root<PasswordlessDevicesDO> root = criteriaDelete.from(PasswordlessDevicesDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("device_id_hash")));
        Transaction transaction = session.beginTransaction();
        session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();
    }

    @Override
    public String insertIntoTableValues(String deviceIdHash, String email, String phoneNumber, String linkCodeSalt,
            int failedAttempts, List<PasswordlessCodesDO> codes) {
        PasswordlessDevicesDO passwordlessDevicesDO = new PasswordlessDevicesDO(deviceIdHash, email, phoneNumber,
                linkCodeSalt, failedAttempts, codes);

        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        String id = (String) session.save(passwordlessDevicesDO);
        transaction.commit();
        session.close();
        return id;
    }

    @Override
    public PasswordlessDevicesDO getWhereDeviceIdHashEquals(String deviceIdHash) {
        Session session = sessionFactory.openSession();

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PasswordlessDevicesDO> criteriaQuery = criteriaBuilder.createQuery(PasswordlessDevicesDO.class);

        Root<PasswordlessDevicesDO> root = criteriaQuery.from(PasswordlessDevicesDO.class);

        criteriaQuery.where(criteriaBuilder.equal(root.get("device_id_hash"), deviceIdHash));

        Transaction transaction = session.beginTransaction();
        PasswordlessDevicesDO result = session.createQuery(criteriaQuery).setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getSingleResult();

        transaction.commit();
        session.close();

        return result;
    }

    // can be made more efficient, leave it for now to be upgraded later.
    // the process can be made more efficient by not retrieving entity first and doing
    // an inplace update
    @Override
    public void updateFailedAttemptsWhereDeviceIdHashEquals(String deviceIdHash) {

        Session session = sessionFactory.openSession();
        PasswordlessDevicesDO devicesDO = getWhereDeviceIdHashEquals(deviceIdHash);
        devicesDO.setFailed_attempts(devicesDO.getFailed_attempts() + 1);
        Transaction transaction = session.beginTransaction();
        session.update(devicesDO);
        transaction.commit();
        session.close();
    }

    @Override
    public void deleteWhereDeviceIdHashEquals(String deviceIdHash) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

        CriteriaDelete<PasswordlessDevicesDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(PasswordlessDevicesDO.class);

        Root<PasswordlessDevicesDO> root = criteriaDelete.from(PasswordlessDevicesDO.class);
        criteriaDelete.where(criteriaBuilder.equal(root.get("device_id_hash"), deviceIdHash));

        Transaction transaction = session.beginTransaction();
        int rowsUpdated = session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();

        if (rowsUpdated == 0)
            throw new NoResultException();

    }

    @Override
    public void deleteWherePhoneNumberEquals(String phoneNumber) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

        CriteriaDelete<PasswordlessDevicesDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(PasswordlessDevicesDO.class);

        Root<PasswordlessDevicesDO> root = criteriaDelete.from(PasswordlessDevicesDO.class);
        criteriaDelete.where(criteriaBuilder.equal(root.get("phone_number"), phoneNumber));

        Transaction transaction = session.beginTransaction();
        int rowsUpdated = session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();

        if (rowsUpdated == 0)
            throw new NoResultException();
    }

    @Override
    public void deleteWhereEmailEquals(String email) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

        CriteriaDelete<PasswordlessDevicesDO> criteriaDelete = criteriaBuilder
                .createCriteriaDelete(PasswordlessDevicesDO.class);

        Root<PasswordlessDevicesDO> root = criteriaDelete.from(PasswordlessDevicesDO.class);
        criteriaDelete.where(criteriaBuilder.equal(root.get("email"), email));

        Transaction transaction = session.beginTransaction();
        int rowsUpdated = session.createQuery(criteriaDelete).executeUpdate();
        transaction.commit();
        session.close();

        if (rowsUpdated == 0)
            throw new NoResultException();
    }

    @Override
    public List<PasswordlessDevicesDO> getDevicesWhereEmailEquals(String email) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PasswordlessDevicesDO> criteriaQuery = criteriaBuilder.createQuery(PasswordlessDevicesDO.class);
        Root<PasswordlessDevicesDO> root = criteriaQuery.from(PasswordlessDevicesDO.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("email"), email));
        Query<PasswordlessDevicesDO> query = session.createQuery(criteriaQuery);
        List<PasswordlessDevicesDO> result = query.getResultList();
        session.close();

        if (result.size() == 0)
            throw new NoResultException();

        return result;
    }

    @Override
    public List<PasswordlessDevicesDO> getDevicesWherePhoneNumberEquals(String phoneNumber) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PasswordlessDevicesDO> criteriaQuery = criteriaBuilder.createQuery(PasswordlessDevicesDO.class);
        Root<PasswordlessDevicesDO> root = criteriaQuery.from(PasswordlessDevicesDO.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("phone_number"), phoneNumber));
        Query<PasswordlessDevicesDO> query = session.createQuery(criteriaQuery);
        List<PasswordlessDevicesDO> result = query.getResultList();
        session.close();

        if (result.size() == 0)
            throw new NoResultException();

        return result;
    }
}
