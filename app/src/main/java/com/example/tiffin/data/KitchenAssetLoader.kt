package com.example.tiffin.data

import android.content.Context
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KitchenAssetLoader(
    context: Context,
    private val assetName: String = "kitchens.json",
) {
    // applicationContext so we never hold on to an Activity.
    private val appContext = context.applicationContext

    /** Reads and parses the asset off the main thread. Throws [KitchenDataException] on any failure. */
    suspend fun load(): List<Kitchen> = withContext(Dispatchers.IO) {
        val text = try {
            appContext.assets.open(assetName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            throw KitchenDataException("Could not read $assetName", e)
        }
        parseKitchens(text)
    }
}