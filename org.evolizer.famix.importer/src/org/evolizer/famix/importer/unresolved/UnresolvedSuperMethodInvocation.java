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
package org.evolizer.famix.importer.unresolved;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.evolizer.famix.importer.nodehandler.AbstractInvocationHandler;
import org.evolizer.famix.model.entities.FamixMethod;

/**
 * Handles unresolved super method calls, e.g., <code>super.method(arg1)</code>.
 * 
 * @author pinzger
 */
@Entity
public class UnresolvedSuperMethodInvocation extends UnresolvedMethodInvocation {

    /**
     * The constructor
     * 
     * @param caller
     *            The FAMIX method containing the unresolved call
     * @param invocationHandler
     *            The AST node handler representing the unresolved call
     */
    public UnresolvedSuperMethodInvocation(FamixMethod caller, AbstractInvocationHandler invocationHandler) {
        super(caller, invocationHandler);
    }

    /**
     * Returns the name of the super method. 
     * 
     * @see org.evolizer.famix.importer.unresolved.UnresolvedMethodInvocation#getASTNodeName()
     * 
     * @return the name of the super method invocation.
     */
    @Override
    @Transient
    protected String getASTNodeName() {
        return ((SuperMethodInvocation) getASTNode()).getName().toString();
    }

    /**
     * Resolves the super-data type receiving the method call.
     * 
     * @see UnresolvedMethodInvocation#resolveObjectIdentifier()
     */
    @Override
    public void resolveObjectIdentifier() {
        // if there is only one superclass, we could get the type
        if (!isObjectIDResolved()) {
            String classID = resolveTypeBinding(((SuperMethodInvocation) getASTNode()).getQualifier());
            if ((classID != null) && !classID.equals("")) {
                setResolvedID(classID);
            }
        }
    }
}
