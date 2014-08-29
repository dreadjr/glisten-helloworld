package com.dreadjr.glisten.flow

import com.netflix.glisten.impl.local.LocalWorkflowOperations
import spock.lang.Specification

class FlowWorkflowSpec extends Specification {

  WorkflowActivities mockActivities = Mock(WorkflowActivities)
  LocalWorkflowOperations workflowOperations = LocalWorkflowOperations.of(mockActivities)
  def workflowExecuter = workflowOperations.getExecuter(FlowWorkflowImpl)

  def 'should process a "flow"'() {
    def attrs = [input: 'input']

    when:
    workflowExecuter.start(input: 'input')

    then:
    workflowOperations.logHistory == [
        "{result={status=1}}",
        "{result={status=1}}",
        "{result={status=1}}"
    ]
    0 * _
    then: 1 * mockActivities.step1(attrs.input) >> "{result={status=1}}"
    then: 1 * mockActivities.step2(attrs.input) >> "{result={status=1}}"
    then: 1 * mockActivities.step3(attrs.input) >> "{result={status=1}}"
  }
}
