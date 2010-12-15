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
package org.evolizer.model.resources.factories;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.evolizer.core.exceptions.EvolizerException;
import org.evolizer.core.hibernate.session.EvolizerSessionHandler;
import org.evolizer.core.hibernate.session.api.IEvolizerSession;
import org.evolizer.model.resources.entities.humans.Person;

/**
 * This class is not intended to be used yet!.
 * 
 * @author wuersch
 */
public class PersonFactory {

    private Set<Person> fCachedPersons;
    private IEvolizerSession fSession;

    /**
     * Instantiates a new person factory.
     * 
     * @param project
     *            the project
     * @throws EvolizerException
     *             the evolizer exception
     */
    public PersonFactory(IProject project) throws EvolizerException {
        fCachedPersons = new HashSet<Person>();
        fSession = EvolizerSessionHandler.getHandler().getCurrentSession(project);
    }

    /**
     * Creates a new Person object.
     * 
     * @param firstName
     *            the first name
     * @param lastName
     *            the last name
     * @param email
     *            the email
     * @param nicknames
     *            the nicknames
     * @return the person
     */
    public Person createPerson(String firstName, String lastName, String email, String... nicknames) {
        Person result = new Person();

        result.setFirstName(firstName);
        result.setLastName(lastName);
        result.setEmail(email);

        for (String nickname : nicknames) {
            result.addNickName(nickname);
        }

        return result;
    }

    /**
     * Creates a new Person object.
     * 
     * @param undef
     *            some string representing a person, e.g. an email or a nickname
     * @return the person
     */
    public Person createPerson(String undef) {
        Person result = new Person();

        if (isEmail(undef)) {
            result.setEmail(undef);
        } else { // we guess that undef is a nickname
            result.addNickName(undef);
        }

        return result;
    }

    private static boolean isEmail(String input) {
        Pattern p = Pattern.compile("(\\w+)@(\\w+\\.)(\\w+)(\\.\\w+)?"); // name@subdomain.domain.suffix
        Matcher m = p.matcher(input);
        if (m.find()) {
            return true;
        }

        return false;
    }
}

/*
 private void createPerson(String author, Revision revision) {
		P e = null;
		   if(isEmail(author)){
			  
			   if(personEmails.containsKey(author)){
				   e = personEmails.get(author);
				   
			   } else {
				   Person person = new Person();
				   person.setEmail(author);
				   
				   e = new P();
				   e.person = person;
				   e.role = new CommitterRole();
				   
				   personEmails.put(author, e);
			   }
			   
			   e.role.addRevision(revision);
			   revision.setAuthor(e.person);
			   revision.setAuthorNickName(author); //MW: facilitates e.g. bug linking
		   } else{ //We guess that author is a nickname
			   
			   if(personNickNames.containsKey(author)){
				   e = personNickNames.get(author);
			   }else{
				   Person person = new Person();
				   person.addNickName(author);
				   
				   e = new P();
				   e.person = person;
				   e.role = new CommitterRole();
			   
				   personNickNames.put(author, e);
			   }
			   
			   e.role.addRevision(revision);
			   revision.setAuthor(e.person);
			   revision.setAuthorNickName(author);
		   }
	}
*/  
