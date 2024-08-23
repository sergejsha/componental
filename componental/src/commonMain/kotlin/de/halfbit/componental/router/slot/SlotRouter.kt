/** Copyright 2024 Halfbit GmbH, Sergej Shafarenka */
package de.halfbit.componental.router.slot

import de.halfbit.componental.ComponentContext
import de.halfbit.componental.Componental
import de.halfbit.componental.coroutines.createChildCoroutineScope
import de.halfbit.componental.createChildContext
import de.halfbit.componental.lifecycle.Lifecycle.State
import de.halfbit.componental.lifecycle.createMutableChildLifecycle
import de.halfbit.componental.restorator.Restorator
import de.halfbit.componental.router.RestorableRoute
import de.halfbit.componental.router.RouteNode
import de.halfbit.componental.router.Router
import de.halfbit.componental.router.RuntimeRouteNode
import kotlinx.coroutines.flow.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.protobuf.ProtoBuf

public data class Slot<out R : Any, out C : Any>(
    val active: RouteNode<R, C>? = null,
)

public class SlotRouter<R : Any> : Router<TransformSlot<R>>()
public typealias TransformSlot<R> = (active: R?) -> R?

public fun <R : Any> SlotRouter<R>.set(active: R?) {
    route { _ -> active }
}

@OptIn(ExperimentalSerializationApi::class)
public fun <Route : Any, Child : Any> ComponentContext.childSlot(
    router: SlotRouter<Route>,
    initial: Route? = null,
    serializer: () -> KSerializer<Route>,
    childFactory: (route: Route, context: ComponentContext) -> Child,
): StateFlow<Slot<Route, Child>> {
    var runtimeNode: RuntimeRouteNode<Route, Child>? = null

    val restoredRoute: RestorableRoute<Route>? =
        restorator.restoreRoute()?.let {
            ProtoBuf.decodeFromByteArray(
                RestorableRoute.serializer(serializer()), it
            )
        }

    fun createRuntimeRouteNode(route: Route): RuntimeRouteNode<Route, Child> {
        val tag = "route:${route::class.simpleName.toString()}"
        val childLifecycle = lifecycle.createMutableChildLifecycle()
        val restoredChildState = if (restoredRoute?.route == route) {
            restoredRoute.consumeChildState()
        } else null
        val context = createChildContext(
            childLifecycle = childLifecycle,
            childCoroutineScope = coroutineScope.createChildCoroutineScope(tag),
            restorator = Restorator(restoredChildState),
        )
        val node = RouteNode(route, childFactory(route, context))
        return RuntimeRouteNode(
            node = node,
            lifecycle = childLifecycle,
            restorator = context.restorator,
        ).also { newNode ->
            Componental.debug("SLOT Created: ${newNode.node.route}")
            runtimeNode = newNode
            childLifecycle.moveToState(State.Resumed)
        }
    }

    fun Route?.asRuntimeSlot(): Slot<Route, Child> {
        val oldNode = runtimeNode
        val newNode = when (this) {
            null -> {
                runtimeNode = null
                null
            }

            else -> when {
                oldNode == null || this != oldNode.node.route -> {
                    createRuntimeRouteNode(this)
                }

                else -> oldNode
            }
        }

        if (oldNode != null && oldNode != newNode) {
            Componental.debug("SLOT Destroyed: ${oldNode.node.route}")
            oldNode.lifecycle.moveToState(State.Destroyed)
        }

        return Slot(active = newNode?.node)
    }

    var activeRoute = restoredRoute?.route ?: initial
    restorator.storeRoute {
        val route = activeRoute ?: return@storeRoute null
        ProtoBuf.encodeToByteArray(
            RestorableRoute.serializer(serializer()),
            RestorableRoute(
                route = route,
                childState = runtimeNode?.restorator?.storeAll()
            )
        )
    }

    return flow {
        emit(activeRoute)
        router.transformers.collect { transform ->
            activeRoute = transform(activeRoute)
            emit(activeRoute)
        }
    }.distinctUntilChanged()
        .map { it to it.asRuntimeSlot() }
        .map { (route, slot) ->
            Componental.debug("SLOT Active: $route")
            slot
        }
        .stateIn(
            coroutineScope,
            SharingStarted.Eagerly,
            initial.asRuntimeSlot(),
        )
}
