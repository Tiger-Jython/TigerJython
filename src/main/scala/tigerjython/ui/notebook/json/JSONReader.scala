/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.notebook.json

import scala.collection.BufferedIterator

/**
 * Parses a JSON file and returns a tree of `JSONValue`s.
 *
 * There are more mature and good packages for reading JSON out there.  However, in order to minimise dependencies we
 * implement our own JSON parser here.
 */
class JSONReader(val tokenArray: Array[JSONToken]) {

  def this(source: CharSequence) = this(JSONTokenizer.tokenize(source))

  protected val tokens: BufferedIterator[JSONToken] = tokenArray.iterator.buffered

  def parse(): JSONValue = parse_atom()

  protected def parse_list(): JSONValue = {
    val result = collection.mutable.ArrayBuffer[JSONValue]()
    while (tokens.hasNext && !tokens.head.isInstanceOf[JSONToken.RightBracket]) {
      result += parse_atom()
      if (tokens.head.isInstanceOf[JSONToken.Comma])
        tokens.next()
    }
    tokens.next() match {
      case JSONToken.RightBracket(_) =>
      case t =>
        throw new RuntimeException(s"Syntax error in JSON parsing. Expected ']', got '$t'")
    }
    JSONArray(result.toArray)
  }

  protected def parse_map(): JSONValue = {
    val result = collection.mutable.Map[String, JSONValue]()
    while (tokens.hasNext && !tokens.head.isInstanceOf[JSONToken.RightBrace]) {
      result += parse_key_value_pair()
      if (tokens.head.isInstanceOf[JSONToken.Comma])
        tokens.next()
    }
    tokens.next() match {
      case JSONToken.RightBrace(_) =>
      case t =>
        throw new RuntimeException(s"Syntax error in JSON parsing. Expected '}', got '$t'")
    }
    JSONMap(result.toMap)
  }

  protected def parse_key_value_pair(): (String, JSONValue) = {
    val key = tokens.next() match {
      case JSONToken.Name(value, _) =>
        value
      case JSONToken.StringValue(value, _) =>
        value
      case k =>
        throw new RuntimeException(s"Syntax error in JSON parsing. Expected name, got '$k'")
    }
    tokens.next() match {
      case JSONToken.Colon(_) =>
      case c =>
        throw new RuntimeException(s"Syntax error in JSON parsing. Expected colon, got '$c'")
    }
    val value = parse_atom()
    (key, value)
  }

  protected def parse_atom(): JSONValue =
    tokens.next() match {
      case JSONToken.BooleanValue(value, _) =>
        JSONBoolean(value)
      case JSONToken.IntegerValue(value, _) =>
        JSONInteger(value)
      case JSONToken.Name(value, _) =>
        JSONName(value)
      case JSONToken.NumberValue(value, _) =>
        JSONNumber(value)
      case JSONToken.StringValue(value, _) =>
        JSONString(value)
      case JSONToken.LeftBrace(_) =>
        parse_map()
      case JSONToken.LeftBracket(_) =>
        parse_list()
      case v =>
        throw new RuntimeException(s"Syntax error in JSON parsing. Expected JSON value, got '$v'")
    }
}
