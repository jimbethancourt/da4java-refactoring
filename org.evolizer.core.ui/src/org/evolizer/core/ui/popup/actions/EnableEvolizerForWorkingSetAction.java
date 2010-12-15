/*
 * Copyright 2009 University of Zurich, Switzerland
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
package org.evolizer.core.ui.popup.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;

/**
 * Action that invokes a wizard and enables Evolizer for the projects in the selected working set by importing the
 * versioning model from e.g. cvs.
 * 
 * @author wuersch
 */
// TODO: untested and unfinished.
public class EnableEvolizerForWorkingSetAction implements IObjectActionDelegate {

    private IStructuredSelection fSelection;

    // private Logger logger = Activator.getLogManager().getLogger(EnableEvolizerForWorkingSetAction.class.getName());

    /**
     * Constructor for 'Enable Evolizer for Working Set'-Action.
     */
    public EnableEvolizerForWorkingSetAction() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

    /**
     * {@inheritDoc}
     */
    public void run(IAction action) {
        // TODO: cleanup?
        System.out.println("Not yet implemented. Following Projects are in the Working Set ("
                + getWorkingSet().getName() + "):");
        IProject[] projects = getProjects();
        for (IProject project : projects) {
            System.out.println(project.getName());
        }

        // System.out.println("loaded");
        // IProject project = getProject();
        //
        // Shell activeShell = PlatformUI.getWorkbench()
        // .getActiveWorkbenchWindow().getShell();
        // EvolizerImporterWizard wizard = new EvolizerImporterWizard();
        //
        // WizardDialog dialog = new WizardDialog(activeShell, wizard);
        // dialog.open();
        //
        // if (!wizard.isCanceled()) {
        // // Store user input
        // try {
        // project.setPersistentProperty(EvolizerPreferences.DB_HOST,
        // wizard.getHost());
        // project.setPersistentProperty(EvolizerPreferences.DB_NAME,
        // wizard.getDatabaseName());
        // project.setPersistentProperty(EvolizerPreferences.DB_USER,
        // wizard.getUsername());
        // project.setPersistentProperty(EvolizerPreferences.DB_PASSWORD,
        // wizard.getPassword());
        // } catch (CoreException e) {
        // logger.error("Error while attachting database connection Info to selected project", e);
        // }
        //
        // if (wizard.isImportEnabled()) { // Perform CVS-Log-Parsing and
        // // source code import if user has
        // // selected so, also sets
        // // CVSImporterJob sets the project's
        // // nature to "evolizer-enabled".
        // Job mainJob = new CVSImporterJob(project, wizard.getFileExtensionRegEx(),
        // wizard.isFileContentImportEnabled());
        //
        // mainJob.setUser(true);
        // mainJob.schedule();
        // } else { // If user did not choose to import, we expect that he
        // // wants to attach evolizer to an existing db.
        // // Therefore, we just need to mark the project as
        // // "evolizer-enabled".
        // try {
        // EvolizerNatureManager.applyEvolizerNature(project,
        // new NullProgressMonitor());
        // } catch (EvolizerException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
        // }
    }

    private IWorkingSet getWorkingSet() {
        Object selectedElement = fSelection.getFirstElement();
        if (selectedElement instanceof IWorkingSet) {
            return (IWorkingSet) selectedElement;
        } else {
            return null;
        }
    }

    private IProject[] getProjects() {
        IProject[] projects;
        Object selectedElement = fSelection.getFirstElement();
        if (selectedElement instanceof IWorkingSet) {
            IWorkingSet workingSet = (IWorkingSet) selectedElement;
            IAdaptable[] adaptables = workingSet.getElements();

            projects = new IProject[adaptables.length];

            for (int i = 0; i < adaptables.length; i++) {
                if (adaptables[i] instanceof IProject) {
                    projects[i] = (IProject) adaptables[i];
                } else {
                    projects[i] = (IProject) adaptables[i].getAdapter(IProject.class);
                }
            }
        } else {
            projects = new IProject[0];
        }

        return projects;
    }

    /**
     * {@inheritDoc}
     */
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            fSelection = (IStructuredSelection) selection;
        }
    }

}
