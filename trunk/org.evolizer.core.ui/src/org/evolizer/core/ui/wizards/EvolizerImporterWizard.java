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
package org.evolizer.core.ui.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.evolizer.core.ui.EvolizerUIPlugin;

/**
 * Wizard that asks for the database information, i.e. host, db name, user name and password.
 * 
 * @author wuersch
 */
public class EvolizerImporterWizard extends Wizard {

    private EvolizerImporterPage fPage;
    private boolean fCanceled;

    private String fHost;
    private String fDatabase;
    private String fUser;
    private String fPassword;

    // private Logger logger = Activator.getLogManager().getLogger(EvolizerImporterWizard.class.getName());

    /**
     * Checks if is canceled.
     * 
     * @return <code>true</code> if wizard is canceled; <code>false</code> otherwise.
     */
    public boolean isCanceled() {
        return fCanceled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performFinish() {
        // We cannot retrieve user input after page has been disposed - so we have to fetch them before
        // disposing/finishing.

        fHost = fPage.getHost();
        fDatabase = fPage.getDatabaseName();
        fUser = fPage.getUsername();
        fPassword = fPage.getPassword();

        return true;
        // We treat the wizard as container for user input and therefore no special finish processing is needed.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPages() {
        fPage = new EvolizerImporterPage();
        fPage.setTitle("Enable Evolizer");
        fPage.setDescription("Please enter your Database Connection Information");

        fPage
                .setImageDescriptor(ImageDescriptor.createFromFile(
                        EvolizerUIPlugin.class,
                        "/icons/wizard_background.png"));

        addPage(fPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performCancel() {
        fCanceled = true;
        return super.performCancel();
    }

    /**
     * Returns the host.
     * 
     * @return <code>mysql://&lt;host&gt;</code>
     */
    public String getHost() {
        return "mysql://" + fHost;
    }

    /**
     * Returns the database name.
     * 
     * @return the db name.
     */
    public String getDatabaseName() {
        return fDatabase;
    }

    /**
     * Returns the user name.
     * 
     * @return the user name.
     */
    public String getUsername() {
        return fUser;
    }

    /**
     * Returns the password.
     * 
     * @return the password.
     */
    public String getPassword() {
        return fPassword;
    }

    /**
     * Since the EvolizerImporterWizard is very simple, its only page is implemented as an inner class. It contains a
     * few text fields for gathering database information from the user.
     * 
     * @author wuersch
     */
    private class EvolizerImporterPage extends WizardPage implements Listener {

        private Text fHostText;
        private Text fDatabaseText;
        private Text fUserText;
        private Text fPasswordText;

        /**
         * Constructor.
         * 
         * @param pageName
         */
        protected EvolizerImporterPage() {
            super("Evolizer Importer");
        }

        public void createControl(Composite parent) {
            Composite mainComposite = new Composite(parent, SWT.NONE);

            GridLayout mainGridLayout = new GridLayout();
            mainComposite.setLayout(mainGridLayout);
            mainGridLayout.numColumns = 2;

            Group group = new Group(mainComposite, SWT.BORDER);
            group.setText("Database Connection Info");
            group.setLayoutData(new GridData(GridData.FILL_BOTH));

            GridLayout gridLayout = new GridLayout();
            group.setLayout(gridLayout);

            gridLayout.numColumns = 2;

            Label hostLabel = new Label(group, SWT.NONE);
            hostLabel.setText("MySQL-Hostname: mysql://");

            fHostText = new Text(group, SWT.BORDER);
            fHostText.setText("localhost:3306");
            fHostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            fHostText.addListener(SWT.KeyUp, this);

            Label databaseLabel = new Label(group, SWT.NONE);
            databaseLabel.setText("Database-name: ");

            fDatabaseText = new Text(group, SWT.BORDER);
            fDatabaseText.setText("evolizer");
            fDatabaseText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            fDatabaseText.addListener(SWT.KeyUp, this);

            Label userLabel = new Label(group, SWT.NONE);
            userLabel.setText("User: ");

            fUserText = new Text(group, SWT.BORDER);
            fUserText.setText("evolizer");
            fUserText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            fUserText.addListener(SWT.KeyUp, this);

            Label passwordLabel = new Label(group, SWT.NONE);
            passwordLabel.setText("Password: ");

            fPasswordText = new Text(group, SWT.BORDER | SWT.PASSWORD);
            fPasswordText.setText("evolizer");
            fPasswordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            setControl(mainComposite);
        }

        /**
         * Returns the host name.
         * 
         * @return the host.
         */
        public String getHost() {
            return fHostText.getText();
        }

        /**
         * Returns the database name.
         * 
         * @return the database name.
         */
        public String getDatabaseName() {
            return fDatabaseText.getText();
        }

        /**
         * Returns the user name.
         * 
         * @return the username.
         */
        public String getUsername() {
            return fUserText.getText();
        }

        /**
         * Returns the password.
         * 
         * @return the password.
         */
        public String getPassword() {
            return fPasswordText.getText();
        }

        public void handleEvent(Event event) {
            // Evaluates #isPageComplete() and enables/disables 'Finish'-Button
            getWizard().getContainer().updateButtons();
        }

        @Override
        public boolean isPageComplete() {
            return super.isPageComplete() && !getHost().equals("") && !getDatabaseName().equals("")
                    && !getUsername().equals("");
            // password is not mandatory
        }
    }
}
