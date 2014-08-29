package com.dreadjr.glisten.flow

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient
import com.amazonaws.services.simpleworkflow.flow.ActivityWorker
import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker
import com.amazonaws.services.simpleworkflow.model.*
import com.netflix.glisten.*
import groovy.util.logging.Log4j
import org.apache.log4j.PropertyConfigurator

import java.util.concurrent.TimeUnit

@Log4j
class FlowWorkflowHost {
  // TODO: pull out into separate builder/dsl class
  static void main(String[] args) {
    def config = new ConfigSlurper().parse(new File('config/FlowConfig.groovyy').toURI().toURL())
    PropertyConfigurator.configure(config.toProperties())

    println config

    log.info("Starting...")

    // load the AWS credentials
    final AWSCredentials awsCredentials = new BasicAWSCredentials(config.aws.AWS_ACCESS_KEY_ID, config.aws.AWS_SECRET_ACCESS_KEY)

    log.info("Loaded AWS credentials.")

    // create the AWS SWF client
    final AmazonSimpleWorkflow simpleWorkflow = new AmazonSimpleWorkflowClient(awsCredentials)

    log.info("Created SWF client.")

    // Make sure the domain is registered
    final listDomainsRequest = new ListDomainsRequest().withRegistrationStatus(RegistrationStatus.REGISTERED)
    final domainInfos = simpleWorkflow.listDomains(listDomainsRequest)
    println domainInfos.getDomainInfos()
    final domainExists =
        domainInfos.getDomainInfos().find { DomainInfo domainInfo ->
          domainInfo.getName() == config.aws.swf.domain.name
        } != null

    if (!domainExists) {
      // we need to register the domain because it doesn't exist
      final registerDomainRequest = new RegisterDomainRequest()
          .withName(config.aws.swf.domain.name)
          .withDescription(config.aws.swf.domain.description)
          .withWorkflowExecutionRetentionPeriodInDays("1")

      simpleWorkflow.registerDomain(registerDomainRequest)

      log.info("Registered SWF domain ${config.aws.swf.domain.name}.")
    }

    ////////////////////////
    // Setup the workflow
    ////////////////////////

    log.info("Creating workflow objects...")

    // create the Glisten WorkflowClientFactory
    final workflowClientFactory = new WorkflowClientFactory(simpleWorkflow, config.aws.swf.domain.name, config.aws.swf.domain.task_list)

    // the description template
    final WorkflowDescriptionTemplate workflowDescriptionTemplate = new FlowWorkflowDescriptionTemplate()

    // create tags -- these are required per https://github.com/Netflix/glisten/issues/21
    final workflowTags = new WorkflowTags(config.aws.swf.tags.name)

    // create the client for the BayAreaTripWorkflow
    final InterfaceBasedWorkflowClient<FlowWorkflow> glistenWorkflowClient =
        workflowClientFactory.getNewWorkflowClient(FlowWorkflow, workflowDescriptionTemplate, workflowTags)

    // create and start the workflow worker
    final workflowWorker = new WorkflowWorker(simpleWorkflow, config.aws.swf.domain.name, config.aws.swf.domain.task_list)
    workflowWorker.setWorkflowImplementationTypes([FlowWorkflowImpl])
    workflowWorker.start()

    // create the activity object
    WorkflowActivitiesImpl flowActivities = new WorkflowActivitiesImpl()

    // create and start the activity worker
    final activityWorker = new ActivityWorker(simpleWorkflow, config.aws.swf.domain.name, config.aws.swf.domain.task_list)
    activityWorker.addActivitiesImplementations([flowActivities])
    activityWorker.start()

    //////////////////////
    // start the workflow
    //////////////////////
    glistenWorkflowClient.asWorkflow().start(file: 'filepath')

    final workflowExecution = glistenWorkflowClient.getWorkflowExecution()
    final workflowId = workflowExecution.getWorkflowId()

    log.info("Running workflow execution $workflowId")

    ///////////////////////////
    // wait for it to finish
    ///////////////////////////

    List<HistoryEvent> historyEvents = []
    def running = true
    def executionContext = ""
    while (running) {
      log.info("Workflow still running...")

      // sleep a bit
      Thread.currentThread().sleep(5 * 1000)

      // get the history of workflow events
      final getWorkflowExecutionHistoryRequest = new GetWorkflowExecutionHistoryRequest()
          .withDomain(config.aws.swf.domain.name)
          .withExecution(workflowExecution)
      final history = simpleWorkflow.getWorkflowExecutionHistory(getWorkflowExecutionHistoryRequest)

      final latestEvents = history.getEvents()

      ///////////////////////
      // log the new events
      ///////////////////////

      final newEvents = latestEvents - historyEvents
      newEvents.each { HistoryEvent historyEvent ->
        log.info("Event: ${historyEvent.getEventTimestamp()}, ID: ${historyEvent.getEventId()}, Type: ${historyEvent.getEventType()}")

        final eventType = historyEvent.getEventType()
        if (eventType == "ActivityTaskScheduled") {
          final activityTaskScheduledEventAttributes = historyEvent.getActivityTaskScheduledEventAttributes()
          final activityId = activityTaskScheduledEventAttributes.getActivityId()
          final activityType = activityTaskScheduledEventAttributes.getActivityType()
          final activityTypeName = activityType.getName()
          final activityTypeVersion = activityType.getVersion()
          final input = activityTaskScheduledEventAttributes.getInput()

          log.info("ActivityTaskScheduled details -- activity ID: $activityId, name: $activityTypeName, version: $activityTypeVersion, input: $input")
        } else if (eventType == "ActivityTaskStarted") {
          final activityTaskStartedEventAttributes = historyEvent.getActivityTaskStartedEventAttributes()
          final workerIdentity = activityTaskStartedEventAttributes.getIdentity()
          final scheduledEventId = activityTaskStartedEventAttributes.getScheduledEventId()

          log.info("ActivityTaskStarted details -- worker ID: $workerIdentity, scheduled event ID: $scheduledEventId")
        } else if (eventType == "ActivityTaskCompleted") {
          final activityTaskCompletedEventAttributes = historyEvent.getActivityTaskCompletedEventAttributes()
          final scheduledEventId = activityTaskCompletedEventAttributes.getScheduledEventId()
          final result = activityTaskCompletedEventAttributes.getResult()
          final startedEventId = activityTaskCompletedEventAttributes.getStartedEventId()

          log.info("ActivityTaskCompleted details -- scheduled event ID: $scheduledEventId, result: $result, started event ID: $startedEventId")
        } else if (eventType == "DecisionTaskCompleted") {
          final decisionTaskCompletedEventAttributes = historyEvent.getDecisionTaskCompletedEventAttributes()

          final dtceExecutionContext = decisionTaskCompletedEventAttributes.getExecutionContext()
          final startedEventId = decisionTaskCompletedEventAttributes.getStartedEventId()
          final scheduledEventId = decisionTaskCompletedEventAttributes.getScheduledEventId()

          log.info("DecisionTaskCompletedEvent details -- execution context: $dtceExecutionContext, scheduled event ID: $scheduledEventId, started event ID: $startedEventId")
        }
      }
      // store the events
      historyEvents = latestEvents

      // see if the workflow is still running:
      final describeWorkflowExecutionRequest = new DescribeWorkflowExecutionRequest()
          .withExecution(workflowExecution)
          .withDomain(config.aws.swf.domain.name)

      final workflowExecutionDetail = simpleWorkflow.describeWorkflowExecution(describeWorkflowExecutionRequest)

      final workflowExecutionInfo = workflowExecutionDetail.getExecutionInfo()
      final executionStatus = workflowExecutionInfo.getExecutionStatus()
      executionContext = workflowExecutionDetail.getLatestExecutionContext()

      running = executionStatus == "OPEN"
    }

    log.info("The workflow is now complete.")

    log.info("Final raw workflow execution context: $executionContext")

    // Use the Glisten HistoryAnalyzer to get nicely formatted log messages
    final historyAnalyzer = HistoryAnalyzer.of(historyEvents)
    final logMessages = historyAnalyzer.getLogMessages()
    log.info("Log messages processed by the Glisten HistoryAnalyzer:")
    logMessages.each { final LogMessage logMessage ->
      final timestamp = logMessage.getTimestamp()
      final text = logMessage.getText()

      log.info("\t\t$timestamp $text")
    }

    // gracefully shutdown all workers
    log.info("Gracefully shutting down workers.")
    activityWorker.shutdownAndAwaitTermination(30, TimeUnit.SECONDS)
    log.info("Activity worker stopped.")

    workflowWorker.shutdownAndAwaitTermination(30, TimeUnit.SECONDS)
    log.info("Workflow worker stopped.")

  }


}
