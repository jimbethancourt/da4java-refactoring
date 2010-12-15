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

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.model.entities.FamixClass;

/**
 * Handle single variable declarations, e.g., occurring in catch clauses.
 * 
 * @author pinzger
 */
public class SingleVariableDeclarationHandler extends VariableDeclarationFragmentHandler {

    /**
     * The constructor.
     * 
     * @param crawler Instance of ASTCrawler to obtain current type and method information.
     */
    public SingleVariableDeclarationHandler(ASTCrawler crawler) {
        super(crawler);
    }

    /**
     * Helper method for returning the AST node.
     * 
     * @see org.evolizer.famix.importer.nodehandler.VariableDeclarationFragmentHandler#getASTNode()
     * 
     * @return The single variable declaration AST node.
     */
    @Override
    public SingleVariableDeclaration getASTNode() {
        return (SingleVariableDeclaration) super.getASTNode();
    }

    /**
     * Initializes the declared data type. 
     * 
     * @see org.evolizer.famix.importer.nodehandler.VariableDeclarationFragmentHandler#initDataType()
     */
    @Override
    protected void initDataType() {
        if (getDataType() == null) {
            IVariableBinding lVariableBinding = getASTNode().resolveBinding();
            ITypeBinding lTypeBinding = null;
            if (lVariableBinding != null) {
                lTypeBinding = lVariableBinding.getType();
            }
            FamixClass lDataType = getClass(lTypeBinding, getASTNode().getType(), false);
            lDataType = (FamixClass) getModel().addElement(lDataType);
            setDataType(lDataType);
        }
    }
}
