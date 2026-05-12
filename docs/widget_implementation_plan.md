# Shopping List Widget Implementation Plan

## Overview
This plan breaks down the implementation of a 1x1 home screen widget for the Shopping List Manager into discrete, fully separable, LLM-readable agent tasks. The widget will act as a quick status indicator showing whether there are unchecked items in the shopping list.

## Requirements Summary
- **Size**: 1x1 (icon-sized) on the Android home screen.
- **Background**: Dark background (similar to the app icon, utilizing `MinimalistBackground`).
- **Foreground**: A distinct circular outline. The stroke width must be thicker than a hairline (e.g., 4dp - 6dp).
- **Status Colors**:
  - **Light Green**: The list is empty OR all items in the list are checked off.
  - **Red**: There is at least one unchecked item remaining in the list.
- **Technology Choice**: Jetpack Glance (Compose for Widgets), as the project uses Jetpack Compose, Kotlin, and modern Android architecture.

---

## Agent Tasks

### Task 1: Setup Jetpack Glance and Widget Foundation
**Objective**: Add necessary widget dependencies, define the widget metadata, and create the base receiver/provider.

**Instructions**:
1. Open `app/build.gradle.kts`. Add the Jetpack Glance dependencies for AppWidgets and Material3: `implementation("androidx.glance:glance-appwidget:<latest_version>")` and `implementation("androidx.glance:glance-material3:<latest_version>")` (use version `1.1.0` or higher).
2. Create a new package: `com.sranker.shoppinglistmanager.widget`.
3. Inside the new package, create an `AppWidgetProvider` class named `StatusWidgetReceiver` that extends `GlanceAppWidgetReceiver`.
4. Create a `GlanceAppWidget` class named `StatusWidget` containing a basic UI placeholder (e.g., a simple Box with a text).
5. Create `res/xml/widget_info.xml` defining the widget properties. Set `minWidth` and `minHeight` for a 1x1 grid cell (approx `40dp`), `targetCellWidth` and `targetCellHeight` to `1`, and `updatePeriodMillis` to `0` (we will use push-based updates).
6. Register the `StatusWidgetReceiver` in `app/src/main/AndroidManifest.xml` within the `<application>` tag, including the `<intent-filter>` for `android.appwidget.action.APPWIDGET_UPDATE` and `<meta-data>` pointing to `@xml/widget_info`.

### Task 2: Implement the Widget UI (Glance Compose)
**Objective**: Build the visual representation of the widget with a dark background and the required circular outline.

**Instructions**:
1. In `Color.kt`, ensure the definition of a specific light green and red for the widget if not already present (e.g., `WidgetGreen` and `WidgetAlertRed`).
2. Create a vector drawable `res/drawable/ic_widget_circle_outline.xml`. This should be an oval shape with a transparent center and a thick stroke (e.g., `android:strokeWidth="5dp"`).
3. Update the `StatusWidget` class to implement the UI layout:
   - Use a root `Box` or `Column` with a background color of `MinimalistBackground` (or equivalent dark color to match the app icon).
   - Use `GlanceModifier.fillMaxSize()`, `.padding()`, and `CornerRadius` to give it a rounded background if desired.
   - Inside the root layout, use an `Image` composable (from `androidx.glance.Image`) loading the `ic_widget_circle_outline` drawable.
   - Apply `ColorFilter.tint(color)` to the image to dynamically set the circle's color based on the current state.
4. Implement a state mechanism within `StatusWidget` (e.g., `PreferencesGlanceStateDefinition` or just accepting a boolean/enum state) to switch between the Light Green and Red tints.

### Task 3: Implement Data Layer Integration and Update Trigger
**Objective**: Connect the widget to the database to react instantly to changes in the shopping list.

**Instructions**:
1. Create a `WidgetUpdater` utility object or class in the `widget` package. This class should have a function to recalculate the widget state and update all active instances of `StatusWidget` using `GlanceAppWidgetManager`.
2. The logic to calculate the status should be:
   - Retrieve all items from the database (via `ShoppingItemDao` or `ShoppingListRepository`).
   - If the total count is `0`, the status is **Green**.
   - If the count of items where `isChecked == false` is `0`, the status is **Green**.
   - If the count of items where `isChecked == false` is `> 0`, the status is **Red**.
3. Integrate the `WidgetUpdater` into the `ShoppingListRepository` (or the relevant UseCase/ViewModel depending on architecture). Whenever an item is inserted, deleted, or its `isChecked` status changes, trigger the widget update function so the home screen reflects the new reality immediately.

### Task 4: UI Interaction and Final Polish
**Objective**: Ensure the widget opens the app when tapped, displays properly in the widget picker, and passes all edge cases.

**Instructions**:
1. Add an action to the root element of `StatusWidget`: `modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>())`. This ensures tapping the widget opens the app directly.
2. Provide a default preview image or layout for the widget so the user can see an accurate representation in the Android Widget Picker.
3. Verify the visual hierarchy to ensure the widget size perfectly fits a 1x1 grid cell without cropping the thick circular outline.
4. Test edge cases:
   - List is completely cleared -> Circle must be Green.
   - All items are checked off -> Circle must be Green.
   - User unchecks a previously checked item -> Circle must instantly turn Red.
