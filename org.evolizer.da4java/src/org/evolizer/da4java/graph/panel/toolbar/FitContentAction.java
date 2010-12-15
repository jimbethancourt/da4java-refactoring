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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.evolizer.da4java.graph.panel.DA4JavaGraphPanel;

/**
 * Tits the content inside the view.
 * 
 * @author pinzger
 */
public class FitContentAction extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -4150814027316935370L;

    /** The hierarchic graph panel. */
    private final DA4JavaGraphPanel fGraphPanel;

    /**
     * The constructor.
     * 
     * @param graphPanel the graph panel
     */
    public FitContentAction(DA4JavaGraphPanel graphPanel) {
        super("Fit Content");
        fGraphPanel = graphPanel;
        URL imageURL = ClassLoader.getSystemResource("icons/FitContent16.gif");
        if (imageURL != null) {
            this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
        }
        this.putValue(Action.SHORT_DESCRIPTION, "Fit Content");
    }

    /** 
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e) {
        fGraphPanel.getView().fitContent();
        fGraphPanel.getView().updateView();
    }
}
