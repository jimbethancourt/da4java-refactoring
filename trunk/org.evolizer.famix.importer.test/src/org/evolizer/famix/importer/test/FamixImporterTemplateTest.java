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
package org.evolizer.famix.importer.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.evolizer.core.util.projecthandling.JavaProjectHelper;
import org.evolizer.famix.importer.FamixModelFactory;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixAttribute;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.entities.FamixModel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 *
 * @author pinzger
 */
public class FamixImporterTemplateTest {
    /**
     * The FAMIX model container
     */
    protected static FamixModel aModel = null;
    /**
     * The FAMIX model factory used by the importer
     */
    protected static FamixModelFactory aFactory = new FamixModelFactory();
    /**
     * The Java project to parse
     */
    protected static IJavaProject project = null;

    /**
     * Setup method for all test cases. Load database properties.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject projectNormal = workspace.getRoot().getProject("TestProject1");
        if(projectNormal.exists()){
            projectNormal.delete(true, true, new NullProgressMonitor());
        }
        
        JavaCore.setOptions(TestHelper.getJavaCoreOptions());
        IJavaModel model =  JavaCore.create(workspace.getRoot());

        setUpProject();
        
        project = model.getJavaProject("TestProject1");
        assertTrue("TestProject1 does not exist", project.exists());
        
        aModel = TestHelper.parseProject(project);
    }

    /**
     * Tear down after all test cases have been executed.
     * 
     * @throws Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * Setup method for each test case.
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Tear down after each test case.
     * 
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
    }
    
    /**
     * Sets up the temporary Java project.
     * 
     * @throws CoreException
     * @throws IOException
     */
    public static void setUpProject() throws CoreException, IOException{
        JavaProjectHelper helper = new JavaProjectHelper();
        helper.createProject("TestProject1", "bin", null);
        helper.addStandartSourceFolder(null);
        helper.addPackage("testPackage", null);
        helper.addPackage("testPackage.ae", null);
    
        helper.addSourceFile("testPackage.ae", "SimpleTemplate.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/ae/SimpleTemplate.java")), null);
        helper.addSourceFile("testPackage.ae", "UseTemplates.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/ae/UseTemplates.java")), null);
        helper.addSourceFile("testPackage", "IBase.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/IBase.java")), null);
        helper.addSourceFile("testPackage", "Base.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/Base.java")), null);
        helper.addSourceFile("testPackage.ae", "IImplementTemplate.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/ae/IImplementTemplate.java")), null);
        helper.addSourceFile("testPackage.ae", "ImplementTemplate.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/ae/ImplementTemplate.java")), null);
    }

    @Test
    public void testModelContainer() {
        // output all entities and source anchors
      for (AbstractFamixEntity entity : aModel.getFamixEntities()) {
          System.out.print(entity.getType() + " " + entity.getUniqueName());
//          if (entity.getSourceAnchor() != null) {
//              System.out.print(": " + entity.getSourceAnchor().toString());
//          }
          System.out.println();
      }
    }
    
    @Test
    public void testMethodDeclarationWithParametrizedParam() {
        FamixClass classSimpleTemplate = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.SimpleTemplate<T>", null));
        FamixMethod methodAdd = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.SimpleTemplate<T>.add(testPackage.ae.SimpleTemplate$T)", null));
        FamixMethod wrongMethodAdd = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.SimpleTemplate.add(testPackage.ae.SimpleTemplate$T)", null));

        assertNotNull("FamixModel must contain testPackage.ae.SimpleTemplate<T>", classSimpleTemplate);
        assertNotNull("FamixModel must contain testPackage.ae.SimpleTemplate<T>.add(testPackage.ae.SimpleTemplate$T)", methodAdd);
        assertEquals("FamixModel should not contain testPackage.ae.SimpleTemplate.add(testPackage.ae.SimpleTemplate$T)", null, wrongMethodAdd);
        
        assertEquals("Parent of " + methodAdd + " must be class " + classSimpleTemplate.getUniqueName(), classSimpleTemplate, methodAdd.getParent());
    }
    
    @Test
    public void testAttributeDeclartionwithParametrizedType() {
        FamixClass classSimpleTemplate = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.SimpleTemplate<T>", null));
        FamixClass classTypeT = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.SimpleTemplate$T", null));
        FamixAttribute attributeLastEntry = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.ae.SimpleTemplate<T>.lastEntry", null));
        FamixAttribute wrongAttributeLastEntry = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.ae.SimpleTemplate.lastEntry", null));
        
        assertNotNull("FamixModel must contain testPackage.ae.SimpleTemplate<T>", classSimpleTemplate);
        assertNotNull("FamixModel must contain testPackage.ae.SimpleTemplate$T", classTypeT);
        assertNotNull("FamixModel must contain testPackage.ae.SimpleTemplate<T>.lastEntry", attributeLastEntry);
        assertEquals("FamixModel must not contain testPackage.ae.SimpleTemplate.lastEntry", null, wrongAttributeLastEntry);
        
        assertEquals("Parent of " + attributeLastEntry + " must be class " + classSimpleTemplate.getUniqueName(), classSimpleTemplate, attributeLastEntry.getParent());
        assertEquals("Declared type of " + attributeLastEntry + " must be type " + classTypeT.getUniqueName(), classTypeT, attributeLastEntry.getDeclaredClass());
    }
    
    @Test
    public void testInvocationOfGenericConstructor() {
        FamixMethod caller = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.UseTemplates.useSimpleTemplate()", null));
        FamixMethod constructor = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.SimpleTemplate<T>.<init>()", null));

        assertNotNull("FamixModel must contain testPackage.ae.UseTemplates.useSimpleTemplate()", caller);
        assertNotNull("FamixModel must contain testPackage.ae.SimpleTemplate<T>.<init>()", constructor);
        
        Set<FamixAssociation> lRelations = aModel.getAssociations(caller);
        assertTrue("FamixMethod " + caller.getUniqueName() + " must contain relationships", lRelations.size() > 0);

        int containsInvocationTo = TestHelper.containsRelationTo(constructor, lRelations);
        assertEquals("Missing invocation relationship from " + caller.getUniqueName() + " to " + constructor.getUniqueName(), 1, containsInvocationTo);
    }
    
    @Test
    public void testInvocationOfGenericMethod() {
        FamixMethod caller = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.UseTemplates.useSimpleTemplate()", null));
        FamixMethod methodAdd = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.SimpleTemplate<T>.add(testPackage.ae.SimpleTemplate$T)", null));
        FamixMethod methodCount = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.SimpleTemplate<T>.count(java.util.Collection<E>)", null));
            
        assertNotNull("FamixModel must contain testPackage.ae.UseTemplates.useSimpleTemplate()", caller);
        assertNotNull("FamixModel must contain testPackage.ae.SimpleTemplate<T>.add(testPackage.ae.SimpleTemplate$T)", methodAdd);
        assertNotNull("FamixModel must contain testPackage.ae.SimpleTemplate<T>.count(java.util.Collection<E>)", methodCount);
        
        Set<FamixAssociation> lRelations = aModel.getAssociations(caller);
        assertTrue("FamixMethod " + caller.getUniqueName() + " must contain relationships", lRelations.size() > 0);

        int containsInvocationTo = TestHelper.containsRelationTo(methodAdd, lRelations);
        assertEquals("Missing invocation relationship from " + caller.getUniqueName() + " to " + methodAdd.getUniqueName(), 1, containsInvocationTo);
        containsInvocationTo = TestHelper.containsRelationTo(methodAdd, lRelations);
        assertEquals("Missing invocation relationship from " + caller.getUniqueName() + " to " + methodCount.getUniqueName(), 1, containsInvocationTo);
    }

    @Test
    public void testImplementGenericType() {
        FamixClass classGenericInterface = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.IImplementTemplate<T>", null));
        FamixClass subTypeTemplate = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.ImplementTemplate<T>", null));
        assertNotNull("FamixModel must contain testPackage.ae.IImplementTemplate<T>", classGenericInterface);
        assertNotNull("FamixModel must contain testPackage.ae.ImplementTemplate<T>", subTypeTemplate);
        
        Set<FamixAssociation> lRelations = aModel.getAssociations(classGenericInterface);
        assertTrue("FamixClass " + classGenericInterface.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        int containsInheritsTo = TestHelper.containsRelationTo(classGenericInterface, lRelations);
        assertTrue("Missing inheritance relationship in class" + classGenericInterface.getUniqueName() + " to " + subTypeTemplate.getUniqueName(), containsInheritsTo > 0);

        lRelations = aModel.getAssociations(subTypeTemplate);
        assertTrue("FamixClass " + subTypeTemplate.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        containsInheritsTo = TestHelper.containsRelationTo(classGenericInterface, lRelations);
        assertTrue("Missing inheritance relationship in sub class " + classGenericInterface.getUniqueName() + " to " + subTypeTemplate.getUniqueName(), containsInheritsTo > 0);     
    }
    
    @Test
    public void testTemplateMethod() {
        FamixMethod templateMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.SimpleTemplate<T>.templateMethod(java.util.List<E>)", null));
        assertNotNull("FamixModel must contain testPackage.ae.SimpleTemplate.templateMethod(java.util.List<E>)", templateMethod);
        
        FamixMethod caller = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.UseTemplates.useSimpleTemplate()", null));
        FamixMethod callee = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.SimpleTemplate<T>.templateMethod(java.util.List<E>)", null));
        
        Set<FamixAssociation> lRelations = aModel.getAssociations(caller);
        assertTrue("FamixMethod " + caller.getUniqueName() + " must contain relationships", lRelations.size() > 0);

        int containsInvocationTo = TestHelper.containsRelationTo(callee, lRelations);
        assertEquals("Missing invocation relationship from " + caller.getUniqueName() + " to " + callee.getUniqueName(), 1, containsInvocationTo);
    }
}
