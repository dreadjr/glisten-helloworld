package com.dreadjr.glisten.flow

import com.amazonaws.services.simpleworkflow.flow.annotations.Execute
import com.amazonaws.services.simpleworkflow.flow.annotations.GetState
import com.amazonaws.services.simpleworkflow.flow.annotations.Workflow
import com.amazonaws.services.simpleworkflow.flow.annotations.WorkflowRegistrationOptions

/**
 * Example of an SWF workflow that describes a trip to various locations.
 */
@Workflow
@WorkflowRegistrationOptions(defaultExecutionStartToCloseTimeoutSeconds = 60L)
interface FlowWorkflow {

  /**
   * Start the workflow. The Execute annotation indicates that this it the entry point of the workflow.
   *
   * @param attrs of the file(s) to be processed by Flow
   */
  @Execute(version = '1.0')
  void start(Map attrs)

  /**
   * The GetState annotation indicates that this is the method that will return the current state of the workflow.
   * The result is put in the execution context field of a DecisionTaskCompletedEvent in the workflow history.
   * Since a workflow log is not provided by SWF, Glisten (ab)uses the execution context field to store all workflow
   * status messages.
   * @see com.amazonaws.services.simpleworkflow.model.DecisionTaskCompletedEventAttributes#executionContext
   * @see com.netflix.glisten.HistoryAnalyzer#getLogMessages()
   * @see com.netflix.glisten.WorkflowOperations#logHistory
   *
   * @return workflow status
   */
  @GetState
  List<String> getLogHistory()

}
