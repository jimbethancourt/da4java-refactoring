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
package org.evolizer.core.hibernate.session;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.evolizer.core.exceptions.EvolizerException;
import org.evolizer.core.exceptions.EvolizerRuntimeException;
import org.evolizer.core.hibernate.EvolizerHibernatePlugin;
import org.evolizer.core.hibernate.model.api.IEvolizerModelEntity;
import org.evolizer.core.hibernate.model.api.IEvolizerModelProvider;
import org.evolizer.core.hibernate.session.api.IEvolizerSession;
import org.evolizer.core.hibernate.session.internal.EvolizerSessionImpl;
import org.evolizer.core.preferences.EvolizerPreferences;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

/**
 * Handling of multiple Hibernate sessions using the ThreadLocal sessions.
 * 
 * Multiple Hibernate Sessions are managed by keeping a map of database url to session factory. There are two usage
 * patterns to obtain a session.
 * <ol>
 * <li>Via the Evolizer project properties for Eclipse Evolizer plugins.Using the properties, the current session of the
 * corresponding session factory is obtained. No precedent initialization of the session factory is needed.</li>
 * <li>Manually by providing the session database url as string or in a property object. In this case the session factory needs to be
 * initialized before.</li>
 * </ol>
 * 
 * Some suggestions: According to some articles on the Internet it is suggested to use one session per view. It does not
 * make sense to open and close sessions for each database access. You simply will get dozens of Hibernate exceptions,
 * e.g., because of lazy loading. Therefore, the session should be opened when the view is created and closed when the
 * view is closed.
 * 
 * @author pinzger
 * 
 */
public class EvolizerSessionHandler {

    private final static Logger sfLogger = EvolizerHibernatePlugin.getLogManager().getLogger(EvolizerSessionHandler.class.getName());

    /**
     * Default Hibernate dialect
     */
    private final static String DEFAULT_DIALECT = "org.hibernate.dialect.MySQLDialect";
    /**
     * Default Hibernate database driver
     */
    private final static String DEFAULT_DRIVER = "com.mysql.jdbc.Driver";

    /**
     * Singleton of EvolizerSessionHandler
     */
    private static EvolizerSessionHandler fSessionHandler = null;

    /**
     * Map caching Hibernate session factories. There is one session factory per database.
     */
    private Hashtable<String, SessionFactory> fSessionFactoryMap = new Hashtable<String, SessionFactory>();

    /**
     * Map caching Hibernate sessions. Currently, we follow the strategy to have one session per database.
     */
    private Hashtable<String, IEvolizerSession> fSessionMap = new Hashtable<String, IEvolizerSession>();

    /**
     * Map of dbURL to Hibernate configuration properties. There is one configuration per database.
     */
    private Hashtable<String, Properties> fConnectionPropertiesMap = new Hashtable<String, Properties>();

    /**
     * Hidden default constructor.
     */
    private EvolizerSessionHandler() {}

    /**
     * Factory method creating the single instance of {@link EvolizerSessionHandler}.
     * 
     * @return The single instance of the current {@link EvolizerSessionHandler}
     */
    public static EvolizerSessionHandler getHandler() {
        if (fSessionHandler == null) {
            fSessionHandler = new EvolizerSessionHandler();
        }
        return fSessionHandler;
    }

    /**
     * Obtains the current session for the given the Url of the database. Keep in mind that ThreadLocal has to be
     * activated: current_session_context_class = "thread" This method can only be used after the Hibernate session has
     * been initialized.
     * 
     * @param dbUrl
     *            URL of the database in the form of <code>dbHost/dbName</code>, e.g.,
     *            <code>mysql://localhost:3306/evolizer_test</code>
     * @return the current Hibernate session.
     * @throws EvolizerException
     *             if the {@link IEvolizerSession} could not been initialized/obtained
     */
    public IEvolizerSession getCurrentSession(String dbUrl) throws EvolizerException {
        IEvolizerSession session = null;

        try {
            if (fSessionMap.containsKey(dbUrl) && fSessionMap.get(dbUrl).isOpen()) {
                session = fSessionMap.get(dbUrl);
            } else {
                if (getSessionFactory(dbUrl) != null) {
                    Session hibernateSession = getSessionFactory(dbUrl).openSession();
                    session = new EvolizerSessionImpl(hibernateSession);
                    fSessionMap.put(dbUrl, session);
                } else {
                    //                    sfLogger.error("Evolizer session factory for '" + dbUrl + "' has not been initialized.");
                    throw new EvolizerException("Evolizer session factory for '" + dbUrl + "' has not been initialized.");
                }

            }
        } catch (HibernateException he) {
            throw new EvolizerException(he);
        }

        return session;
    }

    /**
     * Obtains the Hibernate configuration from the project properties, initializes the Hibernate session factory and
     * returns the current Hibernate session.
     * 
     * @param project
     *            the Eclipse project containing Evolizer properties.
     * @return the current Hibernate session.
     * @throws EvolizerException
     *             if the {@link IEvolizerSession} could not been initialized/obtained
     * @see getCurrentSession(Properties)
     */
    public IEvolizerSession getCurrentSession(IProject project) throws EvolizerException {
        IEvolizerSession session = null;

        String dbUrl = getDBUrl(project);
        if (getSessionFactory(dbUrl) == null) {
            initSessionFactory(project);
        }

        session = getCurrentSession(dbUrl);
        return session;
    }

    /**
     * Initialize the Hibernate session factory from the given properties and
     * return the current Hibernate session. For the initialization the dbURL,
     * login, and password must be specified.
     *  
     * @param properties    The connection properties.
     * @return  The opened session.
     * @throws EvolizerException
     */
    public IEvolizerSession getCurrentSession(Properties properties) throws EvolizerException {
        IEvolizerSession session = null;

        if (properties.getProperty("hibernate.connection.url") != null) {
            String dbUrl = properties.getProperty("hibernate.connection.url");
            if (dbUrl.startsWith("jdbc:")) {
                dbUrl = dbUrl.substring("jdbc:".length());
            }
            if (getSessionFactory(dbUrl) == null) {
                initSessionFactory(properties);
            }

            session = getCurrentSession(dbUrl);
        } else {
            throw new EvolizerException("Properties must contain the 'hibernate.connection.url' entry");
        }

        return session;
    }

    /**
     * Initializes the Hibernate session factory with the given dbUrl, dbUser, and dbPassword. Each newly initializes
     * session factory is remembered in the session factory map.
     * 
     * @param dbUrl
     *            Url of the dabatase.
     * @param dbUser
     *            User of the database.
     * @param dbPassword
     *            Password of the user.
     * @throws EvolizerException
     *             if the {@link IEvolizerSession} could not been initialized/obtained
     */
    public void initSessionFactory(String dbUrl, String dbUser, String dbPassword) throws EvolizerException {
        Properties properties = getDefaultDBCofig(dbUrl, "", "", dbUser, dbPassword);
        initSessionFactory(properties);
    }

    /**
     * Initializes the Hibernate session factory with the Evolizer properties of the given project.
     * 
     * @param project   The project.
     * @throws EvolizerException
     */
    public void initSessionFactory(IProject project) throws EvolizerException {
        Properties properties = computeProperties(project);
        initSessionFactory(properties);
    }

    /**
     * Initializes the Hibernate session factory with the given options. Properties must contain an entry
     * for the database connection url.
     * 
     * @param properties  Hibernate configuration parameters
     * @throws EvolizerException  
     */
    public void initSessionFactory(Properties properties) throws EvolizerException {
        try {
            if (properties.getProperty("hibernate.connection.url") != null) {
                String dbUrl = properties.getProperty("hibernate.connection.url");
                if (dbUrl.startsWith("jdbc:")) {
                    dbUrl = dbUrl.substring("jdbc:".length());
                }
                if (!fSessionFactoryMap.containsKey(dbUrl)) {
                    AnnotationConfiguration configuration = configureDataBaseConnection(properties);
                    fSessionFactoryMap.put(dbUrl, configuration.buildSessionFactory());
                    fConnectionPropertiesMap.put(dbUrl, properties);
                }
            } else {
                throw new EvolizerException("Properties must contain the 'hibernate.connection.url' entry");
            }
        } catch (HibernateException he) {
            throw new EvolizerException(he);
        }
    }

    /**
     * Cleanup all open Hibernate sessions. Should be used when an application is closed.
     */
    public void cleanupHibernateSessions() {
        for (String dbUrl : fSessionMap.keySet()) {
            if (fSessionMap.get(dbUrl).isOpen()) {
                fSessionMap.get(dbUrl).close();
                fSessionMap.remove(dbUrl);
            }
        }

        cleanupHibernateSessionFactories();
    }

    /**
     * Cleanup open Hibernate session factories.
     */
    private void cleanupHibernateSessionFactories() {
        for (String dbUrl : fSessionFactoryMap.keySet()) {
            fSessionFactoryMap.get(dbUrl).close();
            fSessionFactoryMap.remove(dbUrl);
            fConnectionPropertiesMap.remove(dbUrl);
        }
    }

    /**
     * Helper function using the dbUrl to obtain the Hibernate session factory from the map. If the session factory has
     * not been initializes before, <code>null</code> is returned.
     * 
     * @param dbUrl
     *            Url (key) of the database.
     * @return the corresponding Hibernate session factory.
     * @throws EvolizerException
     *             if the {@link IEvolizerSession} could not been obtained
     */
    private SessionFactory getSessionFactory(String dbUrl) throws EvolizerException {
        SessionFactory sessionFactory = null;

        if (fSessionFactoryMap.containsKey(dbUrl)) {
            sessionFactory = fSessionFactoryMap.get(dbUrl);
        }

        return sessionFactory;
    }

    /**
     * Return the Hibernate properties for the given dbUrl.
     * 
     * @param dbUrl The database URL.
     * @return  The properties.
     */
    public Properties getProperties(String dbUrl) {
        Properties properties = null;
        if (fConnectionPropertiesMap.containsKey(dbUrl)) {
            properties = fConnectionPropertiesMap.get(dbUrl);
        }

        return properties;
    }

    /**
     * Create database schema.
     * 
     * @param project
     *            the project
     * @throws EvolizerException
     *             the evolizer exception
     * @see createSchema(Properties)
     */
    public void createSchema(IProject project) throws EvolizerException {
        createSchema(computeProperties(project));
    }

    /**
     * Update database schema.
     * 
     * @param project   The project.
     * @throws EvolizerException
     * @see updateSchema(Properties)
     */
    public void updateSchema(IProject project) throws EvolizerException {
        updateSchema(computeProperties(project));
    }

    /**
     * Drop database schema.
     * 
     * @param project   The project.
     * @throws EvolizerException
     * @see dropSchema(Properties)
     */
    public void dropSchema(IProject project) throws EvolizerException {
        dropSchema(computeProperties(project));
    }

    /**
     * Create database schema
     * 
     * @param dbUrl
     *            the db url
     * @param dbDialect
     *            the db dialect
     * @param dbDriverName
     *            the db driver name
     * @param dbUser
     *            the db user
     * @param dbPassword
     *            the db password
     * @throws EvolizerException
     *             if error occurs during o/r mapping
     * @see createSchema(Properties)
     */
    @Deprecated
    public void createSchema(String dbUrl, String dbDialect, String dbDriverName, String dbUser, String dbPassword)
    throws EvolizerException {
        createSchema(getDefaultDBCofig(dbUrl, dbDialect, dbDriverName, dbUser, dbPassword));
    }

    /**
     * Updates database schema.
     * 
     * @param dbUrl
     *            the db url
     * @param dbDialect
     *            the db dialect
     * @param dbDriverName
     *            the db driver name
     * @param dbUser
     *            the db user
     * @param dbPassword
     *            the db password
     * @throws EvolizerException
     * @see updateSchema(Properties)
     */
    @Deprecated
    public void updateSchema(String dbUrl, String dbDialect, String dbDriverName, String dbUser, String dbPassword)
    throws EvolizerException {
        updateSchema(getDefaultDBCofig(dbUrl, dbDialect, dbDriverName, dbUser, dbPassword));
    }

    /**
     * Drop the database schema.
     * 
     * @param dbUrl
     *            the db url
     * @param dbDialect
     *            the db dialect
     * @param dbDriverName
     *            the db driver name
     * @param dbUser
     *            the db user
     * @param dbPassword
     *            the db password
     * @throws EvolizerException
     * @see dropSchema(Properties)
     */
    @Deprecated
    public void dropSchema(String dbUrl, String dbDialect, String dbDriverName, String dbUser, String dbPassword)
    throws EvolizerException {
        dropSchema(getDefaultDBCofig(dbUrl, dbDialect, dbDriverName, dbUser, dbPassword));
    }

    /**
     * Creates the database schema based on the o/r mappings (e.g. the Hibernate/ejb3-annotations). Can only be executed
     * when session is closed.
     * 
     * @param properties    Database connection properties 
     * @throws EvolizerException
     */
    public void createSchema(Properties properties) throws EvolizerException {
        try {
            AnnotationConfiguration configuration = configureDataBaseConnection(properties); 
            SchemaExport exporter = new SchemaExport(configuration);
            exporter.create(false, true);
        } catch (HibernateException he) {
            throw new EvolizerException(he);
        }
    }

    /**
     * Updates a database schema based on the o/r mappings (e.g. the Hibernate/ejb3-annotations). Can only be executed
     * when session is closed.
     *
     * @param properties    Database connection properties 
     * @throws EvolizerException
     */
    public void updateSchema(Properties properties) throws EvolizerException {
        try {
            AnnotationConfiguration configuration = configureDataBaseConnection(properties); 
            SchemaUpdate updater = new SchemaUpdate(configuration);
            updater.execute(false, true);
        } catch (HibernateException he) {
            throw new EvolizerException(he);
        }
    }

    /**
     * Drops the database schema. Can only be executed when session is closed.
     * 
     * @param properties    Database connection properties 
     * @throws EvolizerException
     */
    public void dropSchema(Properties properties) throws EvolizerException {
        try {
            AnnotationConfiguration configuration = configureDataBaseConnection(properties); 
            SchemaExport exporter = new SchemaExport(configuration);
            exporter.drop(false, true);
        } catch (HibernateException he) {
            throw new EvolizerException(he);
        }
    }

    /**
     * Creates the Hibernate configuration from the given properties.
     * 
     * @param properties
     *            the Hibernate properties
     * @return the Hinberate configuration.
     * @throws EvolizerException
     *             if this method fails. Reasons include:
     *             <ul>
     *             <li>The project does not exist.</li>
     *             <li>The project is not local.</li>
     *             <li>The project is a project that is not open.</li>
     *             </ul>
     * @throws EvolizerException
     */
    private AnnotationConfiguration configureDataBaseConnection(Properties properties) throws EvolizerException {
        AnnotationConfiguration configuration = new AnnotationConfiguration();

        try {
            configuration.setProperties(properties);

            for (Class<?> annotatedClass : gatherModels()) {
                configuration.addAnnotatedClass(annotatedClass);
                // sfLogger.debug("Added annotated class '" + annotatedClass.getCanonicalName() +
                // "' to configuration.");
            }
        } catch (MappingException e) {
            sfLogger.error("Error while mapping annotated classes " + e.getMessage(), e);
            throw new EvolizerException(e);
        }

        return configuration;
    }

    /**
     * Creates the Hibernate configuration for the given Eclipse project. Hibernate properties are obtained from the
     * project's Evolizer properties.
     * 
     * @param project
     *            the Eclipse project holding the Evolizer properties.
     * @return the Hinberate configuration.
     * @throws EvolizerException
     */
    private Properties computeProperties(IProject project) throws EvolizerException {
        Properties properties = new Properties();

        try {
            Boolean isInMemoryDBEnabled = false;
            if (project.getPersistentProperty(EvolizerPreferences.DB_USE_INMEMORY) != null) {
                isInMemoryDBEnabled = Boolean.valueOf(project.getPersistentProperty(EvolizerPreferences.DB_USE_INMEMORY));
            }
            if (isInMemoryDBEnabled) {
                properties = getDefaultH2InMemoryConfig(project.getName());
            } else {
                String dbHost = project.getPersistentProperty(EvolizerPreferences.DB_HOST);
                String dbName = project.getPersistentProperty(EvolizerPreferences.DB_NAME);
                String dbUrl = dbHost + "/" + dbName;
                String dbUser = project.getPersistentProperty(EvolizerPreferences.DB_USER);
                String dbPassword = project.getPersistentProperty(EvolizerPreferences.DB_PASSWORD);

                properties.putAll(getDefaultDBCofig(dbUrl, "", "", dbUser, dbPassword));
            }
        } catch (CoreException e) {
            sfLogger.error("Error while fetching persistent properties from project '" + project.getName() + "'."
                    + e.getMessage(), e);
            throw new EvolizerException(e);
        }

        return properties;
    }

    /**
     * Returns the database URL from the given project. The project needs
     * the Evolizer properties to be defined.
     * 
     * @param project   The project.
     * @return  The database URL
     * @throws CoreException
     */
    public String getDBUrl(IProject project) throws EvolizerException {
        Properties properties = computeProperties(project);
        String dbUrl = properties.getProperty("hibernate.connection.url");
        if (dbUrl.startsWith("jdbc:")) {
            dbUrl = dbUrl.substring("jdbc:".length());
        }

        return dbUrl;
    }

    /**
     * Return the default configuration for an SQL-database connection. 
     * 
     * @param dbUrl The URL of the database
     * @param dbDialect The dialect
     * @param dbDriverName  The driver name
     * @param dbUser    The login name
     * @param dbPassword     The password.
     * @return  The default properties.
     */
    public static Properties getDefaultDBCofig(
            String dbUrl,
            String dbDialect,
            String dbDriverName,
            String dbUser,
            String dbPassword) {

        Properties properties = new Properties();

        if (dbDialect == "") {
            dbDialect = DEFAULT_DIALECT;
        }
        if (dbDriverName == "") {
            dbDriverName = DEFAULT_DRIVER;
        }

        properties.setProperty("hibernate.connection.url", "jdbc:" + dbUrl);
        properties.setProperty("hibernate.connection.username", dbUser);
        properties.setProperty("hibernate.connection.password", dbPassword);
        properties.setProperty("hibernate.dialect", dbDialect);
        properties.setProperty("hibernate.connection.driver_class", dbDriverName);

        properties.setProperty("hibernate.jdbc.batch_size", "25");
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");

        // configuration.setProperty("hibernate.current_session_context_class", "thread");
        // configuration.setProperty("hibernate.current_session_context_class", "managed");

        // fHibernateAnnotationConfig.setProperty("hibernate.show_sql", "true");

        return properties;
    }

    /**
     * Returns the default configuration for the HSQLDB in-memory database.
     * 
     * @param dbName    The name of the database.
     * @return  The default properties.
     */
    public static Properties getDefaultHsqldbInMemoryConfig(final String dbName) {
        Properties properties = new Properties();

        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        properties.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
        properties.setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:" + dbName);
        properties.setProperty("hibernate.connection.username", "sa");
        properties.setProperty("hibernate.connection.password", "");
        properties.setProperty("hibernate.connection.pool_size", "1");
        properties.setProperty("hibernate.connection.autocommit", "true");
        //        properties.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.HashtableCacheProvider");
        properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");

        return properties;
    }

    /**
     * Returns the default configuration for the H2 in-memory database.
     * 
     * @param dbName    The name of the database
     * @return  The default properties
     */
    public static Properties getDefaultH2InMemoryConfig(final String dbName) {
        Properties properties = new Properties();

        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        properties.setProperty("hibernate.connection.url", "jdbc:h2:mem:" + dbName);
        properties.setProperty("hibernate.connection.username", "sa");
        properties.setProperty("hibernate.connection.password", "sa");
        //        properties.setProperty("hibernate.connection.pool_size", "1");
        //        properties.setProperty("hibernate.connection.autocommit", "true");
        //        properties.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.HashtableCacheProvider");
        properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");

        return properties;
    }

    /**
     * Queries all model providers and returns ejb3-annotated classes.
     * 
     * @return A list containing classes that are annotated with ejb3-tags for mapping.
     */
    private List<Class<?>> gatherModels() {
        List<Class<?>> annotatedClasses = new ArrayList<Class<?>>();

        // Iterate over all extensions and gather classes that are hibernate annotated
        IExtension[] extensions =
            Platform.getExtensionRegistry().getExtensionPoint(EvolizerHibernatePlugin.PLUGIN_ID, "modelProvider").getExtensions();
        for (IExtension element : extensions) {
            IConfigurationElement[] configElements = element.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                try {
                    IEvolizerModelProvider provider =
                        (IEvolizerModelProvider) configElement.createExecutableExtension("class"); // Throws
                    // CoreException
                    // if executable
                    // could not be
                    // created.
                    Class<?>[] classes = provider.getAnnotatedClasses();
                    for (Class<?> clazz : classes) {
                        if (isModelEntity(clazz)) {
                            annotatedClasses.add(clazz);
                        } else {
                            throw new EvolizerRuntimeException(clazz.getSimpleName()
                                    + " does not implement IEvolizerModelEntity.");
                        }
                    }

                    sfLogger.debug("Added model " + configElement.getAttribute("name"));
                } catch (CoreException exception) {
                    String message =
                        "Could not create executable extension from " + configElement.getContributor() + ". "
                        + exception.getMessage();

                    sfLogger.error(message, exception);

                    throw new EvolizerRuntimeException("Error while initializing log properties.", exception);
                }
            }
        }

        return annotatedClasses;
    }

    /**
     * Check whether the given class is an Evolizer model entity.
     * 
     * @param annotatedClass
     * @return
     */
    private boolean isModelEntity(Class<?> annotatedClass) {
        Class<?>[] interfaces = annotatedClass.getInterfaces();
        for (Class<?> interf : interfaces) {
            if (interf.equals(IEvolizerModelEntity.class)) {
                return true;
            }
        }

        Class<?> superClass = annotatedClass.getSuperclass();
        if (superClass.equals(Object.class)) {
            return false;
        } else {
            return isModelEntity(superClass);
        }
    }
}
