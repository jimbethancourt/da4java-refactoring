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
package org.evolizer.famix.model;

import org.evolizer.core.hibernate.model.api.IEvolizerModelProvider;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.AbstractFamixGeneralization;
import org.evolizer.famix.model.entities.AbstractFamixObject;
import org.evolizer.famix.model.entities.AbstractFamixVariable;
import org.evolizer.famix.model.entities.FamixAccess;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixAttribute;
import org.evolizer.famix.model.entities.FamixCastTo;
import org.evolizer.famix.model.entities.FamixCheckInstanceOf;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixInheritance;
import org.evolizer.famix.model.entities.FamixInvocation;
import org.evolizer.famix.model.entities.FamixLocalVariable;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.entities.FamixModel;
import org.evolizer.famix.model.entities.FamixPackage;
import org.evolizer.famix.model.entities.FamixParameter;
import org.evolizer.famix.model.entities.FamixSubtyping;
import org.evolizer.famix.model.entities.SourceAnchor;

/**
 * The Evolizer model provider for the FAMIX model classes.
 * 
 * @author pinzger
 */
public class EvolizerFamixModelProvider implements IEvolizerModelProvider {

    /** 
     * {@inheritDoc}
     */
    public Class<?>[] getAnnotatedClasses() {
        Class<?>[] annotatedClasses =
                {
                        FamixModel.class,
                        FamixAccess.class,
                        FamixAssociation.class,
                        FamixAttribute.class,
                        FamixCastTo.class,
                        FamixCheckInstanceOf.class,
                        FamixClass.class,
                        AbstractFamixEntity.class,
                        AbstractFamixObject.class,
                        FamixParameter.class,
                        AbstractFamixGeneralization.class,
                        FamixInheritance.class,
                        FamixInvocation.class,
                        FamixLocalVariable.class,
                        FamixMethod.class,
                        FamixPackage.class,
                        SourceAnchor.class,
                        AbstractFamixVariable.class,
                        FamixSubtyping.class};

        return annotatedClasses;
    }

}
