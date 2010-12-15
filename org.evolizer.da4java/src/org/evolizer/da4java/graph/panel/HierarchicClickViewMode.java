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
package org.evolizer.da4java.graph.panel;

import java.awt.event.MouseEvent;

import org.evolizer.da4java.commands.AbstractGraphEditCommand;
import org.evolizer.da4java.commands.FolderNodeHandleCommand;

import y.base.Node;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ProxyShapeNodeRealizer;
import y.view.ViewMode;
import y.view.hierarchy.GroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;

/**
 * View mode that allows to navigate hierarchical graphs. 
 * 
 * @author Katja Graefenhain
 */
public class HierarchicClickViewMode extends ViewMode {
    
    /** The Constant END_OF_EDIT_ACTION. */
    public static final byte END_OF_EDIT_ACTION = 120;

    /** The graph panel. */
    private DA4JavaGraphPanel fGraphPanel;

    /**
     * The constructor.
     * 
     * @param graphPanel The graph panel
     */
    public HierarchicClickViewMode(DA4JavaGraphPanel graphPanel) {
        fGraphPanel = graphPanel;
    }

    /**
     * Return the graph view.
     * 
     * @return The graph view
     */
    private Graph2DView getView() {
        return getGraphPanel().getView();
    }

    /** 
     * {@inheritDoc}
     */
    public void mouseClicked(MouseEvent e) {
        // if (e.getClickCount() == 2) {
        // Node v = getHitInfo(e).getHitNode();
        // if (v != null) {
        // navigateToInnerGraph(v);
        // } else {
        // navigateToParentGraph();
        // }
        // } else {
        Node node = getHitInfo(e).getHitNode();
        if (node != null && !getHierarchyManager().isNormalNode(node)) {
            double x = translateX(e.getX());
            double y = translateY(e.getY());
            Graph2D graph = getView().getGraph2D();
            NodeRealizer r = graph.getRealizer(node);
            GroupNodeRealizer gnr = null;
            if (r instanceof GroupNodeRealizer) {
                gnr = (GroupNodeRealizer) r;
            } else if (r instanceof ProxyShapeNodeRealizer
                    && ((ProxyShapeNodeRealizer) r).getRealizerDelegate() instanceof GroupNodeRealizer) {
                gnr = (GroupNodeRealizer) ((ProxyShapeNodeRealizer) r).getRealizerDelegate();
            }
            if (gnr != null) {
                NodeLabel handle = gnr.getStateLabel();
                if (handle.getBox().contains(x, y)) {
                    AbstractGraphEditCommand command;
                    if (getHierarchyManager().isFolderNode(node)) {
                        command = new FolderNodeHandleCommand(getGraphPanel().getGraphLoader(), getGraphPanel().getEdgeGrouper(), node, true);
                        getGraphPanel().getCommandController().executeCommand(command);
                    } else {
                        command = new FolderNodeHandleCommand(getGraphPanel().getGraphLoader(), getGraphPanel().getEdgeGrouper(), node, false);
                        getGraphPanel().getCommandController().executeCommand(command);
                    }
                }
            }
        }
    }

    /**
     * Return the hierarchy manager.
     * 
     * @return the hierarchyManager
     */
    private HierarchyManager getHierarchyManager() {
        return getGraphPanel().getHierarchyManager();
    }

    /**
     * Return the graph panel.
     * 
     * @return the graphPanel
     */
    private DA4JavaGraphPanel getGraphPanel() {
        return fGraphPanel;
    }
}
