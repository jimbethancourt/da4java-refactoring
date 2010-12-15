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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.evolizer.core.util.projecthandling.JavaProjectHelper;
import org.evolizer.famix.importer.FamixModelFactory;
import org.evolizer.famix.importer.ProjectParser;
import org.evolizer.famix.model.entities.FamixModel;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author pinzger
 *
 */
public class FamixImporterMultiProjectParsingTest {
    /**
     * The FAMIX model container
     */
    protected static FamixModel aModel = null;
    /**
     * The FAMIX model factory used by the importer
     */
    protected static FamixModelFactory aFactory = new FamixModelFactory();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception, AssertionError{
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject project1 = workspace.getRoot().getProject("TestProject1");
        if(project1.exists()){
            project1.delete(true, true, new NullProgressMonitor());
        }
		
		IProject project2 = workspace.getRoot().getProject("TestProject2");
        if(project2.exists()){
            project2.delete(true, true, new NullProgressMonitor());
        }
        
        JavaCore.setOptions(TestHelper.getJavaCoreOptions());
        IJavaModel model =  JavaCore.create(workspace.getRoot());

		setUpProject1();
		setUpProject2();
		
        IJavaProject javaProject1 =model.getJavaProject("TestProject1");
        assertTrue("TestProject1 does not exist", project2.exists());
		IJavaProject javaProject2 = model.getJavaProject("TestProject2");
		assertTrue("TestProject2 does not exist", project2.exists());

		List<IJavaElement> selection = new LinkedList<IJavaElement>();
		selection.add(javaProject1);
		selection.add(javaProject2);
		
		ProjectParser parser = new ProjectParser(selection);
		parser.parse(null);
//		parser.parseProject(project, null);
		aModel = parser.getModel();
	}

	protected static void setUpProject1() throws CoreException, IOException{
		JavaProjectHelper helper = new JavaProjectHelper();
		helper.createProject("TestProject1", "bin", null);
		helper.addStandartSourceFolder(null);
		helper.addPackage("testPackage", null);
		
		helper.addSourceFile("testPackage", "Base.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/Base.java")), null);
		helper.addSourceFile("testPackage", "IBase.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/IBase.java")), null);
		helper.addSourceFile("testPackage", "InnerAnonym.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/InnerAnonym.java")), null);
		helper.addSourceFile("testPackage", "Sum.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/Sum.java")), null);
		helper.addSourceFile("testPackage", "Variables.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/Variables.java")), null);
	}
	
	protected static void setUpProject2() throws CoreException, IOException{
		JavaProjectHelper helper = new JavaProjectHelper();
		helper.createProject("TestProject2", "bin", null);
		helper.addStandartSourceFolder(null);
		helper.addPackage("testPackage.ae", null);
		
		helper.addSourceFile("testPackage.ae", "Test.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/ae/Test.java")), null);
	}

	@Test
	public void testModelContainer(){
		assertNotNull(aModel);
		assertTrue("FamixModel does not contain class testPackage.Sum", aModel.contains(aFactory.createClass("testPackage.Sum", null)));
		assertTrue("FamixModel does not contain class testPackage.ae.Test", aModel.contains(aFactory.createClass("testPackage.ae.Test", null)));
		assertTrue("FamixModel does not contain inner class testPackage.ae.Test$Inner", aModel.contains(aFactory.createClass("testPackage.ae.Test$Inner", null)));
		assertTrue("FamixModel does not contain inner inner classe testPackage.ae.Test$InnerInner", aModel.contains(aFactory.createClass("testPackage.ae.Test$Inner$InnerInner", null)));
	}
}
