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
package org.evolizer.famix.importer.nodehandler;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixClass;

/**
 * Handles top level and inner class declarations. The current type is pushed onto the type reminder stack to handle
 * nesting of types. After the type declaration has been processed the type is removed from the stack and the parent
 * type is set as the current type.
 * 
 * @author pinzger
 */
public class TypeDeclarationHandler extends AbstractTypeHandler {
    /**
     * The Logger.
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(TypeDeclarationHandler.class.getName());

    /**
     * Reference to the source code of a top level class.
     */
    private String fCurrSource;

    /**
     * The constructor
     * 
     * @param crawler
     *            Instance of ASTCrawler to obtain current type and method information.
     */
    public TypeDeclarationHandler(ASTCrawler crawler) {
        super(crawler);
    }

    /**
     * Helper method for returning the AST node.
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return The type declaration AST node.
     */
    @Override
    public TypeDeclaration getASTNode() {
        return (TypeDeclaration) super.getASTNode();
    }

    /**
     * Process top-level and inner class declarations. To handle nested classes the current type and method are pushed
     * onto stacks.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#visit(org.eclipse.jdt.core.dom.ASTNode)
     * 
     * @param typeDeclaration Type declaration node.
     * @return true, if successfully process otherwise false.
     */
    @Override
    public boolean visit(ASTNode typeDeclaration) {
        boolean visitChildren = true;
        
        setASTNode(typeDeclaration);
        setTypeBinding(getASTNode().resolveBinding());

        try {
            if (getTypeBinding() != null) {
                if (!getTypeBinding().isTopLevel()) {
                    addCurrTypeToTypeReminder();
                    addCurrMethodToMethodReminder();
                }
    
                // Insert this type
                FamixClass lCurrType = getClass(getTypeBinding(), null, false);
                lCurrType.setModifiers(getTypeBinding().getModifiers()
                        | (getTypeBinding().isInterface() ? AbstractFamixEntity.MODIFIER_INTERFACE : 0));
                lCurrType.setSourceAnchor(getSourceAnchor(getASTNode()));
    
                getModel().addElement(lCurrType);
                setCurrType(lCurrType);
    
                initAnonymousClassCounter();
                setCurrMethod(addObjectInitMethod(lCurrType));
    
                addParentLink();
    
                if (getTypeBinding().isTopLevel()) {
                    lCurrType.setSource(fCurrSource);
                }
    
                addInheritanceLinks();
            } else {
                visitChildren = false;
            }
        } catch (NullPointerException e) {
            sLogger.error("Error processing type declaration " 
                    + getASTNode().toString() 
                    + " - skipping type declaration");
            visitChildren = false;
        }

        return visitChildren;
    }

    /**
     * Post-process class declaration. Add attributes to handle unresolved method invocations. Get and set the parent
     * class and method from the stacks.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#endVisit()
     */
    @Override
    public void endVisit() {
        if (getTypeBinding() != null) {
            try {
                addFieldsToUnresolvedInvocationsInScope();
            } catch (NullPointerException e) {
                sLogger.error("Error post processing type declaration " + getASTNode().toString());
            }
    
            // if there has not been a constructor explicitly defined
            // we most at minimum add the default constructor because thats what is automatically called when
            // creating an instance of a class that has nor explicitly defined constructor
            createDefaultConstructor();
    
            // restore current type and temp method
            setCurrTypeFromTypeReminder();
            setCurrMethodFromMethodReminder();
        }
    }


    
    /**
     * Initialize anonymous class counter for each class declaration to handle unresolved anonymous classes.
     */
    private void initAnonymousClassCounter() {
        getAnonymClassCounter().put(getCurrType(), 0);
    }

    /**
     * Add sub-typing and inheritance associations.
     */
    public void addInheritanceLinks() {
        if (getASTNode().isInterface()) {
            handleInterfaces();
        } else {
            handleClassExtension();
            handleInterfaces();
        }
    }

    /**
     * Add sub-typing associations with interfaces.
     * 
     * If an interface extends other interfaces, it is considered as subtyping as well as if a class implements
     * interfaces. In both cases no source code is inherited and we therefore have subtyping and use one method to to
     * handle them.
     */
    @SuppressWarnings("unchecked")
    private void handleInterfaces() {
        List<Type> superInterfaces = getASTNode().superInterfaceTypes();
        for (Type superInterface : superInterfaces) {
            FamixClass lInterface = getClass(superInterface.resolveBinding(), superInterface, false);
            lInterface = (FamixClass) getModel().addElement(lInterface);
            lInterface.setModifiers(lInterface.getModifiers() | AbstractFamixEntity.MODIFIER_INTERFACE);
            FamixAssociation subtyping = getFactory().createSubtyping(getCurrType(), lInterface);
            subtyping.setSourceAnchor(getSourceAnchor(superInterface));
            getModel().addRelation(subtyping);
        }
    }

    /**
     * Add inheritance associations with superclass.
     */
    private void handleClassExtension() {
        Type superType = getASTNode().getSuperclassType();
//        String nameFromBinding = "Object";
//        if ((getTypeBinding() != null) && (getTypeBinding().getSuperclass() != null)
//                && (getTypeBinding().getSuperclass().getName() != null)) {
//            nameFromBinding = getTypeBinding().getSuperclass().getName();
//        }

//        FamixClass lSuperClass = null;
//        if ((superType == null) && nameFromBinding.equals("Object")) {
//            lSuperClass = getClass(getTypeBinding().getSuperclass(), null, false);
//        } else {
//            lSuperClass = getClass(superType.resolveBinding(), superType, false);
//        }
        if (superType != null) {
            FamixClass lSuperClass = getClass(superType.resolveBinding(), superType, false);
	        lSuperClass = (FamixClass) getModel().addElement(lSuperClass);
	        FamixAssociation inheritance = getFactory().createInheritance(getCurrType(), lSuperClass);
	        if (superType != null) {
	        	inheritance.setSourceAnchor(getSourceAnchor(superType));
	        } else {
	        	inheritance.setSourceAnchor(getSourceAnchor());
	        }
	        getModel().addRelation(inheritance);
        }
    }

    /**
     * Set the source code of the top level type declaration (i.e., the compilation unit).
     * 
     * @param source
     *            The source code of the corresponding compilation unit.
     */
    public void setSource(String source) {
        fCurrSource = source;
    }
}
