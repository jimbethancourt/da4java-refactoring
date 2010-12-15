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

import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.unresolved.UnresolvedSuperMethodInvocation;
import org.evolizer.famix.model.entities.FamixMethod;

/**
 * Handler for super method invocation statements, e.g., <code>super.methoID(...);</code>.
 * 
 * @author pinzger
 * 
 */
public class SuperMethodInvocationHandler extends AbstractInvocationHandler {

    /**
     * The constructor.
     * 
     * @param crawler Instance of ASTCrawler to obtain current type and method information.
     */
    public SuperMethodInvocationHandler(ASTCrawler crawler) {
        super(crawler);
    }

    /**
     * Helper method for returning the AST node.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return The super method invocation AST node.
     */
    @Override
    public SuperMethodInvocation getASTNode() {
        return (SuperMethodInvocation) super.getASTNode();
    }

    /**
     * Initializes the method binding.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractInvocationHandler#initMethodBinding()
     */
    @Override
    public void initMethodBinding() {
        setMethodBinding(getASTNode().resolveMethodBinding());
    }

    /** 
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void initArguments() {
        setArguments(getASTNode().arguments());
    }

    /**
     * Create FAMIX method from super method invocation. 
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractInvocationHandler#createMethodFromInvocation()
     * 
     * @return The called FAMIX method, otherwise null.
     */
    @Override
    public FamixMethod createMethodFromInvocation() {
        FamixMethod lSuperMethod = null;
        int successfullResolvingCounter = countResolvedArguments();

        if ((getMethodBinding() != null) && (successfullResolvingCounter == getArguments().size())) {
            lSuperMethod = createMethod();
        } else {
            lSuperMethod = rememberUnresolvedInvocation(new UnresolvedSuperMethodInvocation(getCurrMethod(), this));
        }
        return lSuperMethod;
    }
}
