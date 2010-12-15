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
package org.evolizer.model.resources.entities.fs;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.evolizer.model.resources.entities.misc.IHierarchicalElement;

/**
 * This class represents a directory/folder in a file system.
 * 
 * @author wuersch
 */
@Entity
public class Directory extends File implements IHierarchicalElement<File> {

    /**
     * Denotes the root of the file system, i.e., <code>/</code> in unix-like systems.
     */
    public static final Directory ROOT = new Directory("/", null);

    private Set<File> fChildren;

    /**
     * Constructor.
     * 
     * @param path
     *            the fully qualified path of the directory. The ending slash has to be omitted.
     * @param parentDirectory
     *            the directory/folder that contains the actual directory. Use {@link Directory#ROOT} if it is a
     *            top-level directory (the root directory does not need to be created explicitly, as we assume that it
     *            always exists). In case of non-unix systems, paths should be converted into a unix-like
     *            representation. For example: <code>C:\Java\Documents</code> should be converted to
     *            <code>/C/Java/Documents</code>.
     */
    public Directory(String path, Directory parentDirectory) {
        super(path, parentDirectory);
        fChildren = new HashSet<File>();
    }

    /**
     * Default constructor for Hibernate.
     */
    @SuppressWarnings("unused")
    private Directory() {
        super();
    }

    /**
     * Returns the files and sub-directories that are contained within the actual directory.
     * 
     * @return a {@link Set} of instances of {@link File} or {@link Directory} that are contained within the actual
     *         directory.
     */
    @OneToMany(cascade = CascadeType.ALL)
    public Set<File> getChildren() {
        return fChildren;
    }

    /**
     * Sets the children of this directory.
     * 
     * @param content
     *            the new children
     */
    public void setChildren(Set<File> content) {
        fChildren = content;
    }

    /**
     * Returns the content of this directory.
     * 
     * @return the content
     */
    @Transient
    public Set<File> getContent() {
        return getChildren();
    }

    /**
     * Adds a file or sub-directory to the actual directory.
     * 
     * @param child
     *            An instance of {@link File} or {@link Directory} that is contained within the actual directory.
     */
    @Transient
    public void add(File child) {
        fChildren.add(child);
    }
}
