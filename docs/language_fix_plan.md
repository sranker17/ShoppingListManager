# Plan: Safe Fix for Language Not Changing

This plan fixes language switching with minimal risk by using a single source of truth for language (`SettingsRepository` / DataStore) and applying locale updates from top-level app state.

## Why the current behavior can fail
1. **UI layer applies locale directly**: `SettingsScreen` currently calls `LocaleHelper.setLocale(...)` in click handlers, which couples UI events with app-level configuration.
2. **No locale config for Android 13+ app language settings**: The app does not declare `locales_config.xml` and `android:localeConfig`.
3. **Activity base and theme setup are not AppCompat-oriented**: `MainActivity` is `ComponentActivity`; locale APIs are most predictable with `AppCompatActivity`.
4. **Risk of dual persistence**: Adding AppCompat auto-store locale service while also storing language in DataStore can create conflicting sources of truth.

## Safety principles
- Keep **one source of truth** for selected language: `SettingsRepository.languageFlow`.
- Do **not** add `AppLocalesMetadataHolderService` or AppCompat auto-store metadata.
- Apply locale from top-level state (activity/root composition), not directly from settings UI.
- Keep language mapping centralized in `LocaleHelper`.

---

## Implementation Tasks

### Task 1: Add Android locale infrastructure (Android 13+)
**Goal**: Expose supported app languages to the system.

**Deliverables**:
- Create `app/src/main/res/xml/locales_config.xml`:
  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <locale-config xmlns:android="http://schemas.android.com/apk/res/android">
      <locale android:name="en" />
      <locale android:name="hu" />
      <locale android:name="de" />
  </locale-config>
  ```
- Update `app/src/main/AndroidManifest.xml` `<application>` with:
  - `android:localeConfig="@xml/locales_config"`

**Do not add**:
- `androidx.appcompat.app.AppLocalesMetadataHolderService`
- Any `autoStoreLocales` metadata

**Acceptance Criteria**:
- `locales_config.xml` exists with exactly supported locales.
- Manifest includes `android:localeConfig`.

### Task 2: Make activity/theme AppCompat-compatible
**Goal**: Ensure stable behavior of `AppCompatDelegate.setApplicationLocales`.

**Deliverables**:
- Create `app/src/main/res/values/themes.xml`:
  ```xml
  <resources>
      <style name="Theme.ShoppingListManager" parent="Theme.AppCompat.DayNight.NoActionBar" />
  </resources>
  ```
- Set application theme in `AndroidManifest.xml`:
  - `android:theme="@style/Theme.ShoppingListManager"`
- Update `MainActivity.kt`:
  - Extend `AppCompatActivity` instead of `ComponentActivity`.
  - Keep Compose setup unchanged otherwise.

**Acceptance Criteria**:
- App compiles and launches.
- `MainActivity` extends `AppCompatActivity`.
- No theme crash at startup.

### Task 3: Refactor locale application to top-level state
**Goal**: Apply locale consistently from state, not UI clicks.

**Deliverables**:
- `ui/settings/SettingsScreen.kt`:
  - Remove direct `LocaleHelper.setLocale(...)` call from language button click.
  - Keep only `viewModel.setLanguage(lang)`.
- `MainActivity.kt` (inside composition):
  - Observe `settingsViewModel.uiState.language`.
  - Use a guarded side effect (`LaunchedEffect(language)`) to call `LocaleHelper.setLocale(language)` only when needed.
  - Compare current app locales (`AppCompatDelegate.getApplicationLocales()`) before setting to avoid redundant updates.
- `ui/settings/SettingsViewModel.kt`:
  - Keep persistence only (`repository.setLanguage(language)`).
  - Do not call locale APIs from the ViewModel.

**Acceptance Criteria**:
- Changing language in Settings updates visible UI text.
- Selected language persists across restart.
- No direct locale call in UI button handlers.
- Locale updates are state-driven from top-level composition.

---

## Verification Checklist
- Run app on Android 13+:
  - Open app language settings (system per-app language) and confirm available languages: English, Hungarian, German.
- In-app Settings:
  - Switch `EN -> HU -> DE` and verify key labels update.
  - Navigate between screens after each change; language remains correct.
- Restart app:
  - Previously selected language is still active.
- Regression checks:
  - Theme switching still works.
  - Text size switching still works.

## Rollback Plan
If regressions occur:
1. Revert `MainActivity` locale side-effect changes.
2. Keep manifest `localeConfig` and resource file (safe to retain).
3. Restore prior activity base/theme only if startup/theming regressions are confirmed.

## Definition of Done
- Language change works reliably at runtime.
- Persistence works across restart.
- Architecture uses one locale source of truth (DataStore).
- No AppCompat auto-store locale service is introduced.
