package com.ibm.openpages.scheduler;


import com.ibm.openpages.api.scheduler.AbstractScheduledProcess;
import com.ibm.openpages.api.trigger.ext.DefaultEventHandler;

public class SchedulerDM extends AbstractScheduledProcess {


    public SchedulerDM(int processType) {
        super(processType);
    }

    @Override
    public void execute() throws Exception {

        System.out.println("************************ Test ************************");

    }
}
