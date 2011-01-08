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
package org.evolizer.da4java.plugin.actions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.evolizer.da4java.commands.AbstractGraphEditCommand;
import org.evolizer.da4java.commands.additions.AddEntitiesCommand;
import org.evolizer.da4java.plugin.DA4JavaGraphEditor;
import org.evolizer.da4java.plugin.selectionhandler.AbstractSelectionHandler;
import org.evolizer.da4java.plugin.selectionhandler.SelectionHandlerFactory;
import org.evolizer.famix.model.entities.AbstractFamixEntity;

/**
 * Job that adds the selected entities to the current graph.
 * 
 * @author pinzger
 */
public class AddSelectedEntitiesJob extends Job {
    
    /** The selection. */
    private ISelection fSelection;
    
    /** The monitor. */
    private IProgressMonitor fMonitor;

    /**
     * The constructor.
     * 
     * @param name the job name
     * @param selection the Eclipse selection
     */
    public AddSelectedEntitiesJob(String name, ISelection selection) {
        super(name);
        fSelection = selection;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        fMonitor = monitor;
        fMonitor.beginTask("Adding selected entities...", 30);

        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                // monitor.subTask("opening editor");
                IWorkbenchPage page = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage();

                IEditorPart editor = page.getActiveEditor();
                if (editor instanceof DA4JavaGraphEditor) {
                    DA4JavaGraphEditor graphEditor = (DA4JavaGraphEditor) editor;
                    fMonitor.worked(10);
                    // AbstractSelectionHandler selectionHandler = new JavaElementSelectionHandler(graphEditor.getPanel().getGraphLoader().getSnapshotAnalyzer());
                    // List<AbstractFamixEntity> entities = selectionHandler.getSelectedEntities(fSelection);
                    AbstractSelectionHandler selectionHandler = SelectionHandlerFactory.getInstance().getSelectionHandler(fSelection);
                    List<AbstractFamixEntity> entities = selectionHandler.getSelectedEntities(graphEditor.getPanel().getGraphLoader().getSnapshotAnalyzer());
                    AbstractGraphEditCommand command = new AddEntitiesCommand(entities, graphEditor.getPanel().getGraphLoader(), graphEditor.getPanel().getEdgeGrouper());
                    graphEditor.getPanel().getCommandController().executeCommand(command);

                    fMonitor.worked(25);
                } else {
                    System.err.println("Editor is not a graph editor - select an active graph editor first");
                }
            }
        });
        fMonitor.done();
        return Status.OK_STATUS;
    }

}
