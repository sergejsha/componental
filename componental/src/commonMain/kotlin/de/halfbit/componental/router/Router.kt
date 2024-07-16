package de.halfbit.componental.router

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

public abstract class Router<Event : Any> {
    private val mutableEventFlow =
        MutableSharedFlow<Event>(extraBufferCapacity = 32)

    public val events: Flow<Event>
        get() = mutableEventFlow.asSharedFlow()

    public fun route(event: Event) {
        check(mutableEventFlow.tryEmit(event)) { "Cannot process event: $event" }
    }
}
