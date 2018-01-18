package jp.co.bizreach.trace.akka.actor

import jp.co.bizreach.trace.akka.actor.ActorTraceSupport.RemoteActorTraceData
import play.api.libs.json._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
  * Play json formats for the [[RemoteActorTraceData]]
  */
object RemoteActorTraceDataFormats {

  trait RemoteActorTraceDataFormatLike {

    implicit val remoteActorTraceDataFormat: Format[RemoteActorTraceData] =
      RemoteActorTraceDataFormat.remoteActorTraceDataFormat

    object RemoteActorTraceDataFormat {

      implicit val remoteActorTraceDataReader: Reads[RemoteActorTraceData] = {
        (JsPath \ "span").read[Map[String, String]]
          .map(mymap => new java.util.HashMap[String, String](mymap))
          .map(RemoteActorTraceData.apply)
      }

      implicit val remoteActorTraceDataWriter = new OWrites[RemoteActorTraceData] {
        def writes(remoteActorTraceData: RemoteActorTraceData): JsObject = {
          val span: Map[String, String] = remoteActorTraceData.span.asScala.toMap
          Json.obj("span" -> span)
        }
      }

      implicit val remoteActorTraceDataFormat: Format[RemoteActorTraceData] =
        Format(remoteActorTraceDataReader, remoteActorTraceDataWriter)
    }

  }

}
