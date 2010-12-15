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
package org.evolizer.da4java.graph.panel.rendering.edgerenderer;

import java.awt.Color;
import java.util.Hashtable;

import org.evolizer.famix.model.entities.FamixAccess;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixCastTo;
import org.evolizer.famix.model.entities.FamixCheckInstanceOf;
import org.evolizer.famix.model.entities.FamixInheritance;
import org.evolizer.famix.model.entities.FamixInvocation;
import org.evolizer.famix.model.entities.FamixSubtyping;

/**
 * The edge color manager contains the color for each FAMIX association.
 * 
 * @author pinzger
 */
public final class EdgeColorManager {

    /** The sColorMap. */
    private static Hashtable<Class<? extends FamixAssociation>, Color> sColorMap;

    /** The sInstance. */
    private static EdgeColorManager sInstance;

    /**
     * Instantiates a new edge color manager.
     */
    private EdgeColorManager() {
    }

    /**
     * Initializes the node color map.
     */
    private static void initNodeColors() {
        sColorMap = new Hashtable<Class<? extends FamixAssociation>, Color>();
        sColorMap.put(FamixInvocation.class, new Color(255, 52, 52));
        sColorMap.put(FamixAccess.class, new Color(51, 102, 255));
        sColorMap.put(FamixInheritance.class, new Color(28, 28, 28));
        sColorMap.put(FamixSubtyping.class, new Color(28, 28, 28));
        sColorMap.put(FamixCastTo.class, new Color(0, 128, 64));
        sColorMap.put(FamixCheckInstanceOf.class, new Color(0, 128, 64));
    }

    /**
     * Return the color for the given association type.
     * 
     * @param associationType the association type
     * 
     * @return the color
     */
    public static Color getColor(Class<? extends FamixAssociation> associationType) {
        if (sInstance == null) {
            sInstance = new EdgeColorManager();
            initNodeColors();
        }

        Color fillColor;
        if (sColorMap.containsKey(associationType)) {
            fillColor = sColorMap.get(associationType);
        } else {
            fillColor = new Color(0, 0, 0);
        }

        return fillColor;
    }
}
