/**
 * 
 */
package org.evolizer.famix.metrics.strategies.nom;

import java.util.List;

import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.utils.SnapshotAnalyzer;

/**
 * Strategy to calculate the number of class (i.e., static) methods.
 * Counts all static methods of contained classes and their inner and anonymous classes.
 * 
 * @author pinzger
 */
public class FamixNumberOfStaticMethodsPackageStrategy extends AbstractFamixNumberOfStaticMethodsStrategy {

    /** 
     * {@inheritDoc}
     */
    @Override
    protected double calculate() {
        double value = 0d;
        SnapshotAnalyzer snapshotAnalyzer = new SnapshotAnalyzer(getCurrentSession());
        List<AbstractFamixEntity> entities = snapshotAnalyzer.getDescendants(getCurrentEntity());
        for (AbstractFamixEntity famixEntity : entities) {
            if (famixEntity instanceof FamixMethod) {
                FamixMethod method = (FamixMethod) famixEntity;
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
