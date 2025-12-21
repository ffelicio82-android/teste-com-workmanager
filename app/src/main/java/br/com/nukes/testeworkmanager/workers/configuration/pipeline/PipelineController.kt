package br.com.nukes.testeworkmanager.workers.configuration.pipeline

import br.com.nukes.testeworkmanager.domain.models.AppModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PipelineController {
    private val _state = MutableStateFlow<PipelineStatus>(PipelineStatus.Idle)

    val state: StateFlow<PipelineStatus> = _state

    suspend fun start() {
        _state.emit(PipelineStatus.Started)
    }

    suspend fun stepRunning(step: PipelineStep) {
        _state.emit(PipelineStatus.StepRunning(step))
    }

    suspend fun stepProgress(step: PipelineStep, app: AppModel, progress: Int) {
        _state.emit(PipelineStatus.Progress(step, app, progress))
    }

    suspend fun error(step: PipelineStep, message: String, map: Map<String, Any?>? = null) {
        _state.emit(PipelineStatus.Error(step, message, map))
    }

    suspend fun finish() {
        _state.emit(PipelineStatus.Finished)
    }

    suspend fun reset() {
        _state.emit(PipelineStatus.Idle)
    }
}