package org.evolizer.da4java.graph.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.evolizer.core.exceptions.EvolizerRuntimeException;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.model.resources.entities.misc.IHierarchicalElement;

import y.base.Edge;
import y.base.EdgeMap;
import y.base.Node;
import y.base.NodeMap;
import y.view.Graph2D;

//CLASS A
public class GraphModelMapper extends Graph2D {
	/** FAMIX entity to yFiles node map of each entity contained in the graph. */
    private Map<AbstractFamixEntity, Node> fFamixToNodeMap;
    
    /** FAMIX association to yFiles edge map of each association contained in the graph. */
    private Map<FamixAssociation, Edge> fFamixToEdgeMap;
    
    /** The logger. */
    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(GraphModelMapper.class.getName());
    
    public GraphModelMapper() {
    	createNodeMap(); // node to FAMIX entity map
        createEdgeMap(); // edge to FAMIX association map
        createEdgeMap(); // high level to low level edges map
    	
    	fFamixToNodeMap = new HashMap<AbstractFamixEntity, Node>();
        fFamixToEdgeMap = new HashMap<FamixAssociation, Edge>();
    }
    
    /**
     * Return the yFiles Node for the given FAMIX entity.
     * 
     * @param entity The FAMIX entity.
     * 
     * @return The node that represents the given FAMIX entity.
     */
    public Node getNode(AbstractFamixEntity entity) {
        return getFamixToNodeMap().get(entity);
    }

    /**
     * Return the yFiles edge for the given FAMIX association.
     * 
     * @param association The FAMIX association.
     * 
     * @return The corresponding yFiles edge.
     */
    public Edge getEdge(FamixAssociation association) {
        return getFamixToEdgeMap().get(association);
    }

    /**
     * Return the FAMIX entity of the given node.
     * 
     * @param node The node.
     * 
     * @return The FAMIX entity for the given node.
     */
    public AbstractFamixEntity getFamixEntity(Node node) {
        return (AbstractFamixEntity) getNodeToFamixMap().get(node);
    }

    /**
     * Return corresponding FAMIX entities of the given list of nodes.
     * 
     * @param nodes The list of nodes.
     * 
     * @return The list of matched FAMIX entities.
     */
    public List<AbstractFamixEntity> getFamixEntities(List<Node> nodes) {
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
    }

    /**
     * Return the corresponding FAMIX associations of the given list of edges
     * (low and higher level).
     * 
     * @param edges The list of edges.
     * 
     * @return  The list of associations.
     */
    public List<FamixAssociation> getFamixAssociations(List<Edge> edges) {
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
    }

    /**
     * Return the FAMIX association of the given low level edge.
     * 
     * @param lowLevelEdge The lowe level edge.
     * 
     * @return The corresponding FAMIX association.
     */
    public FamixAssociation getAssociation(Edge lowLevelEdge) {
        return (FamixAssociation) getEdgeToFamixMap().get(lowLevelEdge);
    }
    
    /**
     * Return the list of aggregated low level edges represented by the given
     * high level edge.
     * 
     * @param edge The high level edge.
     * 
     * @return The list of aggregated low level edges.
     */
    @SuppressWarnings("unchecked")
    public List<Edge> getLowLevelEdges(Edge edge) {
        return (List<Edge>) getAggregatedEdgeMap().get(edge);
    }

    /**
     * Return all FAMIX entities currently presented by the graph.
     * 
     * @return The list of FAMIX entities.
     */
    public Set<AbstractFamixEntity> getAllFamixEntities() {
        return getFamixToNodeMap().keySet();
    }

    /**
     * Return all FAMIX associations currently presented by the graph.
     * 
     * @return The list of FAMIX associations.
     */
    public Set<FamixAssociation> getAllAssociations() {
        return getFamixToEdgeMap().keySet();
    }

    /**
     * Gets the famix to node map.
     * 
     * @return the famix to node map
     */
    protected Map<AbstractFamixEntity, Node> getFamixToNodeMap() {
        return fFamixToNodeMap;
    }

    /**
     * Gets the famix to edge map.
     * 
     * @return the famix to edge map
     */
    protected Map<FamixAssociation, Edge> getFamixToEdgeMap() {
        return fFamixToEdgeMap;
    }
    
    /**
     * Gets the node to famix map.
     * 
     * @return the node to famix map
     */
    protected NodeMap getNodeToFamixMap() {
        return getRegisteredNodeMaps()[0];
    }

    /**
     * Gets the edge to famix map.
     * 
     * @return the edge to famix map
     */
    protected EdgeMap getEdgeToFamixMap() {
        return getRegisteredEdgeMaps()[0];
    }

    /**
     * Gets the aggregated edge map.
     * 
     * @return the aggregated edge map
     */
    EdgeMap getAggregatedEdgeMap() {
        return getRegisteredEdgeMaps()[1];
    }
    
    /**
     * Check whether the given FAMIX entity is a container entity (entity that
     * implements the IHierarchicalElement interface and contains children.
     * 
     * @param entity The FAMIX entity to check.
     * 
     * @return True if the entity is a container entity otherwise false.
     */
    @SuppressWarnings("unchecked")
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
    }

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
    public java.lang.Class<? extends FamixAssociation> getEdgeType(Edge edge) throws EvolizerRuntimeException {
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
    }
    
    /**
     * Returns a string representation of the FAMIX association this edge
     * represents.
     * 
     * @param selectedEdge the selected edge
     * 
     * @return a string representation of the FAMIX association this edge
     * represents.
     */
    @SuppressWarnings("unchecked")
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
    }
}
