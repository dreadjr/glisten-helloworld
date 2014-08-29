package com.dreadjr.glisten.flow
import com.amazonaws.services.simpleworkflow.flow.core.Promise
import com.amazonaws.services.simpleworkflow.flow.interceptors.ExponentialRetryPolicy
import com.amazonaws.services.simpleworkflow.flow.interceptors.RetryPolicy
import com.netflix.glisten.DoTry
import com.netflix.glisten.WorkflowOperations
import com.netflix.glisten.WorkflowOperator
import com.netflix.glisten.example.trip.BayAreaLocation
import com.netflix.glisten.example.trip.NotDoneYetException
import com.netflix.glisten.impl.swf.SwfWorkflowOperations
/**
 * SWF workflow implementation for the BayAreaTripWorkflow example.
 */
class FlowWorkflowImpl implements FlowWorkflow, WorkflowOperator<WorkflowActivities> {

  @Delegate
  WorkflowOperations<WorkflowActivities> workflowOperations = SwfWorkflowOperations.of(WorkflowActivities)

  @Override
  void start(Map attrs) {
    println "Yo yo ${attrs.input}"
//    final Settable<String> taskList = new Settable<String>();

//      Promise<String> activityWorkerTaskList = store.download(sourceBucketName, sourceFilename, localSourceFilename);
//      // chaining is a way for one promise get assigned value of another
//      taskList.chain(activityWorkerTaskList);
//      // Call processFile activity to zip the file
//      Promise<Void> fileProcessed = processFileOnHost(localSourceFilename, localTargetFilename, activityWorkerTaskList);
//      // Call upload activity to upload zipped file
//      upload(targetBucketName, targetFilename, localTargetFilename, taskList, fileProcessed);

    doTry {
      Promise<String> step1 = promiseFor(activities.step1(attrs.input));
      Promise<String> step2 = promiseFor(activities.step2(attrs.input));
      Promise<String> step3 = promiseFor(activities.step3(attrs.input));

//      waitFor(step1) {
//        println "here ${it}"
//        status it
//      }

//      waitFor(allPromises(step1, step2, step3)) {
//        status it
//        Promise.Void()
//      }

      waitFor(step1) {
        println "step1"
        println it
        status it

        waitFor(step2) {
          println "step2"
          println it
          status it

          waitFor(step3) {
            println "step3"
            println it
            status it
          }
        }
      }

//      waitFor(activities.step1(attrs.file)) {
//        println "step1"
//        println it
//        status it
//
//        waitFor(activities.step2(attrs.file)) {
//          println "step2"
//          println it
//          status it
//
//          waitFor(activities.step3(attrs.file)) {
//            println "step3"
//            println it
//
//            status it
//
//          }
//        }
//
////        Promise.Void()
//      }

//      waitFor(activities.step1(attrs.file)) {
//        println 'step1'
//        status it
//        println 'step1-status'
//
//        waitFor(activities.step2(attrs.file)) {
//          println 'step2'
//          status it
//
//          waitFor(activities.step3(attrs.file)) {
//            println 'step3'
//            status it
//
//          }
//        }
//      }
    } withCatch { Throwable t ->
      status "Oh Noes! ${t.message}"
      //status t.message
    }
    Promise.Void();

//        Promise<BayAreaLocation> destinationPromise = determineDestination(previouslyVisited)
//        waitFor(destinationPromise) {
//            BayAreaLocation destination -> waitFor(activities.goTo(name, destination)) {
//                status it
//                Map<BayAreaLocation, Closure<Promise<Void>>> doAtLocation = [
//                        (BayAreaLocation.GoldenGateBridge): this.&doAtBridge,
//                        (BayAreaLocation.Redwoods): this.&doAtRedwoods,
//                        (BayAreaLocation.Monterey): this.&doAtMonterey,
//                        (BayAreaLocation.Boardwalk): this.&doAtBoardwalk
//                ]
//                doTry {
//                    doAtLocation[destination].call()
//                } withCatch { Throwable t ->
//                    status "Oh Noes! ${t.message}"
//                }
//                Promise.Void()
//            }
//        }
  }

//    private Promise<Void>

  private Promise<BayAreaLocation> determineDestination(previouslyVisited) {
    if (!previouslyVisited.contains(BayAreaLocation.GoldenGateBridge)) {
      return promiseFor(BayAreaLocation.GoldenGateBridge)
    }
    if (!previouslyVisited.contains(BayAreaLocation.Redwoods)) {
      return promiseFor(BayAreaLocation.Redwoods)
    }
    waitFor(activities.askYesNoQuestion('Do you like roller coasters?')) { boolean isThrillSeeker ->
      if (isThrillSeeker) {
        return promiseFor(BayAreaLocation.Boardwalk)
      }
      promiseFor(BayAreaLocation.Monterey)
    }
  }

  private Promise<Void> doAtBridge() {
    waitFor(activities.hike('across the bridge')) {
      status it
    }
  }

  private Promise<Void> doAtRedwoods() {
    // take time to stretch before hiking
    status 'And stretched for 10 seconds before hiking.'
    waitFor(timer(10, 'stretching')) {
      DoTry<String> hiking = doTry {
        retry(new ExponentialRetryPolicy(1).withExceptionsToRetry([NotDoneYetException])) {
          promiseFor(activities.hike('through redwoods'))
        }
      }
      DoTry<Void> countDown = cancelableTimer(30, 'countDown')

      // hike until done or out of time (which ever comes first)
      Promise<Boolean> doneHiking = waitFor(anyPromises(countDown.result, hiking.result)) {
        if (hiking.result.isReady()) {
          countDown.cancel(null)
          status "${hiking.result.get()}"
        } else {
          hiking.cancel(null)
          status 'And ran out of time when hiking.'
        }
        Promise.asPromise(true)
      }
      waitFor(doneHiking) {
        status 'Left forest safely (no bigfoot attack today).'
      }
    }
  }

  private Promise<Void> doAtMonterey() {
    // parallel activities (eat while watching)
    Promise<String> eating = promiseFor(activities.enjoy('eating seafood'))
    Promise<String> watching = promiseFor(activities.enjoy('watching sea lions'))
    waitFor(allPromises(eating, watching)) {
      status "${eating.get()} ${watching.get()}"
      doTry {
        promiseFor(activities.enjoy('looking for sea glass on the beach'))
      } withCatch { Throwable t ->
        status t.message
        promiseFor(activities.enjoy('the aquarium'))
      } withFinally { String result ->
        status result
        waitFor(activities.enjoy('the 17-Mile Drive')) { status it }
      }
      Promise.Void()
    }
  }

  @SuppressWarnings('UnnecessaryReturnKeyword')
  private Promise<Void> doAtBoardwalk() {
    int numberOfTokensGiven = 3
    int numberOfTokens = numberOfTokensGiven
    RetryPolicy retryPolicy = new ExponentialRetryPolicy(60).
        withMaximumAttempts(numberOfTokens).withExceptionsToRetry([IllegalStateException])
    DoTry<String> tryToWin = doTry {
      return retry(retryPolicy) {
        numberOfTokens--
        promiseFor(activities.win('a carnival game'))
      }
    } withCatch { Throwable e ->
      return promiseFor("${e.message} ${numberOfTokensGiven} times.")
    }
    waitFor(tryToWin.result) {
      status it
      if (numberOfTokens > 0) {
        waitFor(activities.enjoy('a roller coaster')) { status it }
      }
      Promise.Void()
    }
  }

}
