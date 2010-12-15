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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixParameter;
import org.evolizer.famix.model.entities.FamixMethod;

/**
 * Handles method and constructor declarations.
 * 
 * @author pinzger
 */
public class MethodDeclarationHandler extends AbstractASTNodeHandler {
    /**
     * The Logger.
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(MethodDeclarationHandler.class.getName());

    /**
     * The constructor
     * 
     * @param crawler Instance of ASTCrawler to obtain current type and method information.
     */
    public MethodDeclarationHandler(ASTCrawler crawler) {
        super(crawler);
    }

    /**
     * Helper method for returning the AST node.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return The method declaration AST node.
     */
    @Override
    public MethodDeclaration getASTNode() {
        return (MethodDeclaration) super.getASTNode();
    }

    /**
     * Handle method declarations.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#visit(org.eclipse.jdt.core.dom.ASTNode)
     * 
     * @param methodDeclaration The AST node representing the method declaration
     * @return true, if contained nodes should be visited
     */
    @Override
    public boolean visit(ASTNode methodDeclaration) {
        boolean visitChildren = true;
        setASTNode(methodDeclaration);
        addCurrMethodToMethodReminder();

        try {
            FamixMethod lCurrMethod = createMethod();
            lCurrMethod = (FamixMethod) getModel().addElement(lCurrMethod);

            int lModifiers = getASTNode().getModifiers();
            lCurrMethod.setModifiers(lModifiers);
            lCurrMethod.setSourceAnchor(getSourceAnchor());
            if (getCurrType().getMethods().contains(lCurrMethod)) {
                getCurrType().getMethods().remove(lCurrMethod); // always use the declared method object
            }
            getCurrType().getMethods().add(lCurrMethod);
            lCurrMethod.setParent(getCurrType());
            setCurrMethod(lCurrMethod);

            createMethodParameters();
            handleMethodReturnType();
        } catch (NullPointerException e) {
            sLogger.error("Error processing method declaration in type " 
                    + (getCurrType() != null ? getCurrType().getUniqueName() : "<no type>") + "\n" 
                    + getASTNode().toString() 
                    + " - skipping method declaration");
            visitChildren = false;
        }

        return visitChildren;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void endVisit() {
        setCurrMethodFromMethodReminder();
    }

    /**
     * Computes and sets the return type class of a method declaration.
     */
    private void handleMethodReturnType() {
        if (!getASTNode().isConstructor()) {
            ITypeBinding returnTypeBinding = getASTNode().getReturnType2().resolveBinding();
            FamixClass lReturnType = getClass(returnTypeBinding, getASTNode().getReturnType2(), false);
            lReturnType = (FamixClass) getModel().addElement(lReturnType);
            getCurrMethod().setDeclaredReturnClass(lReturnType);
        }
    }

    /**
     * Creates and adds the formal parameters to the current method object.
     */
    private void createMethodParameters() {
        int position = 0;
        for (Object lParamDeclaration : getASTNode().parameters()) {
            String lParamID =
                    getCurrMethod().getUniqueName() + AbstractFamixEntity.NAME_DELIMITER
                            + ((SingleVariableDeclaration) lParamDeclaration).getName();
            FamixParameter lParameter = getFactory().createFormalParameter(lParamID, getCurrMethod(), position);
            if (getModel().contains(lParameter)) {
                sLogger.error("Formal parameter " + lParamID + " already exists - skip it");
            } else {
                lParameter = (FamixParameter) getModel().addElement(lParameter);
                lParameter.setModifiers(((SingleVariableDeclaration) lParamDeclaration).getModifiers());
                lParameter.setSourceAnchor(getSourceAnchor((SingleVariableDeclaration) lParamDeclaration));
                getCurrMethod().getParameters().add(lParameter);

                // set data type
                ITypeBinding pBinding = ((SingleVariableDeclaration) lParamDeclaration).getType().resolveBinding();
                FamixClass lDataType = getClass(pBinding, ((SingleVariableDeclaration) lParamDeclaration).getType(), false);
                lDataType = (FamixClass) getModel().addElement(lDataType);
                lParameter.setDeclaredClass(lDataType);

                position++;
            }
        }
    }

    /**
     * Compute method signature from method declaration node. Parameter types are converted manually either using the
     * resolved binding name or the unresoved type name. Array types are treated differently, because they are part of
     * the method signature and not only of tyep ARRAY.
     * 
     * @return The FAMIX method.
     */
    @SuppressWarnings("unchecked")
    private FamixMethod createMethod() {
        StringBuilder lMethodID = new StringBuilder();
        
        lMethodID.append(getCurrType().getUniqueName() + AbstractFamixEntity.NAME_DELIMITER);
        if (getASTNode().isConstructor()) {
            lMethodID.append(AbstractFamixEntity.CONSTRUCTOR_PREFIX);
        } else {
            lMethodID.append(getASTNode().getName().getIdentifier());
        }
        lMethodID.append(AbstractFamixEntity.METHOD_START_BRACE);

        List<SingleVariableDeclaration> parameters = getASTNode().parameters();
        for (int i = 0; i < parameters.size(); i++) {
            SingleVariableDeclaration parameter = parameters.get(i);
            IVariableBinding lVariableBinding = parameter.resolveBinding();
            if (lVariableBinding != null) {
                lMethodID.append(convertParameterTypeBindingForMethodSignature(lVariableBinding.getType()));
            } else {
                lMethodID.append(convertParameterTypeForMethodSignature(parameter));
            }
            if (i < parameters.size() - 1) {
                lMethodID.append(AbstractFamixEntity.METHOD_PARAMETER_SEPARATOR);
            }
        }
        lMethodID.append(AbstractFamixEntity.METHOD_END_BRACE);

        return createMethodFromSignature(lMethodID.toString());
    }

    /**
     * Get the data type name of an "unresolved" parameter in a method declaration node.
     * 
     * @param parameter
     * @return
     */
    private String convertParameterTypeForMethodSignature(SingleVariableDeclaration parameter) {
        String lClassID = null;
        if (parameter.getType().isArrayType()) {
            StringBuilder arrayString = new StringBuilder();
            ArrayType arrayType = (ArrayType) parameter.getType();
            lClassID = convert(arrayType.getElementType());
            if (arrayType.getDimensions() == 0) {
                arrayString.append("[]");
            } else {
                for (int i = 0; i < arrayType.getDimensions(); i++) {
                    arrayString.append("[]");
                }
            }
            lClassID += arrayString.toString();
        } else {
            lClassID = convert(parameter.getType());
        }
        return lClassID;
    }
}
