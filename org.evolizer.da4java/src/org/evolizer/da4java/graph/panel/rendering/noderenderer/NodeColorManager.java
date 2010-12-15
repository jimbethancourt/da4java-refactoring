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
package org.evolizer.da4java.graph.panel.rendering.noderenderer;

import java.awt.Color;
import java.util.Hashtable;

import org.evolizer.famix.model.entities.AbstractFamixEntity;

/**
 * Simple color handler for FAMIX entities.
 * 
 * @author Martin Pinzger
 */
public final class NodeColorManager {
    
    /** The Constant PACKAGE_TYPE. */
    private static final String PACKAGE_TYPE = "FamixPackage";
    
    /** The Constant CLASS_TYPE. */
    private static final String CLASS_TYPE = "FamixClass";
    
    /** The Constant INTERFACE_TYPE. */
    private static final String INTERFACE_TYPE = "Interface";
    
    /** The Constant METHOD_TYPE. */
    private static final String METHOD_TYPE = "FamixMethod";
    
    /** The Constant ATTRIBUTE_TYPE. */
    private static final String ATTRIBUTE_TYPE = "FamixAttribute";

    /** The node colors. */
    private static Hashtable<String, Color> sNodeColorMap;

    /** The sInstance. */
    private static NodeColorManager sInstance;

    /**
     * Instantiates a new node color manager.
     */
    private NodeColorManager() {
    }

    /**
     * Initializes the node color map.
     */
    private static void initNodeColors() {
        sNodeColorMap = new Hashtable<String, Color>();
        sNodeColorMap.put(PACKAGE_TYPE, new Color(255, 240, 200));
        sNodeColorMap.put(CLASS_TYPE, new Color(206, 235, 206));
        sNodeColorMap.put(INTERFACE_TYPE, new Color(227, 212, 234));
        sNodeColorMap.put(METHOD_TYPE, new Color(184, 183, 255));
        sNodeColorMap.put(ATTRIBUTE_TYPE, new Color(51, 102, 255));
    }

    /**
     * Determine the color of the given FAMIX entity.
     * 
     * @param famixEntity the FAMIX entity
     * 
     * @return the color
     */
    public static Color getColor(AbstractFamixEntity famixEntity) {
        if (sInstance == null) {
            sInstance = new NodeColorManager();
            initNodeColors();
        }

        String entityType = famixEntity.getType();

        if (famixEntity instanceof org.evolizer.famix.model.entities.FamixClass) {
            org.evolizer.famix.model.entities.FamixClass clazz = (org.evolizer.famix.model.entities.FamixClass) famixEntity;
            if (clazz.isInterface()) {
                entityType = INTERFACE_TYPE;
            }
        }

        Color fillColor;
        if (sNodeColorMap.containsKey(entityType)) {
            fillColor = sNodeColorMap.get(entityType);
        } else {
            fillColor = new Color(0, 200, 0);
        }

        return fillColor;
    }
}
