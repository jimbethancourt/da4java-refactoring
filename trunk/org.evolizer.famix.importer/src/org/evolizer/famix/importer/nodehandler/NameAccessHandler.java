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

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.model.entities.AbstractFamixEntity;

/**
 * Handle read/write accesses via QualifiedName, e.g., <code>package.class.field</code> and SimpleName, e.g.,
 * <code>field</code>.
 * 
 * @author pinzger
 */
public class NameAccessHandler extends AbstractAccessHandler {

    /**
     * The constructor.
     * 
     * @param crawler Instance of ASTCrawler to obtain current type and method information.
     */
    public NameAccessHandler(ASTCrawler crawler) {
        super(crawler);
    }

    /**
     * Helper method for returning the AST node.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return The AST node.
     */
    @Override
    public Name getASTNode() {
        return (Name) super.getASTNode();
    }

    /**
     * Compute the FAMIX conform unique name of the accessed field.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractAccessHandler#convertFieldID()
     * 
     * @return The unique Name.
     */
    @Override
    protected String convertFieldID() {
        String lFieldID = null;

        IBinding binding = getASTNode().resolveBinding();
        if (binding != null) {
            if (binding.getKind() == IBinding.VARIABLE) {
                if (((IVariableBinding) binding).isField()) {
                    IVariableBinding lVariableBinding = (IVariableBinding) binding;
                    if (lVariableBinding.getDeclaringClass() == null) {
                        // This is most likely an access to the length
                        // field of an array.
                        return null;
                    }

                    org.evolizer.famix.model.entities.FamixClass lDeclClass = getClass(lVariableBinding.getDeclaringClass(), null, false);
                    lFieldID = lDeclClass.getUniqueName() + AbstractFamixEntity.NAME_DELIMITER + lVariableBinding.getName();
                }
            }
        }

        return lFieldID;
    }
}
