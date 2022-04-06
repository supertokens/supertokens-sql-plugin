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
    private Set<Object> entitySet = new HashSet<>();
    SQLStorage.TransactionIsolationLevel currentIsolationLevel = null;

    private void clearNullEntityCache() {
        this.nullEntityCache = new HashMap<>();
    }

    public CustomSessionWrapper(Session session) {
        this.session = session;
    }

    @Override
    public SharedSessionBuilder sessionWithOptions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush() throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFlushMode(FlushModeType flushMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFlushMode(FlushMode flushMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FlushModeType getFlushMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHibernateFlushMode(FlushMode flushMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FlushMode getHibernateFlushMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCacheMode(CacheMode cacheMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CacheMode getCacheMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SessionFactory getSessionFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancelQuery() throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDirty() throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDefaultReadOnly() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDefaultReadOnly(boolean readOnly) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Serializable getIdentifier(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(String entityName, Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void evict(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T load(Class<T> theClass, Serializable id, LockMode lockMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T load(Class<T> theClass, Serializable id, LockOptions lockOptions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object load(String entityName, Serializable id, LockMode lockMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object load(String entityName, Serializable id, LockOptions lockOptions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T load(Class<T> theClass, Serializable id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object load(String entityName, Serializable id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void load(Object object, Serializable id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replicate(Object object, ReplicationMode replicationMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replicate(String entityName, Object object, ReplicationMode replicationMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Serializable save(Object object) {
        this.clearNullEntityCache();
        this.entitySet.add(object);
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
        this.clearNullEntityCache();
        this.entitySet.add(object);
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Object entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void persist(String entityName, Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Object object) {
        throw new UnsupportedOperationException("Use session.delete(Class, Serializable, Object)");
    }

    public <T> void delete(Class<T> theClass, Serializable id, Object object) {
        if (this.contains(object)) {
            // if it comes here, it means that the entity is already loaded up in our entity memory or
            // in hibernate's session memory.
            object = this.get(theClass, id);
        }
        this.clearNullEntityCache();
        this.entitySet.remove(object);
        this.session.delete(object);
    }

    @Override
    public void delete(String entityName, Object object) {
        throw new UnsupportedOperationException("Please use delete(Object)");
    }

    @Override
    public void lock(Object object, LockMode lockMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void lock(String entityName, Object object, LockMode lockMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LockRequest buildLockRequest(LockOptions lockOptions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(String entityName, Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(Object object, LockMode lockMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(Object object, LockOptions lockOptions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(String entityName, Object object, LockOptions lockOptions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LockMode getCurrentLockMode(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query createFilter(Object collection, String queryString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void detach(Object entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object entity) {
        if (this.entitySet.contains(entity)) {
            return true;
        }
        return this.session.contains(entity);
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getProperties() {
        throw new UnsupportedOperationException();
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

    public void updateCache(Object toSave, String entityName, Serializable id) {
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
                // TODO: sql-plugin -> should we remove from entitySet?
            } else {
                if (cacheForEntity != null) {
                    // we remove this ID so that future queries for this ID
                    // check the Hibernate session.
                    cacheForEntity.remove(id);
                }
                this.entitySet.add(toSave);
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
        updateCache(result, entityType.getName(), id);
        return result;
    }

    @Override
    public <T> T get(Class<T> entityType, Serializable id, LockMode lockMode) {
        if (isInNullEntityCache(entityType.getName(), id)) {
            // this means that we had fetched it previously and the db had returned a null value.
            return null;
        }
        T result = this.session.get(entityType, id, lockMode);
        updateCache(result, entityType.getName(), id);
        return result;

    }

    @Override
    public <T> T get(Class<T> entityType, Serializable id, LockOptions lockOptions) {
        throw new UnsupportedOperationException("Please use session.get(..., LockMode) instead");
    }

    @Override
    public Object get(String entityName, Serializable id) {
        throw new UnsupportedOperationException("Please use session.get(Class<T>, ....) instead");
    }

    @Override
    public Object get(String entityName, Serializable id, LockMode lockMode) {
        throw new UnsupportedOperationException("Please use session.get(Class<T>, ....) instead");
    }

    @Override
    public Object get(String entityName, Serializable id, LockOptions lockOptions) {
        throw new UnsupportedOperationException("Please use session.get(Class<T>, ....) instead");
    }

    @Override
    public String getEntityName(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IdentifierLoadAccess byId(String entityName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> MultiIdentifierLoadAccess<T> byMultipleIds(Class<T> entityClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MultiIdentifierLoadAccess byMultipleIds(String entityName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> IdentifierLoadAccess<T> byId(Class<T> entityClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NaturalIdLoadAccess byNaturalId(String entityName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> NaturalIdLoadAccess<T> byNaturalId(Class<T> entityClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleNaturalIdLoadAccess bySimpleNaturalId(String entityName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> SimpleNaturalIdLoadAccess<T> bySimpleNaturalId(Class<T> entityClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Filter enableFilter(String filterName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Filter getEnabledFilter(String filterName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disableFilter(String filterName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SessionStatistics getStatistics() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReadOnly(Object entityOrProxy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setReadOnly(Object entityOrProxy, boolean readOnly) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> RootGraph<T> createEntityGraph(Class<T> rootType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RootGraph<?> createEntityGraph(String graphName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RootGraph<?> getEntityGraph(String graphName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Connection disconnect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reconnect(Connection connection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFetchProfileEnabled(String name) throws UnknownProfileException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enableFetchProfile(String name) throws UnknownProfileException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disableFetchProfile(String name) throws UnknownProfileException {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeHelper getTypeHelper() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LobHelper getLobHelper() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addEventListeners(SessionEventListener... listeners) {
        this.session.addEventListeners(listeners);
    }

    @Override
    public <T> org.hibernate.query.Query<T> createQuery(String queryString, Class<T> resultType) {
        throw new UnsupportedOperationException();
//        if (queryString.toLowerCase().trim().startsWith("select")) {
//            // we ask to use the gte function cause that utilises our null value cache
//            throw new UnsupportedOperationException("Please use session.get instead");
//        }
//        this.clearCustomCache();
//        return this.session.createQuery(queryString, resultType);
    }

    @Override
    public org.hibernate.query.Query createQuery(String queryString) {
        throw new UnsupportedOperationException();
//        if (queryString.toLowerCase().trim().startsWith("select")) {
//            // we ask to use the gte function cause that utilises our null value cache
//            throw new UnsupportedOperationException("Please use session.get instead");
//        }
//        this.clearCustomCache();
//        return this.session.createQuery(queryString);
    }

    @Override
    public org.hibernate.query.Query createNamedQuery(String name) {
        throw new UnsupportedOperationException("Please use session.createQuery instead");
    }

    @Override
    public <T> org.hibernate.query.Query<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        throw new UnsupportedOperationException("Please use session.createQuery instead");
    }

    @Override
    public org.hibernate.query.Query createQuery(CriteriaUpdate updateQuery) {
        throw new UnsupportedOperationException("Please use session.createQuery instead");
    }

    @Override
    public org.hibernate.query.Query createQuery(CriteriaDelete deleteQuery) {
        throw new UnsupportedOperationException("Please use session.createQuery instead");
    }

    @Override
    public <T> org.hibernate.query.Query<T> createNamedQuery(String name, Class<T> resultType) {
        throw new UnsupportedOperationException("Please use session.createQuery instead");
    }

    @Override
    public NativeQuery createNativeQuery(String sqlString, Class resultClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public NativeQuery createSQLQuery(String queryString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NativeQuery createNativeQuery(String sqlString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NativeQuery createNativeQuery(String sqlString, String resultSetMapping) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NativeQuery getNamedNativeQuery(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Session getSession() {
        return this.session.getSession();
    }

    @Override
    public String getTenantIdentifier() {
        throw new UnsupportedOperationException();
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
        this.clearNullEntityCache();
        this.clearCustomEntityCache();
        return this.session.beginTransaction();
    }

    private void clearCustomEntityCache() {
        this.entitySet = new HashSet<>();
    }

    @Override
    public Transaction getTransaction() {
        return this.session.getTransaction();
    }

    @Override
    public org.hibernate.query.Query getNamedQuery(String queryName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProcedureCall getNamedProcedureCall(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName, Class... resultClasses) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String procedureName, String... resultSetMappings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Criteria createCriteria(Class persistentClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Criteria createCriteria(Class persistentClass, String alias) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Criteria createCriteria(String entityName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Criteria createCriteria(String entityName, String alias) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer getJdbcBatchSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJdbcBatchSize(Integer jdbcBatchSize) {
        throw new UnsupportedOperationException();
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
