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
import java.util.List;

import org.evolizer.da4java.commands.EditResult;
import org.evolizer.da4java.commands.selection.NopSelectionStrategy;
import org.evolizer.da4java.graph.data.EdgeGrouper;
import org.evolizer.da4java.graph.data.GraphLoader;
import org.evolizer.famix.model.entities.AbstractFamixEntity;

import y.base.Node;
import y.base.NodeCursor;

/**
 * The Class AddDescendantsAndDependencies.
 * 
 * @author pinzger
 */
public class AddDescendantsAndDependencies extends AbstractGraphAddCommand {
    
    /** The selected nodes. */
    private List<Node> fSelectedNodes;

    /**
     * Instantiates a new adds the descendants and dependencies.
     * 
     * @param nodes the nodes
     * @param graphLoader the graph loader
     * @param edgeGrouper the edge grouper
     */
    public AddDescendantsAndDependencies(List<Node> nodes, GraphLoader graphLoader, EdgeGrouper edgeGrouper) {
        super(graphLoader, edgeGrouper);
        fSelectedNodes = nodes;

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

            List<AbstractFamixEntity> selectedEntities = new ArrayList<AbstractFamixEntity>();
            for (Node node : fSelectedNodes) {
                AbstractFamixEntity entity = getGraphLoader().getGraph().getGraphModelMapper().getFamixEntity(node);
                if (entity != null) {
                    selectedEntities.add(entity);
                }
            }
            getEditResult().addAll(getGraphLoader().addEntitiesAndAssociations(selectedEntities));
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
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Add descendants of selected nodes and their dependencies";
    }
}
