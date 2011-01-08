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
import java.util.List;

import org.evolizer.da4java.commands.EditResult;
import org.evolizer.da4java.commands.selection.AbstractSelectionStrategy;
import org.evolizer.da4java.commands.selection.SelectNodesAffectedByRemove;
import org.evolizer.da4java.commands.selection.UnselectAllStrategy;
import org.evolizer.da4java.graph.data.EdgeGrouper;
import org.evolizer.da4java.graph.data.GraphLoader;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;

import y.base.Edge;
import y.base.Node;
import y.base.NodeCursor;

/**
 * Filter the selected entities and associations.
 * 
 * @author Katja Graefenhain, Martin Pinzger
 */
public class FilterSelectedEntities extends AbstractGraphFilterCommand {
    
    /** The logger. */
//    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(FilterSelectedEntities.class.getName());

    /** The selection. */
    private List<Object> fSelection;

    /**
     * The constructor
     * 
     * @param selection The list of selected graph entities.
     * @param graphLoader The graph loader
     * @param edgeGrouper The edge grouper
     */
    public FilterSelectedEntities(List<Object> selection, GraphLoader graphLoader, EdgeGrouper edgeGrouper) {
        super(graphLoader, edgeGrouper);
        fSelection = selection;
    }

    /** 
     * {@inheritDoc}
     */
    public void execute() {
        setEditResult(new EditResult());
        if (!fSelection.isEmpty()) {
            fireGraphPreEvent();

            initExecutionSelectionStrategy();

            List<FamixAssociation> associationsToRemove = getGraphLoader().getGraph().getGraphModelMapper().getFamixAssociations(getEdgesFromSelection());
            List<AbstractFamixEntity> entitiesToRemove = getGraphLoader().getGraph().getGraphModelMapper().getFamixEntities(getNodesFromSelection());

            for (NodeCursor nc = getGraphLoader().getGraph().nodes(); nc.ok(); nc.next()) {
                getEdgeGrouper().reinsertLowLevelEdges(nc.node());
            }

            getEditResult().addAll(getGraphLoader().removeEntitiesAndAssociations(entitiesToRemove));
            getEditResult().addAll(getGraphLoader().removeAssociations(associationsToRemove));

            getEdgeGrouper().groupAll();

            fireGraphPostEvent();
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected void initExecutionSelectionStrategy() {
        AbstractSelectionStrategy preProcessSelection = new SelectNodesAffectedByRemove(this, fSelection);
        preProcessSelection.initSelection();
        setPreLayoutSelectionStrategy(preProcessSelection);

        setPostLayoutSelectionStrategy(new UnselectAllStrategy(this));
    }

    /** 
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Filter selected entities";
    }

    /**
     * Return the selected nodes.
     * 
     * @return The nodes
     */
    private List<Node> getNodesFromSelection() {
        List<Node> nodes = new ArrayList<Node>();
        for (Object entity : fSelection) {
            if (entity instanceof Node) {
                nodes.add((Node) entity);
            }
        }

        return nodes;
    }

    /**
     * Return the selected edges.
     * 
     * @return The edges
     */
    private List<Edge> getEdgesFromSelection() {
        List<Edge> edges = new ArrayList<Edge>();
        for (Object entity : fSelection) {
            if (entity instanceof Edge) {
                edges.add((Edge) entity);
            }
        }

        return edges;
    }
}
