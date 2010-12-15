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
 * This is the unchecked version of {@link EvolizerException}. It should be used whenever
 * an exception is not caused by e.g., malicious user input, non-responding servers, etc., but
 * may indicate a programming mistake instead (e.g., passed <code>null</code> to a method that
 * only operates on non-<code>null</code> values) or any other less likely cause.
 * 
 * Evolizer plug-ins should not throw/expose any other exception than {@link EvolizerException} or
 * {@link EvolizerRuntimeException} to other (Evolizer) plug-ins. For example, you should never throw an
 * {@link IOException} over plug-in boundaries, but instead catch it an re-throw an Evolizer exception.
 * 
 * @author wuersch
 */
@SuppressWarnings("serial")
public class EvolizerRuntimeException extends RuntimeException {

    /**
     * Instantiates a new {@link EvolizerRuntimeException}. The error message is set to 'Unexpected Exception'.
     */
    public EvolizerRuntimeException() {
        super("Unexpected Exception");
    }

    /**
     * Instantiates a new {@link EvolizerRuntimeException}.
     * 
     * @param message
     *            the error message
     */
    public EvolizerRuntimeException(String message) {
        super(message);
    }

    /**
     * Instantiates a new {@link EvolizerRuntimeException}.
     * 
     * @param message
     *            the error message
     * @param cause
     *            the exception that was caught and re-thrown
     */
    public EvolizerRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
