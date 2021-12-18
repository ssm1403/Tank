/**
 * Copyright 2011 Intuit Inc. All Rights Reserved
 */
package com.intuit.tank.api.model.v1.user;

/*
 * #%L
 * User Rest API
 * %%
 * Copyright (C) 2011 - 2015 Intuit Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * ProjectContainer jaxb container for ProjectTo
 * 
 * @author dangleton
 * 
 */
@XmlRootElement(name = "UserContainer", namespace = Namespace.NAMESPACE_V1)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserContainer", namespace = Namespace.NAMESPACE_V1, propOrder = {
        "users"
})
public class UserContainer implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElementWrapper(name = "users", namespace = Namespace.NAMESPACE_V1)
    @XmlElement(name = "project", namespace = Namespace.NAMESPACE_V1, required = false, nillable = false)
    private List<User> users = new ArrayList<User>();

    public UserContainer() {

    }

    /**
     * @param projects
     */
    public UserContainer(List<User> users) {
        this.users = users;
    }

    /**
     * @return the scripts
     */
    public List<User> getUsers() {
        return users;
    }

}
