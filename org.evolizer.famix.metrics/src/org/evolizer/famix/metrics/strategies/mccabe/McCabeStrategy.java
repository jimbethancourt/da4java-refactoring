package org.evolizer.famix.metrics.strategies.mccabe;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.evolizer.metrics.store.IMetricCalculationStrategy;

/**
 * Abstract Superclass for all strategies that calculate McCabe's cyclomatic
 * complexity
 * 
 * @author Reto
 * 
 */
public abstract class McCabeStrategy implements IMetricCalculationStrategy {

    private static final String identifier = "McCabe";
    private static final String description = "Calculates cyclomatic complexity according to McCabe";

    public String getDescription() {
        return description;
    }

    public String getIdentifier() {
        return identifier;
    }
    
    /*
     * Calculates the McCabe value for a string
     */
    protected double calculateMcCabe(String code, int start, int end) {
        McCabeASTVisitor mcbVisitor = new McCabeASTVisitor();
        
        ASTParser lParser = ASTParser.newParser(AST.JLS3); // up to J2SE 1.5
        lParser.setSource(code.toCharArray());
        lParser.setResolveBindings(false);
        lParser.setSourceRange(start, (end-start)+1);
        
        
        ASTNode lResult = lParser.createAST(null);
        // Not sure if this always finds the correct ASTNode!
        ASTNode specificNode = NodeFinder.perform(lResult, start, end-start);
        if (specificNode.getStartPosition() != start || specificNode.getLength() != (end-start)) {
            System.out.println("Could not find the corresponding AST node at position: " + start);
        }
        try {
            specificNode.accept(mcbVisitor);
        } catch (Exception ex) {
            //TODO Error handling
        }

        return mcbVisitor.getMetricValue();
    }

    private class McCabeASTVisitor extends ASTVisitor {
        private double metricValue;

        public McCabeASTVisitor() {
            metricValue = 1.0d;
        }

        public boolean visit(MethodDeclaration node) {
            metricValue++;
            return true;
        }

        public boolean visit(IfStatement node) {
            metricValue++;
            return true;
        }

        public boolean visit(SwitchCase node) {
            metricValue++;
            return true;
        }

        public boolean visit(ForStatement node) {
            metricValue++;
            return true;
        }

        public boolean visit(WhileStatement node) {
            metricValue++;
            return true;
        }

        public boolean visit(DoStatement node) {
            metricValue++;
            return true;
        }

        public boolean visit(CatchClause node) {
            metricValue++;
            return true;
        }

        public boolean visit(ConditionalExpression node) {
            metricValue++;
            return true;
        }

        public double getMetricValue() {
            return metricValue;
        }

    }
}
