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

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.unresolved.UnresolvedMethodInvocation;
import org.evolizer.famix.model.entities.FamixMethod;

/**
 * Handles method invocation statements, e.g., <code>object.methodID(arg1);</code>.
 * 
 * @author pinzger
 * 
 */
public class MethodInvocationHandler extends AbstractInvocationHandler {

    /**
     * The constructor.
     * 
     * @param crawler Instance of ASTCrawler to obtain current type and method information.
     */
    public MethodInvocationHandler(ASTCrawler crawler) {
        super(crawler);
    }

    /**
     * Helper method for returning the AST node.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return The method invocation AST node.
     */
    @Override
    public MethodInvocation getASTNode() {
        return (MethodInvocation) super.getASTNode();
    }

    /**
     * Initializes the method binding.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractInvocationHandler#initMethodBinding()
     */
    @Override
    public void initMethodBinding() {
        IMethodBinding lMethodBinding = getASTNode().resolveMethodBinding();
        if (lMethodBinding != null) {
            setMethodBinding(lMethodBinding.getMethodDeclaration());
        }
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
     * Create FAMIX method from method invocation.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractInvocationHandler#createMethodFromInvocation()
     * 
     * @return The called FAMIX method, otherwise null.
     */
    @Override
    public FamixMethod createMethodFromInvocation() {
        FamixMethod lMethod = null;
        int successfullResolvingCounter = countResolvedArguments();

        if ((getMethodBinding() != null) && (successfullResolvingCounter == getArguments().size())) {
            lMethod = createMethod();
        } else {
            lMethod = rememberUnresolvedInvocation(new UnresolvedMethodInvocation(getCurrMethod(), this));
        }
        return lMethod;
    }
}
