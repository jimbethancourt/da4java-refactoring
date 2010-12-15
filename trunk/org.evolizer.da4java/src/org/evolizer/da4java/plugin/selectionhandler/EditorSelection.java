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

import org.eclipse.jdt.core.IJavaElement;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;

/**
 * The EditorSelection represents the 
 * The user points the cursor to a lass, a method or a field and and is able to load a graph from that
 * selection.
 * 
 * @author mark
 */
public class EditorSelection implements ISelection {

    /** The selected text. */
    private ITextSelection fTextSelection;

    /** The selected compilation unit. */
    private IJavaElement fCompilationUnit;

    /** The name of the selection which will serve as the title of the editor title. */
    private String fSelectionName;

    /**
     * The constructor.
     * 
     * @param textSelection the text selection
     * @param cu the compilation unit
     */
    public EditorSelection(ITextSelection textSelection, IJavaElement cu) {
        fTextSelection = textSelection;
        fCompilationUnit = cu;
    }

    /**
     * Gets the compilation unit.
     * 
     * @return the compilation unit
     */
    public IJavaElement getCompilationUnit() {
        return fCompilationUnit;
    }

    /**
     * Returns true, if the selection is empty.
     * 
     * @return true, if checks if is empty
     */
    public boolean isEmpty() {
        boolean isEmpty = false;
        if (fTextSelection == null && fCompilationUnit == null) {
            isEmpty = true;
        }
        return isEmpty;
    }

    /**
     * Gets the text selection.
     * 
     * @return the text selection
     */
    public ITextSelection getTextSelection() {
        return fTextSelection;
    }

    /**
     * Gets the selection name.
     * 
     * @return the selection name
     */
    public String getSelectionName() {
        return fSelectionName;
    }

    /**
     * Sets the selection name.
     * 
     * @param name the new selection name
     */
    public void setSelectionName(String name) {
        fSelectionName = name;
    }
}
