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
package org.evolizer.da4java.visibility;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.da4java.graph.utils.FamixAssociationMap;
import org.evolizer.da4java.plugin.DA4JavaGraphEditor;
import org.evolizer.famix.model.entities.FamixAssociation;


/**
 * Association visibility view. The view is enabled only when a DA4JavaGraphEditor
 * instance is currently the top editor.
 * 
 * The configuration of the association view is stored in the ViewConfigModel at the current
 * DA4JavaGraphPanel which is contained by the current DA4JavaGraphEditor instance.
 * 
 * @author pinzger
 */
public class AssociationVisibilityView extends ViewPart {
    
    /** The Constant VIEW_ID. */
    public static final String VIEW_ID = "org.evolizer.da4java.visibility.AssociationVisibilityControllerView";
    
    /** The title. */
    private static final String VIEW_TITLE = "FamixAssociation Visibility Control";
    
    /** The logger. */
    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(AssociationVisibilityView.class.getName()); 

    /** The parent. */
    private Composite fParent;
    
    /** The graph editor. */
    private DA4JavaGraphEditor fGraphEditor;
    
    /** The association type buttons. */
    private Group fAssociationTypeButtons;

    /** 
     * {@inheritDoc}
     */
    @Override
    public void createPartControl(Composite parent) {
        fParent = parent;

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        parent.setLayout(gridLayout);

        fAssociationTypeButtons = new Group(parent, SWT.NULL);
        fAssociationTypeButtons.setText("FamixAssociation Visibility");
        gridLayout = new GridLayout();
        gridLayout.numColumns = 1;

        fAssociationTypeButtons.setLayout(gridLayout);
        GridData layoutData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        fAssociationTypeButtons.setLayoutData(layoutData);

        for (String typeName : FamixAssociationMap.getInstance().getNames()) {
            if (FamixAssociationMap.getInstance().getType(typeName) != null) {
                Button button = new Button(fAssociationTypeButtons, SWT.CHECK);
                button.setText(typeName);
                button.setSelection(true);
                button.addListener(SWT.Selection, new CheckBoxListener(button));
            }
        }

        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(new EditorPartListener());
    }

    /**
     * Obtain the actual association visibility configuration and update the 
     * check boxes accordingly.
     */
    private void updateView() {
        if (fGraphEditor != null) {
            ViewConfigModel viewConfigModel = fGraphEditor.getPanel().getViewConfigModel();
            for (Control childWidget : fAssociationTypeButtons.getChildren()) {
                if (childWidget instanceof Button) {
                    Class<? extends FamixAssociation> associationType = FamixAssociationMap.getInstance().getType(((Button) childWidget).getText());
                    if (associationType != null) {
                        Boolean selectionStatus = viewConfigModel.getAssociationTypeVisibility().get(associationType);
                        if (selectionStatus != null) {
                            ((Button) childWidget).setSelection(selectionStatus);
                        } else {
                            sLogger.error("Error in setting association visibility selection");
                        }
                    } else {
                        sLogger.error("Error in setting association visibility selection");
                    }
                }
            }
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void setFocus() {

    }

    /** 
     * {@inheritDoc}
     */
    public String getTitle() {
        return VIEW_TITLE;
    }

    /**
     * Listener for handling changes in the active workbench. Whenever a graph editor
     * is selected/opened the view config model is updated.
     * 
     * @author pinzger
     */
    private class EditorPartListener implements IPartListener {
        /** 
         * {@inheritDoc}
         */
        public void partActivated(IWorkbenchPart part) {
            if (part instanceof DA4JavaGraphEditor) {
                fGraphEditor = (DA4JavaGraphEditor) part;
                updateView();
                fParent.setVisible(true);
            } else {
                IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
                if (editor == null || !(editor instanceof DA4JavaGraphEditor)) {
                    fParent.setVisible(false);
                    fGraphEditor = null;
                }
            }
        }

        /** 
         * {@inheritDoc}
         */
        public void partBroughtToTop(IWorkbenchPart part) {}
        /** 
         * {@inheritDoc}
         */
        public void partClosed(IWorkbenchPart part) {
            if (part instanceof AssociationVisibilityView) {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().removePartListener(this);
            }
        }
        /** 
         * {@inheritDoc}
         */
        public void partDeactivated(IWorkbenchPart part) {}
        /** 
         * {@inheritDoc}
         */
        public void partOpened(IWorkbenchPart part) {
        }
    }

    /**
     * Listener for handling changes in check-boxes.
     * Whenever a check-box is selected/deselected the corresponding
     * view config model is updated.
     * 
     * @author pinzger
     */
    private class CheckBoxListener implements Listener {
        
        /** The button. */
        private Button fButton;
        
        /**
         * Default constructor.
         * 
         * @param button  The check-box.
         */
        public CheckBoxListener(Button button) {
            fButton = button;
        }

        /** 
         * {@inheritDoc}
         */
        public void handleEvent(Event event) {
            if (fGraphEditor != null) {
                ViewConfigModel viewConfigModel = fGraphEditor.getPanel().getViewConfigModel();
                viewConfigModel.updateAssociationVisibility(FamixAssociationMap.getInstance().getType(fButton.getText()), fButton.getSelection());
            }
        }
    }
}
