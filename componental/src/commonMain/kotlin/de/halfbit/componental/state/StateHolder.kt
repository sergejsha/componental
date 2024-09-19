package de.halfbit.componental.state

import de.halfbit.componental.ComponentContext
import de.halfbit.componental.coroutines.ComponentalDispatchers
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlin.reflect.KClass
import kotlin.reflect.cast

public interface StateHolder<out S : Any> {
    public val state: StateFlow<S>
}

public abstract class MutableStateHolder<S : Any> : StateHolder<S> {
    public abstract fun updateState(reducer: (S) -> S)

    public abstract fun <W : S> updateIf(stateType: KClass<W>, reducer: (W) -> S)
    public inline fun <reified W : S> updateIf(noinline reducer: (W) -> S) {
        updateIf(W::class, reducer)
    }

    public abstract suspend fun <W : S> updateWhen(stateType: KClass<W>, reducer: (W) -> S)
    public suspend inline fun <reified W : S> updateWhen(noinline reducer: (W) -> S) {
        updateWhen(W::class, reducer)
    }

    public abstract fun <S : Any> updateUiWhen(stateType: KClass<S>, block: (S) -> Unit)
    public inline fun <reified S : Any> updateUiWhen(noinline block: (S) -> Unit) {
        updateUiWhen(S::class, block)
    }

    public abstract fun <S : Any> launchWithStateWhen(stateType: KClass<S>, block: suspend (S) -> Unit)
    public inline fun <reified S : Any> launchWithStateWhen(noinline block: suspend (S) -> Unit) {
        launchWithStateWhen(S::class, block)
    }

    public abstract fun <S : Any> withStateWhen(stateType: KClass<S>, block: (S) -> Unit)
    public inline fun <reified S : Any> withStateWhen(noinline block: (S) -> Unit) {
        withStateWhen(S::class, block)
    }
}

public fun <S : Any> mutableStateHolder(
    initialState: S,
    coroutineScope: CoroutineScope,
): MutableStateHolder<S> =
    DefaultMutableStateHolder(
        initialState,
        coroutineScope,
    )

public fun <S : Any> ComponentContext.mutableStateHolder(
    initialState: S,
): MutableStateHolder<S> =
    mutableStateHolder(
        initialState,
        coroutineScope,
    )

private class DefaultMutableStateHolder<S : Any>(
    private val initialState: S,
    private val coroutineScope: CoroutineScope,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val mainDispatcher: CoroutineDispatcher = ComponentalDispatchers.optimalMain,
) : MutableStateHolder<S>() {

    private val channel = Channel<(S) -> S>(capacity = 64)

    override val state: StateFlow<S> =
        flow {
            emit(initialState)
            var state = initialState
            while (true) {
                val reducer = channel.receive()
                state = reducer.invoke(state)
                emit(state)
            }
        }.onCompletion {
            channel.cancel()
        }.stateIn(
            coroutineScope,
            SharingStarted.Eagerly,
            initialState,
        )

    override fun updateState(reducer: (S) -> S) {
        val result = channel.trySend(reducer)
        if (result.isFailure && !result.isClosed) {
            throw IllegalStateException(
                "Failed to schedule reducer to a not closed channel"
            )
        }
    }

    override fun <W : S> updateIf(stateType: KClass<W>, reducer: (W) -> S) {
        updateState { state ->
            if (stateType.isInstance(state)) {
                reducer(stateType.cast(state))
            } else state
        }
    }

    override suspend fun <W : S> updateWhen(stateType: KClass<W>, reducer: (W) -> S) {
        try {
            withTimeout(5_000) {
                state.first { stateType.isInstance(it) }
                updateIf(stateType, reducer)
            }
        } catch (e: TimeoutCancellationException) {
            println("State $stateType not reached")
        }
    }

    override fun <S : Any> updateUiWhen(stateType: KClass<S>, block: (S) -> Unit) {
        updateState { state ->
            if (stateType.isInstance(state)) {
                coroutineScope.launch(mainDispatcher) {
                    block(stateType.cast(state))
                }
            }
            state
        }
    }

    override fun <S : Any> launchWithStateWhen(stateType: KClass<S>, block: suspend (S) -> Unit) {
        updateState { state ->
            if (stateType.isInstance(state)) {
                coroutineScope.launch(defaultDispatcher) {
                    block(stateType.cast(state))
                }
            }
            state
        }
    }

    override fun <S : Any> withStateWhen(stateType: KClass<S>, block: (S) -> Unit) {
        updateState { state ->
            if (stateType.isInstance(state)) {
                block(stateType.cast(state))
            }
            state
        }
    }
}
