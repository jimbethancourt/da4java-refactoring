/**
 * 
 */
package org.evolizer.famix.metrics.strategies.nom;

import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixMethod;


/**
 * Strategy to calculate the number of instance methods.
 * Only counts the methods declared by this class.
 * 
 * @author pinzger
 */
public class FamixNumberOfMethodsClassStrategy extends AbstractFamixNumberOfMethodsStrategy {

    /** 
     * {@inheritDoc}
     */
    @Override
    protected double calculate() {
        double value = 0d;
        FamixClass famixClass = (FamixClass) getCurrentEntity();
        for (FamixMethod famixMethod : famixClass.getMethods()) {
            if ((famixMethod.getModifiers() & AbstractFamixEntity.MODIFIER_STATIC) != AbstractFamixEntity.MODIFIER_STATIC) {
                value += 1d;
            }
        }

        return value;
    }

    /** 
     * {@inheritDoc}
     */
    public Class<?>[] getCompatibleTypes() {
        return new Class<?>[] { org.evolizer.famix.model.entities.FamixClass.class };
    }
}
