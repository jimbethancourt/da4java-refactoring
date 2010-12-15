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
package org.evolizer.famix.importer.ui.popup.actions;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.evolizer.core.exceptions.EvolizerException;
import org.evolizer.core.hibernate.session.EvolizerSessionHandler;
import org.evolizer.core.util.selectionhandling.JavaSelectionHelper;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.importer.ProjectParser;
import org.evolizer.famix.importer.jobs.FamixStoreModelJob;
import org.evolizer.famix.importer.ui.FamixImporterUIPlugin;
import org.evolizer.famix.model.entities.AbstractFamixEntity;

/**
 * Handler to store the FAMIX model to a Hibernate mapped SQL database.
 * 
 * @author pinzger
 */
public class StoreModelAction extends AbstractHandler {
    /**
     * The logger.
     */
    private static Logger sLogger = FamixImporterUIPlugin.getLogManager().getLogger(StoreModelAction.class.getName());

    /**
     * Initializes and runs the job to store the last parsed FAMIX model of the selected project.
     * 
     * {@inheritDoc}
     */
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        
        try {
            final IJavaProject javaProject = JavaSelectionHelper.getProject(selection);

            if ((selection != null) && (FamixImporterPlugin.getDefault().getParser() != null)) {
                ProjectParser currentParser = FamixImporterPlugin.getDefault().getParser();
                IJavaProject parsedProject = JavaSelectionHelper.getProject(currentParser.getSelection().iterator().next());
                if (javaProject.equals(parsedProject)) {
                    if ((currentParser.getModel() != null)
                            && (currentParser.getModel().getFamixEntities().size() > 0)) {
                        AbstractFamixEntity firstEntity = currentParser.getModel().getFamixEntities().iterator().next();
                        if (firstEntity.getId() == null) {
                            EvolizerSessionHandler.getHandler().initSessionFactory(javaProject.getProject());

                            String dbUrl = EvolizerSessionHandler.getHandler().getDBUrl(javaProject.getProject());
                            Job mainJob = new FamixStoreModelJob(dbUrl, currentParser.getModel());
                            mainJob.setUser(true);
                            mainJob.schedule();
                        } else {
                            sLogger.warn("FamixModel has already been stored.");
                        }
                    } else {
                        sLogger.warn("No elements to store for the selected project. Extract a model first.");
                    }
                } else {
                    sLogger.warn("Last processed project does not correspond to slected project. Select "
                            + parsedProject.getElementName());
                }
            } else {
                sLogger.warn("The project needs to be parsed first.");
            }
        } catch (EvolizerException ee) {
            throw new ExecutionException(ee.getMessage());
        }

        return null;
    }
}
