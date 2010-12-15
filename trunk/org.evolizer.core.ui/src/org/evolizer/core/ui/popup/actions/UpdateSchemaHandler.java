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
package org.evolizer.core.ui.popup.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.evolizer.core.exceptions.EvolizerException;
import org.evolizer.core.hibernate.session.EvolizerSessionHandler;

/**
 * Menu action that updates the RHDB schema, e.g., after another
 * Evolizer model was registered with Hibernate. Normal users should rarely
 * need to invoke that action, as they will check out a fixed set of Evolizer
 * Features once.
 * 
 * @author wuersch
 */
public class UpdateSchemaHandler extends AbstractHandler {

    private IStructuredSelection fSelection;

    /**
     * {@inheritDoc}
     */
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IProject project = getProject(event);

        try {
            EvolizerSessionHandler handler = EvolizerSessionHandler.getHandler();
            handler.updateSchema(project);
        } catch (EvolizerException e) {
            e.printStackTrace();
        }
        return null;
    }

    private IProject getProject(ExecutionEvent event) {
        if (HandlerUtil.getCurrentSelection(event) instanceof IStructuredSelection) {
            fSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
        }
        Object selectedElement = fSelection.getFirstElement();
        IProject project;
        if (selectedElement instanceof IProject) {
            project = (IProject) selectedElement;
        } else {
            project = (IProject) ((IAdaptable) selectedElement).getAdapter(IProject.class);
        }
        return project;
    }
}
