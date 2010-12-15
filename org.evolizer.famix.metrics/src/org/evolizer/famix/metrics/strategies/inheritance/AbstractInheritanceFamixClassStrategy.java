/**
 * 
 */
package org.evolizer.famix.metrics.strategies.inheritance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evolizer.famix.metrics.strategies.AbstractFamixMetricStrategy;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.AbstractFamixGeneralization;
import org.evolizer.famix.model.entities.FamixClass;
import org.evolizer.famix.model.entities.FamixMethod;
import org.evolizer.famix.model.utils.SnapshotAnalyzer;


/**
 *
 * @author mpinzger@tudelft.net
 *
 */
public abstract class AbstractInheritanceFamixClassStrategy extends AbstractFamixMetricStrategy {

    /** 
     * {@inheritDoc}
     */
    public Class<?>[] getCompatibleTypes() {
        return new Class<?>[] { org.evolizer.famix.model.entities.FamixClass.class };
    }
    
    protected List<FamixClass> querySuperClasses() {
        FamixClass famixClass = (FamixClass) getCurrentEntity();
        SnapshotAnalyzer snapshotAnalyzer = new SnapshotAnalyzer(getCurrentSession());
        List<FamixClass> entities = new ArrayList<FamixClass>();
        entities.add(famixClass);
        Set<FamixClass> dependentEntities = new HashSet<FamixClass>();
        snapshotAnalyzer.queryDependentEntities(
                entities, 
                dependentEntities, 
                FamixClass.class,
                AbstractFamixGeneralization.class,
                "from",
                0,
                -1);
        
        return new ArrayList<FamixClass>(dependentEntities);
    }
    
    protected Set<String> getMethodNames(List<FamixClass> famixClasses) {
        Set<String> methodNames = new HashSet<String>();
        for (FamixClass famixClass : famixClasses) {
            if (!famixClass.isInterface()) {
                for (FamixMethod famixMethod : famixClass.getMethods()) {
                    String methodName = getMethodName(famixMethod.getUniqueName());
                    if (!methodName.endsWith(AbstractFamixEntity.OBJECT_INIT_METHOD) 
                        && !methodName.endsWith(AbstractFamixEntity.CLASS_INIT_METHOD)) {
                        methodNames.add(methodName);
                    }
                }
            }
        }
        return methodNames;
    }
    
    protected String getMethodName(String methodName) {
        int pos = 0;
        String name = methodName;
        name = name.substring(0, name.indexOf("("));
        pos = name.lastIndexOf(".") + 1;
        return methodName.substring(pos);
    }
}
