/**
 * 
 */
package org.evolizer.famix.metrics.strategies.noa;

import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAttribute;
import org.evolizer.famix.model.entities.FamixClass;


/**
 * Strategy to calculate the number of attributes for classes.
 * Only counts the attributes declared by this class.
 *
 * @author pinzger
 */
public class FamixNumberOfAttributesClassStrategy extends AbstractFamixNumberOfAttributesStrategy {

    /** 
     * {@inheritDoc}
     */
    @Override
    protected double calculate() {
        double value = 0d;
        FamixClass famixClass = (FamixClass) getCurrentEntity();
        for (FamixAttribute attribute: famixClass.getAttributes()) {
            if ((attribute.getModifiers() & AbstractFamixEntity.MODIFIER_STATIC) != AbstractFamixEntity.MODIFIER_STATIC) {
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
