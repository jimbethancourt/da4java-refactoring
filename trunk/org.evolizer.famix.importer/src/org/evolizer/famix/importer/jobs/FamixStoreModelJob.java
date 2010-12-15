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
package org.evolizer.famix.importer.jobs;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.evolizer.core.exceptions.EvolizerException;
import org.evolizer.core.exceptions.EvolizerRuntimeException;
import org.evolizer.core.hibernate.session.EvolizerSessionHandler;
import org.evolizer.core.hibernate.session.api.IEvolizerSession;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.importer.util.DAOModel;
import org.evolizer.famix.model.entities.FamixModel;


/**
 * Job for storing the last parsed FAMIX model to the database 
 * given by its URL.
 *
 * @author pinzger
 *
 */
public class FamixStoreModelJob extends Job {
    /**
     * The logger. 
     */
    private static final Logger sfLogger = FamixImporterPlugin.getLogManager().getLogger(FamixStoreModelJob.class.getName());
    /**
     * URL of the database.
     */
    private String fDBUrl;
    /**
     * The FAMIX model.
     */
    private FamixModel fFamixModel;
    
    /**
     * The constructor.
     * 
     * @param dbUrl The URL of the database.
     * @param famixModel    The FAMIX model.
     */
    public FamixStoreModelJob(String dbUrl, FamixModel famixModel) {
        super("Store last parsed FAMIX model");
        
        fDBUrl = dbUrl;
        fFamixModel = famixModel;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        IStatus status = Status.OK_STATUS;
        SubMonitor progress = SubMonitor.convert(monitor, "Storing entities and associations", 100);

        try {
            // Always perform a schema update before storing the model
            progress.setTaskName("Checking database schema ...");
            IEvolizerSession session = EvolizerSessionHandler.getHandler().getCurrentSession(fDBUrl);
            if (session.isOpen()) {
//                session.close();  // closing the session causes inconsistencies!
                Properties properties = EvolizerSessionHandler.getHandler().getProperties(fDBUrl);
                EvolizerSessionHandler.getHandler().updateSchema(properties);
            }
            progress.worked(20);
            progress.setTaskName("Deleting existing models of this project ...");
            DAOModel queryModel= new DAOModel(fDBUrl);
            List<FamixModel> existingModels = queryModel.queryStoredModels(fFamixModel.getName());
            for (FamixModel famixModel : existingModels) {
                DAOModel modelToDelete = new DAOModel(fDBUrl, famixModel);
                modelToDelete.deleteModel();
            }
            progress.worked(20);
            
            DAOModel newDAOModel = new DAOModel(fDBUrl, fFamixModel);
            newDAOModel.store(progress.newChild(60));
        } catch (EvolizerRuntimeException ere) {
            sfLogger.error("Error storing FAMIX" , ere);
        } catch (EvolizerException ee) {
            sfLogger.error("Error storing FAMIX" , ee);
        } finally {
            if (monitor != null) {
                monitor.done();
            }
        }

        return status;
    }

}
