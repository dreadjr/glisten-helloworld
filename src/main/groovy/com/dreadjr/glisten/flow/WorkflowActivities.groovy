package com.dreadjr.glisten.flow

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions

/**
 * SWF activities for the FlowWorkflow example.
 */

@Activities(version = '1.0')
@ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = -1L, defaultTaskStartToCloseTimeoutSeconds = 300L)
interface WorkflowActivities {
  @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = -1L, defaultTaskStartToCloseTimeoutSeconds = 86400L)
  String step1(String input)

//    String extractFeatures(String name, BayAreaLocation location)
//    String classify(String name, BayAreaLocation location)

  @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = -1L, defaultTaskStartToCloseTimeoutSeconds = 86400L)
  String step2(String input)

  @ActivityRegistrationOptions(defaultTaskScheduleToStartTimeoutSeconds = -1L, defaultTaskStartToCloseTimeoutSeconds = 86400L)
  String step3(String input)
}
