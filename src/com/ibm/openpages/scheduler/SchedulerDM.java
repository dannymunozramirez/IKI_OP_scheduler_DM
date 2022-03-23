package com.ibm.openpages.scheduler;


import com.ibm.openpages.api.scheduler.AbstractScheduledProcess;

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
            System.out.println("************************ Test ************************");

        } catch (Exception ex) {
            System.out.println("Exception occur: " + ex);
        }

    }
}
