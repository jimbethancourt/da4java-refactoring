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
package org.evolizer.da4java.plugin.selectionhandler;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.utils.SnapshotAnalyzer;

/**
 * Abstract class for the various selection strategies to obtain the FAMIX entities from
 * the given Eclipse selection.
 * 
 * @author Katja Graefenhain, Martin Pinzger
 */
public abstract class AbstractSelectionHandler {
    
    /** The logger. */
//    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(AbstractSelectionHandler.class.getName());

    /** The selection. */
    private ISelection fSelection;

    /**
     * The constructor.
     * 
     * @param selection the selection
     */
    protected AbstractSelectionHandler(ISelection selection) {
        fSelection = selection;
    }

    /**
     * Traverse the selection and query corresponding FAMIX entities from the
     * database.
     * 
     * @param snapshotAnalyzer the snapshot analyzer
     * 
     * @return List of FAMIX entities.
     */
    public abstract List<AbstractFamixEntity> getSelectedEntities(SnapshotAnalyzer snapshotAnalyzer);

    /**
     * Determines the Java project of selected entities.
     * 
     * @return The corresponding Java project
     */
    public abstract IJavaProject getSelectedProject();


    /**
     * Determine the editor title which is based on the selection.
     * 
     * @return The editor title
     */
    public abstract String getEditorTitle();

    /**
     * Returns the selection.
     * 
     * @return The selection.
     */
    protected ISelection getSelection() {
        return fSelection;
    }
}
