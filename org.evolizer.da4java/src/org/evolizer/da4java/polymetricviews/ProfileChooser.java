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
package org.evolizer.da4java.polymetricviews;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.evolizer.da4java.DA4JavaPlugin;
import org.evolizer.da4java.plugin.DA4JavaGraphEditor;
import org.evolizer.da4java.polymetricviews.model.PolymetricViewProfile;
import org.evolizer.da4java.visibility.ViewConfigModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Add profile combobox, Save and Delete button to manage polymetric view profiles.
 * Provides corresponding functionality including the methods for persistence handling.
 * 
 * @author pinzger
 */
public class ProfileChooser {

    /** The Constant UPDATE_NODE_COLORS. */
    public static final String UPDATE_NODE_COLORS = "update_node_colors";
    
    /** The Constant UPDATE_NODE_HEIGHTS. */
    public static final String UPDATE_NODE_HEIGHTS = "update_node_heights";
    
    /** The Constant UPDATE_NODE_WIDTHS. */
    public static final String UPDATE_NODE_WIDTHS = "update_node_widths";
    
    /** The Constant UPDATE_GRAPH_EVENT. */
    public static final String UPDATE_GRAPH_EVENT = "update_graph";
    
    /** The Constant LOAD_PROFILE_EVENT. */
    public static final String LOAD_PROFILE_EVENT = "load_profile";
    
    /** The Constant REMOVE_PROFILE_EVENT. */
    public static final String REMOVE_PROFILE_EVENT = "remove_profile";
    
    /** The Constant SAVE_PROFILE_EVENT. */
    public static final String SAVE_PROFILE_EVENT = "save_profile";
    
    /** The Constant DISPOSING_VIEW_EVENT. */
    public static final String DISPOSING_VIEW_EVENT = "dispose_view";
    
    /** The polymetric file path. */
    private static final String POLYMETRIC_VIEW_PROFILE = "config/ProfileList.xml";
    
    /** The Constant sLogger. */
    private static Logger sLogger = DA4JavaPlugin.getLogManager().getLogger(ProfileChooser.class.getName()); 

    /** A map that stores the name of a profile to the corresponding profile. */
    private HashMap<String, PolymetricViewProfile> fProfileMap;
    
    /** The Document that is received from the FamixImporterPlugin and contains the Profile data from an XML-File. */
    private Document fProfileDocument;
    
    /** The profile chooser. */
    private Combo fProfileCombo;

    /** The View that is controlled by this controller. */
    private PolymetricViewControllerView fPVControllerView;

    /**
     * The constructor.
     * 
     * @param controllerView the controller view
     * @param parent the parent
     * @param style the style
     */
    public ProfileChooser(PolymetricViewControllerView controllerView, Composite parent, int style) {
        fPVControllerView = controllerView;
        fProfileMap = new HashMap<String, PolymetricViewProfile>();
        
        initGroup(parent, style);
        initProfiles();
    }

    /**
     * Update view.
     */
    public void updateView() {
        DA4JavaGraphEditor graphEditor = fPVControllerView.getGraphEditor();
        ViewConfigModel viewConfigModel = graphEditor.getPanel().getViewConfigModel();
        PolymetricViewProfile profile = viewConfigModel.getProfile();

        if (profile.getName() != null && profile.getName().length() > 0) {
            fPVControllerView.select(fProfileCombo, profile.getName());
        } else {
            sLogger.warn("Profile name of editor " + graphEditor.getPartName() + " is null!");
        }
    }
    
    /**
     * Gets the currently selected text.
     * 
     * @return the text
     */
    public String getText() {
        return fProfileCombo.getText();
    }
    
    /**
     * Dispose the component.
     */
    public void dispose() {
        prepareDocToWriteToFile();
    }

    /**
     * Initialize the list of available profiles.
     */
    private void initProfiles() {
        fProfileDocument = loadPolymetricViewControllerProfiles();
        initStoredProfiles();
        fProfileCombo.setItems(getProfileList());
    }
    
    /**
     * Returns an array of Profiles that will be available in the
     * polymetricViewControllerView.
     * 
     * @return list a list of all profiles saved
     */
    private String[] getProfileList() {
        String[] list = fProfileMap.keySet().toArray(new String[]{});
        return list;
    }

    /**
     * Initializes all profiles given in the profiles XML (check FamixImporterPlugin).
     */
    private void initStoredProfiles() {
        Element docElem = fProfileDocument.getDocumentElement();
        NodeList list = docElem.getElementsByTagName("Profile");
        
        if (list != null && list.getLength() > 0) {
            for (int i = 0; i < list.getLength(); i++) {
                Element e = (Element) list.item(i);
                createProfile(e);
            }
        }
    }
    
    /**
     * Creates a Profile for Element of the XML and saves the profile in the fProfileMap.
     * 
     * @param e the e
     */
    private void createProfile(Element e) {
        String name = e.getAttribute("name");
        String heightMetric = getAttributeAsString(e, "heightMetric");
        String widthMetric = getAttributeAsString(e, "widthMetric");
        String colorMetric = getAttributeAsString(e, "colorMetric");

        fProfileMap.put(name, new PolymetricViewProfile(name, heightMetric, widthMetric, colorMetric));
    }

    /**
     * Returns the Value with the given tagname from the given element.
     * 
     * @param e the e
     * @param tagname the tagname
     * 
     * @return the attribute as string
     */
    private String getAttributeAsString(Element e, String tagname) {
        String value = null;
        NodeList attributes = e.getElementsByTagName(tagname);
        if (attributes != null && attributes.getLength() > 0) {
            Element result = (Element) attributes.item(0);
            value = result.getFirstChild().getNodeValue();
        }
        
        return value;
    }
    
    /**
     * Inits the group.
     * 
     * @param parent the parent
     * @param style the style
     */
    private void initGroup(Composite parent, int style) {
//        GridLayout parentLayout = new GridLayout();
//        parentLayout.numColumns = 1;
//        parent.setLayout(parentLayout);
//        parent.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        
        Group profileChooserGroup = new Group(parent, style);
        profileChooserGroup.setText("Profiles");

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        profileChooserGroup.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        profileChooserGroup.setLayoutData(gridData);

        new Label(profileChooserGroup, SWT.NULL).setText("Available Profiles:");

        fProfileCombo = new Combo(profileChooserGroup, SWT.NULL);
        fProfileCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fProfileCombo.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                processLoadEvent();
            }
        });

        Button saveButton = new Button(profileChooserGroup, SWT.Selection);
        saveButton.setText("Save");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        saveButton.setLayoutData(gridData);
        saveButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                processSaveEvent();
            }
        });

        Button deleteButton = new Button(profileChooserGroup, SWT.Selection);
        deleteButton.setText("Delete");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        deleteButton.setLayoutData(gridData);
        deleteButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                processRemoveEvent();
            }
        });

    }
    
    /**
     * Sets the selected profile.
     */
    private void processLoadEvent() {
        String name = fProfileCombo.getText();
        PolymetricViewProfile profile = fProfileMap.get(name);
        
        // TODO: set the profile in the view config model, which then should notify the checkboxes to update
        fPVControllerView.select(fPVControllerView.getWidthMetricChooser(), profile.getWidthMetric());
        fPVControllerView.select(fPVControllerView.getHeightMetricChooser(), profile.getHeightMetric());
        fPVControllerView.select(fPVControllerView.getColorMetricChooser(), profile.getColorMetric());
        
        ViewConfigModel viewConfigModel = fPVControllerView.getGraphEditor().getPanel().getViewConfigModel();
        viewConfigModel.updatePolymetricViewsProfile(ViewConfigModel.UPDATE_GRAPH_EVENT, profile);
    }
    
    /**
     * Save the actual polymetric view profile.
     */
    private void processSaveEvent() {
        String name = fProfileCombo.getText();
        String heightVal = fPVControllerView.getHeightMetricChooser().getText();
        String widthVal = fPVControllerView.getWidthMetricChooser().getText();
        String colorVal = fPVControllerView.getColorMetricChooser().getText();
        PolymetricViewProfile profile = new PolymetricViewProfile(name, heightVal, widthVal, colorVal);
        
        if (fProfileMap.containsKey(profile.getName())) {
            sLogger.info("Update profile " + profile.getName());
        } else {
            sLogger.info("Add new profile " + profile.getName());
            fProfileCombo.add(profile.getName());
        }
        fProfileMap.put(profile.getName(), profile);
        
        prepareDocToWriteToFile();
    }
    
    /**
     * Removes the selected profile.
     */
    private void processRemoveEvent() {
        String name = fProfileCombo.getText();
        
        if (fProfileMap.containsKey(name) && !name.equalsIgnoreCase(PolymetricViewProfile.DEFAULT)) {
            sLogger.info("Remove profile " + name);
            
            fProfileMap.remove(name);
            fProfileCombo.remove(name);

            prepareDocToWriteToFile();
        }
    }
    
    /**
     * FamixMethod that builds the new ElementTree for the XML to save the profiles
     * and invokes the FamixImporterPlugin to save the profiles.
     */
    private void prepareDocToWriteToFile() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder;

        try {
            builder = dbf.newDocumentBuilder();

            fProfileDocument = builder.newDocument();
            Element rootElem = fProfileDocument.createElement("ProfileList");
            fProfileDocument.appendChild(rootElem);

            for (String elem : fProfileMap.keySet()) {
                Element profileElem = createProfileElement(fProfileMap
                        .get(elem));
                rootElem.appendChild(profileElem);
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        
        savePolymetricViewProfiles(fProfileDocument);
    }
    
    /**
     * Helper method needed to reconstruct a valid XML-File with the Profile information.
     * 
     * @param profile the profile
     * 
     * @return Element for the given Profile
     */
    private Element createProfileElement(PolymetricViewProfile profile) {
        Element profileElem = fProfileDocument.createElement("Profile");
        profileElem.setAttribute("name", profile.getName());
        
        Element height = fProfileDocument.createElement("heightMetric");
        Text heightText = fProfileDocument.createTextNode(profile.getHeightMetric());
        height.appendChild(heightText);
        profileElem.appendChild(height);
        
        Element width = fProfileDocument.createElement("widthMetric");
        Text widthText = fProfileDocument.createTextNode(profile.getWidthMetric());
        width.appendChild(widthText);
        profileElem.appendChild(width);
        
        Element color = fProfileDocument.createElement("colorMetric");
        Text colorText = fProfileDocument.createTextNode(profile.getColorMetric());
        color.appendChild(colorText);
        profileElem.appendChild(color);
        
        return profileElem;
    }
    
       
    /**
     * Loads the ProfileListXML file and parses it to be used by PolymetricViewProfileController.
     * 
     * @return Document profilesDocument
     */
    private Document loadPolymetricViewControllerProfiles() {
        System.out.println("Loading polymetric view config file");
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document polymetricViewProfileDocument = null;
        try {
            db = dbf.newDocumentBuilder();
            InputStream is = DA4JavaPlugin.getDefault().openBundledFile(POLYMETRIC_VIEW_PROFILE);
            polymetricViewProfileDocument = db.parse(is);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return polymetricViewProfileDocument;
    }

    /**
     * Saves a given Document to the ProfileListXML.
     * 
     * @param doc the document to save
     */
    private void savePolymetricViewProfiles(Document doc) {
      TransformerFactory xformFactory = TransformerFactory.newInstance();
      Transformer idTransform;
      try {
          idTransform = xformFactory.newTransformer();
          Source input = new DOMSource(doc);
          Result output = new StreamResult(new FileOutputStream(DA4JavaPlugin.getDefault().getFile(POLYMETRIC_VIEW_PROFILE)));
          idTransform.transform(input, output);
      } catch (TransformerConfigurationException e) {
          e.printStackTrace();
      } catch (FileNotFoundException e) {
          e.printStackTrace();
      } catch (IOException e) {
          e.printStackTrace();
      } catch (TransformerException e) {
          e.printStackTrace();
      }
    }
}
