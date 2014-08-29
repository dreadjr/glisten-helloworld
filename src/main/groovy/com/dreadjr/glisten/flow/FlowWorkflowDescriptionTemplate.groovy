package com.dreadjr.glisten.flow

import com.netflix.glisten.WorkflowDescriptionTemplate

/**
 * Constructs the description for a specific execution of the FlowWorkflow.
 */
class FlowWorkflowDescriptionTemplate extends WorkflowDescriptionTemplate implements FlowWorkflow {

  @Override
  void start(Map attrs) {
    description = "${attrs.input} is being processed."
  }
}
