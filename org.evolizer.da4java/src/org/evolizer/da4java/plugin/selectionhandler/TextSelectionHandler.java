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
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.evolizer.core.exceptions.EvolizerException;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.utils.SnapshotAnalyzer;
import org.hibernate.HibernateException;

/**
 * Selection handler that loads the needed FAMIX entities from the database corresponding to the
 * given TextSelection of a JavaEditor.
 * 
 * TODO: The text selection is currently not working.
 * 
 * @author mark
 */
public class TextSelectionHandler extends AbstractSelectionHandler {

    /**
     * Constructor.
     * 
     * @param selection the selection
     */
    public TextSelectionHandler(ISelection selection) {
        super(selection);
    }


    /**
     * FamixMethod that returns a List of FamixEntities corresponding to the given EditorSelection
     * Returns null if the given selection is not of type EditorSelection.
     * 
     * @param snapshotAnalyzer the snapshot analyzer
     * 
     * @return the selected entities
     */
    @SuppressWarnings({ "unchecked", "restriction" })
    @Override
    public List<AbstractFamixEntity> getSelectedEntities(SnapshotAnalyzer snapshotAnalyzer) {
        List<AbstractFamixEntity> selectedEntities = new ArrayList<AbstractFamixEntity>();
        if (getSelection() instanceof EditorSelection) {
            EditorSelection editorSelection = (EditorSelection) getSelection();
            IJavaElement compilationUnit = editorSelection.getCompilationUnit();
            IPath cuPath = compilationUnit.getPath();
            String path = cuPath.toString().substring(1);

            ITextSelection textSelection = editorSelection.getTextSelection();
            String offset = Integer.toString(textSelection.getOffset());
            try {
                @SuppressWarnings("unused")
                List result = snapshotAnalyzer.getHibernateSession().createSQLQuery("SELECT fe.* FROM famixentity fe, sourceanchor sa " 
                        + " WHERE fe.sourceanchor_fk = sa.id AND sa.file like '%" + path + "'" 
                        + " AND fe.uniqueName not like '%.<clinit>()' and fe.uniqueName not like '%.<oinit>()'" 
                        + " AND sa.start < " + offset + " AND sa.end > " + offset).addEntity(AbstractFamixEntity.class).list();

                // selectedEntities = new Vector(filterResult(result));

                editorSelection.setSelectionName(selectedEntities.get(0).getName());
            } catch (HibernateException e) {
                e.printStackTrace();
            } catch (EvolizerException e) {
                e.printStackTrace();
            }
        }

        return selectedEntities;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public IJavaProject getSelectedProject() {
        EditorSelection editorSelection = (EditorSelection) getSelection();
        IJavaProject project = editorSelection.getCompilationUnit().getJavaProject();

        return project;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public String getEditorTitle() {
        EditorSelection eSelection = (EditorSelection) getSelection();
        return eSelection.getSelectionName();
    }

//    /**
//     * Gets the active editor java input.
//     * 
//     * @return the active editor java input
//     */
//    private IJavaElement getActiveEditorJavaInput() {
//        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//        if (page != null) {
//            IEditorPart part = page.getActiveEditor();
//            if (part != null) {
//                IEditorInput input = part.getEditorInput();
//                if (input != null) {
//                    return JavaUI.getEditorInputJavaElement(input);
//                }
//            }
//        }
//        return null;
//    }
//
//    /**
//     * FamixMethod to filter unneeded entities.
//     * 
//     * @param listToFilter the list to filter
//     * 
//     * @return the set< abstract famix entity>
//     */
//    private Set<AbstractFamixEntity> filterResult(List<AbstractFamixEntity> listToFilter) {
//        Set<AbstractFamixEntity> result = new HashSet<AbstractFamixEntity>(listToFilter.size());
//        boolean noClassSelected = false;
//        for (AbstractFamixEntity entity : listToFilter) {
//            if (!entity.getUniqueName().endsWith(".<clinit>()") && !entity.getUniqueName().endsWith(".<oinit>()")) {
//                result.add(entity);
//                if (!(entity instanceof FamixClass)) {
//                    noClassSelected = true;
//                }
//            }
//        }
//
//        if (noClassSelected) {
//            for (AbstractFamixEntity entity : result) {
//                if (entity instanceof FamixClass) {
//                    result.remove(entity);
//                }
//            }
//        }
//        return result;
//    }

}
