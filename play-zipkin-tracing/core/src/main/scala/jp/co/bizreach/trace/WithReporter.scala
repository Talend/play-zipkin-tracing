package jp.co.bizreach.trace

import zipkin2.Span
import zipkin2.reporter.{Reporter, Sender}

/**
  * Used to expose the [[Reporter]] and the [[Sender]] variables.
  * Implementations that extend this trait should use these variables to create a [[brave.Tracing]] instance.
  */
trait WithReporter {

  /**
    * The [[Reporter]]
    */
  val reporter: Reporter[Span]

  /**
    * The [[Sender]]. Optional because it could not exist in a dummy reporter implementation.
    */
  val sender: Option[Sender]

}
