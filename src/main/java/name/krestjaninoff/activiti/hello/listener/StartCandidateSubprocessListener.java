package org.bpmnwithactiviti.chapter6.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

public class StartCandidateSubprocessListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        String name = (execution.getCurrentActivityName() == null) ? "StartCandidateSubprocess" : execution.getCurrentActivityName();
        System.out.println(execution.getProcessDefinitionId() + ":" + execution.getProcessInstanceId() + "\t" + name);;
    }
}