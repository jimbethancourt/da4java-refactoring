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
package org.evolizer.da4java.visibility.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.apache.log4j.Logger;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.da4java.commands.AbstractGraphEditCommand;
import org.evolizer.da4java.graph.panel.DA4JavaGraphPanel;
import org.evolizer.da4java.visibility.ViewConfigModel;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;

import y.base.Edge;
import y.base.GraphEvent;
import y.base.GraphListener;
import y.base.Node;
import y.base.NodeCursor;
import y.view.EdgeRealizer;
import y.view.NodeRealizer;

/**
 * The GraphElementsVisibilityUpdater controls the visibility of the node and edge realizers. Visibility is obtained
 * from the ViewConfigModel (stored in the graph panel object). Note, that invisibility means that the node is still
 * in the graph and also considered in the layout.
 * 
 * @author Martin Pinzger, mark
 */
public class GraphElementsVisibilityUpdater implements PropertyChangeListener, GraphListener {
    
    /** The logger. */
    private Logger fLogger = DA4JavaPlugin.getLogManager().getLogger(GraphElementsVisibilityUpdater.class.getName());

    /** The panel. */
    private DA4JavaGraphPanel fPanel;

    /**
     * Instantiates a new graph elements visibility updater.
     * 
     * @param panel the panel
     */
    public GraphElementsVisibilityUpdater(DA4JavaGraphPanel panel) {
        fPanel = panel;
    }

    /**
     * React on change events on the ViewConfigModel. The graph needs to be 
     * repainted, however, no re-layout is needed.
     * 
     * @param event the event
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(ViewConfigModel.ASSOCIATION_VISIBILITY_CHANGE) 
                || event.getPropertyName().equals(ViewConfigModel.ENTITY_VISIBILITY_CHANGE)) {

            updateEntityTypeVisibility();
            updateAssociationTypeVisibility();

            fPanel.getGraph().updateViews();
//            refreshLayout(false, null, null);
        }
    }


    /**
     * React on events signaling structural changes in the graph coming from graph edit commands.
     * 
     * @param graphEvent the graph event
     * 
     * @see y.base.GraphListener#onGraphEvent(y.base.GraphEvent)
     */
    public void onGraphEvent(GraphEvent graphEvent) {
        if (graphEvent.getType() == GraphEvent.POST_EVENT) {
            if (graphEvent.getData() != null 
                    && graphEvent.getData() instanceof AbstractGraphEditCommand) {

                updateEntityTypeVisibility();
                updateAssociationTypeVisibility();
            }
        }
    }

    /**
     * Update entity type visibility.
     */
    private void updateEntityTypeVisibility() {
        Node anchor = fPanel.getHierarchyManager().getAnchorNode(fPanel.getGraph());
        updateEntityTypeVisibility(fPanel.getHierarchyManager().getChildren(anchor), false);
    }

    /**
     * Starting from the root nodes check whether the node type is visible or not. If a parent not is visible or
     * set to visible then continue checking the visibility of child nodes. If a parent node is set to invisible
     * then also set all its descendant nodes to invisible by setting the hide parameter to false.
     * 
     * Note that the visibility of edges have to be configured separately.
     * 
     * @param parentNodes Parent nodes.
     * @param hide Enforce hiding of child nodes.
     */
    private void updateEntityTypeVisibility(NodeCursor parentNodes, boolean hide) {
        for (NodeCursor nc = parentNodes; nc.ok(); nc.next()) {
            Node parentNode = nc.node();
            AbstractFamixEntity entity = fPanel.getGraph().getGraphModelMapper().getFamixEntity(parentNode);
            if (!hide && fPanel.getViewConfigModel().getEntityTypeVisibility().get(entity.getClass()).booleanValue()) {
                NodeRealizer parentRealizer = fPanel.getGraph().getRealizer(parentNode);
                parentRealizer.setVisible(true);
                updateEntityTypeVisibility(fPanel.getHierarchyManager().getChildren(parentNode), false);
            } else {
                // hide all descendant nodes and edges
                NodeRealizer parentRealizer = fPanel.getGraph().getRealizer(parentNode);
                if (parentRealizer.isVisible()) {
                    parentRealizer.setVisible(false);
                    updateEntityTypeVisibility(fPanel.getHierarchyManager().getChildren(parentNode), true);
                } 
            }
        }
    }

    /**
     * Check association visibility. For visible associations also check whether the source and target node are visible.
     * If one is not visible the edge is set to invisible. For aggregated edges (higher level edges) the visibility
     * of the first lower edge is checked. Note, that for checking the visibility of nodes, the correct (not the parent) source and
     * target nodes have to be obtained.
     */
    private void updateAssociationTypeVisibility() {
        for (Edge edge : fPanel.getGraph().getEdgeArray()) {
            Class<? extends FamixAssociation> associationType = fPanel.getGraph().getGraphModelMapper().getEdgeType(edge);
            EdgeRealizer edgeRealizer = fPanel.getGraph().getRealizer(edge);
            if (fPanel.getViewConfigModel().getAssociationTypeVisibility().get(associationType).booleanValue()) {
                // check whether from and to node are also visible
                // if edge is an aggregated edge then check if the source and target node of the first lower level edge are visible
                List<Edge> lowLevelEdges = fPanel.getGraph().getGraphModelMapper().getLowLevelEdges(edge);
                FamixAssociation association;
                if (lowLevelEdges != null && lowLevelEdges.size() > 0) {
                    association = fPanel.getGraph().getGraphModelMapper().getAssociation(lowLevelEdges.get(0));
                } else {
                    association = fPanel.getGraph().getGraphModelMapper().getAssociation(edge);
                }
                NodeRealizer fromRealizer = fPanel.getGraph().getRealizer(fPanel.getGraph().getGraphModelMapper().getNode(association.getFrom()));
                NodeRealizer toRealizer = fPanel.getGraph().getRealizer(fPanel.getGraph().getGraphModelMapper().getNode(association.getTo()));
                if (fromRealizer != null && toRealizer != null) {
                    if (fromRealizer.isVisible() && toRealizer.isVisible()) {
                        edgeRealizer.setVisible(true);
                    } else {
                        edgeRealizer.setVisible(false);
                    }
                } else {
                    fLogger.error("From or to node of edge not in the graph - error in graph structure");
                }
            } else {
                edgeRealizer.setVisible(false);
            }
        }
    }
}
