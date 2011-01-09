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
package org.evolizer.da4java.commands;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.da4java.graph.data.GraphLoader;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAttribute;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.entities.FamixPackage;
import org.evolizer.famix.model.entities.SourceAnchor;

import y.base.Node;

/**
 * Action to open the selected node in the Eclipse Java Editor {@link org.eclipse.jdt.ui.CompilationUnitEditor}.
 * If a package is selected all contained classes are opened when the user confirmed, if a method is selected
 * the containing class is opened and the method is highlighted.
 * 
 * @author Katja Graefenhain, Martin Pinzger
 */
@SuppressWarnings("restriction")
public class NavigateToSourceCodeAction extends AbstractAction {
    
    /** Serial version id. */
    private static final long serialVersionUID = 8222777406931342801L;
    
    /** The shared logger instance. */
    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(NavigateToSourceCodeAction.class.getName());
    
    /** ID of the eclipse jdt compilation unit editor. */
    private static final String COMPILATION_UNIT_EDITOR_ID = "org.eclipse.jdt.ui.CompilationUnitEditor";
    
    /** The selected node. */
    private Node fSelectedNode;
    
    /** The graph loader. */
    private GraphLoader fGraphLoader;

    /**
     * The constructor.
     * 
     * @param selectedNode Node to open in the editor.
     * @param graphLoader  The graph loader.
     */
    public NavigateToSourceCodeAction(Node selectedNode, GraphLoader graphLoader) {
        super("Open source code");
        fSelectedNode = selectedNode;
        fGraphLoader = graphLoader;
    }

    /** 
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent event) {
        AbstractFamixEntity entity = fGraphLoader.getGraph().getGraphModelMapper().getFamixEntity(fSelectedNode);
        if (entity instanceof FamixClass) {
            AbstractFamixEntity parent = entity.getParent();
            if (parent != null) {
                if (parent instanceof FamixClass) {
                    openInEditor(entity, true);
                } else {
                    openInEditor(entity, false);
                }
            }
        } else if (entity instanceof FamixMethod) {
            openInEditor(entity, true);
        } else if (entity instanceof FamixPackage) {
            FamixPackage selectedPackage = (FamixPackage) entity;
            final Set<FamixClass> classes = selectedPackage.getClasses();
            final String message = "You are about to open the source code of " + classes.size()
            + " files. Do you really want to continue?";
            final IWorkbench workbench = PlatformUI.getWorkbench();
            Display display = workbench.getDisplay();
            display.syncExec(new Runnable() {
                public void run() {
                    IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
                    Shell shell = activeWorkbenchWindow.getShell();
                    boolean confirmed = MessageDialog.openConfirm(shell, "Open Source Code", message);
                    if (confirmed) {
                        for (FamixClass clazz : classes) {
                            openInEditor(clazz, false);
                        }
                    }
                }
            });
        } else if (entity instanceof FamixAttribute) {
            openInEditor(entity, true);
        } else {
            sLogger.error("cannot open source of selection " + fSelectedNode);
        }
    }

    /**
     * Gets the source anchor for the selected entity and opens a JavaEditor
     * showing the source code.
     * 
     * @param entity the entity
     * @param highlight True, if the source of the entity should be selected.
     */
    private void openInEditor(AbstractFamixEntity entity, boolean highlight) {
        SourceAnchor sourceAnchor = entity.getSourceAnchor();
        if (sourceAnchor != null) {
            String filePath = sourceAnchor.getFile();
            try {
                IEditorPart editor = openInEditor(filePath);
                if (highlight) {
                    Integer start = sourceAnchor.getStartPos();
                    Integer end = sourceAnchor.getEndPos();
                    if (start != 0 && end != null) {
                        highlightSource(editor, start, end);
                    }
                }
            } catch (final PartInitException e) {
                final IWorkbench workbench = PlatformUI.getWorkbench();
                Display display = workbench.getDisplay();
                display.syncExec(new Runnable() {
                    public void run() {
                        IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
                        IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
                        DialogUtil.openError(activePage.getWorkbenchWindow().getShell(), WorkbenchMessages.Error, e
                                .getMessage(), e);
                    }
                });
            }
        } else {
            sLogger.info("cannot determine file - no source anchor given");
        }
    }

    /**
     * Open in editor.
     * 
     * @param sourcePath the source path
     * 
     * @return the i editor part
     * 
     * @throws PartInitException the part init exception
     */
    private IEditorPart openInEditor(final String sourcePath) throws PartInitException {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        // Workspace location
        IPath location = root.getLocation();

        String fullSourcePath = location.toString() + "/" + sourcePath;

        sLogger.info("try to open file for " + fullSourcePath);
        IPath iPath = new Path(fullSourcePath);
        IFile fileForLocation = ((Workspace) workspace).getFileSystemManager().fileForLocation(iPath);
        final IEditorInput input = new FileEditorInput(fileForLocation);

        final IEditorPart[] result = new IEditorPart[1];
        final PartInitException[] exception = new PartInitException[1];
        final IWorkbench workbench = PlatformUI.getWorkbench();
        Display display = workbench.getDisplay();
        display.syncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
                IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
                try {
                    IEditorPart openEditor = activePage.openEditor(input, COMPILATION_UNIT_EDITOR_ID);
                    result[0] = openEditor;
                } catch (PartInitException e) {
                    sLogger.error("unable to open java editor for source " + sourcePath);
                    exception[0] = e;
                }
            }
        });
        if (exception[0] != null) {
            throw exception[0];
        }
        return result[0];
    }

    /**
     * Highlight source.
     * 
     * @param editor the editor
     * @param start the start
     * @param end the end
     */
    private void highlightSource(IEditorPart editor, Integer start, Integer end) {
        // jump to position in file
        if (editor instanceof JavaEditor) {
            JavaEditor javaEditor = (JavaEditor) editor;
            // javaEditor.collapseMembers();
            final ISelectionProvider selectionProvider = javaEditor.getSelectionProvider();

            // get designated document
            IDocumentProvider documentProvider = javaEditor.getDocumentProvider();
            IDocument document = documentProvider.getDocument(javaEditor.getEditorInput());

            // change selection
            final ISelection selection = new TextSelection(document, start - 1, end - start + 1);
            final IWorkbench workbench = PlatformUI.getWorkbench();
            Display display = workbench.getDisplay();
            display.syncExec(new Runnable() {
                public void run() {
                    selectionProvider.setSelection(selection);
                }
            });
        }
    }

}
