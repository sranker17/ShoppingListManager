# Plan: Fix Language Selection Issue

The language selection is currently not working because the app lacks the necessary infrastructure for per-app language settings and the `MainActivity` does not support `AppCompatDelegate`'s locale management correctly.

## Problem Analysis
1.  **Missing `locales_config.xml`**: Android 13+ requires this file to know which languages are supported for per-app settings.
2.  **Missing Manifest configuration**: The `localeConfig` attribute is missing from the `<application>` tag.
3.  **Base Activity**: `MainActivity` extends `ComponentActivity`, but `AppCompatDelegate.setApplicationLocales` works best with `AppCompatActivity`.
4.  **No Android Theme**: `AppCompatActivity` requires an `AppCompat` theme, which is currently missing.
5.  **Direct UI Call**: `SettingsScreen` calls `LocaleHelper.setLocale` directly, which is not ideal for state management.

## Proposed Solution
1.  **Infrastructure Setup**: Add `locales_config.xml` and update `AndroidManifest.xml`.
2.  **Base Activity Update**: Switch to `AppCompatActivity` and add a compatible Android theme.
3.  **Refactor Locale Management**: Move locale switching logic to the `ViewModel` and ensure it's applied correctly.

---

## Agent Tasks

### Task 1: Android Infrastructure & Resources
**Context**: Prepare the project for per-app language support.
**Deliverables**:
- Create `app/src/main/res/xml/locales_config.xml`:
    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <locale-config xmlns:android="http://schemas.android.com/apk/res/android">
        <locale android:name="en"/>
        <locale android:name="hu"/>
        <locale android:name="de"/>
    </locale-config>
    ```
- Update `app/src/main/AndroidManifest.xml`:
    - Add `android:localeConfig="@xml/locales_config"` to the `<application>` tag.
    - Add the following service inside `<application>` for backward compatibility:
        ```xml
        <service
            android:name="androidx.appcompat.app.AppLocalesMetadataHolderService"
            android:enabled="false"
            android:exported="false">
            <intent-filter>
                <action android:name="androidx.appcompat.app.AppLocalesMetadataHolderService" />
            </intent-filter>
        </service>
        ```
**Acceptance Criteria**:
- `locales_config.xml` exists.
- `AndroidManifest.xml` contains the new configuration.

### Task 2: Activity & Theme Modernization
**Context**: Ensure `MainActivity` supports `AppCompat` features.
**Deliverables**:
- Create `app/src/main/res/values/themes.xml`:
    ```xml
    <resources>
        <style name="Theme.ShoppingListManager" parent="Theme.AppCompat.DayNight.NoActionBar" />
    </resources>
    ```
- Update `app/src/main/AndroidManifest.xml` to use `android:theme="@style/Theme.ShoppingListManager"`.
- Modify `MainActivity.kt`:
    - Change inheritance from `ComponentActivity` to `AppCompatActivity`.
    - Ensure imports are updated.
**Acceptance Criteria**:
- `MainActivity` extends `AppCompatActivity`.
- App builds and runs with the new theme.

### Task 3: Locale Management Refactoring
**Context**: Centralize locale switching logic.
**Deliverables**:
- Update `ui/settings/SettingsViewModel.kt`:
    - Call `LocaleHelper.setLocale(language)` inside `setLanguage(language)`.
- Update `ui/settings/SettingsScreen.kt`:
    - Remove the direct call to `LocaleHelper.setLocale(lang)` from the `onClick` handler.
- Update `MainActivity.kt`:
    - In `onCreate`, observe the language from `SettingsViewModel` and ensure `LocaleHelper.setLocale` is called at least once if the current `AppCompatDelegate.getApplicationLocales()` is empty or different (to sync with DataStore on first launch).
**Acceptance Criteria**:
- Selecting a language in Settings immediately updates the UI language.
- The selected language persists across app restarts.
- No direct `LocaleHelper` calls in the UI layer.
