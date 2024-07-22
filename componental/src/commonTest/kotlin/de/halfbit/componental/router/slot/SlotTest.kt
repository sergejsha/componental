/** Copyright 2024 Halfbit GmbH, Sergej Shafarenka */
package de.halfbit.componental.router.slot

import de.halfbit.componental.testing.runExitingTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SlotTest {

    @Test
    fun push_setsNewActiveChild() = runExitingTest {
        val (router, events) = createSlotRouter(slot = null)
        val actual = events.receive()
        val expected = Slot<Id, Child>(active = null)
        assertEquals(expected, actual)

        router.set(Id.Page1)
        val actual2 = events.receive()
        val expected2 = Slot(active = page1())
        assertEquals(expected2, actual2)
    }

    @Test
    fun push_replacesActiveChild() = runExitingTest {
        val (router, events) = createSlotRouter(slot = Id.Page1)
        val actual = events.receive()
        val expected = Slot(active = page1())
        assertEquals(expected, actual)

        router.set(Id.Page2)
        val actual2 = events.receive()
        val expected2 = Slot(active = page2())
        assertEquals(expected2, actual2)

        router.set(Id.Page3)
        val actual3 = events.receive()
        val expected3 = Slot(active = page3())
        assertEquals(expected3, actual3)
    }

    @Test
    fun push_clearsActiveChild() = runExitingTest {
        val (router, events) = createSlotRouter(slot = Id.Page1)
        val actual = events.receive()
        val expected = Slot(active = page1())
        assertEquals(expected, actual)

        router.set(null)
        val actual2 = events.receive()
        val expected2 = Slot<Id, Child>(active = null)
        assertEquals(expected2, actual2)
    }
}