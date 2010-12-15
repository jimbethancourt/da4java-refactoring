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
package org.evolizer.model.resources.entities.humans;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.evolizer.core.hibernate.model.api.IEvolizerModelEntity;
import org.hibernate.annotations.CollectionOfElements;

/**
 * This class implements the concept of a person that is somehow related to a software development process.
 * 
 * @author wuersch
 */
@SuppressWarnings("restriction")
@Entity
public class Person implements IEvolizerModelEntity {

    private Long fId;

    private String fFirstName;
    private String fLastName;
    private Set<String> fNickNames;
    // TODO should probably be a set
    private String fEmail;
    private Set<Role> fRoles;

    /**
     * Instantiates a new person.
     */
    public Person() {
        fRoles = new HashSet<Role>();
        fNickNames = new HashSet<String>();
    }

    /**
     * Unique ID, used by Hibernate.
     * 
     * @return unique Hibernate ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return fId;
    }

    /**
     * Set unique ID of Hibernate.
     * 
     * @param id
     *            to set
     */
    @SuppressWarnings("unqualified-field-access")
    protected void setId(Long id) {
        fId = id;
    }

    /**
     * Returns the person's first name.
     * 
     * @return the first name of the person.
     */
    public String getFirstName() {
        return fFirstName;
    }

    /**
     * Sets the first name.
     * 
     * @param firstName
     *            the new first name
     */
    public void setFirstName(String firstName) {
        fFirstName = firstName;
    }

    /**
     * Returns the person's last name.
     * 
     * @return the last name of the person.
     */
    public String getLastName() {
        return fLastName;
    }

    /**
     * Sets the last name.
     * 
     * @param lastName
     *            the last name
     */
    public void setLastName(String lastName) {
        fLastName = lastName;
    }

    /**
     * A person can have several user- or nicknames, e.g., <code>wuersch</code> in Bugzilla, <code>mwuersch</code> in
     * SVN, etc.
     * 
     * @return a {@link Set} of nicknames that are known for this person.
     */
    @CollectionOfElements
    public Set<String> getNickNames() {
        return fNickNames;
    }

    /**
     * Sets the set of nicknames of this person.
     * 
     * @param nickNames
     *            the nick names
     */
    public void setNickNames(Set<String> nickNames) {
        fNickNames = nickNames;
    }

    /**
     * Adds an additional entry to the set of nicknames of this person.
     * 
     * @param nickName
     *            the nickname of this person.
     */
    public void addNickName(String nickName) {
        fNickNames.add(nickName);
    }

    /**
     * Returns the email.
     * 
     * @return the email address of this person.
     */
    public String getEmail() {
        return fEmail;
    }

    /**
     * Setter for email.
     * 
     * @param email
     *            an email address following the format <code>[name]@[domain].[suffix]</code> (unchecked).
     */
    public void setEmail(String email) {
        fEmail = email;
    }

    /**
     * A person can have different roles, for example the person can be a developer of an artifact.
     * 
     * @return the persons roles
     */
    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(name = "Person_Role")
    public Set<Role> getRoles() {
        return fRoles;
    }

    /**
     * Setter for roles.
     * 
     * @param roles
     *            the roles
     */
    public void setRoles(Set<Role> roles) {
        fRoles = roles;
    }

    /**
     * Adds an additional role to this person.
     * 
     * @param role
     *            a role of this person.
     */
    public void addRole(Role role) {
        fRoles.add(role);
    }

    /**
     * {@inheritDoc}
     */
    @Transient
    public String getLabel() {
        return fFirstName + " " + fLastName;
    }

    /**
     * {@inheritDoc}
     */
    @Transient
    public String getURI() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fEmail == null) ? 0 : fEmail.hashCode());
        result = prime * result + ((fFirstName == null) ? 0 : fFirstName.hashCode());
        result = prime * result + ((fLastName == null) ? 0 : fLastName.hashCode());
        result = prime * result + ((fNickNames == null) ? 0 : fNickNames.hashCode());
        result = prime * result + ((fRoles == null) ? 0 : fRoles.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Person other = (Person) obj;
        if (fEmail == null) {
            if (other.fEmail != null) {
                return false;
            }
        } else if (!fEmail.equals(other.fEmail)) {
            return false;
        }
        if (fFirstName == null) {
            if (other.fFirstName != null) {
                return false;
            }
        } else if (!fFirstName.equals(other.fFirstName)) {
            return false;
        }
        if (fLastName == null) {
            if (other.fLastName != null) {
                return false;
            }
        } else if (!fLastName.equals(other.fLastName)) {
            return false;
        }
        if (fNickNames == null) {
            if (other.fNickNames != null) {
                return false;
            }
        } else if (!fNickNames.equals(other.fNickNames)) {
            return false;
        }
        if (fRoles == null) {
            if (other.fRoles != null) {
                return false;
            }
        } else if (!fRoles.equals(other.fRoles)) {
            return false;
        }
        return true;
    }
}
