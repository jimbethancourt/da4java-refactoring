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

import java.util.List;

import org.evolizer.da4java.commands.EditResult;
import org.evolizer.da4java.graph.data.EdgeGrouper;
import org.evolizer.da4java.graph.data.GraphLoader;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;

import y.base.Node;

/**
 * Filters the edges/associations of a given type between the given nodes
 * from the graph.
 * 
 * @author Martin Pinzger, mark
 */
public class FilterDependenciesBetweenNodes extends AbstractGraphFilterCommand {
    
    /** The selected nodes. */
    private List<Node> fSelectedNodes;
    
    /** The association type. */
    private java.lang.Class<? extends FamixAssociation> fAssociationType;

    /**
     * The constructor.
     * 
     * @param selectedNodes The list of selected nodes
     * @param associationType The association type
     * @param graphLoader The graph loader
     * @param edgeGrouper The edge grouper
     */
    public FilterDependenciesBetweenNodes(List<Node> selectedNodes, 
            java.lang.Class<? extends FamixAssociation> associationType, 
            GraphLoader graphLoader,
            EdgeGrouper edgeGrouper) {

        super(graphLoader, edgeGrouper);
        fSelectedNodes = selectedNodes;
        fAssociationType = associationType;
    }

    /**
     * Returns the association type.
     * 
     * @return the association type
     */
    public java.lang.Class<? extends FamixAssociation> getAssociationType() {
        return fAssociationType;
    }

    /** 
     * {@inheritDoc}
     */
    public void execute() {
        setEditResult(new EditResult());
        List<AbstractFamixEntity> entities = getGraphLoader().getGraph().getFamixEntities(fSelectedNodes);
        if (!entities.isEmpty()) {
            fireGraphPreEvent();

            initExecutionSelectionStrategy();

            for (Node node : fSelectedNodes) {
                getEdgeGrouper().reinsertLowLevelEdges(node);
            }

            List<? extends FamixAssociation> associationsToRemove = getGraphLoader().getSnapshotAnalyzer().getAssociationsBetweenParentEntities(entities, fAssociationType);
            getEditResult().addAll(getGraphLoader().removeAssociations(associationsToRemove));

            getEdgeGrouper().groupAll();

            fireGraphPostEvent();
        }
    }

    /** 
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Filter dependencies between the selected nodes";
    }
}
