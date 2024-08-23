package de.halfbit.componental.router

import de.halfbit.componental.lifecycle.MutableLifecycle
import de.halfbit.componental.restorator.Restorator

public data class RouteNode<out R : Any, out C : Any>(
    val route: R,
    val child: C,
)

public data class RuntimeRouteNode<out R : Any, out C : Any>(
    val node: RouteNode<R, C>,
    val lifecycle: MutableLifecycle,
    val restorator: Restorator,
)
