package jp.co.bizreach.trace.play26.implicits

import brave.Span
import brave.propagation.Propagation
import jp.co.bizreach.trace.akka.actor.ActorTraceSupport.{ActorTraceData, RemoteActorTraceData, RemoteSpan}
import jp.co.bizreach.trace.{TraceData, ZipkinTraceServiceLike}
import play.api.mvc.RequestHeader

trait ZipkinTraceImplicits {

  // for injection
  val tracer: ZipkinTraceServiceLike

  /**
    * Creates a trace data including a span from request headers.
    *
    * @param req the HTTP request header
    * @return the trace data
    */
  implicit def request2trace(implicit req: RequestHeader): TraceData = {
    TraceData(
      span = tracer.toSpan(req.headers)((headers, key) => headers.get(key))
    )
  }

  /**
    * Creates a trace data including a span from request headers for Akka actor.
    *
    * @param req the HTTP request header
    * @return the trace data
    */
  implicit def request2actorTrace(implicit req: RequestHeader): ActorTraceData = {
    val span = tracer.toSpan(req.headers)((headers, key) => headers.get(key))
    val oneWaySpan = tracer.newSpan(Some(span.context())).kind(Span.Kind.CLIENT)
    ActorTraceData(span = oneWaySpan)
  }

  /**
    * Creates a [[TraceData]] from a [[ActorTraceData]]
    *
    * @param traceData the [[ActorTraceData]]
    * @return the [[TraceData]]
    */
  def actorTraceData2TraceData(implicit traceData: ActorTraceData): TraceData = TraceData(traceData.span)

  /**
    * Creates a [[TraceData]] from a [[RemoteActorTraceData]]
    *
    * @param traceData the [[RemoteActorTraceData]]
    * @return the [[TraceData]]
    */
  def remoteActorTraceData2TraceData(implicit traceData: RemoteActorTraceData): TraceData = {
    val contextOrFlags = tracer.tracing.propagation().extractor(new Propagation.Getter[RemoteSpan, String] {
      def get(carrier: RemoteSpan, key: String): String = carrier.get(key)
    }).extract(traceData.span)

    val span = tracer.tracing.tracer.joinSpan(contextOrFlags.context())
    TraceData(span)
  }

  /**
    * Creates a [[RemoteActorTraceData]] from a [[TraceData]]
    *
    * @param traceData the [[TraceData]]
    * @return the [[RemoteActorTraceData]]
    */
  def traceData2RemoteActorTraceData(implicit traceData: TraceData): RemoteActorTraceData = {
    val data = new RemoteSpan()
    tracer.tracing.propagation().injector(new Propagation.Setter[RemoteSpan, String] {
      def put(carrier: RemoteSpan, key: String, value: String): Unit = carrier.put(key, value)
    }).inject(traceData.span.context(), data)
    RemoteActorTraceData(data)
  }

}
