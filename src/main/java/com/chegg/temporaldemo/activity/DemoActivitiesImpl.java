package com.chegg.temporaldemo.activity;

import org.springframework.stereotype.Component;

@Component
public class DemoActivitiesImpl implements DemoActivities{
    @Override
    public String answerQuestion(String uuid) {
        return "Answer generated for "+ uuid;
    }
}
