/**
 * 
 */
package org.evolizer.famix.metrics.strategies.noa;

import org.evolizer.famix.metrics.strategies.AbstractFamixMetricStrategy;


/**
 * Abstract class for the number of attributes strategies.
 *
 * @author pinzger
 *
 */
public abstract class AbstractFamixNumberOfAttributesStrategy extends AbstractFamixMetricStrategy {
    private static final String identifier = "NOA";
    private static final String description = "Calculates the number of attributes";
  
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
