/**
 * 
 */
package org.evolizer.famix.metrics.strategies.inheritance;

import java.util.ArrayList;
import java.util.List;

import org.evolizer.famix.metrics.strategies.AbstractFamixMetricStrategy;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.AbstractFamixGeneralization;
import org.evolizer.famix.model.utils.SnapshotAnalyzer;


/**
 * Calculates the number of sub-classes that directly inherit from this class/interface.
 *
 * @author pinzger 
 */
public class NOCFamixClassStrategy extends AbstractFamixMetricStrategy {
    private static final String identifier = "NOC";
    private static final String description = "Calculates the number of direct sub-classes"; 
    
    /** 
     * {@inheritDoc}
     */
    @Override
    protected double calculate() {
        double value = 0d;
        SnapshotAnalyzer snapshotAnalyzer = new SnapshotAnalyzer(getCurrentSession());
        List<AbstractFamixEntity> entities = new ArrayList<AbstractFamixEntity>();
        entities.add(getCurrentEntity());
        List<AbstractFamixGeneralization> generalizations = snapshotAnalyzer.queryAssociationsOfEntities(entities, AbstractFamixGeneralization.class, "to");
        value = generalizations.size(); 
        return value;
    }

    /** 
     * {@inheritDoc}
     */
    public Class<?>[] getCompatibleTypes() {
        return new Class<?>[] { org.evolizer.famix.model.entities.FamixClass.class };
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
