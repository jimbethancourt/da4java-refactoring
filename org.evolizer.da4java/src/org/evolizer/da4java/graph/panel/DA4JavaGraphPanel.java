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
package org.evolizer.da4java.graph.panel;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.evolizer.da4java.commands.CommandController;
import org.evolizer.da4java.graph.data.EdgeGrouper;
import org.evolizer.da4java.graph.data.GraphLoader;
import org.evolizer.da4java.graph.data.GraphManager;
import org.evolizer.da4java.graph.panel.rendering.FamixRealizerConfigurator;
import org.evolizer.da4java.graph.panel.rendering.GraphReLayouter;
import org.evolizer.da4java.graph.panel.toolbar.DA4JavaToolbar;
import org.evolizer.da4java.polymetricviews.controller.PolymetricViewGraphUpdater;
import org.evolizer.da4java.polymetricviews.model.PolymetricViewDataContainer;
import org.evolizer.da4java.visibility.ViewConfigModel;
import org.evolizer.da4java.visibility.controller.GraphElementsVisibilityUpdater;

import y.base.Edge;
import y.base.Node;
import y.module.IncrementalHierarchicLayoutModule;
import y.module.LayoutModule;
import y.module.SmartOrganicLayoutModule;
import y.option.OptionHandler;
import y.option.OptionItem;
import y.view.Graph2DView;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.hierarchy.GroupNodeRealizer;
import y.view.hierarchy.HierarchyEditMode;
import y.view.hierarchy.HierarchyManager;

/**
 * Main graph panel containing the graph, the toolbar, and links to diverse controllers.
 * It also provides the main method for layouting the graph.
 * 
 * @author Martin Pinzger
 */
public class DA4JavaGraphPanel extends JPanel {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -284112815423335652L;
    
    /** The logger. */
//    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(DA4JavaGraphPanel.class.getName()); 

    /** The graph view. */
    private Graph2DView fGraphView;

    /** The layout modules. */
    private Map<String, LayoutModule> fLayoutModules = new HashMap<String, LayoutModule>();
    
    /** The toolbar. */
    private DA4JavaToolbar fToolbar;
    
    /** The command controller. */
    private CommandController fCommandController;

    /** The depencency graph. */
    private GraphManager fGraph;
    
    /** The graph loader. */
    private GraphLoader fGraphLoader;
    
    /** The edge grouper. */
    private EdgeGrouper fEdgeGrouper;

    /** The view config model. */
    private ViewConfigModel fViewConfigModel;
    
    /** The polymetric view data collector. */
    private PolymetricViewDataContainer fPolymetricViewDataCollector;

    /**
     * The constructor.
     * 
     * @param graphLoader The graph loader
     */
    public DA4JavaGraphPanel(GraphLoader graphLoader) {
        fGraphLoader = graphLoader;
        fCommandController = new CommandController();
        fGraph = new GraphManager();

        fViewConfigModel = new ViewConfigModel();
        fPolymetricViewDataCollector = new PolymetricViewDataContainer(this);
    }

    /**
     * Initialize the graph panel.
     */
    public void initGraphPanel() {
        fGraphLoader.initGraph(fGraph);

        fGraphView = new Graph2DView(fGraph);
        fGraphView.setAntialiasedPainting(true);
        fGraph.registerView(fGraphView);

        initGraphLayout();
        registerViewModes();
        registerViewListeners();

        setLayout(new BorderLayout());
        fToolbar = new DA4JavaToolbar(this);
        fCommandController.addPropertyChangeListener(fToolbar);
        add(fToolbar, BorderLayout.NORTH);
        add(fGraphView, BorderLayout.CENTER);

        fEdgeGrouper = new EdgeGrouper(fGraph);
        fEdgeGrouper.groupAll();
    }

    /**
     * Register the various view modes.
     */
    private void registerViewModes() {
        // add another view mode that acts upon clicking on
        // a folder node and clicking on the open/close icon
        getView().addViewMode(new HierarchicClickViewMode(this));

        HierarchyEditMode hierarchyEditMode = createHierarchyEditMode();
        // add custom popup menus
        hierarchyEditMode.setPopupMode(new DA4JavaPopupMode(this));
        getView().addViewMode(hierarchyEditMode);
    }

    /**
     * Instantiates and registers the listeners for the view.
     */
    private void registerViewListeners() {
        fGraphView.getCanvasComponent().addMouseWheelListener(new Graph2DViewMouseWheelZoomListener());

        // The plan is to use the GraphListener and maybe also the HierarchyListener to listen 
        // to structural changes in the graph. This could be use to decide when graphs need to
        // be re-layout and to control the layout (i.e., only layout the changed parts of the graph)
        fGraph.addPropertyChangeListener(new FamixRealizerConfigurator());
        getHierarchyManager().addHierarchyListener(new GroupNodeRealizer.StateChangeListener());

        // init the graph visibility updater
        GraphElementsVisibilityUpdater visibilityUpdater = new GraphElementsVisibilityUpdater(this);
        fGraph.addGraphListener(visibilityUpdater);        // listen to graph events
        fViewConfigModel.addPropertyChangeListener(visibilityUpdater); // listen to changes in the view config model

        // init the polymetric view graph updater
        PolymetricViewGraphUpdater polyViewGraphUpdater = new PolymetricViewGraphUpdater(this);
        fGraph.addGraphListener(polyViewGraphUpdater);        // listen to graph events
        fViewConfigModel.addPropertyChangeListener(polyViewGraphUpdater); // listen to changes in the view config model

        // re-layouter should be notified as last element
        GraphReLayouter graphLayouter = new GraphReLayouter(fGraphView);
        fGraph.addGraphListener(graphLayouter);
//        fGraph.addGraph2DListener(graphLayouter);
        fGraph.addPropertyChangeListener(graphLayouter);
        fCommandController.addPropertyChangeListener(graphLayouter);

        // propagates text label changes on nodes as change events
        // on the hierarchy.
        // getView().getGraph2D().addGraph2DListener(new DefaultNodeChangePropagator());
    }

    /**
     * Init the graph layout.
     */
    private void initGraphLayout() {
        fLayoutModules = new HashMap<String, LayoutModule>();

        // y.module.OrganicLayoutModule organicLayoutModule = new y.module.OrganicLayoutModule();
        // organicLayoutModule.setBufferedMode(true);
        // fLayoutModules.put("Organic", organicLayoutModule);

        SmartOrganicLayoutModule smartOrganicLayoutModule = createSmartOrganicLayoutModule();
        fLayoutModules.put("Smart Organic", smartOrganicLayoutModule);

        // y.module.RandomLayoutModule randomLayoutModule = new y.module.RandomLayoutModule();
        // randomLayoutModule.setBufferedMode(true);
        // fLayoutModules.put("Random", randomLayoutModule);

        // y.module.CircularLayoutModule circularLayoutModule = new y.module.CircularLayoutModule();
        // circularLayoutModule.setBufferedMode(true);
        // fLayoutModules.put("Circular", circularLayoutModule);
        //
        // y.module.HierarchicLayoutModule hierarchicLayoutModule = new y.module.HierarchicLayoutModule();
        // hierarchicLayoutModule.setBufferedMode(true);
        // fLayoutModules.put("Hierarchic", hierarchicLayoutModule);

        y.module.IncrementalHierarchicLayoutModule incrementalHierarchicLayoutModule = createIncrementalHierarchicLayoutModule();
        fLayoutModules.put("Incremental Hierarchic", incrementalHierarchicLayoutModule);

        // y.module.OrthogonalLayoutModule orthogonalLayoutModule = new y.module.OrthogonalLayoutModule();
        // orthogonalLayoutModule.setBufferedMode(true);
        // fLayoutModules.put("Orthogonal", orthogonalLayoutModule);

        fGraph.initLayoutModule(incrementalHierarchicLayoutModule);
    }

    /**
     * Creates the incremental hierarchic layout module.
     * 
     * @return the incremental hierarchic layout module
     */
    private IncrementalHierarchicLayoutModule createIncrementalHierarchicLayoutModule() {
        IncrementalHierarchicLayoutModule layoutModule = new IncrementalHierarchicLayoutModule();

        layoutModule.setBufferedMode(true);

        OptionHandler options = layoutModule.getOptionHandler();
        OptionItem item = options.getItem("GROUP_LAYERING_STRATEGY");
        item.setValue("RECURSIVE_LAYERING");
        item = options.getItem("SELECTED_ELEMENTS_INCREMENTALLY");
        item.setValue(true);

        return layoutModule;
    }

    /**
     * Creates the smart organic layout module.
     * 
     * @return the smart organic layout module
     */
    private SmartOrganicLayoutModule createSmartOrganicLayoutModule() {
        SmartOrganicLayoutModule layoutModule = new SmartOrganicLayoutModule();

        layoutModule.setBufferedMode(true);

        OptionHandler options = layoutModule.getOptionHandler();
        OptionItem item = options.getItem("AVOID_NODE_EDGE_OVERLAPS");
        item.setValue(true);

        item = options.getItem("MINIMAL_NODE_DISTANCE");
        item.setValue(30d);

        item = options.getItem("SCOPE");
        item.setValue("MAINLY_SUBSET");

        item = options.getItem("ACTIVATE_DETERMINISTIC_MODE");
        item.setValue(true);

        return layoutModule;
    }

    /**
     * Create a new HierarchyEditMode that uses the edge tips defined on {@link GraphLoader}.
     * 
     * @return the configured edit mode
     */
    private HierarchyEditMode createHierarchyEditMode() {
        HierarchyEditMode mode = new HierarchyEditMode() {
            @Override
            protected String getEdgeTip(Edge edge) {
                return fGraph.getEdgeTip(edge);
            }

            @Override
            protected String getNodeTip(Node node) {
                return fGraph.getNodeTip(node);
            }
        };

        mode.allowBendCreation(false);
        mode.allowEdgeCreation(false);
        mode.allowMoveLabels(false);
        mode.allowMovePorts(false);
        mode.allowNodeCreation(false);
        mode.allowNodeEditing(false);
        mode.allowResizeNodes(false);
        // show tool tips over nodes & edges
        mode.showNodeTips(true);
        mode.showEdgeTips(true);
        return mode;
    }

    /**
     * Return the graph.
     * 
     * @return The graph
     */
    public GraphManager getGraph() {
        return fGraph;
    }

    /**
     * Return the graph view.
     * 
     * @return the Graph2DView this panel contains.
     */
    public Graph2DView getView() {
        return fGraphView;
    }

    /**
     * return the command controller.
     * 
     * @return the command controller
     */
    public CommandController getCommandController() {
        return fCommandController;
    }

    /**
     * Return the hierarchy manager.
     * 
     * @return The hierarchy manager
     */
    public HierarchyManager getHierarchyManager() {
        return getView().getGraph2D().getHierarchyManager();
    }

    /**
     * Return the edge grouper.
     * 
     * @return The edge grouper
     */
    public EdgeGrouper getEdgeGrouper() {
        return fEdgeGrouper;
    }

    /**
     * Return the graph loader.
     * 
     * @return The graph loader
     */
    public GraphLoader getGraphLoader() {
        return fGraphLoader;
    }

    /**
     * Return the polymetric view data collector.
     * 
     * @return The polymetric view data collector
     */
    public PolymetricViewDataContainer getPolymetricViewDataCollector() {
        return fPolymetricViewDataCollector;
    }

    /**
     * Return the view config model.
     * 
     * @return The view config model
     */
    public ViewConfigModel getViewConfigModel() {
        return fViewConfigModel;
    }

    /**
     * Return the map of layout modules.
     * 
     * @return The layout modules.
     */
    public Map<String, LayoutModule> getLayoutModules() {
        return fLayoutModules;
    }

    /**
     * Return the toolbar.
     * 
     * @return The toolbar.
     */
    public DA4JavaToolbar getToolbar() {
        return fToolbar;
    }
}
