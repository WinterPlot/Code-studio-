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
package com.userlabs.codestudio.tooling.api.util

import com.google.gson.GsonBuilder
import com.userlabs.codestudio.builder.model.DefaultJavaCompileOptions
import com.userlabs.codestudio.builder.model.IJavaCompilerSettings
import com.userlabs.codestudio.tooling.api.IProject
import com.userlabs.codestudio.tooling.api.IToolingApiClient
import com.userlabs.codestudio.tooling.api.IToolingApiServer
import com.userlabs.codestudio.tooling.api.models.AndroidProjectMetadata
import com.userlabs.codestudio.tooling.api.models.AndroidVariantMetadata
import com.userlabs.codestudio.tooling.api.models.BasicAndroidVariantMetadata
import com.userlabs.codestudio.tooling.api.models.BasicProjectMetadata
import com.userlabs.codestudio.tooling.api.models.GradleTask
import com.userlabs.codestudio.tooling.api.models.JavaModuleCompilerSettings
import com.userlabs.codestudio.tooling.api.models.JavaModuleDependency
import com.userlabs.codestudio.tooling.api.models.JavaModuleExternalDependency
import com.userlabs.codestudio.tooling.api.models.JavaModuleProjectDependency
import com.userlabs.codestudio.tooling.api.models.JavaProjectMetadata
import com.userlabs.codestudio.tooling.api.models.Launchable
import com.userlabs.codestudio.tooling.api.models.ProjectMetadata
import com.userlabs.codestudio.tooling.events.OperationDescriptor
import com.userlabs.codestudio.tooling.events.OperationResult
import com.userlabs.codestudio.tooling.events.ProgressEvent
import com.userlabs.codestudio.tooling.events.StatusEvent
import com.userlabs.codestudio.tooling.events.configuration.ProjectConfigurationFinishEvent
import com.userlabs.codestudio.tooling.events.configuration.ProjectConfigurationOperationDescriptor
import com.userlabs.codestudio.tooling.events.configuration.ProjectConfigurationOperationResult
import com.userlabs.codestudio.tooling.events.configuration.ProjectConfigurationProgressEvent
import com.userlabs.codestudio.tooling.events.configuration.ProjectConfigurationStartEvent
import com.userlabs.codestudio.tooling.events.download.FileDownloadFinishEvent
import com.userlabs.codestudio.tooling.events.download.FileDownloadOperationDescriptor
import com.userlabs.codestudio.tooling.events.download.FileDownloadProgressEvent
import com.userlabs.codestudio.tooling.events.download.FileDownloadResult
import com.userlabs.codestudio.tooling.events.download.FileDownloadStartEvent
import com.userlabs.codestudio.tooling.events.internal.DefaultFinishEvent
import com.userlabs.codestudio.tooling.events.internal.DefaultOperationDescriptor
import com.userlabs.codestudio.tooling.events.internal.DefaultOperationResult
import com.userlabs.codestudio.tooling.events.internal.DefaultProgressEvent
import com.userlabs.codestudio.tooling.events.internal.DefaultStartEvent
import com.userlabs.codestudio.tooling.events.task.TaskExecutionResult
import com.userlabs.codestudio.tooling.events.task.TaskFailureResult
import com.userlabs.codestudio.tooling.events.task.TaskFinishEvent
import com.userlabs.codestudio.tooling.events.task.TaskOperationDescriptor
import com.userlabs.codestudio.tooling.events.task.TaskOperationResult
import com.userlabs.codestudio.tooling.events.task.TaskProgressEvent
import com.userlabs.codestudio.tooling.events.task.TaskSkippedResult
import com.userlabs.codestudio.tooling.events.task.TaskStartEvent
import com.userlabs.codestudio.tooling.events.task.TaskSuccessResult
import com.userlabs.codestudio.tooling.events.test.TestFinishEvent
import com.userlabs.codestudio.tooling.events.test.TestOperationDescriptor
import com.userlabs.codestudio.tooling.events.test.TestOperationResult
import com.userlabs.codestudio.tooling.events.test.TestProgressEvent
import com.userlabs.codestudio.tooling.events.test.TestStartEvent
import com.userlabs.codestudio.tooling.events.transform.TransformFinishEvent
import com.userlabs.codestudio.tooling.events.transform.TransformOperationDescriptor
import com.userlabs.codestudio.tooling.events.transform.TransformProgressEvent
import com.userlabs.codestudio.tooling.events.transform.TransformStartEvent
import com.userlabs.codestudio.tooling.events.work.WorkItemFinishEvent
import com.userlabs.codestudio.tooling.events.work.WorkItemOperationDescriptor
import com.userlabs.codestudio.tooling.events.work.WorkItemOperationResult
import com.userlabs.codestudio.tooling.events.work.WorkItemProgressEvent
import com.userlabs.codestudio.tooling.events.work.WorkItemStartEvent
import org.eclipse.lsp4j.jsonrpc.Launcher
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Executors

/**
 * Utility class for launching [IToolingApiClient] and [IToolingApiServer].
 *
 * @author Akash Yadav
 */
object ToolingApiLauncher {

  fun <T> createIOLauncher(
    local: Any?, remote: Class<T>?, `in`: InputStream?, out: OutputStream?): Launcher<T> {
    return Launcher.Builder<T>()
      .setInput(`in`)
      .setOutput(out)
      .setLocalService(local)
      .setRemoteInterface(remote)
      .configureGson { configureGson(it) }
      .create()
  }

  @JvmStatic
  fun configureGson(builder: GsonBuilder) {
    builder.registerTypeAdapter(File::class.java, FileTypeAdapter())

    // some methods return BasicProjectMetadata while some return ProjectMetadata
    // so we need to register type adapter for both of them
    builder.runtimeTypeAdapter(
      BasicProjectMetadata::class.java,
      ProjectMetadata::class.java,
      AndroidProjectMetadata::class.java,
      JavaProjectMetadata::class.java
    )
    builder.runtimeTypeAdapter(
      ProjectMetadata::class.java,
      AndroidProjectMetadata::class.java,
      JavaProjectMetadata::class.java
    )
    builder.runtimeTypeAdapter(
      BasicAndroidVariantMetadata::class.java,
      AndroidVariantMetadata::class.java
    )
    builder.runtimeTypeAdapter(
      JavaModuleDependency::class.java,
      JavaModuleExternalDependency::class.java,
      JavaModuleProjectDependency::class.java
    )
    builder.runtimeTypeAdapter(
      IJavaCompilerSettings::class.java,
      DefaultJavaCompileOptions::class.java,
      JavaModuleCompilerSettings::class.java
    )
    builder.runtimeTypeAdapter(
      Launchable::class.java,
      GradleTask::class.java
    )
    builder.runtimeTypeAdapter(
      ProgressEvent::class.java,
      ProjectConfigurationProgressEvent::class.java,
      ProjectConfigurationStartEvent::class.java,
      ProjectConfigurationFinishEvent::class.java,

      FileDownloadProgressEvent::class.java,
      FileDownloadStartEvent::class.java,
      FileDownloadFinishEvent::class.java,

      TaskProgressEvent::class.java,
      TaskStartEvent::class.java,
      TaskFinishEvent::class.java,

      TestProgressEvent::class.java,
      TestStartEvent::class.java,
      TestFinishEvent::class.java,

      TransformProgressEvent::class.java,
      TransformStartEvent::class.java,
      TransformFinishEvent::class.java,

      WorkItemProgressEvent::class.java,
      WorkItemStartEvent::class.java,
      WorkItemFinishEvent::class.java,

      DefaultProgressEvent::class.java,
      DefaultStartEvent::class.java,
      DefaultFinishEvent::class.java,

      StatusEvent::class.java
    )
    builder.runtimeTypeAdapter(
      OperationDescriptor::class.java,
      ProjectConfigurationOperationDescriptor::class.java,
      FileDownloadOperationDescriptor::class.java,
      TaskOperationDescriptor::class.java,
      TestOperationDescriptor::class.java,
      TransformOperationDescriptor::class.java,
      WorkItemOperationDescriptor::class.java,
      DefaultOperationDescriptor::class.java
    )
    builder.runtimeTypeAdapter(
      OperationResult::class.java,
      ProjectConfigurationOperationResult::class.java,
      FileDownloadResult::class.java,
      TaskOperationResult::class.java,
      TestOperationResult::class.java,
      WorkItemOperationResult::class.java,
      DefaultOperationResult::class.java
    )
    builder.runtimeTypeAdapter(
      TaskOperationResult::class.java,
      TaskFailureResult::class.java,
      TaskSkippedResult::class.java,
      TaskExecutionResult::class.java,
      TaskSuccessResult::class.java
    )
  }

  private fun <T> GsonBuilder.runtimeTypeAdapter(baseClass: Class<T>,
    vararg subtypes: Class<out T>) {
    registerTypeAdapterFactory(
      RuntimeTypeAdapterFactory.of(baseClass, "gsonType", true)
        .registerSubtype(baseClass, baseClass.name).also { factory ->
          subtypes.forEach { subtype ->
            factory.registerSubtype(subtype, subtype.name)
          }
        }
    )
  }

  fun newClientLauncher(
    client: IToolingApiClient, `in`: InputStream?, out: OutputStream?): Launcher<Any> {
    return newIoLauncher(arrayOf(client), arrayOf(
      IToolingApiServer::class.java, IProject::class.java), `in`, out)
  }

  fun newIoLauncher(
    locals: Array<Any>, remotes: Array<Class<*>?>, `in`: InputStream?,
    out: OutputStream?): Launcher<Any> {
    return Launcher.Builder<Any>()
      .setInput(`in`)
      .setOutput(out)
      .setExecutorService(Executors.newCachedThreadPool())
      .setLocalServices(listOf(*locals))
      .setRemoteInterfaces(listOf(*remotes))
      .configureGson { configureGson(it) }
      .setClassLoader(locals[0].javaClass.classLoader)
      .create()
  }

  @JvmStatic
  fun newServerLauncher(
    server: IToolingApiServer, project: IProject, `in`: InputStream?,
    out: OutputStream?): Launcher<Any> {
    return newIoLauncher(arrayOf(server, project), arrayOf(
      IToolingApiClient::class.java), `in`, out)
  }
}