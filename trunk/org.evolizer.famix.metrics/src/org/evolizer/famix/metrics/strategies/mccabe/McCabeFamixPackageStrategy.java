package org.evolizer.famix.metrics.strategies.mccabe;

import java.util.Set;

import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixPackage;

/**
 * Strategy to calculated a McCabe metric value for a FamixPackage
 * 
 * @author Reto Zenger
 * 
 */
public class McCabeFamixPackageStrategy extends McCabeFamixEntitiesStrategy {

	@Override
	protected double calculateValue(AbstractFamixEntity entity) {
        double value = 0d;
		FamixPackage pEntity = (FamixPackage) entity;
		Set<AbstractFamixEntity> children = pEntity.getChildren();
		for (AbstractFamixEntity famixEntity : children) {
			if (famixEntity instanceof FamixClass) {
				//TODO: Alternativen als Summer anbieten (Median, Mittel, Modus)
				value += (new McCabeFamixClassStrategy()).calculateValue(famixEntity);
			}
		}

		return value;
	}

	public java.lang.Class<?>[] getCompatibleTypes() {
		return new java.lang.Class<?>[] { org.evolizer.famix.model.entities.FamixPackage.class };
	}

}
