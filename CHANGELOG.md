# Changelog

## 0.1.6 (2026-03-20)

- Fix README: remove Groovy section, update badge label to "Tests"
- Fix CHANGELOG formatting: split malformed entry, remove preamble

## 0.1.5 (2026-03-20)

- Standardize README: fix title, badges, version sync, remove Requirements section

## 0.1.4 (2026-03-20)

- Add issueManagement to POM metadata

## 0.1.2 (2026-03-18)

- Upgrade to Kotlin 2.0.21 and Gradle 8.12
- Enable explicitApi() for stricter public API surface
- Add issueManagement to POM metadata

## 0.1.1 (2026-03-18)

- Fix CI badge and gradlew permissions

## 0.1.0 (2026-03-17)

### Added
- `EventBus` class backed by `MutableSharedFlow`
- `on<T>()` for type-safe event subscription tied to a CoroutineScope
- `emit()` for dispatching events to all matching subscribers
- `flow<T>()` for Flow-based event consumption with type filtering
- Automatic unsubscription on scope cancellation
- Configurable replay and buffer capacity
