/** Copyright 2024 Halfbit GmbH, Sergej Shafarenka */
package de.halfbit.componental.router

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion

public abstract class Router<Transform : Any> {
    private val channel = Channel<Transform>(capacity = 64)

    public val transformers: Flow<Transform> =
        flow {
            while (true)
                emit(channel.receive())
        }.onCompletion {
            channel.close()
        }

    public fun route(transform: Transform) {
        val result = channel.trySend(transform)
        if (result.isFailure && !result.isClosed) {
            throw IllegalStateException(
                "Failed to schedule transform to a not closed channel"
            )
        }
    }
}
