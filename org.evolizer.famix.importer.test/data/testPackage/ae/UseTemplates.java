/**
 * 
 */
package testPackage.ae;

import java.util.ArrayList;

import testPackage.Base;
import testPackage.IBase;


/**
 *
 * @author mpinzger@tudelft.net
 *
 */
public class UseTemplates {
    public void useSimpleTemplate() {
        SimpleTemplate<String> stringList = new SimpleTemplate<String>();
        stringList.add("TestString");
        
        stringList.count(new ArrayList<String>());
        stringList.templateMethod(new ArrayList<String>());
    }
    
    public void useImplementTemplate() {
        IBase base = new Base(10, 20) {
            public int compute() {
                return getA() * getB();
            };    
        };
        
        ImplementTemplate<IBase> it = new ImplementTemplate<IBase>();
        it.foo(base);
    }
}
