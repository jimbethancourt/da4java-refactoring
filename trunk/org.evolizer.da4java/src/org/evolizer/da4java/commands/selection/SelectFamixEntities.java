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

import org.apache.log4j.Logger;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.da4java.commands.AbstractGraphEditCommand;
import org.evolizer.da4java.graph.data.GraphManager;
import org.evolizer.famix.model.entities.AbstractFamixEntity;

import y.base.Node;

/**
 * Select the given FAMIX entities.
 * 
 * @author pinzger
 */
public class SelectFamixEntities extends AbstractSelectionStrategy {
    /** The logger. */
    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(SelectFamixEntities.class.getName()); 
    
    /** The entities. */
    private List<AbstractFamixEntity> fEntities;

    /**
     * The Constructor.
     * 
     * @param command The command
     * @param entities The FAMIX entities
     */
    public SelectFamixEntities(AbstractGraphEditCommand command, List<AbstractFamixEntity> entities) {
        super(command);
        fEntities = entities;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initSelection() {
        Set<Node> nodesToSelect = new HashSet<Node>();
        GraphManager graph = getCommand().getGraphLoader().getGraph(); 

        for (AbstractFamixEntity entity : fEntities) {
            Node node = graph.getGraphModelMapper().getNode(entity);
            if (node != null) {
                nodesToSelect.add(node);
            } else {
                sLogger.error("Could not determine graph node of FAMIX entity " + entity.getUniqueName());
            }
        }

        setNodesToSelect(nodesToSelect);
    }
}
