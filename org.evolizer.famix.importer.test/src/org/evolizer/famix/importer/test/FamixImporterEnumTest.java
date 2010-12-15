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
 *
 */
public class FamixImporterEnumTest {
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
    
        helper.addSourceFile("testPackage.ae", "IEnumPlanet.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/ae/IEnumPlanet.java")), null);
        helper.addSourceFile("testPackage.ae", "EnumPlanet.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/ae/EnumPlanet.java")), null);
        helper.addSourceFile("testPackage.ae", "UseEnumPlanet.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/ae/UseEnumPlanet.java")), null);
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
    public void testEnumClassType() {
        FamixClass enumEnumPlanet = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.EnumPlanet", null));

        assertNotNull("FamixModel must contain class testPackage.ae.EnumPlanet", enumEnumPlanet);
        
        assertEquals("Class " + enumEnumPlanet.getUniqueName() + " must be of type Enum", AbstractFamixEntity.MODIFIER_ENUM, enumEnumPlanet.getModifiers() & AbstractFamixEntity.MODIFIER_ENUM);
    }
    
    @Test
    public void testClassMethodConainer() {
        FamixClass parentClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.EnumPlanet", null));
        FamixMethod simpleMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.EnumPlanet.mass()", null));

        assertNotNull("FamixModel must contain class testPackage.ae.EnumPlanet", parentClass);
        assertNotNull("FamixModel must contain method testPackage.ae.EnumPlanet.mass()", simpleMethod);
        
        assertTrue("FamixClass must contain simple method mass()", parentClass.getMethods().contains(simpleMethod));
        assertEquals("No or wrong parent class for method mass()", parentClass, simpleMethod.getParent());
    }

    @Test
    public void testClassAttributeContainer() {
        FamixClass parentClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.EnumPlanet", null));
        FamixAttribute simpleAttribute = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.ae.EnumPlanet.mass", null));

        assertNotNull("FamixModel must contain class testPackage.ae.EnumPlanet", parentClass);
        assertNotNull("FamixModel must contain method testPackage.ae.EnumPlanet.mass", simpleAttribute);

        assertTrue("FamixClass must contain simple attribute mass", parentClass.getAttributes().contains(simpleAttribute));
        assertEquals("No or wrong parent class for attribute mass", parentClass, simpleAttribute.getParent());
    }

    @Test
    public void testEnumConstantContainer() {
        FamixClass parentClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.EnumPlanet", null));
        FamixAttribute enumConstantMERCURY = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.ae.EnumPlanet.MERCURY", null));
        FamixAttribute enumConstantVENUS = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.ae.EnumPlanet.VENUS", null));

        assertNotNull("FamixModel must contain class testPackage.ae.EnumPlanet", parentClass);
        assertNotNull("FamixModel must contain method testPackage.ae.EnumPlanet.MERCURY", enumConstantMERCURY);
        assertNotNull("FamixModel must contain method testPackage.ae.EnumPlanet.VENUS", enumConstantVENUS);

        assertTrue("FamixClass must contain simple attribute " + enumConstantMERCURY.getUniqueName(), parentClass.getAttributes().contains(enumConstantMERCURY));
        assertEquals("No or wrong parent class for attribute " + enumConstantMERCURY.getUniqueName(), parentClass, enumConstantMERCURY.getParent());
        
        assertTrue("FamixClass must contain simple attribute " + enumConstantVENUS.getUniqueName(), parentClass.getAttributes().contains(enumConstantVENUS));
        assertEquals("No or wrong parent class for attribute " + enumConstantVENUS.getUniqueName(), parentClass, enumConstantVENUS.getParent());

        assertEquals("Type of attribute MERCURY must be " + parentClass.getUniqueName(), parentClass, enumConstantMERCURY.getDeclaredClass());
        assertEquals("Type of attribute VENUS must be " + parentClass.getUniqueName(), parentClass, enumConstantVENUS.getDeclaredClass());
        
        assertEquals("Modifier of attribute " + enumConstantMERCURY.getUniqueName() + " must be " +  AbstractFamixEntity.MODIFIER_PUBLIC, AbstractFamixEntity.MODIFIER_PUBLIC, enumConstantMERCURY.getModifiers() & AbstractFamixEntity.MODIFIER_PUBLIC);
        assertEquals("Modifier of attribute " + enumConstantMERCURY.getUniqueName() + " must be " +  AbstractFamixEntity.MODIFIER_FINAL, AbstractFamixEntity.MODIFIER_FINAL, enumConstantMERCURY.getModifiers() & AbstractFamixEntity.MODIFIER_FINAL);
        assertEquals("Modifier of attribute " + enumConstantMERCURY.getUniqueName() + " must be " +  AbstractFamixEntity.MODIFIER_STATIC, AbstractFamixEntity.MODIFIER_STATIC, enumConstantMERCURY.getModifiers() & AbstractFamixEntity.MODIFIER_STATIC);
    }
    
    @Test
    public void testEnumSubtyping() {
        FamixClass interfaceClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.IEnumPlanet", null));
        FamixClass baseClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.EnumPlanet", null));

        Set<FamixAssociation> lRelations = aModel.getAssociations(baseClass);
        assertTrue("FamixClass " + baseClass.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        int containsInheritsTo = TestHelper.containsRelationTo(interfaceClass, lRelations);
        assertTrue("Missing inheritance relationship in class" + baseClass.getUniqueName() + " to " + interfaceClass.getUniqueName(), containsInheritsTo > 0);

        lRelations = aModel.getAssociations(interfaceClass);
        assertTrue("FamixClass " + interfaceClass.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        containsInheritsTo = TestHelper.containsRelationTo(interfaceClass, lRelations);
        assertTrue("Missing inheritance relationship in sub class " + baseClass.getUniqueName() + " to " + interfaceClass.getUniqueName(), containsInheritsTo > 0);     
    }
    
    @Test
    public void testMethodAnonymousClassContainer() {
        FamixMethod initializer = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.EnumPlanet.<oinit>()", null));
        FamixClass anonymClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.EnumPlanet$1", null));

        assertNotNull("FamixModel must contain method testPackage.ae.EnumPlanet.<oinit>()", initializer);
        assertNotNull("FamixModel must contain anonymous class testPackage.ae.EnumPlanet$1", anonymClass);

        assertTrue("FamixMethod must contain anonymous class", initializer.getAnonymClasses().contains(anonymClass));
        assertEquals("No or wrong parent method for anonymous class", initializer, anonymClass.getParent());
    }
    
    @Test
    public void testAnonymousClassInheritance() {
        FamixClass baseClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.EnumPlanet", null));
        FamixClass anonymClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.EnumPlanet$1", null));

        assertNotNull("FamixModel must contain class testPackage.ae.EnumPlanet", baseClass);
        assertNotNull("FamixModel must contain anonymous class testPackage.ae.EnumPlanet$1", anonymClass);
        
        Set<FamixAssociation> lRelations = aModel.getAssociations(baseClass);
        int nrInheritsFrom = TestHelper.containsRelationTo(baseClass, lRelations);
        assertEquals("Missing inheritance relationship from " + baseClass.getUniqueName() + " to " + anonymClass.getUniqueName(), 1, nrInheritsFrom);

        lRelations = aModel.getAssociations(anonymClass);
        nrInheritsFrom = TestHelper.containsRelationTo(baseClass, lRelations);
        assertEquals("Missing inheritance relationship from " + baseClass.getUniqueName() + " to " + anonymClass.getUniqueName(), 1, nrInheritsFrom);
    }
    
    @Test
    public void testAccessEnumConstant() {
        FamixMethod methodDoSomething = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.UseEnumPlanet.doSomething(double)", null));
        FamixAttribute enumConstantEARTH = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.ae.EnumPlanet.EARTH", null));

        assertNotNull("FamixModel must contain method testPackage.ae.UseEnumPlanet.doSomething(double)", methodDoSomething);
        assertNotNull("FamixModel must contain atrribute class testPackage.ae.EnumPlanet.EARTH", enumConstantEARTH);
        
        Set<FamixAssociation> lRelations = aModel.getAssociations(methodDoSomething);
        int nrAccessesTo = TestHelper.containsRelationTo(enumConstantEARTH, lRelations);
        assertEquals("Missing oaccess from " + methodDoSomething.getUniqueName() + " to " + enumConstantEARTH.getUniqueName(), 1, nrAccessesTo);
    }
    
    @Test
    public void testAccessStaticViaEnumConstant() {
        FamixMethod methodDoSomething = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.UseEnumPlanet.doSomething(double)", null));
        FamixAttribute enumConstantMARS = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.ae.EnumPlanet.MARS", null));
        FamixAttribute staticAttributeG = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.ae.EnumPlanet.G", null));

        assertNotNull("FamixModel must contain method testPackage.ae.UseEnumPlanet.doSomething(double)", methodDoSomething);
        assertNotNull("FamixModel must contain atrribute class testPackage.ae.EnumPlanet.MARS", enumConstantMARS);
        assertNotNull("FamixModel must contain atrribute class testPackage.ae.EnumPlanet.G", staticAttributeG);
        
        Set<FamixAssociation> lRelations = aModel.getAssociations(methodDoSomething);
        int nrAccessesTo = TestHelper.containsRelationTo(enumConstantMARS, lRelations);
        assertEquals("Missing access to " + methodDoSomething.getUniqueName() + " to " + enumConstantMARS.getUniqueName(), 1, nrAccessesTo);

        lRelations = aModel.getAssociations(methodDoSomething);
        nrAccessesTo = TestHelper.containsRelationTo(staticAttributeG, lRelations);
        assertEquals("Missing access to " + methodDoSomething.getUniqueName() + " to " + staticAttributeG.getUniqueName(), 1, nrAccessesTo);
    }
    
    @Test
    public void testInvoceEnumMethod() {
        FamixMethod methodDoSomething = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.UseEnumPlanet.doSomething(double)", null));
        FamixMethod methodSurfaceGravity = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.EnumPlanet.surfaceGravity()", null));
        FamixMethod methodSurfaceWeight = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.EnumPlanet.surfaceWeight(double)", null));
   
        assertNotNull("FamixModel must contain method testPackage.ae.UseEnumPlanet.doSomething(double)", methodDoSomething);
        assertNotNull("FamixModel must contain method testPackage.ae.EnumPlanet.surfaceGravity()", methodSurfaceGravity);
        assertNotNull("FamixModel must contain method testPackage.ae.EnumPlanet.surfaceWeight(double)", methodSurfaceWeight);
        
        Set<FamixAssociation> lRelations = aModel.getAssociations(methodDoSomething);
        int nrInvocationsTo = TestHelper.containsRelationTo(methodSurfaceGravity, lRelations);
        assertEquals("Missing invocation from " + methodDoSomething.getUniqueName() + " to " + methodSurfaceGravity.getUniqueName(), 1, nrInvocationsTo);

        lRelations = aModel.getAssociations(methodDoSomething);
        nrInvocationsTo = TestHelper.containsRelationTo(methodSurfaceWeight, lRelations);
        assertEquals("Missing invocation from " + methodDoSomething.getUniqueName() + " to " + methodSurfaceWeight.getUniqueName(), 1, nrInvocationsTo);
    }
    
    @Test
    public void testInvokeSuperMethodFromAnonymousClass() {
        FamixClass anonymClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.EnumPlanet$1", null));
        FamixMethod newMethodMass = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.EnumPlanet$1.mass()", null));
        FamixMethod superMethodMass = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.EnumPlanet.mass()", null));

        assertNotNull("FamixModel must contain anonymous class testPackage.ae.EnumPlanet$1", anonymClass);
        assertNotNull("FamixModel must contain method testPackage.ae.EnumPlanet$1.mass()", newMethodMass);
        assertNotNull("FamixModel must contain method testPackage.ae.EnumPlanet.mass()", superMethodMass);

        assertTrue("FamixClass must contain method", anonymClass.getMethods().contains(newMethodMass));
        assertEquals("No or wrong parent class of method ", anonymClass, newMethodMass.getParent());
        
        Set<FamixAssociation> lRelations = aModel.getAssociations(newMethodMass);
        int nrInvocationsTo = TestHelper.containsRelationTo(superMethodMass, lRelations);
        assertEquals("Missing invocation from " + newMethodMass.getUniqueName() + " to " + superMethodMass.getUniqueName(), 1, nrInvocationsTo);
    }
}
