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
package org.evolizer.core.util.resourcehandling;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

/**
 * This class provides some utility methods to create file handlers. While the handlers are associated to an Eclipse
 * project, the files themselves are stored in the system's temp directory.
 * 
 * @author wuersch
 */
public class EvolizerFileHandler {

    /**
     * System wide temp directory. Fetched via Java properites.
     */
    private static final String SYS_TMP_DIR = System.getProperty("java.io.tmpdir");

    /**
     * Default temp dir inside the project.
     */
    private static final String TMP_DIR = ".evolizer.tmp";

    /**
     * Project in which files are created, opened, deleted, and handeled.
     */
    private IProject fProject;

    /**
     * Constructor to initialize file handler with a <code>IProject</code>.
     * 
     * @param project
     *            the project
     */
    public EvolizerFileHandler(IProject project) {
        fProject = project;
    }

    /**
     * Initialize and open file handler.
     */
    public void open() {
        // Create temp folder structure
        File actualTmp = new File(new File(SYS_TMP_DIR), "evolizer");
        actualTmp.mkdir();

        IFolder evoTmp = fProject.getFolder(TMP_DIR);
        try {
            if (!evoTmp.exists()) {
                evoTmp.createLink(new Path(actualTmp.getAbsolutePath()), 0, null);
            }

            if (!evoTmp.exists()) {
                evoTmp.create(true /*force*/, true /*local*/, null);
            }
        } catch (CoreException e) {
            // TODO evolizer error handling ?
            e.printStackTrace();
        }
    }

    /**
     * Create a file inside the temp folder of project.
     * 
     * @param fileName
     *            of file
     * @param content
     *            of file
     * @return IFile handle to newly created file
     */
    public IFile createFile(String fileName, String content) {
        IFolder evoTmp = fProject.getFolder(TMP_DIR);
        IFile result = evoTmp.getFile(fileName);
        try {
            result.create(new ByteArrayInputStream(content.getBytes()), true /*force*/, null /*monitor*/);
        } catch (CoreException e) {
            // TODO evolizer error handling ?
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Closes this file handler.
     */
    public void close() {
        cleanup();
    }

    /**
     * Cleans up all created folder structures and files.
     */
    public void cleanup() {
        // clean up project folder
        IFolder evoTmp = fProject.getFolder(TMP_DIR);
        if (evoTmp.exists()) {
            try {
                evoTmp.delete(true /* force */, false /* no history */, null /* progress monitor */);
            } catch (CoreException e) {
                // TODO evolizer error handling
                e.printStackTrace();
            }
        }
        // clean up system tmp directory
        File actualTmp = new File(new File(SYS_TMP_DIR), "evolizer");
        if (actualTmp.exists()) {
            deleteAll(actualTmp);
        }
    }

    /**
     * Deletes directory recursively.
     * 
     * @param dir
     *            to delete recursively
     */
    private void deleteAll(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String element : children) {
                deleteAll(new File(dir, element));
            }
        }
        dir.delete();
    }
}
