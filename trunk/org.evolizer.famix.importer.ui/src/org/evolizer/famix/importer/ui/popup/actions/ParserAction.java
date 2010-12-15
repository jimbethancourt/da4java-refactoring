/*
 * Copyright 2009 University of Zurich, Switzerland
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
package org.evolizer.famix.importer.ui.popup.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.evolizer.core.exceptions.EvolizerException;
import org.evolizer.core.util.selectionhandling.JavaSelectionHelper;
import org.evolizer.famix.importer.jobs.FamixParserJob;

/**
 * Handler to perform the parsing of selected source code units.
 * 
 * @author pinzger
 */
public class ParserAction extends AbstractHandler {

    /**
     * Initializes and runs the parsing job on the current selection.
     * 
     * {@inheritDoc}
     */
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);

        List<IJavaElement> selectedJavaElements = new ArrayList<IJavaElement>();
        try {
            selectedJavaElements = JavaSelectionHelper.getPackagesAndSelectedJavaElements(selection);

            if (selectedJavaElements.size() > 0) {
                Job mainJob = new FamixParserJob(selectedJavaElements);
                mainJob.setUser(true);
                mainJob.schedule();
            }
        } catch (EvolizerException ee) {
            throw new ExecutionException(ee.getMessage());
        }


        return null;
    }

    //    @SuppressWarnings("unchecked")
    //    private List<IJavaElement> getSelectedJavaElements(ISelection selection) {
    //        List<IJavaElement> selectedJavaElements = new ArrayList<IJavaElement>();
    //
    //        if ((selection != null) && (selection instanceof IStructuredSelection)) {
    //            for (Iterator it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
    //                Object lElement = it.next();
    //                if (lElement instanceof IJavaElement) {
    //                    selectedJavaElements.add((IJavaElement) lElement);
    //                }
    //            }
    //        }
    //
    //        return selectedJavaElements;
    //    }
}
