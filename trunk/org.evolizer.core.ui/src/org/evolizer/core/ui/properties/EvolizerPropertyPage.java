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
package org.evolizer.core.ui.properties;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.evolizer.core.exceptions.EvolizerException;
import org.evolizer.core.natures.EvolizerNature;
import org.evolizer.core.natures.EvolizerNatureManager;
import org.evolizer.core.preferences.EvolizerPreferences;
import org.evolizer.core.ui.EvolizerUIPlugin;

/**
 * Property page that provides access to the RHDB settings associated
 * to a Evolizer-enabled project. Also allows to add/remove the {@link EvolizerNature}
 * to a project.
 * 
 * @author wuersch
 */
public class EvolizerPropertyPage extends PropertyPage implements Listener {

    private Logger fLogger = EvolizerUIPlugin.getLogManager().getLogger(EvolizerPropertyPage.class.getCanonicalName());

    private IProject fProject;

    private Button fEnableEvolizerCheckbox;

    private Text fHostText;
    private Text fDatabaseText;
    private Text fUserText;
    private Text fPasswordText;

    private Label fStatusLabelForPreferenceStore;

    private String fHost;
    private String fDatabase;
    private String fUser;
    private String fPassword;
    
    private Button fEnableInMemoryDBCheckbox;
    private Boolean fIsInMemoryDBEnabled;

    /**
     * Instantiates a new evolizer property page.
     */
    public EvolizerPropertyPage() {
        super();
    }

    private void retrievePropertiesValues() {
        try {
            fHost =
                    (fProject.getPersistentProperty(EvolizerPreferences.DB_HOST) != null) ? fProject
                            .getPersistentProperty(EvolizerPreferences.DB_HOST) : "";
            fDatabase =
                    (fProject.getPersistentProperty(EvolizerPreferences.DB_NAME) != null) ? fProject
                            .getPersistentProperty(EvolizerPreferences.DB_NAME) : "";
            fUser =
                    (fProject.getPersistentProperty(EvolizerPreferences.DB_USER) != null) ? fProject
                            .getPersistentProperty(EvolizerPreferences.DB_USER) : "";
            fPassword =
                    (fProject.getPersistentProperty(EvolizerPreferences.DB_PASSWORD) != null) ? fProject
                            .getPersistentProperty(EvolizerPreferences.DB_PASSWORD) : "";
                            
            String isInMemoryDBEnabled = (fProject.getPersistentProperty(EvolizerPreferences.DB_USE_INMEMORY) != null) ? 
                            fProject.getPersistentProperty(EvolizerPreferences.DB_USE_INMEMORY) : "false";
            fIsInMemoryDBEnabled = Boolean.valueOf(isInMemoryDBEnabled);

        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite mainComposite = new Composite(parent, SWT.NONE);

        GridLayout mainGridLayout = new GridLayout();
        mainComposite.setLayout(mainGridLayout);
        mainGridLayout.numColumns = 2;

        fEnableEvolizerCheckbox = new Button(mainComposite, SWT.CHECK);
        fEnableEvolizerCheckbox.setText("Enable Evolizer");
        fEnableEvolizerCheckbox.addListener(SWT.Selection, this);

        Group dbConnectionGroup = new Group(mainComposite, SWT.BORDER);
        dbConnectionGroup.setText("Database Connection Info");
        dbConnectionGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        GridLayout gridLayout = new GridLayout();
        dbConnectionGroup.setLayout(gridLayout);

        gridLayout.numColumns = 2;

        fEnableInMemoryDBCheckbox = new Button(dbConnectionGroup, SWT.CHECK);
        GridData checkBoxLayoutData = new GridData(GridData.FILL_HORIZONTAL);
        checkBoxLayoutData.horizontalSpan = 2;
        fEnableInMemoryDBCheckbox.setLayoutData(checkBoxLayoutData);
        fEnableInMemoryDBCheckbox.setText("Use in-memory database");
        fEnableInMemoryDBCheckbox.addListener(SWT.Selection, this);

        Label hostLabel = new Label(dbConnectionGroup, SWT.NONE);
        hostLabel.setText("MySQL-Hostname:");
        fHostText = new Text(dbConnectionGroup, SWT.BORDER);
        fHostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label databaseLabel = new Label(dbConnectionGroup, SWT.NONE);
        databaseLabel.setText("Database-name: ");
        fDatabaseText = new Text(dbConnectionGroup, SWT.BORDER);
        fDatabaseText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label userLabel = new Label(dbConnectionGroup, SWT.NONE);
        userLabel.setText("User: ");
        fUserText = new Text(dbConnectionGroup, SWT.BORDER);
        fUserText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label passwordLabel = new Label(dbConnectionGroup, SWT.NONE);
        passwordLabel.setText("Password: ");
        fPasswordText = new Text(dbConnectionGroup, SWT.BORDER | SWT.PASSWORD);
        fPasswordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(mainComposite, SWT.NONE);

        fStatusLabelForPreferenceStore = new Label(mainComposite, SWT.NONE);
        fStatusLabelForPreferenceStore.setText(" ");
        GridData statusGridData = new GridData(GridData.FILL_HORIZONTAL);
        fStatusLabelForPreferenceStore.setLayoutData(statusGridData);

        try {
            // Properties are only available for Evolizer-enabled projects.
            initProperties();
        } catch (EvolizerException e) {
            // If something goes wrong, it's better to remove user controls and to display an error msg instead.
            fLogger.error("Error while initializing property page" + e.getMessage(), e);

            mainComposite.dispose();

            mainComposite = new Composite(parent, SWT.NONE);
            mainGridLayout = new GridLayout();
            mainComposite.setLayout(mainGridLayout);

            Label label = new Label(mainComposite, SWT.NONE);
            label.setText(e.getMessage());
        }

        return mainComposite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
    }

    private void initProperties() throws EvolizerException {
        retrieveProject();
        retrievePropertiesValues();

        fEnableInMemoryDBCheckbox.setSelection(fIsInMemoryDBEnabled);
        fHostText.setText(fHost);
        fDatabaseText.setText(fDatabase);
        fUserText.setText(fUser);
        fPasswordText.setText(fPassword);

        setEnabled(isEvolizerEnabled());
        enableDBConfiguration(!isInMemoryDBEnabled());
    }

    private void setEnabled(boolean enabled) {
        fEnableEvolizerCheckbox.setSelection(enabled);
        fEnableInMemoryDBCheckbox.setEnabled(enabled);
        enableDBConfiguration(enabled);
    }

    private void enableDBConfiguration(boolean enabled) {
        fHostText.setEnabled(enabled);
        fDatabaseText.setEnabled(enabled);
        fUserText.setEnabled(enabled);
        fPasswordText.setEnabled(enabled);
    }
    
    private void retrieveProject() {
        fProject = (IProject) (getElement()).getAdapter(IProject.class);
    }

    private boolean isEvolizerEnabled() throws EvolizerException {
        return EvolizerNatureManager.hasEvolizerNature(fProject);
    }

    private boolean isInMemoryDBEnabled() {
        return fIsInMemoryDBEnabled;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void performApply() {
        if (fEnableEvolizerCheckbox.getSelection()) {

            storeUserInput();

            fStatusLabelForPreferenceStore.setText("Database Information stored/updated.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performDefaults() {
        fHostText.setText("mysql://localhost:3306");
        fDatabaseText.setText("test");
        fUserText.setText("evolizer");
        fPasswordText.setText("evolizer");
        fEnableInMemoryDBCheckbox.setSelection(false);

        storeUserInput();

        fStatusLabelForPreferenceStore.setText("Defaults restored.");
    }

    private void storeUserInput() {
        try {
            fProject.setPersistentProperty(EvolizerPreferences.DB_HOST, fHostText.getText());
            fProject.setPersistentProperty(EvolizerPreferences.DB_NAME, fDatabaseText.getText());
            fProject.setPersistentProperty(EvolizerPreferences.DB_USER, fUserText.getText());
            fProject.setPersistentProperty(EvolizerPreferences.DB_PASSWORD, fPasswordText.getText());
            fProject.setPersistentProperty(EvolizerPreferences.DB_USE_INMEMORY, fIsInMemoryDBEnabled.toString());
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performOk() {
        storeUserInput();

        try {
            if (!isEvolizerEnabled() && fEnableEvolizerCheckbox.getSelection()) {
                EvolizerNatureManager.applyEvolizerNature(fProject, new NullProgressMonitor());
            } else if (isEvolizerEnabled() && !fEnableEvolizerCheckbox.getSelection()) {
                EvolizerNatureManager.removeEvolizerNature(fProject, new NullProgressMonitor());
            }

        } catch (EvolizerException e) {
            e.printStackTrace();
        }

        return super.performOk();
    }

    /**
     * {@inheritDoc}
     */
    public void handleEvent(Event event) {
        if (event.widget.equals(fEnableEvolizerCheckbox)) {
            setEnabled(fEnableEvolizerCheckbox.getSelection());
        } else if (event.widget.equals(fEnableInMemoryDBCheckbox)) {
            fIsInMemoryDBEnabled = fEnableInMemoryDBCheckbox.getSelection();
            enableDBConfiguration(!fIsInMemoryDBEnabled);
        }
    }
}
