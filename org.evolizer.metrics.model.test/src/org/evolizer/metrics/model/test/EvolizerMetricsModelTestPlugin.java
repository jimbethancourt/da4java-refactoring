package org.evolizer.metrics.model.test;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class EvolizerMetricsModelTestPlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.evolizer.metrics.model.test";

	// The shared instance
	private static EvolizerMetricsModelTestPlugin plugin;
	
	/**
	 * The constructor
	 */
	public EvolizerMetricsModelTestPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
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
	public static EvolizerMetricsModelTestPlugin getDefault() {
		return plugin;
	}

}
