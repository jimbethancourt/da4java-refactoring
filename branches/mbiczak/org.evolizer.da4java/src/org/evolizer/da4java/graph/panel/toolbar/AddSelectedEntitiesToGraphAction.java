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
package org.evolizer.da4java.graph.panel.toolbar;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.evolizer.da4java.plugin.actions.AddSelectedEntitiesJob;

/**
 * The Class AddSelectedEntitiesToGraphAction.
 * 
 * @author pinzger
 */
public class AddSelectedEntitiesToGraphAction extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8457428419921097525L;
    
    /** The Eclipse selection. */
    private ISelection fSelection;

    /** 
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent event) {

        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                IViewPart packageExplorer = page.findView(JavaUI.ID_PACKAGES);
                ISelection packageExplorerSelection = packageExplorer.getViewSite().getSelectionProvider().getSelection();
                fSelection = packageExplorerSelection;
            }
        });

        AddSelectedEntitiesJob job = new AddSelectedEntitiesJob("Add selected entities to active graph window", fSelection);
        job.setUser(true);
        job.schedule();
    }
}
