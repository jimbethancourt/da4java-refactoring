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
package org.evolizer.da4java.graph.panel.toolbar;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.evolizer.da4java.graph.panel.DA4JavaGraphPanel;

/**
 * Zooming in or out.
 * 
 * @author pinzger
 */
public class ZoomAction extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8684643985312083075L;
    
    /** The graph panel. */
    private final DA4JavaGraphPanel fGraphPanel;
    
    /** The zoom factor. */
    private double fZoomFactor;

    /**
     * The constructor.
     * 
     * @param graphPanel the graph panel
     * @param zoomFactor the zoom factor
     */
    public ZoomAction(DA4JavaGraphPanel graphPanel, double zoomFactor) {
        super("ZoomAction " + (zoomFactor > 1.0 ? "In" : "Out"));
        fGraphPanel = graphPanel;
        URL imageURL;
        if (zoomFactor > 1.0d) {
            imageURL = ClassLoader.getSystemResource("icons/ZoomIn16.gif");
        } else {
            imageURL = ClassLoader.getSystemResource("icons/ZoomOut16.gif");
        }
        if (imageURL != null) {
            this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
        }
        this.putValue(Action.SHORT_DESCRIPTION, "ZoomAction " + (zoomFactor > 1.0 ? "In" : "Out"));
        this.fZoomFactor = zoomFactor;
    }

    /** 
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e) {
        // fMagnifierButton.setSelected(false);
        fGraphPanel.getView().setZoom(fGraphPanel.getView().getZoom() * fZoomFactor);
        // optional code that adjusts the size of the
        // view's world rectangle. The world rectangle
        // defines the region of the canvas that is
        // accessible by using the scrollbars of the view.
        Rectangle box = fGraphPanel.getView().getGraph2D().getBoundingBox();
        fGraphPanel.getView().setWorldRect(box.x - 20, box.y - 20, box.width + 40, box.height + 40);

        fGraphPanel.getView().updateView();
    }
}
