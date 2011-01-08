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

import y.view.Arrow;
import y.view.EdgeRealizer;

/**
 * Solid delta edge realizer.
 * 
 * @author pinzger
 */
public class DeltaEdgeRealizer extends DefaultFamixEdgeRealizer {

    /**
     * Instantiates a new delta edge realizer.
     * 
     * @param realizer the edge realizer
     */
    public DeltaEdgeRealizer(final EdgeRealizer realizer) {
        super(realizer);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initAttributes(final Class<? extends FamixAssociation> edgeType, final int nrLowLevelEdges) {
        setArrow(Arrow.WHITE_DELTA);
        setLineColor(edgeType);
        setLineType(nrLowLevelEdges);
    }
}
