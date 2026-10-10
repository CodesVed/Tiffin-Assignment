package com.example.tiffin.data

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Thrown when the bundled kitchen data cannot be read, parsed, or fails validation. */
class KitchenDataException(message: String, cause: Throwable? = null) : Exception(message, cause)

private val json = Json // default settings: strict, unknown keys are errors

/**
 * Pure function (no Android types) so it can be unit-tested on the JVM.
 * An empty array is valid and returns an empty list; the UI treats that as the "empty" state.
 */
fun parseKitchens(text: String): List<Kitchen> {
    val kitchens = try {
        json.decodeFromString(ListSerializer(Kitchen.serializer()), text)
    } catch (e: IllegalArgumentException) {
        // SerializationException (malformed JSON, missing fields, wrong types) extends
        // IllegalArgumentException, so this one catch covers all of them.
        throw KitchenDataException("Kitchen data is malformed: ${e.message}", e)
    }
    validate(kitchens)
    return kitchens
}

private fun validate(kitchens: List<Kitchen>) {
    val seenIds = HashSet<String>()
    for (k in kitchens) {
        fun fail(reason: String): Nothing =
            throw KitchenDataException("Invalid kitchen '${k.id}': $reason")

        if (k.id.isBlank()) fail("id is blank")
        if (!seenIds.add(k.id)) fail("duplicate id")
        if (k.name.isBlank()) fail("name is blank")
        if (k.pricePerTiffin <= 0) fail("price must be positive")
        if (k.rating !in 0.0..5.0) fail("rating must be between 0 and 5")
        if (k.distanceKm < 0) fail("distance cannot be negative")
        if (k.weeklyMenu.isEmpty()) fail("weekly menu is empty")
        if (k.weeklyMenu.any { it.items.isEmpty() }) fail("a menu day has no items")
    }
}