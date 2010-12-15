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

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.evolizer.core.exceptions.EvolizerException;
import org.evolizer.core.hibernate.EvolizerHibernatePlugin;
import org.evolizer.core.hibernate.session.api.IEvolizerSession;
import org.evolizer.core.preferences.EvolizerPreferences;

/**
 * A factory for creating {@link EvolizerSessionFactory} objects.
 * 
 * @author wuersch
 */
public final class EvolizerSessionFactory {

    private static Logger sLogger =
            EvolizerHibernatePlugin.getLogManager().getLogger(EvolizerSessionFactory.class.getName());

    private EvolizerSessionFactory() {}

    /**
     * Returns an implementation of {@link IEvolizerSession} configured using the persistent properties of the passed
     * {@link IProject}.
     * 
     * @param project
     *            to gain database configuration of
     * @return an implementation of {@link EvolizerSessionFactory}
     * @throws EvolizerException
     *             if session could not be created because preference store of project is not accessible. Reasons
     *             include
     *             <ul>
     *             <li>Project does not exist.</li>
     *             <li>Project is not local.</li>
     *             <li>Project is not open.</li>
     *             </ul>
     */
    public static IEvolizerSession getEvolizerSession(IProject project) throws EvolizerException {
        sLogger.debug("getEvolizerSession() has been invoked for project '" + project.getName() + "'");

        try {
            String host = project.getPersistentProperty(EvolizerPreferences.DB_HOST);
            String database = project.getPersistentProperty(EvolizerPreferences.DB_NAME);

            String user = project.getPersistentProperty(EvolizerPreferences.DB_USER);
            String password = project.getPersistentProperty(EvolizerPreferences.DB_PASSWORD);

            return EvolizerSessionFactory.getEvolizerSession(
                    host + "/" + database,
                    "org.hibernate.dialect.MySQLDialect",
                    "com.mysql.jdbc.Driver",
                    user,
                    password);

        } catch (CoreException e) {
            sLogger.error("Error while fetching persistent properties from project '" + project.getName() + "'."
                    + e.getMessage(), e);
            throw new EvolizerException(e);
        }
    }

    /**
     * Returns an implementation of {@link IEvolizerSession} configured with the passed params.
     * 
     * @param dbUrl
     *            database host (e.g. <code>mysql://localhost:3306/evolizer_test</code>)
     * @param dbDialect
     *            database dialect (e.g. o<code>rg.hibernate.dialect.MySQLDialect</code>)
     * @param dbDriverName
     *            jdbc-compliant database driver (e.g. <code>com.mysql.jdbc.Driver</code>)
     * @param dbUser
     *            database username
     * @param dbPasswd
     *            database password for dbUser
     * @return an implementation of {@link EvolizerSessionFactory}
     */
    public static IEvolizerSession getEvolizerSession(
            String dbUrl,
            String dbDialect,
            String dbDriverName,
            String dbUser,
            String dbPasswd) {
        sLogger.debug("getEvolizerSession() has been invoked with params '" + dbUrl + "', '" + dbDialect + "', '"
                + dbDriverName + "', '" + dbUser + "', '" + dbPasswd + "'");

        return new EvolizerSessionImpl(dbUrl, dbDialect, dbDriverName, dbUser, dbPasswd);
    }
}
