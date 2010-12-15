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

import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.unresolved.UnresolvedThisInvocation;
import org.evolizer.famix.model.entities.FamixMethod;

/**
 * Handles constructor invocation statements <code>this(arg)</code>. The class containing such a statement is always
 * defined so the binding must be resolvable. The arguments, however, may not be resolvable which causes jdt to fail. In
 * this case the statement is added to the list of unresolved method invocations.
 * 
 * @author pinzger
 * 
 */
public class ConstructorInvocationHandler extends AbstractInvocationHandler {

    /**
     * The constructor.
     * 
     * @param crawler Instance of ASTCrawler to obtaind current type and method information.
     */
    public ConstructorInvocationHandler(ASTCrawler crawler) {
        super(crawler);
    }

    /**
     * Helper method for returning the AST node.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return The constructor invocation AST node.
     */
    @Override
    public ConstructorInvocation getASTNode() {
        return (ConstructorInvocation) super.getASTNode();
    }

    /**
     * Initializes the method binding with the constructor binding.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractInvocationHandler#initMethodBinding()
     */
    @Override
    public void initMethodBinding() {
        setMethodBinding(getASTNode().resolveConstructorBinding());
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void initArguments() {
        setArguments(getASTNode().arguments());
    }

    /**
     * Creates FAMIX method from the constructor statement.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractInvocationHandler#createMethodFromInvocation()
     * 
     * @return The called FAMIX method (constructor), otherwise null.
     */
    @Override
    public FamixMethod createMethodFromInvocation() {
        FamixMethod lConstructor = null;
        int successfullResolvingCounter = countResolvedArguments();
        if ((getMethodBinding() != null) && (successfullResolvingCounter == getArguments().size())) {
            lConstructor = createMethod();
        } else {
            lConstructor = rememberUnresolvedInvocation(new UnresolvedThisInvocation(getCurrMethod(), this));
        }
        return lConstructor;
    }
}
