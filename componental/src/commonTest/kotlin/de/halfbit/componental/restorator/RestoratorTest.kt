package de.halfbit.componental.restorator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RestoratorTest {

    @Test
    fun `storeAll persists null values`() {
        // prepare
        val restorator = Restorator(null)
        restorator.storeRoute { null }

        // test
        val bytes = restorator.storeAll()

        // assert
        assertTrue(bytes.isNotEmpty())
    }

    @Test
    fun `restore can restore null values`() {
        // prepare
        val restorator = Restorator(null)
        restorator.storeRoute { null }
        val bytes = restorator.storeAll()

        // test
        val restorator2 = Restorator(bytes)
        val routeByte = restorator2.restoreRoute()

        // assert
        assertNull(routeByte)
    }

    @Test
    fun `restore can restore multiple values`() {
        // prepare
        val restorator = Restorator(null)
        val route1: ByteArray? = null
        val route2: ByteArray = byteArrayOf(2)
        val route3: ByteArray = byteArrayOf(3)
        val route4: ByteArray? = null
        val route5: ByteArray = byteArrayOf(4)

        restorator.storeRoute { route1 }
        restorator.storeRoute { route2 }
        restorator.storeRoute { route3 }
        restorator.storeRoute { route4 }
        restorator.storeRoute { route5 }
        val bytes = restorator.storeAll()

        // test
        val restorator2 = Restorator(bytes)
        val actual1 = restorator2.restoreRoute()
        val actual2 = restorator2.restoreRoute()
        val actual3 = restorator2.restoreRoute()
        val actual4 = restorator2.restoreRoute()
        val actual5 = restorator2.restoreRoute()

        // assert
        assertByteArrayEquals(route1, actual1, "route1")
        assertByteArrayEquals(route2, actual2, "route2")
        assertByteArrayEquals(route3, actual3, "route3")
        assertByteArrayEquals(route4, actual4, "route4")
        assertByteArrayEquals(route5, actual5, "route5")

        println("*** size: ${bytes.size}")
    }
}

private fun assertByteArrayEquals(
    expected: ByteArray?,
    actual: ByteArray?,
    message: String,
) {
    val expectedList = expected?.let {
        it.fold("") { acc, byte -> acc + "0x" + byte.toString(16) + ", " }
    }?.removeSuffix(", ")

    val actualList = actual?.let {
        it.fold("") { acc, byte -> acc + "0x" + byte.toString(16) + ", " }
    }?.removeSuffix(", ")

    assertEquals(expectedList, actualList, message)
}