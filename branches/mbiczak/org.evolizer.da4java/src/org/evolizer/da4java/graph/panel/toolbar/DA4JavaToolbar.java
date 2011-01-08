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
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.da4java.commands.CommandController;
import org.evolizer.da4java.graph.panel.DA4JavaGraphPanel;

import y.module.LayoutModule;
import y.view.MagnifierViewMode;

/**
 * The toolbar with all its buttons and actions.
 */
public class DA4JavaToolbar extends JToolBar implements PropertyChangeListener {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -2468361803793600896L;
    
    /** The Constant IMAGE_PATH. */
    private static final String IMAGE_PATH = "/org/evolizer/da4java/graph/panel/images/";

    /** The logger. */
    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(DA4JavaToolbar.class.getName());

    /** The panel. */
    private DA4JavaGraphPanel fPanel;
    
    /** The toolbar buttons. */
    private List<AbstractButton> fToolbarButtons;

    /** The undo button. */
    private JButton fUndoButton;
    
    /** The redo button. */
    private JButton fRedoButton;

    /**
     * Instantiates a new dA for java toolbar.
     * 
     * @param panel The panel
     */
    public DA4JavaToolbar(DA4JavaGraphPanel panel) {
        fPanel = panel;
        init();
    }

    /**
     * Initialize the toolbar and its actions. 
     */
    private void init() {
        // Initialize default Action set
        final Map<String, Action> toolActions = new HashMap<String, Action>();
        toolActions.put("Save As", new SaveAsAction(fPanel));
        toolActions.put("ZoomAction In", new ZoomAction(fPanel, 1.2));
        toolActions.put("ZoomAction Out", new ZoomAction(fPanel, 0.8));
        toolActions.put("ZoomAction Area", new ZoomAreaAction(fPanel));
        toolActions.put("Fit Content", new FitContentAction(fPanel));
        toolActions.put("ConfigureLayoutModuleAction", new ConfigureLayoutModuleAction(fPanel.getGraph()));
        toolActions.put("ExportAction", new ExportAction(fPanel));
        toolActions.put("Add Selection", new AddSelectedEntitiesToGraphAction());
        toolActions.put("Remove Selection", new FilterSelectedElementsAction(fPanel));
        toolActions.put("Undo", new UndoCommandAction(fPanel));
        toolActions.put("Redo", new RedoCommandAction(fPanel));

        // add buttons
        fToolbarButtons = new ArrayList<AbstractButton>();
        addFileButtons();
        addSeparator();
        addZoomInAndOutButtons();
        addMagnifierButton();
        addFitContentButton();
        addSeparator();
        addAddAndRemoveButtons();
        addSeparator();

        addUndoRedoButtons();
        addSeparator();
        addLayoutChooser();
        // addSeparator();
        // addSearchField();
        // addSeparator();

        addConfigureButton();
        addActionListenersToButtons(toolActions);
    }

    /**
     * Adds the file buttons.
     */
    private void addFileButtons() {
        // JButton open = new JButton(new ImageIcon(getClass().getResource(IMAGE_PATH + "Open16.gif")));
        // open.setActionCommand("Open");
        // open.setToolTipText("Opens an existing snapshot");
        // open.setBorderPainted(false);
        // fToolbarButtons.add(open);
        // add(open);
        //
        // JButton save = new JButton(new ImageIcon(getClass().getResource(IMAGE_PATH + "Save16.gif")));
        // save.setActionCommand("Save");
        // save.setBorderPainted(false);
        // save.setToolTipText("Saves the current snapshot");
        // fToolbarButtons.add(save);
        // add(save);

        JButton saveAs = new JButton(new ImageIcon(getClass().getResource(IMAGE_PATH + "SaveAs16.gif")));
        saveAs.setActionCommand("Save As");
        saveAs.setBorderPainted(false);
        saveAs.setToolTipText("Saves the current snapshot to file...");
        fToolbarButtons.add(saveAs);
        add(saveAs);

        JButton export = new JButton(new ImageIcon(getClass().getResource(IMAGE_PATH + "Export16.gif")));
        export.setActionCommand("ExportAction");
        export.setToolTipText("Allows you to export an image into various file formats");
        export.setBorderPainted(false);
        fToolbarButtons.add(export);

        add(export);
    }

    /**
     * Adds the zoom in and out buttons.
     */
    private void addZoomInAndOutButtons() {
        JButton zoomIn = new JButton(new ImageIcon(getClass().getResource(IMAGE_PATH + "ZoomIn16.gif")));
        zoomIn.setActionCommand("ZoomAction In");
        zoomIn.setToolTipText("Zooms in the view");
        zoomIn.setBorderPainted(false);
        fToolbarButtons.add(zoomIn);
        add(zoomIn);

        JButton zoomOut = new JButton(new ImageIcon(getClass().getResource(IMAGE_PATH + "ZoomOut16.gif")));
        zoomOut.setActionCommand("ZoomAction Out");
        zoomOut.setToolTipText("Zooms out the view");
        zoomOut.setBorderPainted(false);
        fToolbarButtons.add(zoomOut);
        add(zoomOut);
    }

    /**
     * Adds the magnifier button.
     */
    private void addMagnifierButton() {
        final MagnifierViewMode magnifierMode = new MagnifierViewMode();
        magnifierMode.setMagnifierRadius(100);
        magnifierMode.setMagnifierZoomFactor(2.0);
        final JToggleButton magnifierButton = new JToggleButton(
                new ImageIcon(getClass().getResource(IMAGE_PATH + "Zoom16.gif")));
        magnifierButton.setToolTipText("Allows you to zoom into a local area of the graph");
        magnifierButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (magnifierButton.isSelected()) {
                    fPanel.getView().addViewMode(magnifierMode);
                } else {
                    fPanel.getView().removeViewMode(magnifierMode);
                }
            }
        });

        add(magnifierButton);
    }

    /**
     * Adds the fit content button.
     */
    private void addFitContentButton() {
        JButton fitContent = new JButton(new ImageIcon(getClass().getResource(IMAGE_PATH + "FitContent16.gif")));
        fitContent.setBorderPainted(false);
        fitContent.setActionCommand("Fit Content");
        fitContent.setToolTipText("Adjust the zoom and the view to fit screen size");
        fToolbarButtons.add(fitContent);
        add(fitContent);
    }

    /**
     * Adds the add and remove buttons.
     */
    private void addAddAndRemoveButtons() {
        JButton addSelection = new JButton(new ImageIcon(getClass().getResource(IMAGE_PATH + "Paste16.gif")));
        addSelection.setBorderPainted(false);
        addSelection.setActionCommand("Add Selection");
        addSelection.setToolTipText("Adds the selected entity in the FamixPackage Explorer to the graph");
        fToolbarButtons.add(addSelection);
        add(addSelection);

        JButton removeSelection = new JButton(new ImageIcon(getClass().getResource(IMAGE_PATH + "Cut16.gif")));
        removeSelection.setBorderPainted(false);
        removeSelection.setActionCommand("Remove Selection");
        removeSelection.setToolTipText("Removes the selected entity from the graph");
        fToolbarButtons.add(removeSelection);
        add(removeSelection);
    }

    /**
     * Adds the undo redo buttons.
     */
    private void addUndoRedoButtons() {
        fUndoButton = new JButton(new ImageIcon(getClass().getResource(IMAGE_PATH + "Undo16.gif")));
        fUndoButton.setBorderPainted(false);
        fUndoButton.setActionCommand("Undo");
        fUndoButton.setToolTipText("Undo...");
        fToolbarButtons.add(fUndoButton);
        add(fUndoButton);

        fRedoButton = new JButton(new ImageIcon(getClass().getResource(IMAGE_PATH + "Redo16.gif")));
        fRedoButton.setBorderPainted(false);
        fRedoButton.setActionCommand("Redo");
        fRedoButton.setToolTipText("Redo...");
        fToolbarButtons.add(fRedoButton);
        add(fRedoButton);
    }

    /**
     * Adds the action listeners to buttons.
     * 
     * @param actionCommandToActionMap the action command to action map
     */
    private void addActionListenersToButtons(final Map<String, Action> actionCommandToActionMap) {
        for (AbstractButton button : fToolbarButtons) {
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Action a = actionCommandToActionMap.get(e.getActionCommand());
                    if (a != null) {
                        a.actionPerformed(e);
                    } else {
                        sLogger.error("Corresponding Action not found: "
                                + e.getActionCommand());
                    }
                }
            });
        }
    }

    /**
     * Adds the layout chooser.
     */
    private void addLayoutChooser() {
        add(new JLabel("Layout: "));
        Map<String, LayoutModule> layoutModules = fPanel.getLayoutModules();
        LayoutModule selectedModule = fPanel.getGraph().getLayoutModule();

        JComboBox comboBox = new JComboBox(layoutModules.keySet().toArray());
        for (Map.Entry<String, LayoutModule> entry : layoutModules.entrySet()) {
            Class<? extends LayoutModule> type = entry.getValue().getClass();
            if (selectedModule.getClass() == type) {
                comboBox.setSelectedItem(entry.getKey());
            }
        }
        comboBox.addActionListener(new LayoutChangerAction(fPanel));

        add(comboBox);
    }

    /**
     * Adds the configure button.
     */
    private void addConfigureButton() {
        JButton configure = new JButton(new ImageIcon(getClass().getResource(IMAGE_PATH + "Preferences16.gif")));
        configure.setActionCommand("ConfigureLayoutModuleAction");
        configure.setToolTipText("Opens a dialog to configure the appearance of the graph");
        configure.setBorderPainted(false);
        fToolbarButtons.add(configure);
        add(configure);
    }

    /**
     * Catch events of executed commands to update the status of the undo/redo buttons.
     * 
     * @param evt the evt
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(CommandController.COMMAND_EXECUTED) 
                || evt.getPropertyName().equals(CommandController.COMMAND_UNDONE) 
                || evt.getPropertyName().equals(CommandController.COMMAND_REDONE)) {

            CommandController controller = (CommandController) evt.getSource();
            if (!controller.canRedo()) {
                fRedoButton.setEnabled(false);
            } else {
                fRedoButton.setEnabled(true);
            }
            if (!controller.canUndo()) {
                fUndoButton.setEnabled(false);
            } else {
                fUndoButton.setEnabled(true);
            }
        }
    }
}
