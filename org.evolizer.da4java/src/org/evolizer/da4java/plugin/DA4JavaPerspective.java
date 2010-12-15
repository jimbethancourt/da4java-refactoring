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
package org.evolizer.da4java.plugin;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.evolizer.da4java.birdseye.BirdsEyeView;
import org.evolizer.da4java.polymetricviews.PolymetricViewControllerView;
import org.evolizer.da4java.visibility.AssociationVisibilityView;
import org.evolizer.da4java.visibility.EntityVisibilityView;

/**
 * The DAForJava Eclipse perspective. 
 * 
 * @author pinzger
 */
public class DA4JavaPerspective implements IPerspectiveFactory {
    
    /** The perspecive ID. */
    public static final String PERSPECTIVE_ID = "org.evolizer.da4java.view.DA4JavaPerspective";

    /** 
     * {@inheritDoc}
     */
    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();

        IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, (float) 0.20f, editorArea);
        left.addView("org.eclipse.jdt.ui.PackageExplorer");
        left.addView(BirdsEyeView.VIEW_ID);

        IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT, (float) 0.75f, editorArea);
        right.addView(PolymetricViewControllerView.VIEW_ID);
        right.addView(EntityVisibilityView.VIEW_ID);
        right.addView(AssociationVisibilityView.VIEW_ID);
        right.addView(IPageLayout.ID_OUTLINE);

    }

}
