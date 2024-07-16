package de.halfbit.componental.android

import android.os.Bundle
import androidx.savedstate.SavedStateRegistry
import de.halfbit.componental.restorator.Restorator

internal class AndroidRestorator(
    savedStateRegistry: SavedStateRegistry,
) : Restorator {

    private val delegate: Restorator =
        Restorator(
            if (savedStateRegistry.isRestored) {
                savedStateRegistry
                    .consumeRestoredStateForKey(KEY_COMPONENTAL)
                    ?.getByteArray(KEY_COMPONENTAL)
            } else {
                null
            }
        )

    init {
        savedStateRegistry
            .registerSavedStateProvider(KEY_COMPONENTAL) {
                val bytes = storeAll()
                Bundle().apply {
                    putByteArray(KEY_COMPONENTAL, bytes)
                }
            }
    }

    override fun restoreRoute(): ByteArray? {
        val bytes = delegate.restoreRoute()
        return bytes
    }

    override fun storeRoute(block: () -> ByteArray?) {
        delegate.storeRoute(block)
    }

    override fun storeAll(): ByteArray {
        return delegate.storeAll()
    }
}

private const val KEY_COMPONENTAL = "componental-state"