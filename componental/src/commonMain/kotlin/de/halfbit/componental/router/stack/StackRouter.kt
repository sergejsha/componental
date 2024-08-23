/** Copyright 2024 Halfbit GmbH, Sergej Shafarenka */
package de.halfbit.componental.router.stack

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
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.protobuf.ProtoBuf

public data class Stack<out I : Any, out C : Any>(
    val active: RouteNode<I, C>,
    val inactive: List<RouteNode<I, C>>
)

public class StackRouter<R : Any> : Router<TransformStack<R>>()
public typealias TransformStack<R> = (routes: List<R>) -> List<R>

public fun <R : Any> StackRouter<R>.push(route: R) {
    route { routes -> routes + route }
}

public fun <R : Any> StackRouter<R>.pop(onLastItem: () -> Unit) {
    route { routes ->
        if (routes.size > 1) {
            routes.subList(0, routes.size - 1)
        } else {
            onLastItem()
            routes
        }
    }
}

public fun <R : Any> StackRouter<R>.replace(route: R) {
    route { routes ->
        routes.subList(0, routes.size - 1) + route
    }
}

public fun <R : Any> StackRouter<R>.replaceAll(route: R) {
    route { listOf(route) }
}

@OptIn(ExperimentalSerializationApi::class)
public fun <Route : Any, Child : Any> ComponentContext.childStack(
    router: StackRouter<Route>,
    initial: List<Route>,
    serializer: () -> KSerializer<Route>,
    childFactory: (route: Route, context: ComponentContext) -> Child,
): StateFlow<Stack<Route, Child>> {
    val runtimeNodes = mutableMapOf<Route, RuntimeRouteNode<Route, Child>>()
    val restoredRoute: Map<Route, RestorableRoute<Route>>? =
        restorator.restoreRoute()?.let {
            ProtoBuf.decodeFromByteArray(
                ListSerializer(RestorableRoute.serializer(serializer())), it
            ).associateBy { route -> route.route }
        }

    fun createRuntimeRouteNode(route: Route): RuntimeRouteNode<Route, Child> {
        val tag = "route:${route::class.simpleName.toString()}"
        val childLifecycle = lifecycle.createMutableChildLifecycle()
        val restoredChildState = restoredRoute?.get(route)?.consumeChildState()
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
            Componental.debug("STACK Created: ${newNode.node.route}")
            runtimeNodes[route] = newNode
            childLifecycle.moveToState(State.Resumed)
        }
    }

    fun Collection<Route>.asRuntimeStack(): Stack<Route, Child> {
        val stackNodes = map { route ->
            runtimeNodes[route] ?: createRuntimeRouteNode(route)
        }

        stackNodes.reversed().forEachIndexed { index, node ->
            val targetState = if (index == 0) State.Resumed else State.Created
            node.lifecycle.moveToState(targetState)
        }

        val destroyedRoutes = runtimeNodes.keys - toSet()
        destroyedRoutes.forEach { route ->
            Componental.debug("STACK Destroyed: $route")
            runtimeNodes[route]?.lifecycle?.moveToState(State.Destroyed)
        }

        runtimeNodes.keys.removeAll(destroyedRoutes)
        return stackNodes.map { it.node }.toStack()
    }

    var routes = restoredRoute?.keys?.toList() ?: initial
    restorator.storeRoute {
        ProtoBuf.encodeToByteArray(
            ListSerializer(RestorableRoute.serializer(serializer())),
            routes.map { route ->
                RestorableRoute(
                    route = route,
                    childState = runtimeNodes[route]?.restorator?.storeAll()
                )
            }
        )
    }

    return flow {
        emit(routes)
        router.transformers.collect { transform ->
            routes = transform(routes)
            emit(routes)
        }
    }.distinctUntilChanged()
        .map { it.asRuntimeStack() to it }
        .map { (stack, routes) ->
            Componental.debug("STACK Current: $routes")
            stack
        }
        .stateIn(
            coroutineScope,
            SharingStarted.Eagerly,
            initial.asRuntimeStack(),
        )
}

private fun <R : Any, C : Any> List<RouteNode<R, C>>.toStack(): Stack<R, C> {
    check(isNotEmpty()) { "List used as a stack must have at least one entry" }
    return Stack(
        active = last(),
        inactive = if (size == 1) emptyList() else subList(0, lastIndex),
    )
}
