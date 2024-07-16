package de.halfbit.componental.interop

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

public class StateFlowValue<out T : Any>(
    private val stateFlow: StateFlow<T>,
) {
    public val value: T get() = stateFlow.value
    private var observer: StateFlowObserver? = null

    public fun observe(onValue: (T) -> Unit) {
        check(observer == null) { "Observer is already installed" }
        StateFlowObserver(onValue).apply {
            observer = this
            observe()
        }
    }

    public fun cancel() {
        observer?.let {
            observer = null
            it.cancel()
        }
    }

    private inner class StateFlowObserver(
        private val onValue: (T) -> Unit,
    ) {
        private val job = Job()

        fun observe() {
            stateFlow
                .onEach { onValue(it) }
                .launchIn(CoroutineScope(job + Dispatchers.Main))
        }

        fun cancel() {
            job.cancel()
        }
    }
}
