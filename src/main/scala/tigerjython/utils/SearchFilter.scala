/*
 * This file is part of the 'TigerJython' project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tigerjython.utils

/**
 * @author Tobias Kohn
 */
class SearchFilter(val searchText: String) {

  protected val words: Array[String] = searchText.split(' ')

  protected def findWords(word: String, text: String): Array[Int] = {
    val result = collection.mutable.ArrayBuffer[Int]()
    var index = text.indexOf(word)
    while (index >= 0) {
      result += index
      index = text.indexOf(word, index)
    }
    result.toArray
  }

  protected def hasWord(word: String, text: String): Boolean =
    text.contains(word)

  def getScore(text: String): Int =
    if (searchText == "")
      100
    else if (!text.contains(searchText)) {
      var result: Int = 0
      for (word <- words)
        if (hasWord(word, text))
          result += 1
      result
    } else
      100
}
object SearchFilter {

  def apply(searchText: String): SearchFilter = new SearchFilter(searchText)
}