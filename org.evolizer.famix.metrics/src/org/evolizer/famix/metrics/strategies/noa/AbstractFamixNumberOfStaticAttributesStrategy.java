/**
 * 
 */
package org.evolizer.famix.metrics.strategies.noa;

import org.evolizer.famix.metrics.strategies.AbstractFamixMetricStrategy;


/**
 * Abstract class for the number of static attributes strategies.
 *
 * @author pinzger
 */
public abstract class AbstractFamixNumberOfStaticAttributesStrategy extends AbstractFamixMetricStrategy {
    private static final String identifier = "NOSA";
    private static final String description = "Calculates the number of static attributes";

    /** 
     * {@inheritDoc}
     */
    public String getDescription() {
        return description;
    }

    /** 
     * {@inheritDoc}
     */
    public String getIdentifier() {
        return identifier;
    }

}
