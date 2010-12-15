/**
 * 
 */
package org.evolizer.metrics.model;

import org.evolizer.core.hibernate.model.api.IEvolizerModelProvider;
import org.evolizer.metrics.model.entities.AbstractMeasurement;

/**
 * This classed is used by the evolizer hibernate plug-in to register the measurement model.
 * 
 * @author pinzger
 * 
 */
public class EvolizerMetricsModelProvider implements IEvolizerModelProvider {

    /**
     * {@inheritDoc}
     */
    public Class<?>[] getAnnotatedClasses() {
        Class<?>[] annotatedClasses = {AbstractMeasurement.class};

        return annotatedClasses;
    }
}
