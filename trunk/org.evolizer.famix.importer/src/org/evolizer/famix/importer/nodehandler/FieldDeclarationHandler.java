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
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixAttribute;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixMethod;

/**
 * Handles declaration of single and multiple fields, e.g., <code>Type f1, f2;</code>.
 * 
 * @author pinzger
 */
public class FieldDeclarationHandler extends AbstractASTNodeHandler {
    /**
     * The Logger.
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(FieldDeclarationHandler.class.getName());

    /**
     * The variable declaration fragments.
     */
    private List<VariableDeclaration> fFragments;
    /**
     * The declared data type.
     */
    private FamixClass fDataType;

    /**
     * The constructor.
     * 
     * @param crawler Instance of ASTCrawler to obtain current type and method information.
     */
    public FieldDeclarationHandler(ASTCrawler crawler) {
        super(crawler);
        fDataType = null;
    }

    /**
     * Helper method for returning the AST node.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return The field declaration AST node.
     */
    @Override
    public FieldDeclaration getASTNode() {
        return (FieldDeclaration) super.getASTNode();
    }

    /**
     * Handle field declaration statements.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#visit(org.eclipse.jdt.core.dom.ASTNode)
     * 
     * @param fieldDeclaration The AST node representing the field declaration
     * @return true, if contained nodes should be visited.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean visit(ASTNode fieldDeclaration) {
        setASTNode(fieldDeclaration);
        setFragments(getASTNode().fragments());

        try {
            initDataType();
            handleClassInitializer();
            createAttributes();
        } catch (NullPointerException e) {
            sLogger.error("Error processing field declaration in type " 
                    + (getCurrType() != null ? getCurrType().getUniqueName() : "<no type>") + "\n" 
                    + getASTNode().toString() 
                    + " - skipping field declaration");
        }

        // Because we have covered everything we need about field declaration,
        // we dont' have to go deeper into this node because otherwise we would get to
        // the VariableDeclarionFragment nodes.
        return false;
    }

    private void initDataType() {
        if (getDataType() == null) {
            ITypeBinding pBinding = getASTNode().getType().resolveBinding();
            setDataType(getClass(pBinding, getASTNode().getType(), false));
            getModel().addElement(getDataType());
        }
    }

    /**
     * Remove initializer methods from stack.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#endVisit()
     */
    @Override
    public void endVisit() {
        setCurrMethodFromMethodReminder();
    }

    /**
     * Whenever we encounter a static field we add a static class initializer method to the current type. The source
     * anchor of the initializer is set to the type declaration node.
     */
    private void handleClassInitializer() {
        if (Modifier.isStatic(getASTNode().getModifiers())) {
            addCurrMethodToMethodReminder();

            String lMethodID =
                    getCurrType().getUniqueName() + AbstractFamixEntity.NAME_DELIMITER + AbstractFamixEntity.CLASS_INIT_METHOD;
            FamixMethod classInit = getFactory().createMethod(lMethodID, null);
            classInit = (FamixMethod) getModel().addElement(classInit);
            classInit.setParent(getCurrType());
            getCurrType().getMethods().add(classInit);
            classInit.setModifiers(getASTNode().getModifiers());
            classInit.setSourceAnchor(getCurrType().getSourceAnchor());
            setCurrMethod(classInit);
        }
    }

    private void createAttributes() {
        FamixAttribute lField = null;

        int lModifiers = getASTNode().getModifiers();
        for (VariableDeclaration fragment : getFragments()) {
            String lFieldID = null;
            String lSimpleName = fragment.getName().getFullyQualifiedName();
            if (lSimpleName != null) {
                lFieldID = getCurrType().getUniqueName() + AbstractFamixEntity.NAME_DELIMITER + lSimpleName;
                lField = getFactory().createAttribute(lFieldID, null);
                lField = (FamixAttribute) getModel().addElement(lField);
                lField.setModifiers(lModifiers);
                lField.setSourceAnchor(getSourceAnchor(fragment));
                lField.setParent(getCurrType());
                getCurrType().getAttributes().add(lField);
                lField.setDeclaredClass(getDataType());

                // If there is any initialization to this field then we write them as an access by <init> or <clinit>
                Expression lInit = fragment.getInitializer();
                if (lInit != null) {
                    FamixAssociation access = getFactory().createAccess(getCurrMethod(), lField);
                    access.setSourceAnchor(getSourceAnchor(fragment));
                    getModel().addRelation(access);

                    lInit.accept(getCrawler());
                }
            } else {
                sLogger.warn("Could not get name of variable declaration " + fragment);
            }
        }
    }

    /**
     * Sets the list of variable declaration fragments. 
     * 
     * @param fragments The variable declaration fragments.
     */
    protected void setFragments(List<VariableDeclaration> fragments) {
        fFragments = fragments;
    }

    /**
     * Returns the list of variable declaration fragments.
     * 
     * @return The variable declaration fragments.
     */
    public List<VariableDeclaration> getFragments() {
        return fFragments;
    }

    /**
     * Sets the declared data type.
     * 
     * @param dataType The declared data type.
     */
    protected void setDataType(FamixClass dataType) {
        fDataType = dataType;
    }

    /**
     * Returns the declared data type.
     * 
     * @return The declared data type.
     */
    public FamixClass getDataType() {
        return fDataType;
    }
}
