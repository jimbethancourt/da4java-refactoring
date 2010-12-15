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
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixClass;

/**
 * Handles enum declarations. 
 *
 * @author pinzger
 *
 */
public class EnumDeclarationHandler extends AbstractTypeHandler {
    /**
     * The Logger.
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(EnumDeclarationHandler.class.getName());

    /**
     * Reference to the source code of a top level class.
     */
    private String fCurrSource;
    
    /**
     * The constructor.
     * 
     * @param crawler   The ASTCrawler instance.
     */
    public EnumDeclarationHandler(ASTCrawler crawler) {
        super(crawler);
    }

    /**
     * Helper method for returning the AST node.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return The enum declaration AST node
     */
    @Override
    public EnumDeclaration getASTNode() {
        return (EnumDeclaration) super.getASTNode();
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
            if (!getTypeBinding().isTopLevel()) {
                addCurrTypeToTypeReminder();
                addCurrMethodToMethodReminder();
            }

            // Insert this enum type
            FamixClass lCurrType = getClass(getTypeBinding(), null, false);
            lCurrType.setModifiers(getTypeBinding().getModifiers() | AbstractFamixEntity.MODIFIER_ENUM);
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
        } catch (NullPointerException e) {
            sLogger.error("Error processing enum declaration " 
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
        try {
            addFieldsToUnresolvedInvocationsInScope();
        } catch (NullPointerException e) {
            sLogger.error("Error post processing enum declaration " + getASTNode().toString() + "\n"
                            + e.getMessage());
        }

        // if there has not been a constructor explicitly defined
        // we most at minimum add the default constructor because thats what is automatically called when
        // creating an instance of a class that has nor explicitly defined constructor
        createDefaultConstructor();

        // restore current type and temp method
        setCurrTypeFromTypeReminder();
        setCurrMethodFromMethodReminder();
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
    @SuppressWarnings("unchecked")
    public void addInheritanceLinks() {
        List<Type> superInterfaces = getASTNode().superInterfaceTypes();
        for (Type superInterface : superInterfaces) {
            FamixClass lInterface = getClass(superInterface.resolveBinding(), superInterface, false);
            lInterface = (FamixClass) getModel().addElement(lInterface);
            lInterface.setModifiers(lInterface.getModifiers() | AbstractFamixEntity.MODIFIER_INTERFACE);
            FamixAssociation subtyping = getFactory().createSubtyping(getCurrType(), lInterface);
            subtyping.setSourceAnchor(getSourceAnchor());
            getModel().addRelation(subtyping);
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
