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

import org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler;
import org.evolizer.famix.importer.nodehandler.AbstractInvocationHandler;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixMethod;

/**
 * Handles unresolved anonymous class instance creation statements. The computation of the call receiver object/class
 * is: declaring class of caller + anonymous class counter.
 * 
 * @author pinzger
 */
@Entity
public class UnresolvedAnonymousClassInstanceCreationInvocation extends UnresolvedMethodInvocation {

    /**
     * Counter for anonymous classes
     */
    @Transient
    private int fAnonymClassCounter;

    /**
     * The constructor
     * 
     * @param caller
     *            The caller method.
     * @param invocationHandler
     *            Object, handling the invocation node. This contains the mandatory AST node information.
     * @param anonymClassCounter
     *            Number of the anonymous class.
     */
    public UnresolvedAnonymousClassInstanceCreationInvocation(
            FamixMethod caller,
            AbstractInvocationHandler invocationHandler,
            int anonymClassCounter) {
        super(caller, invocationHandler);
        fAnonymClassCounter = anonymClassCounter;
    }

    /**
     * The name of the constructor <code>init</code>. 
     * 
     * @see org.evolizer.famix.importer.unresolved.UnresolvedMethodInvocation#getASTNodeName()
     * 
     * @return <code>init</code>.
     */
    @Override
    @Transient
    protected String getASTNodeName() {
        return AbstractFamixEntity.CONSTRUCTOR_PREFIX;
    }

    /**
     * Resolve the type of the anonymous class.
     * 
     * @see org.evolizer.famix.importer.unresolved.UnresolvedMethodInvocation#resolveObjectIdentifier()
     */
    @Override
    public void resolveObjectIdentifier() {
        if (!isObjectIDResolved()) {
            String classID = getInvocationHandler().convertAnonymousType(
                    (FamixClass) getCaller().getParent(), fAnonymClassCounter)
                + AbstractASTNodeHandler.BINDING_ERROR_SIGN;
            if ((classID != null) && !classID.equals("")) {
                setResolvedID(classID);
            }
        }
    }
}
