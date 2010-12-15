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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.evolizer.core.util.projecthandling.JavaProjectHelper;
import org.evolizer.famix.importer.ProjectParser;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixLocalVariable;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.entities.FamixModel;


/**
 * FamixClass providing some helper methods to check the extracted FAMIX model.
 * 
 * @author pinzger
 */
public class TestHelper {
	public static int containsRelationTo(AbstractFamixEntity to, Set<FamixAssociation> lRelations) {
		int count = 0;
		for (FamixAssociation association : lRelations) {
			if (association.getTo().equals(to)) {
				count++;
			}
		}
		return count;
	}
	public static boolean containsRelationTo(FamixAssociation pRel, Set<FamixAssociation> pRelations) {
		boolean containsRelation = false;
		for (FamixAssociation association : pRelations) {
			if (association.getClass() == pRel.getClass() && association.getTo().equals(pRel.getTo())) {
				containsRelation = true;
			}
		}
		return containsRelation;
	}
	
	public static String readFile(String fileName) throws Exception {
		StringBuffer result = new StringBuffer();
		char b[] = new char[2048];
		FileReader fis = new FileReader(fileName);
		int n;
		while ((n = fis.read(b)) > 0) {
			result.append(b, 0, n);
		}
		fis.close();
		return result.toString();
	}

    /**
     * Configures the jdt parser for Java 1.5
     * 
     * @return  Hashtable containing the configuration
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Hashtable getJavaCoreOptions() {
        Hashtable options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);

        return options;
    }

    /**
     * Extracts the FAMIX model from the given Java project
     * 
     * @param project   the given Java project
     * @return  the FAMIX model
     */
    public static FamixModel parseProject(IJavaProject project) {
        List<IJavaElement> selection = new LinkedList<IJavaElement>();
        selection.add(project);
        ProjectParser parser = new ProjectParser(selection);
        parser.parse(null);

        return parser.getModel();
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
    
        helper.addSourceFile("testPackage", "Base.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/Base.java")), null);
        helper.addSourceFile("testPackage", "IBase.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/IBase.java")), null);
        helper.addSourceFile("testPackage", "InnerAnonym.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/InnerAnonym.java")), null);
        helper.addSourceFile("testPackage", "Sum.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/Sum.java")), null);
        helper.addSourceFile("testPackage", "Variables.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/Variables.java")), null);
        helper.addSourceFile("testPackage.ae", "Test.java", TestHelper.getFileContent(FamixImporterTestPlugin.openBundledFile("./data/testPackage/ae/Test.java")), null);
    }
    /**
     * Helper method to obtain the content of a source file.
     * 
     * @param in	The input stream
     * @return	The content in a string
     * @throws IOException
     */
    public static String getFileContent(InputStream in) throws IOException{
        String content = "";
        BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
        while(inReader.ready()){
            String line = inReader.readLine();
            content = content + line +"\n";
        }
        return content;
    }
    
    public static boolean containsLocalVariable(FamixMethod simpleMethod, FamixLocalVariable simpleLocal) {
        boolean containsLocal = false;
        for (FamixLocalVariable lLocal : simpleMethod.getLocalVariables()) {
            if (lLocal.getUniqueName().equals(simpleLocal.getUniqueName())) {
                containsLocal = true;
                break;
            }
        }
        return containsLocal;
    }
}
