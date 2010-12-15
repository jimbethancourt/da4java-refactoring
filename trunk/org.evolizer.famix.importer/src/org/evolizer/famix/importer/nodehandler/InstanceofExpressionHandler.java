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
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixClass;

/**
 * Handles Java instance of expressions, e.g., <code>object instanceof Type</code>.
 * 
 * @author pinzger
 */
public class InstanceofExpressionHandler extends AbstractASTNodeHandler {
    /**
     * The Logger.
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(InstanceofExpressionHandler.class.getName());

    /**
     * The constructor.
     * 
     * @param crawler Instance of ASTCrawler to obtain current type and method information.
     */
    public InstanceofExpressionHandler(ASTCrawler crawler) {
        super(crawler);
    }

    /**
     * Helper method for return the AST node.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return The instanceof expression AST node. 
     */
    @Override
    public InstanceofExpression getASTNode() {
        return (InstanceofExpression) super.getASTNode();
    }

    /**
     * Handle instanceof expressions.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#visit(org.eclipse.jdt.core.dom.ASTNode)
     * 
     * @param instanceofExpression  The AST node representing the intanceof expression.
     * @return true, if contained nodes should be visited.
     */
    @Override
    public boolean visit(ASTNode instanceofExpression) {
        boolean visitChildren = true;
        setASTNode(instanceofExpression);

        try {
            ITypeBinding lBinding = getASTNode().getRightOperand().resolveBinding();
            FamixClass lClass = getClass(lBinding, getASTNode().getRightOperand(), false);
            lClass = (FamixClass) getModel().addElement(lClass);
            FamixAssociation instanceOf = getFactory().createCheckInstanceOf(getCurrMethod(), lClass);
            instanceOf.setSourceAnchor(getSourceAnchor());
            getModel().addRelation(instanceOf);
        } catch (NullPointerException e) {
            sLogger.error("Error processing instanceof expression in method " 
                    + (getCurrMethod() != null ? getCurrMethod().getUniqueName() : "<no method>") + "\n" 
                    + getASTNode().toString() 
                    + " - skipping instanceof expression");
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
