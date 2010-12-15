package org.evolizer.famix.metrics.model;


import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.metrics.model.entities.AbstractMeasurement;

/**
 * Hibernate enabled data collection which can be stored in DB for calculations
 * of metric values of a FamixEntity
 * 
 * @author Reto Zenger
 * 
 */
@Entity
public class FamixMeasurement extends AbstractMeasurement {

	/**
	 * Entity of which the metric value is calculated
	 */
	private AbstractFamixEntity entity;

	public FamixMeasurement() {
	}

	public FamixMeasurement(AbstractFamixEntity entity, String identifier) {
		this.setIdentifier(identifier);
		this.entity = entity;
	}

	@ManyToOne
	public AbstractFamixEntity getEntity() {
		return entity;
	}

	public void setEntity(AbstractFamixEntity entity) {
		this.entity = entity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FamixMeasurement other = (FamixMeasurement) obj;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity))
			return false;
		return true;
	}

	@Transient
	public String getLabel() {
		return getIdentifier() + " of " + entity.getLabel();
	}

	@Transient
	public String getURI() {
		// TODO Auto-generated method stub
		return null;
	}
}
