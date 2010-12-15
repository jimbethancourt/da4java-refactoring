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
package org.evolizer.famix.model.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.evolizer.core.hibernate.model.api.IEvolizerModelEntity;

/**
 * Abstract base class of FAMIX entities and associations.
 * 
 * @author pinzger
 */
@MappedSuperclass
public abstract class AbstractFamixObject implements Cloneable, IEvolizerModelEntity {

    /** Delimiter for computing the hash string. */
    protected static final String HASH_STRING_DELIMITER = ":";

    /**
     * The location of the entity/association in the source code.
     */
    private SourceAnchor fSourceAnchor;

    /**
     * List of comments - currently not supported by the FAMIX importer.
     */
    private Set<String> fComments = new HashSet<String>();

    /**
     * Set of properties.
     */
    private Set<String> fProperties = new HashSet<String>();

    /**
     * The default constructor.
     */
    public AbstractFamixObject() {
        super();
    }

    /**
     * Returns the source anchor.
     * 
     * @return The source anchor.
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = true, name = "sourceanchor_fk")
    public SourceAnchor getSourceAnchor() {
        return fSourceAnchor;
    }

    /**
     * Sets the source anchor.
     * 
     * @param sourceAnchor
     *            The source anchor.
     */
    public void setSourceAnchor(SourceAnchor sourceAnchor) {
        fSourceAnchor = sourceAnchor;
    }

    /**
     * Returns the comments.
     * 
     * @return The comments.
     */
    @Transient
    public Set<String> getComments() {
        return fComments;
    }

    /**
     * Sets the comments.
     * 
     * @param comments
     *            The comments.
     */
    public void setComments(Set<String> comments) {
        fComments = comments;
    }

    /**
     * Returns the properties.
     * 
     * @return The properties.
     */
    @Transient
    public Set<String> getProperties() {
        return fProperties;
    }

    /**
     * Sets the properties.
     * 
     * @param properties
     *            The properties.
     */
    public void setProperties(Set<String> properties) {
        fProperties = properties;
    }

    /**
     * Returns the type.
     * 
     * @return The FAMIX class name.
     */
    @Transient
    public String getType() {
        String fullClassName = this.getClass().getName();
        return fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractFamixObject clone() {
        try {
            AbstractFamixObject copy = (AbstractFamixObject) super.clone();
            return copy;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Transient
    public String getLabel() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Transient
    public String getURI() {
        // Override in subclasses
        return null;
    }
}
