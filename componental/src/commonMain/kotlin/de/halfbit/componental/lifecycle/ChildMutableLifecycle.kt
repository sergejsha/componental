package de.halfbit.componental.lifecycle

import de.halfbit.componental.lifecycle.Lifecycle.State
import de.halfbit.componental.lifecycle.Lifecycle.Subscriber

internal fun Lifecycle.createMutableChildLifecycle(): MutableLifecycle =
    MutableChildLifecycle(
        parent = this,
    )

private class MutableChildLifecycle(
    parent: Lifecycle,
) : MutableLifecycle {

    private val child = MutableLifecycle.create()
    private val merged = MutableLifecycle.create()

    init {
        parent.subscribe(
            object : Subscriber.States {
                override fun onState(state: State) {
                    merged.moveToState(minOf(state, child.state))
                    if (state == State.Destroyed) {
                        parent.unsubscribe(this)
                    }
                }
            }
        )
        child.subscribe(
            object : Subscriber.States {
                override fun onState(state: State) {
                    merged.moveToState(minOf(parent.state, state))
                }
            }
        )
    }

    override val state: State
        get() = merged.state

    override fun subscribe(subscriber: Subscriber) {
        merged.subscribe(subscriber)
    }

    override fun unsubscribe(subscriber: Subscriber) {
        merged.unsubscribe(subscriber)
    }

    override fun moveToState(newState: State) {
        child.moveToState(newState)
    }
}
