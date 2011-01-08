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
package org.evolizer.da4java.polymetricviews;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.da4java.plugin.DA4JavaGraphEditor;
import org.evolizer.da4java.polymetricviews.controller.ProfileChooser;
import org.evolizer.da4java.polymetricviews.model.PolymetricViewProfile;
import org.evolizer.da4java.visibility.ViewConfigModel;
import org.evolizer.metrics.store.MetricStore;

/**
 * Polymetric-view configuration view. The view is enabled only when a 
 * DA4JavaGraphEditor instance is currently the top editor.
 * The configuration of the entity view is stored in the ViewConfigModel at the current
 * DA4JavaGraphPanel which is contained by the current DA4JavaGraphEditor instance.
 * 
 * @author pinzger
 */
public class PolymetricViewControllerView extends ViewPart {
    /** The VIE w_ title. */
    public static final String VIEW_TITLE = "Polymetric View Control";

    /** The VIE w_ id. */
    public static final String VIEW_ID = "org.evolizer.da4java.polymetricviews.PolymetricViewControllerView";

    /** The METRI c_ uniform. */
    public static final String METRIC_UNIFORM = "Uniform";
    
    /** The Constant sLogger. */
    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(PolymetricViewControllerView.class.getName()); 

    /** The parent. */
    private Composite fParent;
    
    /** The graph editor. */
    private DA4JavaGraphEditor fGraphEditor;

    /** List of Metrics that can be selected for each dimension. */
    private String[] fAvailableMetrics;

    /** The height metric chooser. */
    private Combo fHeightMetricChooser;
    
    /** The color metric chooser. */
    private Combo fColorMetricChooser;
    
    /** The width metric chooser. */
    private Combo fWidthMetricChooser;
    
    /** The profile chooser. */
    private ProfileChooser fProfileChooser;

    /** 
     * {@inheritDoc}
     */
    @Override
    public void createPartControl(Composite parent) {
        sLogger.info("Creating polymetric view part");

        fParent = parent;

        // read out the available metrics out of the metric store and add the
        // uniform "strategy"
        Set<String> helper = MetricStore.listAllMetrics();
        List<String> availableMetrics = new ArrayList<String>(helper);
        Collections.sort(availableMetrics);
        availableMetrics.add(0, METRIC_UNIFORM);
        fAvailableMetrics = availableMetrics.toArray(new String[]{});

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        parent.setLayout(gridLayout);
        insertProfileChooser(parent);
        insertMetricDimensionSelectors(parent);

        //        fDefaultPropChangeSupport = new PropertyChangeSupport(this);
        //        fDefaultPropChangeSupport.addPropertyChangeListener(new PolymetricViewProfileController(this));

        // add part listener
        try {
            IWorkbenchWindow wbWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (wbWindow != null) {
                wbWindow.getPartService().addPartListener(new EditorPartListener());
            } else {
                sLogger.error("No workbench window is active - could not add listeners for polymetric view");
            }
        } catch (NullPointerException e) {
            sLogger.error("Error in adding listener to polymetric view - close and reopen polymetric view");
        }
    }

    /**
     * Adds the Polymetric View selection group.
     * 
     * @param parent    Parent component.
     */
    private void insertProfileChooser(Composite parent) {
        fProfileChooser = new ProfileChooser(this, parent, SWT.NULL);
    }

    /**
     * Insert metric dimension selectors.
     * 
     * @param parent the parent
     */
    private void insertMetricDimensionSelectors(Composite parent) {
        Group configure = new Group(parent, SWT.NULL);
        configure.setText("Metric Dimension Selectors");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        configure.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        configure.setLayoutData(gridData);

        new Label(configure, SWT.NULL).setText("Node Height: ");

        fHeightMetricChooser = new Combo(configure, SWT.READ_ONLY);
        fHeightMetricChooser.setLayoutData(new GridData(SWT.CENTER));
        fHeightMetricChooser.setItems(fAvailableMetrics);
        fHeightMetricChooser.select(0);
        fHeightMetricChooser.addListener(SWT.Selection, new MetricSelectionListener(
                ViewConfigModel.UPDATE_NODE_HEIGHTS));

        new Label(configure, SWT.NULL).setText("Node Width:");
        fWidthMetricChooser = new Combo(configure, SWT.READ_ONLY);
        fWidthMetricChooser.setItems(fAvailableMetrics);
        fWidthMetricChooser.select(0);
        fWidthMetricChooser.addListener(SWT.Selection, new MetricSelectionListener(
                ViewConfigModel.UPDATE_NODE_WIDTHS));

        new Label(configure, SWT.NULL).setText("Node Color:");

        fColorMetricChooser = new Combo(configure, SWT.READ_ONLY);
        fColorMetricChooser.setItems(fAvailableMetrics);
        fColorMetricChooser.select(0);
        fColorMetricChooser.addListener(SWT.Selection, new MetricSelectionListener(
                ViewConfigModel.UPDATE_NODE_COLORS));

        //        fRefreshButton = new Button(configure,SWT.PUSH);
        //        fRefreshButton.setText("Refresh Graph");
        //        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        //        gridData.horizontalSpan = 2;
        //        fRefreshButton.setLayoutData(gridData);
        //        fRefreshButton.addListener(SWT.Selection, new MetricSelectionListener(
        //                ViewConfigModel.UPDATE_GRAPH_EVENT));
    }

    /**
     * Update view.
     */
    private void updateView() {
        if (fGraphEditor != null) {
            sLogger.info("Updating view to editor " + fGraphEditor.getPartName());

            ViewConfigModel viewConfigModel = fGraphEditor.getPanel().getViewConfigModel();
            PolymetricViewProfile profile = viewConfigModel.getProfile();

            fProfileChooser.updateView();
            select(fHeightMetricChooser, profile.getHeightMetric());
            select(fWidthMetricChooser, profile.getWidthMetric());
            select(fColorMetricChooser, profile.getColorMetric());
        }
    }

    /**
     * Select the corresponding entry in the given combo box. A helper method.
     * 
     * @param combo the combo
     * @param entry the name of the combo box entry
     */
    public void select(Combo combo, String entry) {
        if (entry != null && entry.length() > 0) {
            for (int index = 0; index < combo.getItemCount(); index++) {
                if (combo.getItem(index).equalsIgnoreCase(entry)) {
                    combo.select(index);
                    break;
                }
            }
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return VIEW_TITLE;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void setFocus() {
    }

    /** 
     * {@inheritDoc}
     */
    public void dispose() {
        //        fDefaultPropChangeSupport.firePropertyChange(PolymetricViewProfileController.DISPOSING_VIEW_EVENT, null, null);
        fProfileChooser.dispose();
        super.dispose();
    }

    /**
     * Gets the height metric chooser.
     * 
     * @return the height metric chooser
     */
    public Combo getHeightMetricChooser() {
        return fHeightMetricChooser;
    }

    /**
     * Gets the color metric chooser.
     * 
     * @return the color metric chooser
     */
    public Combo getColorMetricChooser() {
        return fColorMetricChooser;
    }

    /**
     * Gets the width metric chooser.
     * 
     * @return the width metric chooser
     */
    public Combo getWidthMetricChooser() {
        return fWidthMetricChooser;
    }

    /**
     * Gets the available metrics.
     * 
     * @return the available metrics
     */
    public String[] getAvailableMetrics() {
        return fAvailableMetrics;
    }

    /**
     * Gets the graph editor.
     * 
     * @return the graph editor
     */
    public DA4JavaGraphEditor getGraphEditor() {
        return fGraphEditor;
    }

    /**
     * Listener for handling changes in the active workbench. Whenever a graph editor
     * is selected/opened the view config model is updated.
     * 
     * @author pinzger
     */
    private class EditorPartListener implements IPartListener {
        /** 
         * {@inheritDoc}
         */
        public void partActivated(IWorkbenchPart part) {
            sLogger.info("Polymetric view part activated");
            if (part instanceof DA4JavaGraphEditor) {
                fGraphEditor = (DA4JavaGraphEditor) part;
                fParent.setVisible(true);
                updateView();
            } else {
                IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
                if (editor == null || !(editor instanceof DA4JavaGraphEditor)) {
                    fParent.setVisible(false);
                }
            }
        }

        /** 
         * {@inheritDoc}
         */
        public void partBroughtToTop(IWorkbenchPart part) {}
        /** 
         * {@inheritDoc}
         */
        public void partClosed(IWorkbenchPart part) {
            if (part instanceof PolymetricViewControllerView) {
                IWorkbenchWindow wbWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (wbWindow != null) {
                    wbWindow.getPartService().removePartListener(this);
                } else {
                    sLogger.error("No workbench window is active - could not add listeners for polymetric view");
                }
            }
        }
        /** 
         * {@inheritDoc}
         */
        public void partDeactivated(IWorkbenchPart part) {}
        /** 
         * {@inheritDoc}
         */
        public void partOpened(IWorkbenchPart part) {
        }
    }

    /**
     * Listener for changes in the metric selection combo boxes.
     * It updates the current polymetric view profile of the
     * view config model instance.
     * 
     * @author pinzger
     */
    private class MetricSelectionListener implements Listener {

        /** The property change type. */
        private String fPropertyChangeType;

        /**
         * The constructor.
         * 
         * @param propertyChangeType the property change type
         */
        public MetricSelectionListener(String propertyChangeType) {
            this.fPropertyChangeType = propertyChangeType;
        }

        /** 
         * {@inheritDoc}
         */
        public void handleEvent(Event event) {
            sLogger.info("Metric selection changed");
            if (fGraphEditor != null) {
                ViewConfigModel viewConfigModel = fGraphEditor.getPanel().getViewConfigModel();
                String name = fProfileChooser.getText();
                String height = fHeightMetricChooser.getText();
                String width = fWidthMetricChooser.getText();
                String color = fColorMetricChooser.getText();
                PolymetricViewProfile profile = new PolymetricViewProfile(name, height, width, color);

                viewConfigModel.updatePolymetricViewsProfile(fPropertyChangeType, profile);
            }
        }
    }
}
