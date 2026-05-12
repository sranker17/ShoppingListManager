package com.sranker.shoppinglistmanager.widget

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import dagger.hilt.android.qualifiers.ApplicationContext
import com.sranker.shoppinglistmanager.data.db.ShoppingItemDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recalculates the shopping-list status and pushes it to every active
 * instance of [StatusWidget] on the home screen.
 *
 * This class is the single source of truth for widget state updates.
 * It must be called whenever the shopping list changes (add, delete,
 * toggle, or archive) so the widget reflects the current reality
 * without polling the database itself.
 */
@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    private val itemDao: ShoppingItemDao
) {

    private val statusKey = stringPreferencesKey(PREF_KEY_WIDGET_STATUS)

    /**
     * Queries unchecked item count and updates all active [StatusWidget] instances.
     *
     * This is a suspend function so it runs on the caller's coroutine scope
     * and does not create its own threads.
     */
    suspend fun update() {
        // A single COUNT query avoids loading the entire list into memory.
        val all = itemDao.getAllForDuplicateCheck()
        val unchecked = all.count { !it.isPurchased }
        val newStatus = WidgetStatus.from(totalItems = all.size, uncheckedItems = unchecked)

        val manager = GlanceAppWidgetManager(context)
        val ids = manager.getGlanceIds(StatusWidget::class.java)

        // Push the new state to every widget instance currently pinned on a home screen.
        ids.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[statusKey] = newStatus.name
            }
            StatusWidget().update(context, glanceId)
        }
    }
}
