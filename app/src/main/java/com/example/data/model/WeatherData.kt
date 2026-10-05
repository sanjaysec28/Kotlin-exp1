package com.example.data.model

data class WeatherData(
    val location: LocationItem,
    val currentTempC: Double,
    val feelsLikeC: Double,
    val highTempC: Double,
    val lowTempC: Double,
    val condition: WeatherCondition,
    val conditionText: String,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val uvIndex: Double,
    val isDay: Boolean = true,
    val dateFormatted: String,
    val hourlyForecast: List<HourlyForecast> = emptyList(),
    val dailyForecast: List<DailyForecast> = emptyList(),
    val aiGeneratedDioramaDescription: String? = null
)

data class HourlyForecast(
    val timeFormatted: String,
    val tempC: Double,
    val condition: WeatherCondition,
    val isNow: Boolean = false
)

data class DailyForecast(
    val dayName: String,
    val highTempC: Double,
    val lowTempC: Double,
    val condition: WeatherCondition
)
