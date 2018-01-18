package jp.co.bizreach.trace.play26

import java.util
import javax.inject.Inject

import brave.{NoopSpan, RealSpan, Tracing}
import brave.sampler.Sampler
import jp.co.bizreach.trace.{TraceData, ZipkinTraceServiceLike}
import zipkin2.codec.Encoding
import zipkin2.reporter.{AsyncReporter, Sender}
import zipkin2.{Call, Callback}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

/**
  * Dummy trace service implementation.
  */
class ZipkinDummyTraceService @Inject()(ec: ExecutionContext) extends ZipkinTraceServiceLike {

  implicit val executionContext: ExecutionContext = ec

  val tracing: Tracing = Tracing
    .newBuilder()
    .localServiceName("unknow")
    .spanReporter(AsyncReporter.create(new Sender {
      def encoding: Encoding = Encoding.JSON

      def messageMaxBytes: Int = 5 * 1024 * 1024

      def messageSizeInBytes(encodedSpans: util.List[Array[Byte]]): Int = 2

      def sendSpans(encodedSpans: util.List[Array[Byte]]): Call[Void] = new Call[Void] {

        override def execute: Void = None.orNull

        override def enqueue(delegate: Callback[Void]): Unit = {}

        override def cancel(): Unit = {}

        override def isCanceled: Boolean = true

        override def clone: Call[Void] = this

      }
    }))
    .sampler(Sampler.ALWAYS_SAMPLE)
    .build()

  override def trace[A](traceName: String, tags: (String, String)*)(f: TraceData => A)(
    implicit parentData: TraceData): A = {
    Try(f(TraceData(this.newSpan(None)))) match {
      case Failure(t) => throw t
      case Success(result) => result
    }
  }

  def close(): Unit = {}

}
