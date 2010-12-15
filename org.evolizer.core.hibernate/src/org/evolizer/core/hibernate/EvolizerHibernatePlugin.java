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
package org.evolizer.core.hibernate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.evolizer.core.exceptions.EvolizerRuntimeException;
import org.evolizer.core.hibernate.model.api.IEvolizerModelProvider;
import org.evolizer.core.logging.base.PluginLogManager;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author wuersch
 */
public class EvolizerHibernatePlugin extends Plugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "org.evolizer.core.hibernate";

    // The shared instance
    private static EvolizerHibernatePlugin sPlugin;

    // The path to the log4j.properties file
    private static final String LOG_PROPERTIES_FILE = "config/log4j.properties";

    // The log manager
    private PluginLogManager fLogManager;

    /**
     * The constructor.
     */
    public EvolizerHibernatePlugin() {
        EvolizerHibernatePlugin.sPlugin = this;
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
        EvolizerHibernatePlugin.sPlugin = null;

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
    public static EvolizerHibernatePlugin getDefault() {
        return EvolizerHibernatePlugin.sPlugin;
    }

    /**
     * Queries all model providers and returns ejb3-annotated classes.
     * 
     * @return A list containing classes that are annotated with ejb3-tags for Hibernate mapping.
     */
    public List<Class<?>> gatherModels() {
        List<Class<?>> annotatedClasses = new ArrayList<Class<?>>();

        // Iterate over all extensions and gather classes that are hibernate annotated
        IExtension[] extensions =
                Platform.getExtensionRegistry().getExtensionPoint(EvolizerHibernatePlugin.PLUGIN_ID, "modelProvider")
                        .getExtensions();
        for (IExtension element : extensions) {
            IConfigurationElement[] configElements = element.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                try {
                    IEvolizerModelProvider provider =
                            (IEvolizerModelProvider) configElement.createExecutableExtension("class");
                    // Throws CoreException if executable could not be created
                    Class<?>[] classes = provider.getAnnotatedClasses();

                    for (Class<?> element1 : classes) {
                        annotatedClasses.add(element1);
                    }

                    getLogManager().getLogger(EvolizerHibernatePlugin.class.getCanonicalName()).debug(
                            "Added model " + configElement.getAttribute("name"));

                } catch (CoreException exception) {
                    String message =
                            "Could not create executable extension from " + configElement.getContributor() + ". "
                                    + exception.getMessage();

                    getLogManager().getLogger(EvolizerHibernatePlugin.class.getCanonicalName()).error(
                            message,
                            exception);

                    throw new EvolizerRuntimeException("Error while initializing log properties.", exception);
                }
            }
        }

        return annotatedClasses;
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
        return EvolizerHibernatePlugin.getDefault().getBundle().getEntry(filePath).openStream();
    }

    /**
     * Returns the log manager.
     * 
     * @return the log manager
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

                // Hack: Allows us, to configure hibernate logging independently from other stuff.
                PropertyConfigurator.configure(props);

                fLogManager = new PluginLogManager(this, props);
            }

            propertiesInputStream.close();
        } catch (IOException e) {
            String message = "Error while initializing log properties." + e.getMessage();

            IStatus status =
                    new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), IStatus.ERROR, message, e);
            getLog().log(status);

            throw new EvolizerRuntimeException("Error while initializing log properties.", e);
        }
    }
}
