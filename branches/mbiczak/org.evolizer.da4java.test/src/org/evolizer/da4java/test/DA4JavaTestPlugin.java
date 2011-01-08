package org.evolizer.da4java.test;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class DA4JavaTestPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.evolizer.da4java.test";

	// The shared instance
	private static DA4JavaTestPlugin plugin;
	
	/**
	 * The constructor
	 */
	public DA4JavaTestPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DA4JavaTestPlugin getDefault() {
		return plugin;
	}

    /**
     * Opens a file located within the plugin-bundle
     * @param filePath relative path of the file starting 
     * @return an InputStream reading the specifed file
     * @throws IOException if file could not be opened
     */
    public static InputStream openBundledFile(String filePath) throws IOException {
            return getDefault().getBundle().getEntry(filePath).openStream();
    }
}
