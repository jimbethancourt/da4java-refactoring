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
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAttribute;
import org.evolizer.famix.model.entities.FamixClass;


/**
 * Handles enum constant declarations. For each enum constant a static final attribute
 * is added to the enum FAMIX class entity.
 *
 * @author pinzger
 *
 */
public class EnumConstantDeclarationHandler extends AbstractASTNodeHandler {
    /**
     * The Logger.
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(VariableDeclarationFragmentHandler.class.getName());

    /**
     * The declared data type.
     */
    private FamixClass fDataType;

    /**
     * The constructor.
     * 
     * @param crawler   The ASTCrawler instance.
     */
    public EnumConstantDeclarationHandler(ASTCrawler crawler) {
        super(crawler);
        fDataType = null;
    }

    /**
     * Helper method for returning the enums constant declaration AST node.
     * 
     * @see org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler#getASTNode()
     * 
     * @return The enum constant declaration AST node.
     */
    @Override
    public EnumConstantDeclaration getASTNode() {
        return (EnumConstantDeclaration) super.getASTNode();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean visit(ASTNode node) {
        boolean visitChildren = true;

        setASTNode(node);
        
        try {
            initDataType();

            createAttributes();
        } catch (NullPointerException e) {
            sLogger.error("Error processing enum constant declaration fragment in enum type " 
                    + (getCurrType() != null ? getCurrType().getUniqueName() : "<no type>") + "\n" 
                    + getASTNode().toString() 
                    + " - skipping enum constant declaration fragment");
            visitChildren = false;
        }
        
        return visitChildren;
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void endVisit() {
        // not used
    }
    
    private void initDataType() {
//        IVariableBinding pBinding = getASTNode().resolveVariable();
//        ITypeBinding lTypeBinding = pBinding.getType();
        setDataType(getCurrType());
//        getModel().addElement(getDataType());
    }

    private void createAttributes() {
        FamixAttribute lField = null;
        
        IVariableBinding lVariableBinding = getASTNode().resolveVariable();
        if (lVariableBinding != null) {
            String lFieldID = null;
            int lModifiers = lVariableBinding.getModifiers();
            String lSimpleName = lVariableBinding.getName();
            lFieldID = getCurrType().getUniqueName() + AbstractFamixEntity.NAME_DELIMITER + lSimpleName;
            
            lField = getFactory().createAttribute(lFieldID, null);
            lField = (FamixAttribute) getModel().addElement(lField);
            lField.setModifiers(lModifiers);
            lField.setSourceAnchor(getSourceAnchor());
            lField.setParent(getCurrType());
            getCurrType().getAttributes().add(lField);
            lField.setDeclaredClass(getDataType());
        } else {
            sLogger.error("Could not create attribute from enum constant declaration " + getASTNode().toString());
        }
    }
    
    /**
     * Sets the data type.
     * 
     * @param dataType The data type.
     */
    protected void setDataType(FamixClass dataType) {
        fDataType = dataType;
    }

    /**
     * Returns the data type.
     * 
     * @return The data type.
     */
    public FamixClass getDataType() {
        return fDataType;
    }
}
