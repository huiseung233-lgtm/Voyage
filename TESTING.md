# Voyage Testing Guide

This document outlines how to run tests and the testing infrastructure available in the Voyage project.

## 1. Running Tests

### Local Unit Tests
Run local unit tests (located in `src/test`) using Gradle:
```bash
./gradlew testDebugUnitTest
```
These tests run on your development machine's JVM and are fast.

### Instrumented Tests
Run instrumented tests (located in `src/androidTest`) using Gradle:
```bash
./gradlew connectedDebugAndroidTest
```
**Note:** An Android device or emulator must be connected and visible via `adb devices`.

## 2. Test Infrastructure

### Hilt Dependency Injection
*   **Unit Tests:** Use `@Mock` and standard Mockito or `mock()` manually. Hilt injection is generally not used in local unit tests unless testing Hilt modules specifically.
*   **Instrumented Tests:** 
    *   The project uses a `CustomTestRunner` to support Hilt.
    *   Annotate test classes with `@HiltAndroidTest`.
    *   Add `@get:Rule var hiltRule = HiltAndroidRule(this)`.
    *   Call `hiltRule.inject()` in your test method or `@Before`.

### Coroutines Testing
For testing Coroutines in Unit Tests, use `MainDispatcherRule`:

```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()

@Test
fun myCoroutineTest() = runTest {
    // Test logic...
}
```
This rule replaces the `Main` dispatcher with a `TestDispatcher`.

### LiveData Testing
Use `InstantTaskExecutorRule` to test LiveData synchronously:

```kotlin
@get:Rule
val instantTaskExecutorRule = InstantTaskExecutorRule()
```

### Room Database Testing
*   Use `Room.inMemoryDatabaseBuilder` for instrumented tests to avoid persisting data.
*   Dao functions can be mocked in Unit Tests (as seen in `VoyageRepositoryTest`).

## 3. Libraries Used
*   **JUnit 4:** Core testing framework.
*   **Mockito-Kotlin:** Mocking library for Unit Tests.
*   **Hilt Testing:** Dependency injection for tests.
*   **Coroutines Test:** Utilities for testing suspending functions.
*   **Compose UI Test:** Testing Jetpack Compose UIs.
