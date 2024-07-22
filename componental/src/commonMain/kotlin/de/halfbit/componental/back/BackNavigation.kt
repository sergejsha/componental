/** Copyright 2024 Halfbit GmbH, Sergej Shafarenka */
package de.halfbit.componental.back

public interface BackNavigation {
    public fun register(onNavigateBack: OnNavigateBack)

    public companion object {
        private val callbacks: MutableList<OnNavigateBack> = mutableListOf()

        internal fun register(callback: OnNavigateBack) {
            callbacks += callback
        }

        internal fun unregister(callback: OnNavigateBack) {
            callbacks -= callback
        }

        /** To be called by the screen holding compose UI, e.g. by an activity on android. */
        public fun dispatchOnNavigateBack() {
            callbacks.toList().lastOrNull()?.onNavigateBack()
        }
    }
}
