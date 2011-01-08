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
import y.view.GenericEdgeRealizer;
import y.view.LineType;

/**
 * The base (default) class of FAMIX edge realizers.
 * 
 * @author pinzger
 */
public class DefaultFamixEdgeRealizer extends GenericEdgeRealizer implements IFamixEdgeRealizer {
    
    /**
     * The constructor.
     * 
     * @param realizer the edge realizer
     */
    public DefaultFamixEdgeRealizer(EdgeRealizer realizer) {
        super(realizer);

        setArrow(Arrow.STANDARD);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public EdgeRealizer createCopy(EdgeRealizer arg0) {
        return super.createCopy(arg0);
    }

    /** 
     * {@inheritDoc}
     */
    public void initAttributes(Class<? extends FamixAssociation> edgeType, int nrLowLevelEdges) {
        setLineColor(edgeType);
        setLineType(nrLowLevelEdges);
    }

    /**
     * Set the line type.
     * 
     * @param nrLowLevelEdges The number of aggregated low level edges.
     */
    public void setLineType(int nrLowLevelEdges) {
        int width = (int) (DEFAULT_EDGE_WIDTH + nrLowLevelEdges * EDGE_METRIC_FACTOR);
        if (width > MAXIMUM_EDGE_WIDTH) {
            width = (int) MAXIMUM_EDGE_WIDTH;
        }

        setLineType(LineType.createLineType(width, 0, 1, 0, null, 0));
    }

    /**
     * Sets the line color depending on the association type.
     * 
     * @param associationType the association type
     */
    protected void setLineColor(Class<? extends FamixAssociation> associationType) {
        setLineColor(EdgeColorManager.getColor(associationType));
    }
}
