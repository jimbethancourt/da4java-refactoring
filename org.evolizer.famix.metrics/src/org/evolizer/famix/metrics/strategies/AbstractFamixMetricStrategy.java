/**
 * 
 */
package org.evolizer.famix.metrics.strategies;

import org.evolizer.core.exceptions.EvolizerRuntimeException;
import org.evolizer.core.hibernate.session.api.IEvolizerSession;
import org.evolizer.famix.metrics.model.FamixMeasurement;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.metrics.store.IMetricCalculationStrategy;


/**
 *
 * @author pinzger
 *
 */
public abstract class AbstractFamixMetricStrategy implements IMetricCalculationStrategy {
    private IEvolizerSession fCurrentSession = null;
    private AbstractFamixEntity fCurrentEntity = null;
    
    /** 
     * {@inheritDoc}
     */
    public double calculateValue(Object entity, IEvolizerSession session) throws EvolizerRuntimeException {
        fCurrentEntity = (AbstractFamixEntity) entity;
        fCurrentSession = session;
        
        String query = "from FamixMeasurement as fm " +
        		"where fm.identifier='" + this.getIdentifier() + "' " + 
        		"and fm.entity.id = '" + fCurrentEntity.getId() + "'";
        // look in DB if there is already a value stored for that calculation
        FamixMeasurement measurement = session.uniqueResult(query, FamixMeasurement.class);
        if (measurement == null) {
            // no value in DB stored, so calculate the value and store it in DB
            measurement = new FamixMeasurement(fCurrentEntity, this.getIdentifier());
            measurement.setValue(calculate());
            fCurrentSession.startTransaction();
            fCurrentSession.saveObject(measurement);
            fCurrentSession.endTransaction();
        }
        
        return measurement.getValue();
    }

    /**
     * Calculation of the specific metric implemented in sub-classes.
     * 
     * @param entity    The FAMIX entity.
     * @return  The value.
     */
    protected abstract double calculate();
    
    public IEvolizerSession getCurrentSession() {
        return fCurrentSession;
    }
    
    public AbstractFamixEntity getCurrentEntity() {
        return fCurrentEntity;
    }
}
