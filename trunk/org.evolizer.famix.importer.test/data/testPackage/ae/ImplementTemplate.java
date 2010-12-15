package testPackage.ae;

import testPackage.IBase;


public class ImplementTemplate<T extends IBase> implements IImplementTemplate<T> {
    public int foo(T arg1) {
        return arg1.compute();
    }
}
