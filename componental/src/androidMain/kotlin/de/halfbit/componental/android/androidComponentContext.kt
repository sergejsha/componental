package de.halfbit.componental.android

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.savedstate.SavedStateRegistryOwner
import de.halfbit.componental.ComponentContext
import de.halfbit.componental.coroutines.ComponentalDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

public fun <T> T.androidRootComponentContext(
    contextCoroutineDispatcher: CoroutineDispatcher = ComponentalDispatchers.contextCoroutineDispatcher,
): ComponentContext where
    T : LifecycleOwner,
    T : SavedStateRegistryOwner {

    val parentJob = lifecycleScope.coroutineContext[Job]
    val contextScope = SupervisorJob(parentJob) +
        CoroutineName("Top") +
        contextCoroutineDispatcher

    return ComponentContext.create(
        lifecycleCoroutineScope = CoroutineScope(contextScope),
        lifecycle = AndroidViewLifecycle(lifecycle),
        restorator = AndroidRestorator(savedStateRegistry),
    )
}
