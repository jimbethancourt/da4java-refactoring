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
import y.view.hierarchy.GroupNodeRealizer;

/**
 * The realizer for folder nodes (FAMIX entities with childrens).
 * 
 * @author pinzger
 */
public class FamixGroupNodeRealizer extends GroupNodeRealizer implements IFamixNodeRealizer {
    
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
    public FamixGroupNodeRealizer(NodeRealizer realizer) {
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
        return new FamixGroupNodeRealizer(realizer);
    }

    /** 
     * {@inheritDoc}
     */
    public void initAttributes(AbstractFamixEntity famixEntity) {
        setLabelText(Util.getShortName(famixEntity));
        setFillColor(famixEntity);
    }

    /**
     * Sets the fill color dependent on the FAMIX entity type.
     * 
     * @param famixEntity the FAMIX entity
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

        getLabel().setFont(DEFAULT_LABEL_FONT);
        getLabel().setText(labelText);

        // If we keep the default the label width doesn't get recomputed *shrug*
        byte defaultAutoSizePolicy = getLabel().getAutoSizePolicy(); 
        getLabel().setAutoSizePolicy(YLabel.AUTOSIZE_CONTENT); 

        resizeLabel(labelText);
        getLabel().setAutoSizePolicy(defaultAutoSizePolicy);
    }

    /**
     * Resize label. Remove tailing characters until the label length is shorter than the node width.
     * We check for > 2 since this is the minimal size a label can have (e.g. ISavable could become I.)
     * 
     * @param label the label
     */
    public void resizeLabel(String label) {
        String newLabel = label;
        double nodeWidth = getWidth() - 20; // 20 = Estimated width of the plus / minus icon + a small gap
        while (getLabel().getWidth() > nodeWidth && getLabel().getText().length() > 2) {
            if (newLabel.endsWith(".")) {
                newLabel = newLabel.substring(0, newLabel.length() - 2); // E.g. ISavab. -> ISava
            } else {
                newLabel = newLabel.substring(0, newLabel.length() - 1); // E.g. ISavable -> ISavabl
            }
            getLabel().setText(newLabel + ".");
        }
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
