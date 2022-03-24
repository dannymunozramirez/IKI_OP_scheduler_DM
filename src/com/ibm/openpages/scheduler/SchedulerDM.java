package com.ibm.openpages.scheduler;


import com.ibm.openpages.api.Context;
import com.ibm.openpages.api.metadata.Id;
import com.ibm.openpages.api.query.IQuery;
import com.ibm.openpages.api.query.IResultSetRow;
import com.ibm.openpages.api.query.ITabularResultSet;
import com.ibm.openpages.api.resource.GRCObjectFilter;
import com.ibm.openpages.api.resource.IField;
import com.ibm.openpages.api.resource.IGRCObject;
import com.ibm.openpages.api.resource.IIdField;
import com.ibm.openpages.api.scheduler.AbstractScheduledProcess;
import com.ibm.openpages.api.service.IQueryService;
import com.ibm.openpages.api.service.IResourceService;
import com.ibm.openpages.api.service.IServiceFactory;
import com.ibm.openpages.api.service.IWorkflowService;
import com.ibm.openpages.api.service.local.configuration.CurrentReportingPeriodImpl;
import com.ibm.openpages.api.workflow.*;

import java.util.List;

import static com.ibm.openpages.api.service.ServiceFactory.*;
import static org.bouncycastle145.asn1.x509.X509ObjectIdentifiers.id;

public class SchedulerDM extends AbstractScheduledProcess {

    public SchedulerDM() {
        super(4);
    }

    public SchedulerDM(int processType) {
        super(processType);
    }

    @Override
    public void execute() throws Exception {

        try {

            // Get all KRI Values ID with fastMapUpload = yes and Status = "Collected"
            String query = "SELECT [KeyRiskIndicatorValue].[Resource ID] FROM [KeyRiskIndicatorValue] " + "WHERE [KeyRiskIndicatorValue].[IKI-KRIValues:FastMapUploaded] = 'Yes' " + "AND [KeyRiskIndicatorValue].[OPSS-KRI-Shared:Collection Status] = 'Collected'";

            IServiceFactory iServiceFactory = getServiceFactory(getApplicationContext());
            IResourceService resourceService = iServiceFactory.createResourceService();

            IQueryService queryService = iServiceFactory.createQueryService();
            IQuery iQuery = queryService.buildQuery(query);
            ITabularResultSet iResultSetRows = iQuery.fetchRows(0);

            for (IResultSetRow row : iResultSetRows) {

                IField field = row.getField(0);
                IIdField id = (IIdField) field;
                Id value = id.getValue();

                IWorkflowService wf = iServiceFactory.createWorkflowService();

                IGRCObject object = resourceService.getGRCObject(value);

                GRCObjectFilter filter = new GRCObjectFilter(new CurrentReportingPeriodImpl());
                filter.setIncludeActiveWorkflows(true);

                object = resourceService.getGRCObject(value, filter);

                List<Id> workflowProcess = object.getActiveWorkflowIds();

                System.out.println("workflowProcess != null: " + (workflowProcess != null));

                if (workflowProcess != null) {
                    for (Id processId : workflowProcess) {
                        List<IWFActivityInstance> instances = wf.getActivityInstances(processId);
                        IWFProcess wfProcess = wf.getProcess(processId);
                        List<IWFProcess> activeProcesses = wf.getStartedProcesses(value);

                        if (activeProcesses != null) {
                            for (IWFProcess iwfProcess : activeProcesses) {
                                IWFProcess activeProcess = iwfProcess;
                                System.out.println("Active Processes: " + activeProcess.toString());
                                IWFActivityInstance currentActivityInstance = activeProcess.getCurrentActivityInstance();
                                System.out.println("Current Activity Instance: " + currentActivityInstance);

                                if (currentActivityInstance != null) {

                                    IWFProcessDefinition proDef = wf.getProcessDefinitionByVersionId(activeProcess.getProcessVersionId());
                                    IWFActivity activity = proDef.getActivity(currentActivityInstance.getActivityId());

                                    System.out.println("***********************************************");
                                    System.out.println("Stage Name: " + activity.getName());

                                }

                            }
                        }
                    }
                }

            }
//
//            IGRCObject grcObject = resourceService.getGRCObject(new Id("12501"));

//
//            System.out.println(grcObject.getName());

        } catch (Exception ex) {
            System.out.println("Exception occur: " + ex);
        }

    }
}
