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
package org.evolizer.core.util.resourcehandling;

import java.io.File;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.evolizer.core.util.collections.ArrayIterator;
import org.evolizer.core.util.collections.FilteringIterator;
import org.evolizer.core.util.collections.IPredicate;

/**
 * This class returns all the {@link File}s in a directory (or its (sub-*)directories) that fulfill a given filter
 * predicate. Usage example:
 * 
 * <pre>
 * DirectoryListing content = new DirectoryListing(dir,
 * 
 * new IPredicate&lt;File&gt;() {
 * 
 *     public boolean evaluate(File f) {
 *         return f.getName().endsWith(&quot;.java&quot;);
 *     }
 * 
 * }
 * 
 * );
 * 
 * for (File f : content) {
 *     System.out.println(f.getName());
 * }
 * </pre>
 * 
 * @author wuersch
 */
public class DirectoryListing implements Iterable<File> {

    private final File fDirectory;
    private final IPredicate<File> fFilter;

    /**
     * Constructor.
     * 
     * @param directory
     *            the directory which contents should be "listed".
     * @param filter
     *            a filter that decides whether an element should make it into the listing or not.
     */
    public DirectoryListing(File directory, IPredicate<File> filter) {
        this.fDirectory = directory;
        this.fFilter = filter;
    }

    /**
     * Returns the iterator.
     * 
     * @return the iterator
     */
    public Iterator<File> iterator() {
        return new FilteringIterator<File>(new RecursiveFileListIterator(fDirectory), fFilter);
    }

    /**
     * The Class RecursiveFileListIterator.
     */
    class RecursiveFileListIterator implements Iterator<File> {

        private final Stack<Iterator<File>> fIterators = new Stack<Iterator<File>>();

        private File fNext;

        /**
         * Instantiates a new recursive file list iterator.
         * 
         * @param directory
         *            the directory
         */
        public RecursiveFileListIterator(File directory) {
            fIterators.push(new ArrayIterator<File>(directory.listFiles()));
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasNext() {
            moveToNext();
            return (fNext != null);
        }

        /**
         * {@inheritDoc}
         */
        public File next() {
            moveToNext();

            if (fNext == null) {
                throw new NoSuchElementException();
            } else {
                File nextFile = fNext;
                fNext = null;
                return nextFile;
            }
        }

        /**
         * {@inheritDoc}
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void moveToNext() {
            if ((fNext == null) && !fIterators.empty()) {
                if (!fIterators.peek().hasNext()) {
                    fIterators.pop();
                    moveToNext();
                } else {
                    final File nextFile = fIterators.peek().next();
                    if (nextFile.isDirectory()) {
                        fIterators.push(new ArrayIterator<File>(nextFile.listFiles()));
                        moveToNext();
                    } else {
                        fNext = nextFile;
                    }
                }
            }
        }
    }
}
