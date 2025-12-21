package br.com.nukes.testeworkmanager.workers.configuration.pipeline

import br.com.nukes.testeworkmanager.domain.models.AppModel
import kotlinx.coroutines.flow.MutableStateFlow

object PipelineState {
    val status = MutableStateFlow<PipelineStatus>(PipelineStatus.Idle)
}

sealed class PipelineStatus {
    object Idle: PipelineStatus()
    object Started: PipelineStatus()

    data class StepRunning(val step: PipelineStep) : PipelineStatus()
    data class Progress(val step: PipelineStep, val app: AppModel, val progress: Int): PipelineStatus()
    object Finished: PipelineStatus()
    data class Error(
        val step: PipelineStep,
        val error: String? = null,
        val map: Map<String, Any?>? = null
    ): PipelineStatus()
}

enum class PipelineStep {
    INITIAL,
    FETCH_APP_USAGE_REPORT,
    FETCH_INSTALLED_APPS,
    SEND_DATA_TO_SERVER,
    PROCESS_APPS,
    DOWNLOAD_STARTED,
    DOWNLOADING,
    DOWNLOAD,
    DOWNLOADED,
    INSTALL_APP,
    INSTALL_APP_STARTED,
    INSTALL_APP_FINISHED,
    UNINSTALL_APP,
    UNINSTALL_APP_STARTED,
    UNINSTALL_APP_FINISHED,
    PROCESS_BUILD,
    INSTALL_BUILD,
    INSTALL_BUILD_STARTED,
    INSTALL_BUILD_FINISHED,
    SEND_NOTIFY
}