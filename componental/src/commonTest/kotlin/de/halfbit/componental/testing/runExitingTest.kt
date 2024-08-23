/** Copyright 2024 Halfbit GmbH, Sergej Shafarenka */
package de.halfbit.componental.testing

import de.halfbit.componental.Componental
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
fun runExitingTest(testBody: suspend TestScope.() -> Unit) {
    Componental.setLogger { level, message, err ->
        println("Componental: [$level] $message")
        err?.printStackTrace()
    }
    val exitingException = CancellationException("exited properly")
    try {
        runTest(UnconfinedTestDispatcher()) {
            testBody()
            cancel(exitingException)
        }
    } catch (e: CancellationException) {
        if (e != exitingException) {
            throw e
        }
    }
}
