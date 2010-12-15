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
package org.evolizer.famix.importer.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.evolizer.core.logging.base.PluginLogManager;
import org.osgi.framework.BundleContext;

/**
 * The Activator class for the FAMIX importer ui plug-in.
 * 
 * @author pinzger
 */
public class FamixImporterUIPlugin extends AbstractUIPlugin {
    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "org.evolizer.famix.importer.ui";
    /**
     * Path to the log properties
     */
    static final String LOG_PROPERTIES_FILE = "config/log4j.properties";
    /**
     * The shared instance
     */
    private static FamixImporterUIPlugin sPlugin;
    /**
     * The log manager
     */
    private PluginLogManager fLogManager;

    /**
     * The constructor
     */
    public FamixImporterUIPlugin() {
        sPlugin = this;
    }

    /**
     * Called when the plug-in is started.
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     * 
     * @param context   The bundle context.
     * @throws Exception    Exception if error in starting the plug-in.
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        configure();
    }

    /**
     * Called when the plug-in is stopped.
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     * 
     * @param context   The bundle context.
     * @throws Exception    Exception if error in stopping the plug-in.
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
     * Returns the shared instance.
     * 
     * @return The shared instance.
     */
    public static FamixImporterUIPlugin getDefault() {
        return sPlugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path
     * 
     * @param path The path to the file.
     * @return The image descriptor.
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /**
     * Opens a file located within the plugin-bundle.
     * 
     * @param filePath Relative path of the file starting.
     * @return an InputStream reading the specified file.
     * @throws IOException If file could not be opened.
     */
    public static InputStream openBundledFile(String filePath) throws IOException {
        return getDefault().getBundle().getEntry(filePath).openStream();
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
     * Returns the logging instance.
     * 
     * @return The plug-in specific logger.
     */
    public static PluginLogManager getLogManager() {
        return getDefault().fLogManager;
    }
}
