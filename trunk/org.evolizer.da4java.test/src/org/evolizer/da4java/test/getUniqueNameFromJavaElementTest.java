package org.evolizer.da4java.test;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.evolizer.da4java.plugin.selectionhandler.JavaElementUtilities;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;

public class getUniqueNameFromJavaElementTest {
	Mockery context = new JUnit4Mockery();
	
	@Test
	public void testPackageFragment() throws JavaModelException {
		// set up
		final IPackageFragment packageFragmentEmpty = context.mock(IPackageFragment.class, "empty"); 
		final IPackageFragment packageFragment = context.mock(IPackageFragment.class);
		
		context.checking(new Expectations() {{
			allowing (packageFragmentEmpty).getElementName();will(returnValue(""));
			allowing (packageFragmentEmpty).containsJavaResources();will(returnValue(true));
			allowing (packageFragment).getElementName();will(returnValue("foo"));
	    }});
		
		// test the regular package fragment
		assertEquals(JavaElementUtilities.getUniqueNameFromJavaElement(packageFragment), "foo");
		
		// and the irregular (empty) one
		assertEquals(JavaElementUtilities.getUniqueNameFromJavaElement(packageFragmentEmpty), AbstractFamixEntity.DEFAULT_PACKAGE_NAME);		
	}
	
	
	@Test
	public void testCompilationUnit() throws JavaModelException {
		// set up
		final ICompilationUnit compilationUnit = context.mock(ICompilationUnit.class); 
		final IType type = context.mock(IType.class); 
		
		// TODO XXX need to mock IType as well
		context.checking(new Expectations() {{
			allowing (compilationUnit).findPrimaryType();will(returnValue(type));
			allowing (type).getFullyQualifiedName();will(returnValue("foo"));
	    }});		
		
		assertEquals(JavaElementUtilities.getUniqueNameFromJavaElement(compilationUnit), "foo");
	}
	
	@Test
	public void testType() throws JavaModelException {
		// set up
		final IType type = context.mock(IType.class); 
		
		context.checking(new Expectations() {{
			allowing (type).getFullyQualifiedName();will(returnValue("foo"));
	    }});
		
		assertEquals(JavaElementUtilities.getUniqueNameFromJavaElement(type), "foo");
	}
	
	@Test
	public void testMethod() throws JavaModelException {
		final IMethod method = context.mock(IMethod.class); 
		final IMethod constructor = context.mock(IMethod.class, "constructor");
		final IType type = context.mock(IType.class); 
		
		context.checking(new Expectations() {{
			allowing (method).getDeclaringType();will(returnValue(type));
			allowing (constructor).getDeclaringType();will(returnValue(type));
			
			allowing (method).getElementName();will(returnValue("bar"));
			
			// make the two methods take different execution paths
			allowing (method).isConstructor();will(returnValue(false));
			allowing (constructor).isConstructor();will(returnValue(true));
			
			allowing (type).getFullyQualifiedName();will(returnValue("foo"));
	    }});
	
		assertEquals(JavaElementUtilities.getUniqueNameFromJavaElement(method), "foo" + "." + "bar");
		assertEquals(JavaElementUtilities.getUniqueNameFromJavaElement(constructor), "foo" + "." + AbstractFamixEntity.CONSTRUCTOR_PREFIX);
	
	}
	
	@Test
	public void testField() throws JavaModelException {
		// set up
		final IField field = context.mock(IField.class); 
		final IType type = context.mock(IType.class); 
		
		context.checking(new Expectations() {{
			allowing (field).getDeclaringType();will(returnValue(type));
			allowing (field).getElementName();will(returnValue("field"));
			allowing (type).getFullyQualifiedName();will(returnValue("foo"));
	    }});
		
		assertEquals(JavaElementUtilities.getUniqueNameFromJavaElement(field), "foo" + "." + "field");
	}
}
