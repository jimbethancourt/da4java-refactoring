/**
 * 
 */
package org.evolizer.famix.metrics.strategies.noa;

import java.util.List;

import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAttribute;
import org.evolizer.famix.model.utils.SnapshotAnalyzer;


/**
 * Strategy to calculate the number of static attributes.
 * Counts all static attributes of contained classes and their inner and anonymous classes.
 * 
 * @author pinzger
 */
public class FamixNumberOfStaticAttributesPackageStrategy extends AbstractFamixNumberOfStaticAttributesStrategy {

    /** 
     * {@inheritDoc}
     */
    @Override
    protected double calculate() {
        double value = 0d;
        SnapshotAnalyzer snapshotAnalyzer = new SnapshotAnalyzer(getCurrentSession());
        List<AbstractFamixEntity> entities = snapshotAnalyzer.getDescendants(getCurrentEntity());
        for (AbstractFamixEntity famixEntity : entities) {
            if (famixEntity instanceof FamixAttribute) {
                FamixAttribute method = (FamixAttribute) famixEntity;
                if ((method.getModifiers() & AbstractFamixEntity.MODIFIER_STATIC) == AbstractFamixEntity.MODIFIER_STATIC) {
                    value += 1d;
                }
            }
        }

        return value;
    }

    /** 
     * {@inheritDoc}
     */
    public Class<?>[] getCompatibleTypes() {
        return new Class<?>[] { org.evolizer.famix.model.entities.FamixPackage.class };
    }
}
