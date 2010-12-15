/*
 * Copyright 2009 University of Zurich, Switzerland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.evolizer.core.hibernate.session.api;

import java.io.Serializable;
import java.util.List;

import org.evolizer.core.exceptions.EvolizerRuntimeException;
import org.hibernate.Session;

/**
 * Persistence providers like our Hibernate plug-in have to provide an implementation of this interface. Like this, we
 * provide a common interface for future persistence solutions such as ontologies or other o/r-mappers.
 * 
 * @author wuersch
 */
public interface IEvolizerSession {

    /**
     * Checks whether the session is open or not.
     * 
     * @return <code>true</code>, if session is open. <code>false</code>, otherwise.
     */
    public abstract boolean isOpen();

    /**
     * Closes the session after making pending changes persistent.
     * 
     * <p>
     * Has to invoke flush before closing the session to ensure that now exception/dataloss occurs.
     * 
     * @throws EvolizerRuntimeException
     *             if session is not open.
     */
    public abstract void close() throws EvolizerRuntimeException;

    /**
     * Flushes the session.
     * 
     * @throws EvolizerRuntimeException
     *             if session is not open.
     */
    public abstract void flush() throws EvolizerRuntimeException;

    /**
     * Clears the session.
     * 
     * @throws EvolizerRuntimeException
     *             if session is not open.
     */
    public abstract void clear() throws EvolizerRuntimeException;

    /**
     * Saves the object.
     * 
     * @param saveableObject
     *            an instance of a Hibernate/ejb3-annotated class.
     * @throws EvolizerRuntimeException
     *             if session is not open.
     */
    public abstract void saveObject(Object saveableObject) throws EvolizerRuntimeException;

    /**
     * Saves or updates the object.
     * 
     * @param object
     *            the object
     * @throws EvolizerRuntimeException
     *             if session is not open.
     */
    public abstract void saveOrUpdate(Object object) throws EvolizerRuntimeException;

    /**
     * Deletes the object.
     * 
     * @param object
     *            an instance of a Hibernate/ejb3-annotated class.
     * @throws EvolizerRuntimeException
     *             if session is not open.
     */
    public abstract void delete(Object object) throws EvolizerRuntimeException;

    /**
     * Generic method. Executes a hql query and returns the results.
     * 
     * @param hqlQuery
     *            the query string.
     * @param <T>
     *            the parameterized type of the returned {@link List}
     * @param type
     *            the type
     * @return a list of objects of the type <code>T</code> that match the query
     * @throws EvolizerRuntimeException
     *             if session is not open.
     */
    public abstract <T> List<T> query(String hqlQuery, Class<T> type) throws EvolizerRuntimeException;

    /**
     * Generic method. Executes a hql query and returns the results, but limits the number of results obtained from the
     * database. This is similar to the <code>LIMIT</code> is the standard SQL.
     * 
     * @param <T>
     *            the parameterized type of the returned {@link List}
     * @param hqlQuery
     *            the query string.
     * @param maxResults
     *            number of results obtained from the database
     * @param type
     *            the type
     * @return a list of objects of the type <code>T</code> that match the query
     * @throws EvolizerRuntimeException
     *             if session is not open.
     */
    public <T> List<T> query(String hqlQuery, Class<T> type, int maxResults) throws EvolizerRuntimeException;

    /**
     * Loads an object by its class and id.
     * 
     * @param <T>
     *            the parameterized type of the returned object
     * @param clazz
     *            the class
     * @param id
     *            the id
     * @return the object.
     * @throws EvolizerRuntimeException
     *             if session is not open.
     */
    public <T> T load(Class<T> clazz, Long id) throws EvolizerRuntimeException;

    /**
     * Starts a transaction. Only one transaction per session can be active at any given time.
     * 
     * <p>
     * Invokers eventually have to call {@link #endTransaction()}.
     * 
     * @throws EvolizerRuntimeException
     *             if session is not open or transaction already active.
     */
    public abstract void startTransaction() throws EvolizerRuntimeException;

    /**
     * Commits the transaction.
     * 
     * @throws EvolizerRuntimeException
     *             if session is not open or no transaction is active
     */
    public abstract void endTransaction() throws EvolizerRuntimeException;

    /**
     * Rolls back the active transaction.
     * 
     * @throws EvolizerRuntimeException
     *             if session is not open or no transaction is active
     */
    public abstract void rollbackTransaction() throws EvolizerRuntimeException;

    /**
     * Generic convenience method that can be used whenever a hql-query is intended to return only one result.
     * 
     * @param <T>
     *            The type of the result
     * @param hqlQuery
     *            the hql query
     * @param type
     *            the type
     * @return the result
     * @throws EvolizerRuntimeException
     *             if session is not open or more than one result was found.
     */
    public abstract <T> T uniqueResult(String hqlQuery, Class<T> type);

    /**
     * Updates the object.
     * 
     * @param object
     *            the object
     * @throws EvolizerRuntimeException
     *             if session is not open.
     */
    public abstract void update(Object object) throws EvolizerRuntimeException;

    /**
     * Merges the object. Useful during batch-processing where regular flushing/clearing is necessary due to memory
     * restrictions. If e.g. {@link #saveOrUpdate(Object)} is invoked instead (at least while using Hibernate), we often
     * experience exceptions.
     * 
     * @param object
     *            the object
     * @return the object
     * @throws EvolizerRuntimeException
     *             if session is not open.
     */
    public abstract Object merge(Object object) throws EvolizerRuntimeException;

    /**
     * Return the persistent instance of the given entity class with the given identifier.
     * 
     * @param clazz
     *            the clazz
     * @param id
     *            the id
     * @param <T>
     *            expected type
     * @return the persistent instance of the given entity class with the given identifier, or <code>null</code> if
     *         there is no such persistent instance.
     * @throws EvolizerRuntimeException
     *             if session is not open.
     */
    public abstract <T> T get(Class<T> clazz, Serializable id) throws EvolizerRuntimeException;

    /**
     * Returns the encapsulated Hibernate session to enable access of Hibernate-specific functionality. It is not
     * recommended to use the Hibernated session directly, but sometimes it is mandatory, e.g., to use Hibernate
     * Criteria.
     * 
     * @return the Hibernate session
     */
    public abstract Session getHibernateSession();
}
