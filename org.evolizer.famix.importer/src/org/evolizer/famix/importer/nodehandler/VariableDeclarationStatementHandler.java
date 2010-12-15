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
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.model.entities.FamixClass;

/**
 * Handles multiple local variable declarations, e.g., <code>int l1, l2</code>.
 * 
 * @author pinzger
 */
public class VariableDeclarationStatementHandler extends AbstractASTNodeHandler {
    /**
     * The Logger.
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(VariableDeclarationStatementHandler.class.getName());

    /**
     * The list of declared local variables.
     */
    private List<VariableDeclarationFragment> fFragments;

    /**
     * The data type of declared local variables.
     */
    private FamixClass fDataType;

    /**
     * The constructor.
     * 
     * @param crawler Instance of ASTCrawler to obtain current type and method information.
     */
    public VariableDeclarationStatementHandler(ASTCrawler crawler) {
        super(crawler);
        fDataType = null;
    }

    /**
     * Helper method for return the variable declaration statement AST node.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return the variable declaration statement AST node.
     */
    @Override
    public VariableDeclarationStatement getASTNode() {
        return (VariableDeclarationStatement) super.getASTNode();
    }

    /**
     * Handle statements declaring multiple variables. Creates <code>VariableDeclarationFragmentHandler</code> for each 
     * declared variable. 
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#visit(org.eclipse.jdt.core.dom.ASTNode)
     * 
     * @param variableDeclarationStatement the AST node representing the variable declaration statement.
     * @return true, if the contained nodes should be visited.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean visit(ASTNode variableDeclarationStatement) {
        boolean visitChildren = true;
        setASTNode(variableDeclarationStatement);

        try {
            setFragments(getASTNode().fragments());
            initDataType();

            for (VariableDeclarationFragment fragment : getFragments()) {
                VariableDeclarationFragmentHandler lVariableDeclarationHandler =
                        new VariableDeclarationFragmentHandler(getCrawler());
                lVariableDeclarationHandler.setDataType(getDataType());
                lVariableDeclarationHandler.visit(fragment);
            }
            visitChildren = false;
        } catch (NullPointerException e) {
            sLogger.error("Error processing variable declaration statement in method " 
                    + (getCurrMethod() != null ? getCurrMethod().getUniqueName() : "<no method>") + "\n" 
                    + getASTNode().toString() 
                    + " - skipping variable declaration statement");
            visitChildren = false;
        }

        return visitChildren;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void endVisit() {
    // currently not used
    }

    private void initDataType() {
        ITypeBinding lTypeBinding = getASTNode().getType().resolveBinding();
        FamixClass lDataType = getClass(lTypeBinding, getASTNode().getType(), false);
        lDataType = (FamixClass) getModel().addElement(lDataType);
        setDataType(lDataType);
    }

    /**
     * Sets the list of local variables declared by that statement.
     * 
     * @param fragments The list of variable declaration fragments.
     */
    protected void setFragments(List<VariableDeclarationFragment> fragments) {
        fFragments = fragments;
    }

    /**
     * The list of variable declaration fragments.
     * 
     * @return The list of variable declaration fragments.
     */
    public List<VariableDeclarationFragment> getFragments() {
        return fFragments;
    }

    /**
     * Sets the data type of declared local variables.
     * 
     * @param dataType The declared data type
     */
    protected void setDataType(FamixClass dataType) {
        fDataType = dataType;
    }

    /**
     * Returns the data type.
     * 
     * @return The declared data type
     */
    public FamixClass getDataType() {
        return fDataType;
    }
}
