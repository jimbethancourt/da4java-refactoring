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

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.evolizer.core.hibernate.model.api.IEvolizerModelEntity;
import org.evolizer.famix.model.FamixModelPlugin;

/**
 * Container holding references to the parsed entities and associations. Provides functionality to manage the container
 * and to store it into a Hibernate mapped database.
 * 
 * @author pinzger
 */
@Entity
public class FamixModel implements IEvolizerModelEntity {
    /**
     * The logger. 
     */
    private static Logger sLogger = FamixModelPlugin.getLogManager().getLogger(FamixModel.class.getName());

    /**
     * The Hibernate ID of the FAMIX entity (set by Hibernate).
     */
    private Long fId;

    /**
     * The name of the parsed FAMIX model, e.g., the Java program name.
     */
    private String fName;

    /**
     * The date when the model has been created
     */
    private Date fCreated;

  
    private Set<AbstractFamixEntity> fFamixEntities = new HashSet<AbstractFamixEntity>();
    
    private Set<FamixAssociation> fFamixAssociations = new HashSet<FamixAssociation>();
    
    /**
     * FAMIX entity storage - Maps each parsed entity to its corresponding bundle object.
     */
    private Map<AbstractFamixEntity, Bundle> fFamixToBundleMap = new Hashtable<AbstractFamixEntity, Bundle>();

    /**
     * The default constructor.
     */
    @SuppressWarnings("unused")
    private FamixModel() {
    }
    
    /**
     * @param name
     * @param created
     */
    public FamixModel(String name, Date created) {
        fName = name;
        fCreated = created;
    }

    /**
     * Sets the Hibernate ID.
     * 
     * @param id
     *            Hibernate ID.
     */
    @SuppressWarnings("unused")
    private void setId(Long id) {
        fId = id;
    }
    
    /**
     * Returns the Hibernate ID.
     * 
     * @return The Hibernate ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return fId;
    }
    /**
     * Return the name of the model.
     * 
     * @return The name.
     */
    public String getName() {
        return fName;
    }
    /**
     * Set the name of the model.
     * 
     * @param name The name.
     */
    public void setName(String name) {
        this.fName = name;
    }
    /**
     * Return the date when the model has been created.
     * 
     * @return The date.
     */
    public Date getCreated() {
        return fCreated;
    }
    /**
     * Sets the date when the model has been created.
     * 
     * @param created The date.
     */
    public void setCreated(Date created) {
        this.fCreated = created;
    }

    /**
     * Return the FAMIX entities.
     * 
     * @return  The set of entities.
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "Model_FamixEntity", joinColumns = { @JoinColumn(name = "model_id") }, inverseJoinColumns = @JoinColumn(name = "entity_id"))
    public Set<AbstractFamixEntity> getFamixEntities() {
        return fFamixEntities;
    }
    
    /**
     * Set the FAMIX entities.
     * 
     * @param entities  The set of entities.
     */
    public void setFamixEntities(Set<AbstractFamixEntity> entities) {
        fFamixEntities = entities;
    }
    
    /**
     * Return the set FAMIX associations.
     * 
     * @return  The set of FAMIX associations.
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "Model_Association", joinColumns = { @JoinColumn(name = "model_id") }, inverseJoinColumns = @JoinColumn(name = "association_id"))
    public Set<FamixAssociation> getFamixAssociations() {
        return fFamixAssociations;
    }
    
    /**
     * Set the FAMIX associations.
     * 
     * @param associations  The set of associations.
     */
    public void setFamixAssociations(Set<FamixAssociation> associations) {
        fFamixAssociations = associations;
    }

    /** 
     * {@inheritDoc}
     */
    @Transient
    public String getLabel() {
        return this.getName();
    }

    /** 
     * {@inheritDoc}
     */
    @Transient
    public String getURI() {
        return this.getName() + ":" + this.getCreated().toString();
    }

    
    /**
     * Data bundle associated with an element. Contains the entity and its association relationships.
     */
    private class Bundle {
        /**
         * The FAMIX entity.
         */
        private AbstractFamixEntity fEntity;
        /**
         * The set of associations of the FAMIX entity.
         */
        private Set<FamixAssociation> fRelations;

        /**
         * The constructor.
         * 
         * @param famixEntity The FAMIX entity.
         */
        public Bundle(AbstractFamixEntity famixEntity) {
            fEntity = famixEntity;
            fRelations = new HashSet<FamixAssociation>();
        }

        /**
         * Returns the FAMIX entity.
         * 
         * @return The FAMIX entity.
         */
        public AbstractFamixEntity getEntity() {
            return fEntity;
        }

        /**
         * Return the in-coming and outgoing associations.
         * 
         * @return The set of associations.
         */
        public Set<FamixAssociation> getAssociations() {
            return fRelations;
        }
    }
    
    /**
     * Checks whether the model contains the given FAMIX entity.
     * 
     * @param famixEntity The FAMIX entity.
     * @return True, if the model contains the entity.
     */
    public boolean contains(AbstractFamixEntity famixEntity) {
        if (famixEntity == null) {
            return false;
        }
        return fFamixToBundleMap.containsKey(famixEntity);
    }

    /**
     * Returns the associations of the given FAMIX entity. The set
     * includes in-coming and outgoing associations.
     * 
     * @param famixEntity The FAMIX entity.
     * @return The in-coming and outgoing associations.
     */
    @Transient
    public Set<FamixAssociation> getAssociations(AbstractFamixEntity famixEntity) {
        return fFamixToBundleMap.get(famixEntity).getAssociations();
    }

    /**
     * Returns the bundle of the given FAMIX entity.
     * 
     * @param famixEntity The FAMIX entity.
     * @return The bundle of the given FAMIX entity.
     */
    @Transient
    private Bundle getBundle(AbstractFamixEntity famixEntity) {
        return fFamixToBundleMap.get(famixEntity);
    }

    /**
     * Returns the corresponding FAMIX entity from the model container.
     * 
     * @param famixEntity The FAMIX entity.
     * @return The corresponding FAMIX entity as contained by the model object.
     */
    @Transient
    public AbstractFamixEntity getElement(AbstractFamixEntity famixEntity) {
        if (famixEntity == null) {
            return null;
        }

        AbstractFamixEntity entity = null;
        Bundle lBundle = getBundle(famixEntity);
        if (lBundle != null) {
            entity = lBundle.getEntity();
        }

        return entity;
    }

//    /**
//     * Returns the FAMIX entity map.
//     * 
//     * @return The FAMIX entity map.
//     */
//    @Transient
//    public Map<AbstractFamixEntity, Bundle> getElements() {
//        return fFamixToBundleMap;
//    }

//    /**
//     * Returns the set of FAMIX entities contained by the model object.
//     * 
//     * @return The set of FAMIX entities contained by the model.
//     */
//    @Transient
//    public Set<AbstractFamixEntity> getFamixEntities() {
//        Set<AbstractFamixEntity> entities = new HashSet<AbstractFamixEntity>();
//        for (Bundle bundle : fFamixToBundleMap.values()) {
//            entities.add(bundle.getEntity());
//        }
//        return entities;
//    }

    /**
     * Adds an element in the model. The element is initialized with an empty relation set. If the element is already in
     * the database, nothing happens.
     * 
     * @param famixEntity The entity to add.
     * @return The entity contained by the model, otherwise the given entity.
     */
    public AbstractFamixEntity addElement(AbstractFamixEntity famixEntity) {
        if (famixEntity == null) {
            return null;
        }
        if (!fFamixToBundleMap.containsKey(famixEntity)) {
            fFamixToBundleMap.put(famixEntity, new Bundle(famixEntity));
            if (! fFamixEntities.contains(famixEntity)) {
                fFamixEntities.add(famixEntity);
            }
            return famixEntity;
        } else {
            return getElement(famixEntity);
        }
    }

    /**
     * Adds an association to the model. If from and to entities do not exists nothing happens.
     * 
     * @param association The association to add.
     */
    public void addRelation(FamixAssociation association) {
        if (association == null) {
            return;
        }

        AbstractFamixEntity lFamixEntityFrom = getElement(association.getFrom());
        AbstractFamixEntity lFamixEntityTo = getElement(association.getTo());

        if (lFamixEntityFrom == null) {
            sLogger.error("From entity not found: " + association.getType());
            return;
        }
        if (lFamixEntityTo == null) {
            sLogger.error("To entity not found: " + association.getType());
            return;
        }

        association.setFrom(lFamixEntityFrom);
        association.setTo(lFamixEntityTo);
        if (!getAssociations(lFamixEntityFrom).contains(association)) {
            getAssociations(lFamixEntityFrom).add(association);
            getAssociations(lFamixEntityTo).add(association);
            
            if (!fFamixAssociations.contains(association)) {
                fFamixAssociations.add(association);
            }
        }
    }

    /**
     * Remove an element and all its direct and transpose relations.
     * 
     * @param famixEntity The element to remove. Must not be null and must exist in the database.
     */
//    public void removeElement(AbstractFamixEntity famixEntity) {
//        if (contains(famixEntity)) {
//            Set<FamixAssociation> associations = getAssociations(famixEntity);
//            for (FamixAssociation lRelation : associations) {
//                AbstractFamixEntity toEntity = lRelation.getTo();
//                if (!toEntity.equals(famixEntity)) {
//                    getAssociations(toEntity).remove(lRelation);
//                    fFamixAssociations.remove(lRelation);
//                }
//            }
//
//            fFamixToBundleMap.remove(famixEntity);
//            fFamixEntities.remove(famixEntity);
//        }
//    }
}
