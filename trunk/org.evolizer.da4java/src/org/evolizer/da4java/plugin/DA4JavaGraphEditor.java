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
package org.evolizer.da4java.plugin;


import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JApplet;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.part.EditorPart;
import org.evolizer.core.exceptions.EvolizerException;
import org.evolizer.core.hibernate.session.EvolizerSessionHandler;
import org.evolizer.core.hibernate.session.api.IEvolizerSession;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.da4java.commands.AbstractGraphEditCommand;
import org.evolizer.da4java.commands.additions.AddEntitiesCommand;
import org.evolizer.da4java.graph.data.GraphLoader;
import org.evolizer.da4java.graph.panel.DA4JavaGraphPanel;
import org.evolizer.da4java.graph.panel.MyDropTargetAdapter;
import org.evolizer.da4java.plugin.selectionhandler.AbstractSelectionHandler;
import org.evolizer.da4java.plugin.selectionhandler.SelectionHandlerFactory;
import org.evolizer.famix.importer.ProjectParser;
import org.evolizer.famix.importer.util.DAOModel;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixModel;
import org.evolizer.famix.model.utils.SnapshotAnalyzer;

/**
 * GraphEditor to display graphs in the eclipse editor area. The content, a
 * {@link java.awt.Frame}, is embedded using the SWT_AWT bridge.
 * 
 * @author Martin Pinzger, Katja Graefenhain
 */
public class DA4JavaGraphEditor extends EditorPart {
    /** The logger. */
    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(DA4JavaGraphEditor.class.getName()); 

    /** The ID for the GraphEditor. */
    public static final String DA4JAVA_GRAPH_EDITOR = "org.evolizer.da4java.view.graph.panel.DA4JavaGraphEditor";

    /** The title of the Graph editor which has the default value: Hierarchic Dependency Graph. */
    private String fEditorTitle = "Hierarchic Dependency Graph";

    /** The GraphPanel which displays the DependencyGraph and the toolbar. */
    private DA4JavaGraphPanel fGraphPanel;

    /**
     * Initializes the editor part with a site and input. Initializes drag&drop support.
     * 
     * @param site the site
     * @param input the input
     * 
     * @throws PartInitException the part init exception
     */
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        if (!(input instanceof DA4JavaEditorInput)) {
            throw new PartInitException("Invalid Input");
        }

        setSite(site);
        setInput(input);
    }

    /**
     * Open dependency analyzer perspective.
     */
    private void openDependencyAnalyzerPerspective() {
        try {
            getSite().getWorkbenchWindow().getWorkbench().showPerspective(DA4JavaPerspective.PERSPECTIVE_ID, getSite().getWorkbenchWindow());
        } catch (WorkbenchException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inits the drag and drop.
     */
    private void initDragAndDrop() {
        PlatformUI.getWorkbench().getDisplay().addFilter(SWT.DragDetect, new Listener() {
            public void handleEvent(Event event) {
                if (!(event.widget instanceof Tree)) {
                    return;
                }
            }
        });
    }

    /**
     * Returns the panel.
     * 
     * @return The {@link DA4JavaGraphPanel} embedded in this editor.
     */
    public DA4JavaGraphPanel getPanel() {
        return fGraphPanel;
    }

    /**
     * Hides the corresponding filter view when the editor is closed.
     */
    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * Creates the DA4Java graph panel and embeds it into a SWT component.
     * We use a heavy weight Swing component as proposed by the article
     * {@link http://www.eclipse.org/articles/article.php?file=Article-Swing-SWT-Integration/index.html}
     * 
     * @param parent The parent SWT component
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
        Frame frame = SWT_AWT.new_Frame(composite);
        JApplet applet = new JApplet();

        DA4JavaEditorInput eInput = (DA4JavaEditorInput) this.getEditorInput();
        AbstractSelectionHandler selectionHandler = SelectionHandlerFactory.getInstance().getSelectionHandler(eInput.getSelection());
        setPartName(selectionHandler.getEditorTitle());

        try {
            fGraphPanel = initGraphPanel(selectionHandler);
            applet.add(fGraphPanel);
            
            List<AbstractFamixEntity> entities = querySelectedFamixEntities(selectionHandler);
            
            AbstractGraphEditCommand command = new AddEntitiesCommand(entities, fGraphPanel.getGraphLoader(), fGraphPanel.getEdgeGrouper());
            fGraphPanel.getCommandController().executeCommand(command);

            openDependencyAnalyzerPerspective();
            
        } catch (EvolizerException ee) {
            sLogger.error("Error creating part control " + ee.getMessage());
        }

        frame.add(applet);
    }

    /**
     * Creates and initializes the DA4Java graph panel
     * 
     * @param selectionHandler  handler with the selected Java project
     * @throws EvolizerException
     */
    private DA4JavaGraphPanel initGraphPanel(AbstractSelectionHandler selectionHandler) throws EvolizerException {
        IJavaProject selectedProject = selectionHandler.getSelectedProject();
        IEvolizerSession session = EvolizerSessionHandler.getHandler().getCurrentSession(selectedProject.getProject());
        SnapshotAnalyzer snapshotAnalyzer = new SnapshotAnalyzer(session);
        DA4JavaGraphPanel graphPanel = new DA4JavaGraphPanel(new GraphLoader(snapshotAnalyzer));
        graphPanel.initGraphPanel();
        initDragAndDrop();
        
        return graphPanel;
    }

    /**
     * Queries the selected FAMIX entities. Furthermore, initializes
     * and populates the database if it is empty.
     * 
     * @param selectionHandler  handler with the selected Java project
     * @return  The list of selected Famix entities
     * 
     * @throws EvolizerException
     */
    private List<AbstractFamixEntity> querySelectedFamixEntities(AbstractSelectionHandler selectionHandler) 
            throws EvolizerException {

        IJavaProject selectedProject = selectionHandler.getSelectedProject();
        SnapshotAnalyzer snapshotAnalyzer = fGraphPanel.getGraphLoader().getSnapshotAnalyzer();
        List<AbstractFamixEntity> entities = selectionHandler.getSelectedEntities(snapshotAnalyzer);
        if (entities.size() == 0) {
            initFamixModel(selectedProject);
            entities = selectionHandler.getSelectedEntities(snapshotAnalyzer);
        }
        return entities;
    }

    /**
     * Initializes and populates the database with the selected Java project.  
     * 
     * @param selectedProject   The selected Java project
     * @throws EvolizerException
     */
    private void initFamixModel(IJavaProject selectedProject) throws EvolizerException {
        IProgressMonitor pm = Job.getJobManager().createProgressGroup();
        pm.beginTask("Extracting and storing the FAMIX model for further use ...", 10);

        List<IJavaElement> selection = new ArrayList<IJavaElement>();
        selection.add(selectedProject);
        ProjectParser parser = new ProjectParser(selection);
        parser.parse(pm);
        pm.worked(5);
        FamixModel famixModel = parser.getModel();

        DAOModel DAOFamixModel = new DAOModel(EvolizerSessionHandler.getHandler().getDBUrl(selectedProject.getProject()), famixModel);         
        DAOFamixModel.store(pm);
        pm.worked(5);
    }

    /**
     * Initializes the drag and drop support for the given control based on
     * provided adapter for drop target listeners.
     * 
     * @param control The control
     */
    protected void initializeDragAndDrop(Control control) {
        MyDropTargetAdapter listener = new MyDropTargetAdapter(fGraphPanel);

        DropTarget dropTarget = new DropTarget(control, DND.DROP_COPY | DND.DROP_MOVE);
        dropTarget.setTransfer(listener.getTransfers());
        dropTarget.addDropListener(listener);
    }

    /** 
     * {@inheritDoc}
     */
    public String getTitle() {
        return fEditorTitle;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
        // not implemented
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void doSaveAs() {
        // not implemented
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean isDirty() {
        // not implemented
        return false;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean isSaveAsAllowed() {
        // not implemented
        return false;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void setFocus() {
        // not implemented
    }
}
