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
package org.evolizer.core.logging;

import java.util.ArrayList;

import org.eclipse.core.runtime.Plugin;
import org.evolizer.core.logging.base.PluginLogManager;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author wuersch
 */
public class EvolizerLoggingPlugin extends Plugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "org.evolizer.util.logging";

    // The shared instance
    private static EvolizerLoggingPlugin sPlugin;

    // Keeps an instance of PluginLogManager for each plug-in that uses logging.
    private ArrayList<PluginLogManager> fLogManagers = new ArrayList<PluginLogManager>();

    /**
     * The constructor.
     */
    public EvolizerLoggingPlugin() {
        super();
        sPlugin = this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        synchronized (fLogManagers) {
            for (PluginLogManager logManager : fLogManagers) {
                logManager.internalShutdown();
            }

            fLogManagers.clear();
        }

        sPlugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     * 
     * @return the shared instance
     */
    public static EvolizerLoggingPlugin getDefault() {
        return sPlugin;
    }

    /**
     * Adds a log manager object to the list of active log managers.
     * 
     * @param logManager
     *            the log manager to add
     */
    public void addLogManager(PluginLogManager logManager) {
        synchronized (fLogManagers) {
            if (logManager != null) {
                fLogManagers.add(logManager);
            }
        }
    }

    /**
     * Removes a log manager object from the list of active log managers.
     * 
     * @param logManager
     *            the log manager to remove
     */
    public void removeLogManager(PluginLogManager logManager) {
        synchronized (fLogManagers) {
            if (logManager != null) {
                fLogManagers.remove(logManager);
            }
        }
    }
}
