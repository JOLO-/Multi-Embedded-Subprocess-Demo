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
        startCandidateSubprocess(CANDIDATE_PROCESS_AMOUNT);

        checkSubprocessLaunch();
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
    private static void startCandidateSubprocess(int subprocessAmount) {
        for (int i = 0; i < subprocessAmount; i++)
            runtimeService.startProcessInstanceByKey("candidateProcess");
    }

    private static void checkSubprocessLaunch() {
        int startedProcAmount = runtimeService.createProcessInstanceQuery().list().size();
        if (startedProcAmount != CANDIDATE_PROCESS_AMOUNT + VACANCY_PROCESS_AMOUNT)
            throw new IllegalStateException("there are " + startedProcAmount + " tasks, but should be " + (CANDIDATE_PROCESS_AMOUNT + VACANCY_PROCESS_AMOUNT));
    }
}
