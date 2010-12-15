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
package org.evolizer.famix.importer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.evolizer.core.exceptions.EvolizerException;
import org.evolizer.core.exceptions.EvolizerRuntimeException;
import org.evolizer.core.hibernate.session.EvolizerSessionHandler;
import org.evolizer.core.hibernate.session.api.IEvolizerSession;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixModel;

/**
 * Handles storing and loading FAMIX models of the given Eclipse project. The configuration from the database connection
 * is obtained from the selected project properties. Storing of FAMIX models extracted from multiple projects is
 * supported.
 * 
 * @author pinzger
 */
public class DAOModel {
    /**
     * The sLogger
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(DAOModel.class.getName());

    /**
     * The FAMIX model to store
     */
    private FamixModel fModel;
    /**
     * The selected Java project
     */
    private String fDBUrl;

    /**
     * The constructor for loading a model
     * 
     * @param dbUrl The databse URL
     */
    public DAOModel(String dbUrl) {
        fDBUrl = dbUrl;
        fModel = null;
    }
    
    /**
     * The constructor for storing a model
     * 
     * @param dbUrl
     *            the database URL
     * @param model
     *            the FAMIX model to store
     */
    public DAOModel(String dbUrl, FamixModel model) {
        fDBUrl = dbUrl;
        fModel = model;
    }

    /**
     * Stores FAMIX entities and associations contained by model.
     * 
     * @param monitor The progress monitor.
     */
    public void store(IProgressMonitor monitor) {
        sLogger.debug("Storing Famix-FamixModel " + fModel.getName());

        if (fModel.getFamixEntities().size() > 0) {
            try {
                storeAllFamixObjects(monitor);
            } catch (EvolizerException e) {
                sLogger.error("Error while storing Famix-FamixModel data: " + e.getMessage(), e);
            }
        } else {
            sLogger.warn("Famix-FamixModel is empty: Nothing to store");
        }
        sLogger.info("Famix-FamixModel storage complete");
        sLogger.debug("==== stored " + fModel.getFamixEntities().size() + " Famix-Entities ====");
    }

    /**
     * Load all the <strong>last</strong> stored model object with the given name. 
     * 
     * @param modelName The name of the FAMIX model.
     * @return The FAMIX model. 
     */
    public FamixModel loadModel(String modelName) {
        sLogger.debug("Loading Famix-FamixModel " + modelName + " from database");
        
        try {
            List<FamixModel> storedModels = queryStoredModels(modelName);
            fModel = storedModels.get(0);
            populateBundleContainer(fModel);
        } catch (IndexOutOfBoundsException iobe) {
            sLogger.error("Error while loading Famix-FamixModel data from database: " + iobe.getMessage(), iobe);
        }
        
        sLogger.debug("Loading Famix-FamixModel " + modelName + " from database completed");
        
        return fModel;
    }
    
    private void populateBundleContainer(FamixModel model) {
        for (AbstractFamixEntity entity : model.getFamixEntities()) {
            fModel.addElement(entity);
        }
        for (FamixAssociation association : model.getFamixAssociations()) {
            fModel.addRelation(association);
        }
    }

    /**
     * Returns a list of FAMIX models with the given name. 
     * 
     * @param modelName The name of the FAMIX model.
     * @return  The list of FAMIX models.
     */
    public List<FamixModel> queryStoredModels(String modelName) {
        List<FamixModel> storedModels = new ArrayList<FamixModel>();
        
        try {
            IEvolizerSession lSession = EvolizerSessionHandler.getHandler().getCurrentSession(fDBUrl);
            storedModels = lSession.query("from FamixModel as fm " +
                    "where fm.name = '" + modelName + "'" +
                    "order by fm.created desc", FamixModel.class);
        } catch (EvolizerRuntimeException ere) {
            sLogger.error("Error while querying Famix-FamixModels" + ere.getMessage(), ere);
        } catch (EvolizerException ee) {
            sLogger.error("Error while querying Famix-FamixModels" + ee.getMessage(), ee);
        }
        
        return storedModels;
    }
    
    /**
     * Deletes the current FAMIX model object from the database.
     * Through cascading all contained entities and associations
     * of this model are deleted as well.
     * 
     * @return True if the delete was successful otherwise false.
     */
    public boolean deleteModel() {
        sLogger.debug("Deleting Famix-FamixModel " + fModel.getName() + " from database");
        
        boolean wasSuccessful = false;
        
        IEvolizerSession lSession = null;
        try {
            lSession = EvolizerSessionHandler.getHandler().getCurrentSession(fDBUrl);
            lSession.startTransaction();
            
            lSession.delete(fModel); 
            wasSuccessful = true;
        } catch (EvolizerRuntimeException e) {
            e.printStackTrace();
            if (lSession != null) {
                lSession.rollbackTransaction();
            }
        } catch (EvolizerException e) {
            e.printStackTrace();
            if (lSession != null) {
                lSession.rollbackTransaction();
            }
        } finally {
            if (lSession != null) {
                lSession.endTransaction();
            }
        }
        sLogger.debug("Deleting Famix-FamixModel " + fModel.getName() + " from database completed");
        
        return wasSuccessful;
    }

    /**
     * Store associations assigned to the from-entity (to prevent duplication because associations are also assigned to
     * the to-entity).
     * 
     * @param The progress monitor.
     */
    private void storeAllFamixObjects(IProgressMonitor monitor) throws EvolizerException {
        SubMonitor progress = SubMonitor.convert(monitor, 100);
        progress.setTaskName("Initializing ...");

        Set<AbstractFamixEntity> entities = fModel.getFamixEntities();

        HashMap<AbstractFamixEntity, Set<FamixAssociation>> entityAssocaitionMap = new HashMap<AbstractFamixEntity, Set<FamixAssociation>>();

        for (AbstractFamixEntity lEntity : entities) {
            HashSet<FamixAssociation> associations = new HashSet<FamixAssociation>();
            for (FamixAssociation lAss : fModel.getAssociations(lEntity)) {
                if (lAss.getFrom().getUniqueName().equals(lEntity.getUniqueName())) { // prevent duplication of
                    associations.add(lAss);
                }
            }
            entityAssocaitionMap.put(lEntity, associations);
        }
        progress.worked(10);

        Set<AbstractFamixEntity> keys = entityAssocaitionMap.keySet();

        IEvolizerSession lSession = null;
        try {
            sLogger.debug("Storing all Famix-Entities");
            progress.setTaskName("Storing FAMIX entities");

            lSession = EvolizerSessionHandler.getHandler().getCurrentSession(fDBUrl);
            lSession.startTransaction();
            for (AbstractFamixEntity element : keys) {
                sLogger.debug("Adding to database " + element.getClass().getName() + ":\n" + element.getUniqueName());
                lSession.saveObject(element);
            }
            lSession.flush();
            lSession.clear();

            sLogger.debug("Famix-Entities storage complete");
            progress.worked(45);

            sLogger.debug("Storing all Associstions");
            progress.setTaskName("Storing FAMIX associations");
            for (AbstractFamixEntity element : keys) {
                for (FamixAssociation association : entityAssocaitionMap.get(element)) {
                    if ((association.getFrom().getId() != null) && (association.getTo().getId() != null)) {
                        sLogger.debug("Adding to database " + association.getClass().getName() + ":\n" + "FROM "
                                + association.getFrom().getUniqueName() + " TO " + association.getTo().getUniqueName());
                        // fSession.saveObject(association);
                        lSession.saveObject(association);
                    } else {
                        sLogger.warn("Missing ID in " + association.getType() + " association of entitye "
                                + element.getUniqueName() + "(" + element.getId() + ")");
                    }
                }
                lSession.flush();
                lSession.clear();
            }
            sLogger.debug("Associations storage complete");
            
            lSession.saveObject(fModel);
            lSession.flush();
            lSession.clear();
            sLogger.debug("Model entry stored");
            
            progress.worked(45);
        } catch (EvolizerException e) {
            if (lSession != null) {
                lSession.rollbackTransaction();
            }
            throw new EvolizerException(e);
        } finally {
            if (lSession != null) {
                lSession.endTransaction();
            }
        }
    }
}
