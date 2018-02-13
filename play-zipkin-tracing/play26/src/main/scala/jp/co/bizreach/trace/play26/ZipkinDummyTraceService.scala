package jp.co.bizreach.trace.play26

import javax.inject.Inject

import brave.Tracing
import brave.sampler.Sampler
import jp.co.bizreach.trace.{TraceData, WithReporter, ZipkinTraceServiceLike}
import zipkin2.Span
import zipkin2.reporter.{Reporter, Sender}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

/**
  * Dummy trace service implementation.
  */
class ZipkinDummyTraceService @Inject()(ec: ExecutionContext) extends ZipkinTraceServiceLike with WithReporter {

  implicit val executionContext: ExecutionContext = ec

  val tracing: Tracing = Tracing
    .newBuilder()
    .localServiceName("unknow")
    .spanReporter(reporter)
    .sampler(Sampler.ALWAYS_SAMPLE)
    .build()

  override def trace[A](traceName: String, tags: (String, String)*)(f: TraceData => A)(
    implicit parentData: TraceData): A = {
    Try(f(TraceData(this.newSpan(None)))) match {
      case Failure(t) => throw t
      case Success(result) => result
    }
  }

  override lazy val reporter: Reporter[Span] = new Reporter[Span] {
    override def report(span: Span): Unit = {
      // Ignore
    }
  }

  override val sender: Option[Sender] = None
}
