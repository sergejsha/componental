package de.halfbit.componental

public object Componental {

    // logger section api
    public enum class LogLevel { DEBUG, ERROR }

    public fun setLogger(logger: (LogLevel, String, Throwable?) -> Unit) {
        log = logger
    }

    // logger section implementation
    private var log: ((LogLevel, String, Throwable?) -> Unit)? = null
    internal fun log(level: LogLevel, message: String, err: Throwable? = null) {
        log?.invoke(level, message, err)
    }

    internal fun debug(message: String, err: Throwable? = null) {
        log?.invoke(LogLevel.DEBUG, message, err)
    }
}
