/**
 * 
 */
package org.evolizer.famix.metrics.strategies.inheritance;

import java.util.List;
import java.util.Set;

import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixInvocation;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.utils.SnapshotAnalyzer;


/**
 * Calculate the number of methods extended, i.e., overridden with a call to the
 * overridden method in the super-class.
 *
 * @author pinzger 
 *
 */
public class NMEFamixClassStrategy extends AbstractInheritanceFamixClassStrategy {
    private static final String identifier = "NME";
    private static final String description = "Calculates the number of methods extending a method in one of the super-classes";

    /** 
     * {@inheritDoc}
     * 
     * TODO: Check this metric
     */
    @Override
    protected double calculate() {
        double value = 0d;
        FamixClass famixClass = (FamixClass) getCurrentEntity();
        SnapshotAnalyzer snapshotAnalyzer = new SnapshotAnalyzer(getCurrentSession());
        List<FamixClass> superClasses = querySuperClasses();

        List<FamixInvocation> invocations = snapshotAnalyzer.queryAssociationsOfEntities(
                famixClass.getMethods(), 
                FamixInvocation.class, 
                "from"
        );

        Set<String> superClassMethodNames = getMethodNames(superClasses);
        if (!superClassMethodNames.isEmpty()) {
            for (FamixMethod famixMethod : famixClass.getMethods()) {
                String famixMethodName = getMethodName(famixMethod.getUniqueName());
                if (superClassMethodNames.contains(famixMethodName)) {
                    for (FamixInvocation invocation : invocations) {
                        if (!famixMethod.equals(invocation.getTo()) 
                                && famixMethodName.equals(getMethodName(invocation.getTo().getUniqueName()))) {
                            value++;
                            break;
                        }
                    }
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
