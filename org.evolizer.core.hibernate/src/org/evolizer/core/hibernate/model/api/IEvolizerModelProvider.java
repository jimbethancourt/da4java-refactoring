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
package org.evolizer.core.hibernate.model.api;

/**
 * Plug-ins that intend to provide model entities have to extend the <code>org.evolizer.hibernate.modelProvider</code>
 * extension point and provide a class that implements this interface.
 * 
 * @author wuersch
 */
public interface IEvolizerModelProvider {

    /**
     * This method returns an array of classes that are annotated with hibernate/ejb3 mapping information.
     * 
     * @return an array containing hibernate/ejb3 annotated classes.
     */
    // TODO: IEvolizerModelEntity?
    public Class<?>[] getAnnotatedClasses();
}
