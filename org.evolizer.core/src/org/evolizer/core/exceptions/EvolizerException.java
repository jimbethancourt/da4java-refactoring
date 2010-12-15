/*
 * Copyright 2009 University of Zurich, Switzerland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.evolizer.core.exceptions;

import java.io.IOException;

/**
 * Checked Evolizer Exception. Intended to be used whenever exceptions are likely to occur (e.g. while processing user
 * input). In case where an exception is "unlikely to occur", e.g., when it might be raised because of a programming
 * error, please use {@link EvolizerRuntimeException} instead to keep the code clean from unnecessary try-catch blocks.
 * <p>
 * Evolizer plug-ins should not throw/expose any other exception than {@link EvolizerException} or
 * {@link EvolizerRuntimeException} to other (Evolizer) plug-ins. For example, you should never throw an
 * {@link IOException} over plug-in boundaries, but instead catch it an re-throw an Evolizer exception.
 * 
 * @author wuersch
 */
public class EvolizerException extends Exception {

    private static final long serialVersionUID = -2318207269420156450L;

    /**
     * Constructor.
     * 
     * @param message
     *            the error message
     */
    public EvolizerException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param cause
     *            the exception that was caught and re-thrown
     */
    public EvolizerException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            an error message.
     * @param cause
     *            the exception that was caught and re-thrown
     */
    public EvolizerException(String message, Throwable cause) {
        super(message, cause);
    }
}
