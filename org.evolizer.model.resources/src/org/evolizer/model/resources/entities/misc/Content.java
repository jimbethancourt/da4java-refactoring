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
package org.evolizer.model.resources.entities.misc;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.evolizer.core.hibernate.model.api.IEvolizerModelEntity;

/**
 * Wrapper for e.g., source code. This is needed because Hibernate cannot load primitive data types and Strings lazily.
 * 
 * @author wuersch
 */
@Entity
public class Content implements IEvolizerModelEntity {

    private Long fId;
    private String fDescriptor;
    private String fSource;

    /**
     * Instantiates a new content.
     */
    public Content() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return fId;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            the new id
     */
    public void setId(Long id) {
        this.fId = id;
    }

    /**
     * Returns the descriptor.
     * 
     * @return the descriptor
     */
    public String getDescriptor() {
        return fDescriptor;
    }

    /**
     * Sets the descriptor.
     * 
     * @param descriptor
     *            the new descriptor
     */
    public void setDescriptor(String descriptor) {
        this.fDescriptor = descriptor;
    }

    /**
     * Returns the source.
     * 
     * @return the source
     */
    @Lob
    @Column(length = 2000000000)
    public String getSource() {
        return fSource;
    }

    /**
     * Sets the source.
     * 
     * @param source
     *            the new source
     */
    public void setSource(String source) {
        this.fSource = source;
    }

    /**
     * {@inheritDoc}
     */
    @Transient
    public String getLabel() {
        return fDescriptor;
    }

    /**
     * {@inheritDoc}
     */
    @Transient
    public String getURI() {
        return null;
    }
}
