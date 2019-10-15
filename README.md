# event-bus

[![Tests](https://github.com/philiprehberger/kt-event-bus/actions/workflows/publish.yml/badge.svg)](https://github.com/philiprehberger/kt-event-bus/actions/workflows/publish.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.philiprehberger/event-bus.svg)](https://central.sonatype.com/artifact/com.philiprehberger/event-bus)
[![Last updated](https://img.shields.io/github/last-commit/philiprehberger/kt-event-bus)](https://github.com/philiprehberger/kt-event-bus/commits/main)

Type-safe coroutine-based event bus for Kotlin with Flow integration.

## Installation

### Gradle (Kotlin DSL)

```kotlin
implementation("com.philiprehberger:event-bus:0.1.6")
```

### Maven

```xml
<dependency>
    <groupId>com.philiprehberger</groupId>
    <artifactId>event-bus</artifactId>
    <version>0.1.6</version>
</dependency>
```

## Usage

```kotlin
import com.philiprehberger.eventbus.*

data class UserCreated(val name: String)
data class OrderPlaced(val orderId: Int, val total: Double)

val bus = EventBus()

// Subscribe to specific event types
bus.on<UserCreated>(scope) { event ->
    println("User created: ${event.name}")
}

// Emit events
bus.emit(UserCreated("Alice"))
bus.emit(OrderPlaced(1, 99.99))  // Not received by UserCreated subscriber
```

### Flow Integration

```kotlin
bus.flow<OrderPlaced>()
    .filter { it.total > 100.0 }
    .collect { order ->
        processLargeOrder(order)
    }
```

### Subscription Lifecycle

```kotlin
// Subscription is tied to a CoroutineScope
val job = bus.on<UserCreated>(scope) { handle(it) }

// Cancel manually
job.cancel()

// Or automatically when the scope is cancelled
scope.cancel()
```

## API

| Class / Function | Description |
|------------------|-------------|
| `EventBus` | Main event bus class backed by `MutableSharedFlow` |
| `EventBus.on<T>()` | Subscribe to events of type T within a CoroutineScope |
| `EventBus.emit()` | Emit an event to all matching subscribers |
| `EventBus.flow<T>()` | Get a Flow of events filtered to type T |

## Development

```bash
./gradlew test       # Run tests
./gradlew check      # Run all checks
./gradlew build      # Build JAR
```

## Support

If you find this project useful:

⭐ [Star the repo](https://github.com/philiprehberger/kt-event-bus)

🐛 [Report issues](https://github.com/philiprehberger/kt-event-bus/issues?q=is%3Aissue+is%3Aopen+label%3Abug)

💡 [Suggest features](https://github.com/philiprehberger/kt-event-bus/issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement)

❤️ [Sponsor development](https://github.com/sponsors/philiprehberger)

🌐 [All Open Source Projects](https://philiprehberger.com/open-source-packages)

💻 [GitHub Profile](https://github.com/philiprehberger)

🔗 [LinkedIn Profile](https://www.linkedin.com/in/philiprehberger)

## License

[MIT](LICENSE)
