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

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;

/**
 * Select the given node and all its incoming/outgoing edges.
 * 
 * @author pinzger
 */
public class SelectNodeAndItsEdges extends AbstractSelectionStrategy {

    /** The folder node. */
    private Node fFolderNode;

    /**
     * The Constructor.
     * 
     * @param command The command
     * @param folderNode The folder node
     */
    public SelectNodeAndItsEdges(AbstractGraphEditCommand command, Node folderNode) {
        super(command);
        fFolderNode = folderNode;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initSelection() {
        Set<Node> nodesToSelect = new HashSet<Node>(); 
        Set<Edge> edgesToSelect = new HashSet<Edge>();

        nodesToSelect.add(fFolderNode);
        for (EdgeCursor ec = fFolderNode.edges(); ec.ok(); ec.next()) {
            edgesToSelect.add(ec.edge());
        }

        setNodesToSelect(nodesToSelect);
        setEdgesToSelect(edgesToSelect);
    }
}
