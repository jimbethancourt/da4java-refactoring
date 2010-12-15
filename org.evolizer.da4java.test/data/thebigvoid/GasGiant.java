package thebigvoid;

public class GasGiant extends StellarObject implements INonSolidObject {
	
	private String gasType = new String();

	
	public GasGiant() {
		super();
		setGasType("Nitrogen");
	}

	public GasGiant(String name) {

		// Accessor
		this.name = name;
		
		// Accessor
		setGasType("Nitrogen");
	}

	public String getGasType() {
		
		// Accessor
		return this.gasType;
	}
	
	private void setGasType(String type) {
		
		// Accessor
		this.gasType = type;
	}

	@Override
	public String toString() {
		
		return this.name;
	}
	
	

}
