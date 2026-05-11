package com.sranker.shoppinglistmanager.widget

/**
 * Represents the two possible visual states of the shopping list status widget.
 *
 * [AllDone] — The list is empty or every item is checked off. Renders a light green circle.
 * [HasPending] — At least one item in the list is unchecked. Renders a red circle.
 */
enum class WidgetStatus {
    AllDone,
    HasPending;

    companion object {
        /**
         * Derives the correct [WidgetStatus] from raw item counts.
         *
         * @param totalItems Total number of items in the shopping list.
         * @param uncheckedItems Number of items that are not yet checked off.
         */
        fun from(totalItems: Int, uncheckedItems: Int): WidgetStatus =
            if (uncheckedItems > 0) HasPending else AllDone
    }
}
