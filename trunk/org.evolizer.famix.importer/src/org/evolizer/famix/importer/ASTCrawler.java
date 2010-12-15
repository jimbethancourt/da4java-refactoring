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

import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.evolizer.famix.importer.nodehandler.AbstractASTNodeHandler;
import org.evolizer.famix.importer.nodehandler.AnonymTypeHandler;
import org.evolizer.famix.importer.nodehandler.CastExpressionHandler;
import org.evolizer.famix.importer.nodehandler.ClassInstanceCreationHandler;
import org.evolizer.famix.importer.nodehandler.ConstructorInvocationHandler;
import org.evolizer.famix.importer.nodehandler.EnumConstantDeclarationHandler;
import org.evolizer.famix.importer.nodehandler.EnumDeclarationHandler;
import org.evolizer.famix.importer.nodehandler.FieldAccessHandler;
import org.evolizer.famix.importer.nodehandler.FieldDeclarationHandler;
import org.evolizer.famix.importer.nodehandler.InstanceofExpressionHandler;
import org.evolizer.famix.importer.nodehandler.MethodDeclarationHandler;
import org.evolizer.famix.importer.nodehandler.MethodInvocationHandler;
import org.evolizer.famix.importer.nodehandler.NameAccessHandler;
import org.evolizer.famix.importer.nodehandler.SuperConstructorInvocationHandler;
import org.evolizer.famix.importer.nodehandler.SuperFieldAccessHandler;
import org.evolizer.famix.importer.nodehandler.SuperMethodInvocationHandler;
import org.evolizer.famix.importer.nodehandler.TypeDeclarationHandler;
import org.evolizer.famix.importer.nodehandler.VariableDeclarationFragmentHandler;
import org.evolizer.famix.importer.unresolved.UnresolvedMethodInvocation;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixLocalVariable;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.entities.FamixModel;

/**
 * The main class for extracting the FAMIX source model from an Eclipse Java compilation unit. The crawler first creates
 * the AST using the jdt ASTParser and then traverses it with the visit methods of ASTCrawerl. For each AST node of
 * interest it delegates the extraction to a corresponding node handler object. The node handler objects create the
 * FAMIX entities and associations using the given FamixModelFactory. Extracted facts are added to the
 * FamixModel object. The FamixModel object contains the resulting FAMIX model entities and relationships that after finishing the
 * extraction can be accesses by the user.
 * 
 * For nesting of Java classes and consequently methods it uses a stack. Unresolved method calls are stored and can also
 * be accessed after finishing the extraction process.
 * 
 * In the current version we extract the following FAMIX entities:
 * <ul>
 * <li>FamixClass/Interface/Enum from TypeDeclaration and AnonymousClassDeclaration node - Template classes are also handled
 * <li>FamixMethod from MethodDeclaration node
 * <li>FamixAttribute from FieldDeclaration node
 * <li>FamixParameter from MethodDeclaration node
 * <li>FamixLocalVariable from VariableDeclarationStatement, VariableDeclarationFragment, and SingleVariableDeclaration node
 * </ul>
 * 
 * The following FAMIX associations (cross reference information) between entities are extracted:
 * <ul>
 * <li>FamixInheritance from TypeDeclaration and AnonymousClassDeclaration node
 * <li>Sub-typing from TypeDeclaration and AnonymousClassDeclaration node
 * <li>FamixInvocation from MethodInvocation, SuperMethodInvocation, ConstructorInvocation, SuperConstructorInvocation,
 * ClassInstanceCreation node
 * <li>FamixAccess from FieldAccess, SuperFieldAccess, QualifiedName, SimpleName
 * <li>FamixCheckInstanceOf from InstanceofExpression node
 * <li>FamixCastTo from CastExpression node
 * </ul>
 * 
 * Entities and associations are stored to the FamixModel object that can be retrieved by the user of the ASTCrawler.
 * 
 * The initial version of ASTCrawler was based on the javaDB source code. Many thanks goes to Martin Robillard and Isaac
 * Yuen for providing the javaDB source code.
 * 
 * @author pinzger, giger
 * 
 */
public class ASTCrawler extends ASTVisitor {

    /**
     * Logger
     */
    private static Logger sLogger = FamixImporterPlugin.getLogManager().getLogger(ASTCrawler.class.getName());

    /**
     * The current processed type declaration.
     */
    private FamixClass fCurrType;
    /**
     * Stack for handling nested types (inner classes and anonymous classes).
     */
    private Stack<FamixClass> fCurrTypeReminder;
    /**
     * The currently processed method declaration.
     */
    private FamixMethod fCurrMethod;
    /**
     * Stack for handling methods within nested types.
     */
    private Stack<FamixMethod> fCurrMethodReminder;
    /**
     * Counter for computing the name of anonymous classes when jdt binding resolution fails.
     */
    private Hashtable<FamixClass, Integer> fAnonymClassCounter;
    /**
     * List of unresolved calls (calls whose bindings could not be resolved because the source code is incomplete or
     * libraries are missing).
     */
    private Hashtable<FamixMethod, List<UnresolvedMethodInvocation>> fUnresolvedCalls; // list of anonymous calls

    /**
     * The nesting structure of Java statement blocks.
     */
    private StatementBlock fCurrStatementBlock;
    /**
     * Map for storing the scope of local variable definitions.
     */
    private Hashtable<FamixLocalVariable, StatementBlock> fLocalVariableScope;

    /**
     * The container that holds all the extracted FAMIX entities and associations.
     */
    private FamixModel fModel;
    /**
     * The factory to use for creating FAMIX entities and associations.
     */
    private FamixModelFactory fFactory;

    /**
     * Map that for each AST node remembers the corresponding handler to process the AST node information.
     */
    private Hashtable<ASTNode, AbstractASTNodeHandler> fNodeHandler;

    /**
     * The current compilation unit used to extract the source code.
     */
    private ICompilationUnit fCurrCompilationUnit;

    /**
     * The constructor
     * 
     * @param pModel
     *            The model container to use for storing the FAMIX entities and associations.
     * @param factory
     *            The factory to use for creating FAMIX entities and associations.
     */
    public ASTCrawler(FamixModel pModel, FamixModelFactory factory) {
        setModel(pModel);
        setFactory(factory);
        
        fCurrStatementBlock = null;
        fLocalVariableScope = null;
    }

    /**
     * Initializes the crawler.
     */
    private void resetASTCrawler() {
        setCurrCompilationUnit(null);
        setCurrType(null);
        setCurrTypeReminder(new Stack<FamixClass>());
        setCurrMethod(null);
        setCurrMethodReminder(new Stack<FamixMethod>());

        setLocalVariableScope(new Hashtable<FamixLocalVariable, StatementBlock>());
        setAnonymClassCounter(new Hashtable<FamixClass, Integer>());
        fNodeHandler = new Hashtable<ASTNode, AbstractASTNodeHandler>();
        fUnresolvedCalls = new Hashtable<FamixMethod, List<UnresolvedMethodInvocation>>();
    }

    private void setCurrCompilationUnit(ICompilationUnit cu) {
        fCurrCompilationUnit = cu;
    }

    /**
     * Creates the AST of a Java compilation unit and starts the traversal.
     * 
     * @param cu
     *            Eclipse Java compilation unit to parse.
     * @param monitor
     *            The progress monitor.
     * @return OK_Status if the parsing was successful otherwise error.
     */
    public IStatus analyze(ICompilationUnit cu, IProgressMonitor monitor) {
        IStatus status = Status.OK_STATUS;

        resetASTCrawler();

        fCurrCompilationUnit = cu;

        ASTParser lParser = ASTParser.newParser(AST.JLS3); // up to J2SE 1.5
        lParser.setSource(cu);
        lParser.setResolveBindings(true);
        CompilationUnit lResult = (CompilationUnit) lParser.createAST(monitor);
        IProblem[] problems = lResult.getProblems();
        if (problems.length > 0) {
            for (IProblem problem : problems) {
                sLogger.warn(problem.getMessage());
            }
        }
        sLogger.debug("Visiting AST of " + cu.getPath());

        // catch all errors
        try {
            lResult.accept(this);
        } catch (IllegalArgumentException ex) {
            sLogger.error("Error during processing AST of " + cu.getPath());
            sLogger.error(ex.fillInStackTrace());
        }
        sLogger.debug("Visiting AST of " + cu.getPath() + " complete");

        if (monitor.isCanceled()) {
            status = Status.CANCEL_STATUS;
        }

        return status;
    }

    /**
     * Alternative method to parse the source code provided as a string. If you use this method, then you still have to
     * attach the source code to the classes.
     * 
     * @param sourceCode
     *            String containing the source code.
     * @param monitor
     *            The progress monitor.
     * @return OK_Status if the parsing was successful otherwise error.
     */
    public IStatus analyze(String sourceCode, IProgressMonitor monitor) {
        IStatus status = Status.OK_STATUS;

        resetASTCrawler();
        ASTParser lParser = ASTParser.newParser(AST.JLS3); // up to J2SE 1.5
        lParser.setSource(sourceCode.toCharArray());
        lParser.setResolveBindings(true);
        CompilationUnit lResult = (CompilationUnit) lParser.createAST(monitor);
        IProblem[] problems = lResult.getProblems();
        if (problems.length > 0) {
            for (IProblem problem : problems) {
                sLogger.warn(problem.getMessage());
            }
        }

        // catch all errors
        try {
            lResult.accept(this);
        } catch (IllegalArgumentException ex) {
            sLogger.error("Error during processing AST");
            sLogger.error(ex.fillInStackTrace());
        }
        sLogger.debug("Visiting AST of complete");

        if (monitor.isCanceled()) {
            status = Status.CANCEL_STATUS;
        }

        return status;
    }

    /** 
     * Handle type declarations.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclaration)
     * 
     * @param typeDeclaration The type declaration AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(TypeDeclaration typeDeclaration) {
        sLogger.debug("Processing type declaration node " + typeDeclaration.getName());

        TypeDeclarationHandler lTypeHandler = new TypeDeclarationHandler(this);

        try {
            lTypeHandler.setSource(fCurrCompilationUnit.getSource());
        } catch (JavaModelException e) {
            e.printStackTrace();
        }

        fNodeHandler.put(typeDeclaration, lTypeHandler);
        return lTypeHandler.visit(typeDeclaration);
    }

    /**
     * Finish type declarations.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.TypeDeclaration)
     * 
     * @param typeDeclaration The type declaration AST node.
     */
    @Override
    public void endVisit(TypeDeclaration typeDeclaration) {
        sLogger.debug("Post processing type declaration node " + typeDeclaration.getName());
        fNodeHandler.get(typeDeclaration).endVisit();
    }

    /** 
     * Handle enum type declarations.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclaration)
     * 
     * @param enumDeclaration The enum type declaration AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(EnumDeclaration enumDeclaration) {
        sLogger.debug("Processing enum declaration node " + enumDeclaration.getName());

        EnumDeclarationHandler lTypeHandler = new EnumDeclarationHandler(this);

        try {
            lTypeHandler.setSource(fCurrCompilationUnit.getSource());
        } catch (JavaModelException e) {
            e.printStackTrace();
        }

        fNodeHandler.put(enumDeclaration, lTypeHandler);
        return lTypeHandler.visit(enumDeclaration);
    }

    /**
     * Finish enum declarations.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.TypeDeclaration)
     * 
     * @param enumDeclaration The enum type declaration AST node.
     */
    @Override
    public void endVisit(EnumDeclaration enumDeclaration) {
        sLogger.debug("Post processing enum declaration node " + enumDeclaration.getName());
        fNodeHandler.get(enumDeclaration).endVisit();
    }
    
    /**
     * Handle enum constant declarations.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.EnumConstantDeclaration)
     * 
     * @param enumConstantDeclaration The enum constant declaration AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(EnumConstantDeclaration enumConstantDeclaration) {
        sLogger.debug("Processing enum constant declaration node " + enumConstantDeclaration.getName());

        AbstractASTNodeHandler lEnumConstantHandler = new EnumConstantDeclarationHandler(this);
        fNodeHandler.put(enumConstantDeclaration, lEnumConstantHandler);
        return lEnumConstantHandler.visit(enumConstantDeclaration);
    }
    
    /**
     * Handle anonymous type declarations.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.AnonymousClassDeclaration)
     * 
     * @param anonymTypeDeclaration The anonymous type declaration AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(AnonymousClassDeclaration anonymTypeDeclaration) {
        sLogger.debug("Processing anonymous class declaration node");

        AbstractASTNodeHandler lTypeHandler = new AnonymTypeHandler(this);
        fNodeHandler.put(anonymTypeDeclaration, lTypeHandler);
        return lTypeHandler.visit(anonymTypeDeclaration);
    }

    /**
     * Finish anonymous class declarations.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.AnonymousClassDeclaration)
     * 
     * @param anonymTypeDeclaration The anonymous type declaration AST node.
     */
    @Override
    public void endVisit(AnonymousClassDeclaration anonymTypeDeclaration) {
        sLogger.debug("Post processing anonymous class declaration node");

        fNodeHandler.get(anonymTypeDeclaration).endVisit();
    }

    /**
     * Handle method declarations.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodDeclaration)
     * 
     * @param methodDeclaration The method declaration AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(MethodDeclaration methodDeclaration) {
        sLogger.debug("Processing method declaration node " + methodDeclaration.getName());

        AbstractASTNodeHandler lMethodHandler = new MethodDeclarationHandler(this);
        fNodeHandler.put(methodDeclaration, lMethodHandler);
        return lMethodHandler.visit(methodDeclaration);
    }

    /**
     * Finish method declarations.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.MethodDeclaration)
     * 
     * @param methodDeclaration The method declaration AST node.
     */
    @Override
    public void endVisit(MethodDeclaration methodDeclaration) {
        sLogger.debug("Post processing method declaration node " + methodDeclaration.getName());
        fNodeHandler.get(methodDeclaration).endVisit();
    }

    /**
     * Handle field declarations.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldDeclaration)
     * 
     * @param fieldDeclaration The field declaration node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(FieldDeclaration fieldDeclaration) {
        sLogger.debug("Processing attribute declaration");

        AbstractASTNodeHandler lFieldHandler = new FieldDeclarationHandler(this);
        fNodeHandler.put(fieldDeclaration, lFieldHandler);
        return lFieldHandler.visit(fieldDeclaration);
    }

    /**
     * Finish field declarations.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.FieldDeclaration)
     * 
     * @param fieldDeclaration The field declaration node.
     */
    @Override
    public void endVisit(FieldDeclaration fieldDeclaration) {
        sLogger.debug("Post processing attribute declaration");
        fNodeHandler.get(fieldDeclaration).endVisit();
    }

    /**
     * Handle local variable declarations.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationStatement)
     * 
     * @param variableDeclarationStatement The local variable declaration AST node.
     * @return true, if child nodes should be visited.
     */
//    @Override
//    public boolean visit(VariableDeclarationStatement variableDeclarationStatement) {
//        sLogger.debug("Processing variable declaration Statement node " + variableDeclarationStatement);
//
//        AbstractASTNodeHandler lVariableDeclarationHandler = new VariableDeclarationStatementHandler(this);
//        fNodeHandler.put(variableDeclarationStatement, lVariableDeclarationHandler);
//        return lVariableDeclarationHandler.visit(variableDeclarationStatement);
//    }

    /**
     * Handle field and local variable declaration fragments (type info is missing).
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationFragment)
     * 
     * @param variableDeclarationFragment The variable declaration fragment AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(VariableDeclarationFragment variableDeclarationFragment) {
        sLogger.debug("Processing variable declaration fragment node " + variableDeclarationFragment.getName());

        AbstractASTNodeHandler lVariableDeclarationHandler = new VariableDeclarationFragmentHandler(this);
        fNodeHandler.put(variableDeclarationFragment, lVariableDeclarationHandler);
        return lVariableDeclarationHandler.visit(variableDeclarationFragment);
    }

    /**
     * Handle single variable declarations (e.g., in catch clauses). 
     * Formal parameters are handled in the method declaration.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SingleVariableDeclaration)
     * 
     * @param singleVariableDeclaration The single variable declaration AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(SingleVariableDeclaration singleVariableDeclaration) {
        sLogger.debug("Processing single variable declaration statement node " + singleVariableDeclaration);

        if (singleVariableDeclaration.getParent() instanceof CatchClause) {
            AbstractASTNodeHandler lVariableDeclarationHandler = new VariableDeclarationFragmentHandler(this);
            fNodeHandler.put(singleVariableDeclaration, lVariableDeclarationHandler);
            return lVariableDeclarationHandler.visit(singleVariableDeclaration);
        }

        return true;
    }

    /**
     * Handle method invocations.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodInvocation)
     * 
     * @param methodInvocation The method invocation AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(MethodInvocation methodInvocation) {
        sLogger.debug("Processing method invocation node");

        AbstractASTNodeHandler lInvocationHandler = new MethodInvocationHandler(this);
        fNodeHandler.put(methodInvocation, lInvocationHandler);
        return lInvocationHandler.visit(methodInvocation);
    }

    /**
     * Handle construction invocations.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ConstructorInvocation)
     * 
     * @param constructorInvocation The constructor invocation AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(ConstructorInvocation constructorInvocation) {
        sLogger.debug("Processing constructor invocation node");

        AbstractASTNodeHandler lInvocationHandler = new ConstructorInvocationHandler(this);
        fNodeHandler.put(constructorInvocation, lInvocationHandler);
        return lInvocationHandler.visit(constructorInvocation);
    }

    /**
     * Handle super method invocations.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SuperMethodInvocation)
     * 
     * @param superMethodInvocation The super method invocation AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(SuperMethodInvocation superMethodInvocation) {
        sLogger.debug("Processing super method invocation node");

        AbstractASTNodeHandler lInvocationHandler = new SuperMethodInvocationHandler(this);
        fNodeHandler.put(superMethodInvocation, lInvocationHandler);
        return lInvocationHandler.visit(superMethodInvocation);
    }

    /**
     * Handle super constructor invocations.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SuperConstructorInvocation)
     * 
     * @param superConstructorInvocation The super constructor invocation AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(SuperConstructorInvocation superConstructorInvocation) {
        sLogger.debug("Processing super constructor invocation node");

        AbstractASTNodeHandler lInvocationHandler = new SuperConstructorInvocationHandler(this);
        fNodeHandler.put(superConstructorInvocation, lInvocationHandler);
        return lInvocationHandler.visit(superConstructorInvocation);
    }

    /** 
     * Handle class instance creation statements (i.e., new).
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ClassInstanceCreation)
     * 
     * @param classInstanceCreation The class instance creation AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(ClassInstanceCreation classInstanceCreation) {
        sLogger.debug("Visiting class instance creation node " + classInstanceCreation);

        AbstractASTNodeHandler lInvocationHandler = new ClassInstanceCreationHandler(this);
        fNodeHandler.put(classInstanceCreation, lInvocationHandler);
        return lInvocationHandler.visit(classInstanceCreation);
    }

    /**
     * Handle field read/write accesses, e.g., <code>this.field</code>, <code>foo().bar</code>, or also
     * <code>foo.bar</code>).
     *  
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldAccess)
     * 
     * @param fieldAccess The field access AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(FieldAccess fieldAccess) {
        sLogger.debug("Processing field access node " + fieldAccess.getName());

        AbstractASTNodeHandler lFieldAccessHandler = new FieldAccessHandler(this);
        fNodeHandler.put(fieldAccess, lFieldAccessHandler);
        return lFieldAccessHandler.visit(fieldAccess);
    }

    /**
     * Handle super field accesses (e.g., <code>super.field</code>).
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SuperFieldAccess)
     * 
     * @param superFieldAccess The super field access AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(SuperFieldAccess superFieldAccess) {
        sLogger.debug("Processing super field access node " + superFieldAccess.getName());

        AbstractASTNodeHandler lSuperFieldAccessHandler = new SuperFieldAccessHandler(this);
        fNodeHandler.put(superFieldAccess, lSuperFieldAccessHandler);
        return lSuperFieldAccessHandler.visit(superFieldAccess);
    }

    /**
     * Handle field accesses via qualified names, e.g., <code>package.class.field</code>.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.QualifiedName)
     * 
     * @param qualifiedName The qualified name AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(QualifiedName qualifiedName) {
        sLogger.debug("Processing qualified name node " + qualifiedName.getFullyQualifiedName());

        AbstractASTNodeHandler lNameAccessHandler = new NameAccessHandler(this);
        fNodeHandler.put(qualifiedName, lNameAccessHandler);
        return lNameAccessHandler.visit(qualifiedName);
    }

    /**
     * Handle field accesses via simple names, e.g., <code>field</code>.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SimpleName)
     * 
     * @param simpleName The simple name AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(SimpleName simpleName) {
        sLogger.debug("Processing simple name node " + simpleName.getFullyQualifiedName());

        AbstractASTNodeHandler lNameAccessHandler = new NameAccessHandler(this);
        fNodeHandler.put(simpleName, lNameAccessHandler);
        return lNameAccessHandler.visit(simpleName);
    }

    /**
     * Handle type cast statements.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CastExpression)
     * 
     * @param castExpression The type cast expression AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(CastExpression castExpression) {
        sLogger.debug("Processing cast expression node " + castExpression);

        AbstractASTNodeHandler lCastExpressionHandler = new CastExpressionHandler(this);
        fNodeHandler.put(castExpression, lCastExpressionHandler);
        return lCastExpressionHandler.visit(castExpression);
    }

    /**
     * Handle instance of statements.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.InstanceofExpression)
     * 
     * @param instanceOfExpression The instance of expression AST node.
     * @return true, if child nodes should be visited.
     */
    @Override
    public boolean visit(InstanceofExpression instanceOfExpression) {
        sLogger.debug("Processing instanceof expression node " + instanceOfExpression);

        AbstractASTNodeHandler lInstanceofExpressionHandler = new InstanceofExpressionHandler(this);
        fNodeHandler.put(instanceOfExpression, lInstanceofExpressionHandler);
        return lInstanceofExpressionHandler.visit(instanceOfExpression);
    }

    /**
     * Keep track of the nesting of Java statement blocks (enclosed by <code>{...}</code>).
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Block)
     * 
     * @param pBlock The Java statement block AST node.
     * @return true, if child nodes should be visited. 
     */
    @Override
    public boolean visit(Block pBlock) {
        StatementBlock lStatementBlock =
                new StatementBlock(pBlock.getStartPosition(), pBlock.getStartPosition() + pBlock.getLength());
        lStatementBlock.setParentBlock(getCurrStatementBlock());
        setCurrStatementBlock(lStatementBlock);
        return super.visit(pBlock);
    }

    /**
     * Finish block.
     * 
     * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.Block)
     * 
     * @param pBlock The Java statement block AST node.
     */
    @Override
    public void endVisit(Block pBlock) {
        super.endVisit(pBlock);
        setCurrStatementBlock(getCurrStatementBlock().getParentBlock());
    }

    /**
     * Sets the current data type.
     * 
     * @param currType The current data type.
     */
    public void setCurrType(FamixClass currType) {
        fCurrType = currType;
    }

    /**
     * Returns the current data type.
     * 
     * @return The current data type.
     */
    public FamixClass getCurrType() {
        return fCurrType;
    }

    /**
     * Sets the current type reminder stack.
     * 
     * @param currTypeReminder The current data type reminder stack.
     */
    private void setCurrTypeReminder(Stack<FamixClass> currTypeReminder) {
        fCurrTypeReminder = currTypeReminder;
    }

    /**
     * Returns the current type reminder stack.
     * 
     * @return The current type reminder stack.
     */
    private Stack<FamixClass> getCurrTypeReminder() {
        return fCurrTypeReminder;
    }

    /**
     * Removes the top most entry from the current type reminder stack and sets it as current type.
     */
    public void setCurrTypeFromTypeReminder() {
        if (!getCurrTypeReminder().isEmpty()) {
            setCurrType(getCurrTypeReminder().pop());
        }
    }

    /**
     * Obtains the top most entry from the current type stack.
     * 
     * @return The current data type.
     */
    public FamixClass getCurrTypeFromTypeReminder() {
        if (!getCurrTypeReminder().isEmpty()) {
            return getCurrTypeReminder().peek();
        }
        return null;
    }

    /**
     * Adds the current type to the current type reminder stack.
     */
    public void addCurrTypeToTypeReminder() {
        getCurrTypeReminder().push(getCurrType());
    }

    /**
     * Sets the current method.
     * 
     * @param currMethod The current method.
     */
    public void setCurrMethod(FamixMethod currMethod) {
        fCurrMethod = currMethod;
    }

    /**
     * Returns the current method.
     * 
     * @return The current method.
     */
    public FamixMethod getCurrMethod() {
        return fCurrMethod;
    }

    /**
     * Sets the current method reminder stack.
     * 
     * @param currMethodReminder The current method reminder stack.
     */
    private void setCurrMethodReminder(Stack<FamixMethod> currMethodReminder) {
        fCurrMethodReminder = currMethodReminder;
    }

    /**
     * Returns the current method reminder stack.
     * 
     * @return The current method reminder stack.
     */
    private Stack<FamixMethod> getCurrMethodReminder() {
        return fCurrMethodReminder;
    }

    /**
     * Adds the current method to the current method reminder stack.
     */
    public void addCurrMethodToMethodReminder() {
        getCurrMethodReminder().push(getCurrMethod());
    }

    /**
     * Removes the top most entry from the current method reminder stack and sets it as current method.
     */
    public void setCurrMethodFromMethodReminder() {
        if (!getCurrMethodReminder().isEmpty()) {
            setCurrMethod(getCurrMethodReminder().pop());
        }
    }

    /**
     * Sets the anonymous class counter hash table.
     * 
     * @param anonymClassCounter The anonymous class counter hash table.
     */
    public void setAnonymClassCounter(Hashtable<FamixClass, Integer> anonymClassCounter) {
        fAnonymClassCounter = anonymClassCounter;
    }

    /**
     * Returns the anonymous class counter hash table.
     * 
     * @return The anonymous class counter hash table.
     */
    public Hashtable<FamixClass, Integer> getAnonymClassCounter() {
        return fAnonymClassCounter;
    }

    /**
     * Returns the table of unresolved method invocations.
     * 
     * @return The unresolved method invocation hash table.
     */
    public Hashtable<FamixMethod, List<UnresolvedMethodInvocation>> getUnresolvedCalls() {
        return fUnresolvedCalls;
    }

    /**
     * Sets the model container.
     * 
     * @param model The model container.
     */
    public void setModel(FamixModel model) {
        fModel = model;
    }

    /**
     * Returns the model container.
     * 
     * @return The model container.
     */
    public FamixModel getModel() {
        return fModel;
    }

    /**
     * Sets the model factory.
     * 
     * @param factory The model factory.
     */
    public void setFactory(FamixModelFactory factory) {
        fFactory = factory;
    }

    /**
     * Returns the model factory.
     * 
     * @return The model factory.
     */
    public FamixModelFactory getFactory() {
        return fFactory;
    }

    /**
     * Sets the local variable scope hash table.
     * 
     * @param localVariableScope The local variable scope hash table.
     */
    public void setLocalVariableScope(Hashtable<FamixLocalVariable, StatementBlock> localVariableScope) {
        fLocalVariableScope = localVariableScope;
    }

    /**
     * Returns the local variable scope hash table.
     * 
     * @return The local variable scope hash table.
     */
    public Hashtable<FamixLocalVariable, StatementBlock> getLocalVariableScope() {
        return fLocalVariableScope;
    }

    /**
     * Sets the current statement block.
     * 
     * @param currStatementBlock The current statement block.
     */
    public void setCurrStatementBlock(StatementBlock currStatementBlock) {
        fCurrStatementBlock = currStatementBlock;
    }

    /**
     * Returns the current statement block.
     * 
     * @return The current statement block.
     */
    public StatementBlock getCurrStatementBlock() {
        return fCurrStatementBlock;
    }

    /**
     * FamixClass to remember the hierarchy and scope of statement blocks.
     * 
     * @author pinzger
     */
    public class StatementBlock {

        private int fStart;
        private int fEnd;
        private StatementBlock fParentBlock;

        /**
         * The constructor.
         * 
         * @param start Start position of the statement block.
         * @param end End position of the statement block.
         */
        public StatementBlock(int start, int end) {
            this.fStart = start;
            this.fEnd = end;
        }

        /**
         * Returns the end position.
         * 
         * @return The end position.
         */
        public int getEnd() {
            return fEnd;
        }

        /**
         * Sets the end position.
         * 
         * @param end The end position.
         */
        public void setEnd(int end) {
            this.fEnd = end;
        }

        /**
         * Returns the start position.
         * 
         * @return The start position.
         */
        public int getStart() {
            return fStart;
        }

        /**
         * Sets the start position.
         * 
         * @param start The start position.
         */
        public void setStart(int start) {
            this.fStart = start;
        }

        /**
         * Returns the corresponding AST node.
         * 
         * @return The statement block AST node.
         */
        public StatementBlock getParentBlock() {
            return fParentBlock;
        }

        /**
         * Sets the corresponding AST node.
         * 
         * @param parentBlock The statement block AST node.
         */
        public void setParentBlock(StatementBlock parentBlock) {
            this.fParentBlock = parentBlock;
        }
    }
}
