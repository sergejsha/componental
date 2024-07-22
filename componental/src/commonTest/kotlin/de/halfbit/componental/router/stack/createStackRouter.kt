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
    stack: List<Id>,
    lifecycleState: Lifecycle.State = Lifecycle.State.Resumed,
): Triple<StackRouter<Id>, ComponentContext, Channel<Stack<Id, Child>>> {
    val router = StackRouter<Id>()
    val lifecycle = MutableLifecycle.create()
    val context = ComponentContext.create(
        lifecycle = lifecycle,
        lifecycleCoroutineScope = this,
        restorator = Restorator(null)
    )
    val stackFlow = context.childStack(
        router = router,
        initial = stack,
        serializer = { Id.serializer() },
        childFactory = { id, _ ->
            when (id) {
                Id.Page1 -> Child.Page1
                Id.Page2 -> Child.Page2
                Id.Page3 -> Child.Page3
                Id.Page4 -> Child.Page4
            }
        },
    )
    lifecycle.moveToState(lifecycleState)
    return Triple(router, context, stackFlow.collectIntoChannel(this))
}

fun page1(): RouteNode<Id, Child> = RouteNode(Id.Page1, Child.Page1)
fun page2(): RouteNode<Id, Child> = RouteNode(Id.Page2, Child.Page2)
fun page3(): RouteNode<Id, Child> = RouteNode(Id.Page3, Child.Page3)
fun page4(): RouteNode<Id, Child> = RouteNode(Id.Page4, Child.Page4)

sealed interface Child {
    data object Page1 : Child
    data object Page2 : Child
    data object Page3 : Child
    data object Page4 : Child
}

@Serializable
sealed interface Id {
    @Serializable
    data object Page1 : Id

    @Serializable
    data object Page2 : Id

    @Serializable
    data object Page3 : Id

    @Serializable
    data object Page4 : Id
}
