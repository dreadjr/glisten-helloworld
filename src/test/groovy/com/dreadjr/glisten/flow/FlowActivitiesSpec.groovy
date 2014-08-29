package com.dreadjr.glisten.flow

import com.netflix.glisten.ActivityOperations
import spock.lang.Specification

class FlowActivitiesSpec extends Specification {

  ActivityOperations mockActivity = Mock(ActivityOperations)
  WorkflowActivitiesImpl flowActivities = new WorkflowActivitiesImpl(
      activityOperations: mockActivity //, hikeNameToLengthInSteps: ['there': 3]
  )

  def 'should step1 simple'() {
    expect:
    flowActivities.step1('input') == "{result={status=1}}"
  }

  def 'should step2'() {
    expect:
    flowActivities.step2('input') == "{result={status=1}}"
  }

  def 'should step3'() {
    expect:
    flowActivities.step3('input') == "{result={status=1}}"
  }

  def 'should step1'() {
    when:
    String expectedResult = flowActivities.step1('filepath')

    then:
    expectedResult == "{result={status=1}}"
    with(mockActivity) {
      1 * recordHeartbeat('Step1 completed')
    }
    0 * _
  }

//    def 'should go to Monterey'() {
//        expect:
//        flowActivities.goTo('Clay', BayAreaLocation.Monterey) == 'Clay went to Monterey Bay.'
//    }
//
//    def 'should enjoy something'() {
//        expect:
//        flowActivities.enjoy('ice cream') == 'And enjoyed ice cream.'
//    }
//
//    def 'should hike'() {
//        when:
//        String expectedResult = flowActivities.hike('there')
//
//        then:
//        expectedResult == 'And hiked there.'
//        with(mockActivity) {
//            1 * recordHeartbeat('Took 1 steps.')
//            1 * recordHeartbeat('Took 2 steps.')
//            1 * recordHeartbeat('Took 3 steps.')
//        }
//        0 * _
//    }
//
//    def 'should win'() {
//        flowActivities.isWinner = { true }
//
//        expect:
//        flowActivities.win('a chess match') == 'And won a chess match.'
//    }
//
//    def 'should lose'() {
//        flowActivities.isWinner = { false }
//
//        when:
//        flowActivities.win('a chess match')
//
//        then:
//        IllegalStateException e = thrown()
//        e.message == 'And lost a chess match.'
//    }
//
//    def 'should ask question'() {
//        when:
//        flowActivities.askYesNoQuestion('Are you going to answer this question with a lie?')
//
//        then:
//        with(mockActivity) {
//            1 * getTaskToken()
//            1 * getWorkflowExecution()
//        }
//    }

}
