# UI Redesign Plan

## Design Comparison: Old vs. New

**1. Headers & App Bars**
- **Old:** Utilizes a standard Android TopAppBar with a distinct background color (e.g., purple) across all screens.
- **New:** No standard TopAppBar. The "double header" is removed. Screens use large, centered typography for titles near the top of the screen. Navigation icons (like a back arrow) and action icons (like a theme palette) float directly on the background.

**2. Visual Cleanliness & Element Blending**
- **Old:** UI elements (list items, text input fields, archive sessions) are contained within distinct cards or bounding boxes with different background colors (e.g., grey cards on a dark background).
- **New:** Elements blend seamlessly into the background. List items do not use cards or boxes; text and icons sit directly on the app's main background color. Buttons often use thin outlines rather than solid block backgrounds, creating a lighter, more minimalist feel.

**3. Bottom Navigation Bar**
- **Old:** Features a distinct background bar, icons with text labels below them, and a prominent pill-shaped highlight for the active tab.
- **New:** Minimalist navigation. Icons float at the bottom without a distinct bar background (or its background is identical to the app's background). No text labels under the icons. No prominent active pill highlight.

**4. Typography & Spacing**
- **Old:** Standard sizing, elements packed together.
- **New:** Generous spacing between elements. Titles are larger and act as structural anchors for the screen.

---

## Agent Tasks

Here is the breakdown of the changes into fully separable, LLM-digestible tasks.

### Task 1: Redesign App Theme and Typography
**Context:** Update the color scheme and typography to match the minimalist style.
**Deliverables:**
- Update `ui/theme/Color.kt` and `AppTheme.kt` to define a unified, deep background color.
- Remove contrasting card/surface colors that don't blend.
- Define a primary text/icon color and an accent color.
- Update typography to use a clean font with appropriate sizes for the new large centered headers.

### Task 2: Redesign Scaffold and Bottom Navigation
**Context:** Remove the distinct background, labels, and active highlight from the bottom navigation.
**Deliverables:**
- Modify `AppScaffold.kt` (or where the Bottom Navigation is defined).
- Make the bottom navigation bar background transparent or identical to the main background.
- Remove text labels from the `NavigationBarItem`.
- Remove the active pill highlight (e.g., by overriding `NavigationBarItemColors` to make the indicator color transparent).
- Ensure icons are evenly spaced.

### Task 3: Redesign Top Navigation & Headers
**Context:** Remove the standard `TopAppBar` and replace it with large centered text titles and floating icons.
**Deliverables:**
- Remove the standard `TopAppBar` from `AppScaffold.kt` or individual screens.
- Create a new reusable `CustomHeader` composable that displays a centered text title, an optional left-aligned back button, and optional right-aligned action icons (e.g., the shopping cart or theme palette).
- Integrate this new header into the Shopping List, Archive, and Settings screens.

### Task 4: Redesign Shopping List Screen
**Context:** Remove cards/boxes from list items and inputs, making them blend into the background.
**Deliverables:**
- Update `ShoppingItemRow`: Remove the card/box background. The item text, checkbox, and quantity controls should sit directly on the main background.
- Update `AddItemBar`: Remove the outlined box. Use a minimalist text field (e.g., just a bottom line or no line) and a minimalist `+` button (thin outline or simple icon).
- Adjust spacing and padding for a cleaner look.

### Task 5: Redesign Archive Screen
**Context:** Apply the minimalist list design to the archive sessions.
**Deliverables:**
- Update `ArchiveScreen.kt`: Remove the grey cards from the `ArchiveSession` items.
- Ensure the session name (date) and item count sit directly on the background.
- Apply similar unboxed design to `ArchiveDetailScreen.kt`.

### Task 6: Redesign Settings Screen
**Context:** Update the settings options to match the new toggle and button styles.
**Deliverables:**
- Update `SettingsScreen.kt`: Remove background cards.
- Replace the pill-shaped theme buttons with minimalist outlined or solid circular buttons.
- Update language and text size selectors to match the new minimalist aesthetic.
- Ensure titles and descriptions blend into the background.

### Task 7: Empty States Redesign
**Context:** Ensure empty states match the new minimalist aesthetic.
**Deliverables:**
- Update the illustrations or text for empty states (e.g., no items in the list or empty archive) to match the new minimalist theme, removing background cards and integrating seamlessly with the deep background color.

### Task 8: Main Page Cleanup & UTF-8 Support
**Context:** Clean up the main page and ensure the app supports UTF-8 characters.
**Deliverables:**
- Remove the "Shopping List Manager" title and the shopping cart icon from the main page.
- Ensure the input/output accepts UTF-8 characters (e.g., körte, teniszütő).

### Task 9: Expanded Theme Selection
**Context:** Add more light themes and improve the theme selector UI to support more options.
**Deliverables:**
- Add at least 3 new light color themes using examples from `docs/example/new_colors`.
- Change the theme selector in the settings screen to a new layout (e.g., Grid or expandable list) that scales better with many themes.
