package name.krestjaninoff.activiti.hello;


import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    private final static int VACANCY_PROCESS_AMOUNT = 2;
    private final static int CANDIDATE_PROCESS_AMOUNT = 2;
    private final static List<ProcessInstance> processInstances = new ArrayList<ProcessInstance>();

    /**
     * Activiti services
     */
    private static ClassPathXmlApplicationContext applicationContext;
    private static RuntimeService runtimeService;
    private static TaskService taskService;
    private static IdentityService identityService;
    private static RepositoryService repositoryService;

    static {
        applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        runtimeService = (RuntimeService) applicationContext.getBean("runtimeService");
        taskService = (TaskService) applicationContext.getBean("taskService");
        identityService = (IdentityService) applicationContext.getBean("identityService");
        repositoryService = (RepositoryService) applicationContext.getBean("repositoryService");
    }

	public static void main(String[] args) {
        //launch two vacancy processes
        launchVancySubprocesses();
        ckeckMainProcessLaunch();


        //start two subprocesses for each vacancy subprocess
        startCandidateSubprocess(processInstances.get(0).getId(), CANDIDATE_PROCESS_AMOUNT);
        startCandidateSubprocess(processInstances.get(1).getId(), CANDIDATE_PROCESS_AMOUNT);

        checkSubprocessLaunch(processInstances.get(0).getProcessInstanceId());
        checkSubprocessLaunch(processInstances.get(1).getProcessInstanceId());
    }

    /**
     * Launch VACANCY_PROCESS_AMOUNT vacancy subprocesses, and save their instances.
     */
    private static void launchVancySubprocesses() {
        for (int i = 0; i < VACANCY_PROCESS_AMOUNT; i++) {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("hiringProcess");
            processInstances.add(processInstance);
        }
    }

    /**
     * Check if vacancy project has been launched in the right way.
     * @throws IllegalStateException
     */
    private static void ckeckMainProcessLaunch() throws IllegalStateException {
        for (ProcessInstance processInstance: processInstances) {
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).list();

            if (tasks.size() != 1)
                throw new IllegalStateException();
            Task currentTask = tasks.get(0);
            if (!currentTask.getName().equals("Find employees"))
                throw new IllegalStateException();
        }
    }

    /**
     * Start the subprocessAmount subprocesses for vacancy process with id vacancyProcessId
     * @param vacancyProcessId id of the process from which we will start subprocesses
     * @param subprocessAmount amount of subprocesses
     */
    private static void startCandidateSubprocess(String vacancyProcessId, int subprocessAmount) {
        //find the needed execution, to which we address the signal
        List<Execution> executions = runtimeService.createExecutionQuery().signalEventSubscriptionName("launchCandidateSubprocess").list();
        Execution execution = null;
        for (int i = 0; i < executions.size(); i++)
            if (executions.get(i).getProcessInstanceId().equals(vacancyProcessId))
                execution = executions.get(i);

        //launch the CANDIDATE_PROCESS_AMOUNT candidates subprocesses fir first
        for (int i = 0; i < subprocessAmount; i++) {
            runtimeService.signalEventReceived("launchCandidateSubprocess", execution.getId());
        }
    }

    private static void checkSubprocessLaunch(String vacancyProcessId) {
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(vacancyProcessId).list();
        if (tasks.size() != CANDIDATE_PROCESS_AMOUNT + 1)
            throw new IllegalStateException("there are " + tasks.size() + " tasks, but should be " + (CANDIDATE_PROCESS_AMOUNT + 1));
    }
}
