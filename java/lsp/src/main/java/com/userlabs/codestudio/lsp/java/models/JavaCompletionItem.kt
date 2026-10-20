/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.userlabs.codestudio.lsp.java.models

import com.userlabs.codestudio.lsp.edits.IEditHandler
import com.userlabs.codestudio.lsp.java.edits.BaseJavaEditHandler
import com.userlabs.codestudio.lsp.models.Command
import com.userlabs.codestudio.lsp.models.CompletionItem
import com.userlabs.codestudio.lsp.models.CompletionItemKind
import com.userlabs.codestudio.lsp.models.ICompletionData
import com.userlabs.codestudio.lsp.models.InsertTextFormat
import com.userlabs.codestudio.lsp.models.MatchLevel
import com.userlabs.codestudio.lsp.models.TextEdit

/**
 * Completion item model for java completion items.
 *
 * @author Akash Yadav
 */
class JavaCompletionItem(
  label: String,
  detail: String,
  insertText: String?,
  insertTextFormat: InsertTextFormat?,
  sortText: String?,
  command: Command?,
  kind: CompletionItemKind,
  matchLevel: MatchLevel,
  additionalTextEdits: List<TextEdit>?,
  data: ICompletionData?,

  // Override the default edit handler
  editHandler: IEditHandler = BaseJavaEditHandler()
) :
  CompletionItem(
    label,
    detail,
    insertText,
    insertTextFormat,
    sortText,
    command,
    kind,
    matchLevel,
    additionalTextEdits,
    data,
    editHandler
  ) {

  constructor() :
    this(
      "", // label
      "", // detail
      null, // insertText
      null, // insertTextFormat
      null, // sortText
      null, // command
      CompletionItemKind.NONE, // kind
      MatchLevel.NO_MATCH, // match level
      ArrayList(), // additionalEdits
      null // data
    )
}
