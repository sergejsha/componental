/** Copyright 2024 Halfbit GmbH, Sergej Shafarenka */
package de.halfbit.componental.router

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

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
        if (channel.trySend(transform).isFailure) {
            throw IllegalStateException("Failed to schedule transform")
        }
    }
}
