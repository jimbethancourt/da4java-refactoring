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
package org.evolizer.famix.importer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.evolizer.core.logging.base.PluginLogManager;
import org.evolizer.famix.importer.unresolved.UnresolvedInvocationHandler;
import org.osgi.framework.BundleContext;

/**
 * Activator of the FAMIX Importer plug-in.
 * 
 * @author pinzger
 */
public class FamixImporterPlugin extends Plugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "org.evolizer.famix.importer";
    /**
     * The shared instance
     */
    private static FamixImporterPlugin sPlugin;
    /**
     * The log properties file path
     */
    static final String LOG_PROPERTIES_FILE = "config/log4j.properties";
    /**
     * The shared log manager instance
     */
    private PluginLogManager fLogManager;

    /**
     * The current project parser instance
     */
    private ProjectParser fParser;
    /**
     * The list of unresolved method calls of the last successful parsing
     */
    private UnresolvedInvocationHandler fUnresolvedInvocationHandler;

    /**
     * The constructor.
     */
    public FamixImporterPlugin() {
        sPlugin = this;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        configure();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        sPlugin = null;
        if (fLogManager != null) {
            fLogManager.shutdown();
            fLogManager = null;
        }
        super.stop(context);
    }

    /**
     * Opens a file located within the plugin-bundle.
     * 
     * @param filePath Relative path of the file starting.
     * @return an InputStream reading the specified file.
     * @throws IOException If file could not be opened.
     */
    public static InputStream openBundledFile(String filePath) throws IOException {
        return FamixImporterPlugin.getDefault().getBundle().getEntry(filePath).openStream();
    }

    /**
     * Returns the logger instance.
     * 
     * @return The plug-in specific logger.
     */
    public static PluginLogManager getLogManager() {
        return getDefault().fLogManager;
    }

    private void configure() {
        try {
            InputStream propertiesInputStream = openBundledFile(LOG_PROPERTIES_FILE);

            if (propertiesInputStream != null) {
                Properties props = new Properties();
                props.load(propertiesInputStream);
                propertiesInputStream.close();

                fLogManager = new PluginLogManager(this, props);
            }

            propertiesInputStream.close();
        } catch (IOException e) {
            String message = "Error while initializing log properties." + e.getMessage();

            IStatus status =
                    new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), IStatus.ERROR, message, e);
            getLog().log(status);

            throw new RuntimeException("Error while initializing log properties.", e);
        }
    }

    /**
     * Returns the shared instance.
     * 
     * @return the shared instance
     */
    public static FamixImporterPlugin getDefault() {
        return sPlugin;
    }

    /**
     * Returns the FAMIX parser instance.
     * 
     * @return The current instance of the parser.
     */
    public ProjectParser getParser() {
        return fParser;
    }

    /**
     * Sets the FAMIX parser instance.
     * 
     * @param parser The current parser instance.
     */
    public void setParser(ProjectParser parser) {
        fParser = parser;
    }

    /**
     * Returns the reference to unresolved method calls.
     * 
     * @return The reference to unresolved method calls.
     */
    public UnresolvedInvocationHandler getUnresolvedInvocationHandler() {
        return fUnresolvedInvocationHandler;
    }

    /**
     * Sets the reference to unresolved method calls.
     * 
     * @param unresolvedInvocationHandler Reference to unresolved method calls.
     */
    public void setUnresolvedInvocationHandler(UnresolvedInvocationHandler unresolvedInvocationHandler) {
        fUnresolvedInvocationHandler = unresolvedInvocationHandler;
    }
}
