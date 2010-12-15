package org.evolizer.metrics.model.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.evolizer.core.hibernate.model.api.IEvolizerModelEntity;

/**
 * Abstract Superclass for all measurements in evolizer.
 * 
 * @author Reto Zenger
 * 
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractMeasurement implements IEvolizerModelEntity {

    /**
     * Identifier of the used metric strategy
     */
    private String fIdentifier;

    /**
     * calculated value by the strategy
     */
    private double fValue;

    private Long fId;

    /**
     * Returns a short identifier, such as LOC.
     * 
     * @return the identifier, e.g., LOC.
     */
    public String getIdentifier() {
        return fIdentifier;
    }

    /**
     * Sets a short identifier.
     * 
     * @param identifier
     *            a short identifier, such as LOC.
     */
    public void setIdentifier(String identifier) {
        fIdentifier = identifier;
    }

    /**
     * Returns the value of the measurement.
     * 
     * @return a double containing the value of the measurement.
     */
    public double getValue() {
        return fValue;
    }

    /**
     * Sets the value of a measurement.
     * 
     * @param value
     *            a double containing the value of the measurement.
     */
    public void setValue(double value) {
        fValue = value;
    }

    /**
     * {@inheritDoc}
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return fId;
    }

    /**
     * {@inheritDoc}
     */
    protected void setId(Long id) {
        fId = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (fId ^ (fId >>> 32));
        result = prime * result + ((fIdentifier == null) ? 0 : fIdentifier.hashCode());
        long temp;
        temp = Double.doubleToLongBits(fValue);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractMeasurement other = (AbstractMeasurement) obj;
        if (fId != other.fId) {
            return false;
        }
        if (fIdentifier == null) {
            if (other.fIdentifier != null) {
                return false;
            }
        } else if (!fIdentifier.equals(other.fIdentifier)) {
            return false;
        }
        if (Double.doubleToLongBits(fValue) != Double.doubleToLongBits(other.fValue)) {
            return false;
        }
        return true;
    }

}
