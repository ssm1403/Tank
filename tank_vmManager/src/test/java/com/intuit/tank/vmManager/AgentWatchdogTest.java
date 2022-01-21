package com.intuit.tank.vmManager;

import com.intuit.tank.api.cloud.VMTracker;
import com.intuit.tank.api.model.v1.cloud.CloudVmStatus;
import com.intuit.tank.api.model.v1.cloud.CloudVmStatusContainer;
import com.intuit.tank.api.model.v1.cloud.VMStatus;
import com.intuit.tank.api.model.v1.cloud.ValidationStatus;
import com.intuit.tank.dao.VMImageDao;
import com.intuit.tank.project.VMInstance;
import com.intuit.tank.vm.api.enumerated.JobStatus;
import com.intuit.tank.vm.api.enumerated.VMImageType;
import com.intuit.tank.vm.api.enumerated.VMRegion;
import com.intuit.tank.vm.vmManager.VMInformation;
import com.intuit.tank.vm.vmManager.VMInstanceRequest;
import com.intuit.tank.vmManager.environment.amazon.AmazonInstance;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgentWatchdogTest {

    @Mock
    private AmazonInstance amazonInstanceMock = new AmazonInstance(VMRegion.STANDALONE);

    @Test
    @Disabled
    public void toStringTest() {
        VMTracker vmTracker = new VMTrackerImpl();
        VMInstanceRequest instanceRequest = new VMInstanceRequest();
        instanceRequest.setRegion(VMRegion.STANDALONE);
        List<VMInformation> vmInfo = new ArrayList<>();
        AgentWatchdog agentWatchdog = new AgentWatchdog(instanceRequest, vmInfo, vmTracker);

        String result = agentWatchdog.toString();

        assertNotNull(result);
        assertTrue(result.endsWith("[sleepTime=30000,maxWaitForStart=120000,maxWaitForReporting=360000,maxRelaunch=2]"));
    }

    @Test
    public void alreadyRunningRunTest(@Mock VMTracker vmTrackerMock, @Mock CloudVmStatusContainer cloudVmStatusContainerMock) {
        when(cloudVmStatusContainerMock.getEndTime()).thenReturn(null);
        CloudVmStatus vmstatus = new CloudVmStatus("i-123456789", "123", "sg-123456", JobStatus.Running, VMImageType.AGENT, VMRegion.STANDALONE, VMStatus.running, new ValidationStatus(), 1, 1, new Date(), new Date());
        Set<CloudVmStatus> set = Stream.of(vmstatus).collect(Collectors.toCollection(HashSet::new));
        when(cloudVmStatusContainerMock.getStatuses()).thenReturn(set);
        when(vmTrackerMock.isRunning("123")).thenReturn(true);
        when(vmTrackerMock.getVmStatusForJob("123")).thenReturn(cloudVmStatusContainerMock);

        VMInformation vmInformation = new VMInformation();
        vmInformation.setState("RUNNING");
        vmInformation.setInstanceId("i-123456789");
        List<VMInformation> vmInfo = Collections.singletonList( vmInformation );
        when(amazonInstanceMock.describeInstances(Mockito.anyString())).thenReturn(vmInfo);
        VMInstanceRequest instanceRequest = new VMInstanceRequest();
        instanceRequest.setRegion(VMRegion.STANDALONE);
        instanceRequest.setJobId("123");

        AgentWatchdog agentWatchdog = new AgentWatchdog(instanceRequest, vmInfo, vmTrackerMock, amazonInstanceMock, null, 10, 1000);

        agentWatchdog.run();
        verify(amazonInstanceMock, times(1)).describeInstances("i-123456789");
        verify(amazonInstanceMock, never()).killInstances(Mockito.anyList());
        verify(amazonInstanceMock, never()).create(Mockito.any());
        verify(cloudVmStatusContainerMock, times(2)).getEndTime();
        verify(cloudVmStatusContainerMock, times(1)).getStatuses();
    }

    @Test
    public void progressToRunningRunTest(@Mock VMTracker vmTrackerMock, @Mock CloudVmStatusContainer cloudVmStatusContainerMock) {
        when(cloudVmStatusContainerMock.getEndTime()).thenReturn(null);
        CloudVmStatus vmstatusStarting = new CloudVmStatus("i-123456789", "123", "sg-123456", JobStatus.Starting, VMImageType.AGENT, VMRegion.STANDALONE, VMStatus.starting, new ValidationStatus(), 1, 1, new Date(), new Date());
        CloudVmStatus vmstatusPending = new CloudVmStatus("i-123456789", "123", "sg-123456", JobStatus.Starting, VMImageType.AGENT, VMRegion.STANDALONE, VMStatus.pending, new ValidationStatus(), 1, 1, new Date(), new Date());
        Set<CloudVmStatus> setStarting = Stream.of(vmstatusStarting).collect(Collectors.toCollection(HashSet::new));
        Set<CloudVmStatus> setPending = Stream.of(vmstatusPending).collect(Collectors.toCollection(HashSet::new));
        when(cloudVmStatusContainerMock.getStatuses()).thenReturn(setStarting).thenReturn(setPending);
        when(vmTrackerMock.isRunning("123")).thenReturn(true);
        when(vmTrackerMock.getVmStatusForJob("123")).thenReturn(cloudVmStatusContainerMock);

        VMInformation vmInformation = new VMInformation();
        vmInformation.setState("RUNNING");
        vmInformation.setInstanceId("i-123456789");
        List<VMInformation> vmInfo = Collections.singletonList( vmInformation );
        when(amazonInstanceMock.describeInstances(Mockito.anyString())).thenReturn(vmInfo);
        VMInstanceRequest instanceRequest = new VMInstanceRequest();
        instanceRequest.setRegion(VMRegion.STANDALONE);
        instanceRequest.setJobId("123");

        AgentWatchdog agentWatchdog = new AgentWatchdog(instanceRequest, vmInfo, vmTrackerMock, amazonInstanceMock, null, 10, 1000);

        agentWatchdog.run();
        verify(amazonInstanceMock, times(1)).describeInstances("i-123456789");
        verify(amazonInstanceMock, never()).killInstances(Mockito.anyList());
        verify(amazonInstanceMock, never()).create(Mockito.any());
        verify(cloudVmStatusContainerMock, times(3)).getEndTime();
        verify(cloudVmStatusContainerMock, times(2)).getStatuses();
    }

    @Test
    public void relaunchRunTest(@Mock VMTracker vmTrackerMock, @Mock CloudVmStatusContainer cloudVmStatusContainerMock, @Mock VMImageDao daoMock) {
        when(cloudVmStatusContainerMock.getEndTime()).thenReturn(null);
        CloudVmStatus vmstatusStarting = new CloudVmStatus("i-123456789", "123", "sg-123456", JobStatus.Starting, VMImageType.AGENT, VMRegion.STANDALONE, VMStatus.starting, new ValidationStatus(), 1, 1, new Date(), new Date());
        CloudVmStatus vmstatusPending = new CloudVmStatus("i-123456789", "123", "sg-123456", JobStatus.Starting, VMImageType.AGENT, VMRegion.STANDALONE, VMStatus.pending, new ValidationStatus(), 1, 1, new Date(), new Date());
        Set<CloudVmStatus> setStarting = Stream.of(vmstatusStarting).collect(Collectors.toCollection(HashSet::new));
        Set<CloudVmStatus> setPending = Stream.of(vmstatusPending).collect(Collectors.toCollection(HashSet::new));
        when(cloudVmStatusContainerMock.getStatuses()).thenReturn(setStarting).thenReturn(setStarting).thenReturn(setStarting).thenReturn(setStarting).thenReturn(setStarting).thenReturn(setPending);
        when(vmTrackerMock.isRunning("123")).thenReturn(true);
        when(vmTrackerMock.getVmStatusForJob("123")).thenReturn(cloudVmStatusContainerMock);

        VMInformation vmInformation = new VMInformation();
        vmInformation.setState("RUNNING");
        vmInformation.setInstanceId("i-123456789");
        List<VMInformation> vmInfo = Collections.singletonList( vmInformation );
        when(amazonInstanceMock.describeInstances(Mockito.anyString())).thenReturn(vmInfo);
        when(amazonInstanceMock.create(Mockito.any())).thenReturn(vmInfo);

        VMInstanceRequest instanceRequest = new VMInstanceRequest();
        instanceRequest.setRegion(VMRegion.STANDALONE);
        instanceRequest.setJobId("123");

        VMInstance vmInstance = new VMInstance();
        when(daoMock.getImageByInstanceId(Mockito.anyString())).thenReturn(vmInstance);

        AgentWatchdog agentWatchdog = new AgentWatchdog(instanceRequest, vmInfo, vmTrackerMock, amazonInstanceMock, daoMock, 10, 30);

        agentWatchdog.run();
        verify(amazonInstanceMock, times(2)).describeInstances("i-123456789");
        verify(amazonInstanceMock, times(1)).killInstances(Mockito.anyList());
        verify(amazonInstanceMock, times(1)).create(Mockito.any());
        verify(cloudVmStatusContainerMock, times(8)).getEndTime();
        verify(cloudVmStatusContainerMock, times(6)).getStatuses();
    }
}
