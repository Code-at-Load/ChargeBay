package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import com.example.MainActivity

class StandByPowerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (intent.action == Intent.ACTION_POWER_CONNECTED) {
            val orientation = context.resources.configuration.orientation
            // If phone is connected to charger in landscape (or docked), start StandBy mode
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("AUTO_LAUNCHED_BY_CHARGER", true)
                }
                context.startActivity(launchIntent)
            }
        }
    }
}
