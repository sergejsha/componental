package de.halfbit.componental.android

import androidx.lifecycle.Lifecycle as AndroidLifecycle
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import de.halfbit.componental.lifecycle.Lifecycle.State
import de.halfbit.componental.lifecycle.MutableLifecycle
import de.halfbit.componental.lifecycle.Lifecycle

internal class AndroidViewLifecycle(
    private val androidLifecycle: AndroidLifecycle,
) : Lifecycle {
    private val actual = MutableLifecycle.create()

    init {
        androidLifecycle.addObserver(
            object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Event) {
                    when (event) {
                        Event.ON_CREATE -> actual.moveToState(State.Created)
                        Event.ON_START -> actual.moveToState(State.Started)
                        Event.ON_RESUME -> actual.moveToState(State.Resumed)
                        Event.ON_PAUSE -> actual.moveToState(State.Started)
                        Event.ON_STOP -> actual.moveToState(State.Created)
                        Event.ON_DESTROY -> {
                            actual.moveToState(State.Destroyed)
                            androidLifecycle.removeObserver(this)
                        }

                        Event.ON_ANY -> Unit
                    }
                }
            }
        )
    }

    override val state: State
        get() = actual.state

    override fun subscribe(subscriber: Lifecycle.Subscriber) {
        actual.subscribe(subscriber)
    }

    override fun unsubscribe(subscriber: Lifecycle.Subscriber) {
        actual.unsubscribe(subscriber)
    }
}
