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
package org.evolizer.core.util.projecthandling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * The Class JavaProjectHelper.
 * 
 * @author wuersch
 */
// TODO class javadoc
public class JavaProjectHelper {

    private IJavaProject fJavaProject;
    private IProject fProject;
    private IWorkspace fWorkspace;
    private IWorkspaceRoot fWorkspaceRoot;

    /**
     * Instantiates a new java project helper.
     */
    public JavaProjectHelper() {
        fWorkspace = ResourcesPlugin.getWorkspace();
        fWorkspaceRoot = fWorkspace.getRoot();
    }

    /**
     * Deletes the workspace.
     * 
     * @param deleteContent
     *            whether to delete the workspace's content or not
     * @param force
     *            whether to force the deletion or not
     * @param monitor
     *            the progress monitor
     * @throws CoreException
     *             if this method fails. Reasons include:
     *             <ul>
     *             <li>A project could not be deleted.</li>
     *             <li>A project's contents could not be deleted.</li>
     *             <li>Resource changes are disallowed during certain types of resource change event notification.</li>
     *             </ul>
     */
    public void deleteWorkspace(boolean deleteContent, boolean force, IProgressMonitor monitor) throws CoreException {
        fWorkspaceRoot.delete(deleteContent, force, monitor);
    }

    /**
     * Creates the project.
     * 
     * @param projectName
     *            the project name
     * @param binName
     *            the bin name
     * @param progressMonitor
     *            the progress monitor
     * @throws CoreException
     *             if this method fails. Reasons include:
     *             <ul>
     *             <li>A project could not be deleted.</li>
     *             <li>A project's contents could not be deleted.</li>
     *             <li>Resource changes are disallowed during certain types of resource change event notification.</li>
     *             </ul>
     */
    public void createProject(String projectName, String binName, IProgressMonitor progressMonitor)
            throws CoreException {

        IProgressMonitor monitor = (progressMonitor == null) ? new NullProgressMonitor() : progressMonitor;

        try {

            monitor.beginTask("Creating Java-Project: " + projectName, 7);

            fProject = fWorkspaceRoot.getProject(projectName);
            fProject.create(null);

            monitor.worked(1);

            monitor.setTaskName("Opening new Project");
            fProject.open(null);
            fJavaProject = JavaCore.create(fProject);
            monitor.worked(1);

            monitor.setTaskName("Attaching Java Nature to Project");
            setJavaNature();
            monitor.worked(1);

            monitor.setTaskName("Creating output Folder: " + binName);
            IFolder binFolder = createBinFolder(binName);
            monitor.worked(1);

            monitor.setTaskName("Attaching output Folder to Project");
            setBinFolder(binFolder);
            monitor.worked(1);

            monitor.setTaskName("Setting Classpath");
            fJavaProject.setRawClasspath(new IClasspathEntry[0], null);
            monitor.worked(1);

            monitor.setTaskName("Adding standart Java System Library to Classpath");
            addSystemLibraries();
            monitor.worked(1);

        } finally {
            monitor.done();
        }

    }

    /**
     * Adds a standard source folder.
     * 
     * @param monitor
     *            the progress monitor
     * @throws CoreException
     *             if this method fails. Reasons include:
     *             <ul>
     *             <li>A project could not be deleted.</li>
     *             <li>A project's contents could not be deleted.</li>
     *             <li>Resource changes are disallowed during certain types of resource change event notification.</li>
     *             </ul>
     */
    public void addStandartSourceFolder(IProgressMonitor monitor) throws CoreException {
        addSourceFolder("src", monitor);
    }

    /**
     * Adds a source folder.
     * 
     * @param sourceFolderName
     *            the source folder name
     * @param progressMonitor
     *            the progress monitor
     * @throws CoreException
     *             if this method fails. Reasons include:
     *             <ul>
     *             <li>A project could not be deleted.</li>
     *             <li>A project's contents could not be deleted.</li>
     *             <li>Resource changes are disallowed during certain types of resource change event notification.</li>
     *             </ul>
     */
    public void addSourceFolder(String sourceFolderName, IProgressMonitor progressMonitor) throws CoreException {

        IProgressMonitor monitor = (progressMonitor == null) ? new NullProgressMonitor() : progressMonitor;

        try {
            monitor.beginTask("Creating Source Folder: " + sourceFolderName, 2);

            IFolder folder = fProject.getFolder(sourceFolderName);
            createFolderStructure(folder);
            monitor.worked(1);

            monitor.setTaskName("Adding Source Folder to Classpath");
            IPackageFragmentRoot root = fJavaProject.getPackageFragmentRoot(folder);
            IClasspathEntry[] oldEntries = fJavaProject.getRawClasspath();
            IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
            System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
            newEntries[oldEntries.length] = JavaCore.newSourceEntry(root.getPath());
            fJavaProject.setRawClasspath(newEntries, null);
            monitor.worked(1);
        } finally {
            monitor.done();
        }
    }

    private void createFolderStructure(IFolder folder) throws CoreException {
        if (folder.getParent().exists()) {
            folder.create(false, true, null);
        } else {
            createFolderStructure((IFolder) folder.getParent());
            folder.create(false, true, null);
        }
    }

    /**
     * Adds a package.
     * 
     * @param packageName
     *            the package name
     * @param progressMonitor
     *            the progress monitor
     * @throws JavaModelException
     *             if the element could not be created. Reasons include:
     *             <ul>
     *             <li>This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
     *             <li>A CoreException occurred while creating an underlying resource</li>
     *             <li>This package fragment root is read only (READ_ONLY)</li>
     *             <li>The name is not a valid package name (INVALID_NAME)</li>
     *             </ul>
     */
    public void addPackage(String packageName, IProgressMonitor progressMonitor) throws JavaModelException {
        addPackage("src", packageName, progressMonitor);
    }

    /**
     * Adds a package.
     * 
     * @param sourceFolderName
     *            the source folder name
     * @param packageName
     *            the package name
     * @param progressMonitor
     *            the progress monitor
     * @throws JavaModelException
     *             if the element could not be created. Reasons include:
     *             <ul>
     *             <li>This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
     *             <li>A CoreException occurred while creating an underlying resource</li>
     *             <li>This package fragment root is read only (READ_ONLY)</li>
     *             <li>The name is not a valid package name (INVALID_NAME)</li>
     *             </ul>
     */
    public void addPackage(String sourceFolderName, String packageName, IProgressMonitor progressMonitor)
            throws JavaModelException {

        IProgressMonitor monitor = (progressMonitor == null) ? new NullProgressMonitor() : progressMonitor;

        try {
            monitor.beginTask("Creating Java Package " + packageName + " in " + sourceFolderName, 1);

            IFolder sourceFolder = fProject.getFolder(sourceFolderName);
            IPackageFragmentRoot javaRoot = fJavaProject.getPackageFragmentRoot(sourceFolder);
            javaRoot.createPackageFragment(packageName, false, null);

            monitor.worked(1);
        } finally {
            monitor.done();
        }
    }

    /**
     * Adds a jar.
     * 
     * @param javaProject
     *            the java project
     * @param jar
     *            the jar
     * @param progressMonitor
     *            the progress monitor
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws JavaModelException
     *             if the element could not be created. Reasons include:
     *             <ul>
     *             <li>This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
     *             <li>A CoreException occurred while creating an underlying resource</li>
     *             <li>This package fragment root is read only (READ_ONLY)</li>
     *             <li>The name is not a valid package name (INVALID_NAME)</li>
     *             </ul>
     */
    public static void addJar(IJavaProject javaProject, IPath jar, IProgressMonitor progressMonitor)
            throws IOException,
            JavaModelException {
        IProgressMonitor monitor = (progressMonitor == null) ? new NullProgressMonitor() : progressMonitor;
        try {
            monitor.beginTask("Adding Jar " + jar + " to Project " + javaProject.getProject().getName(), 1);
            IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
            IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
            System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
            newEntries[oldEntries.length] = JavaCore.newLibraryEntry(jar, null, null);
            javaProject.setRawClasspath(newEntries, null);
            monitor.worked(1);
        } finally {
            monitor.done();
        }
    }

    /**
     * Adds a source file.
     * 
     * @param packageName
     *            the package name
     * @param fileName
     *            the file name
     * @param stream
     *            the stream
     * @param progressMonitor
     *            the progress monitor
     * @throws JavaModelException
     *             if the element could not be created. Reasons include:
     *             <ul>
     *             <li>This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
     *             <li>A CoreException occurred while creating an underlying resource</li>
     *             <li>This package fragment root is read only (READ_ONLY)</li>
     *             <li>The name is not a valid package name (INVALID_NAME)</li>
     *             </ul>
     */
    public void addSourceFile(String packageName, String fileName, InputStream stream, IProgressMonitor progressMonitor)
            throws JavaModelException {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
        String line = null;
        StringBuilder content = new StringBuilder();
        try {
            while ((line = buffer.readLine()) != null) {
                content.append(line);
                content.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        addSourceFile(packageName, fileName, content.toString(), progressMonitor);
    }

    /**
     * Adds the source file.
     * 
     * @param packageName
     *            the package name
     * @param fileName
     *            the file name
     * @param content
     *            the content
     * @param progressMonitor
     *            the progress monitor
     * @throws JavaModelException
     *             if the element could not be created. Reasons include:
     *             <ul>
     *             <li>This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
     *             <li>A CoreException occurred while creating an underlying resource</li>
     *             <li>This package fragment root is read only (READ_ONLY)</li>
     *             <li>The name is not a valid package name (INVALID_NAME)</li>
     *             </ul>
     */
    public void addSourceFile(String packageName, String fileName, String content, IProgressMonitor progressMonitor)
            throws JavaModelException {
        addSourceFile("src", packageName, fileName, content, progressMonitor);
    }

    /**
     * Adds the source file.
     * 
     * @param sourceFolder
     *            the source folder
     * @param packageName
     *            the package name
     * @param fileName
     *            the file name
     * @param content
     *            the content
     * @param progressMonitor
     *            the progress monitor
     * @throws JavaModelException
     *             if the element could not be created. Reasons include:
     *             <ul>
     *             <li>This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
     *             <li>A CoreException occurred while creating an underlying resource</li>
     *             <li>This package fragment root is read only (READ_ONLY)</li>
     *             <li>The name is not a valid package name (INVALID_NAME)</li>
     *             </ul>
     */
    public void addSourceFile(
            String sourceFolder,
            String packageName,
            String fileName,
            String content,
            IProgressMonitor progressMonitor) throws JavaModelException {

        IProgressMonitor monitor = (progressMonitor == null) ? new NullProgressMonitor() : progressMonitor;
        try {
            monitor.beginTask("Adding Source File " + fileName + " to Package " + packageName, 1);
            IFolder folder = fProject.getFolder(sourceFolder);
            IPackageFragmentRoot javaRoot = fJavaProject.getPackageFragmentRoot(folder);
            IPackageFragment packageFragment = javaRoot.getPackageFragment(packageName);
            packageFragment.createCompilationUnit(fileName, content, false, null);
        } finally {
            monitor.done();
        }
    }

    /**
     * Returns the java project.
     * 
     * @return the java project
     */
    public IJavaProject getJavaProject() {
        return fJavaProject;
    }

    private void addSystemLibraries() throws JavaModelException {
        IClasspathEntry[] oldEntries = fJavaProject.getRawClasspath();
        IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
        newEntries[oldEntries.length] = JavaRuntime.getDefaultJREContainerEntry();
        fJavaProject.setRawClasspath(newEntries, null);
    }

    private void setJavaNature() throws CoreException {
        IProjectDescription description = fProject.getDescription();
        description.setNatureIds(new String[]{JavaCore.NATURE_ID});
        fProject.setDescription(description, null);
    }

    private IFolder createBinFolder(String name) throws CoreException {
        IFolder binFolder = fProject.getFolder(name);
        binFolder.create(false, true, null);
        return binFolder;
    }

    private void setBinFolder(IFolder binFolder) throws JavaModelException {
        IPath outputLocation = binFolder.getFullPath();
        fJavaProject.setOutputLocation(outputLocation, null);
    }
}
