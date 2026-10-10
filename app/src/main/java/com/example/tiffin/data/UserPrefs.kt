package com.example.tiffin.data

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persistent business state: launch count and the simulated purchase.
 * Backed by SharedPreferences; writes use commit() so they are on disk before we continue.
 * Use from the main thread only.
 */
class UserPrefs private constructor(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    // 0 means "never purchased"; otherwise the epoch millis of the simulated purchase.
    private val _purchasedAtMillis = MutableStateFlow(prefs.getLong(KEY_PURCHASED_AT, 0L))
    val purchasedAtMillis: StateFlow<Long> = _purchasedAtMillis.asStateFlow()

    val isPaid: Boolean get() = _purchasedAtMillis.value > 0L

    val launchCount: Int get() = prefs.getInt(KEY_LAUNCH_COUNT, 0)

    /** Call once per real launch. Returns the new launch count. */
    @SuppressLint("ApplySharedPref") // commit() on purpose: must be on disk before we continue
    fun recordLaunch(): Int {
        val next = launchCount + 1
        prefs.edit().putInt(KEY_LAUNCH_COUNT, next).commit()
        return next
    }

    /**
     * Records the simulated purchase. The first purchase wins: if already paid, nothing changes.
     * Returns true only if the purchase is now stored on disk.
     */
    @SuppressLint("ApplySharedPref")
    fun markPurchased(atMillis: Long): Boolean {
        if (isPaid) return true
        val written = prefs.edit().putLong(KEY_PURCHASED_AT, atMillis).commit()
        if (written) _purchasedAtMillis.value = atMillis
        return written
    }

    companion object {
        private const val FILE_NAME = "tiffin_prefs"
        private const val KEY_LAUNCH_COUNT = "launch_count"
        private const val KEY_PURCHASED_AT = "purchased_at_millis"

        @Volatile
        private var instance: UserPrefs? = null

        fun get(context: Context): UserPrefs =
            instance ?: synchronized(this) {
                instance ?: UserPrefs(context).also { instance = it }
            }
    }
}