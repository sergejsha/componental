/** Copyright 2024 Halfbit GmbH, Sergej Shafarenka */
package de.halfbit.componental.back

import de.halfbit.componental.lifecycle.Lifecycle

public interface BackNavigationOwner {
    public val backNavigation: BackNavigation

    public companion object {
        public fun create(lifecycle: Lifecycle): BackNavigationOwner =
            object : BackNavigationOwner {
                override val backNavigation: BackNavigation =
                    object : BackNavigation {
                        override fun register(onNavigateBack: OnNavigateBack) {
                            lifecycle.subscribe(
                                object : Lifecycle.Subscriber.Callbacks {
                                    override fun onResume() {
                                        BackNavigation.register(onNavigateBack)
                                    }

                                    override fun onPause() {
                                        BackNavigation.unregister(onNavigateBack)
                                    }
                                }
                            )
                        }
                    }
            }
    }
}

public fun BackNavigationOwner.onNavigateBack(onNavigateBack: OnNavigateBack) {
    backNavigation.register(onNavigateBack)
}