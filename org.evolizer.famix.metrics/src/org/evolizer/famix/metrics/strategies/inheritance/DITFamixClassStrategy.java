/**
 * 
 */
package org.evolizer.famix.metrics.strategies.inheritance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evolizer.famix.model.entities.AbstractFamixGeneralization;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.utils.SnapshotAnalyzer;


/**
 * Calculate the DIT (depth of inheritance) metric. This denote the position of a class in the
 * inheritance tree. In case of multiple inheritance the metric provides the maximum length path.
 * 
 * @author pinzger
 *
 */
public class DITFamixClassStrategy extends AbstractInheritanceFamixClassStrategy {
    private static final String identifier = "DIT";
    private static final String description = "Calculates the position of a class in the inheritance tree";

    /** 
     * {@inheritDoc}
     */
    @Override
    protected double calculate() {
        double value = 0d;
        FamixClass famixClass = (FamixClass) getCurrentEntity();
        SnapshotAnalyzer snapshotAnalyzer = new SnapshotAnalyzer(getCurrentSession());
        List<FamixClass> entities = new ArrayList<FamixClass>();
        entities.add(famixClass);
        Set<FamixClass> dependentEntities = new HashSet<FamixClass>();
        value = snapshotAnalyzer.queryDependentEntities(
                entities, 
                dependentEntities, 
                FamixClass.class,
                AbstractFamixGeneralization.class,
                "from",
                0,
                -1);
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
