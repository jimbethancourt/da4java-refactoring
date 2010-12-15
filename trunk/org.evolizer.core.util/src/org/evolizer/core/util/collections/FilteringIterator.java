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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Wrapper for iterators, that only returns elements that fulfill a given predicate.
 * 
 * @author wuersch
 * @param <T>
 *            the type of the collection. E.g., {@link String} for <code>List&lt;String&gt;</code>.
 * @see IPredicate
 */
public class FilteringIterator<T> implements Iterator<T> {

    private Iterator<T> fUnfilteredIterator;
    private IPredicate<T> fPredicate;

    private State fState = State.NOT_READY;

    private T fNext;

    private enum State {
        READY,
        NOT_READY,
        DONE,
        FAILED
    }

    /**
     * Constructor.
     * 
     * @param wrappedIterator
     *            The unfiltered iterator.
     * @param predicate
     *            A predicate used to decide whether an element should be returned by the
     *            {@link FilteringIterator#next()} method or skipped. If the predicate evaluates to <code>true</code>,
     *            the element is returned, otherwise it will be skipped. If the predicate is <code>null</code> all
     *            elements are returned. Then, of course, it is rather useless to apply this iterator. However, this
     *            behaviour (rather than not allowing <code>null</code> predicates) facilitates the implementation of
     *            e.g., wrapping iterators that do not make use of predicates.
     */
    public FilteringIterator(Iterator<T> wrappedIterator, IPredicate<T> predicate) {
        this.fUnfilteredIterator = wrappedIterator;
        this.fPredicate = predicate;
    }

    /**
     * Checks for next element.
     * 
     * @return <code>true</code>, if next element exists; <code>false</code> otherwise
     * @see Iterator#hasNext()
     */
    public boolean hasNext() {
        switch (fState) {
            case DONE:
                return false;
            case READY:
                return true;
            default:
                return tryToComputeNext();
        }
    }

    /**
     * Returns next element.
     * 
     * @return the next element
     * @see Iterator#next()
     * @throws NoSuchElementException
     *             if there is no next element
     */
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        fState = State.NOT_READY;

        return fNext;
    }

    /**
     * Not supported.
     * 
     * @throws UnsupportedOperationException.
     * @see Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private boolean tryToComputeNext() {
        fState = State.FAILED;
        fNext = computeNext();
        if (fState != State.DONE) {
            fState = State.READY;
            return true;
        }

        return false;
    }

    private T computeNext() {
        while (fUnfilteredIterator.hasNext()) {
            T element = fUnfilteredIterator.next();
            if ((fPredicate == null) || fPredicate.evaluate(element)) {
                return element;
            }
        }

        fState = State.DONE;
        return null;
    }
}
