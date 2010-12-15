/**
 * 
 */
package org.evolizer.famix.metrics.strategies.nom;

import org.evolizer.famix.metrics.strategies.AbstractFamixMetricStrategy;


/**
 * Abstract class for the number of methods strategies.
 *
 * @author pinzger
 *
 */
public abstract class AbstractFamixNumberOfMethodsStrategy extends AbstractFamixMetricStrategy {
    private static final String identifier = "NOM";
    private static final String description = "Calculates the number of methods";
  
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
