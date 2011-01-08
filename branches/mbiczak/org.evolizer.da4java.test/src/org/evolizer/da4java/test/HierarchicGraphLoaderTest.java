/**
 * 
 */
package org.evolizer.da4java.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.evolizer.core.exceptions.EvolizerException;
import org.evolizer.core.hibernate.session.EvolizerSessionHandler;
import org.evolizer.core.hibernate.session.api.IEvolizerSession;
import org.evolizer.core.preferences.EvolizerPreferences;
import org.evolizer.core.util.projecthandling.JavaProjectHelper;
import org.evolizer.da4java.commands.AbstractGraphEditCommand;
import org.evolizer.da4java.commands.CommandController;
import org.evolizer.da4java.commands.additions.AddEntitiesCommand;
import org.evolizer.da4java.commands.additions.AddEntitiesViaInDependenciesCommand;
import org.evolizer.da4java.commands.additions.AddEntitiesViaOutDependenciesCommand;
import org.evolizer.da4java.graph.data.DependencyGraph;
import org.evolizer.da4java.graph.panel.DA4JavaGraphPanel;
import org.evolizer.da4java.plugin.DA4JavaEditorInput;
import org.evolizer.da4java.plugin.DA4JavaGraphEditor;
import org.evolizer.da4java.plugin.DA4JavaPerspective;
import org.evolizer.famix.importer.ProjectParser;
import org.evolizer.famix.importer.util.DAOModel;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixModel;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;

/**
 * Test class for DA4Java graph loading features.
 * For each test, a new editor is opened. 
 * 
 * @author Martin Pinzger
 */
public class HierarchicGraphLoaderTest {
    private static IJavaProject project;

    private static DA4JavaGraphPanel fGraphPanel = null; 
//    private static boolean initDone = false;

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject projectNormal = workspace.getRoot().getProject("TestTheBigVoid");
        if(projectNormal.exists()){
            projectNormal.delete(true, true, new NullProgressMonitor());
        }

        @SuppressWarnings("rawtypes")
        Hashtable options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
        JavaCore.setOptions(options);

        setUpProject();

        IJavaModel model =  JavaCore.create(workspace.getRoot());
        project = model.getJavaProject("TestTheBigVoid");
        project.getProject().setPersistentProperty(EvolizerPreferences.DB_USE_INMEMORY, "true");

        List<IJavaElement> selection = new ArrayList<IJavaElement>();
        selection.add(project);

        ProjectParser parser = new ProjectParser(selection);
        parser.parse(null);

        FamixModel createdModel = parser.getModel();
        EvolizerSessionHandler.getHandler().getCurrentSession(project.getProject());
        DAOModel aDAOModel = new DAOModel(EvolizerSessionHandler.getHandler().getDBUrl(project.getProject()), createdModel);         
        aDAOModel.store(null);

//        showClassPlanet();
    }

    private static void setUpProject() throws CoreException, IOException {
        JavaProjectHelper helper = new JavaProjectHelper();
        helper.createProject("TestTheBigVoid", "bin", null);
        helper.addStandartSourceFolder(null);
        helper.addPackage("thebigvoid", null);
        
        helper.addSourceFile("thebigvoid", "Galaxy.java", getFileContent(DA4JavaTestPlugin.openBundledFile("./data/thebigvoid/Galaxy.java")), null);
        helper.addSourceFile("thebigvoid", "GasGiant.java", getFileContent(DA4JavaTestPlugin.openBundledFile("./data/thebigvoid/GasGiant.java")), null);
        helper.addSourceFile("thebigvoid", "ILawsOfTheUniverse.java", getFileContent(DA4JavaTestPlugin.openBundledFile("./data/thebigvoid/ILawsOfTheUniverse.java")), null);
        helper.addSourceFile("thebigvoid", "INonSolidObject.java", getFileContent(DA4JavaTestPlugin.openBundledFile("./data/thebigvoid/INonSolidObject.java")), null);
        helper.addSourceFile("thebigvoid", "Planet.java", getFileContent(DA4JavaTestPlugin.openBundledFile("./data/thebigvoid/Planet.java")), null);
        helper.addSourceFile("thebigvoid", "StellarObject.java", getFileContent(DA4JavaTestPlugin.openBundledFile("./data/thebigvoid/StellarObject.java")), null);
        helper.addSourceFile("thebigvoid", "Universe.java", getFileContent(DA4JavaTestPlugin.openBundledFile("./data/thebigvoid/Universe.java")), null);
    }
    
    protected static String getFileContent(InputStream in) throws IOException{
        String content = "";
        BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
        while(inReader.ready()){
            String line = inReader.readLine();
            content = content + line +"\n";
        }
        return content;
    }

    private void showClassPlanet() {
        IType galaxy = null;
        try {
            galaxy = project.findType("thebigvoid.Planet");
        } catch (JavaModelException e1) {
            e1.printStackTrace();
        }
        Assert.assertNotNull("thebigvoid.Galaxy was not found in the project", galaxy);
        ISelection selection = new StructuredSelection(galaxy);
        
        final DA4JavaEditorInput editorInput = new DA4JavaEditorInput(selection);

        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run() {
                try {
                    PlatformUI.getWorkbench().showPerspective(DA4JavaPerspective.PERSPECTIVE_ID, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
                    DA4JavaGraphEditor part = null;
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    part = (DA4JavaGraphEditor) page.openEditor(editorInput, DA4JavaGraphEditor.DA4JAVA_GRAPH_EDITOR, true);
                    fGraphPanel = part.getPanel();

                    Assert.assertNotNull("Could not open graph editor and panel", part);
                    Assert.assertNotNull("Panel is not openend", part.getPanel());
                    Assert.assertNotNull("Graph is null", part.getPanel().getGraph());
                } catch (PartInitException pie) {
                    pie.printStackTrace();
                } catch (ClassCastException ce) {
                    ce.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    @Test
    public void checkClassPlanet() {
        showClassPlanet();
        
        DependencyGraph graph = fGraphPanel.getGraph();
        org.evolizer.famix.model.entities.FamixPackage thebigvoidPackage = new org.evolizer.famix.model.entities.FamixPackage("thebigvoid", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + thebigvoidPackage.getUniqueName(), graph.contains(thebigvoidPackage));
        org.evolizer.famix.model.entities.FamixClass planetClass = new org.evolizer.famix.model.entities.FamixClass("thebigvoid.Planet", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + planetClass.getUniqueName(), graph.contains(planetClass));
        org.evolizer.famix.model.entities.FamixMethod planetContstructor = new org.evolizer.famix.model.entities.FamixMethod("thebigvoid.Planet.<init>(java.lang.String,java.awt.Color)", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + planetContstructor.getUniqueName(), graph.contains(planetContstructor));
        org.evolizer.famix.model.entities.FamixMethod getColor = new org.evolizer.famix.model.entities.FamixMethod("thebigvoid.Planet.getColor()", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + getColor.getUniqueName(), graph.contains(getColor));
        org.evolizer.famix.model.entities.FamixAttribute color = new org.evolizer.famix.model.entities.FamixAttribute("thebigvoid.Planet.color", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + color.getUniqueName(), graph.contains(color));

        Node thebigvoidPackageNode = graph.getNode(thebigvoidPackage);
        Node planetClassNode = graph.getNode(planetClass);
        Node planetConstructorNode = graph.getNode(planetContstructor);
        Node getColorNode = graph.getNode(getColor);
        Node colorNode = graph.getNode(color);
        Assert.assertNull("Parent node of " + thebigvoidPackageNode + " has to be null", graph.getHierarchyManager().getParentNode(thebigvoidPackageNode));
        Assert.assertEquals("Parent node of " + planetClassNode + " has to be " + thebigvoidPackageNode, thebigvoidPackageNode, graph.getHierarchyManager().getParentNode(planetClassNode));
        Assert.assertEquals("Parent node of " + planetConstructorNode + " has to be " + planetClassNode, planetClassNode, graph.getHierarchyManager().getParentNode(planetConstructorNode));
        Assert.assertEquals("Parent node of " + getColorNode + " has to be " + planetClassNode, planetClassNode, graph.getHierarchyManager().getParentNode(getColorNode));
        Assert.assertEquals("Parent node of " + colorNode + " has to be " + planetClassNode, planetClassNode, graph.getHierarchyManager().getParentNode(colorNode));

        Assert.assertEquals(thebigvoidPackageNode + " does have 0 edges", 0, thebigvoidPackageNode.edges().size());
        Assert.assertEquals(planetClassNode + " does have 0 edges", 0, planetClassNode.edges().size());
        Assert.assertNotNull("There should be an edge between " + getColorNode + " and " + colorNode, getColorNode.getEdge(colorNode));
        Assert.assertNotNull("There should be an edge between " + planetConstructorNode + " and " + colorNode, planetConstructorNode.getEdge(colorNode));
        Assert.assertNull("There should be no edge between " + planetConstructorNode + " and " + colorNode, planetConstructorNode.getEdge(getColorNode));    
    }
    
    @Test
    public void addGalaxyClass() throws EvolizerException {
        showClassPlanet();

        IEvolizerSession session = EvolizerSessionHandler.getHandler().getCurrentSession(project.getProject());
        List<AbstractFamixEntity> queryResult = session.query("from FamixClass as c where c.uniqueName = 'thebigvoid.Galaxy'", AbstractFamixEntity.class, 1);

        AbstractGraphEditCommand command = new AddEntitiesCommand(
                queryResult,
                fGraphPanel.getGraphLoader(), 
                fGraphPanel.getEdgeGrouper());
        command.execute();

        DependencyGraph graph = fGraphPanel.getGraph();

        org.evolizer.famix.model.entities.FamixClass planetClass = new org.evolizer.famix.model.entities.FamixClass("thebigvoid.Planet", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + planetClass.getUniqueName(), graph.contains(planetClass));
        org.evolizer.famix.model.entities.FamixClass galaxyClass = new org.evolizer.famix.model.entities.FamixClass("thebigvoid.Galaxy", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + galaxyClass.getUniqueName(), graph.contains(galaxyClass));
        org.evolizer.famix.model.entities.FamixMethod galaxyContstructor = new org.evolizer.famix.model.entities.FamixMethod("thebigvoid.Galaxy.<init>(java.lang.String)", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + galaxyContstructor.getUniqueName(), graph.contains(galaxyContstructor));

        Assert.assertTrue("Command should add entities", command.getEditedEntities().size() > 0);
        Assert.assertTrue("Command should add associations", command.getEditedAssociations().size() > 0);
        Assert.assertTrue("Command should add class " + galaxyClass, command.getEditedEntities().contains(galaxyClass));
        Assert.assertTrue("Command should add method " + galaxyContstructor, command.getEditedEntities().contains(galaxyContstructor));

        Node planetClassNode = graph.getNode(planetClass);
        Node galaxyClassNode = graph.getNode(galaxyClass);
        Node galaxyConstructorNode = graph.getNode(galaxyContstructor);
        Assert.assertEquals("Parent node of " + galaxyConstructorNode + " has to be " + galaxyClassNode, galaxyClassNode, graph.getHierarchyManager().getParentNode(galaxyConstructorNode));

        Edge invocation = galaxyClassNode.getEdge(planetClassNode);
        Assert.assertNotNull("There should be an edge between " + galaxyClassNode + " and " + planetClassNode, invocation);
    }

    @Test
    public void addIncomingInvocationsToPlanet() {
        showClassPlanet();

        DependencyGraph graph = fGraphPanel.getGraph();
        org.evolizer.famix.model.entities.FamixPackage thebigvoidPackage = new org.evolizer.famix.model.entities.FamixPackage("thebigvoid", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + thebigvoidPackage.getUniqueName(), graph.contains(thebigvoidPackage));
        org.evolizer.famix.model.entities.FamixClass planetClass = new org.evolizer.famix.model.entities.FamixClass("thebigvoid.Planet", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + planetClass.getUniqueName(), graph.contains(planetClass));
        org.evolizer.famix.model.entities.FamixMethod planetContstructor = new org.evolizer.famix.model.entities.FamixMethod("thebigvoid.Planet.<init>(java.lang.String,java.awt.Color)", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + planetContstructor.getUniqueName(), graph.contains(planetContstructor));

        Node thebigvoidPackageNode = graph.getNode(thebigvoidPackage);
        Node planetClassNode = graph.getNode(planetClass);
        //		Node planetConstructorNode = graph.getNode(planetContstructor);
        Assert.assertEquals(thebigvoidPackageNode + " node should have 1 child nodes before adding the associations", 1, graph.getHierarchyManager().getChildren(thebigvoidPackageNode).size());

        AbstractGraphEditCommand command = new AddEntitiesViaInDependenciesCommand(
                planetClassNode, 
                fGraphPanel.getGraphLoader(), 
                fGraphPanel.getEdgeGrouper(), 
                org.evolizer.famix.model.entities.FamixInvocation.class);
        command.execute();

        org.evolizer.famix.model.entities.FamixClass galaxyClass = new org.evolizer.famix.model.entities.FamixClass("thebigvoid.Galaxy", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + galaxyClass.getUniqueName(), graph.contains(galaxyClass));
        org.evolizer.famix.model.entities.FamixMethod galaxyContstructor = new org.evolizer.famix.model.entities.FamixMethod("thebigvoid.Galaxy.<init>(java.lang.String)", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + galaxyContstructor.getUniqueName(), graph.contains(galaxyContstructor));
        Node galaxyClassNode = graph.getNode(galaxyClass);
        Node galaxyConstructorNode = graph.getNode(galaxyContstructor);

        //		fGraphPanel.getHierarchicEdgeGrouper().handleOpenFolder(planetClassNode);
        //		fGraphPanel.getHierarchicEdgeGrouper().handleOpenFolder(galaxyClassNode);

        Assert.assertEquals("Parent node of " + galaxyClassNode + " has to be " + thebigvoidPackageNode, thebigvoidPackageNode, graph.getHierarchyManager().getParentNode(galaxyClassNode));
        Assert.assertEquals("Parent node of " + galaxyConstructorNode + " has to be " + galaxyClassNode, galaxyClassNode, graph.getHierarchyManager().getParentNode(galaxyConstructorNode));
        Assert.assertEquals(thebigvoidPackageNode + " node should have 2 child nodes after adding the associations", 2, graph.getHierarchyManager().getChildren(thebigvoidPackageNode).size());
        Assert.assertEquals(galaxyClassNode + " node should have only 1 child node", 1, graph.getHierarchyManager().getChildren(galaxyClassNode).size());
        Assert.assertEquals(galaxyClassNode + " should have 1 edge", 1, galaxyClassNode.edges().size());
        // class folder nodes are closed
        Assert.assertNotNull("There should be an edge between " + galaxyClassNode + " and " + planetClassNode, galaxyClassNode.getEdge(planetClassNode));
    }

    @Test
    public void addOutgoingAllDependenciesFromPlanet() {
        showClassPlanet();

        DependencyGraph graph = fGraphPanel.getGraph();
        org.evolizer.famix.model.entities.FamixPackage thebigvoidPackage = new org.evolizer.famix.model.entities.FamixPackage("thebigvoid", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + thebigvoidPackage.getUniqueName(), graph.contains(thebigvoidPackage));
        org.evolizer.famix.model.entities.FamixClass planetClass = new org.evolizer.famix.model.entities.FamixClass("thebigvoid.Planet", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + planetClass.getUniqueName(), graph.contains(planetClass));
        org.evolizer.famix.model.entities.FamixMethod planetContstructor = new org.evolizer.famix.model.entities.FamixMethod("thebigvoid.Planet.<init>(java.lang.String,java.awt.Color)", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + planetContstructor.getUniqueName(), graph.contains(planetContstructor));

        Node thebigvoidPackageNode = graph.getNode(thebigvoidPackage);
        Node planetClassNode = graph.getNode(planetClass);
        //		Node planetConstructorNode = graph.getNode(planetContstructor);
        AbstractGraphEditCommand command = new AddEntitiesViaOutDependenciesCommand(
                planetClassNode, 
                fGraphPanel.getGraphLoader(), 
                fGraphPanel.getEdgeGrouper(), 
                null);
        command.execute();

        org.evolizer.famix.model.entities.FamixClass stellarObjectClass = new org.evolizer.famix.model.entities.FamixClass("thebigvoid.StellarObject", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + stellarObjectClass.getUniqueName(), graph.contains(stellarObjectClass));
        org.evolizer.famix.model.entities.FamixAttribute stellarObjectName = new org.evolizer.famix.model.entities.FamixAttribute("thebigvoid.StellarObject.name", null);
        Assert.assertTrue("Graph must contain FAMIX entity " + stellarObjectName.getUniqueName(), graph.contains(stellarObjectName));
        Node stellarObjectClassNode = graph.getNode(stellarObjectClass);
        Node stellarObjectNameNode = graph.getNode(stellarObjectName);

        Assert.assertEquals("Parent node of " + stellarObjectClassNode + " has to be " + thebigvoidPackageNode, thebigvoidPackageNode, graph.getHierarchyManager().getParentNode(stellarObjectClassNode));
        Assert.assertEquals("Parent node of " + stellarObjectNameNode + " has to be " + stellarObjectClassNode, stellarObjectClassNode, graph.getHierarchyManager().getParentNode(stellarObjectNameNode));
        Assert.assertEquals(thebigvoidPackageNode + " node should have 2 child nodes after adding the associations", 2, graph.getHierarchyManager().getChildren(thebigvoidPackageNode).size());
        Assert.assertEquals(stellarObjectClassNode + " node should have only 1 child node", 1, graph.getHierarchyManager().getChildren(stellarObjectClassNode).size());
        Assert.assertEquals(stellarObjectClassNode + " should have 2 edge", 2, stellarObjectClassNode.edges().size());
        // class folder nodes are closed
        //		Assert.assertNotNull("There should be an edge between " + stellarObjectClassNode + " and " + planetClassNode, planetClassNode.getEdge(stellarObjectClassNode));
        for (EdgeCursor ec = stellarObjectClassNode.inEdges(); ec.ok(); ec.next()) {
            Assert.assertEquals("Edge should have " + planetClassNode + " as source node", planetClassNode, ec.edge().source());
        }
    }

    @Test
    public void undoRedoShowClassPlanet() {
        showClassPlanet();

        CommandController commandController = fGraphPanel.getCommandController();
        Assert.assertEquals("Command controller must be able to undo the last command ", true, commandController.canUndo());

        commandController.undoCommand();
        Assert.assertEquals("Graph must not contain nodes after undo", 0, fGraphPanel.getGraph().nodeCount());
        Assert.assertEquals("Graph must not contain edges after undo", 0, fGraphPanel.getGraph().edgeCount());
        Assert.assertEquals("There should be no more command to be undone", false, commandController.canUndo());

        Assert.assertEquals("Command controller must be able to redo the last command ", true, commandController.canRedo());
        commandController.redoCommand();
        Assert.assertEquals("There should be no more command to be redone", false, commandController.canRedo());

        checkClassPlanet();
    }

    @Test
    public void openCloseClassPlanetNode() {

    }
}
