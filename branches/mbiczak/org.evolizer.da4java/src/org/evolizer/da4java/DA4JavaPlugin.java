/*
 * Copyright 2009 Martin Pinzger, Delft University of Technology,
 * and University of Zurich, Switzerland
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
package org.evolizer.da4java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.evolizer.core.exceptions.EvolizerRuntimeException;
import org.evolizer.core.hibernate.session.EvolizerSessionHandler;
import org.evolizer.core.logging.base.PluginLogManager;
import org.osgi.framework.BundleContext;

/**
 * Entry point of the DAForJava plug-in.
 * 
 * @author pinzger
 */
public final class DA4JavaPlugin extends AbstractUIPlugin {
    
    /** The plug-in ID. */
    public static final String PLUGIN_ID = "org.evolizer.da4java";

    /** The single plug-in instance. */
    private static DA4JavaPlugin sPlugin;
    
    /** The log properties file path. */
    private static final String LOG_PROPERTIES = "config/log4j.properties";
    
    /** The log manager shared instance. */
    private transient PluginLogManager fLogManager;
    
    /**
     * The constructor.
     */
    public DA4JavaPlugin() {
        super();
        sPlugin = this;
    }

    /** 
     * {@inheritDoc}
     */
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        configure();
    }

    /**
     * Load and apply the log configuration.
     */
    private void configure() {
        try {
            final InputStream propertiesInputStream = openBundledFile(LOG_PROPERTIES);
            if (propertiesInputStream != null) {
                final Properties props = new Properties();
                props.load(propertiesInputStream);
                propertiesInputStream.close();
                fLogManager = new PluginLogManager(this, props);
            }
        } catch (IOException ioe) {
            throw new EvolizerRuntimeException("Error configuring DA4JavaPlugin", ioe);
        }
    }

    /**
     * Opens a file located within the sPlugin-bundle.
     * 
     * @param filePath relative path of the file starting
     * 
     * @return an InputStream reading the given file
     * 
     * @throws IOException if file could not be opened
     */
    public InputStream openBundledFile(final String filePath) throws IOException {
        return getDefault().getBundle().getEntry(filePath).openStream();
    }

    /**
     * Helper method to access files in the plug-in directory.
     * 
     * @param filePath File path relative to the plug-in directory.
     * 
     * @return Reference to the file.
     * 
     * @throws EvolizerRuntimeException Error in getting access to the file.
     */
    public File getFile(final String filePath) throws EvolizerRuntimeException {
        File profiles = null;
        try {
            profiles = new File(FileLocator.toFileURL(Platform.getBundle(PLUGIN_ID).getEntry(filePath)).toURI());

        } catch (URISyntaxException use) {
            throw new EvolizerRuntimeException("Error getting file " + filePath, use);
        } catch (IOException ioe) {
            throw new EvolizerRuntimeException("Error getting file " + filePath, ioe);
        }
        return profiles;
    }

    /** 
     * {@inheritDoc}
     */
    public void stop(final BundleContext context) throws Exception {
        sPlugin = null;

        if (fLogManager != null) {
            fLogManager.shutdown();
            fLogManager = null;
        }

        EvolizerSessionHandler.getHandler().cleanupHibernateSessions();

        super.stop(context);
    }

    /**
     * Return the logger instance of this plug-in.
     * 
     * @return The logger instance.
     */
    public static PluginLogManager getLogManager() {
        return getDefault().fLogManager;
    }

    /**
     * Returns the shared instance.
     * 
     * @return The shared instance.
     */
    public static DA4JavaPlugin getDefault() {
        return sPlugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path.
     * 
     * @param path the path
     * 
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(final String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
