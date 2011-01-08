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

import org.evolizer.da4java.commands.selection.AbstractSelectionStrategy;
import org.evolizer.da4java.commands.selection.SelectEditedEntities;
import org.evolizer.da4java.commands.selection.SelectNode;
import org.evolizer.da4java.graph.data.EdgeGrouper;
import org.evolizer.da4java.graph.data.GraphLoader;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.AbstractFamixEntity;

import y.base.Node;
import y.base.NodeCursor;

/**
 * Analyzes all incoming dependencies for the selected element and adds dependent elements that are not yet in the graph.
 * 
 * @author Katja Graefenhain, Martin Pinzger
 */
public class AddEntitiesViaInDependenciesCommand extends AbstractGraphAddCommand {
    
    /** The selected node. */
    private Node fSelectedNode;
    
    /** The association type. */
    private java.lang.Class<? extends FamixAssociation> fAssociationType;

    /**
     * The constructor.
     * 
     * @param node The selected node
     * @param graphLoader The graph loader
     * @param edgeGrouper The edge grouper
     * @param associationType The association type
     */
    public AddEntitiesViaInDependenciesCommand(Node node, 
            GraphLoader graphLoader, 
            EdgeGrouper edgeGrouper, 
            java.lang.Class<? extends FamixAssociation> associationType) {

        super(graphLoader, edgeGrouper);
        fSelectedNode = node;
        fAssociationType = associationType;
    }

    /** 
     * {@inheritDoc}
     */
    public void execute() {
        fireGraphPreEvent();

        for (NodeCursor nc = getGraphLoader().getGraph().nodes(); nc.ok(); nc.next()) {
            getEdgeGrouper().reinsertLowLevelEdges(nc.node());
        }

        AbstractFamixEntity entity = getGraphLoader().getGraph().getFamixEntity(fSelectedNode);
        setEditResult(getGraphLoader().addDependentEntitiesAndAssociations(entity, fAssociationType, "to"));
        getEdgeGrouper().groupAll();

        initExecutionSelectionStrategy();

        fireGraphPostEvent();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected void initExecutionSelectionStrategy() {
        AbstractSelectionStrategy preLayoutSelection = new SelectEditedEntities(this);
        preLayoutSelection.initSelection();
        setPreLayoutSelectionStrategy(preLayoutSelection);

        AbstractSelectionStrategy postLayoutSelectionStrategy = new SelectNode(this, fSelectedNode);
        postLayoutSelectionStrategy.initSelection();
        setPostLayoutSelectionStrategy(postLayoutSelectionStrategy);
    }

    /** 
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Add entities via the incoming dependencies of the selected entity";
    }
}
