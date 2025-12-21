package br.com.nukes.testeworkmanager.ui

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import br.com.nukes.testeworkmanager.workers.configuration.pipeline.PipelineController

class PipelineLifecycleObserver(
    private val pipelineController: PipelineController
) : DefaultLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        Log.d("LIFECYCLE", "onCreate")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d("LIFECYCLE", "UI visível")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d("LIFECYCLE", "UI invisível")
    }
}