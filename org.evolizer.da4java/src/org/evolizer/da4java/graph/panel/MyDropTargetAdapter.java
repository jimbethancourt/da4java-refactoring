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
package org.evolizer.da4java.graph.panel;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.da4java.commands.AbstractGraphEditCommand;
import org.evolizer.da4java.commands.additions.AddEntitiesCommand;
import org.evolizer.da4java.plugin.selectionhandler.AbstractSelectionHandler;
import org.evolizer.da4java.plugin.selectionhandler.SelectionHandlerFactory;
import org.evolizer.famix.model.entities.AbstractFamixEntity;

/**
 * Listener to <code>DropTargetEvents</code>. If an
 * <code>IStructuredSelection</code> is dropped to the
 * DependencyGraphEditor the corresponding subgraph is shown.
 * 
 * TODO: This class is not up-to-date because I did not test da4java on Windows
 * 
 * @author Katja Graefenhain
 */
public class MyDropTargetAdapter extends DropTargetAdapter {
    
    /** The logger. */
    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(MyDropTargetAdapter.class.getName());

    /** The graph panel. */
    private DA4JavaGraphPanel fGraphPanel;

    /**
     * The Constructor.
     * 
     * @param graphPanel the graph panel
     */
    public MyDropTargetAdapter(DA4JavaGraphPanel graphPanel) {
        fGraphPanel = graphPanel;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void drop(DropTargetEvent event) {
        if (LocalSelectionTransfer.getTransfer().isSupportedType(event.currentDataType)) {
            Assert.isTrue(event.data instanceof ISelection);
            ISelection selection = (ISelection) event.data;

            if (selection instanceof IStructuredSelection) {
                IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                // Check if current graph shows the same project. If so, load whole project.
                if (isDisplayedProject(structuredSelection)) {
                    sLogger.info("DRAG AND DROP: add selected elements to graph");
                    // add selected elements to graph
                    AbstractSelectionHandler selectionHandler = SelectionHandlerFactory.getInstance().getSelectionHandler(selection);
                    List<AbstractFamixEntity> entities = selectionHandler.getSelectedEntities(fGraphPanel.getGraphLoader().getSnapshotAnalyzer());
                    AbstractGraphEditCommand command = new AddEntitiesCommand(entities, fGraphPanel.getGraphLoader(), fGraphPanel.getEdgeGrouper());
                    fGraphPanel.getCommandController().executeCommand(command);
                } else {
                    final String message = "Unable add elements to the current graph!";
                    final IWorkbench workbench = PlatformUI.getWorkbench();
                    Display display = workbench.getDisplay();
                    display.syncExec(new Runnable() {
                        public void run() {
                            IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
                            Shell shell = activeWorkbenchWindow.getShell();
                            ErrorDialog.openError(shell, "Error", message, new Status(IStatus.ERROR, DA4JavaPlugin.PLUGIN_ID, 1, 
                                    "The selected elements don't belong to the currently visualized project.", null));
                        }
                    });

                    System.err.println("Cannot add elements. Draw new dependency graph.");
                }
            }
        }
    }

    /**
     * Returns a list of <code>Transfer</code> agents that are supported
     * by this listener.
     * 
     * @return the list of transfer agents supported by this listener
     */
    public Transfer[] getTransfers() {
        return new Transfer[] { LocalSelectionTransfer.getTransfer() };
    }

    /**
     * Currently not implemented.
     * 
     * @param structuredSelection the structured selection
     * 
     * @return true, if checks if is displayed project
     */
    private boolean isDisplayedProject(IStructuredSelection structuredSelection) {
        // IProject displayed = fGraphPanel.getGraphLoader().getSnapshotAnalyzer().getProject();
        // IProject selected = getSelectedProject(structuredSelection);
        // if (selected.getName().equals(displayed.getName())) {
        // return true;
        // }
        return false;
    }

    /**
     * Gets the selected project.
     * 
     * @param selection the selection
     * 
     * @return the selected project
     */
//    private IProject getSelectedProject(IStructuredSelection selection) {
//        // select project from first selected element
//        return ((IJavaElement) selection.getFirstElement()).getJavaProject().getProject();
//    }
}
