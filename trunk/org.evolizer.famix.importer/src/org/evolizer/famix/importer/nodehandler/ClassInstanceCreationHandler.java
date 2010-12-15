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

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.importer.unresolved.UnresolvedAnonymousClassInstanceCreationInvocation;
import org.evolizer.famix.importer.unresolved.UnresolvedClassInstanceCreationInvocation;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixMethod;

/**
 * Handles class and anonymous class instance creation statements, e.g., <code>new Type(arg)</code>.
 * 
 * @author pinzger
 */
public class ClassInstanceCreationHandler extends AbstractInvocationHandler {
    /**
     * The Logger.
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(ClassInstanceCreationHandler.class.getName());

    /**
     * The constructor.
     * 
     * @param crawler Instance of ASTCrawler to obtain current type and method information.
     */
    public ClassInstanceCreationHandler(ASTCrawler crawler) {
        super(crawler);
    }

    /**
     * Helper method for returning the AST node.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return The class instance creation AST node.
     */
    @Override
    public ClassInstanceCreation getASTNode() {
        return (ClassInstanceCreation) super.getASTNode();
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
     * Create FAMIX method from class and anonymous class instance creation statements.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractInvocationHandler#createMethodFromInvocation()
     * 
     * @return The called FAMIX method, otherwise null.
     */
    @Override
    public FamixMethod createMethodFromInvocation() {
        FamixMethod lConstructor = null;
        int successfullResolvingCounter = countResolvedArguments();

        // increment the counter of anonymous classes of the current type
        if (getASTNode().getAnonymousClassDeclaration() != null) {
            getAnonymClassCounter().put(getCurrType(), getAnonymClassCounter().get(getCurrType()) + 1);
        }

        if ((getMethodBinding() != null) && (getMethodBinding().getDeclaringClass() != null) && (successfullResolvingCounter == getArguments().size())) {
            lConstructor = createMethod();
        } else {
            // also consider anonymous class instance creation
            if (getASTNode().getAnonymousClassDeclaration() != null) {
                lConstructor =
                        rememberUnresolvedInvocation(new UnresolvedAnonymousClassInstanceCreationInvocation(
                                getCurrMethod(),
                                this,
                                getAnonymClassCounter().get(getCurrType())));
            } else {
                lConstructor =
                        rememberUnresolvedInvocation(new UnresolvedClassInstanceCreationInvocation(
                                getCurrMethod(),
                                this));
            }
        }

        // add the corresponding constructor to the anonymous class (only for anonymous classes)
        if ((getASTNode().getAnonymousClassDeclaration() != null) && (lConstructor != null)) {
            FamixClass lDeclaringClass = getClass(getASTNode().getAnonymousClassDeclaration().resolveBinding(), null, true);
            lDeclaringClass = (FamixClass) getModel().addElement(lDeclaringClass);
            if (lDeclaringClass.getParent() != null) {
                sLogger.warn("Warn - anoym class " + lDeclaringClass.getUniqueName() + " already has a parent: "
                        + lDeclaringClass.getParent().getUniqueName());
            }

            lDeclaringClass.getMethods().add(lConstructor);
            lConstructor.setParent(lDeclaringClass);
        }

        return lConstructor;
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
