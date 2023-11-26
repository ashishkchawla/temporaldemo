package com.chegg.temporaldemo.cloud;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.SimpleSslContextBuilder;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import javax.net.ssl.SSLException;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class HelloActivity {

  static final String TASK_QUEUE = "HelloActivityTaskQueue";

  static final String WORKFLOW_ID = "HelloActivityWorkflow";

  static final String CLIENT_CERT =
      "-----BEGIN CERTIFICATE-----\n"
          + "MIIBwTCCAUegAwIBAgIRAcjjYfhmofkF1wJTgqHvjzkwCgYIKoZIzj0EAwMwEDEO\n"
          + "MAwGA1UEChMFQ2hlZ2cwHhcNMjMxMTIwMTEyMDQwWhcNMjQxMTE5MTEyMTQwWjAQ\n"
          + "MQ4wDAYDVQQKEwVDaGVnZzB2MBAGByqGSM49AgEGBSuBBAAiA2IABFpubjpbHswV\n"
          + "nl99DI6iVQy00b3f077TIZH0vz4iPGh0Awx0C9IzCHdDJrT2QvOjSNUK8BzdxwwM\n"
          + "mEVW/n26LG0fDC7qIjWW26bGgCWZyzeiSzGu1CW456+jyMPBPA+nxKNlMGMwDgYD\n"
          + "VR0PAQH/BAQDAgGGMA8GA1UdEwEB/wQFMAMBAf8wHQYDVR0OBBYEFA2xSZyXX15j\n"
          + "xWdGvGo2jDnN+69nMCEGA1UdEQQaMBiCFmNsaWVudC5yb290LkNoZWdnLndDeGww\n"
          + "CgYIKoZIzj0EAwMDaAAwZQIwcH43xbUERCcI6k6LycljggnLAl/N3fQO6H8kNlcU\n"
          + "Z0PlF7ek7m72dSEU9eG9XZ9pAjEAtGzjCcu2ZflGQfs6nCqgBVCIqF6mpzz0xOo8\n"
          + "xso61xul961Xaux9kvZrizeA9Fs6\n"
          + "-----END CERTIFICATE-----";

  static final String CLIENT_KEY =
      "-----BEGIN PRIVATE KEY-----\n"
          + "MIG2AgEAMBAGByqGSM49AgEGBSuBBAAiBIGeMIGbAgEBBDDNpI4WJAoPZr+E+/2o\n"
          + "V9mqqjem2docH82SrLCZy+yzeqdjLm2rG/XAEGvAvU6UV9+hZANiAARabm46Wx7M\n"
          + "FZ5ffQyOolUMtNG939O+0yGR9L8+IjxodAMMdAvSMwh3Qya09kLzo0jVCvAc3ccM\n"
          + "DJhFVv59uixtHwwu6iI1ltumxoAlmcs3oksxrtQluOevo8jDwTwPp8Q=\n"
          + "-----END PRIVATE KEY-----";

  static final String HOST_URL = "chegg-content-poc.345f5.tmprl.cloud:7233";

  static final String NAMESPACE = "chegg-content-poc.345f5";

  @WorkflowInterface
  public interface GreetingWorkflow {

    @WorkflowMethod
    String getGreeting(String name);
  }

  @ActivityInterface
  public interface GreetingActivities {

    @ActivityMethod(name = "greet")
    String composeGreeting(String greeting, String name);
  }

  public static class GreetingWorkflowImpl implements GreetingWorkflow {

    private final GreetingActivities activities =
        Workflow.newActivityStub(
            GreetingActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    @Override
    public String getGreeting(String name) {
      // This is a blocking call that returns only after the activity has completed.
      return activities.composeGreeting("Hello", name);
    }
  }

  static class GreetingActivitiesImpl implements GreetingActivities {
    private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);

    @Override
    public String composeGreeting(String greeting, String name) {
      log.info("Composing greeting...");
      return greeting + " " + name + "!";
    }
  }

  public static void main(String[] args) {

    InputStream clientCert = new ByteArrayInputStream(CLIENT_CERT.getBytes(StandardCharsets.UTF_8));
    InputStream clientKey = new ByteArrayInputStream(CLIENT_KEY.getBytes(StandardCharsets.UTF_8));

    WorkflowServiceStubs service = null;
    try {
      service =
          WorkflowServiceStubs.newServiceStubs(
              WorkflowServiceStubsOptions.newBuilder()
                  .setTarget(HOST_URL)
                  .setSslContext(SimpleSslContextBuilder.forPKCS8(clientCert, clientKey).build())
                  .build());
    } catch (SSLException e) {
      throw new RuntimeException(e);
    }

    WorkflowClient client =
        WorkflowClient.newInstance(
            service,
            WorkflowClientOptions.newBuilder().setNamespace(NAMESPACE).build());

    WorkerFactory factory = WorkerFactory.newInstance(client);

    Worker worker = factory.newWorker(TASK_QUEUE);

    worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);

    worker.registerActivitiesImplementations(new GreetingActivitiesImpl());

    factory.start();

    GreetingWorkflow workflow =
        client.newWorkflowStub(
            GreetingWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(WORKFLOW_ID)
                .setTaskQueue(TASK_QUEUE)
                .build());

    String greeting = workflow.getGreeting("World");

    log.info(greeting);
    System.exit(0);
  }
}
