/** Copyright 2024 Halfbit GmbH, Sergej Shafarenka */
package de.halfbit.componental.router.stack

import de.halfbit.componental.ComponentContext
import de.halfbit.componental.lifecycle.Lifecycle
import de.halfbit.componental.lifecycle.MutableLifecycle
import de.halfbit.componental.restorator.Restorator
import de.halfbit.componental.router.RouteNode
import de.halfbit.componental.testing.collectIntoChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.Serializable

fun TestScope.createStackRouter(
    stack: List<Route>,
    lifecycleState: Lifecycle.State = Lifecycle.State.Resumed,
): Triple<StackRouter<Route>, ComponentContext, Channel<Stack<Child>>> {
    val router = StackRouter<Route>("TestRouter")
    val lifecycle = MutableLifecycle.create()
    val context = ComponentContext.create(
        lifecycle = lifecycle,
        lifecycleCoroutineScope = this,
        restorator = Restorator(null)
    )
    val stackFlow = context.childStack(
        router = router,
        initial = { stack },
        serializer = { Route.serializer() },
        childFactory = { id, _ ->
            when (id) {
                Route.Page1 -> Child.Page1
                Route.Page2 -> Child.Page2
                Route.Page3 -> Child.Page3
                Route.Page4 -> Child.Page4
            }
        },
    )
    lifecycle.moveToState(lifecycleState)
    return Triple(router, context, stackFlow.collectIntoChannel(this))
}

fun page1(): RouteNode<Child> = RouteNode(Route.Page1, Child.Page1)
fun page2(): RouteNode<Child> = RouteNode(Route.Page2, Child.Page2)
fun page3(): RouteNode<Child> = RouteNode(Route.Page3, Child.Page3)
fun page4(): RouteNode<Child> = RouteNode(Route.Page4, Child.Page4)

sealed interface Child {
    data object Page1 : Child
    data object Page2 : Child
    data object Page3 : Child
    data object Page4 : Child
}

@Serializable
sealed interface Route {
    @Serializable
    data object Page1 : Route

    @Serializable
    data object Page2 : Route

    @Serializable
    data object Page3 : Route

    @Serializable
    data object Page4 : Route
}
