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
package org.evolizer.core.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * Nature that flags a project for which Evolizer is activated.
 * 
 * @author wuersch
 */
public class EvolizerNature implements IProjectNature {

    /**
     * The Evolizer nature id.
     */
    public static final String ID = "org.evolizer.core.natures.evolizerNature";
    private IProject fProject;

    /**
     * {@inheritDoc}
     */
    public void configure() throws CoreException {
    // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void deconfigure() throws CoreException {
    // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public IProject getProject() {
        return fProject;
    }

    /**
     * {@inheritDoc}
     */
    public void setProject(IProject project) {
        fProject = project;
    }
}
