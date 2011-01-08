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
package org.evolizer.da4java.polymetricviews.controller;

import org.evolizer.da4java.polymetricviews.model.INormalizer;
import org.evolizer.da4java.polymetricviews.model.MaxNormalizer;

import y.view.NodeRealizer;

/**
 * Abstract base class for the polymetric view updater. Basically, each
 * graphical attribute comes with its updater. They are implemented in
 * the referenced sub-classes.
 * 
 * The class also specifies the normalizer to use for normalizing
 * the metric value. Default the {@link MaxNormalizer} is used.
 * 
 * @author mark, pinzger
 */
public abstract class AbstractPolymetricViewUpdater {

    /** The metric that is represented by the graphical attribute of the corresponding Updater. */
    private String fMetricToRepresent;

    /** The normalizer that is used to calculate the value which is applied to a node realizer. */
    private INormalizer fNormalizer;

    /**
     * The Constructor.
     * 
     * @param metricToRepresent the metric to represent
     */
    public AbstractPolymetricViewUpdater(String metricToRepresent) {
        fMetricToRepresent = metricToRepresent;
        fNormalizer = new MaxNormalizer();
    }

    /**
     * Alternative constructor to also set the normalizer
     * 
     * @param metricToRepresent the metric to represent
     * @param normalizer the normalizer
     */
    public AbstractPolymetricViewUpdater(String metricToRepresent, INormalizer normalizer) {
        fMetricToRepresent = metricToRepresent;
        fNormalizer = normalizer;
    }
    
    /**
     * This method applies a given value to the given NodeRealizer.
     * 
     * @param realizer to which the value is applied
     * @param value the value which will be applied to the realizer
     */
    public abstract void updateNodeRealizer(NodeRealizer realizer, Float value);

    /**
     * This method applies the default value to the given NodeRealizer.
     * 
     * @param realizer the realizer to set the default value
     */
    public abstract void setDefaultRealizer(NodeRealizer realizer);

    /**
     * Returns the attribute that is represented by the Updater.
     * 
     * @return the metric to represent
     */
    public String getMetricToRepresent() {
        return fMetricToRepresent;
    }

    /**
     * Returns the normalizer that belongs the updater.
     * 
     * @return the value normalizer
     */
    public INormalizer getNormalizer() {
        return fNormalizer;
    }
}
