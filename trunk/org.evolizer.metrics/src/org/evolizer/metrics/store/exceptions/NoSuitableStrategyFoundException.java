package org.evolizer.metrics.store.exceptions;

import org.evolizer.core.exceptions.EvolizerRuntimeException;

/**
 * Exception that is thrown if no suitable strategy is found while trying to calculate a metric for an entity.
 * 
 * @author wuersch
 */
public class NoSuitableStrategyFoundException extends EvolizerRuntimeException {

    private static final long serialVersionUID = -3858570337819549641L;

    /**
     * Constructor that just uses a default error message.
     */
    public NoSuitableStrategyFoundException() {
        super("Could not find a suitable strategy for calculating the metric.");
    }

    /**
     * Constructor that allows to use a customized error message.
     * 
     * @param message
     *            the customized error message.
     */
    public NoSuitableStrategyFoundException(String message) {
        super(message);
    }
}
