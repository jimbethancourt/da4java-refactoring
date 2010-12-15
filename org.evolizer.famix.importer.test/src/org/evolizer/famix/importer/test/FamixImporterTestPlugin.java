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
package org.evolizer.famix.importer.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.evolizer.core.logging.base.PluginLogManager;
import org.osgi.framework.BundleContext;

/**
 * FamixImporterTestPlugin of the FAMIX Importer test plug-in
 */
public class FamixImporterTestPlugin extends Plugin {
	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "org.evolizer.famix.importer.test";
	/**
	 * The shared instance
	 */
	private static FamixImporterTestPlugin plugin;
	/**
	 * The log properties file path
	 */
	static final String LOG_PROPERTIES_FILE = "config/log4j.properties";
	/**
	 * The shared log manager instance
	 */
	private PluginLogManager fLogManager;
	
	/**
	 * The constructor
	 */
	public FamixImporterTestPlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		configure();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		if (fLogManager != null) {
			fLogManager.shutdown();
			fLogManager = null;
		}
		super.stop(context);
	}
	
	/**
	 * @return the plug-in specific logger
	 */
	public static PluginLogManager getLogManager() {
		return getDefault().fLogManager; 
	}

	/**
	 * @return the shared instance
	 */
	public static FamixImporterTestPlugin getDefault() {
		return plugin;
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
		}

		catch (Exception e) {
			String message = "Error while initializing log properties." + e.getMessage();

			IStatus status = new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), IStatus.ERROR, message, e);
			getLog().log(status);

			throw new RuntimeException("Error while initializing log properties.", e);
		}
	}
	
	/**
	 * Opens a file located within the plug-in bundle
	 * 
	 * @param filePath
	 *            relative path of the file starting
	 * @return an InputStream reading the specified file
	 * @throws IOException
	 *             if file could not be opened
	 */
    public static InputStream openBundledFile(String filePath) throws IOException {
            return getDefault().getBundle().getEntry(filePath).openStream();
    }

}
