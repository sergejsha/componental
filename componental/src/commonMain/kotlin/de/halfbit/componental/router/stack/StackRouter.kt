/** Copyright 2024 Halfbit GmbH, Sergej Shafarenka */
package de.halfbit.componental.router.stack

import de.halfbit.componental.ComponentContext
import de.halfbit.componental.coroutines.createChildCoroutineScope
import de.halfbit.componental.createChildContext
import de.halfbit.componental.lifecycle.Lifecycle.State
import de.halfbit.componental.lifecycle.createMutableChildLifecycle
import de.halfbit.componental.restorator.Restorator
import de.halfbit.componental.router.RestorableRoute
import de.halfbit.componental.router.RouteNode
import de.halfbit.componental.router.Router
import de.halfbit.componental.router.RuntimeRouteNode
import de.halfbit.componental.router.stack.StackRouter.Event
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.protobuf.ProtoBuf

public data class Stack<out I : Any, out C : Any>(
    val active: RouteNode<I, C>,
    val inactive: List<RouteNode<I, C>>
)

public class StackRouter<I : Any> : Router<Event<I>>() {
    public class Event<I : Any>(
        public val transform: (all: List<I>) -> List<I>,
    )
}

public fun <I : Any> StackRouter<I>.push(id: I) {
    route(
        event = Event { ids -> ids + id }
    )
}

public fun <K : Any> StackRouter<K>.pop(onLastItem: () -> Unit) {
    route(
        event = Event { ids ->
            if (ids.size > 1) {
                ids.subList(0, ids.size - 1)
            } else {
                onLastItem()
                ids
            }
        }
    )
}

public fun <Id : Any, Child : Any> ComponentContext.childStack(
    router: StackRouter<Id>,
    initial: List<Id>,
    serializer: () -> KSerializer<Id>,
    childFactory: (id: Id, context: ComponentContext) -> Child,
): StateFlow<Stack<Id, Child>> {
    val runtimeNodes = mutableMapOf<Id, RuntimeRouteNode<Id, Child>>()
    val restoredRoute: Map<Id, RestorableRoute<Id>>? =
        restorator.restoreRoute()?.let {
            ProtoBuf.decodeFromByteArray(
                ListSerializer(RestorableRoute.serializer(serializer())), it
            ).associateBy { route -> route.id }
        }

    fun createRuntimeRouteNode(id: Id): RuntimeRouteNode<Id, Child> {
        val tag = "id:${id::class.simpleName.toString()}"
        val childLifecycle = lifecycle.createMutableChildLifecycle()
        val restoredChildState = restoredRoute?.get(id)?.consumeChildState()
        val context = createChildContext(
            childLifecycle = childLifecycle,
            childCoroutineScope = coroutineScope.createChildCoroutineScope(tag),
            restorator = Restorator(restoredChildState),
        )
        val node = RouteNode(id, childFactory(id, context))
        return RuntimeRouteNode(
            node = node,
            lifecycle = childLifecycle,
            restorator = context.restorator,
        ).also { newNode ->
            runtimeNodes[id] = newNode
            childLifecycle.moveToState(State.Resumed)
        }
    }

    fun Collection<Id>.asRuntimeStack(): Stack<Id, Child> {

        val stackNodes = map { id ->
            runtimeNodes[id] ?: createRuntimeRouteNode(id)
        }

        stackNodes.reversed().forEachIndexed { index, node ->
            val targetState = if (index == 0) State.Resumed else State.Created
            node.lifecycle.moveToState(targetState)
        }

        val destroyedIds = runtimeNodes.keys - toSet()
        destroyedIds.forEach { key ->
            runtimeNodes[key]?.lifecycle?.moveToState(State.Destroyed)
        }

        runtimeNodes.keys.removeAll(destroyedIds)
        return stackNodes.map { it.node }.toStack()
    }

    var ids = restoredRoute?.keys?.toList() ?: initial
    restorator.storeRoute {
        ProtoBuf.encodeToByteArray(
            ListSerializer(RestorableRoute.serializer(serializer())),
            ids.map { id ->
                RestorableRoute(
                    id = id,
                    childState = runtimeNodes[id]?.restorator?.storeAll()
                )
            }
        )
    }

    return flow {
        emit(ids.asRuntimeStack())
        router.events.collect { event ->
            ids = event.transform(ids)
            emit(ids.asRuntimeStack())
        }
    }.stateIn(
        coroutineScope,
        SharingStarted.Eagerly,
        initial.asRuntimeStack(),
    )
}

private fun <I : Any, C : Any> List<RouteNode<I, C>>.toStack(): Stack<I, C> {
    check(isNotEmpty()) { "List used as a stack must have at least one entry" }
    return Stack(
        active = last(),
        inactive = if (size == 1) emptyList() else subList(0, lastIndex),
    )
}
