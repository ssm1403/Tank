package com.intuit.tank.service.api.v1;

/*
 * #%L
 * Datafile Rest API
 * %%
 * Copyright (C) 2011 - 2015 Intuit Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

import javax.annotation.Nonnull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;

/**
 * Copyright 2011 Intuit Inc. All Rights Reserved
 */

/**
 * ProjectService
 * 
 * @author dangleton
 * 
 */
@Path(DataFileService.SERVICE_RELATIVE_PATH)
public interface DataFileService {
    public static final String SERVICE_RELATIVE_PATH = "/v1/datafile-service";

    public static final String METHOD_PING = "/ping";

    public static final String METHOD_SAVE_OR_UPDATE = "/datafile";
    public static final String METHOD_GET_DATA_FILES = "/datafile";
    public static final String METHOD_DELETE = "/datafile/{id}";
    public static final String METHOD_GET_DATA_FILE = "/datafile/{id}";
    public static final String METHOD_GET_DATA_FILE_VERSION = "/datafile/{id}/version/{version}";
    public static final String METHOD_GET_DATA_FILE_OFFSET = "/datafile/{id}/data/offset{offset}";
    public static final String METHOD_GET_DATA_FILE_DATA = "/datafile/{id}/data";
    public static final String METHOD_GET_DATA_FILE_DATA_DOWNLOAD = "/datafile/{id}/data/download";

    /**
     * Test method to test if the service is up.
     * 
     * @return non-null String value.
     */
    @Path(METHOD_PING)
    @GET
    @Nonnull
    public String ping();

    @Path(METHOD_SAVE_OR_UPDATE)
    @Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @POST
    @Nonnull
    public Response saveOrUpdateDataFile(FormDataMultiPart formData);

    @Path(METHOD_DELETE)
    @DELETE
    @Nonnull
    public Response deleteDataFile(@PathParam("id") Integer id);

    @Path(METHOD_GET_DATA_FILES)
    @Produces({ MediaType.APPLICATION_XML })
    @GET
    @Nonnull
    public Response getDataFiles();

    @Path(METHOD_GET_DATA_FILE)
    @Produces({ MediaType.APPLICATION_XML })
    @GET
    @Nonnull
    public Response getDataFile(@PathParam("id") Integer id);

    @Path(METHOD_GET_DATA_FILE)
    @PUT
    @Nonnull
    public Response setDataFile(@PathParam("id") Integer id, FormDataMultiPart formData);

    @Path(METHOD_GET_DATA_FILE_VERSION)
    @Produces({ MediaType.TEXT_PLAIN })
    @GET
    public StreamingOutput getDataFileVersion(@PathParam("id") Integer id, @PathParam("version") Integer version,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("-1") @QueryParam("num-lines") int numLines);

    @Path(METHOD_GET_DATA_FILE_OFFSET)
    @Produces({ MediaType.TEXT_PLAIN })
    @GET
    public StreamingOutput getDataFileDataOffset(@PathParam("id") Integer id,
            @DefaultValue("0") @PathParam("offset") int offset,
            @DefaultValue("-1") @QueryParam("num-lines") int numLines);

    @Path(METHOD_GET_DATA_FILE_DATA)
    @Produces({ MediaType.TEXT_PLAIN })
    @GET
    public StreamingOutput getDataFileData(@PathParam("id") Integer id);

    @Path(METHOD_GET_DATA_FILE_DATA_DOWNLOAD)
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    @GET
    public Response downloadDataFileData(@PathParam("id") Integer id);

}
