package jp.co.bizreach.trace.play26

import javax.inject.Inject

import akka.actor.ActorSystem
import brave.Tracing
import brave.sampler.Sampler
import jp.co.bizreach.trace.{WithReporter, ZipkinTraceConfig, ZipkinTraceServiceLike}
import play.api.Configuration
import zipkin2.Span
import zipkin2.reporter.kafka11.KafkaSender
import zipkin2.reporter.{AsyncReporter, Reporter, Sender}

import scala.concurrent.ExecutionContext

/**
  * Kafka trace service implementation
  *
  * @param conf        the play configuration used to get kafka connection information
  * @param actorSystem the actor system used to get the current execution context
  */
class ZipkinKafkaTraceService @Inject()
(
  conf: Configuration,
  actorSystem: ActorSystem
) extends ZipkinTraceServiceLike with WithReporter {

  implicit val executionContext: ExecutionContext = actorSystem.dispatchers.lookup(ZipkinTraceConfig.AkkaName)

  val tracing: Tracing = Tracing
    .newBuilder()
    .localServiceName(serviceName)
    .spanReporter(reporter)
    .sampler(sampleRate)
    .build()

  // Defines the sampler
  lazy val sampleRate: Sampler =
    conf
      .getOptional[String](ZipkinTraceConfig.ZipkinSampleRate)
      .map(s => Sampler.create(s.toFloat))
      .getOrElse(Sampler.ALWAYS_SAMPLE)

  // Defines the service name
  lazy val serviceName: String =
    conf
      .getOptional[String](ZipkinTraceConfig.ServiceName)
      .getOrElse("unknown")

  // Defines the kafka reporter
  override lazy val reporter: Reporter[Span] = AsyncReporter.create(sender.get)

  override lazy val sender: Option[Sender] =
    Some(
      KafkaSender
        .newBuilder()
        .bootstrapServers(conf.getOptional[String](ZipkinKafkaTraceService.ZipkinBaseUrl).getOrElse(""))
        .topic(conf.getOptional[String](ZipkinKafkaTraceService.ZipkinTopic).getOrElse("zipkin"))
        .build()
    )
}

object ZipkinKafkaTraceService {
  val ZipkinBaseUrl = "trace.zipkin.kafka-url"
  val ZipkinTopic = "trace.zipkin.topic"
}
