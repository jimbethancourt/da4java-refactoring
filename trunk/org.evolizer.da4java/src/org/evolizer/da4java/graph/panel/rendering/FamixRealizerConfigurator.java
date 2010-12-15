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
import java.util.List;

import org.apache.log4j.Logger;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.da4java.graph.data.DependencyGraph;
import org.evolizer.da4java.graph.panel.rendering.edgerenderer.DashedDeltaEdgeRealizer;
import org.evolizer.da4java.graph.panel.rendering.edgerenderer.DefaultFamixEdgeRealizer;
import org.evolizer.da4java.graph.panel.rendering.edgerenderer.DeltaEdgeRealizer;
import org.evolizer.da4java.graph.panel.rendering.edgerenderer.IFamixEdgeRealizer;
import org.evolizer.da4java.graph.panel.rendering.noderenderer.DefaultFamixNodeRealizer;
import org.evolizer.da4java.graph.panel.rendering.noderenderer.FamixAttributeNodeRealizer;
import org.evolizer.da4java.graph.panel.rendering.noderenderer.FamixGroupNodeRealizer;
import org.evolizer.da4java.graph.panel.rendering.noderenderer.IFamixNodeRealizer;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAccess;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixAttribute;
import org.evolizer.famix.model.entities.FamixCastTo;
import org.evolizer.famix.model.entities.FamixCheckInstanceOf;
import org.evolizer.famix.model.entities.FamixInheritance;
import org.evolizer.famix.model.entities.FamixInvocation;
import org.evolizer.famix.model.entities.FamixSubtyping;

import y.base.Edge;
import y.base.Node;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.hierarchy.GroupNodeRealizer;

/**
 * Listener for configuring the rendering/realizers of added nodes of FAMIX entities.
 * 
 * @author Martin Pinzger
 */
public class FamixRealizerConfigurator implements PropertyChangeListener {
    
    /** The s logger. */
    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(FamixRealizerConfigurator.class.getName());

    /**
     * The default constructor. 
     */
    public FamixRealizerConfigurator() {
    }

    /**
     * Listen for node and edge creation events to configure realizers accordingly.
     * 
     * @param event The map change event
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(DependencyGraph.ENTITY_ADDED)) {
            Node node = (Node) event.getNewValue();
            configureNode(node);
            // } else if (graphEvent.getType() == DA4JavaGraphEvent.EDGE_CREATION_ASSOCIATION_SET) {
            // Edge edge = (Edge) graphEvent.getData();
            // configureEdge(edge);
        } else if (event.getPropertyName().equals(DependencyGraph.HIGHLEVEL_EDGE_ADDED)) {
            Edge edge = (Edge) event.getNewValue();
            configureHighLevelEdge(edge);
        } 
    }

    /**
     * Configures the node renderer/realizer. The node and its corresponding entity are kept in the node maps. The entity's name 
     * is used as the label text and abbreviated if necessary. Furthermore the node's color is changed according 
     * to the entity's type.
     * 
     * @param node The node to configure.
     */
    private void configureNode(Node node) {
        Graph2D graph = (Graph2D) node.getGraph();
        DependencyGraph rootGraph = (DependencyGraph) graph.getHierarchyManager().getRootGraph();
        AbstractFamixEntity entity = rootGraph.getFamixEntity(node);

        NodeRealizer nr = graph.getRealizer(node);
        NodeRealizer newNodeRealizer = null;
        if (nr instanceof GroupNodeRealizer) {
            newNodeRealizer = new FamixGroupNodeRealizer(nr);
        } else {
            if (entity instanceof FamixAttribute) {
                newNodeRealizer = new FamixAttributeNodeRealizer(nr);
            } else {
                newNodeRealizer = new DefaultFamixNodeRealizer(nr);
            }
        }
        ((IFamixNodeRealizer) newNodeRealizer).initAttributes(entity);
        graph.setRealizer(node, newNodeRealizer);
    }

    /**
     * Configure high level edge.
     * 
     * @param edge the edge
     */
    private void configureHighLevelEdge(Edge edge) {
        Graph2D graph = (Graph2D) edge.getGraph();
        DependencyGraph rootGraph = (DependencyGraph) graph.getHierarchyManager().getRootGraph();

        Class<? extends FamixAssociation> associationType = rootGraph.getEdgeType(edge);
        EdgeRealizer er = graph.getRealizer(edge);
        EdgeRealizer newEdgeRealizer = null;
        if (associationType.equals(FamixInvocation.class)) {
            newEdgeRealizer = new DefaultFamixEdgeRealizer(er);
        } else if (associationType.equals(FamixInheritance.class)) {
            newEdgeRealizer = new DeltaEdgeRealizer(er);
        } else if (associationType.equals(FamixSubtyping.class)) {
            newEdgeRealizer = new DashedDeltaEdgeRealizer(er);
        } else if (associationType.equals(FamixCastTo.class)) {
            newEdgeRealizer = new DefaultFamixEdgeRealizer(er);
        } else if (associationType.equals(FamixCheckInstanceOf.class)) {
            newEdgeRealizer = new DefaultFamixEdgeRealizer(er);
        } else if (associationType.equals(FamixAccess.class)) {
            newEdgeRealizer = new DefaultFamixEdgeRealizer(er);
        } else {
            sLogger.warn("Edgetype of " + associationType + " currently not supported - using default");
            newEdgeRealizer = new DefaultFamixEdgeRealizer(er);
        }

        List<Edge> lowLevelEdges = rootGraph.getLowLevelEdges(edge);
        int nrLowLevelEdges = 1;
        if (lowLevelEdges != null) {
            nrLowLevelEdges = lowLevelEdges.size();
        }

        ((IFamixEdgeRealizer) newEdgeRealizer).initAttributes(associationType, nrLowLevelEdges);
        graph.setRealizer(edge, newEdgeRealizer);
    }
}
