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
package org.evolizer.da4java.commands;

import java.util.ArrayList;
import java.util.List;

import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.AbstractFamixEntity;

/**
 * FamixClass to store the result of graph edit commands, meaning edited entities and associations.
 * The edit result is needed to <code>undo</code> and <code>redo</code> commands.
 * 
 * @author Martin Pinzger
 */
public class EditResult {

    /** The list of edited (added/removed) FAMIX entities. */
    private List<AbstractFamixEntity> fEditedEntities;
    
    /** The list of edited (added/removed) FAMIX associations. */
    private List<FamixAssociation> fEditedAssociations;

    /**
     * The default constructor. Initializes an empty edit result.
     */
    public EditResult() {
        fEditedEntities = new ArrayList<AbstractFamixEntity>();
        fEditedAssociations = new ArrayList<FamixAssociation>();
    }

    /**
     * The constructor. Initializes the edit result with the given list of
     * added/removed FAMIX entities and associations.
     * 
     * @param reloadedEntities The list of edited FAMIX entities.
     * @param reloadedAssociations The list of edited FAMIX associations.
     */
    public EditResult(List<AbstractFamixEntity> reloadedEntities, List<FamixAssociation> reloadedAssociations) {
        fEditedEntities = reloadedEntities;
        fEditedAssociations = reloadedAssociations;
    }

    /**
     * Return the list of edited FAMIX entities.
     * 
     * @return The list of edited FAMIX entities.
     */
    public List<AbstractFamixEntity> getEntities() {
        return fEditedEntities;
    }

    /**
     * Return the list of edited FAMIX associations.
     * 
     * @return The list of edited FAMIX associations.
     */
    public List<FamixAssociation> getAssociations() {
        return fEditedAssociations;
    }

    /**
     * Add edited FAMIX entities.
     * 
     * @param entities The list of edited entities.
     */
    public void addEntities(List<AbstractFamixEntity> entities) {
        getEntities().addAll(entities);
    }

    /**
     * Add edited FAMIX associations.
     * 
     * @param associations The list of edited associations.
     */
    public void addAssociations(List<FamixAssociation> associations) {
        getAssociations().addAll(associations);
    }

    /**
     * Adds the FAMIX entities and associations of the given edit result.
     * 
     * @param result   The edit result.
     */
    public void addAll(EditResult result) {
        if (result.getEntities() != null) {
            fEditedEntities.addAll(result.getEntities());
        }
        if (result.getAssociations() != null) {
            fEditedAssociations.addAll(result.getAssociations());
        }
    }

    /**
     * Check whether the edit result is empty.
     * 
     * @return True, if the list of edited entities is empty, otherwise false.
     */
    public boolean isEmpty() {
        return getEntities().isEmpty() && getAssociations().isEmpty();
    }
}
