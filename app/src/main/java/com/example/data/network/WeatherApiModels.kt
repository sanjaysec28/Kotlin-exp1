package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenMeteoWeatherResponse(
    @param:Json(name = "latitude") val latitude: Double? = null,
    @param:Json(name = "longitude") val longitude: Double? = null,
    @param:Json(name = "current") val current: OpenMeteoCurrent? = null,
    @param:Json(name = "hourly") val hourly: OpenMeteoHourly? = null,
    @param:Json(name = "daily") val daily: OpenMeteoDaily? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrent(
    @param:Json(name = "time") val time: String? = null,
    @param:Json(name = "temperature_2m") val temperature2m: Double? = null,
    @param:Json(name = "relative_humidity_2m") val relativeHumidity2m: Int? = null,
    @param:Json(name = "apparent_temperature") val apparentTemperature: Double? = null,
    @param:Json(name = "is_day") val isDay: Int? = null,
    @param:Json(name = "precipitation") val precipitation: Double? = null,
    @param:Json(name = "weather_code") val weatherCode: Int? = null,
    @param:Json(name = "wind_speed_10m") val windSpeed10m: Double? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoHourly(
    @param:Json(name = "time") val time: List<String>? = null,
    @param:Json(name = "temperature_2m") val temperature2m: List<Double>? = null,
    @param:Json(name = "weather_code") val weatherCode: List<Int>? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoDaily(
    @param:Json(name = "time") val time: List<String>? = null,
    @param:Json(name = "weather_code") val weatherCode: List<Int>? = null,
    @param:Json(name = "temperature_2m_max") val temperature2mMax: List<Double>? = null,
    @param:Json(name = "temperature_2m_min") val temperature2mMin: List<Double>? = null,
    @param:Json(name = "uv_index_max") val uvIndexMax: List<Double>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    @param:Json(name = "results") val results: List<GeocodingResult>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResult(
    @param:Json(name = "id") val id: Long? = null,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "latitude") val latitude: Double,
    @param:Json(name = "longitude") val longitude: Double,
    @param:Json(name = "country") val country: String? = null,
    @param:Json(name = "admin1") val admin1: String? = null
)
