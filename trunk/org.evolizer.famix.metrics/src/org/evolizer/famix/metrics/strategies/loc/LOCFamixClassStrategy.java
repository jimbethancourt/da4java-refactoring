package org.evolizer.famix.metrics.strategies.loc;


public class LOCFamixClassStrategy extends AbstractLOCStrategy {

    public Class<?>[] getCompatibleTypes() {
        return new java.lang.Class<?>[] { org.evolizer.famix.model.entities.FamixClass.class };
    }

    @Override
	protected double calculate() {
		return calculateLOC(getCurrentEntity().getSource());
	}	
}
