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
package org.evolizer.famix.importer.test;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.evolizer.core.hibernate.session.EvolizerSessionHandler;
import org.evolizer.core.preferences.EvolizerPreferences;
import org.evolizer.famix.importer.util.DAOModel;
import org.evolizer.famix.model.entities.AbstractFamixEntity;
import org.evolizer.famix.model.entities.FamixAssociation;
import org.evolizer.famix.model.entities.FamixModel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author pinzger
 */
public class FamixImporterDBTest extends FamixImporterTest {
//    private static Logger logger = FamixImporterTestPlugin.getLogManager().getLogger(FamixImporterDBTest.class.getName());

    private static FamixModel createdModel;
	private static DAOModel aDAOModel;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		FamixImporterTest.setUpBeforeClass();

//        project.getProject().setPersistentProperty(EvolizerPreferences.DB_HOST, "mysql://localhost");
//        project.getProject().setPersistentProperty(EvolizerPreferences.DB_NAME, "evolizer_test");
//        project.getProject().setPersistentProperty(EvolizerPreferences.DB_USER, "evolizer");
//        project.getProject().setPersistentProperty(EvolizerPreferences.DB_PASSWORD, "evolizer");
        project.getProject().setPersistentProperty(EvolizerPreferences.DB_USE_INMEMORY, "true");

//        EvolizerSessionHandler.getHandler().dropSchema(project.getProject());
//        EvolizerSessionHandler.getHandler().createSchema(project.getProject());
		
		createdModel = aModel;

        EvolizerSessionHandler.getHandler().getCurrentSession(project.getProject());
		aDAOModel = new DAOModel(EvolizerSessionHandler.getHandler().getDBUrl(project.getProject()), createdModel);			
		aDAOModel.store(null);

		aModel = aDAOModel.loadModel(createdModel.getName());
	}

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        FamixImporterTest.tearDownAfterClass();

        EvolizerSessionHandler.getHandler().cleanupHibernateSessions();
    }

	@Test
	public void testNumberOfAssociations(){
		int counter = 0;
		Set<AbstractFamixEntity> allEntities = createdModel.getFamixEntities();
		for (AbstractFamixEntity element : allEntities) {
			Set<FamixAssociation> associations = createdModel.getAssociations(element);
			for (FamixAssociation association : associations) {
				if (association.getFrom().getId() != null && association.getTo().getId() != null) {
					counter++;
				}
			}
		}

		int counter2 = 0;
		allEntities = aModel.getFamixEntities();
		for (AbstractFamixEntity element : allEntities) {
			counter2 = counter2 + aModel.getAssociations(element).size();
		}
		assertEquals(counter, counter2);
	}

	@Test
	public void testNumberOfEntities(){
		assertEquals(createdModel.getFamixEntities().size(), aModel.getFamixEntities().size());
	}
}
