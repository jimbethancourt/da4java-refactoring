package org.evolizer.famix.metrics.strategies.mccabe;



import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * ASTVisitor class for crawling source code and calculating the cyclomatic
 * complexity according to McCabe
 * 
 * @author Reto Zenger
 * 
 */
public class ASTMcCabe extends ASTVisitor {

	private float metricValue;

	public ASTMcCabe() {
		metricValue = 0;
	}

	public void resetASTMcCabe() {
		metricValue = 0;
	}

	public IStatus analyze(String sourceCode, int start, int end) {
		IStatus status = Status.OK_STATUS;

		resetASTMcCabe();
		ASTParser lParser = ASTParser.newParser(AST.JLS3); // up to J2SE 1.5
		lParser.setSource(sourceCode.toCharArray());
		lParser.setResolveBindings(false);
		ASTNode lResult = lParser.createAST(null);
//		IProblem[] problems = lResult.getProblems();
//		if (problems.length > 0) {
//			for (int i = 0; i < problems.length; i++) {
//				logger.warn(problems[i].getMessage());
//			}
//		}

		// catch all errors
		try {
			lResult.accept(this);
		} catch (Exception ex) {
			//TODO Exception handling
		}
		return status;
	}

	//
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

	public float getMetricValue() {
		return metricValue;
	}

}
