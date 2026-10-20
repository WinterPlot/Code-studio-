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
package com.userlabs.codestudio.lsp.java.actions.diagnostics

import com.userlabs.codestudio.actions.ActionData
import com.userlabs.codestudio.actions.hasRequiredData
import com.userlabs.codestudio.actions.markInvisible
import com.userlabs.codestudio.actions.requireFile
import com.userlabs.codestudio.actions.requirePath
import com.userlabs.codestudio.lsp.java.JavaCompilerProvider
import com.userlabs.codestudio.lsp.java.actions.BaseJavaCodeAction
import com.userlabs.codestudio.lsp.java.models.DiagnosticCode
import com.userlabs.codestudio.lsp.java.rewrite.CreateMissingMethod
import com.userlabs.codestudio.lsp.java.utils.CodeActionUtils.findPosition
import com.userlabs.codestudio.projects.IProjectManager
import com.userlabs.codestudio.resources.R
import org.slf4j.LoggerFactory

/** @author Akash Yadav */
class CreateMissingMethodAction : BaseJavaCodeAction() {

  override val id: String = "ide.editor.lsp.java.diagnostics.createMissingMethod"
  override var label: String = ""
  private val diagnosticCode = DiagnosticCode.MISSING_METHOD.id

  override val titleTextRes: Int = R.string.action_create_missing_method

  companion object {

    private val log = LoggerFactory.getLogger(CreateMissingMethodAction::class.java)
  }

  override fun prepare(data: ActionData) {
    super.prepare(data)

    if (
      !visible ||
      !data.hasRequiredData(com.userlabs.codestudio.lsp.models.DiagnosticItem::class.java)
    ) {
      markInvisible()
      return
    }

    val diagnostic = data[com.userlabs.codestudio.lsp.models.DiagnosticItem::class.java]!!
    if (diagnosticCode != diagnostic.code) {
      markInvisible()
      return
    }
  }

  override suspend fun execAction(data: ActionData): Any {
    val diagnostic = data[com.userlabs.codestudio.lsp.models.DiagnosticItem::class.java]!!
    val compiler =
      JavaCompilerProvider.get(
        IProjectManager.getInstance().getWorkspace()?.findModuleForFile(data.requireFile(), false)
          ?: return Any()
      )
    val file = data.requirePath()
    return compiler.compile(file).get {
      CreateMissingMethod(file, findPosition(it, diagnostic.range.start))
    }
  }

  override fun postExec(data: ActionData, result: Any) {
    if (result !is CreateMissingMethod) {
      log.warn("Unable to create missing method")
      return
    }

    performCodeAction(data, result)
  }
}
