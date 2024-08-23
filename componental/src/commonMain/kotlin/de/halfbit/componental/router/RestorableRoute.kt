package de.halfbit.componental.router

import kotlinx.serialization.Serializable

@Serializable
public class RestorableRoute<R : Any>(
    public val route: R,
    private var childState: ByteArray?,
) {
    public fun consumeChildState(): ByteArray? =
        childState.also { childState = null }
}