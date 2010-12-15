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
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixAttribute;

/**
 * Abstract class for handling AST nodes representing read/write access to class attributes. Currently, the FAMIX
 * importer distinguishes between accesses via: <code>this.field</code> (FieldAccess) <code>super.field</code>
 * (SuperFieldAccess), <code>package.class.field</code> (QualifiedName) and <code>field</code> (SimpleName). The various
 * ways to resolve the bindings are implemented in corresponding sub-classes.
 * 
 * @author pinzger
 */
public abstract class AbstractAccessHandler extends AbstractASTNodeHandler {
    /**
     * The Logger.
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(AbstractAccessHandler.class.getName());

    private FamixAttribute fField;

    /**
     * The constructor.
     * 
     * @param crawler Instance of the AST Crawler to obtain current type and method information.
     */
    public AbstractAccessHandler(ASTCrawler crawler) {
        super(crawler);
        fField = null;
    }

    /**
     * Template method for handling read/write accesses to fields. 
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
            createAttributeFromAccess();
    
            if ((getField() != null) && (getCurrMethod() != null)) {
                FamixAssociation access = getFactory().createAccess(getCurrMethod(), getField());
                access.setSourceAnchor(getSourceAnchor());
                getModel().addRelation(access);
            }
        } catch (NullPointerException e) {
            sLogger.error("Error processing variable access in method " 
                    + (getCurrMethod() != null ? getCurrMethod().getUniqueName() : "<no method>") + "\n" 
                    + getASTNode().toString() 
                    + " - skipping access");
            visitChildren = false;
        }

        return visitChildren;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void endVisit() {
    // currently not used
    }

    /**
     * Resolve the binding of AST nodes representing read/write accesses to class attributes. The various ways are
     * implemented in corresponding sub-classes.
     * 
     * @return The FAMIX conform unique name.
     */
    protected abstract String convertFieldID();

    /**
     * Create missing fields
     */
    protected void createAttributeFromAccess() {
        String lFieldID = convertFieldID();
        if (lFieldID != null) {
            FamixAttribute lField = getFactory().createAttribute(lFieldID, null);
            lField = (FamixAttribute) getModel().addElement(lField);
            setField(lField);
        }
    }

    /**
     * Sets the accessed FAMIX attribute.
     * 
     * @param field The accessed FAMIX attribute.
     */
    protected void setField(FamixAttribute field) {
        fField = field;
    }

    /**
     * Returns the accessed FAMIX attribute.
     * 
     * @return The accessed FAMIX attribute.
     */
    public FamixAttribute getField() {
        return fField;
    }
}
