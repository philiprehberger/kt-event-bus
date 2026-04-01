package com.philiprehberger.eventbus

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EventBusTest {

    data class UserCreated(val name: String)
    data class OrderPlaced(val orderId: Int, val total: Double)
    data class SystemEvent(val message: String)

    @Test
    fun `subscribe and receive events`() = runTest {
        val bus = EventBus()
        val received = mutableListOf<UserCreated>()

        val job = bus.on<UserCreated>(this) { received.add(it) }
        advanceUntilIdle()

        bus.emit(UserCreated("Alice"))
        advanceUntilIdle()

        assertEquals(1, received.size)
        assertEquals("Alice", received[0].name)
        job.cancel()
    }

    @Test
    fun `type filtering delivers only matching events`() = runTest {
        val bus = EventBus()
        val users = mutableListOf<UserCreated>()
        val orders = mutableListOf<OrderPlaced>()

        val job1 = bus.on<UserCreated>(this) { users.add(it) }
        val job2 = bus.on<OrderPlaced>(this) { orders.add(it) }
        advanceUntilIdle()

        bus.emit(UserCreated("Alice"))
        bus.emit(OrderPlaced(1, 99.99))
        bus.emit(UserCreated("Bob"))
        bus.emit(SystemEvent("startup"))
        advanceUntilIdle()

        assertEquals(2, users.size)
        assertEquals(1, orders.size)
        assertEquals("Alice", users[0].name)
        assertEquals("Bob", users[1].name)
        assertEquals(1, orders[0].orderId)
        job1.cancel()
        job2.cancel()
    }

    @Test
    fun `scope cancellation unsubscribes`() = runTest {
        val bus = EventBus()
        val received = mutableListOf<UserCreated>()

        val childJob = launch {
            bus.on<UserCreated>(this) { received.add(it) }
        }
        advanceUntilIdle()

        bus.emit(UserCreated("Alice"))
        advanceUntilIdle()

        // Cancel the child scope
        childJob.cancel()
        advanceUntilIdle()

        bus.emit(UserCreated("Bob"))
        advanceUntilIdle()

        // Only Alice should be received (Bob was emitted after cancellation)
        assertEquals(1, received.size)
        assertEquals("Alice", received[0].name)
    }

    @Test
    fun `multiple subscribers receive the same event`() = runTest {
        val bus = EventBus()
        val subscriber1 = mutableListOf<UserCreated>()
        val subscriber2 = mutableListOf<UserCreated>()

        val job1 = bus.on<UserCreated>(this) { subscriber1.add(it) }
        val job2 = bus.on<UserCreated>(this) { subscriber2.add(it) }
        advanceUntilIdle()

        bus.emit(UserCreated("Alice"))
        advanceUntilIdle()

        assertEquals(1, subscriber1.size)
        assertEquals(1, subscriber2.size)
        assertEquals("Alice", subscriber1[0].name)
        assertEquals("Alice", subscriber2[0].name)
        job1.cancel()
        job2.cancel()
    }

    @Test
    fun `flow collection works`() = runTest {
        val bus = EventBus()

        launch {
            bus.emit(OrderPlaced(1, 10.0))
            bus.emit(OrderPlaced(2, 20.0))
            bus.emit(UserCreated("Alice"))
            bus.emit(OrderPlaced(3, 30.0))
        }

        val orders = bus.flow<OrderPlaced>().take(3).toList()
        assertEquals(3, orders.size)
        assertEquals(listOf(1, 2, 3), orders.map { it.orderId })
    }

    @Test
    fun `flow filters by type correctly`() = runTest {
        val bus = EventBus()

        launch {
            bus.emit(UserCreated("Alice"))
            bus.emit(OrderPlaced(1, 50.0))
        }

        val event = bus.flow<OrderPlaced>().first()
        assertEquals(1, event.orderId)
        assertEquals(50.0, event.total)
    }

    @Test
    fun `on returns a cancellable job`() = runTest {
        val bus = EventBus()
        val received = mutableListOf<UserCreated>()

        val job = bus.on<UserCreated>(this) { received.add(it) }
        advanceUntilIdle()

        bus.emit(UserCreated("Alice"))
        advanceUntilIdle()
        assertEquals(1, received.size)

        job.cancel()
        advanceUntilIdle()

        bus.emit(UserCreated("Bob"))
        advanceUntilIdle()
        assertEquals(1, received.size, "Should not receive events after job cancellation")
    }

    @Test
    fun `emitting with no subscribers does not throw`() = runTest {
        val bus = EventBus()
        bus.emit(UserCreated("Alice"))
        assertTrue(true)
    }

    @Test
    fun `once receives only the first event then auto-cancels`() = runTest {
        val bus = EventBus()
        val received = mutableListOf<UserCreated>()

        val job = bus.once<UserCreated>(this) { received.add(it) }
        advanceUntilIdle()

        bus.emit(UserCreated("Alice"))
        advanceUntilIdle()

        bus.emit(UserCreated("Bob"))
        advanceUntilIdle()

        // Should only have received the first event
        assertEquals(1, received.size)
        assertEquals("Alice", received[0].name)
        assertTrue(job.isCompleted)
    }

    @Test
    fun `subscriberCount tracks active subscribers`() = runTest {
        val bus = EventBus()
        assertEquals(0, bus.subscriberCount())

        val job1 = bus.on<UserCreated>(this) { }
        advanceUntilIdle()
        assertEquals(1, bus.subscriberCount())

        val job2 = bus.on<OrderPlaced>(this) { }
        advanceUntilIdle()
        assertEquals(2, bus.subscriberCount())

        job1.cancel()
        advanceUntilIdle()
        assertEquals(1, bus.subscriberCount())

        job2.cancel()
        advanceUntilIdle()
        assertEquals(0, bus.subscriberCount())
    }
}
