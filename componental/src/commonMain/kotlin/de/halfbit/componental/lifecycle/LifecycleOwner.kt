package de.halfbit.componental.lifecycle

public interface LifecycleOwner {
    public val lifecycle: Lifecycle

    public companion object {
        public fun create(
            lifecycle: Lifecycle,
        ): LifecycleOwner =
            object : LifecycleOwner {
                override val lifecycle: Lifecycle
                    get() = lifecycle
            }
    }
}
