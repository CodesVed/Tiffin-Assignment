package com.example.tiffin.data

import java.io.File
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class KitchenParserTest {

    // Gradle runs unit tests with the module directory (app/) as the working directory.
    private fun assetText(): String = File("src/main/assets/kitchens.json").readText()

    @Test
    fun bundledAsset_hasTwelveKitchensWithUniqueIdsAndSevenMenuDays() {
        val kitchens = parseKitchens(assetText())
        assertEquals(12, kitchens.size)
        assertEquals(12, kitchens.map { it.id }.toSet().size)
        kitchens.forEach { assertEquals("${it.name} menu days", 7, it.weeklyMenu.size) }
    }

    @Test
    fun vegKitchens_haveNoNonVegItemsInTheirMenus() {
        // Whole-word match: a plain substring check wrongly flags "veggies" (it contains "egg").
        val nonVeg = Regex(
            """\b(chicken|mutton|fish|eggs?|prawns?|kheema|macher)\b""",
            RegexOption.IGNORE_CASE,
        )
        parseKitchens(assetText()).filter { it.isVeg }.forEach { kitchen ->
            kitchen.weeklyMenu.flatMap { it.items }.forEach { item ->
                assertTrue(
                    "${kitchen.name} is veg but lists '$item'",
                    !nonVeg.containsMatchIn(item),
                )
            }
        }
    }

    @Test
    fun emptyArray_isValidAndReturnsEmptyList() {
        assertTrue(parseKitchens("[]").isEmpty())
    }

    @Test
    fun malformedJson_throwsKitchenDataException() {
        assertThrows(KitchenDataException::class.java) { parseKitchens("[{ not json") }
    }

    @Test
    fun missingRequiredField_throwsKitchenDataException() {
        assertThrows(KitchenDataException::class.java) {
            parseKitchens("""[{"id":"k1","name":"Only a name"}]""")
        }
    }

    @Test
    fun duplicateIds_areRejected() {
        val first = parseKitchens(assetText()).first()
        val duplicated = Json.encodeToString(ListSerializer(Kitchen.serializer()), listOf(first, first))
        assertThrows(KitchenDataException::class.java) { parseKitchens(duplicated) }
    }
}