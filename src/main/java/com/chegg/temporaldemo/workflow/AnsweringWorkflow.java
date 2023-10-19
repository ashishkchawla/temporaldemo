package com.chegg.temporaldemo.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface AnsweringWorkflow {

    @WorkflowMethod
    String getAnswered(String questionUUID);
}

