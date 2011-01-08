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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.evolizer.da4java.DA4JavaPlugin;

/**
 * This class provides the functionality to:
 * <ul>
 * <li>executed graph edit commands</li>
 * <li>undo graph edit commands</li> and
 * <li>redo graph edit commands</li>
 * </ul>
 * 
 * Clients are notified of the corresponding action via property change events.
 * 
 * @author pinzger
 */
public class CommandController {
    /** Denotes a command "execute" event. */
    public static final String COMMAND_EXECUTED = "command_executed";
    /** Denotes a command "undone" event. */
    public static final String COMMAND_UNDONE = "command_undone";
    /** Denotes a command "redone" event. */
    public static final String COMMAND_REDONE = "command_redone";
    
    /** The logger instance. */
    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(CommandController.class.getName());
    
    /** List of executed commands. */
    private LinkedList<IGraphEditCommand> fCommands;
    
    /** Points to the last executed command in list of commands. */
    private int fCommandIndex;

    /** Container of listeners to executed, undone, redone events. */
    private PropertyChangeSupport fPropertyChangeSupport;

    /**
     * The default constructor.
     * 
     * Initializes an empty list of commands.
     */
    public CommandController() {
        fCommands = new LinkedList<IGraphEditCommand>();
        fCommandIndex = 0; 

        fPropertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Executes the given edit command.
     * 
     * @param command The FilterCommand to execute.
     */
    public void executeCommand(AbstractGraphEditCommand command) {
        command.execute();
        while (fCommands.size() > fCommandIndex) {
            fCommands.removeLast();
        }
        fCommands.addLast(command);
        fCommandIndex++;

        fPropertyChangeSupport.firePropertyChange(CommandController.COMMAND_EXECUTED, null, command);
    }

    /**
     * Undoes the last executed edit command.
     */
    public void undoCommand() {
        if (canUndo()) {
            IGraphEditCommand command = fCommands.get(fCommandIndex - 1);
            command.undo();
            fCommandIndex--;

            fPropertyChangeSupport.firePropertyChange(CommandController.COMMAND_UNDONE, null, command);
        } else {
            sLogger.info("Cannot undo command - no command applied");
        }
    }

    /**
     * Re-executes the last undone edit command.
     */
    public void redoCommand() {
        if (canRedo()) {
            IGraphEditCommand command = fCommands.get(fCommandIndex);
            command.redo();
            fCommandIndex++;

            fPropertyChangeSupport.firePropertyChange(CommandController.COMMAND_REDONE, null, command);
        } else {
            sLogger.info("No command for re-execution available");
        }
    }

    /**
     * Registers a property change listener.
     * 
     * @param listener The property change listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        fPropertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes the property change listener.
     * 
     * @param listener The property change listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        fPropertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Check whether a command can be undone.
     * 
     * @return True, if a command can be undone, otherwise false.
     */
    public boolean canUndo() {
        boolean canUndo = false;
        if (fCommandIndex > 0) {
            canUndo = true;
        }
        return canUndo;
    }

    /**
     * Check whether a command can be redone.
     * 
     * @return True, if a command can be redone, otherwise false.
     */
    public boolean canRedo() {
        boolean canRedo = false;
        if (fCommands.size() > fCommandIndex) {
            canRedo = true;
        }
        return canRedo;
    }
}
