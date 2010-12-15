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

import org.evolizer.famix.model.entities.AbstractFamixEntity;

/**
 * Helper class with utility methods to convert FAMIX names.
 * 
 * @author Katja Graefenhain
 */
public final class Util {
    
    /**
     * Hidden constructor.
     */
    private Util() {
    }
    
    /**
     * Returns the short name of the AbstractFamixEntity. Used to set node labels.
     * 
     * @param famixEntity the famix entity
     * 
     * @return the name of the AbstractFamixEntity without package declaration and
     * parameter list
     */
    public static String getShortName(AbstractFamixEntity famixEntity) {
        String lName = famixEntity.getName();
        if (lName.indexOf("(") != -1) {
            lName = lName.substring(0, lName.indexOf("(")) + "()";
        }
        if (lName.lastIndexOf(".") != -1) {
            lName = lName.substring(lName.lastIndexOf("."));
        }
        return lName;
    }

}
