package de.halfbit.componental.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

public object ComponentalDispatchers {

    public val optimalMain: CoroutineDispatcher by lazy {
        try {
            Dispatchers.Main.immediate
        } catch (e: UnsupportedOperationException) {
            Dispatchers.Main
        }
    }

    public val contextCoroutineDispatcher: CoroutineDispatcher
        get() = Dispatchers.Default
}
