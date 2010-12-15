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
package org.evolizer.da4java.graph.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAttribute;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.entities.FamixPackage;

/**
 * Simple map of entity types to their corresponding FAMIX entity classes.
 * 
 * @author pinzger
 */
public final class FamixEntityMap {
    /** The single sInstance. */
    private static FamixEntityMap sInstance;
    
    /** The fTypeNames. */
    private List<String> fTypeNames;
    
    /** The name to type map. */
    private Map<String, java.lang.Class<? extends AbstractFamixEntity>> fNameToTypeMap;

    /**
     * Instantiates a new FAMIX entity map.
     */
    private FamixEntityMap() {
        initDefaultTypes();
    }

    /**
     * Return the instance.
     * 
     * @return single sInstance of FamixEntityMap
     */
    public static FamixEntityMap getInstance() {
        if (sInstance == null) {
            sInstance = new FamixEntityMap();
        }

        return sInstance;
    }

    /**
     * Initialize the default types.
     */
    private void initDefaultTypes() {
        fTypeNames = new ArrayList<String>();
        fNameToTypeMap = new HashMap<String, java.lang.Class<? extends AbstractFamixEntity>>();

        addEntityType("FamixPackage", FamixPackage.class);
        addEntityType("FamixClass", FamixClass.class);
        addEntityType("FamixMethod", FamixMethod.class);
        addEntityType("FamixAttribute", FamixAttribute.class);
    }

    /**
     * Adds an entity type
     * 
     * @param name the type name
     * @param type the type class
     */
    public void addEntityType(String name, java.lang.Class<? extends AbstractFamixEntity> type) {
        if (!fNameToTypeMap.containsKey(name)) {
            fTypeNames.add(name);
            fNameToTypeMap.put(name, type);
        }
    }

    /**
     * Return the list of type names.
     * 
     * @return the type names
     */
    public List<String> getNames() {
        return fTypeNames;
    }

    /**
     * Returns the FAMIX entity type with the given type name.
     * 
     * @param name the name
     * 
     * @return the FAMIX entity type
     */
    public java.lang.Class<? extends AbstractFamixEntity> getType(String name) {
        return  fNameToTypeMap.get(name);
    }

    /**
     * Returns the contained FAMIX entity types.
     * 
     * @return the list of FAMIX entity types
     */
    public List<Class<? extends AbstractFamixEntity>>getAllTypes() {
        return new ArrayList<Class<? extends AbstractFamixEntity>>(fNameToTypeMap.values());
    }

    /**
     * Check whether the given FAMIX entity type is already contained.
     * 
     * @param type the FAMIX entity type
     * 
     * @return true, if successful otherwise false
     */
    public boolean containsType(Class<? extends AbstractFamixEntity> type) {
        return fNameToTypeMap.values().contains(type);
    }
}
