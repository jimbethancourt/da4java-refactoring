package org.evolizer.famix.metrics.strategies.mccabe;

import org.evolizer.core.exceptions.EvolizerRuntimeException;
import org.evolizer.core.hibernate.session.api.IEvolizerSession;
import org.evolizer.famix.metrics.model.FamixMeasurement;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.metrics.model.entities.AbstractMeasurement;

/**
 * Abstract Superclass of all Strategies to calculated a McCabe metric value for
 * FamixEntities
 * 
 * @author Reto Zenger
 * 
 */
public abstract class McCabeFamixEntitiesStrategy extends McCabeStrategy {

	protected AbstractMeasurement measurement;

	public double calculateValue(Object entity, IEvolizerSession session) throws EvolizerRuntimeException {
	    AbstractFamixEntity fEntity = (AbstractFamixEntity) entity;
		String query = "from FamixMeasurement as fm where fm.identifier='McCabe' and fm.entity.id = '"
				+ fEntity.getId() + "'";
		measurement = session.uniqueResult(query, FamixMeasurement.class);
		if (measurement == null) {
			FamixMeasurement fMeasurement = new FamixMeasurement(fEntity, "McCabe");
			fMeasurement.setValue(calculateValue(fEntity));
			measurement = fMeasurement;
			session.startTransaction();
			session.saveObject(fMeasurement);
			session.endTransaction();
		}
		return measurement.getValue();

	}

	/*
	 * calculation of the McCabe metric value for FamixEntities
	 */
	protected abstract double calculateValue(AbstractFamixEntity entity);
}
