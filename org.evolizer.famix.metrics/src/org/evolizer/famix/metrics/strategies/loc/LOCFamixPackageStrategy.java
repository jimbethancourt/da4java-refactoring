package org.evolizer.famix.metrics.strategies.loc;

import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixPackage;

/**
 * Strategy to calculate a LOC metric value for a FamixPackage.
 * 
 * @author zenger
 * 
 */
public class LOCFamixPackageStrategy extends AbstractLOCStrategy {

	@Override
	protected double calculate() {
		FamixPackage pEntity = (FamixPackage) getCurrentEntity();

		float value = 0f;
		for (AbstractFamixEntity famixEntity : pEntity.getChildren()) {
			if (famixEntity instanceof FamixClass) {
				value += (new LOCFamixClassStrategy()).calculateValue(famixEntity, getCurrentSession());
			}
		}

		return value;
	}

	public java.lang.Class<?>[] getCompatibleTypes() {
		return new java.lang.Class<?>[] { org.evolizer.famix.model.entities.FamixPackage.class };
	}

}
