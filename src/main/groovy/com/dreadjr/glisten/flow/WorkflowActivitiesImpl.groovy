package com.dreadjr.glisten.flow

import com.netflix.glisten.ActivityOperations
import com.netflix.glisten.impl.swf.SwfActivityOperations

/**
 * SWF activity implementations for the BayAreaTripWorkflow example.
 */
class WorkflowActivitiesImpl implements WorkflowActivities {

  @Delegate
  ActivityOperations activityOperations = new SwfActivityOperations()

  @Override
  String step1(String input) {
    // blah
    println "step1 impl"
    recordHeartbeat("Step1 completed")

    def jsonBuilder = new groovy.json.JsonBuilder()
    jsonBuilder.result(
        status: 1
    )
  }

  @Override
  String step2(String input) {
    // blah
    println "step2 impl"
    recordHeartbeat("Step2 completed")

    def jsonBuilder = new groovy.json.JsonBuilder()
    jsonBuilder.result(
        status: 1
    )
  }

  @Override
  String step3(String input) {
    // blah
    println "step3 impl"
    recordHeartbeat("Step3 completed")

    def jsonBuilder = new groovy.json.JsonBuilder()
    jsonBuilder.result(
        status: 1
    )
  }

}
