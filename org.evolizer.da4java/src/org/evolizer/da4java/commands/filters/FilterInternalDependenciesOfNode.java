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
package org.evolizer.da4java.commands.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evolizer.da4java.commands.EditResult;
import org.evolizer.da4java.graph.data.EdgeGrouper;
import org.evolizer.da4java.graph.data.GraphLoader;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;

import y.base.Edge;
import y.base.Node;
import y.base.NodeCursor;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.hierarchy.GroupNodeRealizer;

/**
 * Filter internal entities and dependencies of the selected parent node, that
 * do not have any dependency to an outside entity.
 * 
 * @author Martin Pinzger, Katja Graefenhain
 */
public class FilterInternalDependenciesOfNode extends AbstractGraphFilterCommand {
    
    /** The selected node. */
    private Node fSelectedNode;

    /**
     * The constructor.
     * 
     * @param selectedNode The selected node
     * @param graphLoader The graph loader
     * @param edgeGrouper The edge grouper
     */
    public FilterInternalDependenciesOfNode(Node selectedNode, GraphLoader graphLoader, EdgeGrouper edgeGrouper) {
        super(graphLoader, edgeGrouper);
        fSelectedNode = selectedNode;
    }

    /** 
     * {@inheritDoc}
     */
    public void execute() {
        setEditResult(new EditResult());

        Graph2D rootGraph = (Graph2D) getGraphLoader().getHierarchyManager().getRootGraph();
        NodeRealizer nr = rootGraph.getRealizer(fSelectedNode);
        if (nr instanceof GroupNodeRealizer) {
            fireGraphPreEvent();

            for (NodeCursor nc = getGraphLoader().getGraph().nodes(); nc.ok(); nc.next()) {
                getEdgeGrouper().reinsertLowLevelEdges(nc.node());
            }

            // first remove the associations and then the lonely child entities of the selected node
            Set<FamixAssociation> associationsToRemove = getAssociationsToRemove();
            Set<AbstractFamixEntity> entitiesToRemove = getEntitiesToRemove(); 

            getEditResult().addAll(getGraphLoader().removeAssociations(new ArrayList<FamixAssociation>(associationsToRemove)));
            getEditResult().addAll(getGraphLoader().removeEntitiesAndAssociations(new ArrayList<AbstractFamixEntity>(entitiesToRemove)));

            getEdgeGrouper().groupAll();

            fireGraphPostEvent();
        }

    }

    /**
     * Determine child entities with no dependency to an outside entity.
     * 
     * @return the entities to remove
     */
    protected Set<AbstractFamixEntity> getEntitiesToRemove() {
        AbstractFamixEntity entity = getGraphLoader().getGraph().getGraphModelMapper().getFamixEntity(fSelectedNode);

        List<AbstractFamixEntity> descendants = getGraphLoader().getSnapshotAnalyzer().getDescendants(entity);
        descendants.remove(entity);

        List<FamixAssociation> associationsToOtherEntities = getGraphLoader().getSnapshotAnalyzer().queryAssociationsOfEntities(descendants, null, "from");
        associationsToOtherEntities.addAll(getGraphLoader().getSnapshotAnalyzer().queryAssociationsOfEntities(descendants, null, "to"));

        Set<AbstractFamixEntity> entitiesToRemove = new HashSet<AbstractFamixEntity>(descendants);
        for (FamixAssociation association : associationsToOtherEntities) {
            Edge edge = getGraphLoader().getGraph().getGraphModelMapper().getEdge(association);
            if (edge != null) {
                AbstractFamixEntity from = association.getFrom();
                AbstractFamixEntity to = association.getTo();
                if (descendants.contains(from) && getGraphLoader().getGraph().contains(to)) {
                    entitiesToRemove.remove(from);
                    entitiesToRemove.removeAll(getGraphLoader().getSnapshotAnalyzer().getParentEntities(from));
                } else if (descendants.contains(to) && getGraphLoader().getGraph().contains(from)) {
                    entitiesToRemove.remove(to);
                    entitiesToRemove.removeAll(getGraphLoader().getSnapshotAnalyzer().getParentEntities(to));
                }
            }
        }

        return entitiesToRemove;
    }

    /**
     * Gets the associations to remove.
     * 
     * @return the associations to remove
     */
    protected Set<FamixAssociation> getAssociationsToRemove() {
        Set<FamixAssociation> associationsToRemove = new HashSet<FamixAssociation>();

        AbstractFamixEntity entity = getGraphLoader().getGraph().getGraphModelMapper().getFamixEntity(fSelectedNode);

        List<AbstractFamixEntity> descendants = getGraphLoader().getSnapshotAnalyzer().getDescendants(entity);
        associationsToRemove.addAll(getGraphLoader().getSnapshotAnalyzer().queryAssociationsBetweenEntities(descendants, null));

        return associationsToRemove;
    }

    /** 
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Filter internal dependencies and not connected nodes";
    }
}
