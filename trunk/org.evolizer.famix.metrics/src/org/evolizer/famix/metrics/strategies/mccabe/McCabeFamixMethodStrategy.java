package org.evolizer.famix.metrics.strategies.mccabe;

import org.evolizer.famix.model.entities.AbstractFamixEntity;

/**
 * Strategy to calculated a McCabe metric value for a FamixMethod
 * 
 * @author Reto Zenger
 * 
 */
public class McCabeFamixMethodStrategy extends McCabeFamixEntitiesStrategy {

	/*
	 * (non-Javadoc)
	 * @see org.evolizer.metrics.model.strategies.mccabe.McCabeForFamixEntities#calculateValue(org.evolizer.famix.model.entities.FamixEntity)
	 */
	@Override
	protected double calculateValue(AbstractFamixEntity fEntity) {
	    double value = 0d;
	    String code = fEntity.getJavaFileSourceCode();
        if (code != null && fEntity.getSourceAnchor() != null) {
            value = calculateMcCabe(code, fEntity.getSourceAnchor().getStartPos(), fEntity.getSourceAnchor().getEndPos());
        }
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see org.evolizer.metrics.model.strategies.IMetricStrategy#getPossibleEntities()
	 */
	public Class<?>[] getCompatibleTypes() {
		return new Class<?>[] { org.evolizer.famix.model.entities.FamixMethod.class };
	}

}
