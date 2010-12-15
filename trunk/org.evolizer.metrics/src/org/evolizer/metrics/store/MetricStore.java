package org.evolizer.metrics.store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.evolizer.core.hibernate.session.api.IEvolizerSession;
import org.evolizer.core.util.collections.CompositeKey;
import org.evolizer.metrics.store.exceptions.NoSuitableStrategyFoundException;

/**
 * This class provides a single point of access for calculating various metrics for evolizer entities. Whenever the
 * plug-in is initialized, {@link org.evolizer.metrics.EvolizerMetricsPlugin#initStrategies()} will gather all known
 * strategies for metrics calculation and register them with the store by invoking
 * {@link MetricStore#register(IMetricCalculationStrategy)}.
 * <p>
 * The most important method of this class is {@link MetricStore#calculateMetricValue(Object, String, IEvolizerSession)}
 * . This method checks whether there is a strategy registered that is capable of calculating the given metric
 * (identified by a string) for the class of the object that was passed to the method. If a suitable strategy is found,
 * then it will usually first check whether there is already a result for the given entity stored within the RHDB. If
 * not, it will calculate the value and persist it.
 * <p>
 * Usage:
 * 
 * <pre>
 * IEvolizerSession activeEvolizerSession = ...
 * 
 * Set<String> applicableMetrics = MetricStore.listMetricsFor(myFamixClass);
 * for(String metricIdentifier : applicableMetrics) {
 *     double loc = MetricStore.calculateMetricValue(myFamixClass, metricIdentifier, activeEvolizerSession);
 *     // ...
 * }
 * </pre>
 * 
 * Metric calculation strategies are looked up with a composite key, consisting of a short string that identifies the
 * kind of metric (e.g., LOC) and the type of the object that a strategy can handle (e.g., FamixClass.class). Like that
 * it is possible to e.g., register two different LOC strategies - one for FamixClasses and one for FamixMethods.
 * 
 * @author wuersch, zenger
 */
// FIXME Find a consistent way for session handling.
// FIXME Test!
public final class MetricStore {

    // TODO use "priority" list instead of map? Last strategy == default/fall-back strategy (e.g., together xml)
    private static Map<CompositeKey<String, Class<?>>, IMetricCalculationStrategy> sRegisteredStrategies =
            new HashMap<CompositeKey<String, Class<?>>, IMetricCalculationStrategy>();

    /**
     * Constructor. Utility class, therefore no public constructor (thanks for reminding me, checkstyle).
     */
    private MetricStore() {
        super();
    }

    /**
     * Called by {@link org.evolizer.metrics.EvolizerMetricsPlugin#initStrategies()} during start-up. Checks for
     * extensions that contribute a strategy to calculate metrics for particular entities.
     * 
     * @param strategy
     *            the strategy that should be registered.
     */
    public static void register(IMetricCalculationStrategy strategy) {
        for (Class<?> c : strategy.getCompatibleTypes()) {
            sRegisteredStrategies.put(new CompositeKey<String, Class<?>>(strategy.getIdentifier(), c), strategy);
        }
    }

    /**
     * Lists all the metrics that can be calculated for a given object's class. No calculation will happen.
     * 
     * @param entity
     *            the object for which we want to query the available metrics.
     * @return a set of strings with the abbreviated names of the metrics that apply to the given object (e.g., LOC,
     *         MCCABE, etc.).
     */
    public static Set<String> listMetricsFor(Object entity) {
        Set<String> result = new HashSet<String>();

        for (CompositeKey<String, Class<?>> key : sRegisteredStrategies.keySet()) {
            if (key.second().equals(entity.getClass())) {
                result.add(key.first());
            }
        }

        return result;
    }

    /**
     * Lists the metrics that can be calculated for a given type.
     * 
     * @param type
     *          the type for which to query available metrics
     * @return the set of strings with the abbreviated metrics names.
     */
    public static Set<String> listMetricsFor(Class<?> type) {
        Set<String> result = new HashSet<String>();

        for (CompositeKey<String, Class<?>> key : sRegisteredStrategies.keySet()) {
            if (key.second().equals(type)) {
                result.add(key.first());
            }
        }

        return result;
    }
    
    /**
     * Lists all the metrics that are registered in the metric store.
     * 
     * @return a set of strings with the abbreviated names of all the metrics that are available in the store.
     */
    public static Set<String> listAllMetrics() {
        Set<String> result = new HashSet<String>();
        for (CompositeKey<String, Class<?>> key : sRegisteredStrategies.keySet()) {
            result.add(key.first());
        }

        return result;
    }

    /**
     * Lists all the metrics that are applicable to each of the classes of the entities that were passed to the method
     * (i.e., an intersection of metrics). Useful if you, e.g., want to query what metrics can be displayed for a
     * selection of different entities.
     * 
     * @param entities
     *            the entities that we are interested in.
     * @return a set of strings with the abbreviated names of the metrics that apply to each of the given objects
     *         equally.
     */
    public static Set<String> listComonMetricsFor(Object... entities) {

        Set<String> tmp = null;
        for (Object entity : entities) {
            if (tmp == null) {
                tmp = listMetricsFor(entity);
            } else {
                boolean notRetainable = !tmp.retainAll(listMetricsFor(entity));
                if (notRetainable) {
                    return null;
                }
            }
        }

        return tmp;
    }

    /**
     * Calculates, stores, and returns a metric for a given entity (or just loads it from the RHDB, if it already
     * exists).
     * 
     * @param entity
     *            the entity for that we want to calculate a metric.
     * @param metricIdentifier
     *            a short identifier denoting the kind of metric (e.g., LOC).
     * @param session
     *            Evolizer session used to retrieve/store calculated measures.
     * @return the metric value
     * @throws NoSuitableStrategyFoundException
     *             if the store does not know how to retrieve a metric for the given entity.
     */
    public static double calculateMetricValue(Object entity, String metricIdentifier, IEvolizerSession session)
            throws NoSuitableStrategyFoundException {
        IMetricCalculationStrategy strategy =
                sRegisteredStrategies.get(new CompositeKey<String, Class<?>>(metricIdentifier, entity.getClass()));

        if (strategy == null) {
            throw new NoSuitableStrategyFoundException("Could not find a suitable " + metricIdentifier
                    + "-strategy for " + entity.getClass());
        }

        return strategy.calculateValue(entity, session); // TODO storage-template method in an abstract class that
        // implements IMetricCalculationStrategy?
    }
}
