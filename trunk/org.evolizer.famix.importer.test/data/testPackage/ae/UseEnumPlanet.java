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
package testPackage.ae;

/**
 *
 * @author pinzger
 *
 */
public class UseEnumPlanet {
    public void doSomething(double weight) {
        double mass = weight / EnumPlanet.EARTH.surfaceGravity();
        for (EnumPlanet p : EnumPlanet.values()) {
            System.out.printf("Your weight on %s is %f%n", p, p.surfaceWeight(mass));
        }
        
        double localG = EnumPlanet.MARS.G / 2.0;
    }
}
