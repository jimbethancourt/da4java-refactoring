package org.evolizer.da4java.graph.data;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import org.apache.log4j.Logger;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.da4java.graph.utils.FamixEntityMap;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixPackage;

import y.base.Edge;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.module.LayoutModule;
import y.view.Graph2D;

//CLASS B
public class GraphManager extends Graph2D {
	/** The layout module. */
    private LayoutModule fLayoutModule;
    
    /** The property change support. */
    private PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);
    
    private GraphModelMapper graphModelMapper = new GraphModelMapper();
	
	/** The logger. */
    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(GraphManager.class.getName());
    
    public GraphManager() {
    	createNodeMap(); // node to FAMIX entity map
        createEdgeMap(); // edge to FAMIX association map
        createEdgeMap(); // high level to low level edges map
    }
    
    public GraphModelMapper getGraphModelMapper() {
    	return this.graphModelMapper;
    }
    
    /**
     * Checks whether the current graph contains the given FAMIX entity.
     * 
     * @param entity The FAMIX entity to check.
     * 
     * @return Ture if the FAMIX entity is already in the graph otherwise false.
     */
    public boolean contains(AbstractFamixEntity entity) {
        return graphModelMapper.getFamixToNodeMap().containsKey(entity);
    }

    /**
     * Check whether the given association is already contained in the graph.
     * 
     * @param association The FAMIX association to check.
     * 
     * @return True if the association is already in the graph otherwise false.
     */
    public boolean contains(FamixAssociation association) {
        return graphModelMapper.getFamixToEdgeMap().containsKey(association);
    }
    
    /**
     * Creates a node in the dependency graph for the given entity.
     * For container entities folder nodes are created.
     * 
     * @param entity The FAMIX entity to create a node for.
     * 
     * @return The created yFiles node.
     */
    public Node createNode(AbstractFamixEntity entity) {
        Node node = null;

        if (!this.contains(entity)) {
            Graph2D graph = getGraph(entity);
            if (graphModelMapper.isContainerEntityNode(entity)) {
                node = getHierarchyManager().createFolderNode(graph);
            } else if (FamixEntityMap.getInstance().containsType(entity.getClass())) {
                node = graph.createNode();
            }

            if (node != null) {
            	graphModelMapper.getNodeToFamixMap().set(node, entity);
            	graphModelMapper.getFamixToNodeMap().put(entity, node);
                fPropertyChangeSupport.firePropertyChange(DependencyGraphSingleton.ENTITY_ADDED, null, node);
            }
        } else {
            sLogger.warn("FAMIX entity already contained in graph " + entity);
        }

        return node;
    }

    /**
     * Create an edge in the dependency graph for the given association.
     * Both, the source and the target node have to be contained by the graph.
     * 
     * @param association The FAMIX association to create an edge for.
     * 
     * @return The created yFiles edge.
     */
    public Edge createEdge(FamixAssociation association) {
        Edge edge = null;

        Node nodeFrom = graphModelMapper.getNode(association.getFrom());
        Node nodeTo = graphModelMapper.getNode(association.getTo());
        if (nodeFrom != null && nodeTo != null) {
            if (!nodeFrom.equals(nodeTo)) {
                if (!this.contains(association)) {
                    edge = this.getHierarchyManager().createEdge(nodeFrom, nodeTo);

                    graphModelMapper.getEdgeToFamixMap().set(edge, association);
                    graphModelMapper.getFamixToEdgeMap().put(association, edge);
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
    }
    
    /**
     * Fire high level edge created.
     * 
     * @param edge the edge
     */
    void fireHighLevelEdgeCreated(Edge edge) {
        fPropertyChangeSupport.firePropertyChange(DependencyGraphSingleton.HIGHLEVEL_EDGE_ADDED, null, edge);
    }
    
    /**
     * Remove the given entity and the corresponding node from the graph and the data containers.
     * 
     * @param entity The FAMIX entity to remove.
     * 
     * @return True if the entity/node has been removed otherwise false.
     */
    public boolean removeFamixEntity(AbstractFamixEntity entity) {
        boolean isRemoved = false;

        Node node = graphModelMapper.getNode(entity);
        if (node != null) {
        	graphModelMapper.getFamixToNodeMap().remove(entity);
        	graphModelMapper.getNodeToFamixMap().set(node, null);
            Graph graph = node.getGraph(); // if parent node has been removed, than node is not in the graph anymore
            if (graph != null) {
                node.getGraph().removeNode(node);
            }
            isRemoved = true;
        }

        return isRemoved;
    }

    /**
     * Remove the given association and the corresponding edge from the graph and the data containers.
     * Make sure that the higher-level edge the association belongs to has been turned into lower level
     * edges, before calling this function.
     * 
     * @param association The FAMIX association to remove.
     * 
     * @return True if the association/edge has been removed otherwise false.
     */
    public boolean removeAssociation(FamixAssociation association) {
        boolean isRemoved = false;

        Edge edge = graphModelMapper.getEdge(association);
        if (edge != null) {
        	graphModelMapper.getFamixToEdgeMap().remove(association);
        	graphModelMapper.getEdgeToFamixMap().set(edge, null);
            Graph graph = edge.getGraph();
            if (graph != null) { // graph is null for edges contained by a collapsed node
                graph.removeEdge(edge);
            }
            isRemoved = true;
        }

        return isRemoved;
    }

    /**
     * Reinsert the given low level edge into the graph. This is mandatory because the aggregated
     * edges are not valid any more in the graph.
     * 
     * @param lowLevelEdge The edge to reinsert into the graph.
     * 
     * @return The reinserted edge (if it has been successfully reinserted and replaced the old one).
     */
    public Edge reinsertLowLevelEdge(Edge lowLevelEdge) {
        Edge reinsertedEdge = null;

        FamixAssociation association = graphModelMapper.getAssociation(lowLevelEdge);
        if (association != null) {
            Node from = graphModelMapper.getNode(association.getFrom());
            Node to = graphModelMapper.getNode(association.getTo());
            reinsertedEdge = getHierarchyManager().createEdge(from, to);

            graphModelMapper.getEdgeToFamixMap().set(lowLevelEdge, null); // delete old edge

            graphModelMapper.getEdgeToFamixMap().set(reinsertedEdge, association); // add link between new low level edge and association
            graphModelMapper.getFamixToEdgeMap().put(association, reinsertedEdge); // and vice versa
        } else {
            sLogger.error("Could not get association of low level edge " + lowLevelEdge);
        }

        return reinsertedEdge;
    }
    
    /**
     * Get the graph to which the given entity should be added, i.e. the inner graph of the
     * entity's parent, if it exists, the root graph otherwise.
     * 
     * @param entity The entity to check.
     * 
     * @return The graph in which the entity is.
     */
    private Graph2D getGraph(AbstractFamixEntity entity) {
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
                Node parentNode = graphModelMapper.getNode(parent);
                graph = (Graph2D) getHierarchyManager().getInnerGraph(parentNode);
            }
        }

        return graph;
    }
    
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
    public NodeList getVisibleDescendants(Node parentNode) {
        NodeList visibleDescendants = new NodeList();
        NodeList allDescendants = getDescendants(parentNode);
        for (NodeCursor nc = getHierarchyManager().getInnerGraph(parentNode).nodes(); nc.ok(); nc.next()) {
            if (allDescendants.contains(nc.node())) {
                visibleDescendants.add(nc.node());
            }
        }

        return visibleDescendants;
    }

    /**
     * Gets the list of all descendant nodes of the given parent node.
     * 
     * @param parentNode Parent node.
     * 
     * @return List of all descendant nodes.
     */
    public NodeList getDescendants(Node parentNode) {
        NodeList descendants = new NodeList();

        descendants.add(parentNode);
        for (NodeCursor nc = getHierarchyManager().getChildren(parentNode); nc.ok(); nc.next()) {
            descendants.addAll(getDescendants(nc.node()));
        }

        return descendants;
    }

    /**
     * Gets the node info.
     * 
     * @param node the node
     * 
     * @return the node info
     */
    public String getNodeInfo(Node node) {
        AbstractFamixEntity entity = (AbstractFamixEntity) graphModelMapper.getNodeToFamixMap().get(node);
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
    }

    /**
     * Gets the node tip.
     * 
     * @param node the node
     * 
     * @return the node tip
     */
    public String getNodeTip(Node node) {
        String tip = "";
        AbstractFamixEntity entity = graphModelMapper.getFamixEntity(node);
        if (entity != null) {
            tip = entity.getUniqueName();
        } else {
            sLogger.error("Could not determine FAMIX entity of node " + node);
            tip = "Error determing FAMIX entity of node " + node;
        }

        return tip;
    }
    
    /**
     * Returns the edge tip.
     * 
     * @param edge the edge
     * 
     * @return a string representation of the edge: [source entity] -> [target entity]
     */
    public String getEdgeTip(Edge edge) {
        StringBuilder tip = new StringBuilder("<HtMl>");
        List<Edge> lowLevelEdges = graphModelMapper.getLowLevelEdges(edge);
        if (lowLevelEdges != null && lowLevelEdges.size() > 0) {
            for (Edge lowLevelEdge : lowLevelEdges) {
                FamixAssociation association = graphModelMapper.getAssociation(lowLevelEdge);
                if (association != null) {
                    tip.append(simpleHTMLConverter(association.getLabel())).append("<br>");
                } else {
                    sLogger.error("Could not determine FAMIX association of edge " + edge);

                }
            }
        } else {
            FamixAssociation association = graphModelMapper.getAssociation(edge);
            if (association != null) {
                tip.append(simpleHTMLConverter(association.getLabel()));
            } else {
                sLogger.error("Could not determine FAMIX association of edge " + edge);
                tip.append("Error determining FAMIX association of edge " + edge);
            }
        }

        return tip.toString();
    }

    /**
     * Simple html converter.
     * 
     * @param text the text
     * 
     * @return the string
     */
    private String simpleHTMLConverter(String text) {
        String htmlLikeText = text.replaceAll("<", "&lt;");
        return htmlLikeText.replaceAll(">", "&gt;");
    }
    
    /**
     * Gets the layout module.
     * 
     * @return The layout module
     */
    public LayoutModule getLayoutModule() {
        return fLayoutModule;
    }

    /**
     * Initialize the layout module. No change event is sent.
     * 
     * @param layoutModule the layout module
     */
    public void initLayoutModule(LayoutModule layoutModule) {
        fLayoutModule = layoutModule;
    }
    
    /**
     * Sets a new <code>LayoutModule</code> for this panel and re-layouts the
     * corresponding Graph2D.
     * 
     * @param layoutModule The layout module.
     */
    public void updateLayoutModule(LayoutModule layoutModule) {
        fLayoutModule = layoutModule;
        fPropertyChangeSupport.firePropertyChange(DependencyGraphSingleton.LAYOUT_MODULE_CHANGED, null, fLayoutModule);
//        refreshLayout(true, null, null);
    }
    
    /**
     * Signals that the dimensions of represented nodes have been changed so that
     * a re-layout of the graph is mandatory.
     */
    public void updatedNodeSizes() {
        fPropertyChangeSupport.firePropertyChange(DependencyGraphSingleton.NODE_SIZE_CHANGED, null, null);
    }
    
    /**
     * Adds the property change listener.
     * 
     * @param listener the listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        fPropertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes the property change listener.
     * 
     * @param listener the listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        fPropertyChangeSupport.removePropertyChangeListener(listener);
    }
}
