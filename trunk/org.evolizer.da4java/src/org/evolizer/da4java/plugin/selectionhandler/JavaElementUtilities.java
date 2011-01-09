package org.evolizer.da4java.plugin.selectionhandler;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.evolizer.core.exceptions.EvolizerRuntimeException;
import org.evolizer.famix.model.entities.AbstractFamixEntity;

/**
 * Gets the FAMIX model conform unique name for the given Java element.
 * Currently the class supports:
 * <ul>
 * <li>{@link IPackageFragment}</li>
 * <li>{@link ICompilationUnit}</li>
 * <li>{@link IType}</li>
 * <li>{@link IMethod}</li>
 * <li>{@link IField}</li>
 * </ul>
 * 
 * @param element the Java element
 * 
 * @return the FAMIX conform unique name
 * 
 * @throws JavaModelException the java model exception
 */

// TODO comments need work. @see? @seeAlso?
public class JavaElementUtilities {	

	/**
	 * A catch-all method, if we do not support the element type.
	 * @param element the Java Element
	 * @throws EvolizerRuntimeException The Java Model Exception
	 */
    public static String getUniqueNameFromJavaElement(IJavaElement element) throws EvolizerRuntimeException {
        throw new EvolizerRuntimeException("Element type not supported " + element.getElementType());
    }
    
    public static String getUniqueNameFromJavaElement(IType type)
    {
    	return type.getFullyQualifiedName();
    }
    
    
    public static String getUniqueNameFromJavaElement(IPackageFragment packageFragment)
    {
    	String uniqueName = packageFragment.getElementName();   
    	
    	try {
			if (uniqueName.equals("") && packageFragment.containsJavaResources()) {
			    uniqueName = AbstractFamixEntity.DEFAULT_PACKAGE_NAME;
			}
		} catch (JavaModelException jme) {
            throw new EvolizerRuntimeException("Error determining FAMIX type of Java element " + packageFragment.getElementName(), jme);
		}
    	
        return uniqueName;
    }
    
	/**
	 * Gets the FAMIX model conform unique name for the given Java compilation unit.
	 *
	 * @param element the Java element
	 * @return the fully qualified name of the type.
	 * @throws JavaModelException the java model exception
	 */
    public static String getUniqueNameFromJavaElement(ICompilationUnit compilationUnit)
    {
    	IType primaryType = compilationUnit.findPrimaryType();
        return primaryType.getFullyQualifiedName();
    }
    
    public static String getUniqueNameFromJavaElement(IMethod method)
    {
        String uniqueName = method.getDeclaringType().getFullyQualifiedName();
        
        try {
			if (method.isConstructor()) {
			    uniqueName += "." + AbstractFamixEntity.CONSTRUCTOR_PREFIX;
			} else {
			    uniqueName += "." + method.getElementName();
			}
		} catch (JavaModelException jme) {
            throw new EvolizerRuntimeException("Error determining FAMIX type of Java element " + method.getElementName(), jme);
		}
        
        return uniqueName;
    }
    
    public static String getUniqueNameFromJavaElement(IField field)
    {
    	IType declaringType = field.getDeclaringType();
        return declaringType.getFullyQualifiedName() + "." + field.getElementName(); 
    }
    
}
