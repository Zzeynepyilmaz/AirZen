package com.example.airzen.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.airzen.MainActivity
import com.example.airzen.R

class AirQualityWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val prefs = context.getSharedPreferences("airzen_widget", Context.MODE_PRIVATE)
            val message = prefs.getString("message", "Veri yok")
            val value = prefs.getString("value", "PM2.5: ? µg/m³")
            val iconRes = prefs.getInt("icon", R.drawable.outline_warning_24)

            val views = RemoteViews(context.packageName, R.layout.widget_air_quality)
            views.setTextViewText(R.id.widget_message, message)
            views.setTextViewText(R.id.widget_value, value)
            views.setImageViewResource(R.id.widget_icon, iconRes)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context, message: String, value: String, iconResId: Int) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, AirQualityWidget::class.java)
            )
            for (widgetId in widgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_air_quality)
                views.setTextViewText(R.id.widget_message, message)
                views.setTextViewText(R.id.widget_value, value)
                views.setImageViewResource(R.id.widget_icon, iconResId)
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }
}