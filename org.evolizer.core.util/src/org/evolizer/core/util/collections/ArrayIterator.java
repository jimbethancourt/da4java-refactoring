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

import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Iterator for arrays.
 * 
 * @param <T>
 *            the array type. E.g., {@link String} for {@link String}-Arrays.
 * @author wuersch
 */
public class ArrayIterator<T> implements ListIterator<T> {

    private T[] fTheArray;
    private int fCursor;

    /**
     * Constructor.
     * 
     * @param theArray
     *            the array that should be iterated.
     */
    public ArrayIterator(T... theArray) {
        this.fTheArray = theArray;
        fCursor = -1;
    }

    /**
     * Arrays are not resizable, therefore this method will throw an {@link UnsupportedOperationException}.
     * 
     * @param o
     *            the o
     */
    public void add(T o) {
        throw new UnsupportedOperationException("Arrays cannot grow!");
    }

    /**
     * Checks for next.
     * 
     * @return <code>true</code> if there are more elements in the array, <code>false</code> otherwise.
     */
    public boolean hasNext() {
        if (fCursor == -1) {
            return fTheArray.length > 0;
        } else {
            return fCursor < fTheArray.length - 1;
        }
    }

    /**
     * Checks for previous element.
     * 
     * @return <code>true</code> if there are previous elements in the array, <code>false</code> otherwise.
     */
    public boolean hasPrevious() {
        return fCursor > 0;
    }

    /**
     * Proceeds to the next element in the array and returns it.
     * 
     * @return the next element.
     * @throws NoSuchElementException
     *             if there is no next element
     */
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        return fTheArray[++fCursor];
    }

    /**
     * Returns the current index + 1 or -1 if the end of the array is reached.
     * 
     * @return <code>currentIndex + 1</code> or <code>-1</code> if end is reached
     */
    public int nextIndex() {
        return (fCursor < fTheArray.length) ? fCursor + 1 : -1;
    }

    /**
     * Proceeds back to the previous element in the array and returns it.
     * 
     * @return the previous element.
     * @throws NoSuchElementException
     *             if there is no previous element
     */
    public T previous() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }

        return fTheArray[--fCursor];
    }

    /**
     * Returns the current index - 1 or -1 if the beginning of the array is reached.
     * 
     * @return <code>currentIndex - 1</code> or <code>-1</code> if beginning is reached
     */
    public int previousIndex() {
        return (fCursor > 0) ? fCursor - 1 : -1;
    }

    /**
     * Arrays are not resizable, therefore this method will throw an {@link UnsupportedOperationException}.
     */
    public void remove() {
        throw new UnsupportedOperationException("Arrays cannot shrink!");
    }

    /**
     * This method will throw an {@link UnsupportedOperationException}.
     * 
     * @param o
     *            the o
     */
    public void set(T o) {
        throw new UnsupportedOperationException();
    }
}
