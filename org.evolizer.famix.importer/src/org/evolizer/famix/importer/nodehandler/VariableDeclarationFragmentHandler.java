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
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixLocalVariable;

/**
 * Handles single local variable declarations, e.g., <code>int local</code>.
 * It seems that single variable declarations in catch clauses also are treated 
 * as <code>VariableDeclarationFragments</code>.
 * 
 * @author pinzger
 */
public class VariableDeclarationFragmentHandler extends AbstractASTNodeHandler {
    /**
     * The Logger.
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(VariableDeclarationFragmentHandler.class.getName());

    /**
     * The type of the local variable.
     */
    private FamixClass fDataType;

    /**
     * The constructor
     * 
     * @param crawler Instance of ASTCrawler to obtain current type and method information.
     */
    public VariableDeclarationFragmentHandler(ASTCrawler crawler) {
        super(crawler);
        fDataType = null;
    }

    /**
     * Helper method for returning the variable declaration AST node.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return The variable declaration AST node.
     */
    @Override
    public VariableDeclaration getASTNode() {
        return (VariableDeclaration) super.getASTNode();
    }

    /**
     * Handle local variable declarations.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#visit(org.eclipse.jdt.core.dom.ASTNode)
     * 
     * @param fragment The AST node representing the local variable declaration.
     * @return true, if contained nodes should be visited.
     */
    @Override
    public boolean visit(ASTNode fragment) {
        boolean visitChildren = true;

        setASTNode(fragment);

        try {
            initDataType();

            createLocalVariableFromFragment();

            // Process initialization to this local variable
            //            Expression lInit = getASTNode().getInitializer();
            //            if (lInit != null) {
            //                lInit.accept(getCrawler());
            //            }
        } catch (NullPointerException e) {
            sLogger.error("Error processing variable declaration fragment in method " 
                    + (getCurrMethod() != null ? getCurrMethod().getUniqueName() : "<no method>") + "\n" 
                    + getASTNode().toString() 
                    + " - skipping variable declaration fragment");
            visitChildren = false;
        }

        // everything done for the fragment, proceed with the next AST node.
        return visitChildren;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void endVisit() {
        // currently not used
    }

    /**
     * Initializes the declared data type of a local variable. Currently, we handle:
     * <ul>
     * <li> Variable declaration statements (declaring multiple local variables of the same types 
     * <li> Single variable declarations as declared in catch clauses
     * <li> Variable declarations with initializer expression, e.g., <code>int local=1</code>
     * </ul>
     */
    protected void initDataType() {
        Type declType = null;
        if (getASTNode() instanceof SingleVariableDeclaration) {
            SingleVariableDeclaration singleVarDecl = (SingleVariableDeclaration) getASTNode();
            declType = singleVarDecl.getType();
        } else {
            ASTNode parentNode = getASTNode().getParent();
            if (parentNode instanceof VariableDeclarationExpression) {
                VariableDeclarationExpression varDeclExpression = (VariableDeclarationExpression) parentNode;
                declType = varDeclExpression.getType();
            } else if (parentNode instanceof VariableDeclarationStatement) {
                VariableDeclarationStatement varDeclStatement = (VariableDeclarationStatement) parentNode;
                declType = varDeclStatement.getType();
            } else {
                sLogger.warn("Parent of this variable declaration statement currently not supported " + getASTNode());
            }
        }

        ITypeBinding lTypeBinding = null;
        if (declType != null) {
            lTypeBinding = declType.resolveBinding();
        }
        FamixClass lDataType = getClass(lTypeBinding, declType, false);
        lDataType = (FamixClass) getModel().addElement(lDataType);
        setDataType(lDataType);
    }

    /**
     * Returns the data type.
     * 
     * @return The data type.
     */
    public FamixClass getDataType() {
        return fDataType;
    }

    /**
     * Sets the data type.
     * 
     * @param dataType The data type.
     */
    protected void setDataType(FamixClass dataType) {
        fDataType = dataType;
    }

    private void createLocalVariableFromFragment() {
        String lLocalVariableID = null;
        String lSimpleName = getASTNode().getName().getFullyQualifiedName();
        if (lSimpleName != null) {
            lLocalVariableID = getCurrMethod().getUniqueName() + AbstractFamixEntity.NAME_DELIMITER + lSimpleName;

            FamixLocalVariable lLocalVariable = getFactory().createLocalVariable(lLocalVariableID, null);
            lLocalVariable.setSourceAnchor(getSourceAnchor());

            if (getModel().contains(lLocalVariable)) {
                sLogger.warn("Duplicated entry " + lLocalVariable.getUniqueName());
            }
            lLocalVariable = (FamixLocalVariable) getModel().addElement(lLocalVariable);
            int modifier =
                (lLocalVariable != null ? lLocalVariable.getModifiers() : AbstractFamixEntity.UNKOWN_OR_NO_MODIFIERS);
            lLocalVariable.setModifiers(modifier);
            lLocalVariable.setParent(getCurrMethod());
            getCurrMethod().getLocalVariables().add(lLocalVariable);
            if (getDataType() != null) {
                lLocalVariable.setDeclaredClass(getDataType());
            }

            // remember the scope of the local variabel for later variable resolution
            getCrawler().getLocalVariableScope().put(lLocalVariable, getCrawler().getCurrStatementBlock());
        } else {
            sLogger.warn("Could not get name of local variable declaration " + getASTNode());
        }
    }
}
