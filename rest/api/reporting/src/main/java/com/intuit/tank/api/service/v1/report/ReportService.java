package com.intuit.tank.api.service.v1.report;

/*
 * #%L
 * Reporting Rest API
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
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.intuit.tank.reporting.api.TPSReportingPackage;
import com.intuit.tank.results.TankResultPackage;
import com.intuit.tank.vm.common.util.ReportUtil;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

/**
 * Copyright 2011 Intuit Inc. All Rights Reserved
 */

/**
 * ProjectService
 * 
 * @author dangleton
 * 
 */
@Path(ReportService.SERVICE_RELATIVE_PATH)
public interface ReportService {

    public static final String SERVICE_RELATIVE_PATH = "/v1/report-service";

    public static final String METHOD_PING = "/ping";
    public static final String METHOD_TIMING_CSV = "/timing/csv";
    public static final String METHOD_TIMING_SUMMARY_CSV = "/timing/summary/csv";
    public static final String METHOD_TIMING_PERIODIC_CSV = "/timing/periodic/csv";
    public static final String METHOD_TIMING_SUMMARY_HTML = "/timing/summary/html";
    public static final String METHOD_TIMING_PERIODIC_HTML = "/timing/periodic/html";
    public static final String METHOD_PROCESS_TIMING = "/processs/timing";
    public static final String METHOD_PROCESS_TIMING_LEGACY = "/process/timing/legacy";
    public static final String METHOD_TIMING = "/timing";
    public static final String METHOD_TPS_INFO = "/report/tps-info";
    public static final String METHOD_TIMING_RESULTS = "/report/timing-results";

    public static final String DATE_FORMAT = ReportUtil.DATE_FORMAT;

    /**
     * Test method to test if the service is up.
     * 
     * @return non-null String value.
     */
    @Path(ReportService.METHOD_PING)
    @Produces({ MediaType.TEXT_PLAIN })
    @GET
    @Nonnull
    public String ping();
    
    /**
     * 
     * @param reportingPackage
     */
    @Path(ReportService.METHOD_TPS_INFO)
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response setTPSInfos(@Nonnull TPSReportingPackage reportingPackage);
    
    /**
     * 
     * @param reportingPackage
     */
    @Path(ReportService.METHOD_TIMING_RESULTS)
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response sendTimingResults(@Nonnull TankResultPackage results);

    /**
     * Gets all MetricDescriptors and returns them in a list.
     * 
     * @return a Response of type MetricList
     */
    @GET
    @Path("/{file}")
    @Produces({ MediaType.TEXT_PLAIN })
    public Response getFile(@PathParam("file") String filePath, @QueryParam("from") String start);

    /**
     * Test method to test if the service is up.
     * 
     * @return non-null String value.
     */
    @Path(ReportService.METHOD_PROCESS_TIMING + "/{jobId}")
    @Produces({ MediaType.TEXT_PLAIN })
    @GET
    @Nonnull
    @TypeHint(String.class)
    public Response processSummary(@PathParam("jobId") String jobId);

   

    /**
     * Retrieves the .
     * 
     * @return non-null String value.
     */
    @Path(ReportService.METHOD_TIMING_CSV + "/{jobId}")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    @GET
    @Nonnull
    @TypeHint(String.class)
    public Response getTimingCsv(@PathParam("jobId") String jobId);

    /**
     * Retrieves the .
     * 
     * @return non-null String value.
     */
    @Path(ReportService.METHOD_TIMING_SUMMARY_CSV + "/{jobId}")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    @GET
    @Nonnull
    @TypeHint(String.class)
    public Response getSummaryTimingCsv(@PathParam("jobId") String jobId);

    /**
     * Retrieves the .
     * 
     * @return non-null String value.
     */
    @Path(ReportService.METHOD_TIMING_PERIODIC_CSV + "/{jobId}")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    @GET
    @Nonnull
    @TypeHint(String.class)
    public Response getTimingBucketCsv(@PathParam("jobId") String jobId,
            @QueryParam("period") @DefaultValue("15") int period, @QueryParam("minTime") String minDate,
            @QueryParam("maxTime") String maxDate);

    /**
     * Retrieves the .
     * 
     * @return non-null String value.
     */
    @Path(ReportService.METHOD_TIMING_SUMMARY_HTML + "/{jobId}")
    @Produces({ MediaType.TEXT_HTML })
    @GET
    @Nonnull
    @TypeHint(String.class)
    public Response getSummaryTimingHtml(@PathParam("jobId") String jobId);

    /**
     * Retrieves the .
     * 
     * @return non-null String value.
     */
    @Path(ReportService.METHOD_TIMING_PERIODIC_HTML + "/{jobId}")
    @Produces({ MediaType.TEXT_HTML })
    @GET
    @Nonnull
    @TypeHint(String.class)
    public Response getTimingBucketHtml(@PathParam("jobId") String jobId,
            @QueryParam("period") @DefaultValue("15") int period);

    /**
     * Deletes The specified timing data.
     * 
     * @param jobId
     *            the job id of the timing data to delete.
     * @return Response containing a status code 204 (no content) if successful and 400 (bad request) if id cannot be
     *         found.
     */
    @Path(ReportService.METHOD_TIMING + "/{jobId}")
    @Produces({ MediaType.TEXT_PLAIN })
    @DELETE
    @Nonnull
    @TypeHint(TypeHint.NO_CONTENT.class)
    public Response deleteTiming(@PathParam("jobId") String jobId);

}
