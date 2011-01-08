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
package org.evolizer.da4java.graph.panel.rendering.edgerenderer;

import org.evolizer.famix.model.entities.FamixAssociation;

/**
 * Interface of FAMIX edge realizers. 
 * 
 * @author pinzger
 */
public interface IFamixEdgeRealizer {

    /** Default edge width */
    float DEFAULT_EDGE_WIDTH = 1.0f;

    /** Max edge width. */
    float MAXIMUM_EDGE_WIDTH = 12.0f;

    /** Metric factor. */
    float EDGE_METRIC_FACTOR = 0.7F;

    /**
     * Initializes the two attributes of edge realizers. The
     * association type determines the color and line type of edges.
     * The number of aggregated low level edges determines the widht
     * of edges.
     * 
     * @param associationType the association type
     * @param nrLowLevelEdges the number of aggregated low level edges
     */
    void initAttributes(Class<? extends FamixAssociation> associationType, int nrLowLevelEdges);
}
