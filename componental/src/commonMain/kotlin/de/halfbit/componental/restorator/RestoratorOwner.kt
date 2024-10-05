package de.halfbit.componental.restorator

public interface RestoratorOwner {
    public val restorator: Restorator
}

/**
 * Wrap initial screen routing code with this builder function to ensure
 * it does not interfere with the state restoration.
 *
 * In the example below the code in lambda will only be executed if the screen
 * is created. If the screen is restored (created during the restoration process),
 * the code in lambda will not be executed.
 *
 * ```
 * init {
 *    routeOnCreate {
 *       val agent = findAgentById(agentId)
 *       when {
 *          agent?.authData == null -> stackRouter.replace(Route.Login)
 *          agent.settings.name.isEmpty() -> stackRouter.replace(Route.RegisterAgent)
 *          else -> stackRouter.replace(Route.Order)
 *       }
 *    }
 * }
 * ```
 */
public inline fun RestoratorOwner.routeOnCreate(block: () -> Unit) {
    if (!restorator.canRestore) {
        block()
    }
}
