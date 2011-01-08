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
package org.evolizer.da4java.graph.data;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.evolizer.core.exceptions.EvolizerRuntimeException;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.da4java.graph.utils.FamixEntityMap;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.entities.FamixPackage;
import org.evolizer.model.resources.entities.misc.IHierarchicalElement;

import y.base.Edge;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.base.NodeMap;
import y.module.LayoutModule;
import y.view.Graph2D;

/**
 * Dependency graph containing the nodes and edges as well as maps to
 * the corresponding FAMIX entities and associations. The class provides
 * the basic methods to add/remove entities and associations to the
 * graph, as well as, managing the map between higher and lower-level edges.
 * 
 * @author Martin Pinzger
 */
public class DependencyGraph extends Graph2D {
    /** The Constant ENTITY_ADDED. */
    //public static final String ENTITY_ADDED = "entity_added";
    
    /** The Constant ASSOCIATION_ADDED. */
    //public static final String ASSOCIATION_ADDED = "association_added";
    
    /** The Constant HIGHLEVEL_EDGE_ADDED. */
    //public static final String HIGHLEVEL_EDGE_ADDED = "highlevel_edge_added";

    /** The Constant LAYOUT_MODULE_CHANGED. */
    //public static final String LAYOUT_MODULE_CHANGED = "layout_module_changed";
    
    /** The Constant NODE_SIZE_CHANGED. */
    //public static final String NODE_SIZE_CHANGED = "node_size_changed";

    /** The logger. */
    //private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(DependencyGraph.class.getName());

    //CLASS A (mapping)
    /** FAMIX entity to yFiles node map of each entity contained in the graph. */
    //private Map<AbstractFamixEntity, Node> fFamixToNodeMap;

    /** FAMIX association to yFiles edge map of each association contained in the graph. */
    //private Map<FamixAssociation, Edge> fFamixToEdgeMap;
    //end
    
    //CLASS B (graph management)
    /** The layout module. */
    //private LayoutModule fLayoutModule;
    
    /** The property change support. */
    //private PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);
    //end
    
    /**
     * Default constructor of a dependency graph. Initializes the maps between
     * FAMIX entities and yFiles nodes and FAMIX associations and yFiles Edges.
     * A map for linking high level edges to aggregated low level edges is initialized
     * as well.
     */
    //Constr()
    /*public DependencyGraph() {
        fFamixToNodeMap = new HashMap<AbstractFamixEntity, Node>();
        fFamixToEdgeMap = new HashMap<FamixAssociation, Edge>();

        createNodeMap(); // node to FAMIX entity map
        createEdgeMap(); // edge to FAMIX association map
        createEdgeMap(); // high level to low level edges map
    }*/

    //CLASS B
    /**
     * Checks whether the current graph contains the given FAMIX entity.
     * 
     * @param entity The FAMIX entity to check.
     * 
     * @return Ture if the FAMIX entity is already in the graph otherwise false.
     */
    /*public boolean contains(AbstractFamixEntity entity) {
        return getFamixToNodeMap().containsKey(entity);
    }*/

    //CLASS B
    /**
     * Check whether the given association is already contained in the graph.
     * 
     * @param association The FAMIX association to check.
     * 
     * @return True if the association is already in the graph otherwise false.
     */
    /*public boolean contains(FamixAssociation association) {
        return getFamixToEdgeMap().containsKey(association);
    }*/

    //CLASS A
    /**
     * Return the yFiles Node for the given FAMIX entity.
     * 
     * @param entity The FAMIX entity.
     * 
     * @return The node that represents the given FAMIX entity.
     */
    /*public Node getNode(AbstractFamixEntity entity) {
        return getFamixToNodeMap().get(entity);
    }*/

    //CLASS A
    /**
     * Return the yFiles edge for the given FAMIX association.
     * 
     * @param association The FAMIX association.
     * 
     * @return The corresponding yFiles edge.
     */
    /*public Edge getEdge(FamixAssociation association) {
        return getFamixToEdgeMap().get(association);
    }*/

    //CLASS A
    /**
     * Return the FAMIX entity of the given node.
     * 
     * @param node The node.
     * 
     * @return The FAMIX entity for the given node.
     */
    /*public AbstractFamixEntity getFamixEntity(Node node) {
        return (AbstractFamixEntity) getNodeToFamixMap().get(node);
    }*/

    //CLASS A
    /**
     * Return corresponding FAMIX entities of the given list of nodes.
     * 
     * @param nodes The list of nodes.
     * 
     * @return The list of matched FAMIX entities.
     */
    /*public List<AbstractFamixEntity> getFamixEntities(List<Node> nodes) {
        List<AbstractFamixEntity> entities = new ArrayList<AbstractFamixEntity>();
        for (Node node : nodes) {
            AbstractFamixEntity entity = getFamixEntity(node);
            if (entity != null) {
                entities.add(entity);
            } else {
                sLogger.error("Entity of node " + node + " not contained in the graph anymore");
            }
        }
        return entities;
    }*/

    //CLASS A
    /**
     * Return the corresponding FAMIX associations of the given list of edges
     * (low and higher level).
     * 
     * @param edges The list of edges.
     * 
     * @return  The list of associations.
     */
    /*public List<FamixAssociation> getFamixAssociations(List<Edge> edges) {
        List<FamixAssociation> associations = new ArrayList<FamixAssociation>();
        for (Edge edge : edges) {
            List<Edge> lowLevelEdges = getLowLevelEdges(edge);
            if (lowLevelEdges != null && lowLevelEdges.size() > 0) {
                for (Edge lowLevelEdge : lowLevelEdges) {
                    FamixAssociation association = getAssociation(lowLevelEdge);
                    if (association != null) {
                        associations.add(association);
                    } else {
                        sLogger.error("Could not determine FAMIX association of edge " + edge);
                    }
                }
            } else {
                FamixAssociation association = getAssociation(edge);
                if (association != null) {
                    associations.add(association);
                } else {
                    sLogger.error("Could not determine FAMIX association of edge " + edge);
                }
            }
        }
    
        return associations;
    }*/

    //CLASS A
    /**
     * Return the FAMIX association of the given low level edge.
     * 
     * @param lowLevelEdge The lowe level edge.
     * 
     * @return The corresponding FAMIX association.
     */
    /*public FamixAssociation getAssociation(Edge lowLevelEdge) {
        return (FamixAssociation) getEdgeToFamixMap().get(lowLevelEdge);
    }*/

    //CLASS A
    /**
     * Return the list of aggregated low level edges represented by the given
     * high level edge.
     * 
     * @param edge The high level edge.
     * 
     * @return The list of aggregated low level edges.
     */
    /*@SuppressWarnings("unchecked")
    public List<Edge> getLowLevelEdges(Edge edge) {
        return (List<Edge>) getAggregatedEdgeMap().get(edge);
    }/*

    //CLASS A
    /**
     * Return all FAMIX entities currently presented by the graph.
     * 
     * @return The list of FAMIX entities.
     */
    /*public Set<AbstractFamixEntity> getAllFamixEntities() {
        return getFamixToNodeMap().keySet();
    }*/

    //CLASS A
    /**
     * Return all FAMIX associations currently presented by the graph.
     * 
     * @return The list of FAMIX associations.
     */
    /*public Set<FamixAssociation> getAllAssociations() {
        return getFamixToEdgeMap().keySet();
    }*/

    //CLASS A
    /**
     * Gets the famix to node map.
     * 
     * @return the famix to node map
     */
    /*private Map<AbstractFamixEntity, Node> getFamixToNodeMap() {
        return fFamixToNodeMap;
    }*/

    //CLASS A
    /**
     * Gets the famix to edge map.
     * 
     * @return the famix to edge map
     */
    /*private Map<FamixAssociation, Edge> getFamixToEdgeMap() {
        return fFamixToEdgeMap;
    }*/

    //CLASS B
    /**
     * Creates a node in the dependency graph for the given entity.
     * For container entities folder nodes are created.
     * 
     * @param entity The FAMIX entity to create a node for.
     * 
     * @return The created yFiles node.
     */
    /*public Node createNode(AbstractFamixEntity entity) {
        Node node = null;

        if (!this.contains(entity)) {
            Graph2D graph = getGraph(entity);
            if (isContainerEntityNode(entity)) {
                node = getHierarchyManager().createFolderNode(graph);
            } else if (FamixEntityMap.getInstance().containsType(entity.getClass())) {
                node = graph.createNode();
            }

            if (node != null) {
                this.getNodeToFamixMap().set(node, entity);
                this.getFamixToNodeMap().put(entity, node);
                fPropertyChangeSupport.firePropertyChange(DependencyGraphSingleton.ENTITY_ADDED, null, node);
            }
        } else {
            sLogger.warn("FAMIX entity already contained in graph " + entity);
        }

        return node;
    }*/

    //CLASS B
    /**
     * Create an edge in the dependency graph for the given association.
     * Both, the source and the target node have to be contained by the graph.
     * 
     * @param association The FAMIX association to create an edge for.
     * 
     * @return The created yFiles edge.
     */
    /*public Edge createEdge(FamixAssociation association) {
        Edge edge = null;

        Node nodeFrom = this.getNode(association.getFrom());
        Node nodeTo = this.getNode(association.getTo());
        if (nodeFrom != null && nodeTo != null) {
            if (!nodeFrom.equals(nodeTo)) {
                if (!this.contains(association)) {
                    edge = this.getHierarchyManager().createEdge(nodeFrom, nodeTo);

                    this.getEdgeToFamixMap().set(edge, association);
                    this.getFamixToEdgeMap().put(association, edge);
                    fPropertyChangeSupport.firePropertyChange(DependencyGraphSingleton.ASSOCIATION_ADDED, null, edge);
                } else {
                    sLogger.warn("FamixAssociation is already contained in the graph " + association);
                }

                // fireGraphEvent(new DA4JavaGraphEvent(this, DA4JavaGraphEvent.EDGE_CREATION_ASSOCIATION_SET, edge));
            }
        } else {
            sLogger.error("could not determine nodes of association: '" 
                    + association.getFrom().getUniqueName() + "' -> '"
                    + association.getTo().getUniqueName() + "'");
        }

        return edge;
    }*/

    //CLASS B
    /**
     * Fire high level edge created.
     * 
     * @param edge the edge
     */
    /*void fireHighLevelEdgeCreated(Edge edge) {
        fPropertyChangeSupport.firePropertyChange(DependencyGraphSingleton.HIGHLEVEL_EDGE_ADDED, null, edge);
    }*/

    //CLASS B
    /**
     * Remove the given entity and the corresponding node from the graph and the data containers.
     * 
     * @param entity The FAMIX entity to remove.
     * 
     * @return True if the entity/node has been removed otherwise false.
     */
    /*public boolean removeFamixEntity(AbstractFamixEntity entity) {
        boolean isRemoved = false;

        Node node = this.getNode(entity);
        if (node != null) {
            this.getFamixToNodeMap().remove(entity);
            this.getNodeToFamixMap().set(node, null);
            Graph graph = node.getGraph(); // if parent node has been removed, than node is not in the graph anymore
            if (graph != null) {
                node.getGraph().removeNode(node);
            }
            isRemoved = true;
        }

        return isRemoved;
    }*/

    //CLASS B
    /**
     * Remove the given association and the corresponding edge from the graph and the data containers.
     * Make sure that the higher-level edge the association belongs to has been turned into lower level
     * edges, before calling this function.
     * 
     * @param association The FAMIX association to remove.
     * 
     * @return True if the association/edge has been removed otherwise false.
     */
    /*public boolean removeAssociation(FamixAssociation association) {
        boolean isRemoved = false;

        Edge edge = this.getEdge(association);
        if (edge != null) {
            this.getFamixToEdgeMap().remove(association);
            this.getEdgeToFamixMap().set(edge, null);
            Graph graph = edge.getGraph();
            if (graph != null) { // graph is null for edges contained by a collapsed node
                graph.removeEdge(edge);
            }
            isRemoved = true;
        }

        return isRemoved;
    }*/

    //CLASS B
    /**
     * Reinsert the given low level edge into the graph. This is mandatory because the aggregated
     * edges are not valid any more in the graph.
     * 
     * @param lowLevelEdge The edge to reinsert into the graph.
     * 
     * @return The reinserted edge (if it has been successfully reinserted and replaced the old one).
     */
    /*public Edge reinsertLowLevelEdge(Edge lowLevelEdge) {
        Edge reinsertedEdge = null;

        FamixAssociation association = this.getAssociation(lowLevelEdge);
        if (association != null) {
            Node from = this.getNode(association.getFrom());
            Node to = this.getNode(association.getTo());
            reinsertedEdge = getHierarchyManager().createEdge(from, to);

            getEdgeToFamixMap().set(lowLevelEdge, null); // delete old edge

            getEdgeToFamixMap().set(reinsertedEdge, association); // add link between new low level edge and association
            getFamixToEdgeMap().put(association, reinsertedEdge); // and vice versa
        } else {
            sLogger.error("Could not get association of low level edge " + lowLevelEdge);
        }

        return reinsertedEdge;
    }*/

    //CLASS A
    /**
     * Gets the node to famix map.
     * 
     * @return the node to famix map
     */
    /*private NodeMap getNodeToFamixMap() {
        return getRegisteredNodeMaps()[0];
    }*/

    //CLASS A
    /**
     * Gets the edge to famix map.
     * 
     * @return the edge to famix map
     */
    /*private EdgeMap getEdgeToFamixMap() {
        return getRegisteredEdgeMaps()[0];
    }*/

    //CLASS A   
    /**
     * Gets the aggregated edge map.
     * 
     * @return the aggregated edge map
     */
    /*EdgeMap getAggregatedEdgeMap() {
        return getRegisteredEdgeMaps()[1];
    }*/

    //CLASS B
    /**
     * Get the graph to which the given entity should be added, i.e. the inner graph of the
     * entity's parent, if it exists, the root graph otherwise.
     * 
     * @param entity The entity to check.
     * 
     * @return The graph in which the entity is.
     */
    /*private Graph2D getGraph(AbstractFamixEntity entity) {
        Graph2D graph = null;
        AbstractFamixEntity parent = entity.getParent();
        if (parent == null) {
            // no parent -> return root graph
            graph = this;
        } else {
            if (!this.contains(parent)) {
                // parent node not in graph
                graph = this;
            } else {
                Node parentNode = this.getNode(parent);
                graph = (Graph2D) getHierarchyManager().getInnerGraph(parentNode);
            }
        }

        return graph;
    }*/

    //CLASS A
    /**
     * Check whether the given FAMIX entity is a container entity (entity that
     * implements the IHierarchicalElement interface and contains children.
     * 
     * @param entity The FAMIX entity to check.
     * 
     * @return True if the entity is a container entity otherwise false.
     */
    /*@SuppressWarnings("unchecked")
    public boolean isContainerEntityNode(AbstractFamixEntity entity) {
        boolean isContainerEntityNode = false;
        if (entity instanceof IHierarchicalElement) {
            IHierarchicalElement<? extends AbstractFamixEntity> parentEntity = (IHierarchicalElement<? extends AbstractFamixEntity>) entity;
            if (!parentEntity.getChildren().isEmpty()) {
                if (entity instanceof org.evolizer.famix.model.entities.FamixMethod) {
                    FamixMethod method = (FamixMethod) entity;
                    if (!method.getAnonymClasses().isEmpty()) {
                        isContainerEntityNode = true;
                    }
                } else {
                    isContainerEntityNode = true;
                }
            }
        }
        return isContainerEntityNode;
    }*/

    //CLASS A
    /**
     * Determine the FAMIX type of the given edge. If the edge is a high level edge
     * then the type of the first contained low level edge is used.
     * 
     * @param edge The edge to check.
     * 
     * @return The FAMIX association type.
     * 
     * @throws EvolizerRuntimeException the evolizer runtime exception
     */
    /*public java.lang.Class<? extends FamixAssociation> getEdgeType(Edge edge) throws EvolizerRuntimeException {
        Edge edgeToCheck = null;
        List<Edge> lowLevelEdges = this.getLowLevelEdges(edge);
        if (lowLevelEdges != null && lowLevelEdges.size() > 0) {
            edgeToCheck = lowLevelEdges.get(0);
        } else {
            edgeToCheck = edge;
        }

        Class<? extends FamixAssociation> type = null;
        try {
            FamixAssociation a = this.getAssociation(edgeToCheck);
            type = a.getClass();
        } catch (NullPointerException npe) {
            sLogger.error("Error obtaining edge type " + edge.toString() + " association not contained in edge map");
            throw new EvolizerRuntimeException("Error obtaining edge type " + edge.toString(), npe);
        }
        return type;
    }*/

    //CLASS B
    /**
     * Computes the visible descendant nodes of the given <code>parentNode</code>.
     * The list of nodes of the inner graph of the parent node (all visible
     * nodes) is intersected with the list of all descendant nodes of the parent
     * node.
     * 
     * @param parentNode The parent node
     * 
     * @return List of descendant nodes (parent node is included)
     */
    /*public NodeList getVisibleDescendants(Node parentNode) {
        NodeList visibleDescendants = new NodeList();
        NodeList allDescendants = getDescendants(parentNode);
        for (NodeCursor nc = getHierarchyManager().getInnerGraph(parentNode).nodes(); nc.ok(); nc.next()) {
            if (allDescendants.contains(nc.node())) {
                visibleDescendants.add(nc.node());
            }
        }

        return visibleDescendants;
    }*/

    //CLASS B
    /**
     * Gets the list of all descendant nodes of the given parent node.
     * 
     * @param parentNode Parent node.
     * 
     * @return List of all descendant nodes.
     */
    /*public NodeList getDescendants(Node parentNode) {
        NodeList descendants = new NodeList();

        descendants.add(parentNode);
        for (NodeCursor nc = getHierarchyManager().getChildren(parentNode); nc.ok(); nc.next()) {
            descendants.addAll(getDescendants(nc.node()));
        }

        return descendants;
    }*/

    //CLASS B
    /**
     * Gets the node info.
     * 
     * @param node the node
     * 
     * @return the node info
     */
    /*public String getNodeInfo(Node node) {
        AbstractFamixEntity entity = (AbstractFamixEntity) getNodeToFamixMap().get(node);
        String result = "Name: " + entity.getUniqueName();
        if (entity instanceof FamixPackage) {
            FamixPackage famixPackage = (FamixPackage) entity;
            // int nop = famixPackage.getPackages().size();
            // result = result + "\nSubpackages: " + nop;
            int noc = famixPackage.getClasses().size();
            result = result + "\nClasses: " + noc;
        } else if (entity instanceof FamixClass) {
            FamixClass famixClass = (FamixClass) entity;
            int nom = famixClass.getMethods().size();
            result = result + "\nMethods: " + nom;
            int noa = famixClass.getAttributes().size();
            result = result + "\nAttributes: " + noa;
            int noic = famixClass.getInnerClasses().size();
            result = result + "\nInner classes: " + noic;
        }
        return result;
    }*/


    //CLASS B
    /**
     * Gets the node tip.
     * 
     * @param node the node
     * 
     * @return the node tip
     */
    /*public String getNodeTip(Node node) {
        String tip = "";
        AbstractFamixEntity entity = this.getFamixEntity(node);
        if (entity != null) {
            tip = entity.getUniqueName();
        } else {
            sLogger.error("Could not determine FAMIX entity of node " + node);
            tip = "Error determing FAMIX entity of node " + node;
        }

        return tip;
    }*/

    //CLASS A
    /**
     * Returns a string representation of the FAMIX association this edge
     * represents.
     * 
     * @param selectedEdge the selected edge
     * 
     * @return a string representation of the FAMIX association this edge
     * represents.
     */
    /*@SuppressWarnings("unchecked")
    public String getEdgeInfo(Edge selectedEdge) {
        StringBuffer buf = new StringBuffer();

        FamixAssociation association = (FamixAssociation) getEdgeToFamixMap().get(selectedEdge);

        if (association != null) {
            buf.append(association.getFrom()).append(" ").append(association.getType()).append(" ").append(association.getTo());
        } else {
            // get contained edges
            List<Edge> edges = (List<Edge>) getAggregatedEdgeMap().get(selectedEdge);
            for (Edge edge : edges) {
                if (buf.length() > 0) {
                    buf.append("\n");
                }
                buf.append(getEdgeInfo(edge));
            }
        }

        return buf.toString();
    }*/

    //CLASS B
    /**
     * Returns the edge tip.
     * 
     * @param edge the edge
     * 
     * @return a string representation of the edge: [source entity] -> [target entity]
     */
/*    public String getEdgeTip(Edge edge) {
        StringBuilder tip = new StringBuilder("<HtMl>");
        List<Edge> lowLevelEdges = this.getLowLevelEdges(edge);
        if (lowLevelEdges != null && lowLevelEdges.size() > 0) {
            for (Edge lowLevelEdge : lowLevelEdges) {
                FamixAssociation association = this.getAssociation(lowLevelEdge);
                if (association != null) {
                    tip.append(simpleHTMLConverter(association.getLabel())).append("<br>");
                } else {
                    sLogger.error("Could not determine FAMIX association of edge " + edge);

                }
            }
        } else {
            FamixAssociation association = getAssociation(edge);
            if (association != null) {
                tip.append(simpleHTMLConverter(association.getLabel()));
            } else {
                sLogger.error("Could not determine FAMIX association of edge " + edge);
                tip.append("Error determining FAMIX association of edge " + edge);
            }
        }

        return tip.toString();
    }*/

    //CLASS B (helpers)
    /**
     * Simple html converter.
     * 
     * @param text the text
     * 
     * @return the string
     */
    /*private String simpleHTMLConverter(String text) {
        String htmlLikeText = text.replaceAll("<", "&lt;");
        return htmlLikeText.replaceAll(">", "&gt;");
    }*/
    
    //CLASS B
    /**
     * Gets the layout module.
     * 
     * @return The layout module
     */
    /*public LayoutModule getLayoutModule() {
        return fLayoutModule;
    }*/

    //CLASS B
    /**
     * Initialize the layout module. No change event is sent.
     * 
     * @param layoutModule the layout module
     */
    /*public void initLayoutModule(LayoutModule layoutModule) {
        fLayoutModule = layoutModule;
    }*/
    
    //CLASS B
    /**
     * Sets a new <code>LayoutModule</code> for this panel and re-layouts the
     * corresponding Graph2D.
     * 
     * @param layoutModule The layout module.
     */
    /*public void updateLayoutModule(LayoutModule layoutModule) {
        fLayoutModule = layoutModule;
        fPropertyChangeSupport.firePropertyChange(DependencyGraphSingleton.LAYOUT_MODULE_CHANGED, null, fLayoutModule);
//        refreshLayout(true, null, null);
    }*/
    
    //CLASS B
    /**
     * Signals that the dimensions of represented nodes have been changed so that
     * a re-layout of the graph is mandatory.
     */
    /*public void updatedNodeSizes() {
        fPropertyChangeSupport.firePropertyChange(DependencyGraphSingleton.NODE_SIZE_CHANGED, null, null);
    }*/
    
    //CLASS B
    /**
     * Adds the property change listener.
     * 
     * @param listener the listener
     */
    /*public void addPropertyChangeListener(PropertyChangeListener listener) {
        fPropertyChangeSupport.addPropertyChangeListener(listener);
    }*/

    //CLASS B
    /**
     * Removes the property change listener.
     * 
     * @param listener the listener
     */
    /*public void removePropertyChangeListener(PropertyChangeListener listener) {
        fPropertyChangeSupport.removePropertyChangeListener(listener);
    }*/
}
