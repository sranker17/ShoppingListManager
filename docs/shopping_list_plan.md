# Shopping List App — Project Plan

## Approved Feature Set

### Original Features
- List with easy item addition
- Toggle items purchased / not purchased
- Adjustable quantities (default: 1)
- Archive session (checked items move to archive, removed from list)
- Separate archive view
- Archive entries named by date (e.g., `2026-04-04`), renameable
- Archived items reloadable into current list (additive, archive intact)
- All data stored locally
- Modern, clean design
- Multiple color themes (≤3 colors each), light & dark, selectable in Settings
- Multilingual: English, Hungarian, German, switchable in Settings
- Adjustable text size
- Modern shopping cart icon
- Home screen = list itself
- Responsive UI for 5–6 inch screens
- Simple, fast animations

### Approved Additions
- **Drag-to-reorder** items manually
- **Auto-move checked items to bottom** of the list
- **Autocomplete / history-based suggestions** when adding items
- **Swipe gestures**: swipe-left or swipe-right = delete (with undo); toggle is done via checkbox tap only
- **Undo last action** via snackbar

---

## Technical Decisions

| Concern | Decision | Rationale |
|---|---|---|
| UI framework | Jetpack Compose | Best fit for animations, swipe, drag-to-reorder |
| DI | Hilt | Standard Android MVVM; great test support |
| Local storage | Room (SQLite) | Relational data: items ↔ archive sessions |
| Preferences | DataStore (Preferences) | Theme, language, text size |
| Unit testing | JUnit 5 + MockK + Turbine | Kotlin-idiomatic; Turbine for Flow/StateFlow |
| Navigation | Jetpack Navigation Compose | Single-activity, type-safe routes |
| Drag-to-reorder | `sh.calvin.reorderable` | Best maintained Compose reorder library |
| Swipe | Compose `SwipeToDismiss` | Native Material3 support |
| Undo | SnackbarHostState + in-memory buffer | Standard Material3 pattern |
| Autocomplete | Separate `ItemHistory` Room table | Stores distinct names ever typed |

## Assumptions (correct if wrong)

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36
- **Kotlin version**: latest stable 2.x (pinned at task start from [kotlinlang.org/releases](https://kotlinlang.org/docs/releases.html))
- **Package name**: `com.sranker.shoppinglistmanager`
- **App name**: `ShoppingListManager`
- **Project location**: current directory (no subdirectory created)
- **Themes**: 4 predefined themes only — Ocean Dark, Forest Light, Sunset Dark, Snow Light (no Follow System option)
- **Swipe left or right** = delete (with undo snackbar); **toggle** = checkbox tap only
- **Auto-sort checked**: checked items sink to bottom; drag-to-reorder works within each group
- **Archive reload**: keeps quantities, adds to existing list, archive unchanged
- **Language switch**: applies immediately at runtime via `AppCompatDelegate` + `LocaleListCompat`

---

## Agent Tasks

Each task is fully self-contained. Each section defines context, inputs, deliverables, and acceptance criteria.

---

### Task 1 — Project Setup & Build Configuration

**Context**  
Create the Android project skeleton. No business logic here — only scaffolding.

**Deliverables**
- Android project initialized in the **current directory** (package `com.sranker.shoppinglistmanager`, app name `ShoppingListManager`)
- `build.gradle.kts` (app + root) with all dependencies declared:
  - Compose BOM (latest stable)
  - Room + KSP
  - Hilt + hilt-navigation-compose
  - DataStore Preferences
  - Navigation Compose
  - `sh.calvin.reorderable`
  - JUnit 5, MockK, Turbine, Hilt testing
  - Kotlin: latest stable 2.x (check [kotlinlang.org](https://kotlinlang.org/docs/releases.html) and pin exact version in `libs.versions.toml`)
- `AndroidManifest.xml` with single activity, internet permission omitted
- `MainActivity.kt` — empty Compose entry point
- `Application` class (`ShoppingListManagerApp`) annotated with `@HiltAndroidApp`
- Proguard rules stubbed

**Acceptance Criteria**
- Project builds with `./gradlew assembleDebug` with zero errors
- All dependencies resolve
- No Sonar-flagged issues (no unused imports, no wildcard imports, proper file headers if required)

---

### Task 2 — Data Layer: Room Entities & Database

**Context**  
Define all Room entities, DAOs, and the database class. No ViewModel or UI yet.

**Inputs**
- Task 1 completed (project exists with Room dependency)

**Entities to create**

| Entity | Fields |
|---|---|
| `ShoppingItem` | `id: Long`, `name: String`, `quantity: Int`, `isPurchased: Boolean`, `sortOrder: Int` |
| `ArchiveSession` | `id: Long`, `name: String` (default = date string `yyyy-MM-dd`), `createdAt: Long` |
| `ArchivedItem` | `id: Long`, `sessionId: Long` (FK), `name: String`, `quantity: Int` |
| `ItemHistory` | `id: Long`, `name: String` (UNIQUE) |

**Deliverables**
- `data/db/` package with entities, DAOs, `ShopListDatabase`
- DAOs:
  - `ShoppingItemDao`: insert, update, delete, getAll (ordered by `sortOrder`), updateAll (for reorder batch)
  - `ArchiveSessionDao`: insert, update (rename), delete, getAll
  - `ArchivedItemDao`: insertAll, getBySession, deleteBySession
  - `ItemHistoryDao`: upsert (insert or ignore), search by prefix
- `ShopListDatabase` with all entities, version 1, exported schema
- Migration stubs for future versions

**Acceptance Criteria**
- All DAOs compile
- No Sonar issues (no raw types, no missing nullability annotations)
- Schema JSON exported to `schemas/`

---

### Task 3 — Data Layer: Repositories

**Context**  
Wrap DAOs in repository classes. Expose only Flow/suspend functions. This is the sole data-access point for ViewModels.

**Inputs**
- Task 2 completed

**Deliverables**
- `data/repository/ShoppingRepository.kt`
  - `getItems(): Flow<List<ShoppingItem>>`
  - `addItem(name, quantity)`: inserts with `sortOrder = max + 1`, adds to `ItemHistory`
  - `toggleItem(id)`: flips `isPurchased`
  - `updateQuantity(id, qty)`
  - `deleteItem(id)`: returns deleted item for undo
  - `reorderItems(items: List<ShoppingItem>)`: batch update `sortOrder`
  - `archiveSession()`: moves all purchased items to a new `ArchiveSession` + `ArchivedItem`s, deletes them from `ShoppingItem`
- `data/repository/ArchiveRepository.kt`
  - `getSessions(): Flow<List<ArchiveSession>>`
  - `getItems(sessionId): Flow<List<ArchivedItem>>`
  - `renameSession(id, name)`
  - `reloadSession(sessionId)`: appends archived items to current `ShoppingItem` list
- `data/repository/SettingsRepository.kt` (DataStore)
  - `themeFlow`, `languageFlow`, `textSizeFlow`
  - `setTheme`, `setLanguage`, `setTextSize`
- `data/repository/SuggestionRepository.kt`
  - `getSuggestions(prefix): Flow<List<String>>`
- Hilt `@Module` providing all repositories
- Unit tests for all repository methods using in-memory Room DB + coroutines test dispatcher
  - 100% branch coverage on all public methods

**Acceptance Criteria**
- All unit tests pass
- No Sonar issues
- Repositories do not import any ViewModel or UI classes

---

### Task 4 — ViewModel: Shopping List

**Context**  
All business logic for the main screen. Exposes `StateFlow` to the UI. No Compose imports.

**Inputs**
- Task 3 completed

**Deliverables**
- `ui/shoppinglist/ShoppingListViewModel.kt`
  - State: `ShoppingListUiState` data class containing item list, loading flag, undo event (nullable), suggestions list
  - Actions:
    - `addItem(name, quantity)`
    - `toggleItem(id)` — if auto-sort-checked enabled, re-sorts after toggle
    - `updateQuantity(id, qty)`
    - `deleteItem(id)` — stores in undo buffer, emits undo event
    - `undoDelete()` — restores from buffer
    - `dismissUndo()` — clears buffer
    - `reorderItems(from, to)` — updates sort order
    - `archiveSession()`
    - `onQueryChanged(text)` — triggers suggestion lookup
- Unit tests (JUnit 5 + MockK + Turbine):
  - All action methods
  - Undo buffer expiry after snackbar dismiss
  - Auto-sort behavior
  - Suggestions emission
  - Full branch + line coverage

**Acceptance Criteria**
- No UI imports in ViewModel
- All StateFlows tested with Turbine
- No Sonar issues

---

### Task 5 — ViewModel: Archive

**Context**  
Business logic for the archive screen.

**Inputs**
- Task 3 completed

**Deliverables**
- `ui/archive/ArchiveViewModel.kt`
  - State: list of sessions, selected session items, rename dialog state
  - Actions: `loadSessions()`, `selectSession(id)`, `renameSession(id, name)`, `reloadSession(id)`
- Unit tests: all actions, full branch/line coverage

**Acceptance Criteria**
- All tests pass
- No Sonar issues

---

### Task 6 — ViewModel: Settings

**Context**  
Business logic for the settings screen.

**Inputs**
- Task 3 completed

**Deliverables**
- `ui/settings/SettingsViewModel.kt`
  - State: current theme, language, text size
  - Actions: `setTheme(theme)`, `setLanguage(lang)`, `setTextSize(size)`
- Unit tests: all actions, all flows

**Acceptance Criteria**
- All tests pass
- No Sonar issues

---

### Task 7 — Theme System

**Context**  
Define Material3 color schemes and typography. No screens yet.

**Inputs**
- Task 1 completed

**Deliverables**
- `ui/theme/AppTheme.kt` — composable wrapping `MaterialTheme`, accepts `AppTheme` enum
- `ui/theme/Color.kt` — all color tokens (max 3 colors per theme, each with surface/on-surface derived shades)
- `ui/theme/Type.kt` — typography using Google Fonts (`Outfit` or `Inter`)
- Themes implemented (4 total, no system-follow option):
  1. **Ocean Dark** — deep navy, teal, white
  2. **Forest Light** — soft green, warm beige, dark brown
  3. **Sunset Dark** — deep charcoal, coral orange, warm cream
  4. **Snow Light** — pure white, slate blue, charcoal
- `AppTheme` enum with `OCEAN_DARK`, `FOREST_LIGHT`, `SUNSET_DARK`, `SNOW_LIGHT`
- Text size: `TextSizePreference` enum (`SMALL`, `MEDIUM`, `LARGE`) mapped to SP multipliers applied in `AppTheme`

**Acceptance Criteria**
- All themes render without crash in a preview
- No hardcoded colors outside `Color.kt`
- No Sonar issues

---

### Task 8 — Localization

**Context**  
All user-facing strings extracted to resource files. Runtime language switching.

**Inputs**
- Task 1 completed

**Deliverables**
- `res/values/strings.xml` (English — default)
- `res/values-hu/strings.xml` (Hungarian)
- `res/values-de/strings.xml` (German)
- All strings for: app name, list screen labels, archive labels, settings labels, button texts, error messages, snackbar messages, dialog texts, accessibility content descriptions
- `util/LocaleHelper.kt` — applies `LocaleListCompat` via `AppCompatDelegate` for runtime switching
- `Language` enum: `ENGLISH`, `HUNGARIAN`, `GERMAN`

**Acceptance Criteria**
- No hardcoded user-visible strings in Kotlin/Compose files
- Switching language in Settings updates all visible text immediately
- No Sonar issues (no unused string resources)

---

### Task 9 — Navigation

**Context**  
Single-activity navigation graph. No screen content yet — just routes and the scaffold.

**Inputs**
- Tasks 1, 7, 8 completed

**Deliverables**
- `ui/navigation/AppNavGraph.kt` — Navigation Compose graph with routes:
  - `Screen.ShoppingList`
  - `Screen.Archive`
  - `Screen.ArchiveDetail(sessionId: Long)`
  - `Screen.Settings`
- `ui/AppScaffold.kt` — outer scaffold with:
  - Bottom navigation bar (List, Archive, Settings)
  - Shopping cart icon in top app bar
  - `SnackbarHost` for undo messages
- `MainActivity.kt` updated to host `AppNavGraph`

**Acceptance Criteria**
- Navigation between all screens works
- Back stack behaves correctly
- No Sonar issues

---

### Task 10 — UI: Shopping List Screen

**Context**  
The home screen. Most complex screen in the app.

**Inputs**
- Tasks 4, 7, 8, 9 completed

**Deliverables**
- `ui/shoppinglist/ShoppingListScreen.kt`
- Components:
  - `AddItemBar` — text field with autocomplete dropdown, quantity stepper (+/-), Add button
  - `ShoppingItemRow` — item name, quantity badge, checkbox; supports drag handle
  - `SwipeToDismissBox` wrapping each row: **both left and right swipe = delete** (triggers undo snackbar); toggle is checkbox-tap only
  - `ReorderableLazyColumn` using `sh.calvin.reorderable`
  - Auto-sort: checked items rendered below unchecked items
  - Drag handle visible only on unchecked items (checked items can't be reordered above unchecked)
  - `ArchiveSessionButton` — fixed at bottom, disabled if no checked items
  - Undo snackbar triggered after delete; calls `undoDelete()` or `dismissUndo()`
- Animations:
  - Item enter: slide-in + fade-in
  - Item exit: slide-out + fade-out
  - Checkbox toggle: scale pulse + color transition
  - Checked item sinking to bottom: animated placement

**Acceptance Criteria**
- All interactions work as specified
- Screen adapts to small (360dp) and large (430dp) widths
- No hardcoded strings or colors
- No Sonar issues

---

### Task 11 — UI: Archive Screen

**Context**  
List of archive sessions and detail view for each.

**Inputs**
- Tasks 5, 7, 8, 9 completed

**Deliverables**
- `ui/archive/ArchiveScreen.kt` — list of `ArchiveSession` cards
  - Each card shows session name (date or custom), item count
  - Long-press or edit icon opens rename dialog
  - Tap navigates to `ArchiveDetailScreen`
- `ui/archive/ArchiveDetailScreen.kt`
  - Shows all archived items (name + quantity)
  - "Reload to list" button — calls `reloadSession`, shows confirmation snackbar
  - Items are read-only (no toggle/delete)
- `ui/archive/RenameDialog.kt` — Material3 AlertDialog with text field

**Acceptance Criteria**
- Rename persists after rotation
- Reload adds items to list without clearing archive
- No Sonar issues

---

### Task 12 — UI: Settings Screen

**Context**  
Settings for theme, language, and text size.

**Inputs**
- Tasks 6, 7, 8, 9 completed

**Deliverables**
- `ui/settings/SettingsScreen.kt`
  - **Theme section**: horizontal scrollable row of theme chips (color swatches + name)
  - **Language section**: segmented button or radio group (EN / HU / DE)
  - **Text size section**: slider with preview label ("Small / Medium / Large")
  - All changes apply immediately; no save button needed
- Section headers styled as group labels

**Acceptance Criteria**
- Theme change applies to entire app immediately
- Language change applies immediately without restart
- Text size slider updates all text sizes live
- No Sonar issues

---

### Task 13 — App Icon

**Context**  
Modern shopping cart launcher icon.

**Inputs**
- Task 1 completed

**Deliverables**
- Adaptive icon set in `res/mipmap-*` (all densities)
- `ic_launcher_foreground.xml` — vector shopping cart
- `ic_launcher_background.xml` — solid color matching Ocean Dark teal
- `ic_launcher.xml` (adaptive icon)
- Monochrome variant for Android 13+
- Top app bar icon: same shopping cart vector in `res/drawable/ic_cart.xml`

**Acceptance Criteria**
- Icon renders correctly on all densities and in adaptive icon preview
- No Sonar issues (no unused resources)

---

### Task 14 — Animations & Polish

**Context**  
Final animation pass and UI polish. Depends on all screens being complete.

**Inputs**
- Tasks 10, 11, 12 completed

**Deliverables**
- Review all screens for animation consistency
- Add screen transition animations to navigation (shared axis or fade)
- Polish:
  - Empty state illustration for list screen (no items yet)
  - Empty state for archive screen
  - Loading shimmer on initial DB load
  - Consistent padding, elevation, and corner radii across all cards
- Verify responsiveness on 360dp, 390dp, 430dp widths using Compose previews

**Acceptance Criteria**
- All animations at 60fps (no jank on mid-range devices)
- Empty states are informative and visually consistent with theme
- No Sonar issues

---

### Task 15 — Sonar Configuration & CI Readiness

**Context**  
Final quality gate setup.

**Inputs**
- All previous tasks completed

**Deliverables**
- `sonar-project.properties` configured for Android (sources, tests, exclusions)
- `build.gradle.kts` configured with JaCoCo for coverage reports
- Gradle task `jacocoTestReport` producing XML report consumable by Sonar
- `.editorconfig` with Kotlin style rules matching Kotlin coding conventions
- `detekt.yml` or `ktlint` config aligned with Sonar rules (optional but recommended)
- Final pass: run `./gradlew lint` and fix all issues
- Final pass: run `./gradlew test` — all tests must pass

**Acceptance Criteria**
- `./gradlew test jacocoTestReport` completes successfully
- Coverage report includes all ViewModel and Repository classes
- Zero Lint errors
- No Sonar blocking issues

---

## Task Dependency Graph

```
Task 1 (Setup)
    ├── Task 2 (DB Entities)
    │       └── Task 3 (Repositories)
    │               ├── Task 4 (VM: List)
    │               ├── Task 5 (VM: Archive)
    │               └── Task 6 (VM: Settings)
    ├── Task 7 (Themes)
    ├── Task 8 (Localization)
    └── Task 13 (Icon)

Tasks 4,7,8 + Task 9 (Navigation)
    ├── Task 10 (UI: List Screen)    ← needs Task 4
    ├── Task 11 (UI: Archive Screen) ← needs Task 5
    └── Task 12 (UI: Settings)      ← needs Task 6

Tasks 10,11,12 → Task 14 (Animations & Polish)

All tasks → Task 15 (Sonar & CI)
```

## Parallelization Opportunities

| Parallel Group | Tasks |
|---|---|
| After Task 1 | Tasks 2, 7, 8, 13 can all run in parallel |
| After Task 3 | Tasks 4, 5, 6 can all run in parallel |
| After Task 9 | Tasks 10, 11, 12 can all run in parallel |
| Sequential only | Task 14 (needs 10+11+12), Task 15 (needs all) |
