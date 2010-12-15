package testPackage;



public class Sum extends Base {
	public int publicField = 0;
	
	public Sum() {
		super();
	}
	public Sum(int a, int b) {
		super(a, b);
	}
	public int compute() {
		return getA() + getB();
	}
	public int computeOtherResolved(String name) {
		return super.computeOther(name);
	}
}