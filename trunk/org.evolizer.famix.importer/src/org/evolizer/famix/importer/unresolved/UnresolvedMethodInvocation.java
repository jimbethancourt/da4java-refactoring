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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.evolizer.core.hibernate.model.api.IEvolizerModelEntity;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.importer.FamixModelFactory;
import org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler;
import org.evolizer.famix.importer.nodehandler.AbstractInvocationHandler;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.AbstractFamixGeneralization;
import org.evolizer.famix.model.entities.AbstractFamixVariable;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.entities.FamixModel;

/**
 * This source code is not final and subject to change.
 * 
 * Handles an unresolved method invocation. Various matching strategies for finding the corresponding method are use:
 * <ul>
 * <li>by name
 * <li>by number of arguments
 * <li>by the type of the object receiving the method call (also taking inheritance into account)
 * <li>by argument types (inheritance is not considered, yet)
 * </ul>
 * 
 * @author pinzger
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING, length = 255)
public class UnresolvedMethodInvocation implements IEvolizerModelEntity {

    /**
     * The sLogger
     */
    private static Logger sLogger =
            FamixImporterPlugin.getLogManager().getLogger(UnresolvedMethodInvocation.class.getName());

    /**
     * The invocation handler containing the AST Node of the unresolved call
     */
    @Transient
    private AbstractInvocationHandler fInvocationHandler;

    /**
     * The fVariables in scope of the unresolved call
     */
    @Transient
    private Hashtable<String, AbstractFamixVariable> fVariables = new Hashtable<String, AbstractFamixVariable>();

    /**
     * Resolved object receiving the call
     */
    @Transient
    private String fResolvedID;
    /**
     * List of resolved arguments (expressions)
     */
    @Transient
    private Hashtable<Expression, String> fResolvedArguments;

    // attributes to store in the db
    /**
     * Hibernate fId
     */
    private Long fId;
    /**
     * The unresolved source code fStatement
     */
    private String fStatement;
    /**
     * The (partially) resolved fStatement
     */
    private String fResolvedStatement;
    /**
     * The called FAMIX method
     */
    private FamixMethod fCaller;
    /**
     * The result of the matching by name strategy
     */
    private Set<FamixMethod> fMatchesByName = new HashSet<FamixMethod>();
    /**
     * The result of the matching by parameter strategy
     */
    private Set<FamixMethod> fMatchesByNrOfParameters = new HashSet<FamixMethod>();
    // private Set<ParameterTypeMatch> matchesByParameterType = new HashSet<ParameterTypeMatch>();
    /**
     * The result of the matching by object type receiving the call strategy
     */
    private Set<FamixMethod> fMatchesByCallReceiverType = new HashSet<FamixMethod>();
    /**
     * The result of the matching by object type receiving the call strategy, also considering sub-typing
     */
    private Set<FamixMethod> fMatchesByCallReceiverTypeSubtyping = new HashSet<FamixMethod>();
    /**
     * The result of the matching by parameter type strategy
     */
    private Set<FamixMethod> fMatchesByAllParametersType = new HashSet<FamixMethod>();
    /**
     * The result of the matching by parameter type strategy, doing partial type matching
     */
    private Set<FamixMethod> fMatchesByAllParametersTypeSoft = new HashSet<FamixMethod>();
    /**
     * The result of the matching by parameter type strategy, also considering sub-typing
     */
    private Set<FamixMethod> fMatchesByAllParametersTypeSubtyping = new HashSet<FamixMethod>();

    // private Set<BehaviouralEntity> matchesByReturnType = new HashSet<BehaviouralEntity>();

    /**
     * Returns the id.
     * 
     * @return The Hibernate ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return fId;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            The Hibernate ID.
     */
    protected void setId(Long id) {
        fId = id;
    }

    /**
     * The constructor.
     * 
     * @param caller
     *            FamixMethod containing the unresolved call.
     * @param invocationHandler
     *            Handler to be used to resolve the call later.
     */
    public UnresolvedMethodInvocation(FamixMethod caller, AbstractInvocationHandler invocationHandler) {
        setCaller(caller);
        setASTNodeHandler(invocationHandler);

        fResolvedID = AbstractASTNodeHandler.UNDEFINED_BINDING;
        setStatement(invocationHandler.getASTNode().toString());

        initResolvedArguments();
    }

    /**
     * Delegator to create the method invoked in the call fStatement.
     * 
     * @return created FAMIX FamixMethod
     */
    public FamixMethod createMethodFromSignature() {
        return getInvocationHandler().createMethodFromSignature(getResolvedStatement());
    }

    /**
     * Initializes the list of arguments of the call with <code>undef</code>.
     */
    protected void initResolvedArguments() {
        fResolvedArguments = new Hashtable<Expression, String>();
        for (Object expr : getArgumentsFromASTNode()) {
            fResolvedArguments.put((Expression) expr, AbstractASTNodeHandler.UNDEFINED_BINDING);
        }
    }

    /**
     * Adds the fVariables in <code>vars</code> to the list of fVariables in scope of a method call. If a variable
     * already exists it is replaced.
     * 
     * @param vars
     *            List of fVariables.
     */
    public void addAndOverrideVariables(Set<AbstractFamixVariable> vars) {
        for (AbstractFamixVariable var : vars) {
            fVariables.put(var.getName(), var);
        }
    }

    /**
     * Adds the fVariables in <code>vars</code> to the list of fVariables in scope of the unresolved method call, but,
     * do not replace if it already exists.
     * 
     * @param vars
     *            List of fVariables.
     */
    public void addAndNotOverrideVariables(Set<AbstractFamixVariable> vars) {
        for (AbstractFamixVariable var : vars) {
            if (!fVariables.containsKey(var.getName())) {
                fVariables.put(var.getName(), var);
            }
        }
    }

    /**
     * Returns the arguments from ast node.
     * 
     * @return The list of arguments (expressions) of a method invocation.
     */
    @Transient
    public List<Expression> getArgumentsFromASTNode() {
        return getInvocationHandler().getArguments();
    }

    /**
     * Apply the various matching strategies to the list of potential callee methods.
     * 
     * @param unresolvedMethodDeclarations
     *            List of potential callee methods
     */
    public void matchWithUnresolvedMethodDeclarations(List<FamixMethod> unresolvedMethodDeclarations) {
        resolve();

        // the following two must be aequivalent
        compareByName(unresolvedMethodDeclarations);
        compareByParameterCount(getMatchesByName());

        // reduce the matched set of potential callee using different heuristics
        // compareByParameterType(getMatchesByNrOfParameters());

        // compareByCallReceiverType(getMatchesByNrOfParameters());
        compareByCallReceiverTypeSubtyping(getMatchesByNrOfParameters());

        // compareByAllParametersType(getMatchesByCallReceiverType());
        // compareByAllParametersTypeSoft(getMatchesByCallReceiverType());
        compareByAllParametersType(getMatchesByCallReceiverTypeSubtyping());
        compareByAllParametersTypeSoft(getMatchesByCallReceiverTypeSubtyping());
    }

    /**
     * Returns the ast node name.
     * 
     * @return The name of the method invocation.
     */
    @Transient
    protected String getASTNodeName() {
        return ((MethodInvocation) getASTNode()).getName().toString();
    }

    /**
     * Compare by method name. Name must be equal.
     * 
     * @param potentialMethodDeclarations
     *            List of potential callee methods
     */
    private void compareByName(List<FamixMethod> potentialMethodDeclarations) {
        fMatchesByName = new HashSet<FamixMethod>();
        for (FamixMethod callee : potentialMethodDeclarations) {
            if (getASTNodeName().equals(callee.getName())) {
                fMatchesByName.add(callee);
            }
        }
    }

    /**
     * Compare by number of parameters. The number of parameters must be equal.
     * 
     * @param potentialMethodDeclarations
     *            List of potential callee methods
     */
    private void compareByParameterCount(Set<FamixMethod> potentialMethodDeclarations) {
        fMatchesByNrOfParameters = new HashSet<FamixMethod>();
        for (FamixMethod callee : potentialMethodDeclarations) {
            if (getArgumentsFromASTNode().size() == callee.getParameters().size()) {
                fMatchesByNrOfParameters.add(callee);
            }
        }
    }

    /**
     * Compare by parameter types. All types must match.
     * 
     * @param potentialMethodDeclarations
     *            List of potential callee methods.
     */
    private void compareByAllParametersType(Set<FamixMethod> potentialMethodDeclarations) {
        fMatchesByAllParametersType = new HashSet<FamixMethod>();
        for (FamixMethod callee : potentialMethodDeclarations) {

            String lParamString =
                    callee.getUniqueName().substring(
                            callee.getUniqueName().indexOf(AbstractFamixEntity.METHOD_START_BRACE) + 1,
                            callee.getUniqueName().indexOf(AbstractFamixEntity.METHOD_END_BRACE));
            String[] lParams = new String[]{};
            if (!lParamString.equals("")) {
                lParams = lParamString.split(AbstractFamixEntity.METHOD_PARAMETER_SEPARATOR);
            }
            if (lParams.length == getArgumentsFromASTNode().size()) {
                int paramCount = 0;
                for (int index = 0; index < getArgumentsFromASTNode().size(); index++) {
                    Expression argument = getArgumentsFromASTNode().get(index);
                    // FamixParameter param = callee.getFormalParameters().get(index);

                    // try a full match to get a best match
                    if (!getResolvedArguments().get(argument).equals(lParams[index])) {
                        break;
                        // } else if
                        // (param.getDeclaredClass().getUniqueName().contains(AbstractASTNodeHandler.UNDEFINED_BINDING)
                        // && getArguments().get(argument).equals(AbstractASTNodeHandler.UNDEFINED_BINDING)) {
                    }

                    paramCount++;
                }
                // all parameter types must match
                if (paramCount == getArgumentsFromASTNode().size()) {
                    fMatchesByAllParametersType.add(callee);
                }
            }
        }

    }

    /**
     * Compare by parameter types. Parameter with type <code>undef</code> are equal, if the type of the corresponding
     * parameter in the potential callee method contains <code>undef</code>.
     * 
     * @param potentialMethodDeclarations
     *            List of potential callee methods
     */
    private void compareByAllParametersTypeSoft(Set<FamixMethod> potentialMethodDeclarations) {
        fMatchesByAllParametersTypeSoft = new HashSet<FamixMethod>();
        for (FamixMethod callee : potentialMethodDeclarations) {

            // get the parameter type names (as specified in the method declaration)
            // first, get list of parameters from unique name of method
            String lParamString =
                    callee.getUniqueName().substring(
                            callee.getUniqueName().indexOf(AbstractFamixEntity.METHOD_START_BRACE) + 1,
                            callee.getUniqueName().indexOf(AbstractFamixEntity.METHOD_END_BRACE));
            String[] lParams = new String[]{};
            if (!lParamString.equals("")) {
                lParams = lParamString.split(AbstractFamixEntity.METHOD_PARAMETER_SEPARATOR);
            }
            if (lParams.length == getArgumentsFromASTNode().size()) {
                int paramCount = 0;
                for (int index = 0; index < getArgumentsFromASTNode().size(); index++) {
                    Expression argument = getArgumentsFromASTNode().get(index);
                    // FamixParameter param = callee.getFormalParameters().get(index);

                    // try a full match to get a best match
                    // we can not compare with the declared class of formal parameters
                    // because in case of of an array this is <ARRAY> causing the match to fail.
                    // System.err.println(getArguments().get(argument) + " - " + lParams[index]);
                    if (!(getResolvedArguments().get(argument).equals(lParams[index]) || ((lParams[index]
                            .contains(AbstractASTNodeHandler.UNDEFINED_BINDING) && getResolvedArguments().get(argument)
                            .contains(AbstractASTNodeHandler.UNDEFINED_BINDING))))) {
                        break;
                    }

                    paramCount++;
                }
                // all parameter types must match
                if (paramCount == getArgumentsFromASTNode().size()) {
                    fMatchesByAllParametersTypeSoft.add(callee);
                }
            }
        }

    }

    @SuppressWarnings("unused")
    private void compareByCallReceiverType(Set<FamixMethod> potentialMethodDeclarations) {
        fMatchesByCallReceiverType = new HashSet<FamixMethod>();
        for (FamixMethod callee : potentialMethodDeclarations) {
            if (getResolvedID().equals(callee.getParent().getUniqueName())) {
                fMatchesByCallReceiverType.add(callee);
            }
        }
    }

    private void compareByCallReceiverTypeSubtyping(Set<FamixMethod> potentialMethodDeclarations) {
        fMatchesByCallReceiverTypeSubtyping = new HashSet<FamixMethod>();

        // init inheritance hierarchy of resolved receiver object/class
        FamixClass receiverClass = (FamixClass) getModel().getElement(getFactory().createClass(getResolvedID(), null));
        List<FamixClass> superTypes = new LinkedList<FamixClass>();
        if (receiverClass != null) {
            List<FamixClass> subTypes = new LinkedList<FamixClass>();
            subTypes.add(receiverClass);
            superTypes = computeSuperTypes(subTypes);
            superTypes.add(receiverClass); // also include the sub-class
        }

        for (FamixMethod callee : potentialMethodDeclarations) {
            // check if the class of the potential callee is contained in the inheritance hierarchy
            // of the type of the unresolved call.
            if (superTypes.contains(callee.getParent())) {
                fMatchesByCallReceiverTypeSubtyping.add(callee);
            }
        }
    }

    /**
     * Computes all super types of the given data types.
     * 
     * @param subTypes
     *            The list of sub-types.
     * 
     * @return The list of super types.
     */
    public List<FamixClass> computeSuperTypes(List<FamixClass> subTypes) {
        List<FamixClass> superTypes = new LinkedList<FamixClass>();

        for (FamixClass subType : subTypes) {
            Set<FamixAssociation> relationships = getModel().getAssociations(subType);
            for (FamixAssociation association : relationships) {
                // only consider sub-type relationships to super classes
                // check if we should exclude java.lang.Object
                if ((association instanceof AbstractFamixGeneralization) && association.getFrom().equals(subType)) {
                    superTypes.add((FamixClass) association.getTo());
                }
            }
        }
        if (superTypes.size() > 0) {
            superTypes.addAll(computeSuperTypes(superTypes));
        }

        return superTypes;
    }

    /**
     * Returns the model.
     * 
     * @return The current model container.
     */
    @Transient
    public FamixModel getModel() {
        return getCrawler().getModel();
    }

    /**
     * Returns the factory.
     * 
     * @return The current model factory.
     */
    @Transient
    public FamixModelFactory getFactory() {
        return getCrawler().getFactory();
    }

    /**
     * Returns the callee method in a FAMIX conform format.
     * 
     * @return The FAMIX conform callee method string.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder signature = new StringBuilder();
        signature.append(getResolvedID());
        signature.append(AbstractFamixEntity.NAME_DELIMITER);
        signature.append(getASTNodeName());
        signature.append("(");
        StringBuilder argumentString = new StringBuilder();
        for (Object expression : getArgumentsFromASTNode()) { // order of arguments!
            if (argumentString.length() > 0) {
                argumentString.append(",");
            }
            argumentString.append(getResolvedArguments().get(expression));
        }
        signature.append(argumentString);
        signature.append(")");
        return signature.toString();
    }

    /**
     * Returns the label.
     * 
     * @return The label.
     * 
     * @see org.evolizer.core.hibernate.model.api.IEvolizerModelEntity#getLabel()
     */
    @Transient
    public String getLabel() {
        return getASTNodeName();
    }

    /**
     * Returns the uri.
     * 
     * @return The URI.
     * 
     * @see org.evolizer.core.hibernate.model.api.IEvolizerModelEntity#getURI()
     */
    @Transient
    public String getURI() {
        return toString();
    }

    /**
     * Resolves the method invocation.
     */
    public void resolve() {
        resolveObjectIdentifier();
        resolveArguments();
        setResolvedStatement(toString());
    }

    /**
     * Resolved the object/class receiving the call.
     */
    protected void resolveObjectIdentifier() {
        if (!isObjectIDResolved()) {
            if (((MethodInvocation) getASTNode()).getExpression() != null) {
                ITypeBinding lTBinding = ((MethodInvocation) getASTNode()).getExpression().resolveTypeBinding();
                String classID = "";
                // Why the additional check? Can we not use the jdt binding.
                // if ((lTBinding != null)
                // && lTBinding.toString().contains(((MethodInvocation) getASTNode()).getExpression().toString())) {
                // // System.err.println("ObjectID: " + getASTNode().toString());
                // }
                if (lTBinding != null) {
                    classID = getInvocationHandler().convert(lTBinding);
                } else {
                    classID = resolveTypeBinding(((MethodInvocation) getASTNode()).getExpression());
                }
                if ((classID != null) && !classID.equals("")) {
                    setResolvedID(classID);
                }
            } else {
                if (getCaller().getParent() != null) {
                    setResolvedID(getCaller().getParent().getUniqueName());
                }
            }
        }
    }

    /**
     * Resolves the types of the arguments.
     */
    protected void resolveArguments() {
        for (Expression expression : getResolvedArguments().keySet()) {
            if (getResolvedArguments().get(expression).equals(AbstractASTNodeHandler.UNDEFINED_BINDING)) {
                String argumentType = AbstractASTNodeHandler.UNDEFINED_BINDING;

                ITypeBinding expressionBinding = expression.resolveTypeBinding();
                if (expressionBinding != null) {
                    argumentType =
                            getInvocationHandler().convertParameterTypeBindingForMethodSignature(expressionBinding);
                } else if (expression instanceof Name) {
                    // manually resolving the type of the argument
                    Name castedName = (Name) expression;
                    IBinding binding = castedName.resolveBinding();
                    if ((binding != null) && (binding.getKind() == IBinding.VARIABLE)) {
                        IVariableBinding varBinding = (IVariableBinding) binding;
                        ITypeBinding varTypeBinding = varBinding.getType();
                        argumentType = getInvocationHandler().convert(varTypeBinding);
                    }
                    if (argumentType.equals(AbstractASTNodeHandler.UNDEFINED_BINDING)) {
                        argumentType = resolveTypeBinding(expression);
                    }
                } else {
                    argumentType = resolveTypeBinding(expression);
                }
                getResolvedArguments().put(expression, argumentType);
            }
        }
    }

    /**
     * Checks if is object id resolved.
     * 
     * @return true, if the object has been resolved.
     */
    @Transient
    protected boolean isObjectIDResolved() {
        return (!getResolvedID().equals(AbstractASTNodeHandler.UNDEFINED_BINDING));
    }

    /**
     * Checks if is all arguments resolved.
     * 
     * @return true, if all argument types have been resolved.
     */
    @Transient
    protected boolean isAllArgumentsResolved() {
        for (Expression expression : getResolvedArguments().keySet()) {
            if (getResolvedArguments().get(expression).equals(AbstractASTNodeHandler.UNDEFINED_BINDING)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if is fully resolved.
     * 
     * @return true, if the call has been fully resolved (i.e., object and argument types).
     */
    @Transient
    public boolean isFullyResolved() {
        return isObjectIDResolved() && isAllArgumentsResolved();
    }

    /**
     * Resolves the type of an expression.
     * 
     * @param expr
     *            The expression node
     * 
     * @return The unique name of the corresponding FAMIX class
     */
    protected String resolveTypeBinding(Expression expr) {

        String name = "";
        if (expr instanceof QualifiedName) {
            // use the fully qualified name - this is not always the right way for instance
            // when resolving the type of fVariables or constants with a fully name
            // then the name is returned instead of the type
            return ((QualifiedName) expr).getFullyQualifiedName();
        } else if (expr instanceof SimpleName) {
            name = ((SimpleName) expr).getFullyQualifiedName();
        } else {
            sLogger.warn("Can not resolve type of method invocation expression " + expr);
        }

        int count = 0;
        AbstractFamixVariable variable = null;
        for (AbstractFamixVariable entity : fVariables.values()) {
            if (entity.getName().equals(name)) {
                variable = entity;
                count++;
            }
        }

        if ((count == 1) && (variable != null)) {
            // get qualified name from declaring class
            return variable.getDeclaredClass().getUniqueName();
        } else {
            sLogger.warn("Found " + count + " matches for " + expr);
        }

        return AbstractASTNodeHandler.UNDEFINED_BINDING;
    }

    /**
     * Returns the caller.
     * 
     * @return the method containing the unresolved method call
     */
    @ManyToOne
    @JoinColumn(name = "caller_id")
    public FamixMethod getCaller() {
        return fCaller;
    }

    /**
     * Returns the matches by name.
     * 
     * @return The list of potential callee methods.
     */
    @ManyToMany
    @JoinTable(name = "callees_byname", joinColumns = {@JoinColumn(name = "unresolved_invocation_id") }, inverseJoinColumns = @JoinColumn(name = "potential_callee_id"))
    public Set<FamixMethod> getMatchesByName() {
        return fMatchesByName;
    }

    /**
     * Sets the matches by name.
     * 
     * @param matchesByName
     *            The list of potential callee methods.
     */
    public void setMatchesByName(Set<FamixMethod> matchesByName) {
        fMatchesByName = matchesByName;
    }

    /**
     * Returns the matches by nr of parameters.
     * 
     * @return The list of potential callee methods.
     */
    @ManyToMany
    @JoinTable(name = "callees_bynrofparameters", joinColumns = {@JoinColumn(name = "unresolved_invocation_id") }, inverseJoinColumns = @JoinColumn(name = "potential_callee_id"))
    public Set<FamixMethod> getMatchesByNrOfParameters() {
        return fMatchesByNrOfParameters;
    }

    /**
     * Sets the matches by nr of parameters.
     * 
     * @param matchesByNrOfParameters
     *            The list of potential callee methods.
     */
    public void setMatchesByNrOfParameters(Set<FamixMethod> matchesByNrOfParameters) {
        fMatchesByNrOfParameters = matchesByNrOfParameters;
    }

    // /**
    // * @return the matchesByParameterType
    // */
    // @ManyToMany(cascade=CascadeType.ALL)
    // @JoinTable(
    // name="callees_byparametertype",
    // joinColumns = { @JoinColumn( name="unresolved_invocation_id") },
    // inverseJoinColumns = @JoinColumn( name="param_type_match_id")
    // )
    // public Set<ParameterTypeMatch> getMatchesByParameterType() {
    // return matchesByParameterType;
    // }
    //
    // /**
    // * @param matchesByParameterType the matchesByParameterType to set
    // */
    // public void setMatchesByParameterType(
    // Set<ParameterTypeMatch> matchesByParameterType) {
    // this.matchesByParameterType = matchesByParameterType;
    // }

    /**
     * Returns the matches by call receiver type.
     * 
     * @return The list of potential callee methods.
     */
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "callees_bycallreceivertype", joinColumns = {@JoinColumn(name = "unresolved_invocation_id") }, inverseJoinColumns = @JoinColumn(name = "potential_callee_id"))
    public Set<FamixMethod> getMatchesByCallReceiverType() {
        return fMatchesByCallReceiverType;
    }

    /**
     * Sets the matches by call receiver type.
     * 
     * @param matchesByCallReceiverType
     *            The list of potential callee methods.
     */
    public void setMatchesByCallReceiverType(Set<FamixMethod> matchesByCallReceiverType) {
        fMatchesByCallReceiverType = matchesByCallReceiverType;
    }

    /**
     * Returns the matches by all parameters type.
     * 
     * @return The list of potential callee methods.
     */
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "callees_byallparameterstype", joinColumns = {@JoinColumn(name = "unresolved_invocation_id") }, inverseJoinColumns = @JoinColumn(name = "potential_callee_id"))
    public Set<FamixMethod> getMatchesByAllParametersType() {
        return fMatchesByAllParametersType;
    }

    /**
     * Sets the matches by all parameters type.
     * 
     * @param matchesByAllParametersType
     *            The list of potential callee methods.
     */
    public void setMatchesByAllParametersType(Set<FamixMethod> matchesByAllParametersType) {
        fMatchesByAllParametersType = matchesByAllParametersType;
    }

    /**
     * Returns the matches by all parameters type soft.
     * 
     * @return The list of potential callee methods.
     */
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "callees_byallparameterstypesoft", joinColumns = {@JoinColumn(name = "unresolved_invocation_id") }, inverseJoinColumns = @JoinColumn(name = "potential_callee_id"))
    public Set<FamixMethod> getMatchesByAllParametersTypeSoft() {
        return fMatchesByAllParametersTypeSoft;
    }

    /**
     * Sets the matches by all parameters type soft.
     * 
     * @param matchesByAllParametersTypeSoft
     *            The list of potential callee methods.
     */
    public void setMatchesByAllParametersTypeSoft(Set<FamixMethod> matchesByAllParametersTypeSoft) {
        fMatchesByAllParametersTypeSoft = matchesByAllParametersTypeSoft;
    }

    /**
     * Returns the matches by all parameters type subtyping.
     * 
     * @return The list of potential callee methods
     */
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "callees_byallparameterstypest", joinColumns = {@JoinColumn(name = "unresolved_invocation_id") }, inverseJoinColumns = @JoinColumn(name = "potential_callee_id"))
    public Set<FamixMethod> getMatchesByAllParametersTypeSubtyping() {
        return fMatchesByAllParametersTypeSubtyping;
    }

    /**
     * Sets the matches by all parameters type subtyping.
     * 
     * @param matchesByAllParametersTypeSubtyping
     *            The list of potential callee methods.
     */
    public void setMatchesByAllParametersTypeSubtyping(Set<FamixMethod> matchesByAllParametersTypeSubtyping) {
        fMatchesByAllParametersTypeSubtyping = matchesByAllParametersTypeSubtyping;
    }

    /**
     * Returns the matches by call receiver type subtyping.
     * 
     * @return The list of potential callee methods.
     */
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "callees_bycallreceivertypest", joinColumns = {@JoinColumn(name = "unresolved_invocation_id") }, inverseJoinColumns = @JoinColumn(name = "potential_callee_id"))
    public Set<FamixMethod> getMatchesByCallReceiverTypeSubtyping() {
        return fMatchesByCallReceiverTypeSubtyping;
    }

    /**
     * Sets the matches by call receiver type subtyping.
     * 
     * @param matchesByCallReceiverTypeSubtyping
     *            The list of potential callee methods.
     */
    public void setMatchesByCallReceiverTypeSubtyping(Set<FamixMethod> matchesByCallReceiverTypeSubtyping) {
        fMatchesByCallReceiverTypeSubtyping = matchesByCallReceiverTypeSubtyping;
    }

    /**
     * Returns the statement.
     * 
     * @return The method invocation statement.
     */
    @Lob
    public String getStatement() {
        return fStatement;
    }

    /**
     * Sets the statement.
     * 
     * @param statement
     *            The method invocation statement.
     */
    public void setStatement(String statement) {
        fStatement = statement;
    }

    /**
     * Returns the ast node.
     * 
     * @return The AST node.
     */
    @Transient
    public ASTNode getASTNode() {
        return getInvocationHandler().getASTNode();
    }

    /**
     * Returns the resolved id.
     * 
     * @return The resolved ID of the object/class receiving the call.
     */
    @Transient
    public String getResolvedID() {
        return fResolvedID;
    }

    /**
     * Sets the resolved id.
     * 
     * @param resolvedID
     *            The resolved ID of the object/class receiving the call.
     */
    public void setResolvedID(String resolvedID) {
        fResolvedID = resolvedID;
    }

    /**
     * Returns the resolved arguments.
     * 
     * @return The hash table of arguments and their resolved type.
     */
    @Transient
    public Hashtable<Expression, String> getResolvedArguments() {
        return fResolvedArguments;
    }

    /**
     * Sets the caller.
     * 
     * @param caller
     *            The FAMIX method containing the unresolved method call.
     */
    public void setCaller(FamixMethod caller) {
        fCaller = caller;
    }

    /**
     * Sets the resolved statement.
     * 
     * @param resolvedStatement
     *            The resolved statement string.
     */
    public void setResolvedStatement(String resolvedStatement) {
        fResolvedStatement = resolvedStatement;
    }

    /**
     * Returns the resolved statement.
     * 
     * @return The resolved statement.
     */
    @Lob
    public String getResolvedStatement() {
        return fResolvedStatement;
    }

    /**
     * Returns the crawler.
     * 
     * @return The instance of the AST crawler.
     */
    @Transient
    public ASTCrawler getCrawler() {
        return getInvocationHandler().getCrawler();
    }

    /**
     * Returns the invocation handler.
     * 
     * @return The handler containing the AST node representing the unresolved method call.
     */
    @Transient
    protected AbstractInvocationHandler getInvocationHandler() {
        return fInvocationHandler;
    }

    /**
     * Sets the ast node handler.
     * 
     * @param astNodeHandler
     *            The handler containing the AST node representing the unresolved method call.
     */
    protected void setASTNodeHandler(AbstractInvocationHandler astNodeHandler) {
        fInvocationHandler = astNodeHandler;
    }
}
