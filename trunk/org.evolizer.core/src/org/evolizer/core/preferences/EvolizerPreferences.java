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
package org.evolizer.core.preferences;

import org.eclipse.core.runtime.QualifiedName;

/**
 * This class holds some constants that are used to reference preference settings.
 * 
 * @author wuersch
 */
public final class EvolizerPreferences {

    private EvolizerPreferences() {}

    /**
     * Preference for the database server host name.
     */
    public static final QualifiedName DB_HOST = new QualifiedName("org.evolizer", "db.host");

    /**
     * Preference for the database name.
     */
    public static final QualifiedName DB_NAME = new QualifiedName("org.evolizer", "db.name");

    /**
     * Preference for the database user name.
     */
    public static final QualifiedName DB_USER = new QualifiedName("org.evolizer", "db.user");

    /**
     * Preference for the database user password.
     */
    public static final QualifiedName DB_PASSWORD = new QualifiedName("org.evolizer", "db.password");
    
    /** 
     * Preference for the using the an in-memory database (currently H2). 
     */
    public static final QualifiedName DB_USE_INMEMORY = new QualifiedName("org.da4java", "db.inmemory");

}
