package com.noviaralabs.ledger.data

import android.content.Context

/** Only thing stored on device: who logged in and when. Expires after 24 hours. */
class Session(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun save(userId: Long) {
        prefs.edit().putLong("userId", userId).putLong("loginAt", System.currentTimeMillis()).apply()
    }

    /** Returns the logged-in user id, or null if not logged in / expired (and clears it). */
    fun userId(): Long? {
        val id = prefs.getLong("userId", -1L)
        val at = prefs.getLong("loginAt", 0L)
        if (id != -1L && System.currentTimeMillis() - at < DAY_MS) return id
        clear()
        return null
    }

    fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val DAY_MS = 24L * 60 * 60 * 1000
    }
}
