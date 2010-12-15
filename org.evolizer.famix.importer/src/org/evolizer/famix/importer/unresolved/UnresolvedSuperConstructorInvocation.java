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

import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.evolizer.famix.importer.nodehandler.AbstractInvocationHandler;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixMethod;

/**
 * Handles unresolved super constructor statements, e.g., <code>super(arg1)</code>.
 * 
 * @author pinzger
 */
@Entity
public class UnresolvedSuperConstructorInvocation extends UnresolvedMethodInvocation {

    /**
     * The constructor.
     * 
     * @param caller FamixMethod containing the unresolved call.
     * @param invocationHandler Handler to be used to resolve the call later.
     */
    public UnresolvedSuperConstructorInvocation(FamixMethod caller, AbstractInvocationHandler invocationHandler) {
        super(caller, invocationHandler);
    }

    /**
     * Returns the constructor prefix.
     * 
     * @see org.evolizer.famix.importer.unresolved.UnresolvedMethodInvocation#getASTNodeName()
     * 
     * @return The name of the super constructor method, i.e., <code>init</code>.
     */
    @Override
    @Transient
    protected String getASTNodeName() {
        return AbstractFamixEntity.CONSTRUCTOR_PREFIX;
    }

    /**
     * Resolve the super-class, receiving the call.  
     * 
     * @see org.evolizer.famix.importer.unresolved.UnresolvedMethodInvocation#resolveObjectIdentifier()
     */
    @Override
    public void resolveObjectIdentifier() {
        // if there is only one superclass, we could get the type
        if (!isObjectIDResolved()) {
            String classID = resolveTypeBinding(((SuperConstructorInvocation) getASTNode()).getExpression());
            if ((classID != null) && !classID.equals("")) {
                setResolvedID(classID);
            }
        }
    }
}
