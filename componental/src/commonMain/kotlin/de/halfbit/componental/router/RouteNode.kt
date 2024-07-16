package de.halfbit.componental.router

import de.halfbit.componental.lifecycle.MutableLifecycle
import de.halfbit.componental.restorator.Restorator

public data class RouteNode<out I : Any, out C : Any>(
    val id: I,
    val child: C,
)

public data class RuntimeRouteNode<out I : Any, out C : Any>(
    val node: RouteNode<I, C>,
    val lifecycle: MutableLifecycle,
    val restorator: Restorator,
)
