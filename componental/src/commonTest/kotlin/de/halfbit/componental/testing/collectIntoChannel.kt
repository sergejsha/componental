/** Copyright 2024 Halfbit GmbH, Sergej Shafarenka */
package de.halfbit.componental.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

fun <T> StateFlow<T>.collectIntoChannel(scope: CoroutineScope): Channel<T> {
    val channel = Channel<T>()
    scope.launch {
        collect {
            channel.send(it)
        }
    }
    return channel
}

suspend fun <T> Channel<T>.ignore() {
    receive()
}
