package com.example.data.model

data class LocationItem(
    val id: String,
    val name: String,
    val country: String,
    val admin1: String? = null,
    val latitude: Double,
    val longitude: Double,
    val dioramaTheme: DioramaTheme = DioramaTheme.fromLocation(name)
) {
    val displaySubtitle: String
        get() = listOfNotNull(admin1, country).filter { it.isNotBlank() }.joinToString(", ")
}
