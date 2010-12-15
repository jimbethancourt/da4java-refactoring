/**
 * 
 */
package testPackage;



/**
 * @author pinzger
 *
 */
public abstract class Base implements IBase {
	private int a=0;
	private int b=0;

	public Base() {
	}
	public Base(int a, int b) {
		this.a = a;
		this.b = b;
	}
	
	public abstract int compute();
	
	public int computeOther(String name) {
		return name.length();
	}
	
	public int computeOther(int offset, String name) {
		return computeOther(name) + compute();
	}
	
	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	public int getB() {
		return b;
	}

	public void setB(int b) {
		this.b = b;
	}
	
}