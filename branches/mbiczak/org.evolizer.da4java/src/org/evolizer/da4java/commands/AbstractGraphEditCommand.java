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
package org.evolizer.da4java.commands;

import java.util.List;

import org.evolizer.da4java.commands.selection.AbstractSelectionStrategy;
import org.evolizer.da4java.commands.selection.NopSelectionStrategy;
import org.evolizer.da4java.graph.data.EdgeGrouper;
import org.evolizer.da4java.graph.data.GraphLoader;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;

/**
 * Abstract graph edit class for adding entities and associations to the graph or
 * removing them from the graph. The result of each graph edit command (i.e. the
 * added/removed entities and associations) is tracked and used to <code>undo</code>
 * and <code>redo</code> commands.
 * 
 * @author Martin Pinzger
 */
public abstract class AbstractGraphEditCommand implements IGraphEditCommand {
    
    /** The logger instance. */
//    private final static Logger fLogger = DA4JavaPlugin.getLogManager().getLogger(AbstractGraphEditCommand.class.getName()); 

    /**
     * Reference to the edge grouper of the edited graph.
     */
    private EdgeGrouper fEdgeGrouper;
    
    /** Reference to the graph loader of the edited graph. */
    private GraphLoader fGraphLoader;
    
    /** The edit results (list of added/removed FAMIX entities and associations). */
    private EditResult fEditResult;

    /** The selection strategy before the layout. */
    private AbstractSelectionStrategy fPreLayoutSelectionStrategy;
    
    /** The selection strategy after the layout. */
    private AbstractSelectionStrategy fPostLayoutSelectionStrategy;

    /**
     * The constructor.
     * 
     * @param graphLoader   Reference to the graph loader of the edited graph.
     * @param edgeGrouper   Reference to the edge grouper of the edited graph.
     */
    public AbstractGraphEditCommand(GraphLoader graphLoader, EdgeGrouper edgeGrouper) {
        fGraphLoader = graphLoader;
        fEdgeGrouper = edgeGrouper;
    }

    /**
 * Return the reference to the edge grouper.
 * 
 * @return The edge grouper.
 */
    public EdgeGrouper getEdgeGrouper() {
        return fEdgeGrouper;
    }

    /**
     * Return the reference to the graph loader.
     * 
     * @return  The graph loader.
     */
    public GraphLoader getGraphLoader() {
        return fGraphLoader;
    }

    /**
     * Return the current edit result.
     * 
     * @return  The edit result.
     */
    public EditResult getEditResult() {
        return fEditResult;
    }

    /**
     * Sets the edit result.
     * 
     * @param editResult The new edit result
     */
    protected void setEditResult(EditResult editResult) {
        this.fEditResult = editResult;
    }
    
    /**
     * Return the list of added/removed FAMIX entities.
     * 
     * @return  The added/removed FAMIX entities.
     */
    public List<AbstractFamixEntity> getEditedEntities() {
        return fEditResult.getEntities();
    }

    /**
     * Return the list of added/removed FAMIX associations.
     * 
     * @return  The added/removed FAMIX associations.
     */
    public List<FamixAssociation> getEditedAssociations() {
        return fEditResult.getAssociations();
    }

    /**
     * Return the selection strategy to be applied before the layout.
     * 
     * @return  The pre-layout selection strategy.
     */
    public AbstractSelectionStrategy getPreLayoutSelectionStrategy() {
        return fPreLayoutSelectionStrategy;
    }

    /**
     * Set the selection strategy to be applied before the layout.
     * 
     * @param selectionStrategy The pre-layout selection strategy.
     */
    protected void setPreLayoutSelectionStrategy(AbstractSelectionStrategy selectionStrategy) {
        fPreLayoutSelectionStrategy = selectionStrategy;
    }

    /**
     * Return the selection strategy to be applied after the layout.
     * 
     * @return The post-layout selection strategy.
     */
    public AbstractSelectionStrategy getPostLayoutSelectionStrategy() {
        return fPostLayoutSelectionStrategy;
    }

    /**
     * Set the selection strategy to be applied after the layout.
     * 
     * @param selectionStrategy The post-layout selection strategy.
     */
    protected void setPostLayoutSelectionStrategy(AbstractSelectionStrategy selectionStrategy) {
        this.fPostLayoutSelectionStrategy = selectionStrategy;
    }

    /**
     * Default selection strategy when executing a command, that is,
     * keep the current selection.
     */
    protected void initExecutionSelectionStrategy() {
        setPreLayoutSelectionStrategy(new NopSelectionStrategy(this));
        setPostLayoutSelectionStrategy(new NopSelectionStrategy(this));
    }

    /**
     * Default strategy when undoing a command, that is,
     * keep the current selection.
     */
    protected void initUndoSelectionStrategy() {
        setPreLayoutSelectionStrategy(new NopSelectionStrategy(this));
        setPostLayoutSelectionStrategy(new NopSelectionStrategy(this));
    }

    /**
     * Default strategy when redoing a command, that is,
     * keep the current selection.
     */
    protected void initRedoSelectionStrategy() {
        setPreLayoutSelectionStrategy(new NopSelectionStrategy(this));
        setPostLayoutSelectionStrategy(new NopSelectionStrategy(this));
    }

    /**
     * Convenience method to fire a graph PRE event including the
     * reference to the edit command.
     */
    protected void fireGraphPreEvent() {
        fGraphLoader.getGraph().firePreEvent(this);
    }

    /**
     * Convenience method to fire a graph POST event inclugin the
     * reference to the edit command.
     */
    protected void fireGraphPostEvent() {
        fGraphLoader.getGraph().firePostEvent(this);
    }
}
