package testPackage.ae;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SimpleTemplate<T> {
    List<T> container = new ArrayList<T>();
    T lastEntry;
    
    public void add(T entity) {
        container.add(entity);
        lastEntry = entity;
    }
    
    public int count(Collection<?> collection) {
        return collection.size();
    }
    
    public T templateMethod(List<T> arg) {
        return arg.get(3);
    }
}
