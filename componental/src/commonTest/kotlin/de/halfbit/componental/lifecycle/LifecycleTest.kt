/** Copyright 2024 Halfbit GmbH, Sergej Shafarenka */
package de.halfbit.componental.lifecycle

import kotlin.test.Test
import kotlin.test.assertEquals

class LifecycleTest {

    @Test
    fun initial_defaultState() {
        val lifecycle = MutableLifecycle.create()
        val actual = lifecycle.state
        assertEquals(Lifecycle.State.Initial, actual)
    }

    @Test
    fun moveToResumed_immediateTransition() {
        val lifecycle = MutableLifecycle.create()
        lifecycle.moveToState(Lifecycle.State.Resumed)

        val actual = lifecycle.state
        assertEquals(Lifecycle.State.Resumed, actual)
    }

    @Test
    fun moveToResumed_thenToDestroyed_immediateTransition() {
        val lifecycle = MutableLifecycle.create()
        lifecycle.moveToState(Lifecycle.State.Resumed)
        lifecycle.moveToState(Lifecycle.State.Destroyed)

        val actual = lifecycle.state
        assertEquals(Lifecycle.State.Destroyed, actual)
    }

    @Test
    fun moveToResumed_callbacks() {
        // given
        val lifecycle = MutableLifecycle.create()
        val actual = mutableListOf<Lifecycle.State>()
        lifecycle.subscribe(
            object : Lifecycle.Subscriber.States {
                override fun onState(state: Lifecycle.State) {
                    actual += state
                }
            }
        )

        // when
        lifecycle.moveToState(Lifecycle.State.Resumed)

        // then
        val expected = listOf(Lifecycle.State.Created, Lifecycle.State.Started, Lifecycle.State.Resumed)
        assertEquals(expected, actual)
    }

    @Test
    fun moveToResumed_thenToDestroyed_callbacks() {
        // given
        val lifecycle = MutableLifecycle.create()
        val actual = mutableListOf<Lifecycle.State>()
        lifecycle.subscribe(
            object : Lifecycle.Subscriber.States {
                override fun onState(state: Lifecycle.State) {
                    actual += state
                }
            }
        )

        // when
        lifecycle.moveToState(Lifecycle.State.Resumed)
        lifecycle.moveToState(Lifecycle.State.Destroyed)

        // then
        val expected = listOf(
            Lifecycle.State.Created, Lifecycle.State.Started, Lifecycle.State.Resumed,
            Lifecycle.State.Started, Lifecycle.State.Created, Lifecycle.State.Destroyed,
        )
        assertEquals(expected, actual)
    }
}