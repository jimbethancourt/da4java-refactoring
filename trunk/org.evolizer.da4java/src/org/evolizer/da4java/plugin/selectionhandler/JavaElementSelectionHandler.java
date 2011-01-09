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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.evolizer.core.exceptions.EvolizerException;
import org.evolizer.core.exceptions.EvolizerRuntimeException;
import org.evolizer.core.util.selectionhandling.JavaSelectionHelper;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.utils.SnapshotAnalyzer;

/**
 * Retrieves the FAMIX entities of from the given Java element selection.
 * For that the Java element names are converted into FAMIX model
 * conform unique names. In case of {@link IMethod} elements, the
 * source reference is also computed to allow the selection of 
 * corresponding FAMIX methods.
 * 
 * @author pinzger
 */
public class JavaElementSelectionHandler extends AbstractSelectionHandler {
    /** The logger. */
    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(AbstractSelectionHandler.class.getName());

    /**
     * The constructor.
     * 
     * @param selection the selection
     */
    public JavaElementSelectionHandler(ISelection selection) {
        super(selection);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public List<AbstractFamixEntity> getSelectedEntities(SnapshotAnalyzer snapshotAnalyzer) throws EvolizerRuntimeException {
        List<AbstractFamixEntity> famixEntities = new ArrayList<AbstractFamixEntity>();

        try {
            List<IJavaElement> javaElements = JavaSelectionHelper.getPackagesAndSelectedJavaElements(getSelection());
            famixEntities.addAll(snapshotAnalyzer.queryEntitiesByUniqueName(getJavaElementNames(javaElements)));
            famixEntities.addAll(snapshotAnalyzer.queryEntitiesBySourceReference(getJavaElementNamesAndSourceReference(javaElements)));
        } catch (EvolizerException ee) {
            throw new EvolizerRuntimeException("Error getting FAMIX conform names of Java elements", ee);
        }

        return famixEntities;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public IJavaProject getSelectedProject() {
        IJavaProject project = null;
        try {
            project = JavaSelectionHelper.getProject(getSelection());
        } catch (EvolizerException e) {
            e.printStackTrace();
        }

        return project;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public String getEditorTitle() {
        String editorTitle = "Graph";

        if (getSelection() instanceof ITreeSelection) {
            ITreeSelection strucSelection = (ITreeSelection) getSelection();
            Object elem = strucSelection.getFirstElement();
            if (elem instanceof IJavaElement) {
                IJavaElement javaElem = (IJavaElement) elem;
                editorTitle = javaElem.getElementName();
            }
        }

        return editorTitle;
    }

    /**
     * Get the FAMIX model conform unique names plus the source reference for the given Java elements.
     * Currently this only applies to {@link IMethod} elements.
     * 
     * @param javaElements the java elements
     * 
     * @return the java element names and source reference
     * 
     * @throws JavaModelException the java model exception
     */
    private Hashtable<String, Integer> getJavaElementNamesAndSourceReference(List<IJavaElement> javaElements) throws EvolizerRuntimeException {
        Hashtable<String, Integer> sourceReferences = new Hashtable<String, Integer>();
        for (IJavaElement element : javaElements) {
            if (element instanceof IMethod) {
                try {
                    sourceReferences.put(JavaElementUtilities.getUniqueNameFromJavaElement(element), ((IMethod) element).getSourceRange().getOffset());
                } catch (JavaModelException jme) {
                    throw new EvolizerRuntimeException("Error determining source range of Java element " + element.getElementName(), jme);
                }
            }
        }
        return sourceReferences;
    }

    /**
     * Get the FAMIX model conform names for Java elements except {@link IMethod}.
     * For that also the source code reference is needed, because the type
     * of the parameters are not fully qualified. 
     * 
     * @param javaElements the java elements
     * 
     * @return the java element names
     * 
     * @throws JavaModelException the java model exception
     */
    private List<String> getJavaElementNames(List<IJavaElement> javaElements) throws EvolizerRuntimeException {
        List<String> names = new ArrayList<String>();
        for (IJavaElement element : javaElements) {
            if (!(element instanceof IMethod)) {
                names.add(JavaElementUtilities.getUniqueNameFromJavaElement(element));
            }
        }
        return names;
    }

}
