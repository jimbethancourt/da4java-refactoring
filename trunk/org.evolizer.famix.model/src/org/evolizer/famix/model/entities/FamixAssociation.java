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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.evolizer.core.hibernate.model.api.IEvolizerModelEntity;
import org.evolizer.famix.model.FamixModelPlugin;

/**
 * Entity representing general associations between FAMIX entities
 * 
 * @author pinzger
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class FamixAssociation extends AbstractFamixObject implements IEvolizerModelEntity {
    /**
     * The logger. 
     */
    private static Logger sLogger = FamixModelPlugin.getLogManager().getLogger(FamixAssociation.class.getName());

    /**
     * The entity ID (set by Hibernate).
     */
    private Long fId;
    /**
     * The source entity of a FAMIX association.
     */
    private AbstractFamixEntity fFrom;

    /**
     * The target entity of a FAMIX association.
     */
    private AbstractFamixEntity fTo;

    /**
     * The source code statement implementing the association.
     */
    private String fStatement;

    /**
     * The default constructor.
     */
    public FamixAssociation() {
        super();
    }

    /**
     * The constructor
     * 
     * @param from The source entity.
     * @param to The target entity.
     */
    public FamixAssociation(AbstractFamixEntity from, AbstractFamixEntity to) {
        this.fFrom = from;
        this.fTo = to;
    }

    /**
     * Sets the Hibernate ID.
     * 
     * @param id The Hibernate ID.
     */
    protected void setId(Long id) {
        this.fId = id;
    }

    /**
     * Returns the Hibernate ID
     * 
     * @return Returns The Hibernate ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return fId;
    }

    /**
     * Returns the source entity.
     * 
     * @return Returns The source entity.
     */
    @ManyToOne
    public AbstractFamixEntity getFrom() {
        return fFrom;
    }

    /**
     * Sets the source entity.
     * 
     * @param from The source entity.
     */
    public void setFrom(AbstractFamixEntity from) {
        this.fFrom = from;
    }

    /**
     * Returns the target entity.
     * 
     * @return Returns The target entity.
     */
    @ManyToOne
    public AbstractFamixEntity getTo() {
        return fTo;
    }

    /**
     * Sets the target entity.
     * 
     * @param to The target entity.
     */
    public void setTo(AbstractFamixEntity to) {
        this.fTo = to;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        String hashString =
                this.getClass().getName() + HASH_STRING_DELIMITER + getFrom().getUniqueName() + HASH_STRING_DELIMITER
                        + getTo().getUniqueName();

        if (getSourceAnchor() == null) {
            sLogger.warn("HASHCODE: " + this.getClass().getName() + " association from " + getFrom().getUniqueName()
                    + " to " + getTo().getUniqueName() + " has no SourceAnchor");
        } else {
            hashString += getSourceAnchor().getFile() + HASH_STRING_DELIMITER + getSourceAnchor().getEndPos();
        }

        return hashString.hashCode();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FamixAssociation)) {
            return false;
        }
        if (!(obj.getClass().getName().equals(this.getClass().getName()))) {
            return false;
        }
        if (!getFrom().equals(((FamixAssociation) obj).getFrom())) {
            return false;
        }
        if (!getTo().equals(((FamixAssociation) obj).getTo())) {
            return false;
        }
        if (getSourceAnchor() != null) {
            return getSourceAnchor().equals(((FamixAssociation) obj).getSourceAnchor());
        } else {
            sLogger.warn("EQUALS: " + this.getClass().getName() + " association fFrom " + getFrom().getUniqueName()
                    + " fTo " + getTo().getUniqueName() + " has no SourceAnchor");
            return true;
        }
    }

    /**
     * Returns the source code statement.
     * 
     * @return The source code statement representing the association.
     */
    @Lob
    public String getStatement() {
        return fStatement;
    }

    /**
     * Sets the source code statement.
     * 
     * @param statement The source code statement.
     */
    public void setStatement(String statement) {
        this.fStatement = statement;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getType() + ": " + getFrom().toString() + " -> " + getTo().toString();
    }

    /** 
     * {@inheritDoc}
     */
    @Transient
    @Override
    public String getLabel() {
        return getType() + ": " + getFrom().getLabel() + " -> " + getTo().getLabel();
    }

    /** 
     * {@inheritDoc}
     */
    @Transient
    @Override
    public String getURI() {
        String uri =
                getType() + ": " + getFrom().getURI() + " -> " + getTo().getURI() + ": " + getSourceAnchor().getURI();
        return uri;
    }
}
