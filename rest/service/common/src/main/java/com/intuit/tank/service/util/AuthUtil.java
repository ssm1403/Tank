/**
 * Copyright 2011 Intuit Inc. All Rights Reserved
 */
package com.intuit.tank.service.util;

/*
 * #%L
 * Rest Service Common Classes
 * %%
 * Copyright (C) 2011 - 2015 Intuit Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

import javax.security.enterprise.SecurityContext;
import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import com.intuit.tank.project.OwnableEntity;
import com.intuit.tank.vm.common.TankConstants;

/**
 * AuthUtil
 * 
 * @author dangleton
 * 
 */
public class AuthUtil {


    private AuthUtil() {
        // private constructor to implement util pattern
    }

    /**
     * Checks to see if the user is currently logged in to the system by checking the Identity object.
     * 
     * @param servletContext
     *            the servletContext
     * @throws WebApplicationException
     *             if the user is not authenticated.
     */
    public static void checkAdmin(ServletContext servletContext) throws WebApplicationException {
        SecurityContext securityContext = new ServletInjector<SecurityContext>().getManagedBean(servletContext, SecurityContext.class);
        if (securityContext == null || !securityContext.isCallerInRole(TankConstants.TANK_GROUP_ADMIN)) {
            throw new WebApplicationException(buildForbiddenResponse("Insuficient Rights"));
        }
    }

    /**
     * Checks to see if the user is currently logged in to the system by checking the Identity object.
     * 
     * @param servletContext
     *            the servletContext
     * @throws WebApplicationException
     *             if the user is not authenticated.
     */
    public static void checkLoggedIn(ServletContext servletContext) throws WebApplicationException {
        SecurityContext securityContext = new ServletInjector<SecurityContext>().getManagedBean(servletContext, SecurityContext.class);
        if (securityContext == null || securityContext.getCallerPrincipal() == null) {
            throw new WebApplicationException(buildForbiddenResponse("Login Required"));
        }
    }

    /**
     * Checks to see if the user is currently logged in to the system by checking the Identity object.
     * 
     * @param servletContext
     *            the servletContext
     * @param ownable
     *            the entiry to check if the user has rights to
     * @throws WebApplicationException
     *             if the user is not authenticated.
     */
    public static void checkOwner(ServletContext servletContext, OwnableEntity ownable) throws WebApplicationException {
        SecurityContext securityContext = new ServletInjector<SecurityContext>().getManagedBean(servletContext, SecurityContext.class);
        
        if (securityContext.isCallerInRole(TankConstants.TANK_GROUP_ADMIN)) {
            return;
        }
        if (StringUtils.isEmpty(ownable.getCreator()) || securityContext.getCallerPrincipal().getName().equals(ownable.getCreator())) {
            return;
        }
        throw new WebApplicationException(buildForbiddenResponse("Insufficient Rights"));
    }

    private static final Response buildForbiddenResponse(String mes) {
        return Response.status(Status.FORBIDDEN).entity(mes).build();
    }

}
