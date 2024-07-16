package de.halfbit.componental.lifecycle

import de.halfbit.componental.lifecycle.Lifecycle.State

public interface Lifecycle {

    public val state: State

    public fun subscribe(subscriber: Subscriber)
    public fun unsubscribe(subscriber: Subscriber)

    public enum class State {
        Destroyed,
        Initial,
        Created,
        Started,
        Resumed
    }

    public sealed interface Subscriber {

        public interface States : Subscriber {
            public fun onState(state: State)
        }

        public interface Callbacks : Subscriber {
            public fun onCreate() {}
            public fun onStart() {}
            public fun onResume() {}
            public fun onPause() {}
            public fun onStop() {}
            public fun onDestroy() {}
        }
    }
}

internal fun State.stateUp(): State? =
    when (this) {
        State.Destroyed -> null
        State.Initial -> State.Created
        State.Created -> State.Started
        State.Started -> State.Resumed
        State.Resumed -> null
    }

internal fun State.stateDown(): State? =
    when (this) {
        State.Destroyed -> null
        State.Initial -> null
        State.Created -> State.Destroyed
        State.Started -> State.Created
        State.Resumed -> State.Started
    }

internal fun State.sequenceTo(toState: State): Sequence<State> =
    when {
        this == State.Destroyed -> emptySequence()
        this == toState -> emptySequence()
        this < toState -> generateSequence(stateUp()) { next ->
            if (next < toState) next.stateUp() else null
        }

        this > toState -> generateSequence(stateDown()) { next ->
            if (next > toState) next.stateDown() else null
        }

        else -> emptySequence()
    }
