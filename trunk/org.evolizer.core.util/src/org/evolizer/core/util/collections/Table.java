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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A table-like data structure with column and row labels. Usage is similar to a Map, except that you have to supply two
 * keys, i.e. the column and row label, to store and retrieve the data.
 * 
 * @param <C>
 *            The type of the column label. Typically a String, but can be any of any type.
 * @param <R>
 *            The type of the row label. Typically a String, but can be any of any type.
 * @param <V>
 *            The type of the value to be stored inside the cells of a table instance.
 * @author wuersch, giger
 */
// TODO Hibernate-Mapping? :-D
public class Table<R, C, V> {

    private List<C> fColumnLabels;
    private List<R> fRowLabels;

    private Map<CompositeKey<R, C>, V> fValues = new HashMap<CompositeKey<R, C>, V>();

    /**
     * Constructor.
     */
    public Table() {
        fColumnLabels = new ArrayList<C>();
        fRowLabels = new ArrayList<R>();
    }

    /**
     * Deletes column for given column label. The values in the specified column are set to <code>null</code>.
     * 
     * @param columnLabel
     *            the label of the column that is deleted
     */
    public void deleteColumn(C columnLabel) {
        if (fColumnLabels.contains(columnLabel)) {
            for (R rowLabel : fRowLabels) {
                put(rowLabel, columnLabel, null);
            }
        }
        fColumnLabels.remove(columnLabel);
    }

    /**
     * Deletes row for given row label. The values in the specified row are set to <code>null</code>.
     * 
     * @param rowLabel
     *            the label of the row that is deleted
     */
    public void deleteRow(R rowLabel) {
        if (fRowLabels.contains(rowLabel)) {
            for (C columnLabel : fColumnLabels) {
                put(rowLabel, columnLabel, null);
            }
        }
        fRowLabels.remove(rowLabel);
    }

    /**
     * Returns the number of rows.
     * 
     * @return the number of rows
     */
    public int getNrOfRows() {
        return fRowLabels.size();
    }

    /**
     * Returns the number of columns.
     * 
     * @return the number of columns
     */
    public int getNrOfColumns() {
        return fColumnLabels.size();
    }

    /**
     * Adds an entry to the table at the position for the given column and row label.
     * 
     * @param columnLabel
     *            the column label
     * @param rowLabel
     *            the row label
     * @param value
     *            the value to insert
     */
    public void put(R rowLabel, C columnLabel, V value) {
        if (!containsColumnLabel(columnLabel)) {
            fColumnLabels.add(columnLabel);
        }
        if (!containsRowLable(rowLabel)) {
            fRowLabels.add(rowLabel);
        }
        fValues.put(new CompositeKey<R, C>(rowLabel, columnLabel), value);
    }

    /**
     * Returns the value at the position of the given column and row label.
     * 
     * @param columnLabel
     *            the column label
     * @param rowLabel
     *            the row label
     * @return the value
     */
    public V get(R rowLabel, C columnLabel) {
        return fValues.get(new CompositeKey<R, C>(rowLabel, columnLabel));
    }

    /**
     * Removes entry stored at the position for the given row and column label.
     * 
     * @param columnLabel
     *            the column label
     * @param rowLabel
     *            the row label
     * @return the removed value
     */
    public V remove(R rowLabel, C columnLabel) {
        fColumnLabels.remove(columnLabel);
        fRowLabels.remove(rowLabel);

        return fValues.remove(new CompositeKey<R, C>(rowLabel, columnLabel));
    }

    /**
     * Returns a value by index.
     * 
     * @param x
     *            the column index
     * @param y
     *            the row index
     * @return the value at the given table index
     */
    public V getValueByIndices(int x, int y) {
        V value = null;
        boolean rowExists = false;
        boolean columnExists = false;

        if ((x >= 0) && (x < fRowLabels.size())) {
            rowExists = true;
        }

        if ((y >= 0) && (y < fColumnLabels.size())) {
            columnExists = true;
        }

        if (rowExists && columnExists) {
            R rowLabel = fRowLabels.get(x);
            C columnLabel = fColumnLabels.get(y);
            value = get(rowLabel, columnLabel);
        }
        return value;
    }

    /**
     * Sets values by index.
     * 
     * @param x
     *            the column index
     * @param y
     *            the row index
     * @param value
     *            the value to set
     * @throws IndexOutOfBoundsException
     *             if one or both indices lie out of bounds
     */
    public void setByIndicies(int x, int y, V value) throws IndexOutOfBoundsException {
        boolean rowExists = false;
        boolean columnExists = false;

        if ((x >= 0) && (x < fRowLabels.size())) {
            rowExists = true;
        }

        if ((y >= 0) && (y < fColumnLabels.size())) {
            columnExists = true;
        }

        if (rowExists && columnExists) {
            put(fRowLabels.get(x), fColumnLabels.get(y), value);
        } else {
            throw new IndexOutOfBoundsException("The cell (" + x + "/" + y + ") does not exist.");
        }
    }

    /**
     * Returns the column labels.
     * 
     * @return the column labels.
     */
    public List<C> getColumnLabels() {
        return fColumnLabels;
    }

    /**
     * Returns the column label index.
     * 
     * @param columnLabel
     *            the label of the column for which the index is returned
     * @return the column label index.
     */
    public int getColumnLabelIndex(String columnLabel) {
        return getColumnLabels().indexOf(columnLabel);
    }

    /**
     * Returns the row label index.
     * 
     * @param rowLabel
     *            the label of the row for which the index is returned
     * @return the row label index.
     */
    public int getRowLabelIndex(String rowLabel) {
        return getRowLabels().indexOf(rowLabel);
    }

    /**
     * Checks whether this table contains a column with the given label.
     * 
     * @param columnLabel
     *            the column label to check for
     * @return <code>true</code>, if column label exists; <code>false</code> otherwise.
     */
    public boolean containsColumnLabel(C columnLabel) {
        return getColumnLabels().contains(columnLabel);
    }

    /**
     * Checks whether this table contains row with the given label.
     * 
     * @param rowLabel
     *            the row label to check for
     * @return <code>true</code>, if row label exists; <code>false</code> otherwise.
     */
    public boolean containsRowLable(R rowLabel) {
        return getRowLabels().contains(rowLabel);
    }

    /**
     * Writes the content to a file.
     * 
     * @param filePath
     *            the file path
     * @param separator
     *            to separate cells
     * @param notAvailable
     *            the value to print if cell contains <code>null</code>
     * @param writeHeader
     *            indicating whether column headers should be written or not
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void writeToFile(String filePath, String separator, String notAvailable, boolean writeHeader)
            throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)), true);
        try {
            StringBuilder builder = new StringBuilder();
            // Write column labels
            if (writeHeader) {
                for (int i = 0; i < getColumnLabels().size(); i++) {
                    C columnLabel = getColumnLabels().get(i);
                    if (i == 0) {
                        builder.append(columnLabel.toString());
                    } else {
                        builder.append(separator + columnLabel.toString());
                    }
                }

                builder.append("\n");
            }

            for (int i = 0; i < getRowLabels().size(); i++) {
                String rowLabel = getRowLabels().get(i).toString();
                if (!rowLabel.trim().equals("")) {
                    if (writeHeader) {
                        builder.append(rowLabel + ",");
                    }
                    for (int j = 0; j < getColumnLabels().size(); j++) {

                        V value = get(getRowLabels().get(i), getColumnLabels().get(j));

                        String cellString = (j == 0 ? "" : separator);
                        cellString += value == null ? notAvailable : value.toString();
                        builder.append(cellString);
                    }
                    builder.append("\n");
                }

                if ((i != 0) && (i % 500 == 0)) {
                    out.print(builder.toString());
                    out.flush();
                    out.close();
                    builder = new StringBuilder();
                }
            }
            out.print(builder.toString());
        } finally {
            out.flush();
            out.close();
        }
    }

    /**
     * Returns the row labels.
     * 
     * @return the row labels.
     */
    public List<R> getRowLabels() {
        return fRowLabels;
    }

    /**
     * Creates two dimensional array containing this tables data.
     * 
     * @return table data.
     */
    public Object[][] toArray() {
        Object[][] result = new Object[fRowLabels.size()][fColumnLabels.size()];

        for (int k = 0; k < fRowLabels.size(); k++) {
            for (int l = 0; l < fColumnLabels.size(); l++) {
                result[k][l] = fValues.get(new CompositeKey<R, C>(fRowLabels.get(k), fColumnLabels.get(l)));
            }
        }

        return result;
    }
}
