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

package org.evolizer.core.util.selectionhandling;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.evolizer.core.exceptions.EvolizerException;


/**
 * This class provides a number of methods for handling with
 * selections in Eclipse.
 *
 * TODO: Refactor statics. Would be better to wrap functionality around an ISelection
 *
 * @author pinzger
 *
 */
public class JavaSelectionHelper {

    /**
     * Return the project object of the given selection.
     * 
     * @param selection The selection.
     * @return  The project object.
     * @throws EvolizerException
     */
    public static IJavaProject getProject(ISelection selection) throws EvolizerException {
        IJavaProject project = null;
        
//        if (selection instanceof ITreeSelection) {
//            ITreeSelection treeSelection = (ITreeSelection) selection;
//            // select project from first selected element
//            project = ((IJavaElement) treeSelection.getFirstElement()).getJavaProject();
//        } else 
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            project = ((IJavaElement) structuredSelection.getFirstElement()).getJavaProject();
        } else {
            throw new EvolizerException("Could not determine the project from the selection");
        }
        
        return project;
    }
    
    /**
     * Return the project object from a given Java element.
     * 
     * @param javaElement   The Java element
     * @return  The project object.
     */
    public static IJavaProject getProject(IJavaElement javaElement) {
        IJavaProject project;
        if (javaElement instanceof IJavaProject) {
            project = (IJavaProject) javaElement;
        } else {
            project = javaElement.getJavaProject();
        }

        return project;
    }

    /**
     * Returns a list of selected Java elements from the given Eclipse selection.
     * 
     * @param selection The Eclipse selection.
     * @return  The list of Java elemenst.
     * @throws EvolizerException
     */
    public static List<IJavaElement> getPackagesAndSelectedJavaElements(ISelection selection) throws EvolizerException {
        List<IJavaElement>lJavaElements = new LinkedList<IJavaElement>();
        Object elements[] = new Object[]{}; 
//        if (selection instanceof ITreeSelection) {
//            elements = ((ITreeSelection) selection).toArray();
//        } else 
        if (selection instanceof IStructuredSelection) {
            elements = ((IStructuredSelection) selection).toArray();
        }
//      if (selection instanceof ITreeSelection) {
            for (Object lElement : elements) {
                if (lElement instanceof IJavaElement) {
                    IJavaElement javaElement = (IJavaElement) lElement;
                    if (javaElement.getElementType() == IJavaElement.JAVA_PROJECT) {
//                      lJavaElements.addAll(getPackageFragmentRoots((IJavaProject) lElement));
                        for (IJavaElement rootPackage : getPackageFragmentRoots((IJavaProject) lElement)) {
                            lJavaElements.addAll(getPackages((IPackageFragmentRoot) rootPackage)); 
                        }
                    } else if (javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) { 
                        lJavaElements.addAll(getPackages((IPackageFragmentRoot) javaElement)); 
                    } else {
                        lJavaElements.add(javaElement);
                    }
                } else if (lElement instanceof IProject) {
                	IProject project = (IProject) lElement;
                	try {
                		IJavaProject javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
                        for (IJavaElement rootPackage : getPackageFragmentRoots(javaProject)) {
                            lJavaElements.addAll(getPackages((IPackageFragmentRoot) rootPackage)); 
                        }
					} catch (CoreException e) {
						e.printStackTrace();
					}
                }
            }
//      }
        
        return lJavaElements;
    }
    
    /**
     * Returns the list of root package fragments of a given Java project.
     * 
     * @param project   The Java project.
     * @return  The list of root package fragments. 
     * @throws EvolizerException
     */
    public static List<IJavaElement> getPackageFragmentRoots(IJavaProject project) throws EvolizerException {
        List<IJavaElement> sourceRootPagckages = new ArrayList<IJavaElement>();
        try {
            for (IPackageFragmentRoot root : project.getPackageFragmentRoots()) {
                if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                    sourceRootPagckages.add(root);
                }
            }
        } catch (JavaModelException jme) {
            throw new EvolizerException(jme);
        }

        return sourceRootPagckages;
    }

//    public static List<IJavaElement> getPackageFragmentRoots(IProject project) throws EvolizerException {
//        List<IJavaElement> sourceRootPagckages = new ArrayList<IJavaElement>();
//        try {
//            for (IPackageFragmentRoot root : project.getProject()etPackageFragmentRoots()) {
//                if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
//                    sourceRootPagckages.add(root);
//                }
//            }
//        } catch (JavaModelException jme) {
//            throw new EvolizerException(jme);
//        }
//
//        return sourceRootPagckages;
//    }
    
    /**
     * Returns the packages contained by the package root fragment.
     * 
     * @param root  The package root fragment
     * @return  The list of contained packages.
     * @throws EvolizerException
     */
    public static List<IJavaElement> getPackages(IPackageFragmentRoot root) throws EvolizerException {
        List<IJavaElement> packages = new ArrayList<IJavaElement>();
        
        try {
            for (IJavaElement fragment : root.getChildren()) {
                if (fragment.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                    packages.add(fragment);
                }
            }
        } catch (JavaModelException jme) {
            throw new EvolizerException(jme);
        }
        
        return packages;
    }
    
}
