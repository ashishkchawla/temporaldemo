package com.chegg.temporaldemo.workflow;

import com.chegg.temporaldemo.activity.DemoActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class AnsweringWorkflowImpl implements AnsweringWorkflow{

    ActivityOptions options = ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(60)).build();

    private final DemoActivities activity = Workflow.newActivityStub(DemoActivities.class, options);
    @Override
    public String getAnswered(String questionUUID) {
        return activity.answerQuestion(questionUUID);
    }
}
