/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.ui.editing

import java.util.concurrent.Executors
import javafx.concurrent.Task
import org.fxmisc.richtext.model.{StyleSpans, StyleSpansBuilder}
import tigerjython.core.Preferences
import tigerjython.syntaxsupport.{SyntaxDocument,TokenVisitor}

/**
 * @author Tobias Kohn
 */
object SyntaxHighlighter {

  private val executor = Executors.newSingleThreadExecutor()

  def computeHighlightingAsync(text: String, syntaxDocument: SyntaxDocument): Task[StyleSpans[java.util.Collection[String]]] = {
    val task = new Task[StyleSpans[java.util.Collection[String]]]() {
      override def call(): StyleSpans[java.util.Collection[String]] =
        computeHighlighting(text, syntaxDocument)
    }
    executor.execute(task)
    task
  }

  private class SpanTokenVisitor extends TokenVisitor {

    private val spansBuilder = new StyleSpansBuilder[java.util.Collection[String]]()

    def create(): StyleSpans[java.util.Collection[String]] = spansBuilder.create()

    def visitSyntaxNode(style: String, length: Int): Unit =
      if (style != null)
        spansBuilder.add(java.util.Collections.singleton(style), length)
      else
        spansBuilder.add(java.util.Collections.emptyList(), length)

    def visitWhitespaceSyntaxNode(length: Int): Unit =
      spansBuilder.add(java.util.Collections.emptyList(), length)
  }

  def computeHighlighting(text: String, syntaxDocument: SyntaxDocument): StyleSpans[java.util.Collection[String]] = {
    val visitor = new SpanTokenVisitor()
    syntaxDocument.repeatIsKeyword = Preferences.repeatLoop.get()
    syntaxDocument.setText(text)
    syntaxDocument.visit(visitor)
    visitor.create()
  }

  def execute(command: Runnable): Unit =
    executor.execute(command: Runnable)

  def shutdown(): Unit =
    executor.shutdown()
}
