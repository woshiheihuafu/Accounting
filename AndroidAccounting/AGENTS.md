# AGENTS.md

## Scope

These instructions apply to Android development under `AndroidAccounting/`.

## General Principles

- Prefer simple, maintainable solutions over complex abstractions.
- Follow the existing architecture and code style in the project.
- Do not perform unrelated refactors.
- Every code change should have a clear purpose and value.
- Read the relevant implementation before modifying code.
- Android technical plans, module plans, progress records, bug records, and necessary code comments should be written in Chinese by default.
- Keep Kotlin identifiers, package names, class names, function names, resource keys, and file names in English unless Android conventions require otherwise.
- For PRD-to-plan, module plans, approvals, progress updates, bug records, and cross-day resume work, use the project-local `android-feature-workflow` skill as the source of truth for workflow details.
- Specialized Android skills define execution checklists for Compose, ViewModel, Repository, network, and module architecture work.

## Kotlin

- Use Kotlin for new code.
- Prefer immutable objects.
- Prefer `val` over `var`.
- Use expression functions where they improve readability.
- Avoid platform type leakage.
- Avoid `!!`.
- Prefer `sealed interface` or `sealed class` for finite state.

For null safety, prefer:

```kotlin
value?.let { }
value ?: defaultValue
```

Avoid:

```kotlin
value!!
```

Only use `!!` when non-nullability is clearly proven.

## Dependency Injection

- Prefer constructor injection.
- Prefer depending on interfaces instead of concrete implementations.
- Use the project's unified dependency injection framework.
- Do not use the Service Locator pattern.
- Do not manually create dependencies in ViewModels or UI code.

Recommended:

```kotlin
class UserRepositoryImpl @Inject constructor(
    private val api: UserApi
)
```

Avoid:

```kotlin
class UserRepositoryImpl {
    private val api = Retrofit.Builder()...
}
```

## Architecture

Use this structure by default:

```text
UI
↓
ViewModel
↓
Repository
↓
DataSource
↓
Network / Database
```

Requirements:

- UI must not access databases directly.
- UI must not access the network directly.
- ViewModels must not create repositories.
- Repository is the single entry point for data access.
- DataSources only handle data reads and writes.
- ViewModels own presentation logic and UI decisions.
- Repositories own data orchestration, cache, refresh, fallback, and data-source policy.
- If a domain/use-case layer exists for a feature, put domain business rules there instead of in UI code.

## MVI State Management

### UiState

- Use immutable `data class` types.
- Expose state with `StateFlow`.
- Include explicit loading and error states.

Example:

```kotlin
data class UserUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val errorMessage: String? = null
)
```

### UiIntent

- Represent user actions.
- Use `sealed interface`.

Example:

```kotlin
sealed interface UserIntent {
    data object Refresh : UserIntent
    data class SelectUser(val id: Long) : UserIntent
}
```

### UiEffect

Use effects for one-time events:

- Navigation
- Toast
- Snackbar
- Dialog

Do not put one-time events in `UiState`.

## ViewModel

Expose:

```kotlin
StateFlow<UiState>
Flow<UiEffect>
```

Use one event entry point:

```kotlin
fun onIntent(intent: UiIntent)
```

Requirements:

- Use `viewModelScope`.
- Put presentation logic in the ViewModel.
- Composables must not contain business logic.

## Coroutines

Prefer:

```kotlin
viewModelScope.launch
```

Prefer these reactive primitives:

```kotlin
Flow
StateFlow
SharedFlow
```

Avoid callback-heavy code.

Do not block the main thread with:

```kotlin
runBlocking
Thread.sleep()
```

## Compose

### Page Structure

Use this structure:

```text
Route
↓
Screen
↓
Content
```

Responsibilities:

Route:

- Navigation handling
- Effect collection

Screen:

- State dispatch
- Event forwarding

Content:

- Stateless rendering

### State Collection

Use:

```kotlin
collectAsStateWithLifecycle()
```

Avoid:

```kotlin
collectAsState()
```

### State Hoisting

Use:

```text
State up, events down
```

Composables should receive only:

- State
- Callbacks

Composables must not access repositories directly.

## Lists

`LazyColumn` and `LazyRow` must provide stable keys.

Recommended:

```kotlin
items(
    items = users,
    key = { it.id }
)
```

Avoid:

```kotlin
items(users)
```

## Side Effects

Use:

```kotlin
LaunchedEffect
```

for one-time event collection.

Do not collect the same event flow from multiple places.

Prefer collecting effects in the Route layer.

## Compose Performance

Avoid expensive computation during composition.

Prefer:

```kotlin
remember
derivedStateOf
```

Read animation values as late as possible.

Recommended:

```kotlin
Modifier.graphicsLayer {
    alpha = animatedAlpha
}
```

## Design System

### Colors

Use colors from the design system.

Recommended:

```kotlin
Theme.colorScheme.primary
Theme.colorScheme.background
```

Avoid hardcoded colors in business code:

```kotlin
Color(0xFFFF0000)
Color.White
Color.Black
```

### Typography

Prefer typography from the design system.

Recommended:

```kotlin
Theme.typography.titleMedium
```

Avoid excessive inline `TextStyle` definitions.

### Shape

Prefer shared design system shape definitions.

Avoid hardcoded corner radius values scattered through code.

## Data Layer

### Repository

Repository is the single entry point for data access.

UI must not directly depend on:

- Retrofit
- Room
- SQL
- API

### Model Mapping

Use this flow:

```text
DTO
↓
Mapper
↓
Domain
↓
UI
```

Do not:

- Pass DTOs directly to UI
- Pass entities directly to UI

## Error Handling

Do not swallow exceptions.

Bad:

```kotlin
try {
} catch (e: Exception) {
}
```

Good:

```kotlin
try {
} catch (e: Exception) {
    logger.error(e)
    throw e
}
```

Alternatively, convert errors to a unified `Result`.

## Local Storage

Recommended:

- Room
- DataStore

Use with caution:

- SharedPreferences

## Network Layer

Network access must be centralized.

Requirements:

- API definitions and business logic must be separated.
- Use a consistent serialization approach.
- Network requests must not appear directly in UI code.

## Internationalization

- Do not hardcode user-visible strings.
- Manage strings with resource files.
- Keep supported languages synchronized.
- Centralize common text.

## Testability

### TestTag

Important UI must provide stable `TestTag` values.

Examples:

```text
home_loading
home_error
home_retry_button
```

Prefer business IDs for list items:

```text
home_item_123
```

Avoid index-based tags:

```text
home_item_0
```

### Test Coverage

For stateful ViewModel, Repository, and data-flow changes, cover the relevant behavior:

- Initial state
- Loading state
- Success state
- Failure state
- Retry flow
- Boundary cases

For UI-only, documentation-only, or small mechanical changes, run the narrowest useful validation and explain any test gaps.

## Naming

Use these naming patterns:

```text
UserUiState
UserUiIntent
UserUiEffect

UserRepository
UserRepositoryImpl

UserRemoteDataSource
UserLocalDataSource
```

## Prohibited

- UI directly accessing the network.
- UI directly accessing databases.
- ViewModels creating Retrofit.
- ViewModels creating Room.
- Exposing `MutableState` to UI.
- Blocking the main thread.
- Mixing multiple architectures without a reason.
- Using `!!` without a clear reason.
- Mixing DTO, Entity, and UI models.
- Running expensive computation in Compose.
