package com.chegg.temporaldemo;

import com.chegg.temporaldemo.activity.DemoActivities;
import com.chegg.temporaldemo.workflow.AnsweringWorkflow;
import com.chegg.temporaldemo.workflow.AnsweringWorkflowImpl;
import com.google.common.base.Stopwatch;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.*;

@SpringBootApplication
public class TemporaldemoApplication {

	private static final WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
	private static final WorkflowClient client = WorkflowClient.newInstance(service);
	private static final WorkerFactory factory = WorkerFactory.newInstance(client);

	public static void main(String[] args) {

		SpringApplication.run(TemporaldemoApplication.class, args);
	}

	@Autowired
	DemoActivities demoActivities;

	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) throws InterruptedException {
		Worker worker = factory.newWorker(Constants.ANSWERING_TASK_QUEUE);
		worker.registerWorkflowImplementationTypes(AnsweringWorkflowImpl.class);
		worker.registerActivitiesImplementations(demoActivities);

		factory.start();

		// Workflow options
		WorkflowOptions workflowOptions = WorkflowOptions.newBuilder()
				//.setWorkflowId("AnsweringWorkflowID")
				.setTaskQueue(Constants.ANSWERING_TASK_QUEUE)
				.build();

		Thread.sleep(5000);

		// run the workflow

		ExecutorService executorService =
				Executors.newFixedThreadPool(100);

		List<Callable<String>> callableTasks = new ArrayList<>();
		Callable<String> callableTask;
		for(int i =0; i<100; i++) {

			callableTask = () -> {
				AnsweringWorkflow answeringWorkflow = client.newWorkflowStub(AnsweringWorkflow.class,
						workflowOptions);

				//System.out.println("Workflow result is***" + answeringWorkflow.getAnswered("test-"+i));
				return answeringWorkflow.getAnswered("test-"+ Math.random());
			};
			callableTasks.add(callableTask);

		}
		StopWatch stopwatch = new StopWatch();
		stopwatch.start();

		List<Future<String>> futures = executorService.invokeAll(callableTasks);

		stopwatch.stop();
		executorService.shutdown();
		System.out.println("Total time taken***"+ stopwatch.getTotalTimeMillis());

	}



}
