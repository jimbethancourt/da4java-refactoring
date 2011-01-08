package thebigvoid;

import java.awt.Color;

public class Planet extends StellarObject {

	private String color = "undef";
	
	public Planet(String name, Color c) {
		
		// Accessor & invocation
		this.color = c.toString();
		
		// Accessor
		this.name = name;
	}
	
	public String getColor() {
		
		return this.color;
	}
}
