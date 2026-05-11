package com.sranker.shoppinglistmanager.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.sranker.shoppinglistmanager.widget.WidgetUpdater
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver entry point for the [StatusWidget].
 *
 * Annotated with [AndroidEntryPoint] so Hilt can inject [WidgetUpdater].
 * This lets us push real list-status data into the widget the moment it is
 * pinned on the home screen, rather than waiting for the first list mutation.
 *
 * The receiver's own coroutine scope uses [SupervisorJob] so a single failed
 * update does not cancel sibling launches.
 */
@AndroidEntryPoint
class StatusWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = StatusWidget()

    @Inject
    lateinit var widgetUpdater: WidgetUpdater

    // Lightweight scope tied to the receiver's lifetime.
    // GlanceAppWidgetReceiver already extends BroadcastReceiver, so
    // coroutines launched here complete before goAsync() returns.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Called by the system when:
     * - The widget is first added to the home screen.
     * - The system requests a periodic refresh (not used here; updatePeriodMillis = 0).
     *
     * Triggers a real-data update so the circle colour is correct from the
     * very first render, even before any list mutation has occurred.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        scope.launch { widgetUpdater.update() }
    }

    /**
     * Called when the last instance of the widget is removed from the home screen.
     * No cleanup required — Glance preferences are scoped to the widget ID and
     * are cleaned up automatically by the framework.
     */
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
    }
}
