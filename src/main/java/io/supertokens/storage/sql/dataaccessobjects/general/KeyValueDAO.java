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

package io.supertokens.storage.sql.dataaccessobjects.general;

import io.supertokens.pluginInterface.sqlStorage.SessionObject;
import io.supertokens.storage.sql.dataaccessobjects.SessionTransactionDAO;
import io.supertokens.storage.sql.dataaccessobjects.jwt.JwtSigningInterfaceDAO;
import io.supertokens.storage.sql.domainobjects.emailpassword.EmailPasswordUsersDO;
import io.supertokens.storage.sql.domainobjects.general.KeyValueDO;
import io.supertokens.storage.sql.domainobjects.jwtsigning.JWTSigningKeysDO;
import io.supertokens.storage.sql.domainobjects.passwordless.PasswordlessUsersDO;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.List;

public class KeyValueDAO extends SessionTransactionDAO implements KeyValueInterfaceDAO {
    public KeyValueDAO(SessionObject sessionInstance) {
        super(sessionInstance);
    }

    @Override
    public String insertIntoValues(String name, String value, long created_at_time) {
        KeyValueDO keyValueDO = new KeyValueDO(name, value, created_at_time);

        Session session = (Session) sessionInstance;

        String id = (String) session.save(keyValueDO);

        return id;
    }

    @Override
    public KeyValueDO getWhereNameEquals_transaction(String name) {
        Session session = (Session) sessionInstance;
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<KeyValueDO> criteriaQuery = criteriaBuilder.createQuery(KeyValueDO.class);

        Root<KeyValueDO> root = criteriaQuery.from(KeyValueDO.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.equal(root.get("name"), name));
        Query<KeyValueDO> query = session.createQuery(criteriaQuery).setLockMode(LockModeType.PESSIMISTIC_WRITE);

        KeyValueDO result = null;
        try {
            result = query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
        return result;
    }

    @Override
    public void updateWhereNameEquals_transaction(String name, String value, long createdAtTime) {
        Session session = (Session) sessionInstance;

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaUpdate<KeyValueDO> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(KeyValueDO.class);

        Root<KeyValueDO> root = criteriaUpdate.from(KeyValueDO.class);
        criteriaUpdate.set(root.get("value"), value);
        criteriaUpdate.set(root.get("created_at_time"), createdAtTime);

        criteriaUpdate.where(criteriaBuilder.equal(root.get("name"), name));

        session.createQuery(criteriaUpdate).executeUpdate();
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
        CriteriaQuery<KeyValueDO> criteria = criteriaBuilder.createQuery(KeyValueDO.class);
        Root<KeyValueDO> root = criteria.from(KeyValueDO.class);
        criteria.select(root);
        Query<KeyValueDO> query = session.createQuery(criteria);
        List<KeyValueDO> results = query.getResultList();
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
        CriteriaDelete<KeyValueDO> criteriaDelete = criteriaBuilder.createCriteriaDelete(KeyValueDO.class);
        Root<KeyValueDO> root = criteriaDelete.from(KeyValueDO.class);
        criteriaDelete.where(criteriaBuilder.isNotNull(root.get("name")));
        session.createQuery(criteriaDelete).executeUpdate();
    }
}
