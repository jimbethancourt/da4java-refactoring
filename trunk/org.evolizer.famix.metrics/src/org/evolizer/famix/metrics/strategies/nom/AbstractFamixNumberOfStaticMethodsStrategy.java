/**
 * 
 */
package org.evolizer.famix.metrics.strategies.nom;

import org.evolizer.famix.metrics.strategies.AbstractFamixMetricStrategy;


/**
 * Abstract class for the number of static methods strategies.
 *
 * @author pinzger
 *
 */
public abstract class AbstractFamixNumberOfStaticMethodsStrategy extends AbstractFamixMetricStrategy {
    private static final String identifier = "NOSM";
    private static final String description = "Calculates the number of static methods";
  
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
