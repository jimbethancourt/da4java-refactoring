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

import org.evolizer.da4java.commands.selection.AbstractSelectionStrategy;
import org.evolizer.da4java.commands.selection.SelectNode;
import org.evolizer.da4java.commands.selection.SelectNodeAndInnerNodes;
import org.evolizer.da4java.commands.selection.SelectNodeAndItsEdges;
import org.evolizer.da4java.graph.data.EdgeGrouper;
import org.evolizer.da4java.graph.data.GraphLoader;
import org.evolizer.famix.model.entities.AbstractFamixEntity;

import y.base.Node;

/**
 * Handle open and close of folder nodes. When a folder node is opened (expanded)
 * its dependencies are expanded as well. When a folder node is closed (collapsed)
 * its dependencies are aggregated to higher-level dependencies.
 * 
 * @author pinzger
 */
public class FolderNodeHandleCommand extends AbstractGraphEditCommand {
    
    /** The selected entity. */
    private AbstractFamixEntity fSelectedEntity;

    /** True, if a folder is to opened, otherwise the folder is closed. */
    private boolean fIsOpenCommand;

    /**
     * The constructor.
     * 
     * @param graphLoader  The graph loader.
     * @param edgeGrouper    The edge grouper.
     * @param node The selected folder node.
     * @param isOpenCommand    True, if the older is opened, false, if it is closed.
     */
    public FolderNodeHandleCommand(GraphLoader graphLoader, EdgeGrouper edgeGrouper, Node node, boolean isOpenCommand) {
        super(graphLoader, edgeGrouper);
//        fSelectedNode = node;
        fSelectedEntity = graphLoader.getGraph().getFamixEntity(node);
        this.fIsOpenCommand = isOpenCommand;
    }

    /** 
     * {@inheritDoc}
     */
    public void execute() {
        Node selectedNode = getGraphLoader().getGraph().getNode(fSelectedEntity);
        if (fIsOpenCommand) {
            openFolder(selectedNode);
        } else {
            closeGroup(selectedNode);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void redo() {
        execute();
    }

    /** 
     * {@inheritDoc}
     */
    public void undo() {
        Node selectedNode = getGraphLoader().getGraph().getNode(fSelectedEntity);
        if (fIsOpenCommand) {
            closeGroup(selectedNode);
        } else {
            openFolder(selectedNode);
        }
    }

    /**
     * Open folder.
     * 
     * @param folderNode the folder node
     */
    private void openFolder(Node folderNode) {
        fireGraphPreEvent();
        getEdgeGrouper().handleOpenFolder(folderNode);

        AbstractSelectionStrategy preProcessSelection = new SelectNodeAndInnerNodes(this, folderNode);
        preProcessSelection.initSelection();
        setPreLayoutSelectionStrategy(preProcessSelection);

        AbstractSelectionStrategy postProcessSelection = new SelectNode(this, folderNode);
        postProcessSelection.initSelection();
        setPostLayoutSelectionStrategy(postProcessSelection);

        fireGraphPostEvent();
    }

    /**
     * Close group.
     * 
     * @param folderNode the folder node
     */
    private void closeGroup(Node folderNode) {
        fireGraphPreEvent();
        getEdgeGrouper().handleCloseFolder(folderNode);

        AbstractSelectionStrategy preLayoutSelection = new SelectNodeAndItsEdges(this, folderNode);
        preLayoutSelection.initSelection();
        setPreLayoutSelectionStrategy(preLayoutSelection);

        AbstractSelectionStrategy postLayoutSelection = new SelectNode(this, folderNode);
        postLayoutSelection.initSelection();
        setPostLayoutSelectionStrategy(postLayoutSelection);

        fireGraphPostEvent();
    }

    /** 
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Handle open/close of folder nodes";
    }

}
