package org.evolizer.metrics.model;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class EvolizerMetricsModelPlugin extends Plugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.evolizer.metrics.model";

    // The shared instance
    private static EvolizerMetricsModelPlugin sPlugin;

    /**
     * The constructor
     */
    public EvolizerMetricsModelPlugin() {}

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        sPlugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        sPlugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static EvolizerMetricsModelPlugin getDefault() {
        return sPlugin;
    }

}
