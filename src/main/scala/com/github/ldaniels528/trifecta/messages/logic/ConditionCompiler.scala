package com.github.ldaniels528.trifecta.messages.logic

import com.github.ldaniels528.trifecta.messages.codec.MessageDecoder
import com.github.ldaniels528.trifecta.messages.logic.Expressions._
import com.github.ldaniels528.trifecta.util.ParsingHelper._

/**
  * Condition Compiler
  * @author lawrence.daniels@gmail.com
  */
object ConditionCompiler {

  def compile(expression: Expression, decoder: Option[MessageDecoder[_]]): Condition = {
    expression match {
      case AND(a, b) => Conditions.AND(compile(a, decoder), compile(b, decoder))
      case KEY_EQ(v) => Conditions.KeyIs(translateValue(v))
      case OR(a, b) => Conditions.OR(compile(a, decoder), compile(b, decoder))
      case op =>
        decoder match {
          case Some(compiler: MessageEvaluation) => compiler.compile(op)
          case Some(_) => throw new IllegalStateException(s"The selected decoder is not a message compiler")
          case None => throw new IllegalStateException(s"No message decoder found to support `$op`")
        }
    }
  }

  def compile(field: String, operator: String, value: String): Expression = {
    operator match {
      case "==" => EQ(field, value)
      case "!=" => NE(field, value)
      case ">" => GT(field, value)
      case "<" => LT(field, value)
      case ">=" => GE(field, value)
      case "<=" => LE(field, value)
      case "is" =>
        if (field == "key") KEY_EQ(value)
        else throw new IllegalArgumentException("Only 'key' can be used with the verb 'is'")
      case _ => throw new IllegalArgumentException(s"Illegal operator '$operator' near '$field'")
    }
  }

  private def translateValue(value: String, encoding: String = "UTF-8"): Array[Byte] = {
    if (isDottedHex(value)) parseDottedHex(value) else value.getBytes(encoding)
  }

}
