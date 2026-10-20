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

package com.userlabs.codestudio.templates.impl

import com.userlabs.codestudio.templates.BooleanParameter
import com.userlabs.codestudio.templates.EnumParameter
import com.userlabs.codestudio.templates.Language
import com.userlabs.codestudio.templates.ProjectTemplate
import com.userlabs.codestudio.templates.ProjectVersionData
import com.userlabs.codestudio.templates.Sdk
import com.userlabs.codestudio.templates.StringParameter
import com.userlabs.codestudio.templates.base.AndroidModuleTemplateBuilder
import com.userlabs.codestudio.templates.base.ProjectTemplateBuilder
import com.userlabs.codestudio.templates.base.baseProject
import com.userlabs.codestudio.templates.impl.base.createRecipe
import com.userlabs.codestudio.templates.minSdkParameter
import com.userlabs.codestudio.templates.packageNameParameter
import com.userlabs.codestudio.templates.projectLanguageParameter
import com.userlabs.codestudio.templates.projectNameParameter
import com.userlabs.codestudio.templates.useKtsParameter

/**
 * Indents the given string for the given [indentation level][level].
 */
fun String.indentToLevel(level: Int): String {
  val lines = split(Regex("[\r\n]"))
  return StringBuilder().apply {
    for (line in lines) {
      append(line)
      append(" ".repeat(level * 4))
    }
  }.toString()
}

@Suppress("UnusedReceiverParameter")
internal fun AndroidModuleTemplateBuilder.templateAsset(name: String,
                                                        path: String
): String {
  return "templates/${name}/${path}"
}

internal inline fun baseProjectImpl(
  projectName: StringParameter = projectNameParameter(),
  packageName: StringParameter = packageNameParameter(),
  useKts: BooleanParameter = useKtsParameter(),
  minSdk: EnumParameter<Sdk> = minSdkParameter(),
  language: EnumParameter<Language> = projectLanguageParameter(),
  projectVersionData: ProjectVersionData = ProjectVersionData(),
  crossinline block: ProjectTemplateBuilder.() -> Unit
): ProjectTemplate =
  baseProject(projectName = projectName, packageName = packageName,
    useKts = useKts, minSdk = minSdk, language = language,
    projectVersionData = projectVersionData) {
    block()

    // make sure we return a proper result
    if (!isRecipeSet) {
      recipe = createRecipe {}
    }
  }