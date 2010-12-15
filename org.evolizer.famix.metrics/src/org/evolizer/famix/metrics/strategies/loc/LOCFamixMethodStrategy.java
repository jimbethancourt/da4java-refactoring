package org.evolizer.famix.metrics.strategies.loc;


/**
 * Strategy to calculate a LOC metric value for a FamixMethod.
 * 
 * @author zenger, wuersch, pinzger
 * 
 */
public class LOCFamixMethodStrategy extends AbstractLOCStrategy {

	@Override
	protected double calculate() {
		return calculateLOC(getCurrentEntity().getSource());
	}

	public Class<?>[] getCompatibleTypes() {
		return new Class<?>[] { org.evolizer.famix.model.entities.FamixMethod.class };
	}

}
