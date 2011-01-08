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
import org.evolizer.da4java.graph.data.GraphManager;

import y.base.Node;
import y.base.NodeCursor;

/**
 * Select the folder node and its children.
 * 
 * @author pinzger
 */
public class SelectNodeAndInnerNodes extends AbstractSelectionStrategy {
    
    /** The folder node. */
    private Node fFolderNode;

    /**
     * The Constructor.
     * 
     * @param command The command
     * @param folderNode The folder node
     */
    public SelectNodeAndInnerNodes(AbstractGraphEditCommand command, Node folderNode) {
        super(command);
        fFolderNode = folderNode;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initSelection() {
        Set<Node> nodesToSelect = new HashSet<Node>();

        GraphManager graph = getCommand().getGraphLoader().getGraph();

        nodesToSelect.add(fFolderNode);
        for (NodeCursor nc = graph.getHierarchyManager().getChildren(fFolderNode); nc.ok(); nc.next()) {
            nodesToSelect.add(nc.node());

        }

        setNodesToSelect(nodesToSelect);
    }
}
