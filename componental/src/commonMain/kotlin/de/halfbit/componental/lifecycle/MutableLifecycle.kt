package de.halfbit.componental.lifecycle

import de.halfbit.componental.lifecycle.Lifecycle.State
import de.halfbit.componental.lifecycle.Lifecycle.Subscriber
import de.halfbit.componental.lifecycle.Lifecycle.Subscriber.Callbacks
import de.halfbit.componental.lifecycle.Lifecycle.Subscriber.States
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

public interface MutableLifecycle : Lifecycle {
    public fun moveToState(newState: State)

    public companion object {
        public fun create(): MutableLifecycle =
            DefaultMutableLifecycle()
    }
}

private class DefaultMutableLifecycle : MutableLifecycle {

    private val lock = SynchronizedObject()
    private var subscribers: Set<Subscriber> = emptySet()
    override var state: State = State.Initial

    override fun subscribe(subscriber: Subscriber) {
        moveToStateOrNull(State.Initial, state) { state, movingUp ->
            notifySubscriber(state, movingUp, subscriber)
        }
        if (state != State.Destroyed) {
            synchronized(lock) {
                subscribers += subscriber
            }
        }
    }

    override fun unsubscribe(subscriber: Subscriber) {
        synchronized(lock) {
            this.subscribers -= subscriber
        }
    }

    override fun moveToState(newState: State) {
        moveToStateOrNull(state, newState) { nextState, movingUp ->
            val subscribers = synchronized(lock) {
                if (movingUp) subscribers else subscribers.reversed()
            }
            subscribers.forEach { subscriber ->
                notifySubscriber(nextState, movingUp, subscriber)
            }
        }?.let { finalState ->
            state = finalState
            if (state == State.Destroyed) {
                synchronized(lock) {
                    subscribers = emptySet()
                }
            }
        }
    }
}

private inline fun moveToStateOrNull(
    fromState: State,
    toState: State,
    notifyNextState: (State, Boolean) -> Unit,
): State? {
    var finalState: State? = null
    fromState
        .sequenceTo(toState)
        .forEach { next ->
            finalState = next
            val movingUp = toState > fromState
            notifyNextState(next, movingUp)
        }
    return finalState
}

private fun notifySubscriber(
    state: State,
    movingUp: Boolean,
    subscriber: Subscriber,
) {
    when (subscriber) {
        is Callbacks -> when (state) {
            State.Created -> if (movingUp) subscriber.onCreate() else subscriber.onStop()
            State.Started -> if (movingUp) subscriber.onStart() else subscriber.onPause()
            State.Resumed -> subscriber.onResume()
            State.Destroyed -> subscriber.onDestroy()
            else -> Unit
        }

        is States -> {
            subscriber.onState(state)
        }
    }
}
