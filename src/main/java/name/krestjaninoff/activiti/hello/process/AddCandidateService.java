package name.krestjaninoff.activiti.hello.process;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

@Service
public class AddCandidateService implements JavaDelegate {
	
	public void execute(DelegateExecution execution) throws Exception {
        System.out.println(execution.getProcessDefinitionId() + ":" + execution.getProcessInstanceId() + "\t" + execution.getCurrentActivityName());

        for(int i = 0; i < Integer.MAX_VALUE; i++) {
            System.out.println(execution.getProcessDefinitionId() + ":" + execution.getProcessInstanceId() + "\t" + execution.getCurrentActivityName() + "\t " + i);
            Thread.yield();
            if (i % 10 == 0)
                Thread.currentThread().sleep(1000);
        }
	}
}
