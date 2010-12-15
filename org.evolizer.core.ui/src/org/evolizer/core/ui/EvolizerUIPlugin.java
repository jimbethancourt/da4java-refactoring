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
package org.evolizer.core.ui;

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
 * The activator class controls the plug-in life cycle.
 * 
 * @author wuersch
 */
public class EvolizerUIPlugin extends AbstractUIPlugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "org.evolizer.core.ui";

    // The shared instance
    private static EvolizerUIPlugin sPlugin;

    // The path to the log4j.properties file
    private static final String LOG_PROPERTIES_FILE = "config/log4j.properties";

    // The log manager
    private PluginLogManager fLogManager;

    /**
     * The constructor.
     */
    public EvolizerUIPlugin() {
        EvolizerUIPlugin.sPlugin = this;
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
        EvolizerUIPlugin.sPlugin = null;

        if (fLogManager != null) {
            fLogManager.shutdown();
            fLogManager = null;
        }

        super.stop(context);
    }

    /**
     * Returns the shared instance.
     * 
     * @return the shared instance
     */
    public static EvolizerUIPlugin getDefault() {
        return EvolizerUIPlugin.sPlugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path.
     * 
     * @param name
     *            the relative path starting at the root of this plug-in
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String name) {
        String imagePath = "icons/" + name;
        return AbstractUIPlugin.imageDescriptorFromPlugin(EvolizerUIPlugin.PLUGIN_ID, imagePath);
    }

    /**
     * Opens a file located within the plugin-bundle.
     * 
     * @param filePath
     *            relative path of the file starting at the root of this plugin
     * @return an InputStream reading the specified file
     * @throws IOException
     *             if file could not be opened
     */
    public static InputStream openBundledFile(String filePath) throws IOException {
        return EvolizerUIPlugin.getDefault().getBundle().getEntry(filePath).openStream();
    }

    /**
     * Returns the log manager.
     * 
     * @return the log manager
     */
    public static PluginLogManager getLogManager() {
        return getDefault().fLogManager;
    }

    /**
     * Configures logging
     */
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
}
