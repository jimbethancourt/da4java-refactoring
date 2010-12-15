/**
 * 
 */
package testPackage.ae;

import testPackage.IBase;


/**
 *
 * @author mpinzger@tudelft.net
 *
 */
public interface IImplementTemplate<T extends IBase> {
    public int foo(T arg1);
}
