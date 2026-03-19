package com.philiprehberger.eventbus

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

/**
 * A type-safe, coroutine-based event bus with Flow integration.
 *
 * Events are dispatched via a [MutableSharedFlow] and subscribers receive only events
 * matching their registered type. Subscription lifecycles are tied to a [CoroutineScope].
 *
 * ```kotlin
 * val bus = EventBus()
 *
 * // Subscribe to specific event types
 * bus.on<UserCreated>(scope) { event ->
 *     println("User created: ${event.name}")
 * }
 *
 * // Emit events
 * bus.emit(UserCreated("Alice"))
 * ```
 *
 * @param replay The number of past events to replay to new subscribers (default 0).
 * @param extraBufferCapacity Additional buffer capacity beyond [replay] (default 64).
 */
public class EventBus(
    replay: Int = 0,
    extraBufferCapacity: Int = 64,
) {
    @PublishedApi internal val _events: MutableSharedFlow<Any> = MutableSharedFlow<Any>(
        replay = replay,
        extraBufferCapacity = extraBufferCapacity,
    )

    /**
     * Subscribes to events of type [T] within the given [scope].
     *
     * The subscription is automatically cancelled when the scope is cancelled.
     *
     * @param T The event type to subscribe to.
     * @param scope The coroutine scope that controls the subscription lifetime.
     * @param handler Suspend function invoked for each matching event.
     * @return A [Job] that can be used to cancel the subscription.
     */
    public inline fun <reified T : Any> on(scope: CoroutineScope, noinline handler: suspend (T) -> Unit): Job {
        return scope.launch {
            _events.asSharedFlow()
                .filterIsInstance<T>()
                .collect { event -> handler(event) }
        }
    }

    /**
     * Emits an event to all subscribers whose type filter matches.
     *
     * This is a suspend function that waits until the event is buffered.
     *
     * @param event The event to emit.
     */
    public suspend fun emit(event: Any) {
        _events.emit(event)
    }

    /**
     * Returns a [Flow] of events filtered to type [T].
     *
     * Useful for integrating with other Flow operators.
     *
     * ```kotlin
     * bus.flow<OrderPlaced>()
     *     .filter { it.total > 100 }
     *     .collect { processOrder(it) }
     * ```
     *
     * @param T The event type to filter for.
     * @return A Flow emitting only events of type [T].
     */
    public inline fun <reified T : Any> flow(): Flow<T> {
        return _events.asSharedFlow().filterIsInstance()
    }
}
