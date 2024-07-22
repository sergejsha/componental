/** Copyright 2024 Halfbit GmbH, Sergej Shafarenka */
package de.halfbit.componental.router.stack

import de.halfbit.componental.back.BackNavigation
import de.halfbit.componental.back.OnNavigateBack
import de.halfbit.componental.back.onNavigateBack
import de.halfbit.componental.testing.ignore
import de.halfbit.componental.testing.runExitingTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StackTest {

    @Test
    fun push_addsPagesToStack() = runExitingTest {

        val (router, _, events) = createStackRouter(stack = listOf<Id>(Id.Page1))
        val actual = events.receive()
        val expected = Stack(active = page1(), inactive = emptyList())
        assertEquals(expected, actual)

        router.push(Id.Page2)
        val actual2 = events.receive()
        val expected2 = Stack(active = page2(), inactive = listOf(page1()))
        assertEquals(expected2, actual2)

        router.push(Id.Page3)
        val actual3 = events.receive()
        val expected3 = Stack(active = page3(), inactive = listOf(page1(), page2()))
        assertEquals(expected3, actual3)

        router.push(Id.Page4)
        val actual4 = events.receive()
        val expected4 = Stack(active = page4(), inactive = listOf(page1(), page2(), page3()))
        assertEquals(expected4, actual4)
    }

    @Test
    fun pop_removesPagesFromStack() = runExitingTest {

        val (router, _, events) = createStackRouter(stack = listOf(Id.Page1, Id.Page2, Id.Page3, Id.Page4))
        val actual = events.receive()
        val expected = Stack(active = page4(), inactive = listOf(page1(), page2(), page3()))
        assertEquals(expected, actual)

        router.pop { }
        val actual2 = events.receive()
        val expected2 = Stack(active = page3(), inactive = listOf(page1(), page2()))
        assertEquals(expected2, actual2)

        router.pop { }
        val actual3 = events.receive()
        val expected3 = Stack(active = page2(), inactive = listOf(page1()))
        assertEquals(expected3, actual3)

        router.pop { }
        val actual4 = events.receive()
        val expected4 = Stack(active = page1(), inactive = emptyList())
        assertEquals(expected4, actual4)
    }

    @Test
    fun pop_callsOnLastItem() = runExitingTest {

        // given
        val (router, _, events) = createStackRouter(stack = listOf(Id.Page1))
        events.ignore()

        // when
        var lastItemPopped = false
        router.pop { lastItemPopped = true }

        // then
        assertTrue(lastItemPopped)
    }

    @Test
    fun backPressed_removesPageFromStack() = runExitingTest {

        val (router, context, events) = createStackRouter(stack = listOf(Id.Page1, Id.Page2))
        context.onNavigateBack { router.pop { } }
        events.ignore()

        BackNavigation.dispatchOnNavigateBack()

        val actual = events.receive()
        val expected = Stack(active = page1(), inactive = emptyList())
        assertEquals(expected, actual)
    }

    @Test
    fun backPressed_callsOnLastItem_whenEmptyStack() = runExitingTest {

        var lastItemCalled = false
        val (router, context, events) = createStackRouter(stack = listOf(Id.Page1))
        context.onNavigateBack { router.pop { lastItemCalled = true } }
        events.ignore()

        BackNavigation.dispatchOnNavigateBack()
        assertTrue(lastItemCalled)
    }

    @Test
    fun backPressed_callsOnLastItem_afterEmptyingStack() = runExitingTest {

        var lastItemCalled = false
        val (router, context, events) = createStackRouter(stack = listOf(Id.Page1, Id.Page2))
        context.onNavigateBack { router.pop { lastItemCalled = true } }
        events.ignore()

        BackNavigation.dispatchOnNavigateBack()
        events.ignore()
        assertFalse(lastItemCalled)

        BackNavigation.dispatchOnNavigateBack()
        assertTrue(lastItemCalled)
    }
}
