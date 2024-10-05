package de.halfbit.componental

public typealias ComponentalLogger = (tag: String, message: String) -> Unit

public object Componental {
    /** Set to actual logger instance. Noop by default. */
    public fun setLogger(logger: ComponentalLogger) {
        log = logger
    }

    internal var log: ComponentalLogger = { _, _ -> }
}
