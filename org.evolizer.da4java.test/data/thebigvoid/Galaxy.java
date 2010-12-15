package thebigvoid;

import java.util.Set;
import java.awt.Color;

public class Galaxy {

	
	public String strAttr1 = new String();
	private int intAttr1 = 1;
	Set<StellarObject> objects;
	
	public Galaxy() {
		
		this.strAttr1 = "Foo bar";
	}
	
	public Galaxy(String name) {
		
		this.strAttr1 = name;
		
		Color c = new Color(255,192,128);
		
		objects.add(new Planet("Saturn",c));
		
	}
	
	public void expand() {
		
		intAttr1++;
		if(intAttr1 >= 1000) {
			return;
		} else {
			
			// Recursive invocation
			expand();
		}
	}
	
	public String getName() {
		
		return strAttr1;
	}
	
	public void setName(String name) {
		
		this.strAttr1 = name;
	}
	
}
