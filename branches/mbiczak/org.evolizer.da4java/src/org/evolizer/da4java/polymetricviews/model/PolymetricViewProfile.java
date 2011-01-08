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
package org.evolizer.da4java.polymetricviews.model;

/**
 * This class is needed by the PolymetricViewProfileController and stores the
 * different height-, width- and color metrics that form together a profile.
 * 
 * @author mark, pinzger
 */
public class PolymetricViewProfile {
    
    /** The Constant DEFAULT. */
    public static final String DEFAULT = "Default";

    /** The fProfileName of the profile. */
    private String fProfileName;

    /** The metric that is represented by the nodes height. */
    private String fHeightMetric;

    /** The metric that is represented by the nodes width. */
    private String fWidthMetric;

    /** The metric that is represented by the nodes color. */
    private String fColorMetric;

    /**
     * The constructor.
     * 
     * @param name The profile name
     * @param height   The height metric fProfileName
     * @param width    The width metric fProfileName
     * @param color    The color metric fProfileName
     */
    public PolymetricViewProfile(String name, String height, String width, String color) {
        this.fColorMetric = color;
        this.fHeightMetric = height;
        this.fWidthMetric = width;
        this.fProfileName = name;
    }

    /**
     * Returns the profile fProfileName.
     * 
     * @return The profile fProfileName.
     */
    public String getName() {
        return fProfileName;
    }

    /**
     * Returns the height metric.
     * 
     * @return The height metric.
     */
    public String getHeightMetric() {
        return fHeightMetric;
    }

    /**
     * Returns the width metric.
     * 
     * @return The width metric.
     */
    public String getWidthMetric() {
        return fWidthMetric;
    }

    /**
     * Returns the color metric.
     * 
     * @return The color metric.
     */
    public String getColorMetric() {
        return fColorMetric;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getName();
    }
}
