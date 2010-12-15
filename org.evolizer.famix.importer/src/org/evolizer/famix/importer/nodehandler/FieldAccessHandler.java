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

import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.model.entities.AbstractFamixEntity;

/**
 * Handle read/write accesses via FieldAccess, e.g., <code>this.field</code>, <code>foo().bar</code>, or also
 * <code>foo.bar</code>).
 * 
 * @author pinzger
 */
public class FieldAccessHandler extends AbstractAccessHandler {

    /**
     * The constructor
     * 
     * @param crawler
     *            Instance of ASTCrawler to obtain current type and method information.
     */
    public FieldAccessHandler(ASTCrawler crawler) {
        super(crawler);
    }

    /**
     * Helper method for returning the AST node.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return The field access AST node.
     */
    @Override
    public FieldAccess getASTNode() {
        return (FieldAccess) super.getASTNode();
    }

    /**
     * Compute the FAMIX conform unique name of the accessed field.
     * 
     * @return The unique name.
     */
    @Override
    protected String convertFieldID() {
        String lFieldID = null;
        IVariableBinding lBinding = (IVariableBinding) getASTNode().getName().resolveBinding();
        if (lBinding != null) {
            if (lBinding.getDeclaringClass() != null) {
                lFieldID =
                        lBinding.getDeclaringClass().getBinaryName() + AbstractFamixEntity.NAME_DELIMITER
                                + lBinding.getName();
            } else {
                lFieldID = lBinding.getName();
            }
        } else {
            // handling of unresolved variable bindings is not supported, yet
            lFieldID =
                    AbstractASTNodeHandler.UNDEFINED_BINDING + AbstractFamixEntity.NAME_DELIMITER
                            + getASTNode().getName().getFullyQualifiedName();
        }

        return lFieldID;
    }
}
