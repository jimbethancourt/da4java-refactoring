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

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.evolizer.core.natures.EvolizerNature;
import org.evolizer.core.natures.EvolizerNatureManager;
import org.evolizer.core.preferences.EvolizerPreferences;
import org.evolizer.core.ui.EvolizerUIPlugin;
import org.evolizer.core.ui.wizards.EvolizerImporterWizard;

/**
 * Menu action that adds the {@link EvolizerNature} to a project.
 * 
 * @author wuersch
 */
public class EnableEvolizerHandler extends AbstractHandler {

    private IStructuredSelection fSelection;
    private static Logger sLogger = EvolizerUIPlugin.getLogManager().getLogger(EnableEvolizerHandler.class.getName());

    /**
     * {@inheritDoc}
     */
    public Object execute(ExecutionEvent event) throws ExecutionException {
        sLogger.debug("Invoked Enable Evolizer Action!");

        IProject project = getProject(event);

        Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        EvolizerImporterWizard wizard = new EvolizerImporterWizard();

        WizardDialog dialog = new WizardDialog(activeShell, wizard);
        dialog.open();

        if (!wizard.isCanceled()) {
            // Store user input
            try {
                project.setPersistentProperty(EvolizerPreferences.DB_HOST, wizard.getHost());
                project.setPersistentProperty(EvolizerPreferences.DB_NAME, wizard.getDatabaseName());
                project.setPersistentProperty(EvolizerPreferences.DB_USER, wizard.getUsername());
                project.setPersistentProperty(EvolizerPreferences.DB_PASSWORD, wizard.getPassword());
            } catch (CoreException e) {
                sLogger.error("Error while attachting database connection Info to selected project", e);
            }

            IStatus result = EvolizerNatureManager.applyEvolizerNature(project, new NullProgressMonitor());

            if (result != Status.OK_STATUS) {
                // error dialog wouldn't open anyways for OK_STATUS, just to make it explicit.
                ErrorDialog.openError(
                        activeShell,
                        "Error",
                        "Cannot apply Evolizer nature to " + project.getName(),
                        result);
            }
        }

        sLogger.debug("Enable Evolizer Action completed!");
        return null;
    }

    private IProject getProject(ExecutionEvent event) {
        if (HandlerUtil.getCurrentSelection(event) instanceof IStructuredSelection) {
            fSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
        }
        Object selectedElement = fSelection.getFirstElement();
        IProject project;
        if (selectedElement instanceof IProject) {
            project = (IProject) selectedElement;
        } else {
            project = (IProject) ((IAdaptable) selectedElement).getAdapter(IProject.class);
        }

        return project;
    }
}
