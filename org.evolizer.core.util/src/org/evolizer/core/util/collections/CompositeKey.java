package org.evolizer.core.util.collections;

/**
 * This class is useful if you want to combine two objects (components), e.g., as key for a value stored in a hash table.
 * 
 * @author wuersch
 *
 * @param <Key1> The type of the first component.
 * @param <Key2> The type of the second component.
 */
public class CompositeKey<Key1, Key2> {
	private Key1 fFirst;
	private Key2 fSecond;
	
	/**
	 * The constructor.
	 * 
	 * @param first the first component of the key.
	 * @param second the second component of the key.
	 */
	public CompositeKey(Key1 first, Key2 second) {
		fFirst = first;
		fSecond = second;
	}
	
	/**
	 * Returns the first component of the composite key.
	 * 
	 * @return the first component
	 */
	public Key1 first() {
		return fFirst;
	}
	
	/**
	 * Returns the second component of the composite key.
	 * 
	 * @return the second component
	 */
	public Key2 second() {
		return fSecond;
	}

	/**
	 * 
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fFirst == null) ? 0 : fFirst.hashCode());
		result = prime * result + ((fSecond == null) ? 0 : fSecond.hashCode());
		return result;
	}

	/**
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompositeKey<?, ?> other = (CompositeKey<?, ?>) obj;
		if (fFirst == null) {
			if (other.fFirst != null)
				return false;
		} else if (!fFirst.equals(other.fFirst))
			return false;
		if (fSecond == null) {
			if (other.fSecond != null)
				return false;
		} else if (!fSecond.equals(other.fSecond))
			return false;
		return true;
	}
}
