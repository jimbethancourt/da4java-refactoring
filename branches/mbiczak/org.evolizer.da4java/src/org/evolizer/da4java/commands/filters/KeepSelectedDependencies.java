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
import y.base.NodeCursor;

/**
 * Keep the selected associations and corresponding source and target entities. All other
 * associations and entities are removed from the graph.
 * 
 * @author pinzger
 */
public class KeepSelectedDependencies extends AbstractGraphFilterCommand {
    
    /** The selected edges. */
    private List<Edge> fSelectedEdges;
    
    /**
     * The default constructor.
     * 
     * @param selectedEdges The list of selected edges
     * @param graphLoader   The graph loader instance
     * @param edgeGrouper   The edge grouper instance
     */
    public KeepSelectedDependencies(List<Edge> selectedEdges,
            GraphLoader graphLoader,
            EdgeGrouper edgeGrouper) {

        super(graphLoader, edgeGrouper);
        fSelectedEdges = selectedEdges;
    }

    /** 
     * {@inheritDoc}
     */
    public void execute() {
        setEditResult(new EditResult());
        if (!fSelectedEdges.isEmpty()) {
            fireGraphPreEvent();
            
            Set<AbstractFamixEntity> entitiesToRemove = getEntitiesToRemove();
            Set<FamixAssociation> associationsToRemove = getAssociationsToRemove();
            
            for (NodeCursor nc = getGraphLoader().getGraph().nodes(); nc.ok(); nc.next()) {
                getEdgeGrouper().reinsertLowLevelEdges(nc.node());
            }
            
            getEditResult().addAll(getGraphLoader().removeEntitiesAndAssociations(new ArrayList<AbstractFamixEntity>(entitiesToRemove)));
            getEditResult().addAll(getGraphLoader().removeAssociations(new ArrayList<FamixAssociation>(associationsToRemove)));
            
            getEdgeGrouper().groupAll();

            fireGraphPostEvent();
        }
    }
    
    /**
     * Keep entities that are involved in the set of selected dependencies. This includes
     * entities directly involved and their parent entities.
     * 
     * @return The set of FAMIX entities to remove from the graph.
     */
    protected Set<AbstractFamixEntity> getEntitiesToRemove() {
        List<FamixAssociation> associations = getGraphLoader().getGraph().getGraphModelMapper().getFamixAssociations(fSelectedEdges);
        Set<AbstractFamixEntity> involvedEntities = new HashSet<AbstractFamixEntity>();
        for (FamixAssociation association : associations) {
            involvedEntities.add(association.getFrom());
            involvedEntities.add(association.getTo());
        }
        
        Set<AbstractFamixEntity> remainingEntities = new HashSet<AbstractFamixEntity>();
        for (AbstractFamixEntity entity : involvedEntities) {
            if (!remainingEntities.contains(entity)) {
                remainingEntities.addAll(getGraphLoader().getSnapshotAnalyzer().getParentEntities(entity));
            }
        }
        remainingEntities.addAll(involvedEntities);
        
        Set<AbstractFamixEntity> entitiesToRemove = new HashSet<AbstractFamixEntity>(getGraphLoader().getGraph().getGraphModelMapper().getAllFamixEntities());
        entitiesToRemove.removeAll(remainingEntities);
        
        return entitiesToRemove;
    }
    
    /**
     * Remove all dependencies except the selected ones. Also inner associations
     * contained by involved parent entities are removed.
     * 
     * @return The set of FAMIX association to remove from the graph.
     */
    protected Set<FamixAssociation> getAssociationsToRemove() {
        Set<FamixAssociation> associationsToRemove = new HashSet<FamixAssociation>(getGraphLoader().getGraph().getGraphModelMapper().getAllAssociations());
        associationsToRemove.removeAll(getGraphLoader().getGraph().getGraphModelMapper().getFamixAssociations(fSelectedEdges));
        
        return associationsToRemove;
    }
    
    /** 
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Keep selected dependencies and corresponding entities";
    }
}
