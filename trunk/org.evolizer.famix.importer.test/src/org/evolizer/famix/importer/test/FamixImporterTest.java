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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.Modifier;
import org.evolizer.famix.importer.FamixModelFactory;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.AbstractFamixVariable;
import org.evolizer.famix.model.entities.FamixAccess;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixAttribute;
import org.evolizer.famix.model.entities.FamixCastTo;
import org.evolizer.famix.model.entities.FamixCheckInstanceOf;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixInheritance;
import org.evolizer.famix.model.entities.FamixInvocation;
import org.evolizer.famix.model.entities.FamixLocalVariable;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.entities.FamixModel;
import org.evolizer.famix.model.entities.FamixPackage;
import org.evolizer.famix.model.entities.FamixParameter;
import org.evolizer.famix.model.entities.SourceAnchor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Abstract base class of FAMIX Importer tests.
 * Tests are applied to the FAMIX model referenced by <code>aModel</code>.
 * 
 * @author pinzger
 */
public class FamixImporterTest {
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

        TestHelper.setUpProject();
        
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

    @Test
    public void testModelContainer(){
        assertNotNull(aModel);
        assertTrue("FamixModel does not contain class testPackage.Sum", aModel.contains(aFactory.createClass("testPackage.Sum", null)));
        assertTrue("FamixModel does not contain class testPackage.ae.Test", aModel.contains(aFactory.createClass("testPackage.ae.Test", null)));
        assertTrue("FamixModel does not contain inner class testPackage.ae.Test$Inner", aModel.contains(aFactory.createClass("testPackage.ae.Test$Inner", null)));
        assertTrue("FamixModel does not contain inner inner classe testPackage.ae.Test$InnerInner", aModel.contains(aFactory.createClass("testPackage.ae.Test$Inner$InnerInner", null)));
    }

    @Test
    public void testModelConistency() {
        for (AbstractFamixEntity entity : aModel.getFamixEntities()) {
            // parent check
            if (entity.getParent() == null) {
                System.err.println("No parent: " + entity.getUniqueName());
            }
            if (entity instanceof AbstractFamixVariable) {
                AbstractFamixVariable var = (AbstractFamixVariable) entity;
                if (var.getDeclaredClass() == null) {
                    System.err.println("No data type: " + var.getUniqueName());
                } else {
                    if (! aModel.contains(var.getDeclaredClass())) {
                        System.err.println("Var data type not contained in model: " + var.getUniqueName());
                    }
                }
            }
            if (entity instanceof FamixMethod) {
                FamixMethod func = (FamixMethod) entity;
                if (func.getDeclaredReturnClass() == null) {
                    System.err.println("No return type: " + func.getUniqueName());
                } else {
                    if (! aModel.contains(func)) {
                        System.err.println("Return type not contained in model: " + func.getUniqueName());
                    }
                }
            }
        }
    }

    @Test
    public void testInitializerAnonymousClassContainer() {
        FamixMethod initializer = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.<oinit>()", null));
        FamixClass anonymClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.Test$1", null));

        assertNotNull("FamixModel must contain initializer testPackage.ae.Test.<oinit>()", initializer);
        assertNotNull("FamixModel must contain anonymous class testPackage.ae.Test$1", anonymClass);

        assertTrue("FamixMethod must contain anonymous class " + anonymClass.getUniqueName(), initializer.getAnonymClasses().contains(anonymClass));
        assertEquals("No or wrong parent method for anonymous class " + anonymClass.getUniqueName(), 
                initializer, anonymClass.getParent());
    }

    @Test
    public void testMethodAnonymousClassContainer() {
        FamixMethod method = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.containsAnonymClass()", null));
        FamixClass anonymClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.Test$3", null));

        assertNotNull("FamixModel must contain initializer testPackage.ae.Test.containsAnonymClass()", method);
        assertNotNull("FamixModel must contain anonymous class testPackage.ae.Test$3", anonymClass);

        assertTrue("FamixMethod must contain anonymous class " + anonymClass.getUniqueName(), method.getAnonymClasses().contains(anonymClass));
        assertEquals("No or wrong parent method for anonymous class " + anonymClass.getUniqueName(), 
                method, anonymClass.getParent());
    }

    @Test
    public void testAnonymousClassMethodContainer() {
        FamixClass anonymClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.Test$1", null));
        FamixMethod initializer = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test$1.<oinit>()", null));
        FamixMethod computeMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test$1.compute()", null));

        assertNotNull("FamixModel must contain anonymous class testPackage.ae.Test$1", anonymClass);
        assertNotNull("FamixModel must contain anonymous class initializer testPackage.ae.Test$1.<oinit>()", initializer);
        assertNotNull("FamixModel must contain ananymous class method testPackage.ae.Test$1.compute()", computeMethod);

        assertTrue("Anonymous class must contain initializer " + initializer.getUniqueName(), anonymClass.getMethods().contains(initializer));
        assertEquals("No or wrong parent class for initializer " + initializer.getUniqueName(), 
                anonymClass, initializer.getParent());

        assertTrue("Anonymous class must contain method " + computeMethod.getUniqueName(), anonymClass.getMethods().contains(computeMethod));
        assertEquals("No or wrong parent class for method " + computeMethod.getUniqueName(), 
                anonymClass, computeMethod.getParent());
    }

    @Test
    public void testAnonymousClassAttributeContainer() {
        FamixClass anonymClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.Test$1", null));
        FamixAttribute anonymAttribute = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.ae.Test$1.anonymAttribute", null));

        assertNotNull("FamixModel must contain anonymous class testPackage.ae.Test$1", anonymClass);
        assertNotNull("FamixModel must contain ananymous class attribute testPackage.ae.Test$1.anonymAttribute", anonymAttribute);

        assertTrue("Anonymous class must contain attribute " + anonymAttribute.getUniqueName(), anonymClass.getAttributes().contains(anonymAttribute));
        assertEquals("No or wrong parent class for attribute " + anonymAttribute.getUniqueName(), 
                anonymClass, anonymAttribute.getParent());
    }

    @Test
    public void testAnonymousClassInitializer() {
        FamixClass anonymClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.Test$1", null));
        FamixMethod oinitTest = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.<oinit>()", null)); 
        FamixMethod oinitAnonym = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test$1.<oinit>()", null));
        FamixMethod initAnonym = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test$1.<init>()", null));
        FamixAttribute anonymAttribute = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.ae.Test$1.anonymAttribute", null));

        assertNotNull("FamixModel must contain anonymous class testPackage.ae.Test$1", anonymClass);
        assertNotNull("FamixModel must contain initializer testPackage.ae.Test.<oinit>()", oinitTest);
        assertNotNull("FamixModel must contain anonymous class initializer testPackage.ae.Test$1.<oinit>()", oinitAnonym);
        assertNotNull("FamixModel must contain anonymous class initializer testPackage.ae.Test$1.<init>()", initAnonym);
        assertNotNull("FamixModel must contain ananymous class attribute testPackage.ae.Test$1.anonymAttribute", anonymAttribute);
        assertNotNull(oinitAnonym);

        assertTrue("Anonymous class must contain initializer " + oinitAnonym.getUniqueName(), anonymClass.getMethods().contains(oinitAnonym));
        assertEquals("No or wrong parent class for initializer " + oinitAnonym.getUniqueName(), 
                anonymClass, oinitAnonym.getParent());
        assertTrue("Anonymous class must contain initializer " + initAnonym.getUniqueName(), anonymClass.getMethods().contains(initAnonym));
        assertEquals("No or wrong parent class for initializer " + initAnonym.getUniqueName(), 
                anonymClass, initAnonym.getParent());

        // FamixInvocation Test.<oinit> -> Test$1.<init>
        Set<FamixAssociation> lRelations = aModel.getAssociations(oinitTest);
        assertTrue("FamixMethod " + oinitTest.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        int containsInvocationTo = TestHelper.containsRelationTo(initAnonym, lRelations);
        assertEquals("Missing invocation relationship from " + oinitTest.getUniqueName() + " to " + initAnonym.getUniqueName(), 1, containsInvocationTo);

        lRelations = aModel.getAssociations(oinitAnonym);
        int containsAccessTo = TestHelper.containsRelationTo(anonymAttribute, lRelations);
        assertEquals("Missing access relationship from " + oinitAnonym.getUniqueName() + " to " + anonymAttribute.getUniqueName(), 1, containsAccessTo);
    }

    @Test
    public void testInvocationAnonymClassMethod() {
        FamixMethod containsAnonymMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.containsAnonymClass()", null));
        FamixMethod baseMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Base.getA()", null));
        FamixMethod sumComputeMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Sum.compute()", null));
        FamixMethod anonymComputeMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test$3.compute()", null));
        FamixMethod doubleItMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test$3.doubleIt()", null));

        assertNotNull("FamixModel must contain method testPackage.ae.Test.containsAnonymClass()", containsAnonymMethod);
        assertNotNull("FamixModel must contain method testPackage.Base.getA()", baseMethod);
        assertNotNull("FamixModel must contain method testPackage.Sum.compute()", sumComputeMethod);
        assertNotNull("FamixModel must contain method testPackage.ae.Test$3.compute()", anonymComputeMethod);
        assertNotNull("FamixModel must contain method testPackage.ae.Test$3.doubleIt()", doubleItMethod);

        // containsAnonymClass() -> Sum.compute() (determined at run-time)
        Set<FamixAssociation> lRelations = aModel.getAssociations(containsAnonymMethod);
        assertTrue("FamixMethod " + containsAnonymMethod.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        int containsInvocationTo = TestHelper.containsRelationTo(sumComputeMethod, lRelations);
        assertTrue("Missing invocation relationship from " + containsAnonymMethod.getUniqueName() + " to " + sumComputeMethod.getUniqueName(), containsInvocationTo > 0);

        // Test$3.compute() -> Sum.getA()
        lRelations = aModel.getAssociations(anonymComputeMethod);
        assertTrue("FamixMethod " + sumComputeMethod.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        containsInvocationTo = TestHelper.containsRelationTo(baseMethod, lRelations);
        assertTrue("Missing invocation relationship from " + anonymComputeMethod.getUniqueName() + " to " + baseMethod.getUniqueName(), containsInvocationTo > 0);
        // Test$3.compute() -> Test$3.doubleIt()
        containsInvocationTo = TestHelper.containsRelationTo(doubleItMethod, lRelations);
        assertTrue("Missing invocation relationship from " + anonymComputeMethod.getUniqueName() + " to " + doubleItMethod.getUniqueName(), containsInvocationTo > 0);
    }

    @Test
    public void testAttributeType() {
        FamixClass classInt = (FamixClass) aModel.getElement(aFactory.createClass("int", null));
        FamixAttribute simpleAttribute = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.Variables.a", null));
        assertEquals("Declared class of " + simpleAttribute.getUniqueName() + " must be int", 
                simpleAttribute.getDeclaredClass(), classInt);
    }

    @Test
    public void testRefAttributeType() {
        FamixClass classSum = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.Sum", null));
        FamixAttribute refAttribute = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.Variables.refSum", null));
        assertEquals("Declared class of " + refAttribute.getUniqueName() + " must be testPackage.Sum", 
                refAttribute.getDeclaredClass(), classSum);

    }

    @Test
    public void testParameterizedAttributeType() {
        FamixClass classVector = (FamixClass) aModel.getElement(aFactory.createClass("java.util.Vector<E>", null));
        FamixAttribute paramAttribute = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.Variables.containerSum", null));
        assertEquals("Declared class of " + paramAttribute.getUniqueName() + " must be java.util.Vector<E>", 
                paramAttribute.getDeclaredClass(), classVector);

    }

    @Test
    public void testArrayAttributeType() {
        FamixClass classArray = (FamixClass) aModel.getElement(aFactory.createClass("<Array>", null));
        FamixAttribute arrayAttribute = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.Variables.arraySum", null));
        assertEquals("Declared class of " + arrayAttribute.getUniqueName() + " must be " + classArray.getUniqueName(), 
                arrayAttribute.getDeclaredClass(), classArray);
    }

    @Test
    public void testAttributeModifiers() {
        FamixAttribute staticFinalAttribute = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.Variables.st", null));
        assertEquals("Type string of " + staticFinalAttribute.getUniqueName() + " must have modifier PRIVATE", 
                staticFinalAttribute.getModifiers() & Modifier.PRIVATE, Modifier.PRIVATE);
        assertEquals("Type string of " + staticFinalAttribute.getUniqueName() + " must have modifier STATIC", 
                staticFinalAttribute.getModifiers() & Modifier.STATIC, Modifier.STATIC);
        assertEquals("Type string of " + staticFinalAttribute.getUniqueName() + " must have modifier FINAL", 
                staticFinalAttribute.getModifiers() & Modifier.FINAL, Modifier.FINAL);
    }

    @Test
    public void testCastTo() {
        FamixMethod method = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.computeAllSums()", null));
        FamixClass classSum = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.Sum", null));

        // check method rel. container
        Set<FamixAssociation> lRelations = aModel.getAssociations(method);
        assertTrue("FamixMethod " + method.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        boolean containsRelTo = TestHelper.containsRelationTo(new FamixCastTo(method, classSum), lRelations);
        assertTrue("Missing castTo relationship from " + method.getUniqueName() + " to " + classSum.getUniqueName(), containsRelTo);

        // check castTo class rel. container
        lRelations = aModel.getAssociations(classSum);
        assertTrue("FamixClass " + classSum.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        containsRelTo = TestHelper.containsRelationTo(new FamixCastTo(method, classSum), lRelations);
        assertTrue("Missing castTo relationship from " + method.getUniqueName() + " to " + classSum.getUniqueName(), containsRelTo);
    }

    @Test
    public void testInnerClasses() {
        FamixClass parentClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.Test", null));
        FamixClass innerClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.Test$Inner", null));
        FamixClass innerInnerClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.Test$Inner$InnerInner", null));

        assertEquals("No or wrong parent class for inner class testPackage.ae.Test$Inner", parentClass, innerClass.getParent());
        assertEquals("No or wrong parent class for inner inner class testPackage.ae.Test$Inner$InnerInner", innerClass, innerInnerClass.getParent());       
        assertTrue("Parent class testPackage.ae.Test must contain inner classes", parentClass.getInnerClasses().size() > 0);
        assertTrue("Inner class testPackage.ae.Test$Inner must contain an inner class", innerClass.getInnerClasses().size() > 0);       
    }

    @Test
    public void testClassMethodConainer() {
        FamixClass parentClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.Test", null));
        FamixMethod simpleMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.foo()", null));

        assertTrue("FamixClass must contain simple method foo()", parentClass.getMethods().contains(simpleMethod));
        assertEquals("No or wrong parent class for method foo()", parentClass, simpleMethod.getParent());
    }

    @Test
    public void testClassAttributeContainer() {
        FamixClass parentClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.Test", null));
        FamixAttribute simpleAttribute = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.ae.Test.a", null));

        assertTrue("FamixClass must contain simple attribute a", parentClass.getAttributes().contains(simpleAttribute));
        assertEquals("No or wrong parent class for attribute a", parentClass, simpleAttribute.getParent());
    }

    @Test
    public void testInnerClassMethodContainer() {
        FamixClass innerClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.Test$Inner", null));
        FamixMethod innerMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test$Inner.innerMethod()", null));

        assertTrue("Inner class must contain method " + innerMethod.getUniqueName(), innerClass.getMethods().contains(innerMethod));
        assertEquals("No or wrong parent class for inner class method " + innerMethod.getUniqueName(), 
                innerClass, innerMethod.getParent());
    }

    @Test
    public void testInnerClassAttributeContainer() {
        FamixClass innerClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.Test$Inner", null));
        FamixAttribute innerAttribute = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.ae.Test$Inner.innerAttribute", null));

        assertTrue("Inner class must contain attribute " + innerAttribute.getUniqueName(), innerClass.getAttributes().contains(innerAttribute));
        assertEquals("No or wrong parent class for inner class attribute " + innerAttribute.getUniqueName(), 
                innerClass, innerAttribute.getParent());
    }

    @Test
    public void testMethodParameterConainer() {
        FamixMethod simpleMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Variables.m(int,int[])", null));
        FamixParameter simpleParam = (FamixParameter) aModel.getElement(aFactory.createFormalParameter("testPackage.Variables.m(int,int[]).param", null, 0));

        assertTrue("FamixMethod must contain formal parameters", simpleMethod.getParameters().size() > 0);
        assertNotNull("FamixMethod must contain formal parameter testPackage.Variables.m(int,int[]).param", simpleParam);

        boolean containsParam = false;
        for (FamixParameter lParam : simpleMethod.getParameters()) {
            if (lParam.getUniqueName().equals(simpleParam.getUniqueName())) {
                containsParam = true;
                break;
            }
        }
        assertTrue("FamixMethod must contain formal parameter param", containsParam);
        assertEquals("No or wrong parent method for formal parameter param", simpleMethod, simpleParam.getParent());
    }

    @Test
    public void testUniqueMethodParamaterContainment() {
        // get all Parameters
        for (AbstractFamixEntity lParameter : aModel.getFamixEntities()) {
            if (lParameter instanceof FamixParameter) {
                // check parameter containers of other behavioural entities to not contain this parameter
                FamixMethod lParentMethod = (FamixMethod) lParameter.getParent();
                assertNotNull("Formal parameter " + lParameter.getUniqueName() + " must have a behavioural entity as parent", lParentMethod);
                for (AbstractFamixEntity lMethod : aModel.getFamixEntities()) {
                    if (lMethod instanceof FamixMethod && lMethod != lParentMethod) {
                        assertFalse("Behavioural entity " + lMethod.getUniqueName() + " must not contain the formal parameter " + lParameter.getUniqueName(), 
                                ((FamixMethod) lMethod).getParameters().contains(lParameter));
                    }
                }               
            }
        }
    }

    @Test
    public void testParameterPosition() {
        FamixParameter simpleParam = (FamixParameter) aModel.getElement(aFactory.createFormalParameter("testPackage.Variables.m(int,int[]).param", null, 0));
        FamixParameter arrayParam = (FamixParameter) aModel.getElement(aFactory.createFormalParameter("testPackage.Variables.m(int,int[]).arrayParam", null, 1));

        assertNotNull("FamixMethod must contain formal parameter testPackage.Variables.m(int,int[]).param", simpleParam);
        assertNotNull("FamixMethod must contain formal parameter testPackage.Variables.m(int,int[]).arrayParam", arrayParam);

        assertTrue("Position of " + simpleParam.getUniqueName() + " must be 0", simpleParam.getParamIndex() == 0);        
        assertTrue("Position of " + arrayParam.getUniqueName() + " must be 1", arrayParam.getParamIndex() == 1);
    }

    @Test
    public void testSimpleParameterType() {
        FamixClass classInt = (FamixClass) aModel.getElement(aFactory.createClass("int", null));
        FamixParameter simpleParam = (FamixParameter) aModel.getElement(aFactory.createFormalParameter("testPackage.Variables.m(int,int[]).param", null, 0));

        assertNotNull("FamixMethod must contain formal parameter testPackage.Variables.m(int,int[]).param", simpleParam);
        assertEquals("Declared class of " + simpleParam.getUniqueName() + " must be " + classInt.getUniqueName(), 
                simpleParam.getDeclaredClass(), classInt);
    }

    @Test
    public void testArrayParameterType() {
        FamixClass classArray = (FamixClass) aModel.getElement(aFactory.createClass("<Array>", null));
        FamixParameter arrayParam = (FamixParameter) aModel.getElement(aFactory.createFormalParameter("testPackage.Variables.m(int,int[]).arrayParam", null, 1));

        assertNotNull("FamixMethod must contain formal parameter testPackage.Variables.m(int,int[]).arrayParam", arrayParam);
        assertEquals("Declared class of " + arrayParam.getUniqueName() + " must be <Array>", 
                arrayParam.getDeclaredClass(), classArray);
    }

    @Test
    public void testClassInheritance() {
        FamixClass baseClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.Base", null));
        FamixClass subClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.Sum", null));

        Set<FamixAssociation> lRelations = aModel.getAssociations(baseClass);
        assertTrue("FamixClass " + baseClass.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        int containsInheritsTo = TestHelper.containsRelationTo(baseClass, lRelations);
        assertTrue("Missing inheritance relationship in base class" + subClass.getUniqueName() + " to " + baseClass.getUniqueName(), containsInheritsTo > 0);

        lRelations = aModel.getAssociations(subClass);
        assertTrue("FamixClass " + subClass.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        containsInheritsTo = TestHelper.containsRelationTo(baseClass, lRelations);
        assertTrue("Missing inheritance relationship in sub class " + subClass.getUniqueName() + " to " + baseClass.getUniqueName(), containsInheritsTo > 0);       
    }

    @Test
    public void testClassSubtyping() {
        FamixClass interfaceClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.IBase", null));
        FamixClass baseClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.Base", null));

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
    public void testInnerInitializerAnonymousClassContainer() {
        FamixMethod initializer = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.InnerAnonym$MemoryMapPool.<oinit>()", null));
        FamixClass anonymClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.InnerAnonym$MemoryMapPool$1", null));

        assertNotNull("FamixModel must contain initializer testPackage.InnerAnonym$MemoryMapPool.<init>()", initializer);
        assertNotNull("FamixModel must contain anonymous class testPackage.InnerAnonym$MemoryMapPool$1", anonymClass);

        assertTrue("FamixMethod must contain anonymous class " + anonymClass.getUniqueName(), initializer.getAnonymClasses().contains(anonymClass));
        assertEquals("No or wrong parent method for anonymous class " + anonymClass.getUniqueName(), 
                initializer, anonymClass.getParent());
    }

    @Test
    public void testInnerAnonymClassFromInterface() {
        FamixMethod foo = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.InnerAnonym$MemoryMapPool.foo(java.lang.String)", null));
        FamixClass anonymClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.InnerAnonym$MemoryMapPool$3", null));

        FamixMethod initializer = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.InnerAnonym$MemoryMapPool$3.<oinit>()", null));
        FamixMethod constructor = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.InnerAnonym$MemoryMapPool$3.<init>()", null));
        FamixMethod compute = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.InnerAnonym$MemoryMapPool$3.compute()", null));
        FamixClass innerAnonymClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.InnerAnonym$MemoryMapPool$3$1", null));
        FamixMethod innerInitializer = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.InnerAnonym$MemoryMapPool$3$1.<oinit>()", null));
        FamixMethod innerCompute = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.InnerAnonym$MemoryMapPool$3$1.compute()", null));
        FamixMethod innerConstructor = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.InnerAnonym$MemoryMapPool$3$1.<init>()", null));

        assertNotNull("FamixModel must contain method testPackage.InnerAnonym$MemoryMapPool.foo(java.lang.String)", foo);
        assertNotNull("FamixModel must contain anonymous class testPackage.InnerAnonym$MemoryMapPool$3", anonymClass);
        assertNotNull("FamixModel must contain initializer testPackage.InnerAnonym$MemoryMapPool$3.<oinit>", initializer);
        assertNotNull("FamixModel must contain method testPackage.InnerAnonym$MemoryMapPool$3.<init>", constructor);
        assertNotNull("FamixModel must contain method testPackage.InnerAnonym$MemoryMapPool$3.compute()", compute);

        assertNotNull("FamixModel must contain inner anonymous class testPackage.InnerAnonym$MemoryMapPool$3$1", innerAnonymClass);
        assertNotNull("FamixModel must contain initializer testPackage.InnerAnonym$MemoryMapPool$3$1.<oinit>", innerInitializer);
        assertNotNull("FamixModel must contain method testPackage.InnerAnonym$MemoryMapPool$3$1.compute()", innerCompute);
        assertNotNull("FamixModel must contain method testPackage.InnerAnonym$MemoryMapPool$3$1.<init>", innerConstructor);

        assertTrue("FamixMethod " + foo.getUniqueName() + " must contain anonym class " + anonymClass.getUniqueName(), foo.getAnonymClasses().contains(anonymClass));
        assertEquals("Parent of anonym class " + anonymClass.getUniqueName() + " must be " + foo.getUniqueName(), foo, anonymClass.getParent());
        assertTrue("FamixClass " + anonymClass.getUniqueName() + " must contain method " + compute.getUniqueName(), anonymClass.getMethods().contains(compute));

        Set<FamixAssociation> lRelations = aModel.getAssociations(foo);
        int nrInvocationTo = TestHelper.containsRelationTo(constructor, lRelations);
        assertEquals("Missing invocation relationship from " + foo.getUniqueName() + " to " + constructor.getUniqueName(), 1, nrInvocationTo);

        lRelations = aModel.getAssociations(compute);
        nrInvocationTo = TestHelper.containsRelationTo(innerConstructor, lRelations);
        assertEquals("Missing invocation relationship from " + compute.getUniqueName() + " to " + innerConstructor.getUniqueName(), 1, nrInvocationTo);
    }

    @Test
    public void testInnerMethodAnonymousClassContainer() {

        FamixMethod method = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.InnerAnonym$MemoryMapPool.clean(java.nio.MappedByteBuffer)", null));
        FamixClass anonymClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.InnerAnonym$MemoryMapPool$2", null));

        assertNotNull("FamixModel must contain method testPackage.InnerAnonym$MemoryMapPool.clean(MappedByteBuffer)", method);
        assertNotNull("FamixModel must contain anonymous class testPackage.InnerAnonym$MemoryMapPool$2", anonymClass);

        assertTrue("FamixMethod must contain anonymous class " + anonymClass.getUniqueName(), method.getAnonymClasses().contains(anonymClass));
        assertEquals("No or wrong parent method for anonymous class " + anonymClass.getUniqueName(), 
                method, anonymClass.getParent());
    }

    @Test
    public void testAnonymMethodInvocations() {
        FamixMethod caller = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.InnerAnonym$MemoryMapPool.clean(java.nio.MappedByteBuffer)", null));
        FamixMethod callee = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.InnerAnonym$MemoryMapPool$2.compute(int,int)", null));

        assertNotNull("FamixModel must contain method testPackage.InnerAnonym$MemoryMapPool.clean(java.nio.MappedByteBuffer)", caller);
        assertNotNull("FamixModel must contain method testPackage.InnerAnonym$MemoryMapPool$2.compute(int,int)", callee);

        Set<FamixAssociation> lRelations = aModel.getAssociations(caller);
        assertTrue("FamixMethod " + caller.getUniqueName() + " must contain relationships", lRelations.size() > 0);

        int containsInvocationTo = TestHelper.containsRelationTo(callee, lRelations);
        assertTrue("Missing invocation relationship from " + caller.getUniqueName() + " to " + callee.getUniqueName(), containsInvocationTo > 0);
    }

    @Test
    public void testInstanceOf() {
        FamixMethod method = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.computeAllSums()", null));
        FamixClass classSum = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.Sum", null));

        // check metho rel. container
        Set<FamixAssociation> lRelations = aModel.getAssociations(method);
        assertTrue("FamixMethod " + method.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        boolean containsRelTo = TestHelper.containsRelationTo(new FamixCheckInstanceOf(method, classSum), lRelations);
        assertTrue("Missing instanceOf relationship from " + method.getUniqueName() + " to " + classSum.getUniqueName(), containsRelTo);

        // check instanOf class rel. container
        lRelations = aModel.getAssociations(classSum);
        assertTrue("FamixClass " + classSum.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        containsRelTo = TestHelper.containsRelationTo(new FamixCheckInstanceOf(method, classSum), lRelations);
        assertTrue("Missing instanceOf relationship from " + method.getUniqueName() + " to " + classSum.getUniqueName(), containsRelTo);
    }

    @Test
    public void testInnerMethodInvocation() {
        FamixMethod caller = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Base.computeOther(int,java.lang.String)", null));
        FamixMethod callee1 = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Base.computeOther(java.lang.String)", null));
        FamixMethod callee2 = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Base.compute()", null));

        assertNotNull("FamixModel must contain method 'testPackage.Base.computeOther(int,java.lang.String)'", caller);
        assertNotNull("FamixModel must contain method 'testPackage.Base.computeOther(java.lang.String)'", callee1);
        assertNotNull("FamixModel must contain method 'testPackage.Base.compute()'", callee2);

        Set<FamixAssociation> lRelations = aModel.getAssociations(caller);
        int containsInvocationTo = TestHelper.containsRelationTo(callee1, lRelations);
        assertEquals("Missing invocation relationship from " + caller.getUniqueName() + " to " + callee1.getUniqueName(), 1, containsInvocationTo);
        containsInvocationTo = TestHelper.containsRelationTo(callee2, lRelations);
        assertEquals("Missing invocation relationship from " + caller.getUniqueName() + " to " + callee2.getUniqueName(), 1, containsInvocationTo);
    }

    @Test
    public void testMethodInvocation() {
        FamixMethod foo = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.foo()", null));
        FamixMethod callee = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Sum.computeOtherResolved(java.lang.String)", null));

        assertNotNull("FamixModel must contain method 'testPackage.ae.Test.foo()'", foo);
        assertNotNull("FamixModel must contain method 'testPackage.Sum.computeOtherResolved(java.lang.String)'", callee);

        Set<FamixAssociation> lRelations = aModel.getAssociations(foo);
        assertTrue("FamixMethod " + foo.getUniqueName() + " must contain relationships", lRelations.size() > 0);

        int containsInvocationTo = TestHelper.containsRelationTo(callee, lRelations);
        assertEquals("Missing invocation relationship from " + foo.getUniqueName() + " to " + callee.getUniqueName(), 1, containsInvocationTo);
    }

    @Test
    public void testMethodInvocationParamIsSubtype() {
        FamixMethod caller = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.getParamSum()", null));
        FamixMethod callee = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Variables.getSum(testPackage.IBase)", null));

        assertNotNull("FamixModel must contain method 'testPackage.ae.Test.getParamSum()'", caller);
        assertNotNull("FamixModel must contain method 'testPackage.Variables.getSum(testPackage.IBase)'", callee);

        Set<FamixAssociation> lRelations = aModel.getAssociations(caller);
        assertTrue("FamixMethod " + caller.getUniqueName() + " must contain relationships", lRelations.size() > 0);

        int containsInvocationTo = TestHelper.containsRelationTo(callee, lRelations);
        assertTrue("Missing invocation relationship from " + caller.getUniqueName() + " to " + callee.getUniqueName(), containsInvocationTo > 0);
    }

    @Test
    public void testMethodInvocationCallSequence() {
        FamixMethod caller = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.callSequence()", null));
        FamixMethod callee1 = (FamixMethod) aModel.getElement(aFactory.createMethod("java.util.Vector<E>.elementAt(int)", null));
        FamixMethod callee2 = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Sum.compute()", null));

        assertNotNull("FamixModel must contain method 'testPackage.ae.Test.getParamSum()'", caller);
        assertNotNull("FamixModel must contain method 'java.util.Vector<E>.elementAt(int)'", callee1);
        assertNotNull("FamixModel must contain method 'testPackage.Sum.compute()'", callee2);

        Set<FamixAssociation> lRelations = aModel.getAssociations(caller);
        assertTrue("FamixMethod " + caller.getUniqueName() + " must contain relationships", lRelations.size() > 0);

        int containsInvocationTo = TestHelper.containsRelationTo(callee1, lRelations);
        assertEquals("Missing invocation relationship from " + caller.getUniqueName() + " to " + callee1.getUniqueName(), 1, containsInvocationTo);
        containsInvocationTo = TestHelper.containsRelationTo(callee2, lRelations);
        assertEquals("Missing invocation relationship from " + caller.getUniqueName() + " to " + callee2.getUniqueName(), 1, containsInvocationTo);
    }

    @Test
    public void testSuperMethodInvocation() {
        FamixMethod caller = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Sum.computeOtherResolved(java.lang.String)", null));
        FamixMethod superMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Base.computeOther(java.lang.String)", null));

        assertNotNull("FamixModel must contain method 'testPackage.Sum.computeOtherResolved(java.lang.String)'", caller);
        assertNotNull("FamixModel must contain method 'testPackage.Base.computeOther(java.lang.String)'", superMethod);

        Set<FamixAssociation> lRelations = aModel.getAssociations(caller);
        assertTrue("FamixMethod " + caller.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        int containsInvocationTo = TestHelper.containsRelationTo(superMethod, lRelations);
        assertTrue("Missing super method invocation relationship from " + caller.getUniqueName() + " to " + superMethod.getUniqueName(), containsInvocationTo > 0);

        lRelations = aModel.getAssociations(superMethod);
        assertTrue("FamixMethod " + superMethod.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        containsInvocationTo = TestHelper.containsRelationTo(superMethod, lRelations);
        assertTrue("Missing super method invocation relationship from " + caller.getUniqueName() + " to " + superMethod.getUniqueName(), containsInvocationTo > 0);
    }

    @Test
    public void testSuperConstructorInvocation() {
        FamixMethod caller = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Sum.<init>()", null));
        FamixMethod superConstructor = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Base.<init>()", null));

        Set<FamixAssociation> lRelations = aModel.getAssociations(caller);
        assertTrue("FamixMethod " + caller.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        int containsInvocationTo = TestHelper.containsRelationTo(superConstructor, lRelations);
        assertTrue("Missing super constructor invocation relationship from " + caller.getUniqueName() + " to " + superConstructor.getUniqueName(), containsInvocationTo > 0);

        lRelations = aModel.getAssociations(superConstructor);
        assertTrue("FamixMethod " + superConstructor.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        containsInvocationTo = TestHelper.containsRelationTo(superConstructor, lRelations);
        assertTrue("Missing super constructor invocation relationship from " + caller.getUniqueName() + " to " + superConstructor.getUniqueName(), containsInvocationTo > 0);
    }

    @Test
    public void testAttributeInitialization() {
        FamixMethod testInit = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.<oinit>()", null));
        FamixAttribute simpleAttribute = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.ae.Test.c", null));
        Set<FamixAssociation> lRelations = aModel.getAssociations(testInit);
        assertTrue("FamixMethod " + testInit.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        int containsAccessTo = TestHelper.containsRelationTo(simpleAttribute, lRelations);
        assertTrue("Missing access relationship from " + testInit.getUniqueName() + " to " + simpleAttribute.getUniqueName(), containsAccessTo > 0);

        FamixMethod baseInit = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Base.<oinit>()", null));
        FamixAttribute baseAttribute = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.Base.a", null));
        lRelations = aModel.getAssociations(baseInit);
        assertTrue("FamixMethod " + baseInit.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        containsAccessTo = TestHelper.containsRelationTo(baseAttribute, lRelations);
        assertTrue("Missing access relationship from " + baseInit.getUniqueName() + " to " + baseAttribute.getUniqueName(), containsAccessTo > 0);
    }

    @Test
    public void testThisAttributeAccess() {
        FamixMethod accessorMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Variables.fieldAccess()", null));
        FamixAttribute simpleAttribute = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.Variables.field", null));

        Set<FamixAssociation> lRelations = aModel.getAssociations(accessorMethod);
        assertTrue("FamixMethod " + accessorMethod.getUniqueName() + " must contain relationships", lRelations.size() > 0);

        int containsInvocationTo = TestHelper.containsRelationTo(simpleAttribute, lRelations);
        assertEquals("Missing access relationship from " + accessorMethod.getUniqueName() + " to " + simpleAttribute.getUniqueName(), 1, containsInvocationTo);
    }

    @Test
    public void testQualifiedFieldAccess() {
        FamixMethod accessorMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Variables.fieldAccess()", null));
        FamixAttribute simpleAttribute = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.Sum.publicField", null));

        Set<FamixAssociation> lRelations = aModel.getAssociations(accessorMethod);
        assertTrue("FamixMethod " + accessorMethod.getUniqueName() + " must contain relationships", lRelations.size() > 0);

        int containsInvocationTo = TestHelper.containsRelationTo(simpleAttribute, lRelations);
        assertEquals("Missing access relationship from " + accessorMethod.getUniqueName() + " to " + simpleAttribute.getUniqueName(), 2, containsInvocationTo);
    }

    @Test
    public void testFieldAccessBySimpleName() {
        FamixMethod accessorMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Variables.fieldAccess()", null));
        FamixAttribute simpleAttribute = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.Variables.fInit", null));

        assertNotNull("FamixModel must contain method testPackage.Variables.fieldAccess()", accessorMethod);
        assertNotNull("FamixModel must contain attribute testPackage.Variables.fInit", simpleAttribute);

        Set<FamixAssociation> lRelations = aModel.getAssociations(accessorMethod);
        assertTrue("FamixMethod " + accessorMethod.getUniqueName() + " must contain relationships", lRelations.size() > 0);

        int containsInvocationTo = TestHelper.containsRelationTo(simpleAttribute, lRelations);
        assertEquals("Missing access relationship from " + accessorMethod.getUniqueName() + " to " + simpleAttribute.getUniqueName(), 1, containsInvocationTo);
    }

    @Test
    public void testClassInstanceCreation() {
        FamixMethod caller = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.computeSum()", null));
        FamixMethod constructor = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Sum.<init>(int,int)", null));

        Set<FamixAssociation> lRelations = aModel.getAssociations(caller);
        assertTrue("FamixMethod " + caller.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        int containsInvocationTo = TestHelper.containsRelationTo(constructor, lRelations);
        assertTrue("Missing instance creation invocation relationship from " + caller.getUniqueName() + " to " + constructor.getUniqueName(), containsInvocationTo > 0);

        lRelations = aModel.getAssociations(constructor);
        assertTrue("FamixMethod " + constructor.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        containsInvocationTo = TestHelper.containsRelationTo(constructor, lRelations);
        assertTrue("Missing instance creation invocation relationship from " + caller.getUniqueName() + " to " + constructor.getUniqueName(), containsInvocationTo > 0);
    }

    @Test
    public void testInnerClassMethodInvocation() {
        // FamixClass innerClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.Test$Inner", null));
        FamixMethod caller = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.accessInner()", null));
        FamixMethod innerMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test$Inner.innerMethod()", null));

        FamixMethod innerConstructor = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test$Inner.<init>()", null));
        Set<FamixAssociation> lRelations = aModel.getAssociations(caller);
        assertTrue("FamixMethod " + caller.getUniqueName() + " must contain relationships", lRelations.size() > 0);

        // invokes Inner class constructor
        boolean containsInvocationTo = TestHelper.containsRelationTo(new FamixInvocation(caller, innerConstructor), lRelations);
        assertTrue("Missing constructor invocation relationship from " + caller.getUniqueName() + " to " + innerConstructor.getUniqueName(), containsInvocationTo);

        // invokes innerMethod
        containsInvocationTo = TestHelper.containsRelationTo(new FamixInvocation(caller, innerMethod), lRelations);
        assertTrue("Missing invocation relationship from " + caller.getUniqueName() + " to " + innerMethod.getUniqueName(), containsInvocationTo);
    }

    @Test
    public void testMethodLocalVariableContainsSimple() {
        FamixMethod simpleMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Variables.m(int,int[])", null));
        FamixLocalVariable simpleLocal = aFactory.createLocalVariable("testPackage.Variables.m(int,int[]).local", null);
        simpleLocal.setSourceAnchor(new SourceAnchor("/TestProject1/src/testPackage/Variables.java", 0, 436));
        simpleLocal = (FamixLocalVariable) aModel.getElement(simpleLocal);

        assertNotNull("FamixModel must contain local variable testPackage.Variables.m(int,int[]).local", simpleLocal);
        assertTrue("FamixMethod must contain local variable", simpleMethod.getLocalVariables().size() > 0);
        boolean containsLocal = TestHelper.containsLocalVariable(simpleMethod, simpleLocal);
        assertTrue("FamixMethod must contain local variable simpleLocal", containsLocal);
        assertEquals("No or wrong parent method for local variable simpleLocal", simpleMethod, simpleLocal.getParent());
    }

    @Test
    public void testMethodLocalVariableContainsMulti() {
        FamixMethod simpleMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Variables.m(int,int[])", null));
        FamixLocalVariable multiLocal1 = aFactory.createLocalVariable("testPackage.Variables.m(int,int[]).multi1", null); 
        multiLocal1.setSourceAnchor(new SourceAnchor("/TestProject1/src/testPackage/Variables.java", 0, 450));
        multiLocal1 = (FamixLocalVariable) aModel.getElement(multiLocal1);
        FamixLocalVariable multiLocal2 = aFactory.createLocalVariable("testPackage.Variables.m(int,int[]).multi2", null);
        multiLocal2.setSourceAnchor(new SourceAnchor("/TestProject1/src/testPackage/Variables.java", 0, 458));
        multiLocal2 = (FamixLocalVariable) aModel.getElement(multiLocal2);

        assertNotNull("FamixModel must contain local variable testPackage.Variables.m(int,int[]).multi1", multiLocal1);
        assertNotNull("FamixModel must contain local variable testPackage.Variables.m(int,int[]).multi2", multiLocal2);
        assertTrue("FamixMethod must contain local variable", simpleMethod.getLocalVariables().size() > 0);

        boolean containsLocal1 = TestHelper.containsLocalVariable(simpleMethod, multiLocal1);
        assertTrue("FamixMethod must contain local variable multiLocal1", containsLocal1);
        assertEquals("No or wrong parent method for local variable multiLocal1", simpleMethod, multiLocal1.getParent());

        boolean containsLocal2 = TestHelper.containsLocalVariable(simpleMethod, multiLocal2);
        assertTrue("FamixMethod must contain local variable multiLocal2", containsLocal2);
        assertEquals("No or wrong parent method for local variable multiLocal2", simpleMethod, multiLocal2.getParent());
    }

    @Test
    public void testMethodLocalVariableContainsWithinFor() {
        FamixMethod simpleMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Variables.m(int,int[])", null));
        FamixLocalVariable withinFor = aFactory.createLocalVariable("testPackage.Variables.m(int,int[]).inFor", null); 
        withinFor.setSourceAnchor(new SourceAnchor("/TestProject1/src/testPackage/Variables.java", 0, 563));
        withinFor = (FamixLocalVariable) aModel.getElement(withinFor);

        assertNotNull("FamixModel must contain method testPackage.Variables.m(int,int[])", simpleMethod);
        assertNotNull("FamixModel must contain local variable testPackage.Variables.m(int,int[]).inFor", withinFor);

        assertTrue("FamixMethod must contain local variable", simpleMethod.getLocalVariables().size() > 0);

        boolean containsLocal = TestHelper.containsLocalVariable(simpleMethod, withinFor);
        assertTrue("FamixMethod must contain local variable inFor within for loop", containsLocal);
        assertEquals("No or wrong parent method for local variable inFor within for loop", simpleMethod, withinFor.getParent());
    }

    @Test
    public void testMethodLocalVariableContainsWithinCatchClause() {
        FamixMethod simpleMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Variables.m(int,int[])", null));
        FamixLocalVariable withinCatch = aFactory.createLocalVariable("testPackage.Variables.m(int,int[]).aiob", null); 
        withinCatch.setSourceAnchor(new SourceAnchor("/TestProject1/src/testPackage/Variables.java", 0, 667));
        withinCatch = (FamixLocalVariable) aModel.getElement(withinCatch);

        assertNotNull("FamixModel must contain method testPackage.Variables.m(int,int[])", simpleMethod);
        assertNotNull("FamixModel must contain local variable testPackage.Variables.m(int,int[]).aiob", withinCatch);

        assertTrue("FamixMethod must contain local variable", simpleMethod.getLocalVariables().size() > 0);

        boolean containsLocal = TestHelper.containsLocalVariable(simpleMethod, withinCatch);
        assertTrue("FamixMethod must contain local variable inFor within for loop", containsLocal);
        assertEquals("No or wrong parent method for local variable inFor within for loop", simpleMethod, withinCatch.getParent());
    }

    @Test
    public void testMethodReturnTypeVoid() {
        FamixClass classVoid = (FamixClass) aModel.getElement(aFactory.createClass("void", null));
        FamixMethod returnVoid = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.foo()", null));
        assertEquals("Return type of " + returnVoid.getUniqueName() + " must be void", 
                returnVoid.getDeclaredReturnClass(), classVoid);
    }

    @Test
    public void testMethodReturnTypePrimitive() {
        FamixClass classInt = (FamixClass) aModel.getElement(aFactory.createClass("int", null));
        FamixMethod returnPrimitive = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.computeSum()", null));
        assertEquals("Return type of " + returnPrimitive.getUniqueName() + " must be " + classInt.getUniqueName(), 
                returnPrimitive.getDeclaredReturnClass(), classInt);
    }

    @Test
    public void testMethodReturnTypeRef() {
        FamixClass classSum = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.Sum", null));
        FamixMethod returnRef = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.getRefSum()", null));
        assertNotNull("FamixModel must contain testPackage.Sum", classSum);
        assertEquals("Return type of " + returnRef.getUniqueName() + " must be " + classSum.getUniqueName(), 
                returnRef.getDeclaredReturnClass(), classSum);
    }

    @Test
    public void testMethodReturnTypeContainer() {
        FamixClass classVector = (FamixClass) aModel.getElement(aFactory.createClass("java.util.Vector<E>", null));
        FamixMethod returnContainer = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.getContainerSum()", null));
        assertNotNull("FamixModel must contain java.util.Vector<E>", classVector);
        assertEquals("Return type of " + returnContainer.getUniqueName() + " must be " + classVector.getUniqueName(), 
                returnContainer.getDeclaredReturnClass(), classVector);
    }

    @Test
    public void testMethodReturnTypeArray() {
        FamixClass classArray = (FamixClass) aModel.getElement(aFactory.createClass("<Array>", null));
        FamixMethod returnArray = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.getArraySum()", null));
        assertNotNull("FamixModel must contain testPackage.Sum", classArray);
        assertEquals("Return type of " + returnArray.getUniqueName() + " must be <Array>", 
                returnArray.getDeclaredReturnClass(), classArray);
    }

    @Test
    public void testConstructor() {
        FamixClass classBase = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.Base", null));
        FamixMethod initializer = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Base.<init>()", null));

        assertNotNull("FamixModel must contain initializer", initializer);

        boolean containsMethod = false;
        for (FamixMethod method : classBase.getMethods()) {
            if (method.getUniqueName().equals(initializer.getUniqueName())) {
                containsMethod = true;
            }
        }
        assertTrue("FamixClass testPackage.Base must contain initializer " + initializer.getUniqueName(), containsMethod);
        assertEquals("Parent class of " + initializer.getUniqueName() + " must be testPackage.Base", initializer.getParent(), classBase);
    }

    @Test
    public void testInitializer() {
        FamixClass classTest = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.Test", null));
        FamixMethod initializer = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.<init>()", null));

        assertNotNull("FamixModel must contain initializer", initializer);

        boolean containsMethod = false;
        for (FamixMethod method : classTest.getMethods()) {
            if (method.getUniqueName().equals(initializer.getUniqueName())) {
                containsMethod = true;
            }
        }
        assertTrue("FamixClass testPackage.ae.Test must contain initializer " + initializer.getUniqueName(), containsMethod);
        assertEquals("Parent class of " + initializer.getUniqueName() + " must be testPackage.ae.Test", initializer.getParent(), classTest);
    }

    @Test
    public void testStaticInitializer() {
        FamixClass classTest = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.Test", null));
        FamixMethod staticInitializer = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.<clinit>()", null));

        assertNotNull("FamixModel must contain a static initializer", staticInitializer);

        boolean containsMethod = false;
        for (FamixMethod method : classTest.getMethods()) {
            if (method.getUniqueName().equals(staticInitializer.getUniqueName())) {
                containsMethod = true;
            }
        }
        assertTrue("FamixClass testPackage.ae.Test must contain static initializer " + staticInitializer.getUniqueName(), containsMethod);
        assertEquals("Parent class of static initializer " + staticInitializer.getUniqueName() + " must be testPackage.ae.Test", staticInitializer.getParent(), classTest);
    }

    @Test
    public void testMethodModifiers() {
        FamixMethod publicAbstractMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Base.compute()", null));
        assertEquals("Type string of " + publicAbstractMethod.getUniqueName() + " must have modifier PUBLIC", 
                publicAbstractMethod.getModifiers() & Modifier.PUBLIC, Modifier.PUBLIC);
        assertEquals("Type string of " + publicAbstractMethod.getUniqueName() + " must have modifier ABSTRACT", 
                publicAbstractMethod.getModifiers() & Modifier.ABSTRACT, Modifier.ABSTRACT);
    }

//    @Test
//    public void testPackageContainer() {
//        FamixPackage parentPackage = (FamixPackage) aModel.getElement(aFactory.createPackage("testPackage", null));
//        FamixPackage subPackage = (FamixPackage) aModel.getElement(aFactory.createPackage("testPackage.ae", null));
//
//        assertNotNull("FamixModel must contain package testPackage", parentPackage);
//        assertNotNull("FamixModel must contain package testPackage.ae", subPackage);
//
//        assertTrue("FamixPackage must contain subpackage " + subPackage.getUniqueName(), parentPackage.getPackages().contains(subPackage));
//        assertEquals("No or wrong parent package for package " + subPackage.getUniqueName(), parentPackage, subPackage.getParent());
//    }

    @Test
    public void testClassContainer() {
        FamixPackage lPackage = (FamixPackage) aModel.getElement(aFactory.createPackage("testPackage", null));
        FamixClass classSum = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.Sum", null));

        assertNotNull("FamixModel must contain package testPackage", lPackage);
        assertNotNull("FamixModel must contain class testPackage.Sum", classSum);

        assertTrue("FamixPackage must contain class " + classSum.getUniqueName(), lPackage.getClasses().contains(classSum));
        assertEquals("No or wrong package for class " + classSum.getUniqueName(), lPackage, classSum.getParent());
    }


    @Test
    public void testAnonymousClassConstructorWithArguments(){
        FamixMethod constructor = (FamixMethod)aModel.getElement(aFactory.createMethod("testPackage.ae.Test$Inner$InnerInner.<init>(int)",null));
        assertNotNull(constructor);
        FamixClass clazz = (FamixClass)aModel.getElement(aFactory.createClass("testPackage.ae.Test$Inner$InnerInner", null));
        assertNotNull(clazz);

        assertEquals(constructor.getParent(), clazz);
    }

    @Test
    public void testAnonymousClassConstructorWithArgumentsInvocation(){
        FamixClass anonymClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.Test$Inner$1", null));
        FamixMethod initializerTest = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test$Inner.innerMethod()", null)); 
        FamixMethod initializer = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test$Inner$1.<init>(int)", null));
        FamixAttribute anonymAttribute = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.ae.Test$Inner$1.x", null));
        FamixMethod anonymObjectInitializer = (FamixMethod)aModel.getElement(aFactory.createMethod("testPackage.ae.Test$Inner$1.<oinit>()", null));

        assertNotNull(anonymClass);
        assertNotNull(initializerTest);
        assertNotNull(initializer);
        assertNotNull( anonymAttribute);
        assertNotNull(anonymObjectInitializer);


        assertTrue("Anonymous class must contain initializer " + initializer.getUniqueName(), anonymClass.getMethods().contains(initializer));
        assertEquals("No or wrong parent class for initializer " + initializer.getUniqueName(), 
                anonymClass, initializer.getParent());

        assertTrue(anonymClass.getMethods().contains(anonymObjectInitializer));
        assertEquals(anonymClass, anonymObjectInitializer.getParent());

        Set<FamixAssociation> lRelations = aModel.getAssociations(initializerTest);
        assertTrue("FamixMethod " + initializerTest.getUniqueName() + " must contain relationships", lRelations.size() > 0);
        int containsInvocationTo = TestHelper.containsRelationTo(initializer, lRelations);
        assertTrue("Missing invocation relationship from " + initializerTest.getUniqueName() + " to " + initializer.getUniqueName(), containsInvocationTo > 0);

        lRelations = aModel.getAssociations(anonymObjectInitializer);
        containsInvocationTo = TestHelper.containsRelationTo(anonymAttribute, lRelations);
        assertTrue("Missing invocation relationship from " + initializerTest.getUniqueName() + " to " + initializer.getUniqueName(), containsInvocationTo > 0);
    }

    @Test
    public void testSourceAnchorClass() throws Exception {
        FamixClass classSum = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.Sum", null));
        
        SourceAnchor anchor = classSum.getSourceAnchor();
        assertNotNull("Source anchor file of class " + classSum.getUniqueName() + " must not be null", anchor);
        assertEquals("Source anchor file of class " + classSum.getUniqueName() + " must be", "/TestProject1/src/testPackage/Sum.java", anchor.getFile());
        assertTrue("The start must be before the end position", anchor.getStartPos() <= anchor.getEndPos());
        InputStream fileStream = ResourcesPlugin.getWorkspace().getRoot().getFile(Path.fromPortableString(anchor.getFile())).getContents();
        String fileContent = TestHelper.getFileContent(fileStream);
        String classSumDecl = fileContent.substring(anchor.getStartPos(), anchor.getEndPos());
        assertTrue("Declaration of class must start with 'public class Sum'", classSumDecl.startsWith("public class Sum"));
        assertEquals("Declaration in source file must equal the source attribute ", classSumDecl, classSum.getSource());
    }

    @Test
    public void testSourceAnchorInnerClass() throws Exception {
        FamixClass classHelperClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.InnerAnonym$HelperClass", null));
        
        SourceAnchor anchor = classHelperClass.getSourceAnchor();
        assertNotNull("Source anchor file of class " + classHelperClass.getUniqueName() + " must not be null", anchor);
        assertEquals("Source anchor file of class " + classHelperClass.getUniqueName() + " must be", "/TestProject1/src/testPackage/InnerAnonym.java", anchor.getFile());
        assertTrue("The start must be before the end position", anchor.getStartPos() <= anchor.getEndPos());
        InputStream fileStream = ResourcesPlugin.getWorkspace().getRoot().getFile(Path.fromPortableString(anchor.getFile())).getContents();
        String fileContent = TestHelper.getFileContent(fileStream);
        String classSumDecl = fileContent.substring(anchor.getStartPos(), anchor.getEndPos());
        assertTrue("Declaration of class must start with 'public class HelperClass'", classSumDecl.startsWith("public class HelperClass"));
        assertEquals("Declaration in source file must equal the source attribute ", classSumDecl, classHelperClass.getSource());
    }

    @Test
    public void testSourceAnchorMethod() throws Exception {
        FamixMethod returnRef = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.getRefSum()", null));

        SourceAnchor anchor = returnRef.getSourceAnchor();
        assertNotNull("Source anchor file of method " + returnRef.getUniqueName() + " must not be null", anchor);
        assertEquals("Source anchor file of method " + returnRef.getUniqueName() + " must be", "/TestProject1/src/testPackage/ae/Test.java", anchor.getFile());
        assertTrue("The start must be before the end position", anchor.getStartPos() <= anchor.getEndPos());
        InputStream fileStream = ResourcesPlugin.getWorkspace().getRoot().getFile(Path.fromPortableString(anchor.getFile())).getContents();
        String fileContent = TestHelper.getFileContent(fileStream);
        String returnRefDecl = fileContent.substring(anchor.getStartPos(), anchor.getEndPos());
        assertTrue("Declaration of method must start with 'public Sum getRefSum()'", returnRefDecl.startsWith("public Sum getRefSum()"));
        assertEquals("Declaration in source file must equal the source attribute ", returnRefDecl, returnRef.getSource());
    }

    @Test
    public void testSourceAnchorAttribute() throws Exception {
        FamixAttribute fMulti3 = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.Variables.fMulti3", null));

        SourceAnchor anchor = fMulti3.getSourceAnchor();
        assertNotNull("Source anchor file of attribute " + fMulti3.getUniqueName() + " must not be null", anchor);
        assertEquals("Source anchor file of attribute " + fMulti3.getUniqueName() + " must be", "/TestProject1/src/testPackage/Variables.java", anchor.getFile());
        assertTrue("The start must be before the end position", anchor.getStartPos() <= anchor.getEndPos());
        InputStream fileStream = ResourcesPlugin.getWorkspace().getRoot().getFile(Path.fromPortableString(anchor.getFile())).getContents();
        String fileContent = TestHelper.getFileContent(fileStream);
        String returnRefDecl = fileContent.substring(anchor.getStartPos(), anchor.getEndPos());
        assertEquals("Declaration of attribute must be 'fMulti3'", "fMulti3", returnRefDecl);
        assertEquals("Declaration in source file must equal the source attribute ", returnRefDecl, fMulti3.getSource());
    }

    @Test
    public void testSourceAnchorAnonymousClass() throws Exception {
        FamixClass anonymClass = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.ae.Test$1", null));

        SourceAnchor anchor = anonymClass.getSourceAnchor();
        assertNotNull("Source anchor file of anonymous class " + anonymClass.getUniqueName() + " must not be null", anchor);
        assertEquals("Source anchor file of anonymous class " + anonymClass.getUniqueName() + " must be", "/TestProject1/src/testPackage/ae/Test.java", anchor.getFile());
        assertTrue("The start must be before the end position", anchor.getStartPos() <= anchor.getEndPos());
        InputStream fileStream = ResourcesPlugin.getWorkspace().getRoot().getFile(Path.fromPortableString(anchor.getFile())).getContents();
        String fileContent = TestHelper.getFileContent(fileStream);
        String classDecl = fileContent.substring(anchor.getStartPos(), anchor.getEndPos());
        assertTrue("Declaration of anonymous class must start with '{\n\t\tprivate int anonymAttribute = 0'", classDecl.startsWith("{\n\t\tprivate int anonymAttribute = 0"));
        assertEquals("Declaration in source file must equal the source attribute ", classDecl, anonymClass.getSource());
    }

    @Test
    public void testSourceAnchorFormalParameter() throws Exception {
        FamixParameter simpleParam = (FamixParameter) aModel.getElement(aFactory.createFormalParameter("testPackage.Variables.m(int,int[]).param", null, 0));
        FamixParameter arrayParam = (FamixParameter) aModel.getElement(aFactory.createFormalParameter("testPackage.Variables.m(int,int[]).arrayParam", null, 1));

        SourceAnchor anchor = simpleParam.getSourceAnchor();
        assertNotNull("Source anchor file of parameter " + simpleParam.getUniqueName() + " must not be null", anchor);
        assertEquals("Source anchor file of parameter " + simpleParam.getUniqueName() + " must be", "/TestProject1/src/testPackage/Variables.java", anchor.getFile());
        assertTrue("The start must be before the end position", anchor.getStartPos() <= anchor.getEndPos());
        InputStream fileStream = ResourcesPlugin.getWorkspace().getRoot().getFile(Path.fromPortableString(anchor.getFile())).getContents();
        String fileContent = TestHelper.getFileContent(fileStream);
        String simpleParamDecl = fileContent.substring(anchor.getStartPos(), anchor.getEndPos());
        assertEquals("Declaration of parameter must be 'param'", "int param", simpleParamDecl);
        assertEquals("Declaration in source file must equal the source attribute ", simpleParamDecl, simpleParam.getSource());

        anchor = arrayParam.getSourceAnchor();
        assertNotNull("Source anchor file of parameter " + arrayParam.getUniqueName() + " must not be null", anchor);
        assertEquals("Source anchor file of parameter " + arrayParam.getUniqueName() + " must be", "/TestProject1/src/testPackage/Variables.java", anchor.getFile());
        assertTrue("The start must be before the end position", anchor.getStartPos() <= anchor.getEndPos());
        String arrayParamDecl = fileContent.substring(anchor.getStartPos(), anchor.getEndPos());
        assertEquals("Declaration of parameter must be 'arrayParam'", "int[] arrayParam", arrayParamDecl);
        assertEquals("Declaration in source file must equal the source attribute ", arrayParamDecl, arrayParam.getSource());
    }

    @Test
    public void testSourceAnchorClassInheritance() throws Exception {
        //	      FamixClass interfaceBase = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.IBase", null));
        FamixClass classBase = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.Base", null));
        FamixClass classSum = (FamixClass) aModel.getElement(aFactory.createClass("testPackage.Sum", null));

        FamixAssociation inheritance = null;
        for (FamixAssociation ass : aModel.getAssociations(classSum)) {
            if (ass instanceof FamixInheritance && ass.getTo().equals(classBase)) {
                inheritance = ass;
                break;
            }
        }
        assertNotNull("The class Sum must contain 1 inheritance associtaino to class Base", inheritance);

        SourceAnchor anchor = inheritance.getSourceAnchor();
        assertNotNull("Source anchor file of invocation " + inheritance.getFrom().getUniqueName() + " - " + inheritance.getTo().getUniqueName() + " must not be null", anchor);
        assertEquals("Source anchor file of invocation " + inheritance.getFrom().getUniqueName() + " - " + inheritance.getTo().getUniqueName() + " must be", "/TestProject1/src/testPackage/Sum.java", anchor.getFile());
        assertTrue("The start must be before the end position", anchor.getStartPos() <= anchor.getEndPos());
        InputStream fileStream = ResourcesPlugin.getWorkspace().getRoot().getFile(Path.fromPortableString(anchor.getFile())).getContents();
        String fileContent = TestHelper.getFileContent(fileStream);
        String extendsStatement = fileContent.substring(anchor.getStartPos(), anchor.getEndPos());
        assertEquals("FamixInheritance of "  + inheritance.getFrom().getUniqueName() + " - " + inheritance.getTo().getUniqueName() + " must be 'Base'", "Base", extendsStatement);
    }

    @Test
    public void testSourceAnchorMethodInvocation() throws Exception {
        FamixMethod computeAllSums = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.computeAllSums()", null));
        FamixMethod compute = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.Sum.compute()", null));

        List<FamixAssociation> invocations = new LinkedList<FamixAssociation>();
        for (FamixAssociation ass : aModel.getAssociations(computeAllSums)) {
            if (ass instanceof FamixInvocation && ass.getTo().equals(compute)) {
                invocations.add(ass);
                //	              break;
            }
        }
        assertEquals("The method computeAllSums() must contain 2 calls to the method compute()", 2, invocations.size());

        //	      String[] invocationStrings = new String[] {"s.compute()", "((Sum) o).compute()"};
        Hashtable<String,String> invocationStrings = new Hashtable<String,String>();
        invocationStrings.put("s.compute()", "s.compute()");
        invocationStrings.put("((Sum) o).compute()", "((Sum) o).compute()");

        for (int i = 0; i < invocations.size(); i++) {
            SourceAnchor anchor = invocations.get(i).getSourceAnchor();
            assertNotNull("Source anchor file of invocation " + invocations.get(i).getFrom().getUniqueName() + " - " + invocations.get(i).getTo().getUniqueName() + " must not be null", anchor);
            assertEquals("Source anchor file of invocation " + invocations.get(i).getFrom().getUniqueName() + " - " + invocations.get(i).getTo().getUniqueName() + " must be", "/TestProject1/src/testPackage/ae/Test.java", anchor.getFile());
            assertTrue("The start must be before the end position", anchor.getStartPos() <= anchor.getEndPos());
            InputStream fileStream = ResourcesPlugin.getWorkspace().getRoot().getFile(Path.fromPortableString(anchor.getFile())).getContents();
            String fileContent = TestHelper.getFileContent(fileStream);
            String invokeStatement = fileContent.substring(anchor.getStartPos(), anchor.getEndPos());
            assertNotNull("FamixInvocation of "  + invocations.get(i).getFrom().getUniqueName() + " - " + invocations.get(i).getTo().getUniqueName() + " is not '" + invokeStatement + "'", invocationStrings.get(invokeStatement));
        }
    }

    @Test
    public void testSourceAnchorFieldInitAccess() throws Exception {
        FamixMethod oinitMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.<oinit>()", null));
        FamixAttribute containerSum = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.ae.Test.containerSum", null));

        List<FamixAssociation> accesses = new LinkedList<FamixAssociation>();
        for (FamixAssociation ass : aModel.getAssociations(oinitMethod)) {
            if (ass instanceof FamixAccess && ass.getTo().equals(containerSum)) {
                accesses.add(ass);
            }
        }
        assertEquals("The method <oinit>() must contain 1 access to the attribute containerSum", 1, accesses.size());

        SourceAnchor anchor = accesses.get(0).getSourceAnchor();
        assertNotNull("Source anchor file of access " + accesses.get(0).getFrom().getUniqueName() + " - " + accesses.get(0).getTo().getUniqueName() + " must not be null", anchor);
        assertEquals("Source anchor file of access " + accesses.get(0).getFrom().getUniqueName() + " - " + accesses.get(0).getTo().getUniqueName() + " must be", "/TestProject1/src/testPackage/ae/Test.java", anchor.getFile());
        assertTrue("The start must be before the end position", anchor.getStartPos() <= anchor.getEndPos());
        InputStream fileStream = ResourcesPlugin.getWorkspace().getRoot().getFile(Path.fromPortableString(anchor.getFile())).getContents();
        String fileContent = TestHelper.getFileContent(fileStream);
        String accessStatement = fileContent.substring(anchor.getStartPos(), anchor.getEndPos());
        assertEquals("FamixAccess of "  + accesses.get(0).getFrom().getUniqueName() + " - " + accesses.get(0).getTo().getUniqueName() + " must equal 'containerSum = new Vector<Sum>()'", "containerSum = new Vector<Sum>()", accessStatement);
    }

    @Test
    public void testSourceAnchorFieldAccess() throws Exception {
        FamixMethod fooMethod = (FamixMethod) aModel.getElement(aFactory.createMethod("testPackage.ae.Test.foo()", null));
        FamixAttribute aAttribute = (FamixAttribute) aModel.getElement(aFactory.createAttribute("testPackage.ae.Test.a", null));

        List<FamixAssociation> accesses = new LinkedList<FamixAssociation>();
        for (FamixAssociation ass : aModel.getAssociations(fooMethod)) {
            if (ass instanceof FamixAccess && ass.getTo().equals(aAttribute)) {
                accesses.add(ass);
            }
        }
        assertEquals("The method foo() must contain 2 access to the attribute a", 2, accesses.size());

        Hashtable<String,String> accessStrings = new Hashtable<String,String>();
        accessStrings.put("this.a", "this.a");
        for (int i = 0; i < accesses.size(); i++) {
            SourceAnchor anchor = accesses.get(i).getSourceAnchor();
            assertNotNull("Source anchor file of access " + accesses.get(i).getFrom().getUniqueName() + " - " + accesses.get(i).getTo().getUniqueName() + " must not be null", anchor);
            assertEquals("Source anchor file of access " + accesses.get(i).getFrom().getUniqueName() + " - " + accesses.get(i).getTo().getUniqueName() + " must be", "/TestProject1/src/testPackage/ae/Test.java", anchor.getFile());
            assertTrue("The start must be before the end position", anchor.getStartPos() <= anchor.getEndPos());
            InputStream fileStream = ResourcesPlugin.getWorkspace().getRoot().getFile(Path.fromPortableString(anchor.getFile())).getContents();
            String fileContent = TestHelper.getFileContent(fileStream);
            String accessStatement = fileContent.substring(anchor.getStartPos(), anchor.getEndPos());
            //	          System.out.println("St: " + accessStatement + " Anchor: " + anchor);
            assertNotNull("FamixAccess of "  + accesses.get(i).getFrom().getUniqueName() + " - " + accesses.get(i).getTo().getUniqueName() + " is not '" + accessStatement + "'", accessStrings.containsKey(accessStatement));
        }
    }
}
