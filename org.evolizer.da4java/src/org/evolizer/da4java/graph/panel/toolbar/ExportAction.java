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

import javax.swing.AbstractAction;

import org.evolizer.da4java.graph.panel.DA4JavaGraphPanel;
import org.freehep.util.export.ExportDialog;

/**
 * Graph export action. The export is not properly working, yet.
 * 
 * @author pinzger
 */
public class ExportAction extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6629723295483529385L;
    
    /** The hierarchic graph panel. */
    private final DA4JavaGraphPanel fGraphPanel;

    /**
     * The constructor.
     * 
     * @param graphPanel the graph panel
     */
    public ExportAction(DA4JavaGraphPanel graphPanel) {
        super("ExportAction");
        fGraphPanel = graphPanel;
    }

    /** 
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e) {
        ExportDialog export = new ExportDialog();
        export.showExportDialog(fGraphPanel.getView(), "ExportAction view as ...", fGraphPanel.getView(), "export");
    }
}
