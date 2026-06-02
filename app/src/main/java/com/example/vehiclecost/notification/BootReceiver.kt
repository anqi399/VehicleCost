package com.example.vehiclecost.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.example.vehiclecost.data.repository.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            runBlocking {
                val enabled = context.dataStore.data.first()[booleanPreferencesKey("reminder_enabled")] ?: false
                if (enabled) {
                    ReminderScheduler.scheduleDailyReminder(context)
                }
            }
        }
    }
}
