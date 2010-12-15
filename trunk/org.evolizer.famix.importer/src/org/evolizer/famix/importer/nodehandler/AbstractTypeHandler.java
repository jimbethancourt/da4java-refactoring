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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.evolizer.famix.importer.ASTCrawler;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.importer.unresolved.UnresolvedMethodInvocation;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.AbstractFamixVariable;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.entities.FamixPackage;
import org.evolizer.famix.model.entities.SourceAnchor;

/**
 * Abstract super class for handling type declaration statements. Currently, FAMIX Importer supports class, interface,
 * inner class, and anonymous class declarations. Parameterized class declarations are not supported, yet.
 * 
 * @author pinzger
 */
public abstract class AbstractTypeHandler extends AbstractASTNodeHandler implements ITypeDeclarationHandler {
    /**
     * The Logger.
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(AbstractTypeHandler.class.getName());

    /**
     * The binding of the declared type.
     */
    private ITypeBinding fTypeBinding;

    /**
     * The constructor.
     * 
     * @param crawler Instance of ASTCrawler to obtain current type and method information.
     */
    public AbstractTypeHandler(ASTCrawler crawler) {
        super(crawler);
    }

    /**
     * Add links to parent entities (file, package, class)
     */
    public void addParentLink() {
        if (getTypeBinding().isNested()) {
            FamixClass cl = getCurrTypeFromTypeReminder();
            cl.getInnerClasses().add(getCurrType());
            getCurrType().setParent(cl);
        } else {
            addClassToPackage();
        }
    }
    
    /**
     * Add the remaining attribute definitions and try to solve unresolved calls occurring in a Java compilation unit.
     */
    protected void addFieldsToUnresolvedInvocationsInScope() {
        for (FamixMethod caller : getUnresolvedCalls().keySet()) {
            // check if current type contains the caller method
            if ((caller.getSourceAnchor().getStartPos() >= getASTNode().getStartPosition())
                    && (caller.getSourceAnchor().getEndPos() <= (getASTNode().getStartPosition() + getASTNode()
                            .getLength()))) {
                List<UnresolvedMethodInvocation> invocations = getUnresolvedCalls().get(caller);
                for (UnresolvedMethodInvocation unresolvedMethodInvocation : invocations) {
                    unresolvedMethodInvocation.addAndNotOverrideVariables(new HashSet<AbstractFamixVariable>(getCurrType()
                            .getAttributes()));
                }
            }
        }
    }

    /**
     * Sets the type binding.
     * 
     * @param typeBinding The type binding.
     */
    protected void setTypeBinding(ITypeBinding typeBinding) {
        fTypeBinding = typeBinding;
    }

    /**
     * Returns the type binding.
     * 
     * @return The type binding.
     */
    protected ITypeBinding getTypeBinding() {
        return fTypeBinding;
    }
    
    /**
     * Add a default constructor if no constructor is defined in the class. 
     */
    protected void createDefaultConstructor() {
        FamixClass currentType = getCurrType();
        
        // 
        if (!existsAConstructor()) {
            StringBuffer defaultConstructorId = new StringBuffer(); 
            defaultConstructorId.append(currentType.getUniqueName()); 
            defaultConstructorId.append(AbstractFamixEntity.NAME_DELIMITER);
            defaultConstructorId.append(AbstractFamixEntity.CONSTRUCTOR_PREFIX); 
            defaultConstructorId.append(AbstractFamixEntity.METHOD_START_BRACE); 
            defaultConstructorId.append(AbstractFamixEntity.METHOD_END_BRACE);
            
            FamixMethod defaultConstructor = getFactory().createMethod(defaultConstructorId.toString(), currentType);
            defaultConstructor = (FamixMethod) getModel().addElement(defaultConstructor);
    
            // if the not explicitly defined default constructor has already been called and is in
            // the model it does not have a parent yet
            if (defaultConstructor.getParent() == null) {
                defaultConstructor.setParent(currentType);
            }
            currentType.getMethods().add(defaultConstructor);
        }
    }

    /**
     * Check whether the class contains a contructor.
     * 
     * @return true, if a constructor exists.
     */
    protected boolean existsAConstructor() {
        boolean constructorExists = false;
        Set<FamixMethod> methodsOfCurrentType = getCurrType().getMethods();
        
        String constructorCheckStr = getCurrMethod().getUniqueName() 
            + AbstractFamixEntity.NAME_DELIMITER
            + AbstractFamixEntity.CONSTRUCTOR_PREFIX
            + AbstractFamixEntity.METHOD_START_BRACE;
        
        for (FamixMethod method : methodsOfCurrentType) {
            if (method.getUniqueName().startsWith(constructorCheckStr)) {
                constructorExists = true;
                break;
            }
        }

        return constructorExists;
    }
    
    /**
     * Add current type to parent package.
     */
    private void addClassToPackage() {
        String lParentPackageID = getTypeBinding().getPackage().getName();
        if (lParentPackageID.equals("")) {
            lParentPackageID = AbstractFamixEntity.DEFAULT_PACKAGE_NAME;
        }
        FamixPackage lParentPackageHelper = getFactory().createPackage(lParentPackageID, null);
        if (getModel().contains(lParentPackageHelper)) {
            FamixPackage lParentPackage = (FamixPackage) getModel().getElement(lParentPackageHelper);
            lParentPackage.getClasses().add(getCurrType());
            getCurrType().setParent(lParentPackage);
        } else {
            sLogger.warn("No parent package defined for class " + getCurrType().getUniqueName());
        }
    }

    /**
     * Adds an object init method to the FAMIX class pClass. The object init method contains the attribute
     * initialization relationships.
     * 
     * @param pClass
     *            Famix class that contains the object init method.
     * @return The object init FAMIX method.
     */
    protected FamixMethod addObjectInitMethod(FamixClass pClass) {
        String lMethodID = pClass.getUniqueName() + AbstractFamixEntity.NAME_DELIMITER + AbstractFamixEntity.OBJECT_INIT_METHOD;
        FamixMethod lObjectInitMethod = getFactory().createMethod(lMethodID, null);
        lObjectInitMethod = (FamixMethod) getModel().addElement(lObjectInitMethod);
        lObjectInitMethod.setModifiers(AbstractFamixEntity.UNKOWN_OR_NO_MODIFIERS);
        lObjectInitMethod.setSourceAnchor(new SourceAnchor(pClass.getSourceAnchor().getFile(), pClass.getSourceAnchor()
                .getStartPos(), pClass.getSourceAnchor().getEndPos()));
        lObjectInitMethod.setParent(pClass);
        pClass.getMethods().add(lObjectInitMethod);

        // initialize unresolved method hash of the oinit FamixMethod
        // getUnresolvedCalls().put(lObjectInitMethod, new LinkedList<UnresolvedMethodInvocation>());

        return lObjectInitMethod;
    }
}
