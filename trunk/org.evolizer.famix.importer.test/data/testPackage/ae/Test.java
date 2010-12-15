package testPackage.ae;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import testPackage.Sum;
import testPackage.Variables;


public class Test {
	private int a,b,c=2;
	private static final String st = "static string";
	
	private Sum refSum;
	private Vector<Sum> containerSum = new Vector<Sum>();
	private Sum[] arraySum;

	private String juhu = "juhu";

	private Sum anonym = new Sum() {
		private int anonymAttribute = 0;
		
		public int compute() {
			return 2*(getA() + getB());
		}
	};
	
	private final Map buffers = new LinkedHashMap( 16, .75F, true ){
		public boolean removeEldestEntry(Map.Entry eldest) {
			return true;
		}
	};  
	
	public Test() {
	}
	
	public class Inner {
		private int innerAttribute = 0;
		public void innerMethod() {
			Sum innerSum = new Sum();
			innerSum.compute();
			innerSum.compute();
			
			System.out.println("FamixMethod of an inner class " + st + " " + computeSum());
			InnerInner innerInnerAnonym = new InnerInner(2){
				private int x = 0;

			};
		}
		
		public class InnerInner {
			public InnerInner(int i){
				// TODO: handle such strange class delcarations
				class InnerInnerMethod {
					private int hmm;
					public InnerInnerMethod(int hmm) {
						this.hmm = hmm;
					}
					public void doSomething() {
						innerMethod();
					}
				}
			}
		}		
	}
	public void foo() {
		this.a = 17;
		System.out.println("The value of a is: " + this.a);
		
		Sum mySum = new Sum();
		mySum.computeOtherResolved(juhu);
	}
	public int goo(int i, int[] j, Object o, Object[] p, Inner.InnerInner x, Inner.InnerInner[][] y, Sum a, Sum[][][] b) {
		return this.a;
	}
	public void caller() {
		foo();
	}

	public void accessInner() {
		(new Inner()).innerMethod();
	}
	
	public int computeSum() {
		Sum s = new Sum(10, 15);
		return s.compute() + s.compute();
	}
	
	public int containsAnonymClass() {
		Sum lAnonym = new Sum() {
			public int compute() {
				return doubleIt() * (getA() + getB());
			}
			public int doubleIt() {
				return 2;
			}
		};
		
		return lAnonym.compute();
	}
	public int computeSumAnonym() {
		return anonym.compute();
	}
	
	public int computeAllSums() {
		int total = 0;
		for (Sum s : containerSum) {
			total += s.compute();
		}
		
		// with instance of check and type cast
		for (Iterator iter = containerSum.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (o instanceof Sum) {
				total += ((Sum) o).compute();
			}
		}

		return total;
	}
	
	public Vector<Sum> getContainerSum() {
		return containerSum;
	}
	public Sum[] getArraySum() {
		return arraySum;
	}
	public Sum getRefSum() {
		return refSum;
	}

	public void getParamSum() {
		Variables vars = new Variables();
		Sum sum = new Sum(10,20);
		vars.getSum(sum);
	}
	
	public void callSequence() {
		containerSum.elementAt(0).compute();
	}
}
