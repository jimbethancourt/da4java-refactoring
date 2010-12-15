/**
 * 
 */
package org.evolizer.famix.metrics.strategies.nopar;

import org.evolizer.famix.metrics.strategies.AbstractFamixMetricStrategy;
import org.evolizer.famix.model.entities.FamixMethod;


/**
 * Strategy to calculate the number of instance methods.
 * 
 * @author pinzger
 */
public class FamixNumberOfMethodParametersStrategy extends AbstractFamixMetricStrategy {
    private static final String identifier = "NOPAR";
    private static final String description = "Calculates the number of method parameters";

    /** 
     * {@inheritDoc}
     */
    @Override
    protected double calculate() {
        double value = 0d;
        FamixMethod method = (FamixMethod) getCurrentEntity();
        value = method.getParameters().size();
        return value;
    }

    /** 
     * {@inheritDoc}
     */
    public Class<?>[] getCompatibleTypes() {
        return new Class<?>[] { org.evolizer.famix.model.entities.FamixMethod.class };
    }

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
