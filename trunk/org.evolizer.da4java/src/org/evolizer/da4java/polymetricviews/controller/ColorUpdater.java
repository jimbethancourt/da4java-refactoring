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


import java.awt.Color;

import org.evolizer.da4java.graph.panel.rendering.noderenderer.IFamixNodeRealizer;
import org.evolizer.da4java.polymetricviews.model.ColorNormalizer;

import y.view.NodeRealizer;
import y.view.hierarchy.GroupNodeRealizer;

/**
 * Update the color of a node realizer.
 * 
 * @author mark
 */
public class ColorUpdater extends AbstractPolymetricViewUpdater {

    /**
     * The Constructor.
     * 
     * @param attributeToRepresent the attribute to represent
     */
    public ColorUpdater(String attributeToRepresent) {
        super(attributeToRepresent, new ColorNormalizer());
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void setDefaultRealizer(NodeRealizer realizer) {
        ((IFamixNodeRealizer) realizer).setDefaultColor();
    }

    @Override
    public void updateNodeRealizer(NodeRealizer realizer, Float value) {
        if (realizer instanceof GroupNodeRealizer) {
            if (((GroupNodeRealizer) realizer).isGroupClosed()) {
                realizer.setFillColor(new Color(1f - value, 1f - value, 1f - value));
            }
        } else {
            realizer.setFillColor(new Color(1f - value, 1f - value, 1f - value));
        }
    }
}
