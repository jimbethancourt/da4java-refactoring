package thebigvoid;

public class Universe implements ILawsOfTheUniverse {
	
	private String strAttr1 = new String();
	private int intAttr3 = 1;
	private boolean booleanAttr4 = false;
	
	
	public Universe() {
				
		// Accessor
		this.intAttr3 = 2;
		methodReturnsVoid(this.strAttr1,this.intAttr3);
	}
	
	public void methodReturnsVoid(String param1, int param2) {
		
		
		// Accessor
		strAttr1 = "Something different";
		
		// Accessor and invocation
		booleanAttr4 = methodReturnsBoolean();
		
	}
	
	public boolean methodReturnsBoolean() {
		
		return booleanAttr4;
	}
	
	
	public class InnerClass1 {
		
		private long longAttr1 = 1;
		private boolean boolAttr2 = true;
		private double floatAttr3 = 3.14;
		
		public InnerClass1 methodInnerClass() {
			
			InnerClass1 c = new InnerClass1();
			
			
			// Accessors
			this.longAttr1 = 2;
			this.boolAttr2 = false;
			this.floatAttr3 = 2.718;
			return c;			
		}
	}
}
