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

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.evolizer.famix.importer.FamixImporterPlugin;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.entities.FamixModel;

/**
 * Create invocation relationships from the list of unresolved calls and add them to the FAMIX model. We currently
 * handle exact matches, multiple matches, and unresolved calls with no match.
 * 
 * In case of an unresolved anonymous class instance creation statement the class is added to the model.
 * 
 * @author pinzger
 */
public class UnresolvedInvocationHandler {

    /**
     * The sLogger, also used in the sub-classes
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(UnresolvedInvocationHandler.class.getName());

    /**
     * The FAMIX model to which unresolved calls are added
     */
    private FamixModel fModel;
    /**
     * Map of FAMIX FamixMethod to its unresolved calls
     */
    private Hashtable<FamixMethod, List<UnresolvedMethodInvocation>> fUnresolvedCalls;

    /**
     * The constructor
     * 
     * @param model
     *            The FAMIX model
     * @param unresolvedCalls
     *            The map of the FAMIX FamixMethod and its unresolved calls
     */
    public UnresolvedInvocationHandler(FamixModel model, Hashtable<FamixMethod, List<UnresolvedMethodInvocation>> unresolvedCalls) {
        setModel(model);
        setUnresolvedCalls(unresolvedCalls);
    }

    /**
     * Processes all unresolved calls to determine potential callee methods from the set of unresolved method
     * declarations. These are method declarations within the source code that could not be resolved (i.e., a parameter
     * type could not be resolved).
     * 
     * @param monitor Instance of the progress monitor
     * @return Status OK, if no errors have been encountered
     */
    public IStatus process(IProgressMonitor monitor) {
        IStatus status = Status.OK_STATUS;
        int tick = 10;
        SubMonitor progress = SubMonitor.convert(monitor, tick * getUnresolvedCalls().keySet().size());
        progress.setTaskName("Resolve method calls");

        try {
            List<FamixMethod> unresolvedMethodDeclarations = getUnresolvedMethodDelcarations();
            for (FamixMethod method : getUnresolvedCalls().keySet()) {
                for (UnresolvedMethodInvocation unresolvedMethodInvocation : getUnresolvedCalls().get(method)) {
                    unresolvedMethodInvocation.matchWithUnresolvedMethodDeclarations(unresolvedMethodDeclarations);
                }

                if (progress.isCanceled()) {
                    status = Status.CANCEL_STATUS;
                    break;
                }
                progress.worked(tick);
            }
        } finally {
            if (monitor != null) {
                monitor.done();
            }
        }

        return status;
    }

    /**
     * Traverse over the set of unresolved method calls and adds them to the FAMIX model. Addition is done in the
     * following way:
     * <ul>
     * <li>One match: add this call to the model as is.
     * <li>Multiple matches: get inheritance hierarchy of call receiving object/class and select the callee method whose
     * declaring type appears first in this hierarchy.
     * <li>No match: create the callee method and add the call to the model.
     * </ul>
     * 
     * In case of an anonymous class the corresponding class is also added to the model.
     * 
     * @param monitor Instance of the progress monitor
     * @return OK, if the process has not been canceled by the user.
     */
    public IStatus addInvocations(IProgressMonitor monitor) {
        IStatus status = Status.OK_STATUS;
        int tick = 10;
        SubMonitor progress = SubMonitor.convert(monitor, tick * getUnresolvedCalls().keySet().size());
        progress.setTaskName("Add resolved calls ...");

        try {
            for (FamixMethod method : getUnresolvedCalls().keySet()) {
                for (UnresolvedMethodInvocation unresolvedMethodInvocation : getUnresolvedCalls().get(method)) {
                    FamixMethod calleeMethod = null;
                    Set<FamixMethod> matches = unresolvedMethodInvocation.getMatchesByAllParametersType();
                    if (matches.size() == 1) {
                        calleeMethod = matches.iterator().next();
                    } else if (matches.size() > 1) {
                        sLogger.warn("Multiple matches for statment " + unresolvedMethodInvocation.getStatement()
                                + " in method " + method.getUniqueName());
                        calleeMethod = selectCalleeFromMultipleMatches(unresolvedMethodInvocation, matches);
                    } else {
                        sLogger.warn("No callee method matched for call " + unresolvedMethodInvocation.getStatement()
                                + " in method: " + method.getUniqueName());
                        calleeMethod = unresolvedMethodInvocation.createMethodFromSignature();
                        calleeMethod = (FamixMethod) getModel().addElement(calleeMethod);
                    }

                    if (calleeMethod != null) {
                        FamixAssociation invocation =
                                unresolvedMethodInvocation.getCrawler().getFactory().createInvocation(
                                        method,
                                        calleeMethod);
                        invocation.setStatement(unresolvedMethodInvocation.getStatement());
                        invocation.setSourceAnchor(unresolvedMethodInvocation.getInvocationHandler().getSourceAnchor());
                        getModel().addRelation(invocation);

                        // add the corresponding constructor to the anonymous class (only for anonymous classes)
                        if (unresolvedMethodInvocation instanceof UnresolvedAnonymousClassInstanceCreationInvocation) {
                            addAnonymousClass(unresolvedMethodInvocation, calleeMethod);
                        }
                    }
                }

                if (progress.isCanceled()) {
                    status = Status.CANCEL_STATUS;
                    break;
                }
                progress.worked(tick);
            }
        } finally {
            if (monitor != null) {
                monitor.done();
            }
        }

        return status;
    }

    /**
     * Use the resolved binding of the anonymous class or otherwise determine the unique name of the class from the
     * class instance creation statement.
     * 
     * @param unresolvedMethodInvocation
     * @param calleeMethod
     */
    private void addAnonymousClass(UnresolvedMethodInvocation unresolvedMethodInvocation, FamixMethod calleeMethod) {
        ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) unresolvedMethodInvocation.getASTNode();
        // determine the anonymous class from the string
        FamixClass lDeclaringClass = null;
        ITypeBinding lTypeBinding = classInstanceCreation.resolveTypeBinding();
        if (lTypeBinding != null) {
            lDeclaringClass = unresolvedMethodInvocation.getInvocationHandler().getClass(lTypeBinding, null, true);
        } else {
            String lClassID = calleeMethod.getUniqueName();
            lClassID = lClassID.substring(0, lClassID.indexOf(AbstractFamixEntity.CONSTRUCTOR_PREFIX) - 1);
            lDeclaringClass = unresolvedMethodInvocation.getCrawler().getFactory().createClass(lClassID, null);
        }
        if (lDeclaringClass != null) {
            lDeclaringClass = (FamixClass) getModel().addElement(lDeclaringClass);
            lDeclaringClass.getMethods().add(calleeMethod);
            calleeMethod.setParent(lDeclaringClass);
        }
    }

    /**
     * Select the callee method of the type that comes first in the subtyping hierarchy of the call receiving
     * object/class.
     * 
     * @param unresolvedMethodInvocation
     * @param matches
     * @return The callee method.
     */
    private FamixMethod selectCalleeFromMultipleMatches(
            UnresolvedMethodInvocation unresolvedMethodInvocation,
            Set<FamixMethod> matches) {
        FamixMethod calleeMethod = null;

        FamixClass receiverClass =
                (FamixClass) getModel().getElement(
                        unresolvedMethodInvocation.getFactory().createClass(
                                unresolvedMethodInvocation.getResolvedID(),
                                null));
        List<FamixClass> superTypes = new LinkedList<FamixClass>();
        if (receiverClass != null) {
            List<FamixClass> subTypes = new LinkedList<FamixClass>();
            subTypes.add(receiverClass);
            superTypes.add(receiverClass); // also include the sub-class
            superTypes.addAll(unresolvedMethodInvocation.computeSuperTypes(subTypes));
        }

        for (FamixClass type : superTypes) {
            for (FamixMethod match : matches) {
                if ((match.getParent() != null) && match.getParent().equals(type)) {
                    // select the first match
                    calleeMethod = match;
                    break;
                }
            }
            if (calleeMethod != null) {
                break;
            }
        }
        return calleeMethod;
    }

    private List<FamixMethod> getUnresolvedMethodDelcarations() {
        // focus on the calls that are to methods in the code
        List<FamixMethod> unresolvedMethodDelcarations = new LinkedList<FamixMethod>();
        for (AbstractFamixEntity entity : getModel().getFamixEntities()) {
            if (entity instanceof FamixMethod) {
                FamixMethod method = (FamixMethod) entity;
                // if (method.getUniqueName().contains(AbstractASTNodeHandler.UNDEFINED_BINDING)) {
                if (method.getParent() != null) {
                    unresolvedMethodDelcarations.add(method);
                }
            }
        }

        return unresolvedMethodDelcarations;
    }

    /**
     * Sets the map of FAMIX FamixMethod and its list of unresolved calls.
     * 
     * @param unresolvedCalls
     *            the map of unresolved calls
     */
    public void setUnresolvedCalls(Hashtable<FamixMethod, List<UnresolvedMethodInvocation>> unresolvedCalls) {
        fUnresolvedCalls = unresolvedCalls;
    }

    /**
     * Returns the table of unresolved calls.
     * 
     * @return The table of unresolved calls.
     */
    public Hashtable<FamixMethod, List<UnresolvedMethodInvocation>> getUnresolvedCalls() {
        return fUnresolvedCalls;
    }

    /**
     * Sets the FAMIX model to add the resolved calls.
     * 
     * @param model
     *            The FAMIX model.
     */
    public void setModel(FamixModel model) {
        fModel = model;
    }

    /**
     * Returns the FAMIX model instance.
     * 
     * @return The FAMIX model.
     */
    public FamixModel getModel() {
        return fModel;
    }
}
