package de.halfbit.componental.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

public interface CoroutineScopeOwner {
    public val coroutineScope: CoroutineScope

    public companion object {
        public fun create(
            coroutineScope: CoroutineScope,
        ): CoroutineScopeOwner =
            object : CoroutineScopeOwner {
                override val coroutineScope: CoroutineScope
                    get() = coroutineScope
            }
    }
}

public fun CoroutineScope.createChildCoroutineScope(name: String): CoroutineScope =
    CoroutineScope(
        context = SupervisorJob(coroutineContext[Job]) +
            CoroutineName("Child: $name") +
            ComponentalDispatchers.contextCoroutineDispatcher
    )
