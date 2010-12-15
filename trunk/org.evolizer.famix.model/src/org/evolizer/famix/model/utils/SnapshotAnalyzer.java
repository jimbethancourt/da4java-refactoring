/*
 * Copyright 2009 Martin Pinzger, Delft University of Technology,
 * and University of Zurich, Switzerland
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
package org.evolizer.famix.model.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.evolizer.core.exceptions.EvolizerException;
import org.evolizer.core.exceptions.EvolizerRuntimeException;
import org.evolizer.core.hibernate.session.api.IEvolizerSession;
import org.evolizer.famix.model.FamixModelPlugin;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.model.resources.entities.misc.IHierarchicalElement;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;


/**
 * Encapsulates the functionality to query FAMIX entities and associations via Hibernate.
 * The SnapshotAnalyzer is initialized with the corresponding project, that contains
 * the Hibernate properties for the database access.
 * 
 * @author Katja Graefenhain, Martin Pinzger
 */
@SuppressWarnings("restriction")
public class SnapshotAnalyzer {
    protected Logger fLogger = FamixModelPlugin.getLogManager().getLogger(getClass().getName());

    private IEvolizerSession fSession;

    /**
     * The constructor.
     * 
     * @param session   Evolizer session.
     */
    public SnapshotAnalyzer(IEvolizerSession session) {
        fSession = session;
    }

    /**
     * Query incoming or outgoing FAMIX associations of the given type and set of entities.
     * 
     * @param entities	The set of entities.
     * @param associationType	FamixAssociation type - if null all associations are queried.
     * @param direction	The direction of associations either "from" (i.e., outgoing) or "to" (i.e., incoming).
     * @return	The list of associations.
     * 
     * TODO: Handle null values for direction or introduce constants
     */
    @SuppressWarnings("unchecked")
    public <T extends FamixAssociation> List<T> queryAssociationsOfEntities(
            Collection<? extends AbstractFamixEntity> entities, 
            java.lang.Class<T> associationType, 
            String direction) throws EvolizerRuntimeException {
        associationType = (associationType != null) ? associationType : (Class<T>) org.evolizer.famix.model.entities.FamixAssociation.class;

        String oppositeDirection = "";
        if (direction.equals("from")) {
            oppositeDirection = "to";
        } else if (direction.equals("to")) {
            oppositeDirection = "from";
        }

        List<T> associations = new ArrayList<T>();
        try {
            if (entities.size() > 0) {
                Criteria invocationQuery = getHibernateSession().createCriteria(associationType).add(
                        Restrictions.and(
                                Restrictions.in(direction, entities), 
                                Restrictions.not(Restrictions.in(oppositeDirection, entities))
                        )
                );
                invocationQuery.createAlias("from", "f");
                invocationQuery.createAlias("to", "t");

                invocationQuery.add(
                        Restrictions.and(
                                Restrictions.isNotNull("f.parent"),
                                Restrictions.isNotNull("t.parent")
                        )
                );

                associations = invocationQuery.list();
            }
        } catch (HibernateException he) {
            fLogger.error("Error in queryAssociationsOfEntities " + he.getMessage());
            throw new EvolizerRuntimeException("Error in queryAssociationsOfEntities", he);
        } catch (EvolizerException ee) {
            fLogger.error("Error in queryAssociationsOfEntities " + ee.getMessage());
            throw new EvolizerRuntimeException("Error in queryAssociationsOfEntities", ee);
        }

        return associations;
    }

    /**
     * Queries associations of the given type between FAMIX entities,i.e., the from and the to
     * entities must be in the set of the given entities. 
     * 
     * @param entities	The list of entities.
     * @param associationType	FamixAssociation type.
     * @return	The list of associations of the given type between the given entities.
     */
    @SuppressWarnings({"unchecked"})
    public <T extends FamixAssociation> List<T> queryAssociationsBetweenEntities(
            List<? extends AbstractFamixEntity> entities, 
            java.lang.Class<T> associationType) throws EvolizerRuntimeException {

        associationType = (associationType != null) ? associationType : (Class<T>) org.evolizer.famix.model.entities.FamixAssociation.class;
        List<T> associations = new ArrayList<T>();
        try {
            if (entities.size() > 0) {
                Criteria invocationQuery = getHibernateSession().createCriteria(associationType).add(
                        Restrictions.and(Restrictions.in("from", entities), Restrictions.in("to", entities)));

                associations = invocationQuery.list();
            }
        } catch (HibernateException he) {
            fLogger.error("Error in queryAssociationsBetweenEntities " + he.getMessage());
            throw new EvolizerRuntimeException("Error in queryAssociationsBetweenEntities", he);
        } catch (EvolizerException ee) {
            fLogger.error("Error in queryAssociationsBetweenEntities " + ee.getMessage());
            throw new EvolizerRuntimeException("Error in queryAssociationsBetweenEntities", ee);
        }

        return associations;
    }

    /**
     * Query FAMIX associations of the given type between the two entities of set1 and set2.
     * 
     * @param set1	First set of FAMIX entities.
     * @param set2	Second set of FAMIX entities.
     * @param associationType	FamixAssociation type - if null associations of all types are queried.
     * @return	The list of associations.
     */
    @SuppressWarnings({"unchecked"})
    public <T extends FamixAssociation> List<T> queryAssociationsBetweenEntitySets(
            List<? extends AbstractFamixEntity> set1, 
            List<? extends AbstractFamixEntity> set2, 
            java.lang.Class<T> associationType) throws EvolizerRuntimeException {

        associationType = (associationType != null) ? associationType : (Class<T>) org.evolizer.famix.model.entities.FamixAssociation.class;

        List<T> associations = new ArrayList<T>();
        try {
            Criteria invocationQuery = getHibernateSession().createCriteria(associationType).add(
                    Restrictions.or(
                            Restrictions.and(Restrictions.in("from", set1), Restrictions.in("to", set2)),
                            Restrictions.and(Restrictions.in("to", set1), Restrictions.in("from", set2))
                    )
            );

            associations = invocationQuery.list();
        } catch (HibernateException he) {
            fLogger.error("Error in queryAssociationsBetweenEntities " + he.getMessage());
            throw new EvolizerRuntimeException("Error in queryAssociationsBetweenEntitySets", he);
        } catch (EvolizerException ee) {
            fLogger.error("Error in queryAssociationsBetweenEntities " + ee.getMessage());
            throw new EvolizerRuntimeException("Error in queryAssociationsBetweenEntitySets", ee);
        }

        return associations;
    }

    /**
     * Query FAMIX entities with a unique name equal the given unique names.
     * 
     * @param uniqueNames	The given unique names.
     * @return	The list of FAMIX entities equaling the given unique names.
     * @throws EvolizerException
     */
    @SuppressWarnings( { "unchecked" })
    public List<AbstractFamixEntity> queryEntitiesByUniqueName(List<String> uniqueNames) throws EvolizerException {
        List<AbstractFamixEntity> entities = new ArrayList<AbstractFamixEntity>();

        try {
            Criteria query = getHibernateSession().createCriteria(AbstractFamixEntity.class);
            Disjunction orClausel = Restrictions.disjunction();

            int countOPs = 0;
            for (String	uniqueName : uniqueNames) {
                if (uniqueName != "") {
                    orClausel.add(Restrictions.eq("uniqueName", uniqueName));
                    countOPs++;
                }
            }

            if (countOPs > 0) {
                entities.addAll(query.add(orClausel).list());
            }
        } catch (HibernateException he) {
            fLogger.error("Error in queryEntitiesByUniqueName " + he.getMessage());
            throw new EvolizerRuntimeException("Error in queryEntitiesByUniqueName", he);
        } catch (EvolizerException ee) {
            fLogger.error("Error in queryEntitiesByUniqueName " + ee.getMessage());
            throw new EvolizerRuntimeException("Error in queryEntitiesByUniqueName ", ee);
        }

        return entities;
    }

    /**
     * Query FAMIX entities by the source reference as obtained from the Eclipse FamixPackage Explorer.
     * 
     * @param sourceReferences
     * @return
     * @throws EvolizerException
     */
    @SuppressWarnings( { "unchecked"} )
    public List<AbstractFamixEntity> queryEntitiesBySourceReference(Hashtable<String,Integer> sourceReferences) throws EvolizerException {
        List<AbstractFamixEntity> entities = new ArrayList<AbstractFamixEntity>();

        try {
            Criteria query = getHibernateSession().createCriteria(AbstractFamixEntity.class);
            query.createAlias("sourceAnchor", "sa");
            Disjunction orClausel = Restrictions.disjunction();

            int countOPs = 0;
            for (String reducedUniqueName : sourceReferences.keySet()) {
                if (reducedUniqueName != "") {
                    orClausel.add(Restrictions.and(
                            Restrictions.like("uniqueName", reducedUniqueName + "(%)"),
                            Restrictions.eq("sa.start", sourceReferences.get(reducedUniqueName))
                    )
                    );

                    countOPs++;
                }
            }

            if (countOPs > 0) {
                entities.addAll(query.add(orClausel).list());
            }
        } catch (HibernateException he) {
            fLogger.error("Error in queryEntitiesBySourceReference " + he.getMessage());
            throw new EvolizerRuntimeException("Error in queryEntitiesBySourceReference", he);
        } catch (EvolizerException ee) {
            fLogger.error("Error in queryEntitiesBySourceReference " + ee.getMessage());
            throw new EvolizerRuntimeException("Error in queryEntitiesBySourceReference", ee);
        }

        return entities;
    }


    /**
     * Returns a list containing the given entity and all its descendants.
     * 
     * @param entity    The parent entity.
     * @return  The list of descendant entities inclusive the given parent entity.
     */
    @SuppressWarnings("unchecked")
    public List<AbstractFamixEntity> getDescendants(AbstractFamixEntity entity) {
        List<AbstractFamixEntity> entities = new ArrayList<AbstractFamixEntity>();
        entities.add(entity);
        if (entity instanceof IHierarchicalElement) {
            IHierarchicalElement<? extends AbstractFamixEntity> parentEntity = (IHierarchicalElement<? extends AbstractFamixEntity>) entity;
            if (parentEntity.getChildren().size() > 0) {
                for (AbstractFamixEntity child : parentEntity.getChildren()) {
                    entities.addAll(getDescendants(child));
                }
            }
        }
        return entities;
    }

    /**
     * Return the entities depending on the given entities in the given direction to the given
     * maximum level deep. If <code>maxLevel=-1</code> then until there are no more new dependent
     * FAMIX entities.
     * 
     * @param entities the entities
     * @param dependentEntities all dependent entities
     * @param entityType the entity type
     * @param associationType the association type
     * @param direction the direction
     * @param level the level
     * @param maxLevel the max level
     * 
     * @return the nesting level
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractFamixEntity> int queryDependentEntities(
            List<T> entities,
            Set<T> dependentEntities,
            final java.lang.Class<T> entityType, 
            final java.lang.Class<? extends FamixAssociation> associationType, 
            final String direction,
            int level,
            final int maxLevel) {

        if (level != maxLevel) {
            List<? extends FamixAssociation> associations = queryAssociationsOfEntities(
                    entities, 
                    associationType, 
                    direction);
            Set<T> newDependentEntities = new HashSet<T>();
            for (FamixAssociation association : associations) {
                T dependentEntity;
                if (direction.equals("from")) {
                    dependentEntity = (T) association.getTo();
                } else {
                    dependentEntity = (T) association.getFrom();                    
                }
                if (!dependentEntities.contains(dependentEntity)) {
                    newDependentEntities.add(dependentEntity);
                }
            }
            if (! newDependentEntities.isEmpty()) {
                dependentEntities.addAll(newDependentEntities);
                level = queryDependentEntities(
                        new ArrayList<T>(newDependentEntities),
                        dependentEntities,
                        entityType,
                        associationType,
                        direction,
                        (level + 1),
                        maxLevel);
            }
        }

        return level;
    }

    /**
     * Calculates associations of the given type between the given parent entities.
     * For each parent entity the descendants are determined. Then the associations between 
     * between these sets of entities are computed. 
     * 
     * @param entities  The list of parent entities.
     * @param associationType   The FAMIX association type.   
     * @return  A list of associations between the parent entities and their contained descendants.
     */
    public <T extends FamixAssociation> List<T> getAssociationsBetweenParentEntities(
            List<AbstractFamixEntity> entities, 
            Class<T> associationType) 
            throws EvolizerRuntimeException {

        List<T> allAssociations = new ArrayList<T>();

        Hashtable<AbstractFamixEntity, List<AbstractFamixEntity>> entitySets = new Hashtable<AbstractFamixEntity, List<AbstractFamixEntity>>();
        for (AbstractFamixEntity entity : entities) {
            entitySets.put(entity, getDescendants(entity));
        }

        Set<AbstractFamixEntity> processedToParents = new HashSet<AbstractFamixEntity>();
        for (AbstractFamixEntity fromParent : entitySets.keySet()) {
            for (AbstractFamixEntity toParent : entitySets.keySet()) {
                if (!fromParent.equals(toParent) && !processedToParents.contains(toParent)) {
                    List<T> associations = queryAssociationsBetweenEntitySets(
                            entitySets.get(fromParent), 
                            entitySets.get(toParent), 
                            associationType);

                    allAssociations.addAll(associations);
                }
            }
            processedToParents.add(fromParent);     // the query is bi-directional - queries for this fromParent can be skipped
        }

        return allAssociations;
    }

    /**
     * Return list of parent entities of the given entity.
     * 
     * @param entity    The entity.
     * @return  List of parent entities.
     */
    public List<AbstractFamixEntity> getParentEntities(AbstractFamixEntity entity) {
        List<AbstractFamixEntity> parentEntities = new ArrayList<AbstractFamixEntity>();
        if (entity.getParent() != null) {
            parentEntities.add(entity.getParent());
            parentEntities.addAll(getParentEntities(entity.getParent()));
        }

        return parentEntities;
    }

    /**
     * Returns the wrapped Hibernate session used when dealing with special
     * issues like Criteria, Restrictions, etc.
     * 
     * @return the wrapped Hibernate session
     */
    public Session getHibernateSession() throws EvolizerException {
        return getEvolizerSession().getHibernateSession();
    }

    /**
     * Returns the current Evolizer session of the given URL. 
     * 
     * @return the Evolizer session.
     * @throws EvolizerException
     */
    public IEvolizerSession getEvolizerSession() throws EvolizerException {
        //        IEvolizerSession session = EvolizerSessionHandler.getHandler().getCurrentSession(fDbUrl);
        //        return session;

        return fSession;
    }
}
