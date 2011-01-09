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
import org.evolizer.da4java.commands.selection.NopSelectionStrategy;
import org.evolizer.da4java.commands.selection.SelectEditedEntities;
import org.evolizer.da4java.commands.selection.SelectNode;
import org.evolizer.da4java.graph.data.EdgeGrouper;
import org.evolizer.da4java.graph.data.GraphLoader;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;

import y.base.Node;
import y.base.NodeCursor;

/**
 * Base class for filters of incoming/outgoing dependencies of a selected node.
 * 
 * @author Katja Graefenhain, Martin Pinzger
 */
public abstract class AbstractKeepDependencies extends AbstractGraphFilterCommand {
    
    /** The logger. */
//    private static Logger fLogger = DA4JavaPlugin.getLogManager().getLogger(AbstractKeepDependencies.class.getName());

    /** The selected node. */
    private Node fSelectedNode;
    
    /** The association type. */
    private java.lang.Class<? extends FamixAssociation> fAssociationType;

    /**
     * The constructor.
     * 
     * @param selectedNode the selected node
     * @param associationType the association type
     * @param graphLoader the graph loader
     * @param edgeGrouper the edge grouper
     */
    protected AbstractKeepDependencies(Node selectedNode, 
            java.lang.Class<? extends FamixAssociation> associationType, 
            GraphLoader graphLoader,
            EdgeGrouper edgeGrouper) {

        super(graphLoader, edgeGrouper);

        fSelectedNode = selectedNode;
        fAssociationType = associationType;
    }

    /**
     * Gets the selected node.
     * 
     * @return the selected node
     */
    Node getSelectedNode() {
        return fSelectedNode;
    }

    /**
     * Returns the association type.
     * 
     * @return the association type
     */
    java.lang.Class<? extends FamixAssociation> getAssociationType() {
        return fAssociationType;
    }

    /** 
     * {@inheritDoc}
     */
    public void execute() {
        setEditResult(new EditResult());
        fireGraphPreEvent();

        for (NodeCursor nc = getGraphLoader().getGraph().nodes(); nc.ok(); nc.next()) {
            getEdgeGrouper().reinsertLowLevelEdges(nc.node());
        }

        List<AbstractFamixEntity> entitiesToRemove = getEntitiesToRemove();
        List<FamixAssociation> associationsToRemove = getAssociationsToRemove();

        getEditResult().addAll(getGraphLoader().removeEntitiesAndAssociations(entitiesToRemove));
        getEditResult().addAll(getGraphLoader().removeAssociations(associationsToRemove));

        getEdgeGrouper().groupAll();

        fireGraphPostEvent();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected void initExecutionSelectionStrategy() {
        setPreLayoutSelectionStrategy(new NopSelectionStrategy(this));
        setPostLayoutSelectionStrategy(new NopSelectionStrategy(this));
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected void initUndoSelectionStrategy() {
        AbstractSelectionStrategy preLayoutSelection = new SelectEditedEntities(this);
        preLayoutSelection.initSelection();
        setPreLayoutSelectionStrategy(preLayoutSelection);

        AbstractSelectionStrategy postLayoutSelection = new SelectNode(this, fSelectedNode);
        postLayoutSelection.initSelection();
        setPostLayoutSelectionStrategy(postLayoutSelection);
    }

    /**
     * Determine entities with not dependency to the selected entity. They are the candidates
     * to remove from the graph.
     * 
     * @return FAMIX entities to remove from the graph.
     */
    protected List<AbstractFamixEntity> getEntitiesToRemove() {
        Set<AbstractFamixEntity> remainingEntities = new HashSet<AbstractFamixEntity>();

        AbstractFamixEntity entity = getGraphLoader().getGraph().getGraphModelMapper().getFamixEntity(getSelectedNode());

        List<AbstractFamixEntity> descendants = getGraphLoader().getSnapshotAnalyzer().getDescendants(entity);
        remainingEntities.addAll(descendants);

        List<AbstractFamixEntity> dependentEntities = getDependentEntities();
        remainingEntities.addAll(dependentEntities);

        for (AbstractFamixEntity remainingEntity : new HashSet<AbstractFamixEntity>(remainingEntities)) {
            remainingEntities.addAll(getGraphLoader().getSnapshotAnalyzer().getParentEntities(remainingEntity));
        }

        Set<AbstractFamixEntity> entitiesToRemove = new HashSet<AbstractFamixEntity>(getGraphLoader().getGraph().getGraphModelMapper().getAllFamixEntities());
        entitiesToRemove.removeAll(remainingEntities);

        return new ArrayList<AbstractFamixEntity>(entitiesToRemove);
    }

    /**
     * Determine the associations between the set of dependent entities and additional associations
     * (e.g., in the opposite direction). They are removed from the graph, as well.
     * 
     * @return The associations to remove from the graph.
     */
    protected List<FamixAssociation> getAssociationsToRemove() {
        List<FamixAssociation> associationsToRemove = new ArrayList<FamixAssociation>();

        List<AbstractFamixEntity> dependentEntities = getDependentEntities();
        associationsToRemove.addAll(getGraphLoader().getSnapshotAnalyzer().queryAssociationsBetweenEntities(dependentEntities, null));
        associationsToRemove.addAll(getAdditionalAssoctionsToRmove());

        return associationsToRemove;
    }

    /**
     * Entities that have an association with the selected entity or one of its descendant.
     * 
     * @return The list of dependent FAMIX entities.
     */
    protected abstract List<AbstractFamixEntity> getDependentEntities();

    /**
     * Additional associations to remove from the graph.
     * 
     * @return The list of associations to remove.
     */
    protected abstract List<FamixAssociation> getAdditionalAssoctionsToRmove();
}
