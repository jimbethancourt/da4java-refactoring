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
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixClass;

/**
 * Handles Java class cast statements, e.g., <code>(NewType) Type</code>.
 * 
 * @author pinzger
 */
public class CastExpressionHandler extends AbstractASTNodeHandler {
    /**
     * The Logger.
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(CastExpressionHandler.class.getName());

    /**
     * The constructor.
     * 
     * @param crawler
     *            Instance of ASTCrawler to obtain current type and method information.
     */
    public CastExpressionHandler(ASTCrawler crawler) {
        super(crawler);
    }

    /**
     * Helper method for returning the AST node.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return the cast expression AST node.
     */
    @Override
    public CastExpression getASTNode() {
        return (CastExpression) super.getASTNode();
    }

    /**
     * Handle cast expressions.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#visit(org.eclipse.jdt.core.dom.ASTNode)
     * 
     * @param castExpression The AST node representing the cast expression
     * @return true, if contained nodes should be visited.
     */
    @Override
    public boolean visit(ASTNode castExpression) {
        boolean visitChildren = true;
        
        setASTNode(castExpression);
        ITypeBinding lBinding = getASTNode().resolveTypeBinding();

        try {
            FamixClass lClass = getClass(lBinding, getASTNode().getType(), false);
            lClass = (FamixClass) getModel().addElement(lClass);
            FamixAssociation castTo = getFactory().createCastTo(getCurrMethod(), lClass);
            castTo.setSourceAnchor(getSourceAnchor());
            getModel().addRelation(castTo);
        } catch (NullPointerException e) {
            sLogger.error("Error processing cast expression in method " 
                    + (getCurrMethod() != null ? getCurrMethod().getUniqueName() : "<no method>") + "\n" 
                    + getASTNode().toString() 
                    + " - skipping expression");
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
}
