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
import java.util.Set;

import org.evolizer.da4java.commands.EditResult;
import org.evolizer.da4java.graph.data.EdgeGrouper;
import org.evolizer.da4java.graph.data.GraphLoader;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;

/**
 * Filter entities that are not connected with any other entity in the graph.
 * 
 * @author pinzger
 */
public class FilterNotConnectedNodes extends AbstractGraphFilterCommand {

    /**
     * The constructor.
     * 
     * @param graphLoader The graph loader
     * @param edgeGrouper The edge grouper
     */
    public FilterNotConnectedNodes(GraphLoader graphLoader, EdgeGrouper edgeGrouper) {
        super(graphLoader, edgeGrouper);
    }

    /** 
     * {@inheritDoc}
     */
    public void execute() {
        setEditResult(new EditResult());

        Set<AbstractFamixEntity> entitiesToRemove = getEntitiesToRemove();
        if (!entitiesToRemove.isEmpty()) {
            fireGraphPreEvent();

            getEditResult().addAll(getGraphLoader().removeEntitiesAndAssociations(new ArrayList<AbstractFamixEntity>(entitiesToRemove)));

            fireGraphPostEvent();
        }
    }

    /**
     * Return the entities to remove (entities that are do not have a dependency to other
     * entities or are not a parent of a dependent entity. 
     * 
     * @return The entities to remove
     */
    protected Set<AbstractFamixEntity> getEntitiesToRemove() {
        Set<AbstractFamixEntity> connectedEntities = new HashSet<AbstractFamixEntity>();

        for (FamixAssociation association : getGraphLoader().getGraph().getGraphModelMapper().getAllAssociations()) {
            if (!connectedEntities.contains(association.getFrom())) {
                connectedEntities.add(association.getFrom());
            }
            if (!connectedEntities.contains(association.getTo())) {
                connectedEntities.add(association.getTo());
            }
        }

        Set<AbstractFamixEntity> remainingEntities = new HashSet<AbstractFamixEntity>(); 
        for (AbstractFamixEntity entity : connectedEntities) {
            if (!remainingEntities.contains(entity)) {
                remainingEntities.add(entity);
                remainingEntities.addAll(getGraphLoader().getSnapshotAnalyzer().getParentEntities(entity));
            }
        }

        Set<AbstractFamixEntity> entitiesToRemove = new HashSet<AbstractFamixEntity>(getGraphLoader().getGraph().getGraphModelMapper().getAllFamixEntities());
        entitiesToRemove.removeAll(remainingEntities);

        return entitiesToRemove;

    }

    /** 
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Filter not connected nodes";
    }
}
