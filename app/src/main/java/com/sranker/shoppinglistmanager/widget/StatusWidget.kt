package com.sranker.shoppinglistmanager.widget

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.unit.ColorProvider
import com.sranker.shoppinglistmanager.MainActivity
import com.sranker.shoppinglistmanager.R
import com.sranker.shoppinglistmanager.ui.theme.WidgetAlertRed
import com.sranker.shoppinglistmanager.ui.theme.WidgetGreen

/** Key used to persist the widget status in Glance Preferences. */
internal const val PREF_KEY_WIDGET_STATUS = "widget_status"

/**
 * 1×1 home screen widget that displays a coloured circle indicating
 * whether all shopping list items have been checked off.
 *
 * State is persisted via [PreferencesGlanceStateDefinition] so the widget
 * survives process restarts and launcher refreshes without a DB round-trip.
 */
class StatusWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // Read persisted status from Glance Preferences (default = AllDone / green)
            val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
            val statusName = prefs[androidx.datastore.preferences.core.stringPreferencesKey(
                PREF_KEY_WIDGET_STATUS
            )] ?: WidgetStatus.AllDone.name
            val status = WidgetStatus.valueOf(statusName)

            val circleColor = when (status) {
                WidgetStatus.AllDone   -> ColorProvider(WidgetGreen)
                WidgetStatus.HasPending -> ColorProvider(WidgetAlertRed)
            }

            // Root container — dark background matching the app icon, rounded corners
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(androidx.compose.ui.graphics.Color(0xFF151515)))
                    .cornerRadius(16)
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.Center
            ) {
                // Circle outline tinted with the status color.
                // Padding ensures the stroke is not clipped by the widget boundary.
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_circle_outline),
                    contentDescription = null,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(10),
                    colorFilter = androidx.glance.ColorFilter.tint(circleColor)
                )
            }
        }
    }
}
