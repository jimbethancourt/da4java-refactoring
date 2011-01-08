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
import java.util.List;
import java.util.Set;

import org.evolizer.da4java.commands.AbstractGraphEditCommand;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;

/**
 * Select the given nodes. For each edge select its source and
 * target node. For each node also select its dependent nodes.
 * 
 * @author Martin Pinzger
 */
public class SelectNodesAffectedByRemove extends AbstractSelectionStrategy {
    
    /** The selection. */
    private List<Object> fSelection;

    /**
     * The Constructor.
     * 
     * @param command The command
     * @param selection The selection
     */
    public SelectNodesAffectedByRemove(AbstractGraphEditCommand command, List<Object> selection) {
        super(command);
        fSelection = selection;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initSelection() {
        Set<Node> nodesToSelect = new HashSet<Node>();
        for (Object selected : fSelection) {
            if (selected instanceof Edge) {
                Edge edge = (Edge) selected;
                nodesToSelect.add(edge.source());
                nodesToSelect.add(edge.target());
            } else if (selected instanceof Node) {
                Node node = (Node) selected;
                for (EdgeCursor ec = node.edges(); ec.ok(); ec.next()) {
                    Edge edge = ec.edge();
                    if (edge.source() != node) {
                        nodesToSelect.add(edge.source());
                    } else if (edge.target() != node) {
                        nodesToSelect.add(edge.target());
                    }
                }
            }
        }

        setNodesToSelect(nodesToSelect);
    }
}
