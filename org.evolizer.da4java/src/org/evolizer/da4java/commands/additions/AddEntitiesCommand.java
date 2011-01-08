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

import java.util.List;

import org.evolizer.da4java.commands.EditResult;
import org.evolizer.da4java.commands.selection.AbstractSelectionStrategy;
import org.evolizer.da4java.commands.selection.NopSelectionStrategy;
import org.evolizer.da4java.commands.selection.SelectFamixEntities;
import org.evolizer.da4java.graph.data.DependencyGraph;
import org.evolizer.da4java.graph.data.EdgeGrouper;
import org.evolizer.da4java.graph.data.GraphLoader;
import org.evolizer.famix.model.entities.AbstractFamixEntity;

import y.base.Node;
import y.base.NodeCursor;
import y.view.hierarchy.GroupNodeRealizer;

/**
 * Add the given FAMIX entities and associations between them and associations to other entities
 * currently contained by the graph.
 * 
 * @author Martin Pinzger
 */
public class AddEntitiesCommand extends AbstractGraphAddCommand {
    
    /** The entities. */
    private List<AbstractFamixEntity> fEntities;

    /**
     * The constructor.
     * 
     * @param entities The entities to add.
     * @param graphLoader The graph loader.
     * @param edgeGrouper The edge grouper.
     */
    public AddEntitiesCommand(List<AbstractFamixEntity> entities, GraphLoader graphLoader, EdgeGrouper edgeGrouper) {
        super(graphLoader, edgeGrouper);
        this.fEntities = entities;
    }

    /** 
     * {@inheritDoc}
     */
    public void execute() {
        setEditResult(new EditResult());

        if (!fEntities.isEmpty()) {
            DependencyGraph graph = getGraphLoader().getGraph();
            fireGraphPreEvent();

            for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
                getEdgeGrouper().reinsertLowLevelEdges(nc.node());
            }

            getEditResult().addAll(getGraphLoader().addEntitiesAndAssociations(fEntities));
            getEdgeGrouper().groupAll();

            initExecutionSelectionStrategy();

            fireGraphPostEvent();
        }
    }

    /**
     * Open up selected, added entities.
     */
    private void expandToEntities() {
        DependencyGraph graph = getGraphLoader().getGraph();
        for (AbstractFamixEntity entity : fEntities) {
            List<AbstractFamixEntity> parentEntities = getGraphLoader().getSnapshotAnalyzer().getParentEntities(entity);
            for (int i = parentEntities.size() - 1; i >= 0; i--) {
                Node parentNode = graph.getNode(parentEntities.get(i));
                if (parentNode != null) {
                    GroupNodeRealizer gr = (GroupNodeRealizer) graph.getRealizer(parentNode);
                    if (gr.isGroupClosed()) {
                        getEdgeGrouper().handleOpenFolder(parentNode);
                    }
                }
            }
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected void initExecutionSelectionStrategy() {
        expandToEntities();

        AbstractSelectionStrategy preLayoutSelection = new SelectFamixEntities(this, fEntities);
        preLayoutSelection.initSelection();
        setPreLayoutSelectionStrategy(preLayoutSelection);


        setPostLayoutSelectionStrategy(new NopSelectionStrategy(this));
    }

    /** 
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Add the given FAMIX entities";
    }
}
