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
 * Normalize the a given value according to the maximum.
 * 
 * @author mark
 */
public class MaxNormalizer implements INormalizer {
    
    /** The MAX_NODE_WIDTH. */
    public static final Float MAX_NODE_WIDTH = 200f;
    
    /** The MIN_NODE_WIDTH. */
    public static final Float MIN_NODE_WIDTH = 30f;

    /** 
     * {@inheritDoc}
     */
    public Float normalize(Float value, Float maxValue) {
        Float normalizedValue = 0f;
        if (value >= 0f && maxValue > 0f) {
            normalizedValue = value * MAX_NODE_WIDTH / maxValue;
        }

        if (normalizedValue < MIN_NODE_WIDTH) {
            normalizedValue = MIN_NODE_WIDTH;
        } else if (normalizedValue > MAX_NODE_WIDTH) {
            normalizedValue = MAX_NODE_WIDTH;
        }

        return normalizedValue;
    }
}
