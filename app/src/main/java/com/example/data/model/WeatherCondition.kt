package com.example.data.model

enum class WeatherCondition(val displayName: String) {
    CLEAR("Clear Sky"),
    CLOUDY("Cloudy"),
    RAIN("Rain"),
    STORM("Thunderstorm"),
    SNOW("Snow"),
    SUNSET_TWILIGHT("Sunset");

    companion object {
        fun fromWmoCode(code: Int, isDay: Boolean = true): WeatherCondition {
            return when (code) {
                0, 1 -> CLEAR
                2, 3, 45, 48 -> CLOUDY
                51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> RAIN
                71, 73, 75, 77, 85, 86 -> SNOW
                95, 96, 99 -> STORM
                else -> if (code in 50..69) RAIN else CLOUDY
            }
        }
    }
}

enum class TemperatureUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F");

    fun toDisplay(celsius: Double): String {
        val value = when (this) {
            CELSIUS -> celsius
            FAHRENHEIT -> (celsius * 9.0 / 5.0) + 32.0
        }
        return "${Math.round(value)}°"
    }

    fun toNumeric(celsius: Double): Int {
        return when (this) {
            CELSIUS -> Math.round(celsius).toInt()
            FAHRENHEIT -> Math.round((celsius * 9.0 / 5.0) + 32.0).toInt()
        }
    }
}

enum class DioramaTheme(val label: String, val subtitle: String) {
    MOUNTAIN_VIEW("Mountain View", "Suburban tech park, lush green lawns & backdrop hills"),
    TOKYO("Tokyo", "Dense mini skyline, Tokyo Tower pinnacle & cherry trees"),
    NEW_YORK("New York", "Miniature skyscrapers, Empire spire & yellow cabs"),
    CHENNAI("Chennai", "Coastal shoreline, Marina lighthouse & tropical palms"),
    BENGALURU("Bengaluru", "Silicon plateau, garden tree canopies & heritage arches"),
    MUMBAI("Mumbai", "Marine Drive crescent, Gateway arch & coastal waves"),
    LONDON("London", "Big Ben clock tower, stone bridges & red double-decker"),
    PARIS("Paris", "Eiffel spire miniature, mansard rooftops & Seine river"),
    COASTAL("Coastal Harbor", "Sandy shoreline, lighthouse & gentle ocean waves"),
    URBAN_METROPOLIS("Metropolis", "Modern high-rises, city avenues & streetlights"),
    MOUNTAIN_VALLEY("Mountain Valley", "Pine forests, rocky summits & alpine huts");

    companion object {
        fun fromLocation(cityName: String): DioramaTheme {
            val lower = cityName.lowercase().trim()
            return when {
                lower.contains("mountain view") || lower.contains("palo alto") || lower.contains("cupertino") -> MOUNTAIN_VIEW
                lower.contains("tokyo") || lower.contains("kyoto") || lower.contains("osaka") -> TOKYO
                lower.contains("new york") || lower.contains("nyc") || lower.contains("manhattan") -> NEW_YORK
                lower.contains("chennai") || lower.contains("madras") -> CHENNAI
                lower.contains("bengaluru") || lower.contains("bangalore") -> BENGALURU
                lower.contains("mumbai") || lower.contains("bombay") -> MUMBAI
                lower.contains("london") -> LONDON
                lower.contains("paris") -> PARIS
                lower.contains("beach") || lower.contains("coast") || lower.contains("miami") || lower.contains("sydney") || lower.contains("goa") -> COASTAL
                lower.contains("alps") || lower.contains("denver") || lower.contains("vancouver") || lower.contains("zurich") -> MOUNTAIN_VALLEY
                else -> URBAN_METROPOLIS
            }
        }
    }
}
