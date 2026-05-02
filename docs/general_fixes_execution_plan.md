# Plan: General UX/UI Fixes (Agent-Separable)

This document breaks the requested fixes into fully separable, LLM-digestible tasks with clear scope, deliverables, and acceptance criteria.

## Finalized Specifications

- IME "Done" on add-item input should add item (same behavior as Add button).
- Archive deletion behavior changed: no long-hold delete dialog. Use swipe-left/right delete with undo, same as shopping list item deletion.
- Settings section flow order: Theme -> Language -> Text Size.
- Do not normalize item casing in code; keep input string as typed (after existing blank/trim validation rules).
- Keyboard should start item input with capitalized sentence behavior (IME keyboard capitalization).
- Duplicate prevention: do not add item if a case-insensitive name match already exists (`apple` equals `ApPlE`).
- Shopping list item vertical gap target: `4.dp` (current was `8.dp`).
- Shopping list horizontal side padding target: `4.dp` (current was `16.dp`).
- Archive edit icon color should use `MaterialTheme.colorScheme.primary`.
- Launcher icon update only; use `OceanDarkColorScheme` primary as dominant color.

---

## Task 1 - Add item on keyboard Done action

**Goal**
Enable adding items via IME Done from the item input field.

**Scope**
- `ui/shoppinglist/ShoppingListScreen.kt` (`AddItemBar`).

**Deliverables**
- Add `keyboardOptions` with IME action `Done` on the add-item `TextField`.
- Add `keyboardActions` so pressing Done triggers exactly the same add logic as the Add button.
- Ensure field reset behavior matches button path (clear name, reset quantity to `1`).

**Acceptance Criteria**
- Pressing keyboard Done with non-blank input adds one item.
- Pressing keyboard Done with blank input does nothing.
- Behavior matches tapping Add (no divergent validation path).

---

## Task 2 - Keyboard capitalization without string normalization

**Goal**
Keep user-entered casing unchanged while guiding capitalization from keyboard behavior.

**Scope**
- Add-item flow in list screen and/or ViewModel/repository path used by add action.

**Deliverables**
- Remove/avoid any add-flow casing normalization that mutates the entered text.
- Configure item input keyboard options for sentence-style capitalization (capital starter behavior).
- Ensure both add entry paths (button and IME Done) persist the same user-entered casing.

**Acceptance Criteria**
- Typing `apple` keeps stored value `apple` (unless existing trim/blank handling changes it).
- Typing `ApPlE` keeps stored value `ApPlE`.
- No code path uppercases first character automatically.
- Existing stored items are not retroactively modified.

---

## Task 2B - Case-insensitive duplicate prevention on add

**Goal**
Prevent duplicate list entries by item name regardless of letter casing.

**Scope**
- Add-item flow in ViewModel/repository/data layer (where uniqueness check is most reliable).

**Deliverables**
- Implement case-insensitive duplicate check before insert.
- Treat names equal after trim + case-insensitive comparison as duplicates.
- If duplicate is detected, skip insertion and keep list unchanged.
- Apply duplicate logic consistently for both Add button and IME Done.

**Acceptance Criteria**
- Existing `apple` + new `ApPlE` -> no new item inserted.
- Existing `Milk` + new ` milk ` -> no new item inserted.
- Existing `Milk` + new `Milky` -> insert succeeds.
- No crash/regression in add-item flow.

---

## Task 3 - Shopping list density and width adjustments

**Goal**
Make rows denser and visually wider.

**Scope**
- `ui/shoppinglist/ShoppingListScreen.kt` list container spacing/padding.

**Deliverables**
- Change list row gap from `8.dp` to `4.dp`.
- Change list content horizontal padding from `16.dp` to `4.dp`.
- Keep row internals untouched unless required to preserve usability.

**Acceptance Criteria**
- Consecutive rows show visibly smaller vertical gap.
- Rows are nearly edge-to-edge with a small side margin.
- No clipping, overlap, or touch target regressions.

---

## Task 4 - Archive swipe-to-delete with undo (session cards)

**Goal**
Make archive sessions deletable by swipe left/right with undo support, replacing long-press delete intent.

**Scope**
- `ui/archive/ArchiveScreen.kt`
- `ui/archive/ArchiveViewModel.kt`
- Archive repository/DAO layer if delete+undo support is missing.

**Deliverables**
- Wrap archive session rows/cards in swipe-to-dismiss supporting both directions.
- On swipe, delete selected archive session and emit undo event/snackbar.
- Implement undo restoration path for deleted archive session (and related archived items).
- When restoring, check for duplicates: do not restore archived items if a case-insensitive name match already exists in the current shopping list.
- If all items from an archive session are filtered out due to duplicates, do not show a Snackbar (silent restoration with zero items).
- Remove/disable deletion semantics tied to long-hold popup flow (not needed anymore).

**Acceptance Criteria**
- Swiping left or right deletes one archive session.
- Snackbar undo button restores the deleted session and its items.
- When restoring, only non-duplicate items are restored to the current shopping list (case-insensitive comparison).
- If all archived items are duplicates and none are restored, no Snackbar appears (silent restoration).
- If some archived items are restored and some are filtered as duplicates, Snackbar appears with undo showing only restored items.
- Dismiss without undo keeps deletion permanent.
- No long-hold delete popup appears.

---

## Task 5 - Archive edit icon accent color

**Goal**
Ensure archive edit icon uses accent color distinct from normal text tone.

**Scope**
- `ui/archive/ArchiveScreen.kt` edit icon styling.

**Deliverables**
- Set edit icon tint to `MaterialTheme.colorScheme.primary`.

**Acceptance Criteria**
- Edit icon color is the current theme primary.
- Session title/body text color remains unchanged.

---

## Task 6 - Settings layout flow confirmation and cleanup

**Goal**
Enforce desired settings flow order and polish grouping.

**Scope**
- `ui/settings/SettingsScreen.kt`

**Deliverables**
- Keep section order exactly: Theme -> Language -> Text Size.
- Ensure section headers and spacing communicate a top-to-bottom flow.
- Remove any code/comments implying a different order.

**Acceptance Criteria**
- Screen renders in requested order on first load.
- No regressions in setting interactions.

---

## Task 7 - Launcher icon redesign (Ocean Dark primary)

**Goal**
Update launcher icon to match app style with Ocean Dark primary.

**Scope**
- Launcher icon resources only:
  - `res/mipmap-*/`
  - adaptive icon XML resources (`ic_launcher*` as applicable)
  - any foreground/background vector XML used by launcher icon

**Deliverables**
- Refresh launcher icon assets with dominant `OceanDarkColorScheme` primary tone.
- Keep adaptive icon compatibility and monochrome handling where present.
- Do not modify in-app toolbar/menu icons.

**Acceptance Criteria**
- New launcher icon appears on device/emulator after install/update.
- Icon remains legible on light/dark launcher backgrounds.
- In-app icons are unchanged.

---

## Parallelization Strategy

Run these in parallel after baseline sync:

- Parallel Group A: Task 1, Task 2, Task 3
- Parallel Group B: Task 5, Task 6, Task 7

Run sequentially:

- Task 4 should run after archive data-path verification (delete + undo safety).

---

## Suggested Agent Prompt Templates

Use one prompt per task to keep runs deterministic.

- **Template**
  - "Implement Task X from `docs/general_fixes_execution_plan.md`. Touch only listed scope files unless required by compiler errors. Satisfy all acceptance criteria. Keep changes minimal and idiomatic Kotlin/Compose. Summarize modified files and how each acceptance criterion is met."

---

## Final Verification Checklist

- IME Done adds item.
- Added item names keep user-entered casing (no forced sentence-case transform).
- Keyboard uses sentence-style capitalization behavior for item input.
- Case-insensitive duplicate names are not added.
- List item gap is `4.dp`.
- List horizontal side padding is `4.dp`.
- Archive sessions delete by swipe both directions with undo.
- Archive restoration checks for case-insensitive duplicates and does not restore duplicates.
- If all archived items are duplicates, no Snackbar appears (silent restoration).
- Archive edit icon tint is theme primary.
- Settings order is Theme -> Language -> Text Size.
- Launcher icon updated with Ocean Dark primary and app style coherence.
