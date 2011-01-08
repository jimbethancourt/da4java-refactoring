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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

import org.evolizer.da4java.commands.filters.FilterSelectedEntities;
import org.evolizer.da4java.graph.panel.DA4JavaGraphPanel;

import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;

/**
 * Filters the selected graph nodes and edges.
 * 
 * @author pinzger
 */
public class FilterSelectedElementsAction extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6417223334448927991L;
    
    /** The graph panel. */
    private DA4JavaGraphPanel fGraphPanel;

    /**
     * The constructor.
     * 
     * @param graphPanel the graph panel
     */
    public FilterSelectedElementsAction(DA4JavaGraphPanel graphPanel) {
        this.fGraphPanel = graphPanel;
    }

    /** 
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        List selectedElements = new ArrayList();
        for (NodeCursor nc = fGraphPanel.getGraph()
                .selectedNodes(); nc.ok(); nc.next()) {
            Node node = nc.node();
            selectedElements.add(node);
        }
        for (EdgeCursor ec = fGraphPanel.getGraph()
                .selectedEdges(); ec.ok(); ec.next()) {
            selectedElements.add(ec.edge());
        }
        FilterSelectedEntities filter = new FilterSelectedEntities(selectedElements, fGraphPanel.getGraphLoader(), fGraphPanel
                .getEdgeGrouper());
        fGraphPanel.getCommandController().executeCommand(filter);
    }

}
