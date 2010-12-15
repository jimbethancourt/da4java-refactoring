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
package org.evolizer.model.resources.entities.misc;

import java.util.Set;

/**
 * Interface that denotes that a certain entity maintains some kind of parent-child relationship. Of course,
 * implementors can have additional behavior to return only certain subsets of its children.
 * 
 * @param <T>
 *            The type of the child entities
 * @author wuersch
 */
public interface IHierarchicalElement<T> {

    /**
     * Returns all the children of the implementor of this interface.
     * 
     * @return a {@link Set} of child entities
     */
    public Set<T> getChildren();
}
