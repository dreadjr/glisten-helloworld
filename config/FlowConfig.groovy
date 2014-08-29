aws {
  AWS_ACCESS_KEY_ID = System.env.AWS_ACCESS_KEY_ID ?: "default"
  AWS_SECRET_ACCESS_KEY = System.env.AWS_SECRET_ACCESS_KEY ?: "default"

  swf {
    domain {
      name = "FlowProcess"
      description = "Domain for running the Flow process."
      task_list = "FlowProcessTaskList"
    }
    tags {
      name = "FlowWorkflowTags"
    }
  }
}

log4j {
  appender.stdout = "org.apache.log4j.ConsoleAppender"
  appender."stdout.layout"="org.apache.log4j.PatternLayout"
  appender.scrlog = "org.apache.log4j.FileAppender"
  appender."scrlog.layout"="org.apache.log4j.TTCCLayout"
  appender."scrlog.file"="script.log"

  //rootLogger="debug,scrlog,stdout"
  rootLogger="scrlog,stdout"

  /** these don't do nothing but illustrate how to configure logging on packages
   logger.org.springframework="info,stdout"
   additivity.org.springframework=false
   **/
}

environments {
  development {}
  test {}
  production {}
}