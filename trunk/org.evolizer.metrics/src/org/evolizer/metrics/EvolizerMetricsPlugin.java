package org.evolizer.metrics;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.SafeRunner;
import org.evolizer.metrics.store.IMetricCalculationStrategy;
import org.evolizer.metrics.store.MetricStore;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class EvolizerMetricsPlugin extends Plugin {

    public static final String STRATEGY_EXTENSION_ID = "org.evolizer.metrics.metricCalculationStrategy";

    // The plug-in ID
    public static final String PLUGIN_ID = "org.evolizer.metrics";

    // The shared instance
    private static EvolizerMetricsPlugin sPlugin;

    /**
     * The constructor
     */
    public EvolizerMetricsPlugin() {}

    /**
     * @param context
     *            the bundle context
     * @throws Exception
     *             if starting the plug-in is not successful
     * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        sPlugin = this;

        initStrategies(); // FIXME Exception handling
    }

    /**
     * @param context
     *            the bundle context
     * @throws Exception
     *             if stopping the plug-in is not successful
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
    public static EvolizerMetricsPlugin getDefault() {
        return sPlugin;
    }

    // FIXME Where does this belong?
    // TODO Do we need additional attributes for the extension point? E.g. directly require identifier/description as an
    // attribute of clients of the extension point?
    /**
     * Collects all the strategies.
     * 
     * @throws CoreException
     *             if something goes wrong with the Eclipse plug-in registry.
     */
    public void initStrategies() throws CoreException {
        IConfigurationElement[] config =
                Platform.getExtensionRegistry().getConfigurationElementsFor(STRATEGY_EXTENSION_ID);
        for (IConfigurationElement configElement : config) {
            final Object obj = configElement.createExecutableExtension("strategy_class");
            if (obj instanceof IMetricCalculationStrategy) {
                ISafeRunnable runnable = new ISafeRunnable() {

                    public void handleException(Throwable exception) {
                    // TODO exception handling
                    }

                    public void run() throws Exception {
                        IMetricCalculationStrategy strategy = (IMetricCalculationStrategy) obj;
                        MetricStore.register(strategy);
                    }

                };

                SafeRunner.run(runnable); // TODO Concurrency issues? Should we use that also for the hibernate plug-in?
            }
        }
    }
}
