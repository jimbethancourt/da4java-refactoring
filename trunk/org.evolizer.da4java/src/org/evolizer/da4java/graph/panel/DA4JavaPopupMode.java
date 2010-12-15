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
package org.evolizer.da4java.graph.panel;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.evolizer.da4java.commands.AbstractGraphEditCommand;
import org.evolizer.da4java.commands.FolderNodeHandleCommand;
import org.evolizer.da4java.commands.NavigateToSourceCodeAction;
import org.evolizer.da4java.commands.additions.AddDependenciesBetweenEntities;
import org.evolizer.da4java.commands.additions.AddDescendantsAndDependencies;
import org.evolizer.da4java.commands.additions.AddEntitiesViaInDependenciesCommand;
import org.evolizer.da4java.commands.additions.AddEntitiesViaInOutDependenciesCommand;
import org.evolizer.da4java.commands.additions.AddEntitiesViaOutDependenciesCommand;
import org.evolizer.da4java.commands.filters.FilterDependenciesBetweenNodes;
import org.evolizer.da4java.commands.filters.FilterInternalDependenciesOfNode;
import org.evolizer.da4java.commands.filters.FilterNotConnectedNodes;
import org.evolizer.da4java.commands.filters.FilterSelectedEntities;
import org.evolizer.da4java.commands.filters.KeepInDependenciesOfNode;
import org.evolizer.da4java.commands.filters.KeepInOutDependenciesOfNode;
import org.evolizer.da4java.commands.filters.KeepOutDependenciesOfNode;
import org.evolizer.da4java.commands.filters.KeepSelectedDependencies;
import org.evolizer.da4java.commands.filters.KeepSelectedNodes;
import org.evolizer.da4java.graph.data.EdgeGrouper;
import org.evolizer.da4java.graph.data.GraphLoader;
import org.evolizer.da4java.graph.utils.FamixAssociationMap;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.view.Graph2D;
import y.view.NodeRealizer;
import y.view.PopupMode;
import y.view.hierarchy.GroupNodeRealizer;

/**
 * The context sensitive popup menu that is shown when a right click on 
 * the graph view occurs.
 * 
 * @author Katja Graefenhain, Martin Pinzger
 */
public class DA4JavaPopupMode extends PopupMode {
    
    /** The logger. */
//    private static Logger fLogger = DA4JavaPlugin.getLogManager().getLogger(DA4JavaPopupMode.class.getName());

    /** The graph panel. */
    private DA4JavaGraphPanel fGraphPanel;

    /**
     * The constructor.
     * 
     * @param graphPanel The graph panel
     */
    public DA4JavaPopupMode(DA4JavaGraphPanel graphPanel) {
        fGraphPanel = graphPanel;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public JPopupMenu getNodePopup(final Node node) {
        JPopupMenu pm = new JPopupMenu();
        pm.add(new ShowNodeInfo(node));
        JMenu folderMenu = getFolderMenu(node);
        pm.add(new JPopupMenu.Separator());
        if (folderMenu != null) {
            pm.add(folderMenu);
        }
        pm.add(getFilterMenu(node));
        // pm.add(new JPopupMenu.Separator());
        pm.add(getAddMenu(node));
        pm.add(new JPopupMenu.Separator());

        pm.add(new NavigateToSourceCodeAction(node, getGraphLoader()));
        return pm;
    }


    /** 
     * {@inheritDoc}
     */
    @Override
    public JPopupMenu getPaperPopup(double x, double y) {
        JPopupMenu pm = new JPopupMenu();
        JMenu filterMenu = new JMenu("Filter");
        GraphEditPopupMenuAction action;
        //      filterMenu.add(new FilterByDependencyStrength());

        //      action = new GraphEditPopupMenuAction("FamixPackage-internal dependencies", fGraphPanel);
        //      action.setDescription("Filter package-internal dependencies");
        //      action.setCommand(new ShowDependenciesAcrossPackages(getGraphLoader(), getHierarchicEdgeGrouper()));
        //      filterMenu.add(action);

        action = new GraphEditPopupMenuAction("Not-connected nodes", fGraphPanel);
        action.setDescription("Filter not-connected nodes");
        action.setCommand(new FilterNotConnectedNodes(getGraphLoader(), getHierarchicEdgeGrouper()));
        filterMenu.add(action);

        pm.add(filterMenu);
        return pm;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public JPopupMenu getEdgePopup(final Edge edge) {
        JPopupMenu pm = new JPopupMenu();
        pm.add(new ShowEdgeInfo(edge));
        pm.add(new JPopupMenu.Separator());
        JMenu filterMenu = new JMenu("Filter");
        filterMenu.setToolTipText("Varios filters that take the selected edge as input.");

        List<Object> listWithObject = new ArrayList<Object>();
        listWithObject.add(edge);
        List<Edge> listWithEdge = new ArrayList<Edge>();
        listWithEdge.add((Edge) edge);

        GraphEditPopupMenuAction action;
        action = new GraphEditPopupMenuAction("Selected edge", fGraphPanel);
        action.setDescription("Filter selected edge");
        action.setCommand(new FilterSelectedEntities(listWithObject, getGraphLoader(), getHierarchicEdgeGrouper()));
        filterMenu.add(action);
        action = new GraphEditPopupMenuAction("Keep selected edge", fGraphPanel);
        action.setDescription("Keep selected edge and corresponding nodes");
        action.setCommand(new KeepSelectedDependencies(listWithEdge, getGraphLoader(), getHierarchicEdgeGrouper()));
        filterMenu.add(action);

        pm.add(filterMenu);
        return pm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JPopupMenu getSelectionPopup(double arg0, double arg1) {
        List<Node> selectedNodes = new ArrayList<Node>();
        List<Edge> selectedEdges = new ArrayList<Edge>();
        List<Object> selectedElements = new ArrayList<Object>();
        for (NodeCursor nc = getGraphLoader().getGraph().selectedNodes(); nc.ok(); nc.next()) {
            Node node = nc.node();
            selectedNodes.add(node);
            selectedElements.add(node);
        }
        for (EdgeCursor ec = getGraphLoader().getGraph().selectedEdges(); ec.ok(); ec.next()) {
            Edge edge = ec.edge();
            selectedEdges.add(edge);
            selectedElements.add(edge);
        }

        JPopupMenu pm = new JPopupMenu();

        // Filter menu
        JMenu filterMenu = new JMenu("Filter");
        filterMenu.setToolTipText("Varios filters that take the selection as input.");

        GraphEditPopupMenuAction action = null;
        action = new GraphEditPopupMenuAction("Selected elements", fGraphPanel);
        action.setDescription("Filter selected elements");
        action.setCommand(new FilterSelectedEntities(selectedElements, getGraphLoader(), getHierarchicEdgeGrouper()));
        filterMenu.add(action);

        if (selectedNodes.size() > 1) {
            filterMenu.add(getFilterMenuKeepDependenciesBetweenNodes(selectedNodes));
            filterMenu.add(getFilterMenuDependenciesBetweenNodes(selectedNodes));
        }

        if (selectedEdges.size() > 1) {
            action = new GraphEditPopupMenuAction("Keep selected edges", fGraphPanel);
            action.setDescription("Keep selected edges and corresponding nodes");
            action.setCommand(new KeepSelectedDependencies(selectedEdges, getGraphLoader(), getHierarchicEdgeGrouper()));
            filterMenu.add(action);
        }
        pm.add(filterMenu);

        // Add menu
        pm.addSeparator();
        JMenu addMenu = new JMenu("Add");

        action = new GraphEditPopupMenuAction("Descendants and dependencies", fGraphPanel);
        action.setDescription("Add internal dependencies of the selected node(s) and their dependencies to other nodes in the graph");
        action.setCommand(new AddDescendantsAndDependencies(selectedNodes, fGraphPanel.getGraphLoader(), fGraphPanel.getEdgeGrouper()));
        addMenu.add(action);

        if (selectedNodes.size() > 1) {
            addMenu.add(getAddMenuDependenciesBetweenNodes(selectedNodes));
        }
        pm.add(addMenu);

        return pm;
    }

    private JMenu getFolderMenu(final Node node) {
        JMenu folderMenu = null;
        NodeRealizer nr = ((Graph2D) node.getGraph()).getRealizer(node);
        if (nr instanceof GroupNodeRealizer) {
            folderMenu = new JMenu("Folder");
            GroupNodeRealizer gnr = (GroupNodeRealizer) nr;
            GraphEditPopupMenuAction action = null;
            if (gnr.isGroupClosed()) {
                action = new GraphEditPopupMenuAction("Expand", fGraphPanel);
                action.setDescription("Expand selected folder node");
                action.setCommand(new FolderNodeHandleCommand(getGraphLoader(), getHierarchicEdgeGrouper(), node, true));
            } else {
                action = new GraphEditPopupMenuAction("Collapse", fGraphPanel);
                action.setDescription("Collapse selected folder node");
                action.setCommand(new FolderNodeHandleCommand(getGraphLoader(), getHierarchicEdgeGrouper(), node, false));
            }
            folderMenu.add(action);
        }

        return folderMenu;
    }

    private JMenu getFilterMenu(final Node node) {
        JMenu filterMenu = new JMenu("Filter");
        filterMenu.setToolTipText("Varios filters that take the selected node as input.");
        final List<Node> listWithNode = new ArrayList<Node>();
        listWithNode.add(node);

        final List<Object> listWithObject = new ArrayList<Object>();
        listWithObject.add(node);
        GraphEditPopupMenuAction action = null;
        action = new GraphEditPopupMenuAction("Selected node", fGraphPanel);
        action.setDescription("Hide the selected node");
        action.setCommand(new FilterSelectedEntities(listWithObject, getGraphLoader(), getHierarchicEdgeGrouper()));
        filterMenu.add(action);

        action = new GraphEditPopupMenuAction("Internal dependencies", fGraphPanel);
        action.setDescription("Filter internal edges and lonely nodes of the selected folder node");
        action.setCommand(new FilterInternalDependenciesOfNode(node, getGraphLoader(), getHierarchicEdgeGrouper()));
        filterMenu.add(action);

        action = new GraphEditPopupMenuAction("Keep selected node", fGraphPanel);
        action.setDescription("Keep selected node");
        action.setCommand(new KeepSelectedNodes(listWithNode, null, getGraphLoader(), getHierarchicEdgeGrouper()));
        filterMenu.add(action);

        // submenus filter for incoming/outgoing dependencies
        filterMenu.add(getFilterMenuForInOutDependencies(node));
        filterMenu.add(getFilterMenuForIncomingDependencies(node));
        filterMenu.add(getFilterMenuForOutgoingDependencies(node));

        return filterMenu;
    }

    private JMenu getFilterMenuForInOutDependencies(final Node node) {
        JMenu inoutFilterMenu = new JMenu("Keep dependencies");
        GraphEditPopupMenuAction action = null;
        int index = 0;
        for (final String associationName : FamixAssociationMap.getInstance().getNames()) {
            action = new GraphEditPopupMenuAction(associationName, fGraphPanel);
            action.setDescription("Keep '" + associationName + "' incoming dependencies and corresponding connected nodes");
            action.setCommand(new KeepInOutDependenciesOfNode(node, FamixAssociationMap.getInstance().getType(associationName), getGraphLoader(), getHierarchicEdgeGrouper()));
            inoutFilterMenu.add(action);
            index++;
        }

        return inoutFilterMenu;
    }

    private JMenu getFilterMenuForIncomingDependencies(final Node node) {
        JMenu incomingFilterMenu = new JMenu("Keep incoming dependencies");
        GraphEditPopupMenuAction action = null;
        int index = 0;
        for (final String associationName : FamixAssociationMap.getInstance().getNames()) {
            action = new GraphEditPopupMenuAction(associationName, fGraphPanel);
            action.setDescription("Keep '" + associationName + "' incoming dependencies and corresponding connected nodes");
            action.setCommand(new KeepInDependenciesOfNode(node, FamixAssociationMap.getInstance().getType(associationName), getGraphLoader(), getHierarchicEdgeGrouper()));
            incomingFilterMenu.add(action);
            index++;
        }

        return incomingFilterMenu;
    }

    private JMenu getFilterMenuForOutgoingDependencies(final Node node) {
        JMenu outgointFilterMenu = new JMenu("Keep outgoing Dependencies");
        GraphEditPopupMenuAction action = null;
        int index = 0;
        for (final String associationName : FamixAssociationMap.getInstance().getNames()) {
            action = new GraphEditPopupMenuAction(associationName, fGraphPanel);
            action.setDescription("Keep '" + associationName + "' outgoing dependencies and corresponding connected nodes");
            action.setCommand(new KeepOutDependenciesOfNode(node, FamixAssociationMap.getInstance().getType(associationName), getGraphLoader(), getHierarchicEdgeGrouper()));
            outgointFilterMenu.add(action);
            index++;
        }

        return outgointFilterMenu;
    }

    private JMenu getAddMenu(final Node node) {
        JMenu addMenu = new JMenu("Add");
        addMenu.setToolTipText("Add dependencies of selected node. Edges and " 
                + "nodes that are not yet in the graph are added.");

        addMenu.add(getAddMenuForAllDependencies(node));
        addMenu.add(getAddMenuForIncomingDependencies(node));
        addMenu.add(getAddMenuForOutgoingDependencies(node));

        List<Node> listWithNode = new ArrayList<Node>();
        listWithNode.add(node);
        GraphEditPopupMenuAction action = new GraphEditPopupMenuAction("Descendants & Dependencies", fGraphPanel);
        action.setCommand(new AddDescendantsAndDependencies(listWithNode, getGraphLoader(), getHierarchicEdgeGrouper()));
        addMenu.add(action);

        return addMenu;
    }

    private JMenu getAddMenuForAllDependencies(final Node node) {
        JMenu addAllDependenciesMenu = new JMenu("All dependencies");
        GraphEditPopupMenuAction action = null;
        int index = 0;
        for (final String associationName : FamixAssociationMap.getInstance().getNames()) {
            action = new GraphEditPopupMenuAction(associationName, fGraphPanel);
            action.setDescription("Add '" + associationName + "' dependencies of selected node and connected nodes");
            action.setCommand(new AddEntitiesViaInOutDependenciesCommand(node, getGraphLoader(), getHierarchicEdgeGrouper(), FamixAssociationMap.getInstance().getType(associationName)));
            addAllDependenciesMenu.add(action);
            index++;
        }
        return addAllDependenciesMenu;
    }

    private JMenu getAddMenuForIncomingDependencies(final Node node) {
        JMenu addIncomingDependencies = new JMenu("Incoming dependencies");
        GraphEditPopupMenuAction action = null;
        int index = 0;
        for (final String associationName : FamixAssociationMap.getInstance().getNames()) {
            action = new GraphEditPopupMenuAction(associationName, fGraphPanel);
            action.setDescription("Add incoming '" + associationName + "' dependencies of selected node and connected nodes");
            action.setCommand(new AddEntitiesViaInDependenciesCommand(node, getGraphLoader(), getHierarchicEdgeGrouper(), FamixAssociationMap.getInstance().getType(associationName)));
            addIncomingDependencies.add(action);
            index++;
        }

        return addIncomingDependencies;
    }

    private JMenu getAddMenuForOutgoingDependencies(final Node node) {
        JMenu addIncomingDependencies = new JMenu("Outgoing dependencies");
        GraphEditPopupMenuAction action = null;
        int index = 0;
        for (final String associationName : FamixAssociationMap.getInstance().getNames()) {
            action = new GraphEditPopupMenuAction(associationName, fGraphPanel);
            action.setDescription("Add outgoing '" + associationName + "' dependencies of selected node and connected nodes");
            action.setCommand(new AddEntitiesViaOutDependenciesCommand(node, getGraphLoader(), getHierarchicEdgeGrouper(), FamixAssociationMap.getInstance().getType(associationName)));
            addIncomingDependencies.add(action);
            index++;
        }

        return addIncomingDependencies;
    }

    private JMenu getFilterMenuKeepDependenciesBetweenNodes(List<Node> nodes) {
        JMenu filterMenuKeepDependenciesBetweenNodes = new JMenu("Keep selected nodes and associations");

        GraphEditPopupMenuAction action = null;
        int index = 0;
        for (final String associationName : FamixAssociationMap.getInstance().getNames()) {
            action = new GraphEditPopupMenuAction(associationName, fGraphPanel);
            action.setDescription("Keep selected nodes and '" + associationName + "' between them");
            action.setCommand(new KeepSelectedNodes(nodes, FamixAssociationMap.getInstance().getType(associationName), getGraphLoader(), getHierarchicEdgeGrouper()));
            filterMenuKeepDependenciesBetweenNodes.add(action);
            index++;
        }

        return filterMenuKeepDependenciesBetweenNodes;
    }

    private JMenu getFilterMenuDependenciesBetweenNodes(List<Node> nodes) {
        JMenu filterMenuDependenciesBetweenNodes = new JMenu("Edges between selected nodes");

        GraphEditPopupMenuAction action = null;
        int index = 0;
        for (final String associationName : FamixAssociationMap.getInstance().getNames()) {
            action = new GraphEditPopupMenuAction(associationName, fGraphPanel);
            action.setDescription("Filter '" + associationName + "' between selected nodes");
            action.setCommand(new FilterDependenciesBetweenNodes(nodes, FamixAssociationMap.getInstance().getType(associationName), getGraphLoader(), getHierarchicEdgeGrouper()));
            filterMenuDependenciesBetweenNodes.add(action);
            index++;
        }

        return filterMenuDependenciesBetweenNodes;
    }

    private JMenu getAddMenuDependenciesBetweenNodes(List<Node> nodes) {
        JMenu addMenuDependenciesBetweenNodes = new JMenu("Edges between selected nodes");

        GraphEditPopupMenuAction action = null;
        int index = 0;
        for (final String associationName : FamixAssociationMap.getInstance().getNames()) {
            action = new GraphEditPopupMenuAction(associationName, fGraphPanel);
            action.setDescription("Add '" + associationName + "' between selected nodes");
            action.setCommand(new AddDependenciesBetweenEntities(nodes, FamixAssociationMap.getInstance().getType(associationName), getGraphLoader(), getHierarchicEdgeGrouper()));
            addMenuDependenciesBetweenNodes.add(action);
            index++;
        }
        return addMenuDependenciesBetweenNodes;
    }

    /**
     * Node info action.
     */
    private class ShowNodeInfo extends AbstractAction {
        
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 6341855038343117750L;
        
        /** The selected node. */
        private Node fSelectedNode;

        /**
         * The constructor.
         * 
         * @param selectedNode The selected node
         */
        public ShowNodeInfo(Node selectedNode) {
            super("Node Info");
            fSelectedNode = selectedNode;
        }

        /** 
         * {@inheritDoc}
         */
        public void actionPerformed(ActionEvent e) {
            String info = getGraphLoader().getGraph().getNodeInfo(fSelectedNode);
            JOptionPane.showMessageDialog(fGraphPanel.getView(), info);
        }
    }

    /**
     * Action info action.
     */
    private class ShowEdgeInfo extends AbstractAction {
        
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 800007731087348726L;
        
        /** The selected edge. */
        private Edge fSelectedEdge;

        /**
         * The constructor.
         * 
         * @param fSelectedEdge the selected edge
         */
        public ShowEdgeInfo(Edge selectedEdge) {
            super("Edge Info");
            this.fSelectedEdge = selectedEdge;
        }

        /** 
         * {@inheritDoc}
         */
        public void actionPerformed(ActionEvent e) {
            String info = getGraphLoader().getGraph().getEdgeInfo(fSelectedEdge);
            JOptionPane.showMessageDialog(fGraphPanel.getView(), info);
        }
    }

    /**
     * Generic class for graph edit actions.
     */
    private class GraphEditPopupMenuAction extends AbstractAction {
        
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = -5899824858103919698L;
        
        /** The graph panel. */
        private DA4JavaGraphPanel fGraphPanel;
        
        /** The command. */
        private AbstractGraphEditCommand fCommand;

        /**
         * The constructor.
         * 
         * @param name The action name
         * @param graphPanel The graph panel
         */
        public GraphEditPopupMenuAction(String name, DA4JavaGraphPanel graphPanel) {
            super(name);
            fGraphPanel = graphPanel;
        }

        /** 
         * {@inheritDoc}
         */
        public void actionPerformed(ActionEvent arg0) {
            getGraphPanel().getCommandController().executeCommand(getCommand());
        }

        /**
         * Sets the description.
         * 
         * @param description The new description
         */
        public void setDescription(String description) {
            super.putValue(Action.SHORT_DESCRIPTION, description);
        }
        
        /**
         * Sets the name.
         * 
         * @param name the new name
         */
//        public void setName(String name){
//            super.putValue(Action.NAME, name);
//        }
        
        /**
         * Gets the graph panel.
         * 
         * @return the graph panel
         */
        public DA4JavaGraphPanel getGraphPanel() {
            return fGraphPanel;
        }
        
        /**
         * Sets the command.
         * 
         * @param command the new command
         */
        public void setCommand(AbstractGraphEditCommand command) {
            fCommand = command;
        }
        
        /**
         * Gets the command.
         * 
         * @return the command
         */
        public AbstractGraphEditCommand getCommand() {
            return fCommand;
        }

    }

    /**
     * Returns the graph loader.
     * 
     * @return the graphLoader from the graphPanel instance
     */
    public GraphLoader getGraphLoader() {
        return fGraphPanel.getGraphLoader();
    }

    /**
     * Gets the hierarchic edge grouper.
     * 
     * @return the hierarchic edge grouper
     */
    public EdgeGrouper getHierarchicEdgeGrouper() {
        return fGraphPanel.getEdgeGrouper();
    }
}
