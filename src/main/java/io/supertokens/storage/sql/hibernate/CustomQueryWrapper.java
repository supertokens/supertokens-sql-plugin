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

import org.hibernate.*;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.graph.GraphSemantic;
import org.hibernate.graph.RootGraph;
import org.hibernate.query.ParameterMetadata;
import org.hibernate.query.Query;
import org.hibernate.query.QueryParameter;
import org.hibernate.query.QueryProducer;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.Type;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

public class CustomQueryWrapper<R> implements Query<R> {

    final Query<R> query;
    final CustomSessionWrapper session;

    public CustomQueryWrapper(Query<R> query, CustomSessionWrapper session) {
        this.query = query;
        this.session = session;
    }

    @Override
    public CustomQueryWrapper<R> setParameter(String name, Object value) {
        query.setParameter(name, value);
        return this;
    }

    @Override
    public int executeUpdate() {
        return this.query.executeUpdate();
    }

    @Override
    public CustomQueryWrapper<R> setLockMode(LockModeType lockMode) {
        this.query.setLockMode(lockMode);
        return this;
    }

    // Returns list from db as well as saves it in the custom cache.
    // Pass null as ID if it shouldn't be saved.
    @Override
    public List<R> list(/* GetPrimaryKey<R> getPrimaryKeyFunc */) {
        List<R> result = this.query.list();
        // the below doesn't seem to be required as the result of query.list() seems to be put in
        // hibernate's L1 cache as well.
//        for (R r : result) {
//            Serializable pk = getPrimaryKeyFunc.op(r);
//            if (pk != null) {
//                session.updateCache(r, r.getClass().getName(), pk);
//            }
//        }
        return result;
    }

    @Override
    public CustomQueryWrapper<R> setParameterList(String name, Object[] values) {
        this.query.setParameterList(name, values);
        return this;
    }

    @Override
    public Query<R> setParameterList(String name, Collection values) {
        this.query.setParameterList(name, values);
        return this;
    }

    @Override
    public CustomQueryWrapper<R> setMaxResults(int maxResult) {
        this.query.setMaxResults(maxResult);
        return this;
    }

    public static interface GetPrimaryKey<R> {
        Serializable op(R item);
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    // UNSUPPORTED FUNCTIONS BELOW....................

//    @Override
//    public List<R> list() {
//        throw new UnsupportedOperationException("Please use list(GetPrimaryKey<R> getPrimaryKeyFunc)");
//    }

    @Override
    public QueryProducer getProducer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<R> uniqueResultOptional() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<R> stream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> applyGraph(RootGraph graph, GraphSemantic semantic) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(Parameter<Instant> param, Instant value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(Parameter<LocalDateTime> param, LocalDateTime value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(Parameter<ZonedDateTime> param, ZonedDateTime value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(Parameter<OffsetDateTime> param, OffsetDateTime value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(String name, Instant value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(String name, LocalDateTime value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(String name, ZonedDateTime value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(String name, OffsetDateTime value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(int position, Instant value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(int position, LocalDateTime value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(int position, ZonedDateTime value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(int position, OffsetDateTime value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScrollableResults scroll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScrollableResults scroll(ScrollMode scrollMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public R uniqueResult() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FlushMode getHibernateFlushMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CacheMode getCacheMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCacheRegion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer getFetchSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LockOptions getLockOptions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getComment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getQueryString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RowSelection getQueryOptions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ParameterMetadata getParameterMetadata() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getNamedParameters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxResults() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setFirstResult(int startPosition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFirstResult() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setHint(String hintName, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getHints() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Query<R> setParameter(Parameter<T> param, T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(String name, Object val, Type type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(String name, Calendar value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(String name, Date value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(int position, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(int position, Calendar value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(int position, Date value, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Parameter<?>> getParameters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Parameter<?> getParameter(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Parameter<?> getParameter(int position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Parameter<T> getParameter(int position, Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBound(Parameter<?> param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getParameterValue(Parameter<T> param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getParameterValue(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getParameterValue(int position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Query<R> setParameter(QueryParameter<T> parameter, T val) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <P> Query<R> setParameter(int position, P val, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <P> Query<R> setParameter(QueryParameter<P> parameter, P val, Type type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameter(int position, Object val, Type type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <P> Query<R> setParameter(QueryParameter<P> parameter, P val, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <P> Query<R> setParameter(String name, P val, TemporalType temporalType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setFlushMode(FlushModeType flushMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FlushModeType getFlushMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LockModeType getLockMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setReadOnly(boolean readOnly) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type[] getReturnTypes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setHibernateFlushMode(FlushMode flushMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setCacheMode(CacheMode cacheMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCacheable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setCacheable(boolean cacheable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setCacheRegion(String cacheRegion) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer getTimeout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setTimeout(int timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setFetchSize(int fetchSize) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReadOnly() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setLockOptions(LockOptions lockOptions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setLockMode(String alias, LockMode lockMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setComment(String comment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> addQueryHint(String hint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<R> iterate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <P> Query<R> setParameterList(QueryParameter<P> parameter, Collection<P> values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public org.hibernate.Query<R> setParameterList(int position, Collection values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameterList(String name, Collection values, Type type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public org.hibernate.Query<R> setParameterList(int position, Collection values, Type type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setParameterList(String name, Object[] values, Type type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public org.hibernate.Query<R> setParameterList(int position, Object[] values, Type type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public org.hibernate.Query<R> setParameterList(int position, Object[] values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setProperties(Object bean) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setProperties(Map bean) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setEntity(int position, Object val) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setEntity(String name, Object val) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type determineProperBooleanType(int position, Object value, Type defaultType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type determineProperBooleanType(String name, Object value, Type defaultType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<R> setResultTransformer(ResultTransformer transformer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getReturnAliases() {
        throw new UnsupportedOperationException();
    }
}
