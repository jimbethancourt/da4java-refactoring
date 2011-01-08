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
package org.evolizer.da4java.birdseye;

import java.awt.Frame;

import javax.swing.JApplet;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.da4java.plugin.DA4JavaGraphEditor;

import y.view.Overview;
import y.view.View;

/**
 * Birdseye view on the graph of the current graph editor window. If no
 * graph editor window is active, the view is empty. When a graph is
 * opened and the birdseye view does not exist it is created and added to
 * the panel. Whenever another graph is opened or the graph
 * editor window is switched the existing birdseye view is replaced by a new one
 * representing the current graph.
 * 
 * @author pinzger
 */
public class BirdsEyeView extends ViewPart {
    /** The view ID. */
    public static final String VIEW_ID = "org.evolizer.da4java.birdseye.BirdsEyeView";
    
    /** The logger instance for this class. */
    private static final Logger LOGGER = DA4JavaPlugin.getLogManager().getLogger(BirdsEyeView.class.getName()); 
    
    /** The title of the birdseye view window. */
    private static final String TITLE = "Birdseye View";

    /** Parent SWT component. */
    private transient Composite fParent;
    
    /** AWT panel containing the birdseye (overview) view. */
    private transient JApplet fApplet;
    
    /** The current overview instance. */
    private transient Overview fCurrentOverview;
    
    /** The active graph editor instance. */
    private transient DA4JavaGraphEditor fGraphEditor;

    /** 
     * {@inheritDoc}
     */
    @Override
    public void createPartControl(final Composite parent) {
        LOGGER.info("Opening Birdseye view");

        fParent = parent;
        final Composite composite = new Composite(parent, SWT.EMBEDDED);
        final Frame frame = SWT_AWT.new_Frame(composite);
        fApplet = new JApplet();
        frame.add(fApplet);

        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(new EditorPartListener());
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void setFocus() {
        // not implemented
    }

    /** 
     * {@inheritDoc}
     */
    public String getTitle() {
        return TITLE;
    }

    /**
     * Updating the birdseye view.
     */
    private void updateView() {
        if (fGraphEditor != null) {
            if (fCurrentOverview != null) {
                final View currentView = fCurrentOverview.getCurrentView();
                if (currentView != fGraphEditor.getPanel().getView()) {
                    LOGGER.info("Replacing overview");

                    fApplet.removeAll();
                    fCurrentOverview = new Overview(fGraphEditor.getPanel().getView());
                    fApplet.add(fCurrentOverview);
                    fApplet.validate();
                    fApplet.repaint();
                }
            } else {
                LOGGER.info("Adding new overview");

                fCurrentOverview = new Overview(fGraphEditor.getPanel().getView());
                fApplet.add(fCurrentOverview);
                fApplet.validate();
                fApplet.repaint();
            }

            fCurrentOverview.updateView();
        }
    }

    /**
     * Listener for handling changes in the active workbench. Whenever an graph editor
     * is selected/opened the view config model is updated.
     * 
     * @author pinzger
     */
    private class EditorPartListener implements IPartListener {
        /** 
         * {@inheritDoc}
         */
        public void partActivated(final IWorkbenchPart part) {
            if (part instanceof DA4JavaGraphEditor) {
                fGraphEditor = (DA4JavaGraphEditor) part;
                updateView();
                fParent.setVisible(true);
            } else {
                final IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
                if (!(editor instanceof DA4JavaGraphEditor)) {
                    fParent.setVisible(false);
                    fGraphEditor = null;
                }
            }
        }

        /** 
         * {@inheritDoc}
         */
        public void partBroughtToTop(final IWorkbenchPart part) {
            // not implemented
        }
        /** 
         * {@inheritDoc}
         */
        public void partClosed(final IWorkbenchPart part) {
            if (part instanceof BirdsEyeView) {
                LOGGER.info("Closing BirdseyeView part");

                if (fCurrentOverview != null) {
                    fApplet.remove(fCurrentOverview);
                }
                fCurrentOverview = null;
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().removePartListener(this);
            }
        }
        /** 
         * {@inheritDoc}
         */
        public void partDeactivated(final IWorkbenchPart part) {
            // not implemented
        }
        /** 
         * {@inheritDoc}
         */
        public void partOpened(final IWorkbenchPart part) {
            // not implemented
        }
    }
}
