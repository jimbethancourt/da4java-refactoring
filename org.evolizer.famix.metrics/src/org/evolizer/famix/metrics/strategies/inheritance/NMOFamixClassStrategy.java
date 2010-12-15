/**
 * 
 */
package org.evolizer.famix.metrics.strategies.inheritance;

import java.util.List;
import java.util.Set;

import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixMethod;


/**
 * Calculate the NOC (number of overridden methods) metric.
 * Interface classes are skipped. Class initializers are also skipped.
 *
 * @author pinzger
 *
 */
public class NMOFamixClassStrategy extends AbstractInheritanceFamixClassStrategy {
    private static final String identifier = "NMO";
    private static final String description = "Calculates the number of methods overriding a method in one of the super-classes";

    /** 
     * {@inheritDoc}
     */
    @Override
    protected double calculate() {
        double value = 0d;
        FamixClass famixClass = (FamixClass) getCurrentEntity();
        List<FamixClass> superClasses = querySuperClasses();
        
        Set<String> superClassMethodNames = getMethodNames(superClasses);
        if (!superClassMethodNames.isEmpty()) {
            for (FamixMethod famixMethod : famixClass.getMethods()) {
                if (superClassMethodNames.contains(getMethodName(famixMethod.getUniqueName()))) {
                    value++;
                }
            }
        }
        return value;
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
