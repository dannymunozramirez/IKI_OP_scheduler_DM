package com.ibm.openpages.scheduler;

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
import com.ibm.openpages.api.workflow.IWFActivity;
import com.ibm.openpages.api.workflow.IWFActivityInstance;
import com.ibm.openpages.api.workflow.IWFProcess;
import com.ibm.openpages.api.workflow.IWFProcessDefinition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ibm.openpages.api.service.ServiceFactory.getServiceFactory;

/**
 * This Class will run when the Scheduler job is executed from UI. It will get all KRI Values with active WorkFlow
 * instances and fields Collection Status = "Collected" AND FastMapUploaded = "Yes", in order to finalize this
 * Workflow instance automatically
 */
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
            IGRCObject object;

            // Initializing Query Services
            IServiceFactory iServiceFactory = getServiceFactory(getApplicationContext());
            IResourceService resourceService = iServiceFactory.createResourceService();

            // Initializing Query Services
            IQueryService queryService = iServiceFactory.createQueryService();

            /*
                Query to get all KRI Values ID with fields
                fastMapUpload = 'yes' and Collection Status = 'Collected'
                present in the system
             */
            IQuery iQuery = queryService.buildQuery(queryBuilder());
            ITabularResultSet iResultSetRows = iQuery.fetchRows(0);

            // Iterating rows
            for (IResultSetRow row : iResultSetRows) {

                // Getting first column element Object Id
                IField field = row.getField(0);
                IIdField id = (IIdField) field;
                Id value = id.getValue();

                // Creating WF Services
                IWorkflowService wf = iServiceFactory.createWorkflowService();

                // Setting as 'true' the active workflow visibility
                GRCObjectFilter filter = new GRCObjectFilter(new CurrentReportingPeriodImpl());
                filter.setIncludeActiveWorkflows(true);
                object = resourceService.getGRCObject(value, filter);

                // Active workflows
                List<Id> workflowProcess = object.getActiveWorkflowIds();

                if (workflowProcess != null) {
                    for (Id processId : workflowProcess) {

                        List<IWFActivityInstance> instances = wf.getActivityInstances(processId);
                        IWFProcess wfProcess = wf.getProcess(processId);
                        List<IWFProcess> activeProcesses = wf.getStartedProcesses(value);

                        if (activeProcesses != null) {
                            for (IWFProcess iwfProcess : activeProcesses) {

                                // Current activity instance
                                IWFActivityInstance currentActivityInstance = iwfProcess.getCurrentActivityInstance();

                                if (currentActivityInstance != null) {

                                    // Passing processId from workFlowProcess List to be Set as a parameter to
                                    // terminate the process
                                    Set<Id> idSetWf = new HashSet<>();
                                    idSetWf.add(processId);

                                    // Terminating workflow process
                                    wf.terminateProcesses(idSetWf);

                                }
                            }
                        }
                    }
                }

            }
        } catch (Exception ex) {
            System.out.println("Exception occur: " + ex);
        }

    }

    public String queryBuilder() {

        String query = "SELECT [KeyRiskIndicatorValue].[Resource ID] FROM [KeyRiskIndicatorValue] \" " + "+ \"WHERE [KeyRiskIndicatorValue].[IKI-KRIValues:FastMapUploaded] = 'Yes' \" " + "+ \"AND [KeyRiskIndicatorValue].[OPSS-KRI-Shared:Collection Status] = 'Collected'";

        return query;
    }
}
