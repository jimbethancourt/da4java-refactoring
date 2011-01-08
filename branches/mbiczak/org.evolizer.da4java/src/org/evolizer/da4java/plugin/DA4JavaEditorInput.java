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
package org.evolizer.da4java.plugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * The input for the GraphEditor.
 * 
 * @author Martin Pinzger, Katja Graefenhain
 */
public class DA4JavaEditorInput implements IEditorInput {
    
    /** The Constant DAFORJAVA_EDITOR_NAME. */
    private static final String DAFORJAVA_EDITOR_NAME = "DA4Java Graph";

    /** The fSelection. */
    private ISelection fSelection;

    /**
     * Instantiates a new d a4 java editor input.
     * 
     * @param selection the selection
     */
    public DA4JavaEditorInput(ISelection selection) {
        super();
        this.fSelection = selection;
    }

    /**
     * Gets the selection.
     * 
     * @return the selection
     */
    public ISelection getSelection() {
        return fSelection;
    }

    /** 
     * {@inheritDoc}
     */
    public boolean exists() {
        if (getSelection().isEmpty()) {
            return false;
        }
        return true;
    }

    /** 
     * {@inheritDoc}
     */
    public ImageDescriptor getImageDescriptor() {
        // not implemented
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public String getName() {
        return DAFORJAVA_EDITOR_NAME;
    }

    /** 
     * {@inheritDoc}
     */
    public IPersistableElement getPersistable() {
        // not implemented
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public String getToolTipText() {
        return getName();
    }

    /** 
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        // not implemented
        return null;
    }
}
