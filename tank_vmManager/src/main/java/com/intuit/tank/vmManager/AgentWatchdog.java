/**
 * Copyright 2011 Intuit Inc. All Rights Reserved
 */
package com.intuit.tank.vmManager;

/*
 * #%L
 * VmManager
 * %%
 * Copyright (C) 2011 - 2015 Intuit Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.amazonaws.xray.AWSXRay;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.intuit.tank.api.cloud.VMTracker;
import com.intuit.tank.api.model.v1.cloud.CloudVmStatus;
import com.intuit.tank.api.model.v1.cloud.CloudVmStatusContainer;
import com.intuit.tank.api.model.v1.cloud.VMStatus;
import com.intuit.tank.api.model.v1.cloud.ValidationStatus;
import com.intuit.tank.dao.VMImageDao;
import com.intuit.tank.project.VMInstance;
import com.intuit.tank.vm.api.enumerated.JobLifecycleEvent;
import com.intuit.tank.vm.api.enumerated.JobStatus;
import com.intuit.tank.vm.api.enumerated.VMImageType;
import com.intuit.tank.vm.event.JobEvent;
import com.intuit.tank.vm.settings.TankConfig;
import com.intuit.tank.vm.settings.VmManagerConfig;
import com.intuit.tank.vm.vmManager.VMInformation;
import com.intuit.tank.vm.vmManager.VMInstanceRequest;
import com.intuit.tank.vmManager.environment.amazon.AmazonInstance;

/**
 * AgentWatchdog
 *
 * @author dangleton
 *
 */
public class AgentWatchdog implements Runnable {

    private static final Logger LOG = LogManager.getLogger(AgentWatchdog.class);
    private static final VmManagerConfig vmManagerConfig = new TankConfig().getVmManagerConfig();
    private long sleepTime;
    private long maxWaitForStart;
    private long maxWaitForReporting;
    private int maxRelaunch;
    private VMTracker vmTracker;
    private VMInstanceRequest instanceRequest;
    private List<VMInformation> vmInfo;
    private int instanceCount;
    private boolean stopped;
    private boolean checkForStart = true;
    private long startTime;
    private int relaunchCount;
    private AmazonInstance amazonInstance;
    private VMImageDao dao;

    /**
     * @param instanceRequest
     * @param vmInfo
     * @param vmTracker
     */
    public AgentWatchdog(VMInstanceRequest instanceRequest, List<VMInformation> vmInfo, VMTracker vmTracker) {
        this(instanceRequest, vmInfo, vmTracker,
                new AmazonInstance(instanceRequest.getRegion()),
                null,
                vmManagerConfig.getWatchdogSleepTime(30 * 1000),  // 30 seconds
                vmManagerConfig.getMaxAgentReportMills(1000 * 60 * 3) // 3 minutes
        );
    }

    /**
     * Constructor
     *
     * @param instanceRequest
     * @param vmInfo
     * @param vmTracker
     * @param amazonInstance
     * @param maxWaitForReporting
     */
    public AgentWatchdog(VMInstanceRequest instanceRequest, List<VMInformation> vmInfo, VMTracker vmTracker, AmazonInstance amazonInstance, VMImageDao dao, long sleepTime, long maxWaitForReporting) {

        this.instanceRequest = instanceRequest;
        this.vmInfo = new LinkedList<>(vmInfo);
        this.vmTracker = vmTracker;
        this.startTime = System.currentTimeMillis();
        this.amazonInstance = amazonInstance;
        this.dao = dao;

        this.maxWaitForReporting = maxWaitForReporting;
        this.maxWaitForStart = vmManagerConfig.getMaxAgentStartMills(1000 * 60 * 2);     // 2 minutes
        this.maxRelaunch = vmManagerConfig.getMaxRestarts(2);
        this.sleepTime = sleepTime;
        this.instanceCount = vmInfo.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("sleepTime", sleepTime)
                .append("maxWaitForStart", maxWaitForStart)
                .append("maxWaitForReporting", maxWaitForReporting)
                .append("maxRelaunch", maxRelaunch).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        LOG.info("Starting WatchDog: " + this.toString());
        AWSXRay.getGlobalRecorder().beginNoOpSegment(); //jdbcInterceptor will throw SegmentNotFoundException,RuntimeException without this
        try {
            List<VMInformation> instances = new ArrayList<VMInformation>(vmInfo);
            while (relaunchCount <= maxRelaunch && !stopped && vmTracker.isRunning(instanceRequest.getJobId())) {
                if (checkForStart) {
                    LOG.info("Checking for " + instances.size() + " out of " + instanceCount + " instances have reached running state...");
                    removeRunningInstances(instances);
                    if (!instances.isEmpty()) {
                        if (shouldRelaunchInstances(maxWaitForStart)) {
                            relaunch(instances);
                        } else {
                            LOG.info("Waiting for " + instances.size() + " instances to reach running: "
                                    + getInstanceIdList(instances));
                        }
                        Thread.sleep(sleepTime);
                        continue;
                    } else {
                        LOG.info("All instances reached running.");
                        vmTracker.publishEvent(new JobEvent(instanceRequest.getJobId(), "All instances are running",
                                JobLifecycleEvent.AGENT_STARTED));
                        checkForStart = false;
                        startTime = System.currentTimeMillis();
                    }
                }
                // all instances are now started
                instances = new ArrayList<VMInformation>(vmInfo);
                String jobId = instanceRequest.getJobId();
                // check to see if all agents have reported back
                LOG.info("Checking for " + instances.size() + " out of " + instanceCount + " tank_agent reporting...");
                removeReportingInstances(jobId, instances);
                if (!instances.isEmpty()) {
                    if (shouldRelaunchInstances(maxWaitForReporting)) {
                        relaunch(instances);
                        checkForStart = true;
                    } else {
                        LOG.info("Waiting for " + instances.size() + " tank_agents to report back: "
                                + getInstanceIdList(instances));
                    }
                    Thread.sleep(sleepTime);
                    continue;
                } else {
                    LOG.info("All tank_agents reported back to controller. Ready to start!");
                    vmTracker.publishEvent(new JobEvent(instanceRequest.getJobId(),
                            "All Agents Reported Back and are ready to start load.", JobLifecycleEvent.AGENT_REPORTED));
                    stopped = true;
                }

            }
        } catch (Exception e) {
            LOG.error("Error in Watchdog: " + e.toString(), e);
        } finally {
            LOG.info("Exiting Watchdog " + this.toString());
            AWSXRay.endSegment();
        }
    }

    /**
     * @param instances
     * @return
     */
    private String getInstanceIdList(List<VMInformation> instances) {
        return StringUtils.join(instances, ", ");
    }

    /**
     * @param instances
     */
    private void removeReportingInstances(String jobId, List<VMInformation> instances) {
        CloudVmStatusContainer vmStatusForJob = vmTracker.getVmStatusForJob(jobId);
        if (vmStatusForJob != null && vmStatusForJob.getEndTime() == null) {
            for (CloudVmStatus status : vmStatusForJob.getStatuses()) {
                if (status.getVmStatus() == VMStatus.pending || status.getVmStatus() == VMStatus.running
                        || (status.getJobStatus() != JobStatus.Unknown && status.getJobStatus() != JobStatus.Starting)) {
                    VMInformation vmInfo = instances.stream()
                            .filter(vminfo -> Objects.equals(vminfo.getInstanceId(), status.getInstanceId()))
                            .findFirst()
                            .get();
                    instances.remove(vmInfo);
                }
            }
        } else {
            stopped = true;
            throw new RuntimeException("Job appears to have been stopped. Exiting...");
        }
    }

    /**
     * @param instances
     *
     */
    private void relaunch(List<VMInformation> instances) {
        relaunchCount++;
        if (relaunchCount <= maxRelaunch) {
            startTime = System.currentTimeMillis();
            String msg = "Have " + instances.size() + " agents that failed to start correctly. Relaunching "
                    + getInstanceIdList(instances);
            vmTracker.publishEvent(new JobEvent(instanceRequest.getJobId(), msg, JobLifecycleEvent.AGENT_REBOOTED));
            LOG.info(msg);
            // relaunch instances and remove old onesn from vmTracker
            // kill them first just to be sure
            List<String> instanceIds = instances.stream()
                    .map(VMInformation::getInstanceId).collect(Collectors.toCollection(() -> new ArrayList<>(instances.size())));
            amazonInstance.killInstances(instanceIds);
            dao = (dao == null) ? new VMImageDao() : dao;
            for (VMInformation info : instances) {
                vmInfo.remove(info);
                vmTracker.setStatus(createTerminatedVmStatus(info));
                VMInstance image = dao.getImageByInstanceId(info.getInstanceId());
                if (image != null) {
                    image.setStatus(VMStatus.terminated.name());
                    dao.saveOrUpdate(image);
                }
            }
            instanceRequest.setNumberOfInstances(instances.size());
            List<VMInformation> newVms = amazonInstance.create(instanceRequest);
            instances.clear();
            for (VMInformation newInfo : newVms) {
                vmInfo.add(newInfo);
                instances.add(newInfo);
                vmTracker.setStatus(createCloudStatus(instanceRequest, newInfo));
                LOG.info("Added instance (" + newInfo.getInstanceId() + ") to VMImage table");
                try {
                    dao.addImageFromInfo(instanceRequest.getJobId(), newInfo,
                            instanceRequest.getRegion());
                } catch (Exception e) {
                    LOG.warn("Error persisting VM instance: " + e);
                }
            }
        } else {
            stopped = true;
            String msg = "Have "
                    + instances.size()
                    + " agents that failed to start correctly and have exceeded the maximum number of relaunch. Killing job.";
            vmTracker.publishEvent(new JobEvent(instanceRequest.getJobId(), msg, JobLifecycleEvent.JOB_ABORTED));
            LOG.info(msg);
            killJob();
        }
    }

    /**
     *
     */
    private void killJob() {
        throw new RuntimeException("Killing jobs and exiting");
    }

    /**
     * @param req
     * @param info
     * @return
     */
    private CloudVmStatus createCloudStatus(VMInstanceRequest req, VMInformation info) {
        return new CloudVmStatus(info.getInstanceId(), req.getJobId(),
                req.getInstanceDescription() != null ? req.getInstanceDescription().getSecurityGroup() : "unknown",
                JobStatus.Starting,
                VMImageType.AGENT, req.getRegion(), VMStatus.pending, new ValidationStatus(), 0, 0, null, null);
    }

    /**
     * @param info
     * @return
     */
    private CloudVmStatus createTerminatedVmStatus(VMInformation info) {
        LOG.info("Terminating " + info);
        return new CloudVmStatus(info.getInstanceId(), instanceRequest.getJobId(), "unknown",
                JobStatus.Stopped, VMImageType.AGENT, instanceRequest.getRegion(),
                VMStatus.terminated, new ValidationStatus(), 0, 0, null, null);
    }

    /**
     * @return
     */
    private boolean shouldRelaunchInstances(long maxWait) {
        return startTime + maxWait < System.currentTimeMillis();
    }

    /**
     * @param instances list of instance to check if reached running state
     */
    private void removeRunningInstances(List<VMInformation> instances) {
        CloudVmStatusContainer vmStatusForJob = vmTracker.getVmStatusForJob(instanceRequest.getJobId());
        if (vmStatusForJob == null || vmStatusForJob.getEndTime() != null) {
            stopped = true;
            throw new RuntimeException("Job appears to have been stopped. Exiting...");
        }
        List<VMInformation> foundInstances = amazonInstance.describeInstances(instances.stream().map(VMInformation::getInstanceId).toArray(String[]::new));
        for (VMInformation info : foundInstances) {
            if ("running".equalsIgnoreCase(info.getState())) {
                instances.remove(info);
            }
        }

    }
}