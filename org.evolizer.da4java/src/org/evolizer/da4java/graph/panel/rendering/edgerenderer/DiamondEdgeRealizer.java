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
import y.view.LineType;

/**
 * Solid diamond edge realizer.
 * 
 * @author pinzger
 */
public class DiamondEdgeRealizer extends DefaultFamixEdgeRealizer {

    /**
     * The constructor.
     * 
     * @param realizer the edge realizer
     */
    public DiamondEdgeRealizer(EdgeRealizer realizer) {
        super(realizer);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initAttributes(Class<? extends FamixAssociation> edgeType, int nrLowlevelEdges) {
        setArrow(Arrow.DIAMOND);
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
                setLineType(LineType.LINE_1);
                break;
            case 2:
                setLineType(LineType.LINE_2);
                break;
            case 3:
                setLineType(LineType.LINE_3);
                break;
            case 4:
                setLineType(LineType.LINE_4);
                break;
            default:
                setLineType(LineType.LINE_4);
                break;
        }
    }

}
