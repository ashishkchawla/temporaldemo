package com.chegg.temporaldemo.activity;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface DemoActivities {
    String answerQuestion(String uuid);
}
