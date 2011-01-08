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

import y.view.EdgeRealizer;
import y.view.LineType;

/**
 * The Class DottedDeltaEdgeRealizer.
 * 
 * @author pinzger
 */
public class DottedDeltaEdgeRealizer extends DefaultFamixEdgeRealizer {

    /**
     * The constructor.
     * 
     * @param realizer the edge realizer
     */
    public DottedDeltaEdgeRealizer(EdgeRealizer realizer) {
        super(realizer);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initAttributes(Class<? extends FamixAssociation> edgeType, int nrLowlevelEdges) {
        setLineColor(edgeType);
        setLineType(nrLowlevelEdges);
    }

    /** 
     * {@inheritDoc}
     */
    public void setLineType(int nrLowLevelEdges) {
        int width = (int) (DEFAULT_EDGE_WIDTH + nrLowLevelEdges * EDGE_METRIC_FACTOR);
        if (width > MAXIMUM_EDGE_WIDTH) {
            width = (int) MAXIMUM_EDGE_WIDTH;
        }

        switch (width) {
            case 1:
                setLineType(LineType.DOTTED_1);
                break;
            case 2:
                setLineType(LineType.DOTTED_2);
                break;
            case 3:
                setLineType(LineType.DOTTED_3);
                break;
            case 4:
                setLineType(LineType.DOTTED_4);
                break;
            default:
                setLineType(LineType.DOTTED_4);
                break;
        }
    }

}
