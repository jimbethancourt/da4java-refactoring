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
package org.evolizer.da4java.commands.additions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evolizer.da4java.commands.EditResult;
import org.evolizer.da4java.commands.selection.NopSelectionStrategy;
import org.evolizer.da4java.graph.data.EdgeGrouper;
import org.evolizer.da4java.graph.data.GraphLoader;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;

import y.base.Node;
import y.base.NodeCursor;

/**
 * Command to add associations between selected entities and their descendants to the graph.
 * Missing descendant entities and their parent entities are added, as well.
 * 
 * @author Martin Pinzger
 */
public class AddDependenciesBetweenEntities extends AbstractGraphAddCommand {
    
    /** The selected nodes. */
    private List<Node> fSelectedNodes;
    
    /** The association type. */
    private java.lang.Class<? extends FamixAssociation> fAssociationType;

    /**
     * Instantiates a new adds the dependencies between entities.
     * 
     * @param selectedNodes the selected nodes
     * @param associationType the association type
     * @param graphLoader the graph loader
     * @param edgeGrouper the edge grouper
     */
    public AddDependenciesBetweenEntities(List<Node> selectedNodes, 
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
        if (!fSelectedNodes.isEmpty()) {
            fireGraphPreEvent();

            for (NodeCursor nc = getGraphLoader().getGraph().nodes(); nc.ok(); nc.next()) {
                getEdgeGrouper().reinsertLowLevelEdges(nc.node());
            }

            Set<AbstractFamixEntity> entitiesToAdd = getEntitiesToAdd();
            Set<FamixAssociation> associationsToAdd = getAssociationsToAdd();
            List<AbstractFamixEntity> addedEntities = getGraphLoader().addEntitiesAndParents(new ArrayList<AbstractFamixEntity>(entitiesToAdd), false);
            List<FamixAssociation> addedAssociations = getGraphLoader().addAssociations(new ArrayList<FamixAssociation>(associationsToAdd));
            getEditResult().addEntities(addedEntities);
            getEditResult().addAssociations(addedAssociations);
            getEdgeGrouper().groupAll();

            initExecutionSelectionStrategy();

            fireGraphPostEvent();
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initExecutionSelectionStrategy() {
        setPreLayoutSelectionStrategy(new NopSelectionStrategy(this));
        setPostLayoutSelectionStrategy(new NopSelectionStrategy(this));
    }

    /**
     * Gets the entities to add.
     * 
     * @return the entities to add
     */
    protected Set<AbstractFamixEntity> getEntitiesToAdd() {
        Set<AbstractFamixEntity> entitiesToAdd = new HashSet<AbstractFamixEntity>();

        List<AbstractFamixEntity> entities = getGraphLoader().getGraph().getFamixEntities(fSelectedNodes);

        List<? extends FamixAssociation> allAssociations = getGraphLoader().getSnapshotAnalyzer().getAssociationsBetweenParentEntities(entities, fAssociationType);
        entitiesToAdd.addAll(getGraphLoader().getListOfNotContainedEntities(allAssociations));

        return entitiesToAdd;
    }

    /**
     * Gets the associations to add.
     * 
     * @return the associations to add
     */
    protected Set<FamixAssociation> getAssociationsToAdd() {
        Set<FamixAssociation> associationsToAdd = new HashSet<FamixAssociation>();

        List<AbstractFamixEntity> entities = getGraphLoader().getGraph().getFamixEntities(fSelectedNodes);
        associationsToAdd.addAll(getGraphLoader().getSnapshotAnalyzer().getAssociationsBetweenParentEntities(entities, fAssociationType));

        return associationsToAdd;
    }

    /** 
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Add dependencies between selected entities";
    }
}
