package org.evolizer.metrics.store;

import org.evolizer.core.hibernate.session.api.IEvolizerSession;

/**
 * Interface for all metric strategies in evolizer. A metric strategy has an identifier (such as LOC), a description
 * (such as 'lines of code') and knows how to calculate/load and store a metric for a given type.
 * 
 * @author wuersch
 */
public interface IMetricCalculationStrategy {

    /**
     * Used to identify the kind of metric that the strategy is able to calculate.
     * 
     * @return a short identifier, such as LOC.
     */
    String getIdentifier(); // FIXME identifier is probably not the best name...

    /**
     * Returns a short description, such as 'lines of code', of the metric, useful e.g., in tool tips or equal.
     * 
     * @return a short description
     */
    String getDescription();

    /**
     * Performs the actual metrics calculation. It's behaviour is as follows:
     * <ol>
     * <li>Check whether there is already a metric value stored in the RHDB for the entity. If yes, return. Otherwise
     * proceed to step 2.</li>
     * <li>Calculate metric value for entity.</li>
     * <li>Store the value.</li>
     * <li>Return the value.</li>
     * </ol>
     * 
     * @param entity
     *            the entity for which we want to calculate the metric.
     * @param session
     *            a evolizer session used to cache the result.
     * @return the calculated/loaded metric value.
     */
    double calculateValue(Object entity, IEvolizerSession session);

    /**
     * Returns the types for which the strategy knows how to calculate the metric (e.g., FamixClass.class,
     * FamixMethod.class, etc.).
     * 
     * @return an array containing all the types for which the metric can be calculated by this strategy.
     */
    Class<?>[] getCompatibleTypes();
}
