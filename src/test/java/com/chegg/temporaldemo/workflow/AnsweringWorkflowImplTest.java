package com.chegg.temporaldemo.workflow;

import com.chegg.temporaldemo.activity.DemoActivitiesImpl;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowRule;
import org.junit.jupiter.api.Test;
//import org.junit.jupiter.R


import static org.junit.jupiter.api.Assertions.*;

class AnsweringWorkflowImplTest {


    public TestWorkflowRule testWorkflowRule =
            TestWorkflowRule.newBuilder()
                    .setWorkflowTypes(AnsweringWorkflowImpl.class)
                    .setDoNotStart(true)
                    .build();

    @Test
    public void testIntegrationGetGreeting() {
        testWorkflowRule.getWorker().registerActivitiesImplementations(new DemoActivitiesImpl());
        testWorkflowRule.getTestEnvironment().start();

        AnsweringWorkflow workflow =
                testWorkflowRule
                        .getWorkflowClient()
                        .newWorkflowStub(
                                AnsweringWorkflow.class,
                                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());
        String greeting = workflow.getAnswered("abcdef");
        assertEquals("Answer generated for abcdef", greeting);
        testWorkflowRule.getTestEnvironment().shutdown();
    }

   /* @Test
    public void testMockedGetGreeting() {
        HelloWorldActivities formatActivities = mock(HelloWorldActivities.class, withSettings().withoutAnnotations());
        when(formatActivities.composeGreeting(anyString())).thenReturn("Hello World!");
        testWorkflowRule.getWorker().registerActivitiesImplementations(formatActivities);
        testWorkflowRule.getTestEnvironment().start();

        HelloWorldWorkflow workflow =
                testWorkflowRule
                        .getWorkflowClient()
                        .newWorkflowStub(
                                HelloWorldWorkflow.class,
                                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());
        String greeting = workflow.getGreeting("World");
        assertEquals("Hello World!", greeting);
        testWorkflowRule.getTestEnvironment().shutdown();
    }*/
}