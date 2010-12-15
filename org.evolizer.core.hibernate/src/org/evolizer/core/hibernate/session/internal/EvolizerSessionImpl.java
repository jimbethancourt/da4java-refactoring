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
package org.evolizer.core.hibernate.session.internal;

import java.io.Serializable;
import java.util.List;

import javax.persistence.NonUniqueResultException;

import org.apache.log4j.Logger;
import org.evolizer.core.exceptions.EvolizerRuntimeException;
import org.evolizer.core.hibernate.EvolizerHibernatePlugin;
import org.evolizer.core.hibernate.session.EvolizerSessionHandler;
import org.evolizer.core.hibernate.session.api.IEvolizerSession;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.hql.ast.QuerySyntaxException;

/**
 * This class wraps a Hibernate session and provides behaviour for making objects
 * of Hibernate/ejb3-annotated classes persistent.
 * 
 * @author wuersch
 *
 */
public class EvolizerSessionImpl implements IEvolizerSession {
    private final static Logger logger = EvolizerHibernatePlugin.getLogManager().getLogger(EvolizerSessionImpl.class.getName());
    
    /**
     * The Hibernate session.
     */
    private Session fHibernateSession = null;
    /**
     * The configuration of the Hibernate session.
     */
    private AnnotationConfiguration fHibernateAnnotationConfig = null;
    /**
     * Whenever a transaction is open, its reference is stored here.
     */
    private Transaction fTransaction = null;
    
    /**
     * Constructor. Not intended to be called by clients directly. Use
     * {@link EvolizerSessionFactory#getEvolizerSession(String, String, String, String, String)} instead.
     * 
     * @param dbUrl
     *            database host (e.g. <code>mysql://localhost:3306/evolizer_test</code>)
     * @param dbDialect
     *            database dialect (e.g. <code>org.hibernate.dialect.MySQLDialect</code>)
     * @param dbDriverName
     *            jdbc-compliant database driver (e.g. <code>com.mysql.jdbc.Driver</code>)
     * @param dbUser
     *            database username
     * @param dbPasswd
     *            database password for dbUser
     */
    public EvolizerSessionImpl(String dbUrl,
                           String dbDialect,
                           String dbDriverName,
                           String dbUser,
                           String dbPasswd) {
        
        logger.debug("getEvolizerSession() has been invoked with params '" + 
                dbUrl + "', '" +
                dbDialect + "', '" +
                dbDriverName + "', '" +
                dbUser + "', '" +
                dbPasswd + "'.");
        
        configureDataBaseConnection(dbUrl,
                                    dbDialect,
                                    dbDriverName,
                                    dbUser,
                                    dbPasswd);
    }

    /**
     * Instantiates a new Evolizer session.
     * 
     * @param session
     *            the hibernate session
     */
    public EvolizerSessionImpl(Session session) {
        this.fHibernateSession = session;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isOpen(){
        return (fHibernateSession != null) && fHibernateSession.isOpen();
    }
    
    /**
     * {@inheritDoc}
     */
    public void close() throws EvolizerRuntimeException{
        assertSessionIsOpen();
        
        flush();
        fHibernateSession.close();
        fHibernateSession = null;
    }

    /**
     * {@inheritDoc}
     */
    public void flush() throws EvolizerRuntimeException {
        assertSessionIsOpen();
        
        fHibernateSession.flush();
    }
    
    /**
     * {@inheritDoc}
     */
    public Object merge(Object object){
        assertSessionIsOpen();
        
        return fHibernateSession.merge(object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void clear() throws EvolizerRuntimeException {
        assertSessionIsOpen();
        
        fHibernateSession.clear();
    }

    /**
     * {@inheritDoc}
     */
    public void saveObject(Object saveableObject) throws EvolizerRuntimeException {
        assertSessionIsOpen();
        
        fHibernateSession.save(saveableObject);
    }
    
    /**
     * {@inheritDoc}
     */
    public void saveOrUpdate(Object object) throws EvolizerRuntimeException{
        assertSessionIsOpen();
        
        fHibernateSession.saveOrUpdate(object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void delete(Object object) throws EvolizerRuntimeException{
        assertSessionIsOpen();
        
        fHibernateSession.delete(object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void update(Object updateableObject) throws EvolizerRuntimeException{
        assertSessionIsOpen();
        
        fHibernateSession.update(updateableObject);
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T>List<T> query(String hqlQuery, Class<T> type) throws EvolizerRuntimeException{
        assertSessionIsOpen();
        
        List<T> result;
        try {
            Query query = fHibernateSession.createQuery(hqlQuery);
            result = query.list();
        } catch (QuerySyntaxException qse) {
            throw new EvolizerRuntimeException("Error in query syntax", qse);
        }
        
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T>List<T> query(String hqlQuery, Class<T> type, int maxResults) throws EvolizerRuntimeException{
        assertSessionIsOpen();
        
        List<T> result;
        try {
            Query query = fHibernateSession.createQuery(hqlQuery);
            query.setMaxResults(maxResults);
            result = query.list();
        } catch (QuerySyntaxException qse) {
            throw new EvolizerRuntimeException("Error in query syntax", qse);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public void startTransaction() throws EvolizerRuntimeException{
        assertSessionIsOpen();
        assertTransactionIsNotActive();
        
        fTransaction = fHibernateSession.beginTransaction();
    }

    private void ensureTransactionIsActive() throws EvolizerRuntimeException{
        if(fTransaction == null){
            EvolizerRuntimeException ex =  new EvolizerRuntimeException("No Transaction is active.");
            logger.error("No Transaction is active.", ex);
            
            throw ex;
        }   
    }

    /**
     * {@inheritDoc}
     */
    public void endTransaction() {
        assertSessionIsOpen();
        ensureTransactionIsActive();
        
        fTransaction.commit();
        fTransaction = null;
    }
    
    /**
     * {@inheritDoc}
     */
    public void rollbackTransaction() {
        assertSessionIsOpen();
        ensureTransactionIsActive();
        
        fTransaction.rollback();
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T>T uniqueResult(String hqlQuery, Class<T> type) throws EvolizerRuntimeException{
        assertSessionIsOpen();
        
        Query query = fHibernateSession.createQuery(hqlQuery);
        
        try {
            return (T) query.uniqueResult();
        } catch (NonUniqueResultException e) {
            EvolizerRuntimeException ex =  new EvolizerRuntimeException("Non unique result for uniqueResult query");
            logger.error("Non unique result for uniqueResult query", ex);
            
            throw ex;
        }
    }

    /**
     * Creates and stores the configuration for the Hibernate-session based on the passed parameters. Furthermore, it
     * queries all <code>org.evolizer.hibernate.modelProvider</code> extensions for annotated classes.
     * 
     * @param dbUrl
     *            database host (e.g. <code>mysql://localhost:3306/evolizer_test</code>)
     * @param dbDialect
     *            database dialect (e.g. <code>org.hibernate.dialect.MySQLDialect</code>)
     * @param dbDriverName
     *            jdbc-compliant database driver (e.g. <code>com.mysql.jdbc.Driver</code>)
     * @param dbUser
     *            database username
     * @param dbPasswd
     *            database password for dbUser
     * @deprecated Method has been moved to
     *             {@link EvolizerSessionHandler#configureDataBaseConnection(String, String, String, String, String)}
     */
    @Deprecated
        private void configureDataBaseConnection(String dbUrl,
                                                 String dbDialect,
                                                 String dbDriverName,
                                                 String dbUser,
                                                 String dbPasswd) {
            logger.debug("getEvolizerSession() has been invoked with params '" + 
                    dbUrl + "', '" +
                    dbDialect + "', '" +
                    dbDriverName + "', '" +
                    dbUser + "', '" +
                    dbPasswd + "'.");
            
            fHibernateAnnotationConfig = new AnnotationConfiguration();
    
            fHibernateAnnotationConfig.setProperty("hibernate.connection.url", "jdbc:" + dbUrl);
            fHibernateAnnotationConfig.setProperty("hibernate.connection.username", dbUser);
            fHibernateAnnotationConfig.setProperty("hibernate.connection.password", dbPasswd);
            fHibernateAnnotationConfig.setProperty("hibernate.dialect", dbDialect);
            fHibernateAnnotationConfig.setProperty("hibernate.connection.driver_class", dbDriverName);
    
            fHibernateAnnotationConfig.setProperty("hibernate.jdbc.batch_size", "25");
            fHibernateAnnotationConfig.setProperty("hibernate.cache.use_second_level_cache", "false");
            
    //      fHibernateAnnotationConfig.setProperty("hibernate.show_sql", "true");
            
            List<Class<?>> annotatedClasses = EvolizerHibernatePlugin.getDefault().gatherModels();
            for (Class<?> annotatedClass : annotatedClasses) {
                fHibernateAnnotationConfig.addAnnotatedClass(annotatedClass);
                
                logger.debug("Added annotated class '" + annotatedClass.getCanonicalName() + "' to configuration.");
            }
        }

    private void assertTransactionIsNotActive() throws EvolizerRuntimeException{
        if(fTransaction != null){
            EvolizerRuntimeException ex =  new EvolizerRuntimeException("A Transaction is already active.");
            logger.error("A Transaction is already active.", ex);
            throw ex;
        }   
    }

    private void assertSessionIsOpen() throws EvolizerRuntimeException {
        if(!isOpen()){ 
            EvolizerRuntimeException ex =  new EvolizerRuntimeException("Session is not open.");
            logger.error("Session is not open.", ex);
            throw ex;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T>T load(Class<T> clazz, Long id) throws EvolizerRuntimeException{
        assertSessionIsOpen();
        
        return (T)fHibernateSession.load(clazz, id);
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz, Serializable id) throws EvolizerRuntimeException {
        assertSessionIsOpen();
        
        return (T) fHibernateSession.get(clazz, id);
    }
    
    /**
     * {@inheritDoc}
     */
    public Session getHibernateSession(){
        return fHibernateSession;
    }
}
