/**
 * Copyright 2011 Intuit Inc. All Rights Reserved
 */
package com.intuit.tank.api.model.v1.filter;

/*
 * #%L
 * Filter Rest Api
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
@XmlRootElement(name = "FilterContainer", namespace = Namespace.NAMESPACE_V1)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FilterContainer", namespace = Namespace.NAMESPACE_V1, propOrder = {
        "filters"
})
public class FilterContainer implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElementWrapper(name = "filters", namespace = Namespace.NAMESPACE_V1)
    @XmlElement(name = "filters", namespace = Namespace.NAMESPACE_V1, required = false, nillable = false)
    private List<FilterTO> filters = new ArrayList<FilterTO>();

    /**
     * @param filterGroups
     */
    public FilterContainer(List<FilterTO> filters) {
        super();
        this.filters = filters;
    }

    /**
     * 
     */
    public FilterContainer() {
        super();
    }

    /**
     * @return the filterGroups
     */
    public List<FilterTO> getFilters() {
        return filters;
    }

}
