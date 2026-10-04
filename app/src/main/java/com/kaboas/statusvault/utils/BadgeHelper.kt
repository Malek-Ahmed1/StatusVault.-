package com.kaboas.statusvault.utils

import android.content.Context

object BadgeHelper {

    private const val PREFS = "badge_prefs"
    private const val KEY_LAST_SEEN = "last_seen_timestamp"

    fun getLastSeen(context: Context): Long {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_SEEN, 0L)
    }

    fun setLastSeen(context: Context, timestamp: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putLong(KEY_LAST_SEEN, timestamp).apply()
    }
}
