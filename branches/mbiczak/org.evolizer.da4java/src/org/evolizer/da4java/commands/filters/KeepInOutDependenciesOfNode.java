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
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;

import y.base.Edge;
import y.base.Node;

/**
 * Keep entities which have an incoming or outgoing dependency with the selected entity (i.e., node).
 * Associations of any type between dependent entities are removed, as well.
 * 
 * @author Katja Graefenhain, Martin Pinzger
 */
public class KeepInOutDependenciesOfNode extends AbstractKeepDependencies {
    
    /**
     * The constructor.
     * 
     * @param selectedNode The selected node
     * @param associationType The association type
     * @param graphLoader The graph loader
     * @param edgeGrouper The edge grouper
     */
    public KeepInOutDependenciesOfNode(Node selectedNode, 
            java.lang.Class<? extends FamixAssociation> associationType, 
            GraphLoader graphLoader, 
            EdgeGrouper edgeGrouper) {

        super(selectedNode, associationType, graphLoader, edgeGrouper);
    }

    /** 
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List<AbstractFamixEntity> getDependentEntities() {
        Set<AbstractFamixEntity> dependentEntities = new HashSet<AbstractFamixEntity>();

        AbstractFamixEntity entity = getGraphLoader().getGraph().getGraphModelMapper().getFamixEntity(getSelectedNode());
        List<AbstractFamixEntity> descendants = getGraphLoader().getSnapshotAnalyzer().getDescendants(entity);

        List<FamixAssociation> associationsToOtherEntities = (List<FamixAssociation>) getGraphLoader().getSnapshotAnalyzer().queryAssociationsOfEntities(descendants, getAssociationType(), "from");
        associationsToOtherEntities.addAll((List<FamixAssociation>) getGraphLoader().getSnapshotAnalyzer().queryAssociationsOfEntities(descendants, getAssociationType(), "to"));
        for (FamixAssociation association : associationsToOtherEntities) {
            Edge edge = getGraphLoader().getGraph().getGraphModelMapper().getEdge(association);
            if (edge != null) {
                AbstractFamixEntity dependentEntity = null;
                if (descendants.contains(association.getFrom())) {
                    dependentEntity = association.getTo();
                } else {
                    dependentEntity = association.getFrom();
                }
                if (!dependentEntities.contains(dependentEntity)) {
                    dependentEntities.add(dependentEntity);
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
        return new ArrayList<FamixAssociation>();
    }

    /** 
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Keep incoming/outgoing dependencies of selected node";
    }
}
