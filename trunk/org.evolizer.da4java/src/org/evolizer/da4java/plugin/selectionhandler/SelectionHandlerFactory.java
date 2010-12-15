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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;

/**
 * Factory class (singleton) for selection handler. Currently, there is support
 * for obtaining FAMIX conform entities from:
 * <ul>
 * <li>ITreeSelection</li>
 * <li>
 * </ul>
 * 
 * @author pinzger
 */
public final class SelectionHandlerFactory {
    
    /** The sInstance. */
    private static SelectionHandlerFactory sInstance;

    /**
     * Instantiates a new selection handler creator.
     */
    private SelectionHandlerFactory() {
    }

    /**
     * The constructor.
     * 
     * @return single sInstance of SelectionHandlerFactory
     */
    public static SelectionHandlerFactory getInstance() {
        if (sInstance == null) {
            sInstance = new SelectionHandlerFactory();
        }

        return sInstance;
    }

    /**
     * Gets the adequate handler to process the given Eclipse selection.
     * 
     * @param selection the selection
     * 
     * @return the selection handler
     */
    public AbstractSelectionHandler getSelectionHandler(ISelection selection) {
        AbstractSelectionHandler selectionHandler = null;
        if (selection instanceof ITreeSelection) {
            selectionHandler = new JavaElementSelectionHandler(selection);
        } else if (selection instanceof EditorSelection) {
            selectionHandler = new TextSelectionHandler(selection);
        } else {
            selectionHandler = new JavaElementSelectionHandler(selection);
        }

        return selectionHandler;
    }
}
