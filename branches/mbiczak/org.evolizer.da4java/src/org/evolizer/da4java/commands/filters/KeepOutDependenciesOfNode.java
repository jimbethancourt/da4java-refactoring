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

import org.evolizer.da4java.graph.data.EdgeGrouper;
import org.evolizer.da4java.graph.data.GraphLoader;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.AbstractFamixEntity;

import y.base.Edge;
import y.base.Node;

/**
 * Keep entities which have an outgoing dependency with the selected entity (i.e., node).
 * Only outgoing dependencies to these entities remain in the graph.
 * 
 * @author Katja Graefenhain, Martin Pinzger
 */
public class KeepOutDependenciesOfNode extends AbstractKeepDependencies {
    
    /**
     * The constructor.
     * 
     * @param selectedNode The selected node
     * @param associationType The association type
     * @param graphLoader The graph loader
     * @param edgeGrouper The edge grouper
     */
    public KeepOutDependenciesOfNode(Node selectedNode, 
            java.lang.Class<? extends FamixAssociation> associationType, 
            GraphLoader graphLoader, 
            EdgeGrouper edgeGrouper) {

        super(selectedNode, associationType, graphLoader, edgeGrouper);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected List<AbstractFamixEntity> getDependentEntities() {
        Set<AbstractFamixEntity> dependentEntities = new HashSet<AbstractFamixEntity>();

        AbstractFamixEntity entity = getGraphLoader().getGraph().getGraphModelMapper().getFamixEntity(getSelectedNode());
        List<AbstractFamixEntity> descendants = getGraphLoader().getSnapshotAnalyzer().getDescendants(entity);

        List<? extends FamixAssociation> associationsToOtherEntities = getGraphLoader().getSnapshotAnalyzer().queryAssociationsOfEntities(descendants, getAssociationType(), "from");
        for (FamixAssociation association : associationsToOtherEntities) {
            Edge edge = getGraphLoader().getGraph().getGraphModelMapper().getEdge(association);
            if (edge != null) {
                if (!dependentEntities.contains(association.getTo())) {
                    dependentEntities.add(association.getTo());
                }
            }
        }

        return new ArrayList<AbstractFamixEntity>(dependentEntities);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected List<FamixAssociation> getAdditionalAssoctionsToRmove() {
        AbstractFamixEntity entity = getGraphLoader().getGraph().getGraphModelMapper().getFamixEntity(getSelectedNode());
        List<AbstractFamixEntity> descendants = getGraphLoader().getSnapshotAnalyzer().getDescendants(entity);
        List<FamixAssociation> associationsToOtherEntities = getGraphLoader().getSnapshotAnalyzer().queryAssociationsOfEntities(descendants, null, "to");

        return associationsToOtherEntities;
    }

    /** 
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Keep outgoing dependencies of selected node";
    }
}
