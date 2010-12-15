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
package org.evolizer.model.resources.entities.humans;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

import org.evolizer.core.hibernate.model.api.IEvolizerModelEntity;

/**
 * A {@link Person} can have different roles in the development process. For example 'Developer', 'Bug Reporter', etc.
 * Concrete subclasses should be implemented in other models, to prevent the resource model from having to many
 * dependencies.
 * 
 * @author wuersch
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Role implements IEvolizerModelEntity {

    /**
     * The Constant UNKNOWN.
     */
    public static final Role UNKNOWN = new Role("unknown");

    private String fDescription;
    /**
     * Unique ID, used by Hibernate.
     */
    private Long fId;

    /**
     * Constructor. The description should be something short and meaningful, e.g., 'Developer'. Longer strings should
     * be avoided because the description is intended to be used as label (@see Role#getLabel) that represents the role
     * in some user interface.
     * 
     * @param description
     *            the description
     */
    public Role(String description) {
        this();
        this.fDescription = description;
    }

    /**
     * Needed for Hibernate.
     */
    private Role() {
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
        this.fId = id;
    }

    /**
     * Returns the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return fDescription;
    }

    /**
     * Sets the description.
     * 
     * @param description
     *            the description
     */
    public void setDescription(String description) {
        this.fDescription = description;
    }

    /**
     * {@inheritDoc}
     */
    @Transient
    public String getLabel() {

        return fDescription;
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
        result = prime * result + ((fDescription == null) ? 0 : fDescription.hashCode());
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
        Role other = (Role) obj;
        if (fDescription == null) {
            if (other.fDescription != null) {
                return false;
            }
        } else if (!fDescription.equals(other.fDescription)) {
            return false;
        }
        return true;
    }
}
