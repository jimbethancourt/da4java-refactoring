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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.log4j.Logger;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.famix.model.entities.FamixAssociation;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.hierarchy.GroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;

/**
 * Helper class that implements edge grouping behavior. Multiple edges between
 * collapsed subgraphs are aggregated to one edge whose width corresponds the
 * number of contained edges. If a node is expanded lower level edges are
 * reinserted, if it is collapsed edges are aggregated. A map containing the
 * aggregated and its contained edges is stored on the root graph. 
 * 
 * @author Martin Pinzger, Katja Graefenhain
 */
public class EdgeGrouper {

    /** The logger. */
    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(EdgeGrouper.class.getName());

    /** The graph. */
    private GraphManager fGraph;

    /**
     * Instantiates a new edge grouper.
     * 
     * @param graph the graph
     */
    public EdgeGrouper(GraphManager graph) {
        fGraph = graph;
    }

    /**
     * Aggregates all edges of the same type in the root graph. Lower level edges
     * are replaced by a single edge and removed from the graph.
     */
    public void groupAll() {
        List<Edge> allEdges = new ArrayList<Edge>();

        HierarchyManager hierarchyManager = getRootGraph().getHierarchyManager();
        Graph rootGraph = hierarchyManager.getRootGraph();
        for (NodeCursor nc = rootGraph.nodes(); nc.ok(); nc.next()) {
            Node v = nc.node();
            for (EdgeCursor ec = v.outEdges(); ec.ok(); ec.next()) {
                Edge e = ec.edge();
                allEdges.add(e);
            }
        }
        group(allEdges);
    }

    /**
     * Reinserts the incoming and outgoing original (lower level) edges of the
     * expanded or collapsed folder node. Existing higher level edges are
     * removed from the graph. Other low level edges (that were not aggregated
     * previously) are also added.
     * 
     * The caller is responsible for preventing double entries.
     * 
     * @param node Node whose incoming and outgoing original edges are
     * reinserted.
     * 
     * @return List of reinserted + other inner graph low level edges.
     */
    public List<Edge> reinsertLowLevelEdges(Node node) {
        List<Edge> reinsertedLowEdges = new ArrayList<Edge>();

        // attention - node and edge maps are all stored in the root graph
        EdgeMap aggregatedEdgeMap = getAggregatedEdgeMap();

        List<Edge> inOutEdges = getInOutEdges(node);
        for (Edge edge : inOutEdges) {
            List<Edge> lowLevelEdges = fGraph.getGraphModelMapper().getLowLevelEdges(edge);
            if (lowLevelEdges != null && lowLevelEdges.size() > 0) {
                for (Edge lowLevelEdge : lowLevelEdges) {
                    Edge reinsertedEdge = fGraph.reinsertLowLevelEdge(lowLevelEdge);
                    if (reinsertedEdge != null) {
                        reinsertedLowEdges.add(reinsertedEdge);
                    } else {
                        sLogger.error("Could not get association of low level edge " + lowLevelEdge);
                    }
                }

                // remove higher level edge from edge map and from graph
                aggregatedEdgeMap.set(edge, null);
                getRootGraph().removeEdge(edge);
            } else {
                reinsertedLowEdges.add(edge);
            }
        }

        return reinsertedLowEdges;
    }

    /**
     * Handle the aggregation of edges when expanding a folder node.
     * <ul>
     * <li>Compute visible nodes contained by the expanded folder node (folder
     * node is included)
     * <li>For each node replace high level edges and reinsert corresponding
     * low level edges of these nodes
     * <li>Aggregate expanded and inserted low level edges and replace them by
     * corresponding high level edges.
     * </ul>
     * 
     * @param parentNode The folder node to expand.
     */
    public void handleOpenFolder(Node parentNode) {
        sLogger.debug("Expand folder node " + parentNode);

        getHierarchyManager().openFolder(parentNode);

        // reinsert low level edges
        NodeList visibleDescendants = fGraph.getVisibleDescendants(parentNode);
        List<Edge> reinsertedLowLevelEdges = new ArrayList<Edge>();
        for (Object node : visibleDescendants) {
//            reinsertLowLevelEdges((Node) node);
            for (Edge edge : reinsertLowLevelEdges((Node) node)) {
                if (!reinsertedLowLevelEdges.contains(edge)) {
                    reinsertedLowLevelEdges.add(edge);
                }
            }
        }

        group(reinsertedLowLevelEdges);
    }

    /**
     * Handle the aggregation of edges when collapsing a folder node.
     * <ul>
     * <li>Reinsert low level incoming/outgoing edges of the folder node.
     * <li>Aggregate inserted low level edges and replace them by corresponding high level edges.
     * </ul>
     * 
     * @param parentNode The folder node to collapse.
     */
    public void handleCloseFolder(Node parentNode) {
        sLogger.debug("Collapse folder node " + parentNode);

        NodeRealizer nr = ((Graph2D) parentNode.getGraph()).getRealizer(parentNode);
        if (nr instanceof GroupNodeRealizer) {
            GroupNodeRealizer gr = (GroupNodeRealizer) nr;

            if (!gr.isGroupClosed()) {
                getHierarchyManager().closeGroup(parentNode);
                List<Edge> reinsertedLowLevelEdges = reinsertLowLevelEdges(parentNode);
                group(reinsertedLowLevelEdges);
            } else {
                sLogger.debug("Folder node is already collapsed " + parentNode);
            }
        }
    }

    /**
     * Checks for all edges whether it represents an invocation or an
     * inheritance relationship. Then all edges of the same type are aggregated
     * Given lower level edges are removed and a high level edge with the
     * corresponding width representing the number of grouped/aggregated
     * lower edges is created.
     * 
     * @param edges The list of lower edges to aggregate.
     */
    private void group(List<Edge> edges) {
        Hashtable<String, List<Edge>> typeToEdgeListMap = new Hashtable<String, List<Edge>>();

        // get list of lower level edges per edge type
        for (Edge edge : edges) {
            FamixAssociation association = fGraph.getGraphModelMapper().getAssociation(edge);
            if (association != null) {
                List<Edge> edgeList;
                if (typeToEdgeListMap.containsKey(association.getType())) {
                    edgeList = typeToEdgeListMap.get(association.getType());
                } else {
                    edgeList = new ArrayList<Edge>();
                    typeToEdgeListMap.put(association.getType(), edgeList);
                }
                edgeList.add(edge);
            }
        }

        // create higher level edges per edge type
        for (Entry<String, List<Edge>> entry : typeToEdgeListMap.entrySet()) {
            createHighLevelEdges(aggregateEdges(entry.getValue()), entry.getKey());
        }
    }

    /**
     * Inserts the higher level edges between the node pairs of
     * <source>nodePairToEdgeMap</source> with a strength that corresponds the
     * number of aggregated lower level edges.
     * 
     * Lower level edges are remembered in the edge map of the higher level edge
     * and then removed from the graph (but not from the edge map).
     * 
     * @param nodePairToEdgeMap Map of from/to nodes referring to the list of lower level
     * edges.
     * @param edgeType the edge type
     * 
     * @return a list with the created higher level edges
     */
    private List<Edge> createHighLevelEdges(Map<MultiKey, List<Edge>> nodePairToEdgeMap, String edgeType) {
        // use the second edge map to store aggregated edges
        EdgeMap aggregatedToContainedEdges = getAggregatedEdgeMap();
        List<Edge> addedHighLevelEdges = new ArrayList<Edge>();

        for (Entry<MultiKey, List<Edge>> entry : nodePairToEdgeMap.entrySet()) {
            Node source = (Node) entry.getKey().getKey(0);
            Node target = (Node) entry.getKey().getKey(1);

            List<Edge> lowLevelEdges = entry.getValue();
            // insert a new edge with the strength corr. to the number of
            // aggregated edges
            Edge highLevelEdge = getHierarchyManager().createEdge(source, target);
            addedHighLevelEdges.add(highLevelEdge);
            aggregatedToContainedEdges.set(highLevelEdge, lowLevelEdges);

            for (Edge lowLevelEdge : lowLevelEdges) {
                try {
                    getRootGraph().removeEdge(lowLevelEdge);
                } catch (IllegalArgumentException iae) {
                    iae.printStackTrace();
                    sLogger.error("Error removing low level edge " + lowLevelEdge);
                }
            }

            fGraph.fireHighLevelEdgeCreated(highLevelEdge);
        }

        return addedHighLevelEdges;
    }

    /**
     * Computes higher level edges from the list of edges in
     * <source>lowLevelEdges</source>. Each higher level edge is stored in the
     * map with key from/to node and the list of corresponding low level edges.
     * 
     * @param lowLevelEdges List of low level edges to aggregated.
     * 
     * @return Map containing the list of aggregated lower level edges.
     */
    private Map<MultiKey, List<Edge>> aggregateEdges(List<Edge> lowLevelEdges) {
        Map<MultiKey, List<Edge>> nodePairToEdgeMap = new Hashtable<MultiKey, List<Edge>>();
        for (Edge edge : lowLevelEdges) {
            MultiKey key = new MultiKey(edge.source(), edge.target());
            if (nodePairToEdgeMap.containsKey(key)) {
                nodePairToEdgeMap.get(key).add(edge);
            } else {
                List<Edge> edgeList = new ArrayList<Edge>();
                edgeList.add(edge);
                nodePairToEdgeMap.put(key, edgeList);
            }
        }

        return nodePairToEdgeMap;
    }

    /**
     * Computes the list of incoming and outgoing edges of node <source>Node</node>.
     * 
     * @param node The node.
     * 
     * @return List of incoming and outgoing edges.
     */
    private List<Edge> getInOutEdges(Node node) {
        List<Edge> inOutEdges = new ArrayList<Edge>();
        for (EdgeCursor ec = node.outEdges(); ec.ok(); ec.next()) {
            inOutEdges.add(ec.edge());
        }
        for (EdgeCursor ec = node.inEdges(); ec.ok(); ec.next()) {
            inOutEdges.add(ec.edge());
        }

        return inOutEdges;
    }

    /**
     * Returns the root graph.
     * 
     * @return the rootGraph
     */
    private Graph2D getRootGraph() {
        return fGraph;
    }

    /**
     * Returns the hierarchy manager.
     * 
     * @return the hierarchyManager
     */
    private HierarchyManager getHierarchyManager() {
        return fGraph.getHierarchyManager();
    }

    /**
     * Gets the aggregated edge map.
     * 
     * @return the aggregated edge map
     */
    private EdgeMap getAggregatedEdgeMap() {
        return fGraph.getGraphModelMapper().getAggregatedEdgeMap();
    }
}
