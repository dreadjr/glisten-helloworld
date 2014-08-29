package com.dreadjr.glisten.flow

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient
import com.amazonaws.services.simpleworkflow.flow.ActivityWorker
import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker
import groovy.util.logging.Log4j
import org.apache.log4j.PropertyConfigurator

@Log4j
class FlowWorker {

  //https://github.com/Netflix/asgard/blob/master/grails-app/services/com/netflix/asgard/FlowService.groovy#L65
  //https://github.com/sjones4/glisten-playground/blob/master/src/main/java/com/github/sjones4/gplay/cancel/CancelMain.groovy
  //https://github.com/balsamiq/hello-world-glisten/blob/master/src/main/groovy/helloworld/swf/GreeterWorker.groovy
  //https://github.com/xnickmx/GlistenRunnableExample/blob/master/src/main/groovy/com/faceture/glisten/GlistenExample.groovy

    static void main(String [] args) {
      def flowConfig = new ConfigSlurper().parse(new File('config/FlowConfig.groovyy').toURI().toURL())
      PropertyConfigurator.configure(flowConfig.toProperties())

      AWSCredentials credentials = new BasicAWSCredentials(flowConfig.aws.AWS_ACCESS_KEY_ID, flowConfig.aws.AWS_SECRET_ACCESS_KEY)

      ClientConfiguration config = new ClientConfiguration().withSocketTimeout(70 * 1000)

      AmazonSimpleWorkflow service = new AmazonSimpleWorkflowClient(credentials, config)

      String domain = flowConfig.aws.swf.domain.name
      String taskListToPoll = flowConfig.aws.swf.domain.task_list

      ActivityWorker aw = new ActivityWorker(service, domain, taskListToPoll)
      aw.addActivitiesImplementation(new WorkflowActivitiesImpl())
      aw.start()

      WorkflowWorker wfw = new WorkflowWorker(service, domain, taskListToPoll)
      wfw.addWorkflowImplementationType(FlowWorkflowImpl)
      wfw.start()
    }
  }