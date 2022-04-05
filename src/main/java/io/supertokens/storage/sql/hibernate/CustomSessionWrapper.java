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

package io.supertokens.storage.sql.hibernate;

import io.supertokens.pluginInterface.sqlStorage.SQLStorage;
import org.hibernate.*;
import org.hibernate.graph.RootGraph;
import org.hibernate.internal.SessionImpl;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.query.NativeQuery;
import org.hibernate.stat.SessionStatistics;

import javax.annotation.Nullable;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.supertokens.pluginInterface.sqlStorage.SQLStorage.TransactionIsolationLevel.SERIALIZABLE;

public class CustomSessionWrapper implements Session {

    final Session session;

    // Example entry "KeyValueDO" -> {name1, name2, ..}
    private Map<String, Set<Serializable>> nullEntityCache = new HashMap<>();
    SQLStorage.TransactionIsolationLevel currentIsolationLevel = null;

    public CustomSessionWrapper(Session session) {
        this.session = session;
    }

    @Override
    public SharedSessionBuilder sessionWithOptions() {
        return this.session.sessionWithOptions();
    }

    @Override
    public void flush() throws HibernateException {
        this.session.flush();
    }

    @Override
    public void setFlushMode(FlushModeType flushMode) {
        this.session.setFlushMode(flushMode);
    }

    @Override
    public void setFlushMode(FlushMode flushMode) {
        this.session.setFlushMode(flushMode);
    }

    @Override
    public FlushModeType getFlushMode() {
        return this.session.getFlushMode();
    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {
        this.session.lock(entity, lockMode);
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        this.session.lock(entity, lockMode, properties);
    }

    @Override
    public void setHibernateFlushMode(FlushMode flushMode) {
        this.session.setHibernateFlushMode(flushMode);
    }

    @Override
    public FlushMode getHibernateFlushMode() {
        return this.session.getHibernateFlushMode();
    }

    @Override
    public void setCacheMode(CacheMode cacheMode) {
        this.session.setCacheMode(cacheMode);
    }

    @Override
    public CacheMode getCacheMode() {
        return this.session.getCacheMode();
    }

    @Override
    public SessionFactory getSessionFactory() {
        return this.session.getSessionFactory();
    }

    @Override
    public void cancelQuery() throws HibernateException {
        this.session.cancelQuery();
    }

    @Override
    public boolean isDirty() throws HibernateException {
        return this.session.isDirty();
    }

    @Override
    public boolean isDefaultReadOnly() {
        return this.session.isDefaultReadOnly();
    }

    @Override
    public void setDefaultReadOnly(boolean readOnly) {
        this.session.setDefaultReadOnly(readOnly);
    }

    @Override
    public Serializable getIdentifier(Object object) {
        return this.session.getIdentifier(object);
    }

    @Override
    public boolean contains(String entityName, Object object) {
        return this.session.contains(entityName, object);
    }

    @Override
    public void evict(Object object) {
        this.session.evict(object);
    }

    @Override
    public <T> T load(Class<T> theClass, Serializable id, LockMode lockMode) {
        return this.session.load(theClass, id, lockMode);
    }

    @Override
    public <T> T load(Class<T> theClass, Serializable id, LockOptions lockOptions) {
        return this.session.load(theClass, id, lockOptions);
    }

    @Override
    public Object load(String entityName, Serializable id, LockMode lockMode) {
        return this.session.load(entityName, id, lockMode);
    }

    @Override
    public Object load(String entityName, Serializable id, LockOptions lockOptions) {
        return this.session.load(entityName, id, lockOptions);
    }

    @Override
    public <T> T load(Class<T> theClass, Serializable id) {
        return this.session.load(theClass, id);
    }

    @Override
    public Object load(String entityName, Serializable id) {
        return this.session.load(entityName, id);
    }

    @Override
    public void load(Object object, Serializable id) {
        this.session.load(object, id);
    }

    @Override
    public void replicate(Object object, ReplicationMode replicationMode) {
        this.session.replicate(object, replicationMode);
    }

    @Override
    public void replicate(String entityName, Object object, ReplicationMode replicationMode) {
        this.session.replicate(entityName, object, replicationMode);
    }

    @Override
    public Serializable save(Object object) {
        this.nullEntityCache.remove(object.getClass().getName());
        return this.session.save(object);
    }

    @Override
    public Serializable save(String entityName, Object object) {
        this.nullEntityCache.remove(entityName);
        return this.session.save(entityName, object);
    }

    @Override
    public void saveOrUpdate(Object object) {
        throw new UnsupportedOperationException("Please do not use this function. Instead use save or update");
    }

    @Override
    public void saveOrUpdate(String entityName, Object object) {
        throw new UnsupportedOperationException("Please do not use this function. Instead use save or update");
    }

    @Override
    public void update(Object object) {
        this.nullEntityCache.remove(object.getClass().getName());
        this.session.update(object);
    }

    @Override
    public void update(String entityName, Object object) {
        this.nullEntityCache.remove(entityName);
        this.session.update(entityName, object);
    }

    @Override
    public Object merge(Object object) {
        throw new UnsupportedOperationException("Please do not use this function. Instead use save or update");
    }

    @Override
    public Object merge(String entityName, Object object) {
        throw new UnsupportedOperationException("Please do not use this function. Instead use save or update");
    }

    @Override
    public void persist(Object object) {
        this.session.persist(object);
    }

    @Override
    public void remove(Object entity) {
        this.session.remove(entity);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return this.session.find(entityClass, primaryKey);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return this.session.find(entityClass, primaryKey, properties);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return this.session.find(entityClass, primaryKey, lockMode);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return this.session.find(entityClass, primaryKey, lockMode, properties);
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return this.session.getReference(entityClass, primaryKey);
    }

    @Override
    public void persist(String entityName, Object object) {
        this.session.persist(entityName, object);
    }

    @Override
    public void delete(Object object) {
        this.nullEntityCache.remove(object.getClass().getName());
        this.session.delete(object);
    }

    @Override
    public void delete(String entityName, Object object) {
        this.nullEntityCache.remove(entityName);
        this.session.delete(entityName, object);
    }

    @Override
    public void lock(Object object, LockMode lockMode) {
        this.session.lock(object, lockMode);
    }

    @Override
    public void lock(String entityName, Object object, LockMode lockMode) {
        this.session.lock(entityName, object, lockMode);
    }

    @Override
    public LockRequest buildLockRequest(LockOptions lockOptions) {
        return this.session.buildLockRequest(lockOptions);
    }

    @Override
    public void refresh(Object object) {
        this.session.refresh(object);
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        this.session.refresh(entity, properties);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        this.session.refresh(entity, lockMode);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        this.session.refresh(entity, lockMode, properties);
    }

    @Override
    public void refresh(String entityName, Object object) {
        this.session.refresh(entityName, object);
    }

    @Override
    public void refresh(Object object, LockMode lockMode) {
        this.session.refresh(object, lockMode);
    }

    @Override
    public void refresh(Object object, LockOptions lockOptions) {
        this.session.refresh(object, lockOptions);
    }

    @Override
    public void refresh(String entityName, Object object, LockOptions lockOptions) {
        this.session.refresh(entityName, object, lockOptions);
    }

    @Override
    public LockMode getCurrentLockMode(Object object) {
        return this.session.getCurrentLockMode(object);
    }

    @Override
    public Query createFilter(Object collection, String queryString) {
        return this.session.createFilter(collection, queryString);
    }

    @Override
    public void clear() {
        this.session.clear();
    }

    @Override
    public void detach(Object entity) {
        this.session.detach(entity);
    }

    @Override
    public boolean contains(Object entity) {
        return this.session.contains(entity);
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        return this.session.getLockMode(entity);
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        this.session.setProperty(propertyName, value);
    }

    @Override
    public Map<String, Object> getProperties() {
        return this.session.getProperties();
    }

    public boolean isInNullEntityCache(String entityName, Serializable id) {
        if (this.currentIsolationLevel == SERIALIZABLE
                || this.currentIsolationLevel == SQLStorage.TransactionIsolationLevel.REPEATABLE_READ) {
            // here we know that the db state will not change if we read again, so we try to do that.
            Set<Serializable> cacheForEntity = this.nullEntityCache.get(entityName);
            if (cacheForEntity == null) {
                return false;
            }
            return cacheForEntity.contains(id);
        } else {
            // attempt to read from db again.
            return false;
        }
    }

    public void updateNullEntityCache(Object toSave, String entityName, Serializable id) {
        if (this.currentIsolationLevel == SERIALIZABLE
                || this.currentIsolationLevel == SQLStorage.TransactionIsolationLevel.REPEATABLE_READ) {
            Set<Serializable> cacheForEntity = this.nullEntityCache.get(entityName);
            if (toSave == null) {
                if (cacheForEntity == null) {
                    cacheForEntity = new HashSet<>();
                }
                // we add this ID so that future queries with this ID
                // don't need to query the db
                cacheForEntity.add(id);
            } else if (cacheForEntity != null) {
                // we remove this ID so that future queries for this ID
                // check the Hibernate session.
                cacheForEntity.remove(id);
            }
            if (cacheForEntity != null) {
                this.nullEntityCache.put(entityName, cacheForEntity);
            }
        }
    }

    @Override
    public <T> T get(Class<T> entityType, Serializable id) {
        if (isInNullEntityCache(entityType.getName(), id)) {
            // this means that we had fetched it previously and the db had returned a null value.
            return null;
        }
        T result = this.session.get(entityType, id);
        updateNullEntityCache(result, entityType.getName(), id);
        return result;
    }

    @Override
    public <T> T get(Class<T> entityType, Serializable id, LockMode lockMode) {
        if (isInNullEntityCache(entityType.getName(), id)) {
            // this means that we had fetched it previously and the db had returned a null value.
            return null;
        }
        T result = this.session.get(entityType, id, lockMode);
        updateNullEntityCache(result, entityType.getName(), id);
        return result;

    }

    @Override
    public <T> T get(Class<T> entityType, Serializable id, LockOptions lockOptions) {
        if (isInNullEntityCache(entityType.getName(), id)) {
            // this means that we had fetched it previously and the db had returned a null value.
            return null;
        }
        T result = this.session.get(entityType, id, lockOptions);
        updateNullEntityCache(result, entityType.getName(), id);
        return result;
    }

    @Override
    public Object get(String entityName, Serializable id) {
        if (isInNullEntityCache(entityName, id)) {
            // this means that we had fetched it previously and the db had returned a null value.
            return null;
        }
        Object result = this.session.get(entityName, id);
        updateNullEntityCache(result, entityName, id);
        return result;
    }

    @Override
    public Object get(String entityName, Serializable id, LockMode lockMode) {
        if (isInNullEntityCache(entityName, id)) {
            // this means that we had fetched it previously and the db had returned a null value.
            return null;
        }
        Object result = this.session.get(entityName, id, lockMode);
        updateNullEntityCache(result, entityName, id);
        return result;
    }

    @Override
    public Object get(String entityName, Serializable id, LockOptions lockOptions) {
        if (isInNullEntityCache(entityName, id)) {
            // this means that we had fetched it previously and the db had returned a null value.
            return null;
        }
        Object result = this.session.get(entityName, id, lockOptions);
        updateNullEntityCache(result, entityName, id);
        return result;
    }

    @Override
    public String getEntityName(Object object) {
        return this.session.getEntityName(object);
    }

    @Override
    public IdentifierLoadAccess byId(String entityName) {
        return this.session.byId(entityName);
    }

    @Override
    public <T> MultiIdentifierLoadAccess<T> byMultipleIds(Class<T> entityClass) {
        return this.session.byMultipleIds(entityClass);
    }

    @Override
    public MultiIdentifierLoadAccess byMultipleIds(String entityName) {
        return this.session.byMultipleIds(entityName);
    }

    @Override
    public <T> IdentifierLoadAccess<T> byId(Class<T> entityClass) {
        return this.session.byId(entityClass);
    }

    @Override
    public NaturalIdLoadAccess byNaturalId(String entityName) {
        return this.session.byNaturalId(entityName);
    }

    @Override
    public <T> NaturalIdLoadAccess<T> byNaturalId(Class<T> entityClass) {
        return this.session.byNaturalId(entityClass);
    }

    @Override
    public SimpleNaturalIdLoadAccess bySimpleNaturalId(String entityName) {
        return this.session.bySimpleNaturalId(entityName);
    }

    @Override
    public <T> SimpleNaturalIdLoadAccess<T> bySimpleNaturalId(Class<T> entityClass) {
        return this.session.bySimpleNaturalId(entityClass);
    }

    @Override
    public Filter enableFilter(String filterName) {
        return this.session.enableFilter(filterName);
    }

    @Override
    public Filter getEnabledFilter(String filterName) {
        return this.session.getEnabledFilter(filterName);
    }

    @Override
    public void disableFilter(String filterName) {
        this.session.disableFilter(filterName);
    }

    @Override
    public SessionStatistics getStatistics() {
        return this.session.getStatistics();
    }

    @Override
    public boolean isReadOnly(Object entityOrProxy) {
        return this.session.isReadOnly(entityOrProxy);
    }

    @Override
    public void setReadOnly(Object entityOrProxy, boolean readOnly) {
        this.session.setReadOnly(entityOrProxy, readOnly);
    }

    @Override
    public <T> RootGraph<T> createEntityGraph(Class<T> rootType) {
        return this.session.createEntityGraph(rootType);
    }

    @Override
    public RootGraph<?> createEntityGraph(String graphName) {
        return this.session.createEntityGraph(graphName);
    }

    @Override
    public RootGraph<?> getEntityGraph(String graphName) {
        return this.session.getEntityGraph(graphName);
    }

    @Override
    public Connection disconnect() {
        return this.session.disconnect();
    }

    @Override
    public void reconnect(Connection connection) {
        this.session.reconnect(connection);
    }

    @Override
    public boolean isFetchProfileEnabled(String name) throws UnknownProfileException {
        return this.session.isFetchProfileEnabled(name);
    }

    @Override
    public void enableFetchProfile(String name) throws UnknownProfileException {
        this.session.enableFetchProfile(name);
    }

    @Override
    public void disableFetchProfile(String name) throws UnknownProfileException {
        this.session.disableFetchProfile(name);
    }

    @Override
    public TypeHelper getTypeHelper() {
        return this.session.getTypeHelper();
    }

    @Override
    public LobHelper getLobHelper() {
        return this.session.getLobHelper();
    }

    @Override
    public void addEventListeners(SessionEventListener... listeners) {
        this.session.addEventListeners(listeners);
    }

    @Override
    public <T> org.hibernate.query.Query<T> createQuery(String queryString, Class<T> resultType) {
        return this.session.createQuery(queryString, resultType);
    }

    @Override
    public org.hibernate.query.Query createNamedQuery(String name) {
        return this.session.createNamedQuery(name);
    }

    @Override
    public <T> org.hibernate.query.Query<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return this.session.createQuery(criteriaQuery);
    }

    @Override
    public org.hibernate.query.Query createQuery(CriteriaUpdate updateQuery) {
        return this.session.createQuery(updateQuery);
    }

    @Override
    public org.hibernate.query.Query createQuery(CriteriaDelete deleteQuery) {
        return this.session.createQuery(deleteQuery);
    }

    @Override
    public <T> org.hibernate.query.Query<T> createNamedQuery(String name, Class<T> resultType) {
        return this.session.createNamedQuery(name, resultType);
    }

    @Override
    public NativeQuery createNativeQuery(String sqlString, Class resultClass) {
        return this.session.createNativeQuery(sqlString, resultClass);
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        return this.session.createNamedStoredProcedureQuery(name);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        return this.session.createStoredProcedureQuery(procedureName);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        return this.session.createStoredProcedureQuery(procedureName, resultClasses);
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        return this.session.createStoredProcedureQuery(procedureName, resultSetMappings);
    }

    @Override
    public void joinTransaction() {
        this.session.joinTransaction();
    }

    @Override
    public boolean isJoinedToTransaction() {
        return this.session.isJoinedToTransaction();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return this.session.unwrap(cls);
    }

    @Override
    public Object getDelegate() {
        return this.session.getDelegate();
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return this.session.getEntityManagerFactory();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return this.session.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return this.session.getMetamodel();
    }

    @Override
    public NativeQuery createSQLQuery(String queryString) {
        return this.session.createSQLQuery(queryString);
    }

    @Override
    public NativeQuery createNativeQuery(String sqlString) {
        return this.session.createNativeQuery(sqlString);
    }

    @Override
    public NativeQuery createNativeQuery(String sqlString, String resultSetMapping) {
        return this.session.createNativeQuery(sqlString, resultSetMapping);
    }

    @Override
    public NativeQuery getNamedNativeQuery(String name) {
        return this.session.getNamedNativeQuery(name);
    }

    @Override
    public Session getSession() {
        return this.session.getSession();
    }

    @Override
    public String getTenantIdentifier() {
        return this.session.getTenantIdentifier();
    }

    @Override
    public void close() {
        this.session.close();
    }

    @Override
    public boolean isOpen() {
        return this.session.isOpen();
    }

    @Override
    public boolean isConnected() {
        return this.session.isConnected();
    }

    @Override
    public Transaction beginTransaction() {
        throw new UnsupportedOperationException("Please use beginTransaction(isolationLevel) function instead");
    }

    public Transaction beginTransaction(@Nullable SQLStorage.TransactionIsolationLevel isolationLevel)
            throws SQLException {
        this.setIsolationLevel(isolationLevel);
        this.nullEntityCache = new HashMap<>();
        return this.session.beginTransaction();
    }

    @Override
    public Transaction getTransaction() {
        return this.session.getTransaction();
    }

    @Override
    public org.hibernate.query.Query createQuery(String queryString) {
        return this.session.createQuery(queryString);
    }

    @Override
    public org.hibernate.query.Query getNamedQuery(String queryName) {
        return this.session.getNamedQuery(queryName);
    }

    @Override
    public ProcedureCall getNamedProcedureCall(String name) {
        return this.session.getNamedProcedureCall(name);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName) {
        return this.session.createStoredProcedureCall(procedureName);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName, Class... resultClasses) {
        return this.session.createStoredProcedureCall(procedureName, resultClasses);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName, String... resultSetMappings) {
        return this.session.createStoredProcedureCall(procedureName, resultSetMappings);
    }

    @Override
    public Criteria createCriteria(Class persistentClass) {
        return this.session.createCriteria(persistentClass);
    }

    @Override
    public Criteria createCriteria(Class persistentClass, String alias) {
        return this.session.createCriteria(persistentClass, alias);
    }

    @Override
    public Criteria createCriteria(String entityName) {
        return this.session.createCriteria(entityName);
    }

    @Override
    public Criteria createCriteria(String entityName, String alias) {
        return this.session.createCriteria(entityName, alias);
    }

    @Override
    public Integer getJdbcBatchSize() {
        return this.session.getJdbcBatchSize();
    }

    @Override
    public void setJdbcBatchSize(Integer jdbcBatchSize) {
        this.session.setJdbcBatchSize(jdbcBatchSize);
    }

    public SessionImpl getSessionImpl() {
        return (SessionImpl) this.session;
    }

    private void setIsolationLevel(@Nullable SQLStorage.TransactionIsolationLevel isolationLevel) throws SQLException {
        Connection con = this.getSessionImpl().connection();

        if (isolationLevel != null) {
            this.currentIsolationLevel = isolationLevel;
            int libIsolationLevel = Connection.TRANSACTION_SERIALIZABLE;
            switch (isolationLevel) {
            case SERIALIZABLE:
                break;
            case REPEATABLE_READ:
                libIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ;
                break;
            case READ_COMMITTED:
                libIsolationLevel = Connection.TRANSACTION_READ_COMMITTED;
                break;
            case READ_UNCOMMITTED:
                libIsolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;
                break;
            case NONE:
                libIsolationLevel = Connection.TRANSACTION_NONE;
                break;
            }
            con.setTransactionIsolation(libIsolationLevel);
        } else {
            this.currentIsolationLevel = SERIALIZABLE;
        }
    }
}
