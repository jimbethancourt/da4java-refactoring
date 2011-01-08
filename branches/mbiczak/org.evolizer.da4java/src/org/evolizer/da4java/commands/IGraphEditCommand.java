/*
 * Copyright 2009 Martin Pinzger, Delft University of Technology,
 * and University of Zurich, Switzerland
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
package org.evolizer.da4java.commands;


/**
 * The graph edit command interface for executing, undoing, and redoing
 * commands. Each graph edit command "has to" implement this interface.
 * 
 * @author pinzger
 *
 */
public interface IGraphEditCommand {

    /**
     * Short description of the graph edit command.
     * 
     * @return The description
     */
    String getDescription();
    
    /**
     * Executing the command.
     */
    void execute();

    /**
     * Undo a graph edit command.
     */
    void undo();
    
    /**
     * Redo a graph edit command.
     */
    void redo();

}
