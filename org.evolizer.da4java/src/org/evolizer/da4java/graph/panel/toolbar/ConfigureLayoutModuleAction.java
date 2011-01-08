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
package org.evolizer.da4java.graph.panel.toolbar;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.evolizer.da4java.graph.data.DependencyGraph;

import y.option.OptionHandler;

/**
 * Opens the dialog for configuring the current layout module.
 * 
 * @author pinzger
 */
public class ConfigureLayoutModuleAction extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -282121307067700221L;
    
    /** The graph panel. */
    private final DependencyGraph fGraph;

    /**
     * The constructor.
     * 
     * @param graph the graph
     */
    public ConfigureLayoutModuleAction(DependencyGraph graph) {
        super("ConfigureLayoutModuleAction");
        fGraph = graph;
    }

    /** 
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent arg0) {
        OptionHandler handler = fGraph.getLayoutModule().getOptionHandler();
        if (handler.showEditor()) {
            fGraph.updateLayoutModule(fGraph.getLayoutModule());
        }
    }
}
