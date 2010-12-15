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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * This class allows iterating over a text file. Usage:
 * 
 * <pre>
 * TextFile tf = new TextFile(new File(&quot;myFile.txt&quot;));
 * for (String line : tf) {
 *     System.out.println(line);
 * }
 * </pre>
 * 
 * @author wuersch
 */
public class TextFile implements Iterable<String> {

    private final File fFile;

    /**
     * Constructor.
     * 
     * @param fileName
     *            the file name
     * @throws NullPointerException
     *             If the <code>fileName</code> argument is <code>null</code>
     */
    public TextFile(String fileName) {
        fFile = new File(fileName);
    }

    /**
     * Constructor.
     * 
     * @param file
     *            the file
     * @throws NullPointerException
     *             If the <code>file</code> argument is <code>null</code>
     */
    public TextFile(File file) {
        fFile = file;
    }

    /**
     * Returns the iterator.
     * 
     * @return the iterator
     */
    public Iterator<String> iterator() {
        return new TextFileIterator();
    }

    /**
     * Returns the content of this text file as a single string. Line breaks are preserved.
     * 
     * @return the content of this file.
     */
    public String asString() {
        StringBuffer result = new StringBuffer();

        for (String line : this) {
            result.append(line).append('\n');
        }

        return result.toString();
    }

    /**
     * Iterator that allows to iterate over each line of a text file.
     * 
     * @author wuersch
     */
    class TextFileIterator implements Iterator<String> {

        private BufferedReader fIn;
        private String fNextLine;

        /**
         * Constructor.
         */
        public TextFileIterator() {
            try {
                fIn = new BufferedReader(new FileReader(fFile));
                fNextLine = fIn.readLine();
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        /**
         * Returns whether there are more lines.
         * 
         * @return <code>true</code>, if there are more lines; <code>false</code> otherwise
         */
        public boolean hasNext() {
            return fNextLine != null;
        }

        /**
         * Returns the next line.
         * 
         * @return next line
         */
        public String next() {
            try {
                String result = fNextLine;
                if (fNextLine != null) {
                    fNextLine = fIn.readLine();
                    if (fNextLine == null) {
                        fIn.close();
                    }
                }

                return result;
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        /**
         * Not supported.
         * 
         * @throws UnsupportedOperationException.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
