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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.importer.ASTCrawler.StatementBlock;
import org.evolizer.famix.importer.unresolved.UnresolvedMethodInvocation;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.AbstractFamixVariable;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixLocalVariable;
import org.evolizer.famix.model.entities.FamixMethod;

/**
 * Abstract class for handling method invocation statements. jdt is used to resolve statements (i.e., obtaining the
 * fully qualified name of the called method). jdt may fail in case of unknown return or argument types. In this case
 * the method invocation statement is added to the list of unresolved method invocations which can be processed later
 * on.
 * 
 * The handling of the different method invocation statements is implemented in the various subclasses.
 * 
 * @author pinzger
 * 
 */
public abstract class AbstractInvocationHandler extends AbstractASTNodeHandler {
    /**
     * The Logger.
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(AbstractInvocationHandler.class.getName());

    /**
     * The method binding.
     */
    private IMethodBinding fMethodBinding;
    /**
     * The list of arguments.
     */
    private List<Expression> fArguments = new ArrayList<Expression>();

    /**
     * The constructor.
     * 
     * @param crawler Instance of ASTcrawler to obtain current type and method information.
     */
    public AbstractInvocationHandler(ASTCrawler crawler) {
        super(crawler);
        fMethodBinding = null;
    }

    /** 
     * Template method for handling method invocations.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#visit(org.eclipse.jdt.core.dom.ASTNode)
     * 
     * @param node The AST node representing the method invocation statement.
     * @return true, if contained nodes should be visited.
     */
    @Override
    public boolean visit(ASTNode node) {
        boolean visitChildren = true;
        setASTNode(node);
        
        try {
            initMethodBinding();
            initArguments();
    
            FamixMethod caller = getCurrMethod();
            FamixMethod callee = createMethodFromInvocation();
    
            if ((caller != null) && (callee != null)) {
                callee = (FamixMethod) getModel().addElement(callee);
                FamixAssociation invocation = getFactory().createInvocation(caller, callee);
                invocation.setSourceAnchor(getSourceAnchor());
                invocation.setStatement(getASTNode().toString());
                getModel().addRelation(invocation);
            }
        } catch (NullPointerException e) {
            sLogger.error("Error processing invocation in method " 
                    + (getCurrMethod() != null ? getCurrMethod().getUniqueName() : "<no method>") + "\n" 
                    + getASTNode().toString() 
                    + " - skipping incovation");
            visitChildren = false;
        }

        return visitChildren;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void endVisit() {
    // currently not needed
    }

    /**
     * Initializes the method binding.
     */
    public abstract void initMethodBinding();

    /**
     * Initializes the list of arguments of the call.
     */
    public abstract void initArguments();

    /**
     * Create the method that is invoked in this statement. FamixInvocation statements that can not be resolved by jdt are
     * added to the list of unresolved method invocations which can be processed later on. Different strategies are
     * needed to resolve these invocations which are instantiated in subclasses.
     * 
     * @return FamixMethod.
     */
    public abstract FamixMethod createMethodFromInvocation();

    /**
     * Adds the unresolved method invocation statement and the handler for resolving it to the list of unresolved method
     * invocations. Remember also the variables in scope for later resolution.
     * 
     * @param invocationResolver
     *            Handler to resolve the invocation later on.
     * @return FamixMethod.
     */
    public FamixMethod rememberUnresolvedInvocation(UnresolvedMethodInvocation invocationResolver) {
        // just remember the unresolved invocation and try to solve later
        if (!getUnresolvedCalls().containsKey(getCurrMethod())) {
            getUnresolvedCalls().put(getCurrMethod(), new LinkedList<UnresolvedMethodInvocation>());
        }
        getUnresolvedCalls().get(getCurrMethod()).add(invocationResolver);
        invocationResolver.addAndOverrideVariables(gatherVariablesInScope(getCurrMethod(), getCrawler()
                .getCurrStatementBlock()));

        // always return null, because the method is resolved later on
        return null;
    }

    private void addVars(Hashtable<String, AbstractFamixVariable> variables, Set<AbstractFamixVariable> varsToAdd) {
        for (AbstractFamixVariable entity : varsToAdd) {
            variables.put(entity.getName(), entity);
        }
    }

    private void gatherVariablesInScope(
            Hashtable<String, AbstractFamixVariable> variables,
            AbstractFamixEntity entity,
            StatementBlock statementBlock) {
        if (entity != null && entity instanceof FamixMethod) {
            gatherVariablesInScope(variables, entity.getParent(), statementBlock);
            addVars(variables, new HashSet<AbstractFamixVariable>(((FamixMethod) entity).getParameters()));
            for (FamixLocalVariable lLocalVariable : ((FamixMethod) entity).getLocalVariables()) {
                // get block surrounding local variable declaration
                StatementBlock surroundingBlock = getCrawler().getLocalVariableScope().get(lLocalVariable);
                // check if block of statement is contained by that block
                if (surroundingBlock != null) {
                    if ((surroundingBlock.getStart() <= statementBlock.getStart())
                            && (surroundingBlock.getEnd() >= statementBlock.getEnd())) {
                        Set<AbstractFamixVariable> tmpVarList = new HashSet<AbstractFamixVariable>();
                        tmpVarList.add(lLocalVariable);
                        addVars(variables, tmpVarList);
                    }
                } else {
                    sLogger.warn("No statement block stored for locacl variable " + lLocalVariable.getUniqueName());
                }
            }
        } else if (entity instanceof FamixClass) {
            gatherVariablesInScope(variables, entity.getParent(), statementBlock);
            addVars(variables, new HashSet<AbstractFamixVariable>(((FamixClass) entity).getAttributes()));
        }
    }

    /**
     * Gather all variables, parameters, and attributes that are in the scope of the current method invocation or field
     * access. The scope of attributes and formal parameters is given by parent relationship (class or method). The
     * scope of local variable has to be determined by considering the statement block the local variable is declared
     * in. If it is in the scope of the invocation statement it is added.
     * 
     * Because variables can have the same name we use the following overriding strategy: local variables override
     * formal parameters override attributes (fields).
     * 
     * @param entity
     * @param statementBlock
     * @return The set of declared variables that are in scope of the method invocation statement.
     */
    private Set<AbstractFamixVariable> gatherVariablesInScope(AbstractFamixEntity entity, StatementBlock statementBlock) {
        Hashtable<String, AbstractFamixVariable> variables = new Hashtable<String, AbstractFamixVariable>();
        gatherVariablesInScope(variables, entity, statementBlock);
        return new HashSet<AbstractFamixVariable>(variables.values());
    }

    /**
     * Returns the number of arguments of invocation statements.
     * 
     * @return The number of arguments.
     */
    protected int countResolvedArguments() {
        int counter = 0;
        for (int i = 0; i < getArguments().size(); i++) {
            if (getArguments().get(i).resolveTypeBinding() != null) {
                counter++;
            }
        }

        return counter;
    }

    /**
     * Create the FAMIX calee method from the invocation statement.  
     * 
     * @return The FAMIX method.
     */
    protected FamixMethod createMethod() {
        StringBuffer lMethodID = new StringBuffer();
        if (getMethodBinding() == null) {
            lMethodID.append(AbstractASTNodeHandler.UNDEFINED_BINDING);
            sLogger.warn("FamixMethod binding is null");
        } else {
            ITypeBinding lTypeBinding = getMethodBinding().getDeclaringClass();
            FamixClass lDeclaringClass = getClass(lTypeBinding, null, lTypeBinding.isAnonymous());

            lMethodID.append(lDeclaringClass.getUniqueName());
            lMethodID.append(AbstractFamixEntity.NAME_DELIMITER);
            if (getMethodBinding().isConstructor()) {
                lMethodID.append(AbstractFamixEntity.CONSTRUCTOR_PREFIX);
            } else {
                lMethodID.append(getMethodBinding().getName());
            }
            lMethodID.append(AbstractFamixEntity.METHOD_START_BRACE);
            ITypeBinding[] lParamBindings = getMethodBinding().getParameterTypes();
            for (int i = 0; i < lParamBindings.length; i++) {
                lMethodID.append(convertParameterTypeBindingForMethodSignature(lParamBindings[i]));
                if (i < lParamBindings.length - 1) {
                    lMethodID.append(AbstractFamixEntity.METHOD_PARAMETER_SEPARATOR);
                }
            }
            lMethodID.append(AbstractFamixEntity.METHOD_END_BRACE);
        }

        return createMethodFromSignature(lMethodID.toString());
    }

    /**
     * Sets the list of arguments.
     * 
     * @param arguments The list of arguments.
     */
    protected void setArguments(List<Expression> arguments) {
        fArguments = arguments;
    }

    /**
     * Returns the list of arguments.
     * 
     * @return The list of arguments.
     */
    public List<Expression> getArguments() {
        return fArguments;
    }

    /**
     * Sets the method binding.
     * 
     * @param methodBinding The method binding.
     */
    protected void setMethodBinding(IMethodBinding methodBinding) {
        fMethodBinding = methodBinding;
    }

    /**
     * Returns the method binding.
     * 
     * @return The method Binding.
     */
    public IMethodBinding getMethodBinding() {
        return fMethodBinding;
    }
}
