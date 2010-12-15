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

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.evolizer.famix.importer.nodehandler.AbstractInvocationHandler;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixMethod;

/**
 * Handles unresolved class instance creation statements. The computation of the call receiver object/class is: type of
 * created instance.
 * 
 * @author pinzger
 */
@Entity
public class UnresolvedClassInstanceCreationInvocation extends UnresolvedMethodInvocation {

    /**
     * The constructor.
     * 
     * @param caller
     *            The method containing the unresolved call.
     * @param invocationHandler
     *            Handler to be used to resolve the call later on.
     */
    public UnresolvedClassInstanceCreationInvocation(FamixMethod caller, AbstractInvocationHandler invocationHandler) {
        super(caller, invocationHandler);
    }

    /**
     * Returns the constructor prefix.
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
     * {@inheritDoc}
     */
    @Override
    public void resolveObjectIdentifier() {
        if (!isObjectIDResolved()) {
            ITypeBinding lTBinding = ((ClassInstanceCreation) getASTNode()).resolveTypeBinding();
            String classID = "";
            if (lTBinding != null) {
                classID = getInvocationHandler().convert(lTBinding);
            } else {
                classID = getInvocationHandler().convert(((ClassInstanceCreation) getASTNode()).getType());
            }
            if ((classID != null) && !classID.equals("")) {
                setResolvedID(classID);
            }
        }
    }
}
