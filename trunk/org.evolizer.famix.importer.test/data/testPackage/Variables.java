/**
 * 
 */
package testPackage;

import java.util.Vector;


/**
 * @author pinzger
 *
 */
public class Variables {
	private int field;
	private int fMulti1, fMulti2, fMulti3;
	private int fInit = 4;

	private int a,b,c=2;
	private static final String st = "static string";
	
	private Sum refSum;
	private Vector<Sum> containerSum = new Vector<Sum>();
	private Sum[] arraySum;
	
	public void m(int param, int[] arrayParam) {
		int local;
		int multi1, multi2;

		String localVariableExpression = "a String";
		try {
			int nrs[] = {1,2,3,4,5};
			for (int inFor=0; inFor < 10; inFor++) {
				int value = nrs[inFor];
			}
		} catch (ArrayIndexOutOfBoundsException aiob) {
			System.err.println("Some error");
		}
	}
	
	public void testDuplicates(int x) {
		if (x==1) {
			int b = 2;
		} else {
			if (x == 2) {
				int b = 3;
			}
		}
	}
	
	public void fieldAccess() {
		this.field = 1;
		refSum.publicField = 10;
		testPackage.ae.Test t = new testPackage.ae.Test();
		int tmp = t.getRefSum().publicField;
		System.out.println("finit " + fInit);
	}
	
	public void getSum(IBase sum) {
		sum.compute();
	}
}