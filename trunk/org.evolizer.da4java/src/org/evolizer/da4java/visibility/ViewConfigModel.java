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
package org.evolizer.da4java.visibility;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Hashtable;

import org.evolizer.da4java.graph.utils.FamixAssociationMap;
import org.evolizer.da4java.graph.utils.FamixEntityMap;
import org.evolizer.da4java.polymetricviews.model.PolymetricViewProfile;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.AbstractFamixEntity;

/**
 * Container for the entity visibility, association visibility and polymetric view profile.
 * Every change in any of the three configuration leads to an corresponding property change
 * event.
 * 
 * @author pinzger
 */
public class ViewConfigModel {
    
    /** The Constant ENTITY_VISIBILITY_CHANGE. */
    public static final String ENTITY_VISIBILITY_CHANGE = "entityVisibility";
    
    /** The Constant ASSOCIATION_VISIBILITY_CHANGE. */
    public static final String ASSOCIATION_VISIBILITY_CHANGE = "associationVisibility";
    
    /** The Constant POLYMETRIC_VIEW_CHANGE. */
    public static final String POLYMETRIC_VIEW_CHANGE = "polymetricViewChange";
    
    /** The Constant UPDATE_NODE_COLORS. */
    public static final String UPDATE_NODE_COLORS = "update_node_colors";
    
    /** The Constant UPDATE_NODE_HEIGHTS. */
    public static final String UPDATE_NODE_HEIGHTS = "update_node_heights";
    
    /** The Constant UPDATE_NODE_WIDTHS. */
    public static final String UPDATE_NODE_WIDTHS = "update_node_widths";
    
    /** The Constant UPDATE_GRAPH_EVENT. */
    public static final String UPDATE_GRAPH_EVENT = "update_graph";
    
    /** The Constant LOAD_PROFILE_EVENT. */
    public static final String LOAD_PROFILE_EVENT = "load_profile";
    
    /** The Constant REMOVE_PROFILE_EVENT. */
    public static final String REMOVE_PROFILE_EVENT = "remove_profile";
    
    /** The Constant SAVE_PROFILE_EVENT. */
    public static final String SAVE_PROFILE_EVENT = "save_profile";
    
    /** The Constant DISPOSING_VIEW_EVENT. */
    public static final String DISPOSING_VIEW_EVENT = "dispose_view";

    /** The property change support. */
    private PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);

    /** Entity visibility map. */
    private Hashtable<java.lang.Class<? extends AbstractFamixEntity>, Boolean> fEntityTypeVisibilityMap;
    
    /** Association visibility map. */
    private Hashtable<java.lang.Class<? extends FamixAssociation>, Boolean> fAssociationTypeVisibilityMap;

    /** Polymetric-View profile. */
    private PolymetricViewProfile fPolyViewProfile;
    
    /** The selected profile. */
    private String fSelectedProfile;

    /**
     * The constructor.
     */
    public ViewConfigModel() {
        initModel();
    }

    /**
     * Initialize the view config model.
     */
    private void initModel() {
        fEntityTypeVisibilityMap = new Hashtable<java.lang.Class<? extends AbstractFamixEntity>, Boolean>();
        for (Class<? extends AbstractFamixEntity> entityType : FamixEntityMap.getInstance().getAllTypes()) {
            fEntityTypeVisibilityMap.put(entityType, true);
        }

        fAssociationTypeVisibilityMap = new Hashtable<java.lang.Class<? extends FamixAssociation>, Boolean>();
        for (Class<? extends FamixAssociation> associationType : FamixAssociationMap.getInstance().getAllTypes()) {
            if (associationType != null) {
                fAssociationTypeVisibilityMap.put(associationType, true);
            }
        }

        fSelectedProfile = "";
        fPolyViewProfile = new PolymetricViewProfile("Default", "Uniform", "Uniform", "Uniform");
    }

    /**
     * Returns the association type visibility.
     * 
     * @return the association type visibility
     */
    public final Hashtable<Class<? extends FamixAssociation>, Boolean> getAssociationTypeVisibility() {
        return fAssociationTypeVisibilityMap;
    }

    /**
     * Returns the entity type visibility.
     * 
     * @return the entity type visibility
     */
    public final Hashtable<Class<? extends AbstractFamixEntity>, Boolean> getEntityTypeVisibility() {
        return fEntityTypeVisibilityMap;
    }

    /**
     * Gets the selected profile.
     * 
     * @return the selected profile
     */
    public final String getSelectedProfile() {
        return fSelectedProfile;
    }

    /**
     * Gets the profile.
     * 
     * @return the profile
     */
    public final PolymetricViewProfile getProfile() {
        return fPolyViewProfile;
    }

    /**
     * Update association visibility.
     * 
     * @param associationType the association type
     * @param visibility the visibility
     */
    public void updateAssociationVisibility(Class<? extends FamixAssociation> associationType, Boolean visibility) {
        fAssociationTypeVisibilityMap.put(associationType, visibility);
        fPropertyChangeSupport.firePropertyChange(ViewConfigModel.ASSOCIATION_VISIBILITY_CHANGE, associationType, visibility);
    }

    /**
     * Update entity visibility.
     * 
     * @param entityType the entity type
     * @param visibility the visibility
     */
    public void updateEntityVisibility(Class<? extends AbstractFamixEntity> entityType, Boolean visibility) {
        fEntityTypeVisibilityMap.put(entityType, visibility);
        fPropertyChangeSupport.firePropertyChange(ViewConfigModel.ENTITY_VISIBILITY_CHANGE, entityType, visibility);
    }

    /**
     * Update polymetric views profile.
     * 
     * @param changeType the change type
     * @param profile the profile
     */
    public void updatePolymetricViewsProfile(String changeType, PolymetricViewProfile profile) {
        fPolyViewProfile = profile;
        fPropertyChangeSupport.firePropertyChange(ViewConfigModel.POLYMETRIC_VIEW_CHANGE, changeType, profile);
    }

    /**
     * Adds the property change listener.
     * 
     * @param listener the listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        fPropertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes the property change listener.
     * 
     * @param listener the listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        fPropertyChangeSupport.removePropertyChangeListener(listener);
    }
}
