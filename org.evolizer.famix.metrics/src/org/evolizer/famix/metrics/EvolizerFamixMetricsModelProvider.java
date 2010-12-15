/**
 * 
 */
package org.evolizer.famix.metrics;

import org.evolizer.core.hibernate.model.api.IEvolizerModelProvider;
import org.evolizer.famix.metrics.model.FamixMeasurement;


/**
 *
 * @author pinzger
 *
 */
public class EvolizerFamixMetricsModelProvider implements IEvolizerModelProvider {

    /** 
     * {@inheritDoc}
     */
    public Class<?>[] getAnnotatedClasses() {
        Class<?>[] annotatedClasses =
        {
                FamixMeasurement.class,
        };

        return annotatedClasses;
    }
}
