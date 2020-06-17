/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.editing

import java.util.concurrent.Executors
import java.util.regex.Pattern
import javafx.concurrent.Task
import org.fxmisc.richtext.model.{StyleSpans, StyleSpansBuilder}
import tigerjython.core.Preferences

/**
 * @author Tobias Kohn
 */
object SyntaxHighlighter {

  private val executor = Executors.newSingleThreadExecutor()

  private val KEYWORDS = Array(
    "and", "as", "assert", "async", "await", "break", "class", "continue", "def", "del", "elif", "else",
    "except", "finally", "for", "from", "global", "if", "import", "in", "is", "lambda", "nonlocal", "not",
    "or", "pass", "raise", "return", "try", "while", "with", "yield",
    "False", "None", "True"
  )

  private val COMMENT_PATTERN = "#[^\\n]*"
  private val KEYWORD_PATTERN = "\\b(" + KEYWORDS.mkString("|") + "|repeat)\\b"
  private val KEYWORD_PATTERN_PLAIN = "\\b(" + KEYWORDS.mkString("|") + ")\\b"
  private val STRING_PATTERN = "\\\"([^\\\"\\\\\\\\]|\\\\\\\\.)*\\\""

  private lazy val PATTERN = Pattern.compile(
    "(?<KEYWORD>" + KEYWORD_PATTERN + ")" +
      "|(?<STRING>" + STRING_PATTERN + ")" +
      "|(?<COMMENT>" + COMMENT_PATTERN + ")"
  )

  private lazy val PATTERN_PLAIN = Pattern.compile(
    "(?<KEYWORD>" + KEYWORD_PATTERN_PLAIN + ")" +
      "|(?<STRING>" + STRING_PATTERN + ")" +
      "|(?<COMMENT>" + COMMENT_PATTERN + ")"
  )

  def computeHighlightingAsync(text: String): Task[StyleSpans[java.util.Collection[String]]] = {
    val task = new Task[StyleSpans[java.util.Collection[String]]]() {
      override def call(): StyleSpans[java.util.Collection[String]] =
        computeHighlighting(text)
    }
    executor.execute(task)
    task
  }

  def computeHighlighting(text: String): StyleSpans[java.util.Collection[String]] = {
    val matcher =
      if (Preferences.repeatLoop.get)
        PATTERN.matcher(text)
      else
        PATTERN_PLAIN.matcher(text)
    val spansBuilder = new StyleSpansBuilder[java.util.Collection[String]]()
    var lastKwEnd = 0
    while (matcher.find()) {
      val styleClass =
        if (matcher.group("KEYWORD") != null)
          "keyword"
        else if (matcher.group("STRING") != null)
          "string"
        else
          "comment"
      spansBuilder.add(java.util.Collections.emptyList(), matcher.start() - lastKwEnd)
      spansBuilder.add(java.util.Collections.singleton(styleClass), matcher.end() - matcher.start())
      lastKwEnd = matcher.end()
    }
    spansBuilder.add(java.util.Collections.emptyList(), text.length - lastKwEnd)
    spansBuilder.create()
  }

  def execute(command: Runnable): Unit =
    executor.execute(command: Runnable)
}
