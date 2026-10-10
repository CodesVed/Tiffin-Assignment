package com.example.tiffin.data

import kotlinx.serialization.Serializable

@Serializable
data class Kitchen(
    val id: String,
    val name: String,
    val cuisine: String,
    val pricePerTiffin: Int,
    val isVeg: Boolean,
    val rating: Double,
    val distanceKm: Double,
    val description: String,
    val weeklyMenu: List<DayMenu>,
)

@Serializable
data class DayMenu(
    val day: String,
    val items: List<String>,
)