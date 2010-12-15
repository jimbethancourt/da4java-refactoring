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

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixMethod;

/**
 * Handle ASTNodes representing anonymous class declarations.
 * 
 * @author pinzger
 */
public class AnonymTypeHandler extends AbstractTypeHandler {
    /**
     * The Logger.
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(AnonymTypeHandler.class.getName());

    /**
     * The constructor.
     * 
     * @param crawler Instance of ASTCrawler to obtain current type and method information.
     */
    public AnonymTypeHandler(ASTCrawler crawler) {
        super(crawler);
    }

    /**
     * Helper method for returning the AST node
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return The anonymous type declaration AST node
     */
    @Override
    public AnonymousClassDeclaration getASTNode() {
        return (AnonymousClassDeclaration) super.getASTNode();
    }

    /**
     * Handle anonymous type declaration statements.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#visit(org.eclipse.jdt.core.dom.ASTNode)
     * 
     * @param anonymTypeDeclaration The AST node representing the anonymous type declaration
     * @return true, if contained nodes should be visited.
     */
    @Override
    public boolean visit(ASTNode anonymTypeDeclaration) {
        boolean visitChilder =  true;
        
        setASTNode(anonymTypeDeclaration);
        setTypeBinding(getASTNode().resolveBinding());

        try {
            addCurrTypeToTypeReminder();
            FamixClass lCurrType = getClass(getTypeBinding(), null, true);
            // if (lCurrType.getParent() != null) {
            // logger.error("Anonymous type conflict - skipping the type: " + lCurrType.getUniqueName() + " in method "
            // + getCurrMethod().getUniqueName());
            // // return true;
            // }
            lCurrType = (FamixClass) getModel().addElement(lCurrType);
            lCurrType.setSourceAnchor(getSourceAnchor());
            setCurrType(lCurrType);

            // also keep a counter for nested anonymous classes
            getAnonymClassCounter().put(getCurrType(), 0);

            FamixMethod anonymousClassInitMethod = addObjectInitMethod(getCurrType());
            getCurrMethod().getAnonymClasses().add(getCurrType());
            getCurrType().setParent(getCurrMethod());

            addInheritanceLinks();

            addCurrMethodToMethodReminder();
            setCurrMethod(anonymousClassInitMethod);
        } catch (NullPointerException e) {
            sLogger.error("Error processing anonymous type declaration in method " 
                    + (getCurrMethod() != null ? getCurrMethod().getUniqueName() : "<no method>") + "\n" 
                    + getASTNode().toString() 
                    + " - skipping declaration fragment");
            visitChilder = false;
        }

        return visitChilder;
    }

    /** 
     *  Handle nesting and the variables in scope of the type declaration.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#endVisit()
     */
    @Override
    public void endVisit() {
        addFieldsToUnresolvedInvocationsInScope();

        setCurrTypeFromTypeReminder();
        setCurrMethodFromMethodReminder();
    }

    /**
     * Add the inheritance links of an anonymous type. The type of inheritance depends
     * on the class/interface the anonymous class is derived from. In case of a class it
     * is an <code>inherits</code> dependency, in case of an interface and <code>subtyping</code>
     * dependency.
     *  
     * {@inheritDoc}
     */
    public void addInheritanceLinks() {
        FamixClass lImplementedType = null;
        if (getTypeBinding() != null) {
            if (getTypeBinding().getInterfaces().length != 0) {
                // An anonymous class can implement only 1 interface
                lImplementedType = getClass(getTypeBinding().getInterfaces()[0], null, false);
                lImplementedType = (FamixClass) getModel().addElement(lImplementedType);
                lImplementedType.setModifiers(lImplementedType.getModifiers() | AbstractFamixEntity.MODIFIER_INTERFACE);
                FamixAssociation subtyping = getFactory().createSubtyping(getCurrType(), lImplementedType);
                subtyping.setSourceAnchor(getSourceAnchor());
                getModel().addRelation(subtyping);
            } else {
                lImplementedType = getClass(getTypeBinding().getSuperclass(), null, false);
                lImplementedType = (FamixClass) getModel().addElement(lImplementedType);
                FamixAssociation inheritance = getFactory().createInheritance(getCurrType(), lImplementedType);
                inheritance.setSourceAnchor(getSourceAnchor());
                getModel().addRelation(inheritance);
            }
        } else {
            String lClassID = AbstractASTNodeHandler.UNDEFINED_BINDING;
            if (getASTNode().getParent() instanceof ClassInstanceCreation) {
                SimpleType castedType = (SimpleType) ((ClassInstanceCreation) getASTNode().getParent()).getType();
                lClassID += AbstractFamixEntity.NAME_DELIMITER + castedType.getName().getFullyQualifiedName();
            }
            lImplementedType = getFactory().createClass(lClassID, null);
            lImplementedType = (FamixClass) getModel().addElement(lImplementedType);
            // We do not know whether it is a superclass or an interface; we create a Subtype
            FamixAssociation subtyping = getFactory().createSubtyping(getCurrType(), lImplementedType);
            subtyping.setSourceAnchor(getSourceAnchor());
            getModel().addRelation(subtyping);
        }
        // getModel().addElement(lImplementedType);
    }
}
