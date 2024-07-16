package de.halfbit.componental.router

import kotlinx.serialization.Serializable

@Serializable
public class RestorableRoute<Id : Any>(
    public val id: Id,
    private var childState: ByteArray?,
) {
    public fun consumeChildState(): ByteArray? =
        childState.also { childState = null }
}