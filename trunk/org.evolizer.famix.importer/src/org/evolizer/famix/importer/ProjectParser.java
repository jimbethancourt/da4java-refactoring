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
package org.evolizer.famix.importer;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.evolizer.core.util.selectionhandling.JavaSelectionHelper;
import org.evolizer.famix.importer.unresolved.UnresolvedMethodInvocation;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.entities.FamixModel;
import org.evolizer.famix.model.entities.FamixPackage;

/**
 * Main entry point for extracting a FAMIX source model from an Eclipse Java project, 
 * packages, and compilation units.
 * 
 * It supports the selection of single and multiple projects, source folders, packages 
 * and compilation units.
 * 
 * @author pinzger
 */
public class ProjectParser {
    /**
     * The logger. 
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(ProjectParser.class.getName());

    /**
     * The container holding the extracted FAMIX entities and associations.
     */
    private FamixModel fModel;
    /**
     * The factory to use for creating instances of FAMIX entities and associations.
     */
    private FamixModelFactory fFactory;
    /**
     * The AST visitor to extract the FAMIX entities and associations from the AST of a Java compilation unit.
     */
    private ASTCrawler fAnalyzer;
    /**
     * The list of method, this, super, and class instance creation invocations that could not be resolved.
     */
    private Hashtable<FamixMethod, List<UnresolvedMethodInvocation>> fUnresolvedCalls =
            new Hashtable<FamixMethod, List<UnresolvedMethodInvocation>>();

    /**
     * Selected IJavaElements.
     */
    private List<IJavaElement> fSelection;
    
    /**
     * Prevents parsing of Java elements twice
     */
    private Set<IJavaElement> fParsedElements;

    /**
     * Creates a new instance of the ProjectParser. Initializes the FamixModel, FamixModelFactory, and the Analyzer.
     * 
     * @param pSelection
     *            List of selected IJavaElements.
     */
    public ProjectParser(List<IJavaElement> pSelection) {
        setSelection(pSelection);
        
        String modelName = JavaSelectionHelper.getProject(pSelection.get(0)).getElementName();
        Date currentTime = Calendar.getInstance().getTime();
        
        fModel = new FamixModel(modelName, currentTime);
        fFactory = new FamixModelFactory();
        fAnalyzer = new ASTCrawler(fModel, fFactory);
    }

    /**
     * Dispatcher method for handling selected Java elements.
     * 
     * @param monitor
     *            Progress monitor.
     * @return status.
     */
    public IStatus parse(IProgressMonitor monitor) {
        fParsedElements = new HashSet<IJavaElement>();
        IStatus status = Status.OK_STATUS;

        SubMonitor progress = SubMonitor.convert(monitor, 100);
        progress.setTaskName("Parse selected items");
        SubMonitor loopProgress = progress.newChild(100).setWorkRemaining(getSelection().size());

        try {
            for (IJavaElement javaElement : getSelection()) {
                switch (javaElement.getElementType()) {
                    case IJavaElement.JAVA_PROJECT:
                        status = parseProject((IJavaProject) javaElement, loopProgress.newChild(1));
                        break;
                    case IJavaElement.PACKAGE_FRAGMENT:
                        status = parsePackageFragment((IPackageFragment) javaElement, loopProgress.newChild(1));
                        break;
                    case IJavaElement.COMPILATION_UNIT:
                        status = parseCompilationUnit((ICompilationUnit) javaElement, loopProgress.newChild(1));
                        break;
                    default:
                        sLogger.error("Parsing of this Java element is not supported.");
                        break;
                }

                if (status.getSeverity() == IStatus.ERROR) {
                    sLogger.error("Error in parsing " + javaElement + " - continuing with next selected element.");
                } else if (status.getSeverity() == IStatus.CANCEL) {
                    sLogger.info("Parsing job canceled");
                    break;
                }
            }
        } finally {
            if (monitor != null) {
                monitor.done();
                monitor.subTask("");
            }
        }

        return status;
    }

    /**
     * Main parsing method. Traverses Eclipse Java project tree, creates package structure and parses each contained
     * compilation unit.
     * 
     * @param project
     *            Selected Eclipse Java project.
     * @param monitor
     *            Progress monitor.
     * @return Status.
     */
    protected IStatus parseProject(IJavaProject project, IProgressMonitor monitor) {
        sLogger.debug("Processing Java-Project: " + project.getElementName());
        IStatus status = Status.OK_STATUS;

        // try {
        List<IPackageFragment> fragments = getSourcePackages(project);
        SubMonitor progress = SubMonitor.convert(monitor, 100);
        progress.setTaskName("Parse project " + project.getElementName());
        SubMonitor loopProgress = progress.newChild(100).setWorkRemaining(fragments.size());
        // float tick = 100F / fragments.size();
        // int workDone = 0;

        for (IPackageFragment lFragment : fragments) {
            status = parsePackageFragment(lFragment, loopProgress.newChild(1));
            // workDone += tick;
            // progress.setWorkRemaining(100-workDone);

            if (status.getSeverity() == IStatus.CANCEL) {
                break;
            }
        }
        // } catch (JavaModelException e) {
        // sLogger.error("Can not get package fragments of the Java-Project " +
        // project.getElementName()+": "+e.getMessage(),e);
        // }

        sLogger.debug("Processing of Java-Project " + project.getElementName() + " complete");
        return status;
    }

    /**
     * Return the list of source packages of an Eclipse Java project.
     * 
     * @param project
     *            Eclipse Java project.
     * @return List of source packages.
     */
    private List<IPackageFragment> getSourcePackages(IJavaProject project) {
        List<IPackageFragment> fragments = new LinkedList<IPackageFragment>();

        try {
            for (IPackageFragment lFragment : project.getPackageFragments()) {
                if (lFragment.getKind() == IPackageFragmentRoot.K_SOURCE) {
                    fragments.add(lFragment);
                }
            }
        } catch (JavaModelException e) {
            sLogger.error("Error determining package fragments from Java project " + project.getElementName());
            e.printStackTrace();
        }

        return fragments;
    }

    /**
     * Parses the given package fragment and contained compilation units. Also creates the FAMIX package and directory
     * hierarchy.
     * 
     * @param pFragment
     *            Source package to be parsed.
     * @param monitor
     *            Progress monitor.
     * @return Status.
     */
    protected IStatus parsePackageFragment(IPackageFragment pFragment, IProgressMonitor monitor) {
        IStatus status = Status.OK_STATUS;
        if (! fParsedElements.contains(pFragment)) {
            sLogger.debug("Processing package fragment " + pFragment.getElementName());
    
            try {
                ICompilationUnit[] compilationUnits = pFragment.getCompilationUnits();
                SubMonitor progress = SubMonitor.convert(monitor, 100);
                progress.setTaskName("Parse package " + pFragment.getElementName());
                SubMonitor loopProgress = progress.newChild(100).setWorkRemaining(compilationUnits.length);
    
                String lPackageID = pFragment.getElementName();
                if ((lPackageID == null) || lPackageID.equals("")) {
                    lPackageID = AbstractFamixEntity.DEFAULT_PACKAGE_NAME;
                }
    
                FamixPackage lPackage = fFactory.createPackage(lPackageID, null);
                fModel.addElement(lPackage);
//                lPackage = (FamixPackage) fModel.addElement(lPackage);
                // set parent package
    //            if (lPackageID.lastIndexOf(".") > -1) {
    //                String lParentPackageID = lPackageID.substring(0, lPackageID.lastIndexOf("."));
    //                FamixPackage lParentPackage = (FamixPackage) fModel.getElement(fFactory.createPackage(lParentPackageID, null));
    //                if (lParentPackage != null) {
    //                    lParentPackage.getPackages().add(lPackage);
    //                    lPackage.setParent(lParentPackage);
    //                }
    //            }
    
                for (ICompilationUnit compilationUnit : compilationUnits) {
                    status = parseCompilationUnit(compilationUnit, loopProgress.newChild(1));
                    if (status.getSeverity() == IStatus.ERROR) {
                        break;
                    } else if (status.getSeverity() == IStatus.CANCEL) {
                        break;
                    }
                }
                
                fParsedElements.add(pFragment);
            } catch (JavaModelException e) {
                sLogger.error("Error while processsing compilation units of " + pFragment.getElementName(), e);
            }
        }

        return status;
    }

    /**
     * Parses an Eclipse compilation unit. FAMIX entities and associations are added to the FamixModel instance. Unresolved
     * calls are added.
     * 
     * @param cu
     *            Eclipse Java compilation unit.
     * @param monitor
     *            Progress monitor.
     * @return Status.
     */
    protected IStatus parseCompilationUnit(ICompilationUnit cu, IProgressMonitor monitor) {
        IStatus status = Status.OK_STATUS;

        if (! fParsedElements.contains(cu)) {
            sLogger.debug("Processing compilation unit " + cu.getElementName() + ": " + cu.getPath());
    
            SubMonitor progress = SubMonitor.convert(monitor, 100);
            progress.subTask("File " + cu.getElementName());
    
            try {
                status = fAnalyzer.analyze(cu, progress);
    
                if (fAnalyzer.getUnresolvedCalls().size() > 0) {
                    fUnresolvedCalls.putAll(fAnalyzer.getUnresolvedCalls());
                }
                
                fParsedElements.add(cu);
            } catch (OperationCanceledException oce) {
                status = Status.CANCEL_STATUS;
            }
        }

        return status;
    }

    /**
     * Returns the FAMIX model.
     * 
     * @return The FAMIX model.
     */
    public FamixModel getModel() {
        return fModel;
    }

    /**
     * Returns the table of unresolved method invocations. 
     * 
     * @return the table of unresolved method invocations.
     */
    public Hashtable<FamixMethod, List<UnresolvedMethodInvocation>> getUnresolvedCalls() {
        return fUnresolvedCalls;
    }

    /**
     * Sets the current selection.
     * 
     * @param selection
     *            The current selection.
     */
    private void setSelection(List<IJavaElement> selection) {
        fSelection = selection;
    }

    /**
     * Returns the current selection.
     * 
     * @return The current selection.
     */
    public List<IJavaElement> getSelection() {
        return fSelection;
    }
}
