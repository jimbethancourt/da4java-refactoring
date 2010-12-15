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
package org.evolizer.core.logging.appender;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Forget it, do not use it.
 * 
 * Not yet implemented, therefore no further class comment. :-P
 * 
 * @author wuersch
 */
// TODO this class does not work as intended. Revisit!
public final class EclipseLogAppender extends AppenderSkeleton {

    private ILog fPluginLog;
    // TODO Read from log4j-properties and decide whether more than errors should be
    private boolean fVerbose;

    // appended to eclipse log!
    private EclipseLogAppender() {
    // TODO remove private constructor - just to prevent anyone from using this class before it has been fixed.
    }

    /**
     * Sets the Eclipse log instance.
     * 
     * @param pluginLog
     *            the plugin log
     */
    public void setLog(ILog pluginLog) {
        fPluginLog = pluginLog;
    }

    /**
     * Logs an event. Translates level to status instance codes:
     * 
     * <pre>
     * level &gt; Level.ERROR - Status.ERROR level &gt; Level.WARN -
     * Status.WARNING level &gt; Level.DEBUG - Status.INFO default - Status.OK
     * </pre>
     * 
     * @param event
     *            LoggingEvent instance
     */
    @Override
    public void append(LoggingEvent event) {
        if (layout == null) {
            errorHandler.error("Missing layout for appender " + name, null, ErrorCode.MISSING_LAYOUT);

            return;
        }

        String text = layout.format(event);
        Throwable thrown = null;

        if (layout.ignoresThrowable()) {
            ThrowableInformation info = event.getThrowableInformation();

            if (info != null) {
                thrown = info.getThrowable();
            }
        }

        Level level = event.getLevel();
        int severity = IStatus.OK;

        if (level.toInt() >= Priority.ERROR_INT) {
            severity = IStatus.ERROR;
        } else if (level.toInt() >= Priority.WARN_INT) {
            severity = IStatus.WARNING;
        } else if (level.toInt() >= Priority.DEBUG_INT) {
            severity = IStatus.INFO;
        }

        if (fVerbose || !(severity == IStatus.INFO)) {
            fPluginLog.log(new Status(severity, fPluginLog.getBundle().getSymbolicName(), level.toInt(), text, thrown));
        }
    }

    /**
     * Closes this appender.
     * 
     * @see Appender#close()
     */
    public void close() {
        closed = true;

    }

    /**
     * Checks if this appender requires layout.
     * 
     * @return always <code>true</code>
     * @see Appender#close()
     */
    public boolean requiresLayout() {
        return true;
    }

    /**
     * Sets the verbose.
     * 
     * @param verbose
     *            the verbose to set
     */
    public void setVerbose(boolean verbose) {
        fVerbose = verbose;
    }
}
