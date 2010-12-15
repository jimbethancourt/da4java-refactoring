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
import org.evolizer.da4java.commands.EditResult;
import org.evolizer.da4java.graph.data.DependencyGraph;
import org.evolizer.famix.model.entities.AbstractFamixEntity;

import y.base.Node;

/**
 * Select the edited FAMIX entities.
 * 
 * @author Martin Pinzger
 */
public class SelectEditedEntities extends AbstractSelectionStrategy {

    /**
     * The Constructor.
     * 
     * @param command The command
     */
    public SelectEditedEntities(AbstractGraphEditCommand command) {
        super(command);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initSelection() {
        Set<Node> nodesToSelect = new HashSet<Node>();

        EditResult editResult = getCommand().getEditResult();
        DependencyGraph graph = getCommand().getGraphLoader().getGraph();

        if (!editResult.isEmpty()) {
            for (AbstractFamixEntity entity : editResult.getEntities()) {
                Node node = graph.getNode(entity);
                if (node != null) {
                    nodesToSelect.add(node);
                }
            }
        }

        setNodesToSelect(nodesToSelect);
    }
}
