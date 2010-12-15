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

import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.model.entities.AbstractFamixEntity;

/**
 * Handles read/write accesses via SuperFieldAccess, e.g., <code>super.field</code>.
 * 
 * @author pinzger
 */
public class SuperFieldAccessHandler extends AbstractAccessHandler {

    /**
     * The Contructor.
     * 
     * @param crawler Instance of ASTCrawler to obtain current type and method information.
     */
    public SuperFieldAccessHandler(ASTCrawler crawler) {
        super(crawler);
    }

    /**
     * Helper method for returning the AST node.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return The super field access AST node.
     */
    @Override
    public SuperFieldAccess getASTNode() {
        return (SuperFieldAccess) super.getASTNode();
    }

    /**
     * Resolve the binding of the field of the super-class (i.e., the unique name of the super class plus the name of the field).
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractAccessHandler#createAttributeFromAccess()
     * 
     * @return The FAMIX conform unique name.
     */
    @Override
    protected String convertFieldID() {
        String lFieldID = null;
        IVariableBinding lBinding = (IVariableBinding) getASTNode().getName().resolveBinding();
        if (lBinding != null) {
            if (lBinding.getDeclaringClass() != null) {
                lFieldID =
                        lBinding.getDeclaringClass().getBinaryName() + AbstractFamixEntity.NAME_DELIMITER + lBinding.getName();
            } else {
                lFieldID = lBinding.getName();
            }
        } else {
            lFieldID =
                    AbstractASTNodeHandler.UNDEFINED_BINDING + AbstractFamixEntity.NAME_DELIMITER
                            + getASTNode().getName().getFullyQualifiedName();
        }

        return lFieldID;
    }
}
