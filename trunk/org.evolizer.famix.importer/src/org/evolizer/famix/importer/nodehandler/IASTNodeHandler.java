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
package org.evolizer.famix.importer.nodehandler;

import org.eclipse.jdt.core.dom.ASTNode;
import org.evolizer.famix.model.entities.SourceAnchor;


/**
 * Interface to handle jdt AST nodes.
 * 
 * @author pinzger
 */
public interface IASTNodeHandler {

    /**
     * Processes a jdt AST node and extracts FAMIX related info.
     * 
     * @param node
     *            jdt AST node to visit.
     * @return true if contained nodes should be visited, otherwise false.
     */
    public boolean visit(ASTNode node);
    
    /**
     * Post-processes a jdt AST node.
     */
    public void endVisit();
    
    /**
     * Returns the jdt AST node.
     * 
     * @return the jdt AST node.
     */
    public ASTNode getASTNode();

    /**
     * Returns the source anchor of the processed AST Node.
     * 
     * @return Source anchor of processed AST Node.
     */
    public SourceAnchor getSourceAnchor();
}
