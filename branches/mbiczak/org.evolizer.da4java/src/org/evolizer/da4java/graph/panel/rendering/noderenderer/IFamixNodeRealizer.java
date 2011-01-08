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


import java.awt.Font;

import org.evolizer.famix.model.entities.AbstractFamixEntity;

/**
 * The interface of FAMIX node realizers.
 * 
 * @author pinzger
 */
public interface IFamixNodeRealizer {

    /** Label length. */
    int LABEL_LENGTH = 6;

    /** Lable font. */
    Font DEFAULT_LABEL_FONT = new Font("Arial", Font.PLAIN, 14);

    /**
     * Initializes the realizer attributes, that is, the FAMIX entity.
     * 
     * @param famixEntity the FAMIX entity
     */
    void initAttributes(AbstractFamixEntity famixEntity);

    /**
     * Sets the default height.
     */
    void setDefaultHeight();

    /**
     * Sets the default width.
     */
    void setDefaultWidth();

    /**
     * Sets the default color.
     */
    void setDefaultColor();

}
