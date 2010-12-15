package org.evolizer.famix.metrics.strategies.mccabe;

import org.evolizer.famix.model.entities.AbstractFamixEntity;

/**
 * Strategy to calculated a McCabe metric value for a FamixClass
 * 
 * @author Reto Zenger
 * 
 */
public class McCabeFamixClassStrategy extends McCabeFamixEntitiesStrategy {

	@Override
	protected double calculateValue(AbstractFamixEntity fEntity) {
        double value = 0d;
        String code = fEntity.getJavaFileSourceCode();
        if (code != null && fEntity.getSourceAnchor() != null) {
            value = calculateMcCabe(code, fEntity.getSourceAnchor().getStartPos(), fEntity.getSourceAnchor().getEndPos());
        }
        return value;
	}

	public Class<?>[] getCompatibleTypes() {
		return new Class<?>[] { org.evolizer.famix.model.entities.FamixClass.class };
	}

}
