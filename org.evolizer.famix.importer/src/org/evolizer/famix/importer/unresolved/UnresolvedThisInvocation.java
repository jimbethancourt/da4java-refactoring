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

import org.evolizer.famix.importer.nodehandler.AbstractInvocationHandler;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixMethod;

/**
 * Handles unresolved this calls, e.g., <code>this(arg1)</code>.
 * 
 * @author pinzger
 */
@Entity
public class UnresolvedThisInvocation extends UnresolvedMethodInvocation {

    /**
     * The constructor.
     * 
     * @param caller
     *            The FAMIX method containing the unresolved call
     * @param invocationHandler
     *            The AST node handler representing the unresolved call
     */
    public UnresolvedThisInvocation(FamixMethod caller, AbstractInvocationHandler invocationHandler) {
        super(caller, invocationHandler);
    }

    /**
     * Returns the constructor name.
     * 
     * @see org.evolizer.famix.importer.unresolved.UnresolvedMethodInvocation#getASTNodeName()
     * 
     * @return The name of the constructor, i.e., <code>init</code>.
     */
    @Override
    @Transient
    protected String getASTNodeName() {
        return AbstractFamixEntity.CONSTRUCTOR_PREFIX;
    }

    /**
     * Resolve the data type containing the call.
     * 
     * @see UnresolvedMethodInvocation#resolveObjectIdentifier()
     */
    @Override
    public void resolveObjectIdentifier() {
        if (!isObjectIDResolved()) {
            if ((getCaller().getParent() != null) && (getCaller().getParent().getUniqueName() != null)
                    && !getCaller().getParent().getUniqueName().equals("")) {
                setResolvedID(getCaller().getParent().getUniqueName());
            }
        }
    }
}
