package jp.co.bizreach.trace

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

/**
  * TSpan annotation used to wrap a method body with tracing boilerplate code. Methods using this annotation must
  * explicitely define an implicit [[TraceData]] in its arguments.
  *
  * @param spanName the name of the span, how it will be displayed in the Zipkin UI.
  */
class TSpan(spanName: String) extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro TSpanMacro.impl
}

object TSpanMacro {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {

    import c.universe._

    val span: String = c.prefix.tree match {
      case q"new TSpan($spanName)" => c.eval[String](c.Expr(spanName))
    }

    val result = {
      annottees.map(_.tree).toList match {
        case q"$mods def $methodName[..$tpes](...$args): $returnType = { ..$body }" :: Nil =>
          q"""$mods def $methodName[..$tpes](...$args): $returnType =  {
            tracer.trace($span) { implicit traceData => ..$body }
          }"""
        case _ => c.abort(c.enclosingPosition, "Annotation @TSpan can be used only with methods")
      }
    }
    c.Expr[Any](result)
  }
}
