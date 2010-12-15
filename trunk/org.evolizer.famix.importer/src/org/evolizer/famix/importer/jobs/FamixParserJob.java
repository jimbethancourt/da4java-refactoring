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
package org.evolizer.famix.importer.jobs;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.importer.ProjectParser;
import org.evolizer.famix.importer.unresolved.UnresolvedInvocationHandler;
import org.evolizer.famix.model.entities.FamixModel;


/**
 * FAMIX Importer parser Job.
 *
 *
 * @author pinzger
 *
 */
public class FamixParserJob extends Job {
    private List<IJavaElement> fSelectedJavaElements;
    private FamixModel fFamixModel;
    
    /**
     * Default constructor.
     * 
     * @param selectedJavaElements  The list of selected Java elements.
     */
    public FamixParserJob(List<IJavaElement> selectedJavaElements) {
        super("Parse selected Java elements");
        
        fSelectedJavaElements = selectedJavaElements;
        fFamixModel = null;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        IStatus status = Status.OK_STATUS;
        SubMonitor progress = SubMonitor.convert(monitor, "Process selected items", 100);
        try {
            final ProjectParser projectParser = new ProjectParser(fSelectedJavaElements);
            status = projectParser.parse(progress.newChild(70));

            // process unresolved method calls
            if ((projectParser.getModel() != null) && (status.getSeverity() == IStatus.OK)) {
                UnresolvedInvocationHandler unresolvedInvocationHandler =
                        new UnresolvedInvocationHandler(projectParser.getModel(), projectParser
                                .getUnresolvedCalls());
                status = unresolvedInvocationHandler.process(progress.newChild(20));
                if (status.getSeverity() == IStatus.OK) {
                    status = unresolvedInvocationHandler.addInvocations(progress.newChild(10));
                }
                if (status.getSeverity() == IStatus.OK) {
                    FamixImporterPlugin.getDefault().setUnresolvedInvocationHandler(
                            unresolvedInvocationHandler);
                    FamixImporterPlugin.getDefault().setParser(projectParser);

                    fFamixModel = projectParser.getModel();
                }
                
            }
        } finally {
            if (monitor != null) {
                monitor.done();
            }
        }

        return status;
    }

    /**
     * Return the parsed FAMIX model
     * 
     * @return  The FAMIX model.
     */
    public FamixModel getFamixModel() {
        return fFamixModel;
    }
}
