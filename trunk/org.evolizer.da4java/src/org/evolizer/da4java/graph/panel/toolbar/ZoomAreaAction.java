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

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.evolizer.da4java.graph.panel.DA4JavaGraphPanel;

import y.view.AreaZoomMode;
import y.view.EditMode;
import y.view.ViewMode;

/**
 * Zooms out a particular area of the graph.
 * 
 * @author pinzger
 */
public class ZoomAreaAction extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6118478147905176205L;
    
    /** The graph panel. */
    private final DA4JavaGraphPanel fGraphPanel;

    /**
     * The constructor
     * 
     * @param graphPanel the graph panel
     */
    public ZoomAreaAction(DA4JavaGraphPanel graphPanel) {
        super("ZoomAction Area");
        fGraphPanel = graphPanel;
        URL imageURL = ClassLoader.getSystemResource("icons/Zoom16.gif");
        if (imageURL != null) {
            this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
        }
        this.putValue(Action.SHORT_DESCRIPTION, "ZoomAction Area");
    }

    /** 
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        // fMagnifierButton.setSelected(false);
        Iterator viewModes = fGraphPanel.getView().getViewModes();
        while (viewModes.hasNext()) {
            ViewMode viewMode = (ViewMode) viewModes.next();
            if (viewMode instanceof EditMode) {
                EditMode editMode = (EditMode) viewMode;
                editMode.setChild(new AreaZoomMode(), null, null);
            }
        }
    }
}
