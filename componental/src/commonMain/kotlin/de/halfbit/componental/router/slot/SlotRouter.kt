/** Copyright 2024 Halfbit GmbH, Sergej Shafarenka */
package de.halfbit.componental.router.slot

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
import de.halfbit.componental.router.slot.SlotRouter.Event
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.KSerializer
import kotlinx.serialization.protobuf.ProtoBuf

public data class Slot<out I : Any, out C : Any>(
    val active: RouteNode<I, C>? = null,
)

public class SlotRouter<I : Any> : Router<Event<I>>() {
    public class Event<I : Any>(
        public val transform: (active: I?) -> I?,
    )
}

public fun <I : Any> SlotRouter<I>.set(active: I?) {
    route(
        event = Event { _ -> active }
    )
}

public fun <Id : Any, Child : Any> ComponentContext.childSlot(
    router: SlotRouter<Id>,
    initial: Id? = null,
    serializer: () -> KSerializer<Id>,
    childFactory: (id: Id, context: ComponentContext) -> Child,
): StateFlow<Slot<Id, Child>> {
    var runtimeNode: RuntimeRouteNode<Id, Child>? = null

    val restoredRoute: RestorableRoute<Id>? =
        restorator.restoreRoute()?.let {
            ProtoBuf.decodeFromByteArray(
                RestorableRoute.serializer(serializer()), it
            )
        }

    fun createRuntimeRouteNode(id: Id): RuntimeRouteNode<Id, Child> {
        val tag = "id:${id::class.simpleName.toString()}"
        val childLifecycle = lifecycle.createMutableChildLifecycle()
        val restoredChildState = if (restoredRoute?.id == id) {
            restoredRoute.consumeChildState()
        } else null
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
            runtimeNode = newNode
            childLifecycle.moveToState(State.Resumed)
        }
    }

    fun Id?.asRuntimeSlot(): Slot<Id, Child> {
        val oldNode = runtimeNode
        val newNode = when (this) {
            null -> {
                runtimeNode = null
                null
            }

            else -> when {
                oldNode == null || this != oldNode.node.id -> {
                    createRuntimeRouteNode(this)
                }

                else -> oldNode
            }
        }

        if (oldNode != null && oldNode != newNode) {
            oldNode.lifecycle.moveToState(State.Destroyed)
        }

        return Slot(active = newNode?.node)
    }

    var activeId = restoredRoute?.id ?: initial
    restorator.storeRoute {
        val id = activeId ?: return@storeRoute null
        ProtoBuf.encodeToByteArray(
            RestorableRoute.serializer(serializer()),
            RestorableRoute(
                id = id,
                childState = runtimeNode?.restorator?.storeAll()
            )
        )
    }

    return flow {
        emit(activeId.asRuntimeSlot())
        router.events.collect { event ->
            activeId = event.transform(activeId)
            emit(activeId.asRuntimeSlot())
        }
    }.stateIn(
        coroutineScope,
        SharingStarted.Eagerly,
        initial.asRuntimeSlot(),
    )
}
