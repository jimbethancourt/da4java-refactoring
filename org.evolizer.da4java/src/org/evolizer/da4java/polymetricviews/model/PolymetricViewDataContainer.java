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
package org.evolizer.da4java.polymetricviews.model;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import org.evolizer.core.exceptions.EvolizerException;
import org.evolizer.core.util.collections.CompositeKey;
import org.evolizer.da4java.graph.data.DependencyGraph;
import org.evolizer.da4java.graph.panel.DA4JavaGraphPanel;
import org.evolizer.da4java.polymetricviews.PolymetricViewControllerView;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.entities.FamixPackage;
import org.evolizer.famix.model.utils.SnapshotAnalyzer;
import org.evolizer.metrics.store.MetricStore;

import y.base.Node;
import y.base.NodeMap;

/**
 * Helper class for caching metric values of represented FAMIX entities and the max metric values
 * per FAMIX entity type.
 * 
 * @author pinzger
 */
public class PolymetricViewDataContainer {
    
    /** The logger. */
//    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(PolymetricViewDataContainer.class.getName());

    /** Cache of max metric values per FAMIX entity type. */
    private HashMap<String, HashMap<java.lang.Class<? extends AbstractFamixEntity>, Float>> fMaxMetricValues;

    /** Cache of values per FAMIX entity and metric. */
    private Hashtable<CompositeKey<AbstractFamixEntity, String>, Float> fFamixEntityMetricToValueMap;

    /** Graph panel containing the graph for which to keep the metric values. */
    private DA4JavaGraphPanel fGraphPanel; 

    /**
     * The constructor.
     * 
     * It has to assured that an editor instance is opened, otherwise we will get an exception here.
     * 
     * @param graphPanel the graph panel
     */
    public PolymetricViewDataContainer(DA4JavaGraphPanel graphPanel) {
        this.fGraphPanel = graphPanel;
        this.fFamixEntityMetricToValueMap = new Hashtable<CompositeKey<AbstractFamixEntity, String>, Float>();
        this.fMaxMetricValues = new HashMap<String, HashMap<java.lang.Class<? extends AbstractFamixEntity>, Float>>();
        initMaxValuesMap();
    }

    /**
     * Method that initializes the fMaxMetricValues map. For each metric the
     * maxValues of a package, a class and a method is initialized by 0.
     */
    private void initMaxValuesMap() {
        for (String metric : MetricStore.listAllMetrics()) {
            HashMap<java.lang.Class<? extends AbstractFamixEntity>, Float> hashMap = new HashMap<java.lang.Class<? extends AbstractFamixEntity>, Float>();

            hashMap.put(FamixPackage.class, 0f);
            hashMap.put(FamixClass.class, 0f);
            hashMap.put(FamixMethod.class, 0f);

            fMaxMetricValues.put(metric, hashMap);
        }
    }

    /**
     * Updates the fRealizerToValue Map that contains NodeRealizers and their
     * corresponding metric value.
     * 
     * @param metricIdentifier the metric identifier
     * 
     * @throws EvolizerException the evolizer exception
     */
    public void updateMetricValues(String metricIdentifier) throws EvolizerException {
        SnapshotAnalyzer snapshotAnalyzer = fGraphPanel.getGraphLoader().getSnapshotAnalyzer();
        DependencyGraph graph = fGraphPanel.getGraph();
        NodeMap map = graph.getRegisteredNodeMaps()[0];
        Node[] nodes = graph.getNodeArray();
        for (int i = 0; i < nodes.length; i++) {
            AbstractFamixEntity entity = (AbstractFamixEntity) map.get(nodes[i]);
            // check if entity is already processed
            // entities ending with <clinit>() or <oinit>() must be ignored to 
            // get suitable visual data
            Set<String> possibleMetrics = MetricStore.listMetricsFor(entity);
            if (!metricIdentifier.equalsIgnoreCase(PolymetricViewControllerView.METRIC_UNIFORM)) {
                if (possibleMetrics.contains(metricIdentifier)) {
                    CompositeKey<AbstractFamixEntity, String> key = new CompositeKey<AbstractFamixEntity, String>(entity, metricIdentifier);
                    if (!fFamixEntityMetricToValueMap.containsKey(key)  
                            && !(entity.getUniqueName().endsWith(AbstractFamixEntity.CLASS_INIT_METHOD) 
                                    || entity.getUniqueName().endsWith(AbstractFamixEntity.OBJECT_INIT_METHOD))) {
                        Float value = 0f;
                        value = new Float(MetricStore.calculateMetricValue(entity, metricIdentifier, snapshotAnalyzer.getEvolizerSession()));
                        fFamixEntityMetricToValueMap.put(key, value);

                        if (fMaxMetricValues.get(metricIdentifier).get(entity.getClass()) < value) {
                            fMaxMetricValues.get(metricIdentifier).put(entity.getClass(), value);
                        }
                    }
                }
            }
        }
    }

    /**
     * Check whether value map contains an entry for the given FAMIX entity and metric.
     * 
     * @param entity   The FAMIX entity.
     * @param metricIdentifier The metric identifier.
     * 
     * @return True, if it contains a value entry, otherwise false.
     */
    public boolean containsEntry(AbstractFamixEntity entity, String metricIdentifier) {
        return fFamixEntityMetricToValueMap.containsKey(new CompositeKey<AbstractFamixEntity, String>(entity, metricIdentifier));
    }

    /**
     * Return the metric value for the given FAMIX entity and metric.
     * 
     * @param entity   The FAMIX entity.
     * @param metricIdentifier The metric identifier.
     * 
     * @return The metric value.
     */
    public Float getValue(AbstractFamixEntity entity, String metricIdentifier) {
        return fFamixEntityMetricToValueMap.get(new CompositeKey<AbstractFamixEntity, String>(entity, metricIdentifier));
    }

    /**
     * Returns the MaxValue for the given metric of the given AbstractFamixEntity type.
     * 
     * @param metric the metric
     * @param type the type
     * 
     * @return the max value
     */
    public Float getMaxValue(String metric, java.lang.Class<? extends AbstractFamixEntity> type) {
        return fMaxMetricValues.get(metric).get(type);
    }
}
