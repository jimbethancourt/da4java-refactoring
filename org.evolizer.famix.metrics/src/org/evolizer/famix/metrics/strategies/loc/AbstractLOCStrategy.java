package org.evolizer.famix.metrics.strategies.loc;

import java.util.regex.Pattern;

import org.evolizer.famix.metrics.strategies.AbstractFamixMetricStrategy;

/**
 * Superclass of all LOCStrategies.
 * 
 * @author zenger
 * 
 */
public abstract class AbstractLOCStrategy extends AbstractFamixMetricStrategy {

	private static final String fIdentifier = "LOC";
	private static final String fDescription = "Calculates the lines of code";
	private boolean inComment = false;

	public String getDescription() {
		return fDescription;
	}

	public String getIdentifier() {
		return fIdentifier;
	}

	/**
	 * Strategy for LOC calculation, counts all code lines (Special: - counts
	 * annotations as code - counts lines with code followed by comment as code
	 * - does not count lines with closing comment (* /) followed by code)
	 * 
	 * @param code
	 * @return calculated value
	 */
	protected float calculateLOC(String code) {
	    float metricValue = 0;
		String[] LOCs = code.split("\n");
		for (String line : LOCs) {
			if (!isComment(line) && !isEmptyLine(line) && !inComment
					&& !isImportStatement(line) && !isPackageDeclaration(line)) {
				metricValue++;
			}
		}
		return metricValue;
	}

	/*
	 * checks whether the string can be interpreted as a package declaration
	 */
	private boolean isPackageDeclaration(String line) {
		return Pattern.compile("^(\\s)*(package)").matcher(line).find();
	}

	/*
	 * checks whether the string can be interpreted as an import statement
	 */
	private boolean isImportStatement(String line) {
		return Pattern.compile("^(\\s)*(import)").matcher(line).find();
	}

	/*
	 * checks whether the string can be interpreted as an empty line
	 */
	private boolean isEmptyLine(String line) {
		return Pattern.compile("^(\\s)*$").matcher(line).find();
	}

	/*
	 * checks whether the string can be interpreted as comment
	 */
	private boolean isComment(String line) {
		boolean comment = false;
		if (Pattern.compile("^(\\s)*(/[*])").matcher(line).find()) {
			inComment = true;
			comment = true;
			;
		}
		if (Pattern.compile("([*]/)").matcher(line).find()) {
			inComment = false;
			comment = true;
		}
		return (Pattern.compile("^(\\s)*[//]").matcher(line).find() || comment);
	}

}
