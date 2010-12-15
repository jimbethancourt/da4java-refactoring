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

import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.importer.FamixModelFactory;
import org.evolizer.famix.importer.unresolved.UnresolvedMethodInvocation;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.entities.FamixModel;
import org.evolizer.famix.model.entities.SourceAnchor;

/**
 * Base class for extracting FAMIX information from jdt AST nodes. Provides also a number of methods to resolve class,
 * method, and field bindings to FAMIX conform unique names.
 * 
 * @author pinzger
 */
public abstract class AbstractASTNodeHandler implements IASTNodeHandler {

    /**
     * The Logger.
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(AbstractASTNodeHandler.class.getName());

    /**
     * Identifier of undefined bindings (which could not be resolved with jdt)
     */
    public static final String UNDEFINED_BINDING = "<undef>";

    /**
     * Sign added to anonymous classes with a binding error.
     */
    public static final String BINDING_ERROR_SIGN = "F";

    /**
     * Actual crawler to retrieve current type and method information.
     */
    private ASTCrawler fCrawler;
    /**
     * The jdt AST node.
     */
    private ASTNode fASTNode;

    /**
     * Switch to control the addition of types to the type reminder stack
     */
    private boolean fTypeAdded;
    /**
     * Switch to control the addition of methods to the method reminder stack
     */
    private boolean fMethodAdded;

    /**
     * The constructor.
     * 
     * @param crawler
     *            Instance of the ASTCrawler to obtain current type and method information.
     */
    public AbstractASTNodeHandler(ASTCrawler crawler) {
        setCrawler(crawler);
        
        fTypeAdded = false;
        fMethodAdded = false;
    }

    /**
     * Processes a jdt AST node and extracts FAMIX related info.
     * 
     * @param node
     *            jdt AST node to visit.
     * @return true if contained nodes should be visited, otherwise false.
     */
    public abstract boolean visit(ASTNode node);

    /**
     * Post-processes a jdt AST node.
     */
    public abstract void endVisit();

    /**
     * Resolve a type binding and get the corresponding FAMIX class. If the binding is null or if we can not get the
     * right binding name we manually resolve the type using the type information from the AST node.
     * 
     * Sometimes the anonymous classes cause problems, because there binding is resolved but no fully qualified name can
     * be retrieved from the binding info. In this case we manually resolve the type and add the BINDING_ERROR_SIGN.
     * 
     * The computed name is used to create a FAMIX class instance.
     * 
     * @param pBinding
     *            ... jdt binding information
     * @param pType
     *            ... type information used if binding is null
     * @param pAnonym
     *            ... explicitly states, that this type is an anonymous class
     * @return FAMIX FamixClass
     */
    public FamixClass getClass(ITypeBinding pBinding, Type pType, boolean pAnonym) {
        String lClassID = null;
        // first try to get jdt resolving the binding
        if (pBinding != null) {
            lClassID = convert(pBinding);
        }

        // if jdt can not resolve the binding try to do it with our algorithms
        if ((lClassID == null) || lClassID.equals(AbstractASTNodeHandler.UNDEFINED_BINDING)) {
            // is the pBinding an anonymous class or did we state that it is one
            // this only works if we are in the right top level type
            if (((pBinding != null) && pBinding.isAnonymous()) || pAnonym) {
                lClassID = convertAnonymousType(getCurrType(), getAnonymClassCounter().get(getCurrType()))
                                + AbstractASTNodeHandler.BINDING_ERROR_SIGN;
            } else {
                lClassID = convert(pType);
            }
        }

        // check if the class is contained in the model
        FamixClass lClass = getFactory().createClass(lClassID, null);
        if (getModel().contains(lClass)) {
            lClass = (FamixClass) getModel().getElement(lClass);
        }
        return lClass;
    }

    /**
     * Compute unique name of anonymous class whose binding could not be resolved.
     * 
     * @param parentClass FamixClass containing the anonymous class.
     * @param nr The number of the anonymous class within a class declaration. 
     * @return Unique name of anonymous class.
     */
    public String convertAnonymousType(FamixClass parentClass, int nr) {
        return parentClass.getUniqueName() + AbstractFamixEntity.ANONYMOUS_CLASS_SEPARATOR + nr;
        // return parentClass.getUniqueName() + ASTCrawler.ANONYMOUS_CLASS_SEPARATOR + ASTCrawler.UNDEFINED_BINDING;
    }

    /**
     * Compute unique name of an unresolved type.This method is used when jdt binding fails.
     * 
     * @param dataType
     *            jdt Type info.
     * @return Unique name of the type.
     */
    public String convert(Type dataType) {
        String lClassID = null;

        if (dataType == null) {
            lClassID = UNDEFINED_BINDING;
            sLogger.warn("Data type is null");
        } else {
            Type typeToResolve = dataType;
            if (dataType.isParameterizedType()) {
                typeToResolve = ((ParameterizedType) dataType).getType();
            }    
            if (typeToResolve.isArrayType()) {
                lClassID = AbstractFamixEntity.ARRAY_TYPE_NAME;
            } else if (typeToResolve.isPrimitiveType()) {
                lClassID = ((PrimitiveType) typeToResolve).getPrimitiveTypeCode().toString();
            } else if (typeToResolve.isSimpleType()) {
                lClassID = convertSimpleName(((SimpleType) typeToResolve).getName());
            }
            
            // handling type parameters - adding a wild card for each unresolved type parameter 
            if (dataType.isParameterizedType()) {
                ParameterizedType paramType = (ParameterizedType) dataType;
                StringBuffer typeParameterStr = new StringBuffer();
//                for (Object param : paramType.typeArguments()) {
                for (int i = 0; i < paramType.typeArguments().size(); i++) {
                    if (typeParameterStr.length() > 0) {
                        typeParameterStr.append(AbstractFamixEntity.CLASS_PARAMETER_SEPARATOR);
                    }
                    typeParameterStr.append("?");
                }
                lClassID += AbstractFamixEntity.CLASS_PARAMETER_START_BRACE + typeParameterStr.toString() + AbstractFamixEntity.CLASS_PARAMETER_END_BRACE;
            }            
        }
        return lClassID;
    }

    /**
     * Compute unique name of a type from its jdt binding. This method is used when binding was resolved.
     * 
     * @param typeBinding
     *            jdt Binding info.
     * @return Unique name of the type.
     */
    public String convert(ITypeBinding typeBinding) {
        String lClassID = null;

        if (typeBinding == null) {
            lClassID = UNDEFINED_BINDING;
            sLogger.warn("Type binding is null");
        } else if (typeBinding.isArray()) {
            lClassID = AbstractFamixEntity.ARRAY_TYPE_NAME;
        } else if (typeBinding.isPrimitive()) {
            lClassID = typeBinding.getQualifiedName();
//        } else if (typeBinding.isAnonymous()) {
//            lClassID = typeBinding.getBinaryName();
        } else {
            lClassID = typeBinding.getBinaryName();
        }

        // handling type parameters
        if ((typeBinding != null) && (typeBinding.isParameterizedType() || typeBinding.isGenericType())) {
            StringBuffer typeParameterStr = new StringBuffer();
            for (ITypeBinding parameterTypeBinding : typeBinding.getTypeDeclaration().getTypeParameters()) {
                if (typeParameterStr.length() > 0) {
                    typeParameterStr.append(AbstractFamixEntity.CLASS_PARAMETER_SEPARATOR);
                }
                typeParameterStr.append(parameterTypeBinding.getName());
            }
            lClassID += AbstractFamixEntity.CLASS_PARAMETER_START_BRACE + typeParameterStr.toString() + AbstractFamixEntity.CLASS_PARAMETER_END_BRACE;
        }
        
        // Again check, if we got a name (sometimes no name can be obtained for anonymous types).
        if (lClassID == null || lClassID.equals("")) {
            sLogger.error("Could not get the type name from the binding " + typeBinding);
            lClassID = UNDEFINED_BINDING;
        }
        return lClassID;
    }

    /**
     * Compute unique name of array parameter in a method signature. In a method signature we need the type of the array
     * such as int[][].
     * 
     * @param typeBinding
     *            Resolved jdt Binding.
     * @return Unique name of parameter.
     */
    public String convertParameterTypeBindingForMethodSignature(ITypeBinding typeBinding) {
        String lClassID = null;

        if (typeBinding.isArray()) {
            String baseType = convert(typeBinding.getElementType());
            StringBuilder arrayAppender = new StringBuilder();
            for (int i = 0; i < typeBinding.getDimensions(); i++) {
                arrayAppender.append(AbstractFamixEntity.METHOD_PARAMETER_ARRAY);
            }
            lClassID = baseType + arrayAppender.toString();
        } else {
            lClassID = convert(typeBinding); // otherwise use normal type converter
        }

        return lClassID;
    }

    /**
     * Compute unique name from unresolved jdt Name. Fully qualified names are returned as is. Simple names are prefixed
     * with UNDEFINED_BINDING. This may lead to wrong unique names, so this method is currently tentative.
     * 
     * @param simpleName
     *            jdt Name information.
     * @return Unique name of the unresolved Name node.
     */
    public String convertSimpleName(Name simpleName) {
        if (simpleName.isQualifiedName()) {
            return simpleName.getFullyQualifiedName();
        } else if (simpleName.isSimpleName()) {
            return UNDEFINED_BINDING + AbstractFamixEntity.NAME_DELIMITER + simpleName.getFullyQualifiedName();
        } else {
            sLogger.warn("Could not resolve simple name of " + simpleName);
            return UNDEFINED_BINDING;
        }
    }

    /**
     * Create FAMIX method from the unique name. The unique name consist of: 1. fully qualified name of the declaring
     * class 2. NAME_DELIMITER 3. method name 4. "(" 5. list of fully qualified parameter types seperated by "," 6. ")"
     * 
     * @param lMethodID
     *            The unique name of the method. Cannot be null.
     * @return FAMIX method.
     */
    public FamixMethod createMethodFromSignature(String lMethodID) {
        FamixMethod lMethod = null;
        if (lMethodID != null && lMethodID.length() > 0) {
            lMethod = getFactory().createMethod(lMethodID, null);
            if (getModel().contains(lMethod)) {
                lMethod = (FamixMethod) getModel().getElement(lMethod);
            }
        }
        return lMethod;
    }

    /**
     * Obtain the FAMIX source anchor from jdt AST node information.
     * 
     * @param astNode
     *            jdt AST node for which the source anchor information is retrieved.
     * @return Source anchor from the referenced jdt AST node.
     */
    protected SourceAnchor getSourceAnchor(ASTNode astNode) {
        ASTNode root = astNode.getRoot();
        String file = "";
        if (root instanceof CompilationUnit) {
            file = ((CompilationUnit) root).getJavaElement().getPath().toString();
        }

        int startPosition = astNode.getStartPosition();
        int length = astNode.getLength();

        return new SourceAnchor(file, startPosition, startPosition + length);

    }

    /**
     * Delegator method to obtain source anchor from the jdt AST information referenced within this handler.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getSourceAnchor(org.eclipse.jdt.core.dom.ASTNode)
     * 
     * @return Source anchor from the referenced jdt AST node.
     */
    public SourceAnchor getSourceAnchor() {
        return getSourceAnchor(getASTNode());
    }

    /**
     * Delegator method to set the current type (FAMIX class).
     * 
     * @see org.evolizer.famix.importer.ASTCrawler#setCurrType(org.evolizer.famix.model.entities.FamixClass)
     * 
     * @param currType
     *            Current FAMIX class.
     */
    protected void setCurrType(FamixClass currType) {
        getCrawler().setCurrType(currType);
    }

    /**
     * Delegator method to obtain the current type (FAMIX class).
     * 
     * @see org.evolizer.famix.importer.ASTCrawler#getCurrType()
     * 
     * @return FAMIX class.
     */
    protected FamixClass getCurrType() {
        return getCrawler().getCurrType();
    }

    /**
     * Delegator method to set the current FAMIX method.
     * 
     * @see org.evolizer.famix.importer.ASTCrawler#setCurrMethod(org.evolizer.famix.model.entities.FamixMethod)
     * 
     * @param currMethod
     *            Current FAMIX method.
     */
    protected void setCurrMethod(FamixMethod currMethod) {
        getCrawler().setCurrMethod(currMethod);
    }

    /**
     * Delegator method to obtain the current FAMIX method.
     * 
     * @see org.evolizer.famix.importer.ASTCrawler#getCurrMethod()
     * 
     * @return The current FAMIX method.
     */
    protected FamixMethod getCurrMethod() {
        return getCrawler().getCurrMethod();
    }

    /**
     * Delegator method to add the current type to the type reminder stack.
     * 
     * @see org.evolizer.famix.importer.ASTCrawler#addCurrMethodToMethodReminder()
     */
    protected void addCurrTypeToTypeReminder() {
        getCrawler().addCurrTypeToTypeReminder();
        activateTypeAdded();
    }

    /**
     * Delegator method to obtain the top most class from the type reminder stack without removing it.
     * 
     * @see org.evolizer.famix.importer.ASTCrawler#getCurrTypeFromTypeReminder()
     * 
     * @return The top most class from the type reminder stack.
     */
    protected FamixClass getCurrTypeFromTypeReminder() {
        return getCrawler().getCurrTypeFromTypeReminder();
    }

    /**
     * Delegator method to remove the top most class from the type reminder stack.
     * 
     * @see org.evolizer.famix.importer.ASTCrawler#setCurrTypeFromTypeReminder()
     */
    protected void setCurrTypeFromTypeReminder() {
        if (isTypeAddedActivated()) {
            getCrawler().setCurrTypeFromTypeReminder();
        }
    }

    /**
     * Delegator method to add the current method to the method reminder stack.
     * 
     * @see org.evolizer.famix.importer.ASTCrawler#addCurrMethodToMethodReminder()
     */
    protected void addCurrMethodToMethodReminder() {
        getCrawler().addCurrMethodToMethodReminder();
        activateMethodAdded();
    }

    /**
     * Delegator method to remove the top most method from the method reminder stack.
     * 
     * @see org.evolizer.famix.importer.ASTCrawler#setCurrMethodFromMethodReminder()
     */
    protected void setCurrMethodFromMethodReminder() {
        if (isMethodAddedActivated()) {
            getCrawler().setCurrMethodFromMethodReminder();
        }
    }

    /**
     * Delegator method to obtain the FAMIX model container.
     * 
     * @see org.evolizer.famix.importer.ASTCrawler#getModel()
     * 
     * @return The FAMIX model container.
     */
    protected FamixModel getModel() {
        return getCrawler().getModel();
    }

    /**
     * Delegator method to obtain the FAMIX model factory.
     * 
     * @see org.evolizer.famix.importer.ASTCrawler#getFactory()
     * 
     * @return The FAMIX model factory.
     */
    protected FamixModelFactory getFactory() {
        return getCrawler().getFactory();
    }

    /**
     * Delegator method to obtain the list of unresolved method calls.
     * 
     * @see org.evolizer.famix.importer.ASTCrawler#getUnresolvedCalls()
     * 
     * @return The list of unresolved method calls.
     */
    protected Hashtable<FamixMethod, List<UnresolvedMethodInvocation>> getUnresolvedCalls() {
        return getCrawler().getUnresolvedCalls();
    }

    /**
     * Delegator method to obtain the map containing the counters for anonymous classes per parent class.
     * 
     * @see org.evolizer.famix.importer.ASTCrawler#getAnonymClassCounter()
     * 
     * @return The map containing the counters for anonymous classes per parent class.
     */
    protected Hashtable<FamixClass, Integer> getAnonymClassCounter() {
        return getCrawler().getAnonymClassCounter();
    }

    /**
     * Sets the ASTCrawler instance.
     * 
     * @param crawler
     *            The ASTCrawler instance.
     */
    public void setCrawler(ASTCrawler crawler) {
        fCrawler = crawler;
    }

    /**
     * Returns the ASTCrawler instance.
     * 
     * @return The ASTCrawler instance.
     */
    public ASTCrawler getCrawler() {
        return fCrawler;
    }

    /**
     * Returns the jdt AST node.
     * 
     * @return The jdt AST node.
     */
    public ASTNode getASTNode() {
        return fASTNode;
    }

    /**
     * Sets the jdt AST node.
     * 
     * @param node
     *            The jdt AST node.
     */
    protected void setASTNode(ASTNode node) {
        fASTNode = node;
    }

    /**
     * Type has been added to the current type reminder stack.
     */
    protected void activateTypeAdded() {
        fTypeAdded = true;
    }

    /**
     * FamixMethod has been added to the current method reminder stack.
     */
    protected void activateMethodAdded() {
        fMethodAdded = true;
    }

    /**
     * Check if a type has been added to the current type reminder stack.
     * 
     * @return true yes, otherwise false.
     */
    protected boolean isTypeAddedActivated() {
        return fTypeAdded;
    }

    /**
     * Check if a method has been added to the current method reminder stack.
     * 
     * @return true yes, otherwise false.
     */
    protected boolean isMethodAddedActivated() {
        return fMethodAdded;
    }
}
