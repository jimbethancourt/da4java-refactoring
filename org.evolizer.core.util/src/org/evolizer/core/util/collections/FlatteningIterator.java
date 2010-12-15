/*
 * Copyright 2009 University of Zurich, Switzerland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.evolizer.core.util.collections;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * An iterator that 'flattens out' collections, iterators, etc. Arrays are treated as normal objects (i.e., they are not
 * flattened out). That is it will iterate out their contents in order, descending into any iterators, iterables or
 * arrays provided to it.
 * <p>
 * An example (not valid Java for brevity - some type declarations are omitted):
 * 
 * <pre>
 * new FlattingIterator(new LinkedList{1, 2, 3}, new LinkedList{new ArrayList{1, 2}, new LinkedList{3}}, new ArrayList({1, 2, 3}))
 * </pre>
 * 
 * Will iterate through the sequence 1, 2, 3, 1, 2, 3, 1, 2, 3. Note that this implements a non-generic version of the
 * Iterator interface so may be cast appropriately - it's very hard to give this class an appropriate generic type.
 * <p>
 * Adopted from: http://snippets.dzone.com
 * 
 * @author wuersch
 */
public class FlatteningIterator implements Iterator<Object> {

    /* 
     * Marker object. This is never exposed outside this class, so can be guaranteed
     * to be != anything else. We use it to indicate an absence of any other object. 
     */
    private final Object fBlank = new Object();

    /*
     * This stack stores all the iterators found so far. The head of the stack
     * is the iterator which we are currently progressing through
     */
    private final Stack<Iterator<?>> fIterators = new Stack<Iterator<?>>();

    /* Storage field for the next element to be returned. blank when the next
     * element is currently unknown.
     */
    private Object fNext = fBlank;

    /**
     * Instantiates a new flattening iterator.
     * 
     * @param objects
     *            the objects
     */
    public FlatteningIterator(Object... objects) {
        fIterators.push(Arrays.asList(objects).iterator());
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void moveToNext() {
        if ((fNext == fBlank) && !fIterators.empty()) {
            if (!fIterators.peek().hasNext()) {
                fIterators.pop();
                moveToNext();
            } else {
                final Object next = fIterators.peek().next();
                if (next instanceof Iterator<?>) {
                    fIterators.push((Iterator<?>) next);
                    moveToNext();
                } else if (next instanceof Iterable<?>) {
                    fIterators.push(((Iterable<?>) next).iterator());
                    moveToNext();
                } else {
                    this.fNext = next;
                }
            }
        }
    }

    /**
     * Returns the next element in our iteration.
     * 
     * @return the next element
     * @throws NoSuchElementException
     *             if there is no next element
     */
    public Object next() {
        moveToNext();

        if (fNext == fBlank) {
            throw new NoSuchElementException();
        } else {
            Object next = this.fNext;
            this.fNext = fBlank;
            return next;
        }
    }

    /**
     * Checks whether there exists a next element. This method can change the internal state of the object when it is
     * called, but repeated calls to it will not have any additional side effects.
     * 
     * @return <code>true</code>, if there are any objects left to iterate over; <code>false</code> otherwise
     */
    public boolean hasNext() {
        moveToNext();
        return (fNext != fBlank);
    }
}
