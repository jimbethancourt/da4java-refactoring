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
package org.evolizer.da4java.graph.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.da4java.commands.EditResult;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.utils.SnapshotAnalyzer;
import org.evolizer.model.resources.entities.misc.IHierarchicalElement;

import y.base.Edge;
import y.base.Node;
import y.view.hierarchy.HierarchyManager;

/**
 * Class provides serveral convenience methods to add/remove FAMIX entities to/from the graph.
 * The corresponding data maps in the referenced {@link DependencyGraph} are updated.
 * 
 * @author mark, Martin Pinzger
 */
public class GraphLoader {

    /** The Logger to print messages. */
    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(GraphLoader.class.getName());

    /** Reference to the Graph2D that is populated by the GraphLoader. */
    private DependencyGraph fGraph;

    /** the graph loader loads the graph of a certain snapshot whose data is fetched by the snapshot analyzer. */
    private SnapshotAnalyzer fSnapshotAnalyzer;

    /** The HierachyManager that is needed to generate nested Graphs with FolderNodes and GroupNodes. */
    private HierarchyManager fHierarchyManager;

    /**
     * Instantiates a new graph loader.
     * 
     * @param snapshotAnalyzer the snapshot analyzer
     */
    public GraphLoader(SnapshotAnalyzer snapshotAnalyzer) {
        fSnapshotAnalyzer = snapshotAnalyzer;
    }

    /**
     * Initializes the given Graph and creates.
     * 
     * @param graph the graph
     */
    public void initGraph(DependencyGraph graph) {
        fGraph = graph;
        fHierarchyManager = new HierarchyManager(getGraph());
    }

    /**
     * Add the given entities, their descendants and parents, and all their associations
     * between these entities and entities contained by the graph.
     * 
     * @param entities The list of parent entities to add.
     * @return Added entities and associations.
     */
    public EditResult addEntitiesAndAssociations(List<AbstractFamixEntity> entities) {
        List<AbstractFamixEntity> addedEntities = addEntitiesAndParents(entities, true);

        List<AbstractFamixEntity> involvedEntites = new ArrayList<AbstractFamixEntity>();
        for (AbstractFamixEntity entity : entities) {
            involvedEntites.addAll(getSnapshotAnalyzer().getDescendants(entity));
        }

        List<FamixAssociation> associations = getSnapshotAnalyzer().queryAssociationsBetweenEntities(involvedEntites, null);
        List<FamixAssociation> associationsToOtherEntities = getAssociationsToOtherGraphEntities(involvedEntites, null);
        associations.addAll(associationsToOtherEntities);

        List<FamixAssociation> addedAssociations = addAssociations(associations);

        return new EditResult(addedEntities, addedAssociations);
    }

    /**
     * Remove the given parent entities, their descendants and all the associations between
     * them and between them and other entities currently contained by the graph.
     * 
     * @param entities The list of parent entities to remove from the graph.
     * @return Removed entities and associations.
     */
    public EditResult removeEntitiesAndAssociations(List<AbstractFamixEntity> entities) {
        EditResult editResult;

        List<AbstractFamixEntity> involvedEntites = new ArrayList<AbstractFamixEntity>();
        for (AbstractFamixEntity entity : entities) {
            involvedEntites.addAll(getSnapshotAnalyzer().getDescendants(entity));
        }

        List<FamixAssociation> associations = getSnapshotAnalyzer().queryAssociationsBetweenEntities(involvedEntites, null);
        List<FamixAssociation> associationsToOtherEntities = getAssociationsToOtherGraphEntities(involvedEntites, null);
        associations.addAll(associationsToOtherEntities);

        editResult = removeAssociations(associations);

        List<AbstractFamixEntity> removedEntities = new ArrayList<AbstractFamixEntity>();
        for (AbstractFamixEntity entity : involvedEntites) {
            if (fGraph.removeFamixEntity(entity)) {
                removedEntities.add(entity);
            }
        }

        return new EditResult(removedEntities, editResult.getAssociations());
    }

    /**
     * Remove the associations of the given set of associations currently contained by the graph.
     * 
     * @param associations Associations to remove from the graph.
     * @return Associations removed from the graph.
     */
    public EditResult removeAssociations(List<? extends FamixAssociation> associations) {
        List<FamixAssociation> removedAssociations = new ArrayList<FamixAssociation>();

        for (FamixAssociation association : associations) {
            if (fGraph.removeAssociation(association)) {
                removedAssociations.add(association);
            }
        }

        return new EditResult(new ArrayList<AbstractFamixEntity>(), removedAssociations);
    }


    /**
     * Add the given entities and associations to the graph. Parent entities are checked and
     * eventually added. Note, that child entities are not added.
     * 
     * @param entities Entities to add.
     * @param associations Associations to add.
     * 
     * @return Added entities and associations.
     */
    public EditResult addEntitiesAndAssociations(List<AbstractFamixEntity> entities, List<FamixAssociation> associations) {
        List<AbstractFamixEntity> addedEntities = addEntitiesAndParents(entities, false);
        List<FamixAssociation> addedAssociations = addAssociations(associations);

        return new EditResult(addedEntities, addedAssociations);
    }

    /**
     * Add entities that have dependencies of the given direction with the given parent
     * entity or one of its descendant. Dependent entities are queried via the given
     * association type. Corresponding associations, as well as, parent entities of added
     * entities are added.
     * 
     * @param entity Parent entity.
     * @param associationType FamixAssociation type to look for dependent entities.
     * @param direction from=outgoing, to=incoming, null
     * 
     * @return Added dependent entities and corresponding associations.
     */
    public EditResult addDependentEntitiesAndAssociations(AbstractFamixEntity entity,
            java.lang.Class<? extends FamixAssociation> associationType,
            String direction) {

        List<AbstractFamixEntity> involvedEntites = getSnapshotAnalyzer().getDescendants(entity);
        List<? extends FamixAssociation> associations = getSnapshotAnalyzer().queryAssociationsOfEntities(involvedEntites, associationType, direction);
        List<AbstractFamixEntity> newEntities = getListOfNotContainedEntities(associations);
        List<AbstractFamixEntity> addedEntities = addEntitiesAndParents(newEntities, false);
        List<FamixAssociation> addedAssociations = addAssociations(associations);

        return new EditResult(addedEntities, addedAssociations);
    }

    /**
     * Add associations of the given type or all that are between the given parent entities.
     * Corresponding descendants are added as well. Associations between descendants within
     * a parent entity are not added.
     * 
     * @param entities The parent entities.
     * @param associationType The association type.
     * 
     * @return Added entities and associations.
     */
    public EditResult addAssociationsBetweenParentEntities(
            List<AbstractFamixEntity> entities,
            java.lang.Class<? extends FamixAssociation> associationType) {

        List<? extends FamixAssociation> allAssociations = getSnapshotAnalyzer().getAssociationsBetweenParentEntities(entities, associationType);
        List<AbstractFamixEntity> newEntities = getListOfNotContainedEntities(allAssociations);
        List<AbstractFamixEntity> addedEntities = addEntitiesAndParents(newEntities, false);
        List<FamixAssociation> addedAssociations = addAssociations(allAssociations);

        return new EditResult(addedEntities, addedAssociations);
    }

    /**
     * Adds the given entities, their parent, and child entities to the graph. Child entities
     * are only added when the addAllChildren switch is activated. Note, that no associations
     * of the given entities, their parent and child entities are added by this method.
     * 
     * @param entities a list containing the entities to add
     * @param addAllChildren true, if children should be added
     * 
     * @return A list with all entities added to the graph.
     */
    public List<AbstractFamixEntity> addEntitiesAndParents(List<AbstractFamixEntity> entities, boolean addAllChildren) {
        List<AbstractFamixEntity> addedEntities = addEntitiesAndChildren(entities, addAllChildren);

        List<AbstractFamixEntity> parentEntities = addParentEntities(entities); 
        addedEntities.addAll(parentEntities);

        for (AbstractFamixEntity entity : addedEntities) {
            checkAndUpdateParentNode(entity);
        }
        return addedEntities;
    }

    /**
     * Add the given list of associations to the graph. FamixAssociation type is not taken into account.
     * 
     * @param associations List of associations to add.
     * 
     * @return The list of added associations.
     */
    public List<FamixAssociation> addAssociations(List<? extends FamixAssociation> associations) {
        List<FamixAssociation> addedAssociations = new ArrayList<FamixAssociation>();
        for (FamixAssociation association : associations) {
            Edge edge = getGraph().createEdge(association);
            if (edge != null) {
                addedAssociations.add(association);
            }
        }

        return addedAssociations;
    }

    /**
     * Add the given entities and if activated their children to the graph.
     * 
     * @param entities FAMIX entities to add.
     * @param addAllChildren if true, child entities are added as well.
     * 
     * @return the list of added entities.
     */
    @SuppressWarnings("unchecked")
    private List<AbstractFamixEntity> addEntitiesAndChildren(List<AbstractFamixEntity> entities, boolean addAllChildren) {
        List<AbstractFamixEntity> addedEntities = new ArrayList<AbstractFamixEntity>();
        for (AbstractFamixEntity entity : entities) {
            if (!fGraph.contains(entity)) {
                if (fGraph.createNode(entity) != null) {
                    addedEntities.add(entity);
                }
            }

            if (addAllChildren) {
                if (entity instanceof IHierarchicalElement) {
                    IHierarchicalElement<? extends AbstractFamixEntity> parentEntity = (IHierarchicalElement<AbstractFamixEntity>) entity;
                    Set<? extends AbstractFamixEntity> children = parentEntity.getChildren();
                    if (children != null && children.size() > 0) {
                        List<AbstractFamixEntity> addedChildren = addEntitiesAndChildren(new ArrayList<AbstractFamixEntity>(children), addAllChildren);
                        addedEntities.addAll(addedChildren);
                    }
                }
            }
        }

        return addedEntities;
    }

    /**
     * Add parent entities of the given entities to the graph. Note that re-arranging of parent and child nodes
     * of added entities has to be done afterwards.
     * 
     * @param entities a list of entities whose parents should be loaded
     * 
     * @return The list of added entities.
     */
    private List<AbstractFamixEntity> addParentEntities(List<AbstractFamixEntity> entities) {
        List<AbstractFamixEntity> addedEntities = new ArrayList<AbstractFamixEntity>();

        for (AbstractFamixEntity entity : entities) {
            AbstractFamixEntity parentEntity = entity.getParent();
            if (parentEntity != null && !fGraph.contains(parentEntity)) {
                if (fGraph.createNode(parentEntity) != null) {
                    addedEntities.add(parentEntity);
                }
            }
        }
        if (addedEntities.size() > 0) {
            addedEntities.addAll(addParentEntities(addedEntities));
        }

        return addedEntities;
    }

    /**
     * Compute incoming and outgoing associations of the given entities to other entities
     * contained by the graph.
     * 
     * @param entities The list of given entities.
     * @param associationType Type of association
     * 
     * @return The list of associations to other graph entities in the graph.
     */
    private List<FamixAssociation> getAssociationsToOtherGraphEntities(List<AbstractFamixEntity> entities, 
            java.lang.Class<? extends FamixAssociation> associationType) {

        List<FamixAssociation> associations = new ArrayList<FamixAssociation>();
        List<? extends FamixAssociation> from = getSnapshotAnalyzer().queryAssociationsOfEntities(entities, associationType, "from");
        for (FamixAssociation association : from) {
            if (fGraph.contains(association.getTo())) {
                associations.add(association);
            }
        }
        List<? extends FamixAssociation> to = getSnapshotAnalyzer().queryAssociationsOfEntities(entities, associationType, "to");
        for (FamixAssociation association : to) {
            if (fGraph.contains(association.getFrom())) {
                associations.add(association);
            }
        }
        return associations;
    }

    /**
     * FamixMethod that updates the parent node hierarchy of the the given entity.
     * 
     * @param entity the entity
     */
    private void checkAndUpdateParentNode(AbstractFamixEntity entity) {
        AbstractFamixEntity parent = entity.getParent();
        if (parent != null) {
            if (fGraph.contains(parent) && fGraph.contains(entity)) {
                Node parentNode = fGraph.getNode(parent);
                Node childNode = fGraph.getNode(entity);
                if (!parentNode.equals(fHierarchyManager.getParentNode(childNode))) {
                    fHierarchyManager.setParentNode(childNode, parentNode);
                    sLogger.debug("reassigned node '" + childNode + "' to parent '" + parentNode + "'");
                }
            }
        }
    }

    /**
     * Returns a list of entities that are not yet contained in the graph but are necessary for the creation of the edges of the given associations.
     * 
     * @param associations the associations
     * @return the entities that should be added to the graph
     */
    public List<AbstractFamixEntity> getListOfNotContainedEntities(List<? extends FamixAssociation> associations) {
        Set<AbstractFamixEntity> notContainedEntities = new HashSet<AbstractFamixEntity>();
        for (FamixAssociation association : associations) {
            AbstractFamixEntity from = association.getFrom();
            AbstractFamixEntity to = association.getTo();
            if (!fGraph.contains(from)) {
                notContainedEntities.add(from);
            }

            if (!fGraph.contains(to)) {
                notContainedEntities.add(to);
            }
        }
        return new ArrayList<AbstractFamixEntity>(notContainedEntities);
    }

    /**
     * Returns the hierarchy manager.
     * 
     * @return The hierarchy manager for the hierarchic graph this graph loader creates.
     */
    public HierarchyManager getHierarchyManager() {
        return fHierarchyManager;
    }

    /**
     * Returns the graph.
     * 
     * @return the Graph2D representation associated with this
     * DependencyGraphLoader. Note that in the current Graph2D some
     * nodes and edges may be hidden.
     */
    public DependencyGraph getGraph() {
        return fGraph;
    }
    
    public GraphModelMapper getGraphModelMapper() {
    	return fGraphModelMapper;
    }
    
    public GraphManager getGraphManager(){
    	return fGraphManager;
    }
    /**
     * Gets the snapshot analyzer.
     * 
     * @return the snapshot analyzer
     */
    public SnapshotAnalyzer getSnapshotAnalyzer() {
        return fSnapshotAnalyzer;
    }

}
