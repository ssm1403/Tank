/**
 * Copyright 2011 Intuit Inc. All Rights Reserved
 */
package com.intuit.tank.api.model.v1.agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * 
 * @author denisa
 *
 */
@XmlRootElement(name = "TankHttpClientDefinitionContainer", namespace = Namespace.NAMESPACE_V1)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TankHttpClientDefinitionContainer", namespace = Namespace.NAMESPACE_V1)
public class TankHttpClientDefinitionContainer implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElementWrapper(name = "definitions", namespace = Namespace.NAMESPACE_V1)
    @XmlElement(name = "tankHttpClientDefinition", namespace = Namespace.NAMESPACE_V1, required = false, nillable = false)
    private List<TankHttpClientDefinition> definitions = new ArrayList<TankHttpClientDefinition>();

    @XmlElement(name = "defaultDefinition", namespace = Namespace.NAMESPACE_V1, required = true, nillable = false)
    private String defaultDefinition;

    /**
     * @param definitions
     * @param defaultDefinition
     */
    public TankHttpClientDefinitionContainer(@Nonnull List<TankHttpClientDefinition> definitions, String defaultDefinition) {
        super();
        this.definitions = definitions;
        this.defaultDefinition = defaultDefinition;
    }
    /**
     * @FrameworkUseOnly
     */
    protected TankHttpClientDefinitionContainer() {
    }

    /**
     * @return the definitions
     */
    @Nonnull
    public List<TankHttpClientDefinition> getDefinitions() {
        return definitions;
    }

    /**
     * @return the defaultDefinition
     */
    public String getDefaultDefinition() {
        return defaultDefinition;
    }
    
    

}
