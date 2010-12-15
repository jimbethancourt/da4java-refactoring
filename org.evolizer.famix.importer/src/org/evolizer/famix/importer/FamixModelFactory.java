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
package org.evolizer.famix.importer;

import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.AbstractFamixVariable;
import org.evolizer.famix.model.entities.FamixAccess;
import org.evolizer.famix.model.entities.FamixAttribute;
import org.evolizer.famix.model.entities.FamixCastTo;
import org.evolizer.famix.model.entities.FamixCheckInstanceOf;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixInheritance;
import org.evolizer.famix.model.entities.FamixInvocation;
import org.evolizer.famix.model.entities.FamixLocalVariable;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.entities.FamixPackage;
import org.evolizer.famix.model.entities.FamixParameter;
import org.evolizer.famix.model.entities.FamixSubtyping;

/**
 * Factory to create FAMIX entities and associations. Whenever new FAMIX types and associations are added a
 * corresponding create method needs to be implemented here.
 * 
 * @author pinzger
 */
public class FamixModelFactory {
    /**
     * Creates a FAMIX FamixPackage instance.
     * 
     * @param uniqueName Unique name of the package (full package name).
     * @param parent Parent package (optional).
     * @return FamixPackage.
     */
    public FamixPackage createPackage(String uniqueName, FamixPackage parent) {
        return new FamixPackage(uniqueName, parent);
    }

    /**
     * Creates a FAMIX FamixClass instance.
     * 
     * If the class is a top-level class the name is: full package name + class name. If the class is an inner class the
     * name is: full parent class name + class name. If the class is an anonymous class the name is: full name of the
     * method + anonymous class name.
     * 
     * @param uniqueName Unique name of the class (see above).
     * @param parent FamixPackage, class, or method containing the class (optional).
     * @return FamixClass.
     */
    public FamixClass createClass(String uniqueName, AbstractFamixEntity parent) {
        return new FamixClass(uniqueName, parent);
    }

    /**
     * Creates a FAMIX FamixMethod instance.
     * 
     * @param uniqueName Unique name of the method (full class name + method name + '(' + parameter types + ')'.
     * @param parent FamixClass declaring the method (optional).
     * @return FamixMethod.
     */
    public FamixMethod createMethod(String uniqueName, AbstractFamixEntity parent) {
        return new FamixMethod(uniqueName, parent);
    }

    /**
     * Creates a FAMIX FamixAttribute instance.
     * 
     * @param uniqueName Unique name of attribute (full class name + attribute name).
     * @param parent FamixClass declaring the attribute (optional).
     * @return FamixAttribute.
     */
    public FamixAttribute createAttribute(String uniqueName, AbstractFamixEntity parent) {
        return new FamixAttribute(uniqueName, parent);
    }

    /**
     * Creates a FAMIX FamixParameter instance.
     * 
     * @param uniqueName Unique name of formal parameter (full method name + parameter name).
     * @param parent FamixMethod declaring the parameter (optional).
     * @param position Position of the parameter in the method declaration.
     * @return FamixParameter.
     */
    public FamixParameter createFormalParameter(String uniqueName, FamixMethod parent, Integer position) {
        return new FamixParameter(uniqueName, parent, position);
    }

    /**
     * Creates a FAMIX FamixLocalVariable instance.
     * 
     * @param uniqueName Unique name of local variable (full method name + local variable name).
     * @param parent FamixMethod declaring the local variable (optional).
     * @return FamixLocalVariable.
     */
    public FamixLocalVariable createLocalVariable(String uniqueName, FamixMethod parent) {
        return new FamixLocalVariable(uniqueName, parent);
    }

    // associations
    /**
     * Creates a FAMIX FamixInheritance association.
     * 
     * @param subclass Subclass that inherits behaviour.
     * @param superclass Superclass.
     * @return FamixInheritance
     */
    public FamixInheritance createInheritance(FamixClass subclass, FamixClass superclass) {
        return new FamixInheritance(subclass, superclass);
    }

    /**
     * Creates a FAMIX Sub-typing association.
     * 
     * @param subclass FamixClass that is a sub-type of super-type.
     * @param superclass Super-type.
     * @return Sub-typing.
     */
    public FamixSubtyping createSubtyping(FamixClass subclass, FamixClass superclass) {
        return new FamixSubtyping(subclass, superclass);
    }

    /**
     * Creates a FAMIX FamixInvocation association.
     * 
     * @param caller Methods that calls a method.
     * @param callee Called method.
     * @return FamixInvocation.
     */
    public FamixInvocation createInvocation(FamixMethod caller, FamixMethod callee) {
        return new FamixInvocation(caller, callee);
    }

    /**
     * Creates a FAMIX FamixAccess association.
     * 
     * @param method FamixMethod that access an attribute.
     * @param variable FamixAttribute that is accessed.
     * @return FamixAccess.
     */
    public FamixAccess createAccess(FamixMethod method, AbstractFamixVariable variable) {
        return new FamixAccess(method, variable);
    }

    /**
     * Creates a FAMIX FamixCastTo association.
     * 
     * @param method FamixMethod that contains the cast expression.
     * @param castTo Type which the object/class is casted to.
     * @return FamixCastTo.
     */
    public FamixCastTo createCastTo(FamixMethod method, FamixClass castTo) {
        return new FamixCastTo(method, castTo);
    }

    /**
     * Creates a FAMIX FamixCheckInstanceOf association.
     * 
     * @param method FamixMethod that contains the instanceof expression.
     * @param type Type against which the object is checked.
     * @return FamixCheckInstanceOf.
     */
    public FamixCheckInstanceOf createCheckInstanceOf(FamixMethod method, FamixClass type) {
        return new FamixCheckInstanceOf(method, type);
    }
}
