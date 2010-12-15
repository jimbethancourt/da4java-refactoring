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
package org.evolizer.core.util.collections;

/**
 * A predicate evaluates to <code>true</code> or <code>false</code> for instances of the parameterized type.
 * 
 * @author wuersch
 * @param <T>
 *            the type
 * @see FilteringIterator
 */
public interface IPredicate<T> {

    /**
     * Evaluate.
     * 
     * @param object
     *            the object to evaluate
     * @return <code>true</code> if element is to be returned by {@link FilteringIterator}; <code>false</code>
     *         otherwise.
     */
    public boolean evaluate(T object);
}
