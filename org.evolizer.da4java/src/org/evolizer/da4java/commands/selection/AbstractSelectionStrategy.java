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
package org.evolizer.da4java.commands.selection;

import java.util.HashSet;
import java.util.Set;

import org.evolizer.da4java.commands.AbstractGraphEditCommand;
import org.evolizer.da4java.graph.data.DependencyGraph;

import y.base.Edge;
import y.base.Node;

/**
 * Abstract class for selection strategies of the various graph edit commands.
 * The selection strategies influence the graph layout if the layout is configured
 * to take the selection into account. Layout is also the main purpose of having
 * different selection strategies for the various add and filter commands.
 * 
 * @author Martin Pinzger
 */
public abstract class AbstractSelectionStrategy {
    
    /** The logger. */
//    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(AbstractSelectionStrategy.class.getName()); 

    /** The nodes to select. */
    private Set<Node> fNodesToSelect;
    
    /** The edges to select. */
    private Set<Edge> fEdgesToSelect;

    /** The command. */
    private AbstractGraphEditCommand fCommand;
    
    /**
     * The constructor.
     * 
     * @param command The graph edit command
     */
    public AbstractSelectionStrategy(AbstractGraphEditCommand command) {
        fCommand = command;

        setNodesToSelect(new HashSet<Node>());
        setEdgesToSelect(new HashSet<Edge>());
    }

    /**
     * Initializes the nodes and edges to selected.
     */
    public abstract void initSelection();

    /**
     * Update the selection.
     */
    public void updateSelection() {
        DependencyGraph graph = getCommand().getGraphLoader().getGraph();

        graph.unselectAll();
        for (Node node : getNodesToSelect()) {
            graph.setSelected(node, true);
        }
        for (Edge edge : getEdgesToSelect()) {
            graph.setSelected(edge, true);
        }
    }

    /**
     * Returns the graph edit command.
     * 
     * @return The command
     */
    protected AbstractGraphEditCommand getCommand() {
        return fCommand;
    }

    /**
     * Sets the nodes to select.
     * 
     * @param nodesToSelect The nodes
     */
    protected void setNodesToSelect(Set<Node> nodesToSelect) {
        this.fNodesToSelect = nodesToSelect;
    }

    /**
     * Returns the nodes to select.
     * 
     * @return the fNodesToSelect
     */
    protected Set<Node> getNodesToSelect() {
        return fNodesToSelect;
    }

    /**
     * Sets the edges to select.
     * 
     * @param edgesToSelect The edges
     */
    protected void setEdgesToSelect(Set<Edge> edgesToSelect) {
        this.fEdgesToSelect = edgesToSelect;
    }

    /**
     * Returns the edges to select.
     * 
     * @return The edges
     */
    protected Set<Edge> getEdgesToSelect() {
        return fEdgesToSelect;
    }
}
