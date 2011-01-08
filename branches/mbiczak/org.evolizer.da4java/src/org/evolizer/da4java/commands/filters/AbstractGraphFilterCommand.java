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

import org.evolizer.da4java.commands.AbstractGraphEditCommand;
import org.evolizer.da4java.commands.selection.AbstractSelectionStrategy;
import org.evolizer.da4java.commands.selection.NopSelectionStrategy;
import org.evolizer.da4java.commands.selection.SelectEditedEntities;
import org.evolizer.da4java.graph.data.EdgeGrouper;
import org.evolizer.da4java.graph.data.GraphLoader;

import y.base.NodeCursor;

/**
 * The Class AbstractGraphFilterCommand.
 * 
 * @author Martin Pinzger
 */
public abstract class AbstractGraphFilterCommand extends AbstractGraphEditCommand {

    /**
     * The Constructor.
     * 
     * @param graphLoader the graph loader
     * @param edgeGrouper the edge grouper
     */
    public AbstractGraphFilterCommand(GraphLoader graphLoader,
            EdgeGrouper edgeGrouper) {
        super(graphLoader, edgeGrouper);
    }

    /** 
     * {@inheritDoc}
     */
    public void undo() {
        if (!getEditResult().isEmpty()) {
            fireGraphPreEvent();

            for (NodeCursor nc = getGraphLoader().getGraph().nodes(); nc.ok(); nc.next()) {
                getEdgeGrouper().reinsertLowLevelEdges(nc.node());
            }

            getGraphLoader().addEntitiesAndAssociations(getEditedEntities(), getEditedAssociations());

            getEdgeGrouper().groupAll();

            initUndoSelectionStrategy();

            fireGraphPostEvent();
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void redo() {
        if (!getEditResult().isEmpty()) {
            fireGraphPreEvent();

            for (NodeCursor nc = getGraphLoader().getGraph().nodes(); nc.ok(); nc.next()) {
                getEdgeGrouper().reinsertLowLevelEdges(nc.node());
            }

            getGraphLoader().removeEntitiesAndAssociations(getEditedEntities());
            getGraphLoader().removeAssociations(getEditedAssociations());

            getEdgeGrouper().groupAll();

            initRedoSelectionStrategy();

            fireGraphPostEvent();
        }
    }

    /**
     * Default strategy for executing a filter command - keep the current selection.
     */
    @Override
    protected void initExecutionSelectionStrategy() {
        setPreLayoutSelectionStrategy(new NopSelectionStrategy(this));
        setPostLayoutSelectionStrategy(new NopSelectionStrategy(this));
    }

    /**
     * Default selection strategy for redoing a filter command - the same as for executing a filter command.
     */
    @Override
    protected void initRedoSelectionStrategy() {
        initExecutionSelectionStrategy();
    }

    /**
     * Default strategy when undoing a filter command - select added nodes and edges.
     */
    @Override
    protected void initUndoSelectionStrategy() {
        AbstractSelectionStrategy preLayoutSelection = new SelectEditedEntities(this);
        preLayoutSelection.initSelection();
        setPreLayoutSelectionStrategy(preLayoutSelection);

        setPostLayoutSelectionStrategy(new NopSelectionStrategy(this));
    }
}
