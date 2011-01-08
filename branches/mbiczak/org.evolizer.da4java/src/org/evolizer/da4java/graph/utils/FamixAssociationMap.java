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

import org.evolizer.famix.model.entities.FamixAccess;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixCastTo;
import org.evolizer.famix.model.entities.FamixCheckInstanceOf;
import org.evolizer.famix.model.entities.FamixInheritance;
import org.evolizer.famix.model.entities.FamixInvocation;
import org.evolizer.famix.model.entities.FamixSubtyping;

/**
 * Simple map of association types to their corresponding FAMIX association classes.
 * 
 * @author pinzger
 */
public final class FamixAssociationMap {
    
    /** The fTypeNames. */
    private List<String> fTypeNames;
    
    /** The name to type map. */
    private Map<String, java.lang.Class<? extends FamixAssociation>> fNameToTypeMap;

    /** The single instance. */
    private static FamixAssociationMap sInstance;

    /**
     * Instantiates a new famix association map.
     */
    private FamixAssociationMap() {
        initDefaultTypes();
    }

    /**
     * Gets the single sInstance of FamixAssociationMap.
     * 
     * @return single sInstance of FamixAssociationMap
     */
    public static FamixAssociationMap getInstance() {
        if (sInstance == null) {
            sInstance = new FamixAssociationMap();
        }

        return sInstance;
    }

    /**
     * Inits the default types.
     */
    private void initDefaultTypes() {
        fTypeNames = new ArrayList<String>();
        fNameToTypeMap = new HashMap<String, java.lang.Class<? extends FamixAssociation>>();

        addAssociationType("ALL", null);
        addAssociationType("Accesses", FamixAccess.class);
        addAssociationType("Invocations", FamixInvocation.class);
        addAssociationType("Inherits", FamixInheritance.class);
        addAssociationType("Subtypes", FamixSubtyping.class);
        addAssociationType("Cast tos", FamixCastTo.class);
        addAssociationType("Check Instance ofs", FamixCheckInstanceOf.class);
    }

    /**
     * Adds the association type.
     * 
     * @param name the name
     * @param type the type
     */
    public void addAssociationType(String name, java.lang.Class<? extends FamixAssociation> type) {
        if (!fNameToTypeMap.containsKey(name)) {
            fTypeNames.add(name);
            fNameToTypeMap.put(name, type);
        }
    }

    /**
     * Gets the fTypeNames.
     * 
     * @return the fTypeNames
     */
    public List<String> getNames() {
        return fTypeNames;
    }

    /**
     * Returns the FAMIX association class of a given type string.
     * 
     * @param name the type name
     * 
     * @return the FAMIX association class
     */
    public java.lang.Class<? extends FamixAssociation> getType(String name) {
        return  fNameToTypeMap.get(name);
    }

    /**
     * Returns all FAMIX association classes.
     * 
     * @return all FAMIX association classes
     */
    public List<java.lang.Class<? extends FamixAssociation>> getAllTypes() {
        return new ArrayList<java.lang.Class<? extends FamixAssociation>>(fNameToTypeMap.values());
    }
}
