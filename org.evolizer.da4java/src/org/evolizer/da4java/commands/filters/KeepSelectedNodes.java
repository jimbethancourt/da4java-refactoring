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
import org.evolizer.da4java.commands.selection.AbstractSelectionStrategy;
import org.evolizer.da4java.commands.selection.SelectEditedEntities;
import org.evolizer.da4java.commands.selection.SelectNodes;
import org.evolizer.da4java.graph.data.EdgeGrouper;
import org.evolizer.da4java.graph.data.GraphLoader;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;

import y.base.Node;
import y.base.NodeCursor;

/**
 * Keep the selected entities and the dependencies between them and remove all other entities
 * and associations from the graph.
 * 
 * @author Katja Graefenhain, Martin Pinzger
 */
public class KeepSelectedNodes extends AbstractGraphFilterCommand {
    
    /** The selected nodes. */
    private List<Node> fSelectedNodes;
    
    /** The association type. */
    private java.lang.Class<? extends FamixAssociation> fAssociationType;

    /**
     * The constructor.
     * 
     * @param selectedNodes The selected nodes
     * @param associationType The association type
     * @param graphLoader The graph loader
     * @param edgeGrouper The edge grouper
     */
    public KeepSelectedNodes(List<Node> selectedNodes,
            java.lang.Class<? extends FamixAssociation> associationType,
            GraphLoader graphLoader, 
            EdgeGrouper edgeGrouper) {

        super(graphLoader, edgeGrouper);
        fSelectedNodes = selectedNodes;
        fAssociationType = associationType;
    }

    /** 
     * {@inheritDoc}
     */
    public void execute() {
        setEditResult(new EditResult());
        List<AbstractFamixEntity> entities = getGraphLoader().getGraph().getGraphModelMapper().getFamixEntities(fSelectedNodes);

        if (!entities.isEmpty()) {
            Set<AbstractFamixEntity> entitiesToRemove = getEntitiesToRemove(); 
            Set<FamixAssociation> associationsToRemove = getAssociationsToRemove();

            fireGraphPreEvent();

            for (NodeCursor nc = getGraphLoader().getGraph().nodes(); nc.ok(); nc.next()) {
                Node node = nc.node();
                getEdgeGrouper().reinsertLowLevelEdges(node);
            }

            getEditResult().addAll(getGraphLoader().removeEntitiesAndAssociations(new ArrayList<AbstractFamixEntity>(entitiesToRemove)));
            getEditResult().addAll(getGraphLoader().removeAssociations(new ArrayList<FamixAssociation>(associationsToRemove)));

            getEdgeGrouper().groupAll();

            fireGraphPostEvent();
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected void initUndoSelectionStrategy() {
        AbstractSelectionStrategy preLayoutSelection = new SelectEditedEntities(this);
        preLayoutSelection.initSelection();
        setPreLayoutSelectionStrategy(preLayoutSelection);

        AbstractSelectionStrategy postLayoutSelection = new SelectNodes(this, fSelectedNodes);
        postLayoutSelection.initSelection();
        setPostLayoutSelectionStrategy(postLayoutSelection);
    }

    /**
     * Keep the selected nodes, their parents, and descendants. The other nodes in the graph
     * are removed.
     * 
     * @return the entities to remove
     */
    protected Set<AbstractFamixEntity> getEntitiesToRemove() {
        List<AbstractFamixEntity> entities = getGraphLoader().getGraph().getGraphModelMapper().getFamixEntities(fSelectedNodes);

        Set<AbstractFamixEntity> remainingEntities = new HashSet<AbstractFamixEntity>();
        for (AbstractFamixEntity entity : entities) {
            if (!remainingEntities.contains(entity)) {
                remainingEntities.addAll(getGraphLoader().getSnapshotAnalyzer().getParentEntities(entity));
                remainingEntities.addAll(getGraphLoader().getSnapshotAnalyzer().getDescendants(entity));
            }
        }

        Set<AbstractFamixEntity> entitiesToRemove = new HashSet<AbstractFamixEntity>(getGraphLoader().getGraph().getGraphModelMapper().getAllFamixEntities());
        entitiesToRemove.removeAll(remainingEntities);

        return entitiesToRemove;
    }

    /**
     * Return the associations to remove.
     * 
     * @return the associations to remove
     */
    protected Set<FamixAssociation> getAssociationsToRemove() {
        List<AbstractFamixEntity> entities = getGraphLoader().getGraph().getGraphModelMapper().getFamixEntities(fSelectedNodes);

        Set<FamixAssociation> associationsToRemove = new HashSet<FamixAssociation>(); 
        if (fAssociationType != null) {
            associationsToRemove.addAll(getGraphLoader().getSnapshotAnalyzer().getAssociationsBetweenParentEntities(entities, null));
            List<? extends FamixAssociation> remainingAssociations = getGraphLoader().getSnapshotAnalyzer().getAssociationsBetweenParentEntities(entities, fAssociationType);
            associationsToRemove.removeAll(remainingAssociations);
        }

        return associationsToRemove;
    }

    /** 
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Keep dependencies between selected nodes and filter remaining nodes and dependencies";
    }
}
