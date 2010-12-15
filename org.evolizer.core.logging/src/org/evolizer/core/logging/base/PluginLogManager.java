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
package org.evolizer.core.logging.base;

import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.HierarchyEventListener;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.RootLogger;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Plugin;
import org.evolizer.core.logging.EvolizerLoggingPlugin;
import org.evolizer.core.logging.appender.EclipseLogAppender;

/**
 * An instance of this class manages logging for an Evolizer plug-in.
 * 
 * @author wuersch
 */
public class PluginLogManager {

    private ILog fLog;
    private Hierarchy fHierarchy;

    private class LogListener implements HierarchyEventListener {

        public void addAppenderEvent(Category category, Appender appender) {
            if (appender instanceof EclipseLogAppender) {
                ((EclipseLogAppender) appender).setLog(fLog);
            }
        }

        public void removeAppenderEvent(Category category, Appender appender) {}

    }

    /**
     * This constructor allows the use of XML configuration files. They are more powerful and allow for example the use
     * of filters, logging to different appenders depending on the severity level, etc.
     * 
     * @param plugin
     *            the plugin for which logging is to enable
     * @param xmlProperties
     *            Needs to be an XML file.
     */
    public PluginLogManager(Plugin plugin, URL xmlProperties) {
        fLog = plugin.getLog();
        fHierarchy = new Hierarchy(new RootLogger(Level.DEBUG));
        fHierarchy.addHierarchyEventListener(new LogListener());
        new DOMConfigurator().doConfigure(xmlProperties, fHierarchy);

        EvolizerLoggingPlugin.getDefault().addLogManager(this);
    }

    /**
     * Instantiates a new plugin log manager.
     * 
     * @param plugin
     *            the plugin for which logging is to enable
     * @param properties
     *            the properties file
     */
    public PluginLogManager(Plugin plugin, Properties properties) {
        fLog = plugin.getLog();
        fHierarchy = new Hierarchy(new RootLogger(Level.DEBUG));
        fHierarchy.addHierarchyEventListener(new LogListener());
        new PropertyConfigurator().doConfigure(properties, fHierarchy);

        EvolizerLoggingPlugin.getDefault().addLogManager(this);
    }

    /**
     * Returns a new logger instance named as the first parameter using the default factory. If a logger of that name
     * already exists, then it will be returned. Otherwise, a new logger will be instantiated and then linked with its
     * existing ancestors as well as children.
     * 
     * @param name
     *            logger name
     * @return the logger
     */
    public Logger getLogger(String name) {
        Logger logger = fHierarchy.getLogger(name);

        return logger;
    }

    /**
     * The same as {@link #getLogger(String)} but using a factory instance instead of a default factory.
     * 
     * @param name
     *            logger name
     * @param factory
     *            factory instance
     * @return the logger
     */
    public Logger getLogger(String name, LoggerFactory factory) {
        return fHierarchy.getLogger(name, factory);
    }

    /**
     * Returns the root of this hierarchy.
     * 
     * @return the logger
     */
    public Logger getRootLogger() {
        return fHierarchy.getRootLogger();
    }

    /**
     * Delegate to {@link org.apache.log4j.Hierarchy#exists(String name)}. Check if the named logger exists in the
     * hierarchy. If so return its reference, otherwise returns <code>null</code>.
     * 
     * @param name
     *            the name of the logger.
     * @return If the logger exists return its reference, otherwise returns <code>null</code>.
     */
    public Logger exists(String name) {
        return fHierarchy.exists(name);
    }

    /**
     * Disposes the logger hierarchy.
     */
    public void shutdown() {
        internalShutdown();
        EvolizerLoggingPlugin.getDefault().removeLogManager(this);
    }

    /**
     * Used by Activator to shutdown without removing it from the LoggingPlugin list.
     */
    public void internalShutdown() {
        fHierarchy.shutdown();
    }

    /**
     * Returns all the loggers in this manager.
     * 
     * @return logger enumeration
     */
    @SuppressWarnings("unchecked")
    public Enumeration<Logger> getCurrentLoggers() {
        return fHierarchy.getCurrentLoggers();
    }

    /**
     * Resets configuration values to its defaults.
     */
    public void resetConfiguration() {
        fHierarchy.resetConfiguration();
    }
}
