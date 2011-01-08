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
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.evolizer.da4java.graph.panel.DA4JavaGraphPanel;

import y.module.io.GMLOutput;

/**
 * Save the current graph to the selected file.
 * 
 * TODO: The export of the graph is currently not fully supported.
 * 
 * @author pinzger
 */
public class SaveAsAction extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6913635928992858219L;

    /** The graph panel. */
    private final DA4JavaGraphPanel fGraphPanel;

    /**
     * The constructor.
     * 
     * @param graphPanel the graph panel
     */
    public SaveAsAction(DA4JavaGraphPanel graphPanel) {
        fGraphPanel = graphPanel;
    }

    /** 
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent arg0) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(fGraphPanel) == JFileChooser.APPROVE_OPTION) {
            String fileName = fileChooser.getSelectedFile().getPath();
            GMLOutput outputModule = new GMLOutput();
            try {
                outputModule.setURL(new URL("file://" + fileName));
                outputModule.startAsThread(fGraphPanel.getGraph());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

}
