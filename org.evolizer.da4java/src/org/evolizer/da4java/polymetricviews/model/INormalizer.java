/*
 * Copyright 2009 Martin Pinzger, Delft University of Technology,
 * and University of Zurich, Switzerland
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
package org.evolizer.da4java.polymetricviews.model;

/**
 * The INormalizer interface provides methods to normalize Values 
 * according to a given reference value (e.g., maximum).
 * 
 * @author mark
 */
public interface INormalizer {

    /**
     * Calculates a normalized value for methods given the maxValue and the actual value.
     * 
     * @param value the actual value
     * @param refValue the reference value
     * 
     * @return the normalized value
     */
    Float normalize(Float value, Float refValue);
}
