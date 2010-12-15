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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.evolizer.core.hibernate.model.api.IEvolizerModelEntity;

/**
 * This class represents files or directories according to the posix standard.
 * 
 * @author wuersch
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class File implements IEvolizerModelEntity {

    private String fPath;
    private Directory fParentDirectory;
    /**
     * Unique ID, used by Hibernate.
     */
    private Long fId;

    /**
     * Constructor.
     * 
     * @param path
     *            the fully qualified path of the file or directory.
     * @param parentDirectory
     *            the directory/folder that contains the actual file or directory. Use {@link Directory#ROOT} if it is a
     *            top-level file/directory (the root directory does not need to be created explicitly, as we assume that
     *            it always exists). In case of non-unix systems, paths should be converted into a unix-like
     *            representation. For example: <code>C:\Java\MyClass.java</code> should be converted to
     *            <code>/C/Java/MyClass.java</code>.
     */
    public File(String path, Directory parentDirectory) {
        this();
        fPath = path;
        fParentDirectory = parentDirectory;

        if (parentDirectory != null) {
            parentDirectory.add(this);
        }
    }

    /**
     * Default constructor for Hibernate.
     */
    protected File() {
        super();
    }

    /**
     * Unique ID, used by Hibernate.
     * 
     * @return unique Hibernate ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return fId;
    }

    /**
     * Set unique ID of Hibernate.
     * 
     * @param id
     *            to set
     */
    protected void setId(Long id) {
        fId = id;
    }

    /**
     * Returns the fully qualified path of the file or directory.
     * 
     * @return a {@link String} containing a fully qualified path, for example <code>/C/Java/Documents/Letter.doc</code>
     *         .
     */
    public String getPath() {
        return fPath;
    }

    /**
     * Sets the fully qualified path of this file or directory.
     * 
     * @param aPath
     *            fully qualified path (e.g. <code>/C/Java/Documents/Letter.doc</code>)
     */
    public void setPath(String aPath) {
        fPath = aPath;
    }

    /**
     * Returns the parent directory of the actual file or directory or <code>null</code> in case of the root directory.
     * 
     * @return the {@link Directory} that contains this file or directory.
     */
    @ManyToOne
    public Directory getParentDirectory() {
        return fParentDirectory;
    }

    /**
     * Sets the parent directory of this file or directory.
     * 
     * @param aParentDirectory
     *            the {@link Directory} that contains this file or directory
     */
    public void setParentDirectory(Directory aParentDirectory) {
        fParentDirectory = aParentDirectory;
    }

    /**
     * Returns name of a file or directory, i.e., the last segment of the fully qualified path.
     * 
     * @return a {@link String} that contains the name of the actual file or directory.
     */
    @Transient
    public String getName() {
        return fPath.substring(fPath.lastIndexOf('/') + 1, fPath.length());
    }

    /**
     * {@inheritDoc}
     */
    @Transient
    public String getLabel() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Transient
    public String getURI() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fPath == null) ? 0 : fPath.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        File other = (File) obj;
        if (fPath == null) {
            if (other.fPath != null) {
                return false;
            }
        } else if (!fPath.equals(other.fPath)) {
            return false;
        }
        return true;
    }
}
