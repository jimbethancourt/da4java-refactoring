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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.evolizer.core.natures.EvolizerNature;
import org.evolizer.core.natures.EvolizerNatureManager;
import org.evolizer.core.ui.EvolizerUIPlugin;

/**
 * Menu action that removes the {@link EvolizerNature} from a project.
 * 
 * @author wuersch
 */
public class DisableEvolizerHandler extends AbstractHandler {

    private static Logger sLogger = EvolizerUIPlugin.getLogManager().getLogger(DisableEvolizerHandler.class.getName());
    private IStructuredSelection fSelection;

    /**
     * {@inheritDoc}
     */
    public Object execute(ExecutionEvent event) throws ExecutionException {
        sLogger.debug("Invoked Disable Evolizer Action!");
        IProject project = getProject(event);

        Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        IStatus result = EvolizerNatureManager.removeEvolizerNature(project, new NullProgressMonitor());

        if (result != Status.OK_STATUS) {
            ErrorDialog.openError(
                    activeShell,
                    "Error",
                    "Cannot remove Evolizer nature from " + project.getName(),
                    result);
        }

        sLogger.debug("Disable Evolizer Action completed!");
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
