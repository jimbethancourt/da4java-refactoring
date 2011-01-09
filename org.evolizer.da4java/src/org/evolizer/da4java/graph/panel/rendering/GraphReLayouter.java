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
package org.evolizer.da4java.graph.panel.rendering;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.da4java.commands.AbstractGraphEditCommand;
import org.evolizer.da4java.commands.selection.AbstractSelectionStrategy;
import org.evolizer.da4java.graph.data.DependencyGraphSingleton;
import org.evolizer.da4java.graph.data.GraphManager;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.GraphEvent;
import y.base.GraphListener;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.module.LayoutModule;
import y.module.ModuleEvent;
import y.module.ModuleListener;
import y.view.Graph2DView;

/**
 * Listener for structural changes in the graph. Whenever nodes or edges are
 * added to or removed from the graph the graph is relayout.
 * 
 * Currently, the logic for relayout is implemented with PRE and POST graph events.
 * In the final POST event it is checked, whether nodes/edges have been added/removed.
 * If yes, the graph is relayout.
 * 
 * @author pinzger
 */
public class GraphReLayouter implements GraphListener, PropertyChangeListener {
    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(GraphReLayouter.class.getName());

    /** The graph panel. */
    private Graph2DView fGraphView;

    /** The added nodes. */
    private List<Node> fAddedNodes = new ArrayList<Node>();
    
    /** The added edges. */
    private List<Edge> fAddedEdges = new ArrayList<Edge>();
    
    /** The removed nodes. */
    private List<Node> fRemovedNodes = new ArrayList<Node>();
    
    /** The removed edges. */
    private List<Edge> fRemovedEdges = new ArrayList<Edge>();
    
    /** The subgraph changed. */
    private boolean fSubgraphChanged;

    /** The event level. */
    private int fEventLevel;

    /**
     * The constructor.
     * 
     * @param graphView   The graph to layout
     */
    public GraphReLayouter(Graph2DView graphView) {
        fGraphView = graphView;
        fSubgraphChanged = false;
        fEventLevel = 0;
    }

    /** 
     * {@inheritDoc}
     */
    public void onGraphEvent(GraphEvent graphEvent) {
        if (graphEvent.getType() == GraphEvent.PRE_EVENT) {
            if (fEventLevel <= 0) {
                fEventLevel = 0;
                fAddedNodes = new ArrayList<Node>();
                fAddedEdges = new ArrayList<Edge>();
                fRemovedNodes = new ArrayList<Node>();
                fRemovedEdges = new ArrayList<Edge>();
                fSubgraphChanged = false;
            }
            fEventLevel++;
        } else if (graphEvent.getType() == GraphEvent.POST_EVENT) {
            fEventLevel--;
            if (isGraphChanged()) {
                sLogger.info("Graph will be re-layout");
                boolean doLayout = true;

                AbstractSelectionStrategy preLayout = null;
                AbstractSelectionStrategy postLayout = null;
                if (graphEvent.getData() != null 
                        && graphEvent.getData() instanceof AbstractGraphEditCommand) {
                    AbstractGraphEditCommand command = (AbstractGraphEditCommand) graphEvent.getData();
                    preLayout = command.getPreLayoutSelectionStrategy();
                    postLayout = command.getPostLayoutSelectionStrategy();
                } 
                refreshLayout(doLayout, preLayout, postLayout);
            }
        } else if (graphEvent.getType() == GraphEvent.NODE_CREATION) {
            Node node = (Node) graphEvent.getData();
            fAddedNodes.add(node);
        } else if (graphEvent.getType() == GraphEvent.POST_NODE_REMOVAL) {
            Node node = (Node) graphEvent.getData();
            fRemovedNodes.add(node);
        } else if (graphEvent.getType() == GraphEvent.SUBGRAPH_INSERTION) {
            NodeList nl = (NodeList) graphEvent.getData();
            for (NodeCursor nc = nl.nodes(); nc.ok(); nc.next()) {
                fAddedNodes.add(nc.node());
            }
        } else if (graphEvent.getType() == GraphEvent.EDGE_CREATION) {
            Edge edge = (Edge) graphEvent.getData();
            fAddedEdges.add(edge);
        } else if (graphEvent.getType() == GraphEvent.POST_EDGE_REMOVAL) {
            Edge edge = (Edge) graphEvent.getData();
            fRemovedEdges.add(edge);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void propertyChange(final PropertyChangeEvent event) {
//        sLogger.info("Received property change event " + event.getPropertyName());
        if (event.getPropertyName().equals(DependencyGraphSingleton.LAYOUT_MODULE_CHANGED)) {
            refreshLayout(true, null, null);
        } else if (event.getPropertyName().equals(DependencyGraphSingleton.NODE_SIZE_CHANGED)) {
            refreshLayout(true, null, null);
        }
    }

    /**
     * Update the visual appearance of the graph.
     * 
     * @param doLayout true if graph needs re-layout
     * @param preLayout Strategy for incremental graph layout (e.g., layout only selected entities)
     * @param postLayout Strategy for selecting elements after the graph has been laid out.
     */
    private void refreshLayout(final boolean doLayout, 
            final AbstractSelectionStrategy preLayout, 
            final AbstractSelectionStrategy postLayout) {

        //        try {
        // fToolbar.updateUndoRedoButtons(fCommandController.canUndo(),fCommandController.canRedo());
        // getGraphElementsVisibilityController().updateGraphElementsVisibility();
        final GraphManager graph = (GraphManager) fGraphView.getGraph2D();
        if (doLayout) {
            final LayoutModule layoutModule = graph.getLayoutModule();

            layoutModule.addModuleListener(new ModuleListener() {
                public void moduleEventHappened(ModuleEvent event) {
                    if (event.getEventType() == ModuleEvent.TYPE_MODULE_MAIN_RUN_FINISHED) {
                        if (postLayout != null) {
                            postLayout.updateSelection();
                        }
//                        fGraphView.fitContent();
                        fGraphView.updateView();
//                        graph.updateViews();
                    } else if (event.getEventType() == ModuleEvent.TYPE_MODULE_DISPOSED) {
                        layoutModule.removeModuleListener(this);
                    }
                }
            });

            if (preLayout != null) {
                preLayout.updateSelection();
            }
            sLogger.info("Do graph layout " + getSelectionString());
            layoutModule.start(graph);
        } else {
//            fGraphView.fitContent();
            fGraphView.updateView();
//            graph.updateViews();
        }
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //            sLogger.error("Error in layouting the graph");
        //        }
    }

    /**
     * Gets the selection string.
     * 
     * @return the selection string
     */
    private String getSelectionString() {
        StringBuilder sb = new StringBuilder(" Selected nodes: ");
        for (NodeCursor nc = fGraphView.getGraph2D().selectedNodes(); nc.ok(); nc.next()) {
            sb.append(" ");
            sb.append(nc.node());
        }
        sb.append("\n");

        sb.append("Selected edges: ");
        for (EdgeCursor ec = fGraphView.getGraph2D().selectedEdges(); ec.ok(); ec.next()) {
            sb.append(" ");
            sb.append(ec.edge());
        }

        return sb.toString();
    }
    
    /**
     * Checks if is graph changed.
     * 
     * @return true, if is graph changed
     */
    private boolean isGraphChanged() {
        boolean isChanged = false;
        if (fEventLevel == 0) { 
            if (!fAddedNodes.isEmpty() || !fRemovedNodes.isEmpty()) {
                isChanged = true;
            } else if (!fAddedEdges.isEmpty() || !fRemovedEdges.isEmpty()) {
                // } else if (!fAddedEdges.isEmpty()) {
                isChanged = true;
            } else if (fSubgraphChanged) {
                isChanged = true;
            }
        }

        return isChanged;
    }
}
