package org.evolizer.famix.metrics.strategies.fan_in_out;

import java.util.List;

import org.evolizer.famix.metrics.strategies.AbstractFamixMetricStrategy;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixInvocation;
import org.evolizer.famix.model.utils.SnapshotAnalyzer;

/**
 * Abstract Superclass of all Strategies to calculated a Fan-In metric value
 * 
 * @author Reto Zenger, pinzger
 *
 */
public class FamixFanInStrategy extends AbstractFamixMetricStrategy {
	private static final String identifier = "Fan-In";
	private static final String description = "Calculates the number of modules that call the selected entity";

	/** 
	 * {@inheritDoc}
	 */
	@Override
	protected double calculate() {
	    double value = 0d;
	    SnapshotAnalyzer snapshotAnalyzer = new SnapshotAnalyzer(getCurrentSession());
	    List<AbstractFamixEntity> entities = snapshotAnalyzer.getDescendants(getCurrentEntity());
	    List<FamixInvocation> invocations = snapshotAnalyzer.queryAssociationsOfEntities(entities, FamixInvocation.class, "to");
	    value = invocations.size();

	    return value;
	}

	public String getDescription() {
		return description;
	}

	public String getIdentifier() {
		return identifier;
	}

    /** 
     * {@inheritDoc}
     */
    public Class<?>[] getCompatibleTypes() {
        return new Class<?>[] { org.evolizer.famix.model.entities.FamixPackage.class,
                org.evolizer.famix.model.entities.FamixClass.class,
                org.evolizer.famix.model.entities.FamixMethod.class
        };
    }
}
