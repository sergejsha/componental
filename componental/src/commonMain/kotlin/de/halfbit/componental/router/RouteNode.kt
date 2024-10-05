package de.halfbit.componental.router

import de.halfbit.componental.lifecycle.MutableLifecycle
import de.halfbit.componental.restorator.Restorator

public data class RouteNode<out C : Any>(
    val route: Any,
    val child: C,
)

public data class RuntimeRouteNode<out C : Any>(
    val node: RouteNode<C>,
    val lifecycle: MutableLifecycle,
    val restorator: Restorator,
)
