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
package org.evolizer.da4java.graph.panel.rendering.noderenderer;

import java.awt.Color;

import org.evolizer.da4java.graph.utils.Util;
import org.evolizer.famix.model.entities.AbstractFamixEntity;

import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.YLabel;

/**
 * Default node realizer for FAMIX entities.
 * 
 * @author pinzger
 */
public class DefaultFamixNodeRealizer extends ShapeNodeRealizer implements IFamixNodeRealizer {
    /** The default height. */
    private double fDefaultHeight;
    
    /** The default width. */
    private double fDefaultWidth;
    
    /** The default color. */
    private Color fDefaultColor;

    /**
     * The constructor.
     * 
     * @param realizer the node realizer
     */
    public DefaultFamixNodeRealizer(NodeRealizer realizer) {
        super(realizer);
        setShapeType(ShapeNodeRealizer.ROUND_RECT);
        setLineColor(new Color(192, 192, 192));

        fDefaultHeight = realizer.getHeight();
        fDefaultWidth = realizer.getWidth();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public NodeRealizer createCopy(NodeRealizer realizer) {
        return new DefaultFamixNodeRealizer(realizer);
    }

    /** 
     * {@inheritDoc}
     */
    public void initAttributes(AbstractFamixEntity famixEntity) {
        setLabelText(Util.getShortName(famixEntity));
        setFillColor(famixEntity);
    }

    /**
     * Sets the fill color depending on the FAMIX entity type.
     * 
     * @param famixEntity The FAMIX entity
     */
    private void setFillColor(AbstractFamixEntity famixEntity) {
        fDefaultColor = NodeColorManager.getColor(famixEntity);
        setFillColor(NodeColorManager.getColor(famixEntity));
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void setLabelText(String labelText) {
        super.setLabelText(labelText);
        String newLabelText = labelText;

        // NodeLabel label = getLabel();
        getLabel().setFont(DEFAULT_LABEL_FONT);
        getLabel().setText(newLabelText);

        // If we keep the default the label width doesn't get recomputed *shrug*
        byte defaultAutoSizePolicy = getLabel().getAutoSizePolicy(); 
        getLabel().setAutoSizePolicy(YLabel.AUTOSIZE_CONTENT); 

        if (labelText.length() > IFamixNodeRealizer.LABEL_LENGTH) {
            newLabelText = labelText.substring(0, IFamixNodeRealizer.LABEL_LENGTH - 1) + ".";
        }
        getLabel().setText(newLabelText);

        getLabel().setAutoSizePolicy(defaultAutoSizePolicy);
    }

    /** 
     * {@inheritDoc}
     */
    public void setDefaultHeight() {
        setHeight(fDefaultHeight);
    }

    /** 
     * {@inheritDoc}
     */
    public void setDefaultWidth() {
        setWidth(fDefaultWidth);
    }

    /** 
     * {@inheritDoc}
     */
    public void setDefaultColor() {
        setFillColor(fDefaultColor);
    }
}
