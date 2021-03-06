package jp.co.bizreach.trace.play23

import brave.Tracing
import brave.sampler.Sampler
import jp.co.bizreach.trace._
import play.api.{Play, Configuration}
import play.api.libs.concurrent.Akka
import zipkin2.reporter.AsyncReporter
import zipkin2.reporter.okhttp3.OkHttpSender

import scala.concurrent.ExecutionContext

/**
 * Object for Zipkin tracing at Play2.3.
 */
object ZipkinTraceService extends ZipkinTraceServiceLike {
  import play.api.Play.current
  val conf: Configuration = Play.configuration

  implicit val executionContext: ExecutionContext = Akka.system.dispatchers.lookup(ZipkinTraceConfig.AkkaName)

  val tracing = Tracing.newBuilder()
    .localServiceName(conf.getString(ZipkinTraceConfig.ServiceName) getOrElse "unknown")
    .spanReporter(AsyncReporter
      .create(OkHttpSender.create(
        (conf.getString(ZipkinTraceConfig.ZipkinBaseUrl) getOrElse "http://localhost:9411") + "/api/v2/spans"
      ))
    )
    .sampler(conf.getString(ZipkinTraceConfig.ZipkinSampleRate)
      .map(s => Sampler.create(s.toFloat)) getOrElse Sampler.ALWAYS_SAMPLE
    )
    .build()

}
