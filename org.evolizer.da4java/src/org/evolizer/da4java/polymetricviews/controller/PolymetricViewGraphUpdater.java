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
package org.evolizer.da4java.polymetricviews.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.evolizer.core.exceptions.EvolizerException;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.da4java.commands.AbstractGraphEditCommand;
import org.evolizer.da4java.graph.data.DependencyGraph;
import org.evolizer.da4java.graph.panel.DA4JavaGraphPanel;
import org.evolizer.da4java.graph.panel.rendering.GraphReLayouter;
import org.evolizer.da4java.polymetricviews.model.INormalizer;
import org.evolizer.da4java.polymetricviews.model.PolymetricViewDataContainer;
import org.evolizer.da4java.polymetricviews.model.PolymetricViewProfile;
import org.evolizer.da4java.visibility.ViewConfigModel;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.AbstractFamixVariable;

import y.base.GraphEvent;
import y.base.GraphListener;
import y.base.Node;
import y.base.NodeMap;
import y.view.NodeRealizer;

/**
 * Hander for applying the current polymetric view configuration to the graph.
 * The handler listens to structural changes in the graph signaled by
 * {@link GraphEvents} events and changes in the polymetric view configuration.
 * In case of such events "all" the realizers of all nodes currently displayed
 * by the graph are updated. A re-layout is only mandatory if the polymetric
 * view configuration changes. In case of structural changes the re-layout is
 * done by the {@link GraphReLayouter}.
 * 
 * @author pinzger
 */
public class PolymetricViewGraphUpdater implements PropertyChangeListener, GraphListener {
    
    /** The logger. */
    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(PolymetricViewGraphUpdater.class.getName());

    /** The graph panel. */
    private DA4JavaGraphPanel fGraphPanel;

    /**
     * The constructor.
     * 
     * @param graphPanel the graph panel
     */
    public PolymetricViewGraphUpdater(DA4JavaGraphPanel graphPanel) {
        fGraphPanel = graphPanel;
    }

    /** 
     * {@inheritDoc}
     */
    public void onGraphEvent(GraphEvent graphEvent) {
        if (graphEvent.getType() == GraphEvent.POST_EVENT) {
            if (graphEvent.getData() != null 
                    && graphEvent.getData() instanceof AbstractGraphEditCommand) {

                PolymetricViewProfile profile = fGraphPanel.getViewConfigModel().getProfile();
                List<AbstractPolymetricViewUpdater> polyViewUpdaters = new ArrayList<AbstractPolymetricViewUpdater>();
                polyViewUpdaters.add(new HeightUpdater(profile.getHeightMetric()));
                polyViewUpdaters.add(new WidthUpdater(profile.getWidthMetric()));
                polyViewUpdaters.add(new ColorUpdater(profile.getColorMetric()));

                try {
                    if (polyViewUpdaters.size() > 0) {
                        PolymetricViewDataContainer dataCollector = fGraphPanel.getPolymetricViewDataCollector();
                        for (AbstractPolymetricViewUpdater abstractPolymetricViewUpdater : polyViewUpdaters) {
                            dataCollector.updateMetricValues(abstractPolymetricViewUpdater.getMetricToRepresent());
                        }
                        updateNodeRealizers(polyViewUpdaters);
                        
//                        fGraphPanel.refreshLayoutNew(false, null, null);
                    }
                } catch (EvolizerException ee) {
                    sLogger.error("Could not update metric values in graph " + fGraphPanel.getName() + ee.getMessage());
                    ee.printStackTrace();
                }
            }
        }
    }

    /** 
     * {@inheritDoc}
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(ViewConfigModel.POLYMETRIC_VIEW_CHANGE)) {
            List<AbstractPolymetricViewUpdater> polyViewUpdaters = new ArrayList<AbstractPolymetricViewUpdater>();
            PolymetricViewProfile profile = (PolymetricViewProfile) event.getNewValue();
    
            if (event.getOldValue().equals(ViewConfigModel.UPDATE_NODE_HEIGHTS)) {
                polyViewUpdaters.add(new HeightUpdater(profile.getHeightMetric()));
            } else if (event.getOldValue().equals(ViewConfigModel.UPDATE_NODE_WIDTHS)) {
                polyViewUpdaters.add(new WidthUpdater(profile.getWidthMetric()));
            } else if (event.getOldValue().equals(ViewConfigModel.UPDATE_NODE_COLORS)) {
                polyViewUpdaters.add(new ColorUpdater(profile.getColorMetric()));
            } else if (event.getOldValue().equals(ViewConfigModel.UPDATE_GRAPH_EVENT)) {
                polyViewUpdaters.add(new HeightUpdater(profile.getHeightMetric()));
                polyViewUpdaters.add(new WidthUpdater(profile.getWidthMetric()));
                polyViewUpdaters.add(new ColorUpdater(profile.getColorMetric()));
            }       
    
            try {
                if (polyViewUpdaters.size() > 0) {
                    PolymetricViewDataContainer dataCollector = fGraphPanel.getPolymetricViewDataCollector();
                    for (AbstractPolymetricViewUpdater abstractPolymetricViewUpdater : polyViewUpdaters) {
                        dataCollector.updateMetricValues(abstractPolymetricViewUpdater.getMetricToRepresent());
                    }
                    updateNodeRealizers(polyViewUpdaters);
                    
                    fGraphPanel.getGraph().updatedNodeSizes();
                }
            } catch (EvolizerException ee) {
                sLogger.error("Could not update metric values in graph " + fGraphPanel.getName() + ee.getMessage());
                ee.printStackTrace();
            }
        }
    }
    
    /**
     * Applies all AbstractPolymetricViewUpdaters updateRealizer() method to each NodeRealizer in the graph.
     */
    private void updateNodeRealizers(List<AbstractPolymetricViewUpdater> polyViewUpdater) {
        PolymetricViewDataContainer dataCollector = fGraphPanel.getPolymetricViewDataCollector();
        
        DependencyGraph graph = fGraphPanel.getGraph();
        NodeMap map = graph.getRegisteredNodeMaps()[0];
        Node[] nodes = graph.getNodeArray();
        for (int i = 0; i < nodes.length; i++) {
            AbstractFamixEntity entity = (AbstractFamixEntity) map.get(nodes[i]);
            for (AbstractPolymetricViewUpdater updater : polyViewUpdater) {
                NodeRealizer realizer = graph.getRealizer(nodes[i]);
                String metricIdentifier = updater.getMetricToRepresent(); 
//                if(!metricIdentifier.equalsIgnoreCase(PolymetricViewControllerView.METRIC_UNIFORM)) {
                    if (dataCollector.containsEntry(entity, metricIdentifier)) {
                        float value = dataCollector.getValue(entity, metricIdentifier);
                        INormalizer normalizer = updater.getNormalizer();

                        float normalizedVal = normalizer.normalize(value, dataCollector.getMaxValue(metricIdentifier, entity.getClass()));
                        if (!(entity instanceof AbstractFamixVariable)) {
                            updater.updateNodeRealizer(realizer, normalizedVal);
                        }
                    } else {
//                        sLogger.error("No metric data found for metric: " + updater.getAttributeToRepresent());
                        if (!(entity instanceof AbstractFamixVariable)) {
                            updater.setDefaultRealizer(realizer);
                        }
                    }       
            }
        }       
    }

}
