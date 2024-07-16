package de.halfbit.componental

import de.halfbit.componental.coroutines.CoroutineScopeOwner
import de.halfbit.componental.lifecycle.Lifecycle
import de.halfbit.componental.lifecycle.LifecycleOwner
import de.halfbit.componental.restorator.Restorator
import de.halfbit.componental.restorator.RestoratorOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

public interface ComponentContext : LifecycleOwner, CoroutineScopeOwner, RestoratorOwner {

    public companion object {
        public fun create(
            lifecycle: Lifecycle,
            lifecycleCoroutineScope: CoroutineScope,
            restorator: Restorator,
        ): ComponentContext =
            DefaultComponentContext(
                lifecycle = lifecycle,
                coroutineScope = lifecycleCoroutineScope,
                restorator = restorator,
            )
    }
}

internal fun ComponentContext.createChildContext(
    childLifecycle: Lifecycle,
    childCoroutineScope: CoroutineScope,
    restorator: Restorator,
): ComponentContext = ComponentContext.create(
    lifecycle = childLifecycle,
    lifecycleCoroutineScope = childCoroutineScope,
    restorator = restorator,
).doOnDestroy {
    coroutineScope.launch {
        if (childCoroutineScope.isActive) {
            childCoroutineScope.cancel()
        }
    }
}

private class DefaultComponentContext(
    override val lifecycle: Lifecycle,
    override val coroutineScope: CoroutineScope,
    override val restorator: Restorator,
) : ComponentContext

internal inline fun ComponentContext.doOnDestroy(crossinline callback: () -> Unit): ComponentContext {
    lifecycle.subscribe(
        object : Lifecycle.Subscriber.Callbacks {
            override fun onDestroy() {
                callback()
                lifecycle.unsubscribe(this)
            }
        }
    )
    return this
}

public fun ComponentContext.relaunchOnStart(
    block: suspend CoroutineScope.() -> Unit
): ComponentContext {
    var job: Job? = null
    lifecycle.subscribe(
        object : Lifecycle.Subscriber.Callbacks {
            override fun onStart() {
                job?.cancel()
                job = coroutineScope.launch(block = block)
            }
        }
    )
    return this
}

public fun ComponentContext.launchOnStart(
    block: suspend CoroutineScope.() -> Unit
): ComponentContext {
    lifecycle.subscribe(
        object : Lifecycle.Subscriber.Callbacks {
            override fun onStart() {
                lifecycle.unsubscribe(this)
                coroutineScope.launch(block = block)
            }
        }
    )
    return this
}

public fun ComponentContext.launchOnCreate(
    block: suspend CoroutineScope.() -> Unit
): ComponentContext {
    lifecycle.subscribe(
        object : Lifecycle.Subscriber.Callbacks {
            override fun onCreate() {
                lifecycle.unsubscribe(this)
                coroutineScope.launch(block = block)
            }
        }
    )
    return this
}

public class PendingResult<V : Any> {
    private var result: V? = null
    public fun set(value: V) {
        result = value
    }

    public fun dispatch(consumer: (V) -> Unit) {
        result?.let {
            consumer(it)
            result = null
        }
    }
}