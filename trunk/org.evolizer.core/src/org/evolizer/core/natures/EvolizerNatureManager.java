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
package org.evolizer.core.natures;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.evolizer.core.EvolizerCorePlugin;
import org.evolizer.core.exceptions.EvolizerException;

/**
 * This class is responsible for attaching and removing the {@link EvolizerNature} to an {@link IProject}.
 * Attaching/removing has no particular effect (e.g., registering of special builders) but only flags the project.
 * Usually the nature is applied after RHDB connection information was attached to the project.
 * 
 * @author wuersch
 */
public final class EvolizerNatureManager {

    private static Logger sLogger = EvolizerCorePlugin.getLogManager().getLogger(EvolizerNatureManager.class.getName());

    private EvolizerNatureManager() {}

    /**
     * Checks whether a project has an {@link EvolizerNature} or not.
     * 
     * @param project
     *            a project that potentially might have an {@link EvolizerNature}.
     * @return <code>true</code> if the given project has an {@link EvolizerNature}; <code>false</code> otherwise.
     * @throws EvolizerException
     *             if the project does not exist or if the project is not open
     */
    public static boolean hasEvolizerNature(IProject project) throws EvolizerException {
        try {
            return project.hasNature(EvolizerNature.ID);
        } catch (CoreException e) {
            throw new EvolizerException(e);
        }
    }

    /**
     * Attaches an {@link EvolizerNature} to a project.
     * 
     * @param project
     *            the project that the nature should be attached to.
     * @param monitor
     *            a progress monitor, or <code>null</code> if progress reporting is not desired.
     * @return a status object with code {@link IStatus#OK} if the nature could be applied, otherwise a status object
     *         indicating what went wrong.
     */
    public static IStatus applyEvolizerNature(IProject project, IProgressMonitor monitor) {
        sLogger.debug("Trying to apply Evolizer nature to project '" + project.getName() + "'");

        try {
            if (!hasEvolizerNature(project)) {

                IProjectDescription description = project.getDescription();
                String[] natures = description.getNatureIds();
                String[] newNatures = new String[natures.length + 1];

                System.arraycopy(natures, 0, newNatures, 1, natures.length);
                newNatures[0] = EvolizerNature.ID;
                description.setNatureIds(newNatures);
                project.setDescription(description, monitor);
            }
        } catch (CoreException e) {
            sLogger.error("Error while applying Evolizer nature", e);
            return e.getStatus();
        } catch (EvolizerException e) {
            sLogger.error("Error while applying Evolizer nature", e);
            return ((CoreException) e.getCause()).getStatus();
        }

        sLogger.debug("Applied Evolizer nature to project '" + project.getName() + "'");

        return Status.OK_STATUS;
    }

    /**
     * Removes an {@link EvolizerNature} from a project.
     * 
     * @param project
     *            the project that the nature should be removed from.
     * @param monitor
     *            a progress monitor, or <code>null</code> if progress reporting is not desired.
     * @return a status object with code {@link IStatus#OK} if the nature could be removed, otherwise a status object
     *         indicating what went wrong.
     */
    public static IStatus removeEvolizerNature(IProject project, IProgressMonitor monitor) {
        sLogger.debug("Trying to remove Evolizer nature from project '" + project.getName() + "'");

        try {
            if (hasEvolizerNature(project)) {
                IWorkspace workspace = ResourcesPlugin.getWorkspace();

                IProjectDescription description = project.getDescription();
                String[] natures = description.getNatureIds();
                String[] newNatures = new String[natures.length - 1];

                // Copies all elements of natures[] to newNatures[], except the
                // evolizerNature
                int i = 0;
                for (int j = 0; j < natures.length; j++) {
                    if (!natures[j].equals(EvolizerNature.ID)) {
                        newNatures[i] = natures[j];
                        i++;
                    }
                }

                IStatus status = workspace.validateNatureSet(newNatures);
                if (status.getCode() == IStatus.OK) {

                    description.setNatureIds(newNatures);
                    project.setDescription(description, monitor);
                } else {
                    throw new CoreException(status);
                }
            }
        } catch (CoreException e) {
            sLogger.error("Error while removing Evolizer nature", e);
            return e.getStatus();
        } catch (EvolizerException e) {
            sLogger.error("Error while removing Evolizer nature", e);
            return ((CoreException) e.getCause()).getStatus();
        }

        sLogger.debug("Removed Evolizer nature from project '" + project.getName() + "'");

        return Status.OK_STATUS;
    }
}
