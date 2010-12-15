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
package org.evolizer.model.resources;

import org.evolizer.core.hibernate.model.api.IEvolizerModelEntity;
import org.evolizer.core.hibernate.model.api.IEvolizerModelProvider;
import org.evolizer.model.resources.entities.fs.Directory;
import org.evolizer.model.resources.entities.fs.File;
import org.evolizer.model.resources.entities.humans.Person;
import org.evolizer.model.resources.entities.humans.Role;
import org.evolizer.model.resources.entities.misc.Content;

/**
 * This class collects all the implementors of {@link IEvolizerModelEntity} in this plug-in
 * and is used during start-up to them available to the Hibernate layer 
 * @author wuersch
 */
public class EvolizerResourceModelProvider implements IEvolizerModelProvider {

    /**
     * {@inheritDoc}
     */
    public Class<?>[] getAnnotatedClasses() {
        Class<?>[] annotatedClasses = {Directory.class, File.class, Person.class, Role.class, Content.class};

        return annotatedClasses;
    }
}
