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

import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.unresolved.UnresolvedSuperConstructorInvocation;
import org.evolizer.famix.model.entities.FamixMethod;

/**
 * Handles super constructor invocation statements, e.g., <code>super(arg1)</code>. If the super type is not defined it
 * is not possible to determine it via other information contained in the AST.
 * 
 * @author pinzger
 */
public class SuperConstructorInvocationHandler extends AbstractInvocationHandler {

    /**
     * The constructor.
     * 
     * @param crawler Instance of ASTCrawler to obtain current type and method information.
     */
    public SuperConstructorInvocationHandler(ASTCrawler crawler) {
        super(crawler);
    }

    /**
     * Helper method for returning the AST node.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return the super constructor invocation AST node.
     */
    @Override
    public SuperConstructorInvocation getASTNode() {
        return (SuperConstructorInvocation) super.getASTNode();
    }

    /**
     * Initializes the method binding the the constructor binding.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractInvocationHandler#initMethodBinding()
     */
    @Override
    public void initMethodBinding() {
        setMethodBinding(getASTNode().resolveConstructorBinding());
    }

    /**
     * Create FAMIX method from super constructor call.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractInvocationHandler#createMethodFromInvocation()
     * 
     * @return The called super constructor, otherwise null.
     */
    @Override
    public FamixMethod createMethodFromInvocation() {
        FamixMethod lSuperConstructor = null;
        int successfullResolvingCounter = countResolvedArguments();

        if ((getMethodBinding() != null) && (successfullResolvingCounter == getArguments().size())) {
            lSuperConstructor = createMethod();
        } else {
            lSuperConstructor =
                    rememberUnresolvedInvocation(new UnresolvedSuperConstructorInvocation(getCurrMethod(), this));
        }

        // just to make sure that an undefined super constructor has a source anchor - do we really need this?
        // if (lSuperConstructor.getSourceAnchor() == null) {
        // lSuperConstructor.setSourceAnchor(getSourceAnchor(superConstructorInvocation));
        // }
        return lSuperConstructor;
    }

    /** 
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void initArguments() {
        setArguments(getASTNode().arguments());
    }

}
