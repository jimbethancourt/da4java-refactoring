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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.evolizer.da4java.plugin.DA4JavaEditorInput;
import org.evolizer.da4java.plugin.DA4JavaGraphEditor;

/**
 * Job that loads the FAMIX entities given buy the selection,
 * opens a new graph window and displays these entities.
 * 
 * @author pinzger
 */
public class LoadAndShowGraphJob extends Job {

    /**
     * The Eclipse selection of entities to load and display.
     */
    private ISelection fSelection;

    /** The progess monitor. */
    private IProgressMonitor fMonitor;

    /**
     * The constructor
     * 
     * @param name the job name
     * @param selection the Eclipse selection
     */
    public LoadAndShowGraphJob(String name, ISelection selection) {
        super(name);
        this.fSelection = selection;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        fMonitor = monitor;
        fMonitor.beginTask("Read data and load graph", 100);

        /**
         * NOTE: The ReleaseSnapshot to load is determined by the release time stamp or, if no time stamp exist, 
         * by the creation time of the revision belonging to the release.
         * For the future, we assume that the release info is bundled with the project.
         */
        fMonitor.subTask("fetching snapshot data");
        final DA4JavaEditorInput editorInput = new DA4JavaEditorInput(fSelection);
        fMonitor.worked(15);

        if (fMonitor.isCanceled()) {
            System.out.println("Initializing Graph aborted.");
            return Status.CANCEL_STATUS;
        }

        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                // open editor
                fMonitor.subTask("opening editor");
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                fMonitor.worked(30);
                try {
                    // opens an Editor instance of the class DA4JavaGraphEditor 
                    // -> given by the String DA4JavaGraphEditor.DA4JAVA_GRAPH_EDITOR
                    page.openEditor(editorInput, DA4JavaGraphEditor.DA4JAVA_GRAPH_EDITOR);

                    fMonitor.worked(90);
                } catch (PartInitException e) {
                    e.printStackTrace();
                }
            }
        });
        if (fMonitor.isCanceled()) {
            return Status.CANCEL_STATUS;
        }
        fMonitor.done();
        return Status.OK_STATUS;
    }
}
