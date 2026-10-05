package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.data.model.DioramaTheme
import com.example.data.model.LocationItem
import com.example.data.repository.WeatherRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class LocationService(private val context: Context) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<LocationItem> = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext Result.failure(SecurityException("Location permission not granted"))
        }

        try {
            val location: Location? = try {
                suspendCancellableCoroutine { continuation ->
                    val cts = CancellationTokenSource()
                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                        .addOnSuccessListener { loc ->
                            continuation.resume(loc)
                        }
                        .addOnFailureListener {
                            continuation.resume(null)
                        }
                    continuation.invokeOnCancellation {
                        cts.cancel()
                    }
                }
            } catch (e: Exception) {
                null
            } ?: getFallbackSystemLocation()

            if (location != null) {
                val locationItem = reverseGeocode(location.latitude, location.longitude)
                Result.success(locationItem)
            } else {
                // If hardware sensor didn't return, return default Mountain View location with notice
                Result.success(
                    WeatherRepository.DEFAULT_LOCATION.copy(
                        name = "Mountain View (Default)",
                        country = "United States"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getFallbackSystemLocation(): Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        for (provider in providers) {
            try {
                val loc = lm.getLastKnownLocation(provider)
                if (loc != null) return loc
            } catch (_: Exception) {}
        }
        return null
    }

    private fun reverseGeocode(latitude: Double, longitude: Double): LocationItem {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var result: LocationItem? = null
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    val addr = addresses.firstOrNull()
                    if (addr != null) {
                        val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "My Location"
                        val country = addr.countryName ?: addr.countryCode
                        val admin = addr.adminArea
                        result = LocationItem(
                            id = "current_${latitude}_${longitude}",
                            name = city,
                            latitude = latitude,
                            longitude = longitude,
                            country = country,
                            admin1 = admin,
                            dioramaTheme = resolveDioramaTheme(city, latitude, longitude)
                        )
                    }
                }
                if (result != null) return result!!
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val addr = addresses?.firstOrNull()
                if (addr != null) {
                    val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "My Location"
                    val country = addr.countryName ?: addr.countryCode
                    val admin = addr.adminArea
                    return LocationItem(
                        id = "current_${latitude}_${longitude}",
                        name = city,
                        latitude = latitude,
                        longitude = longitude,
                        country = country,
                        admin1 = admin,
                        dioramaTheme = resolveDioramaTheme(city, latitude, longitude)
                    )
                }
            }
        } catch (_: Exception) {}

        // Fallback geocoding representation based on coordinate range
        val cityName = when {
            latitude in 37.0..38.0 && longitude in -123.0..-121.5 -> "Mountain View"
            latitude in 12.8..13.2 && longitude in 80.0..80.5 -> "Chennai"
            latitude in 12.8..13.2 && longitude in 77.4..77.8 -> "Bengaluru"
            latitude in 18.8..19.3 && longitude in 72.7..73.2 -> "Mumbai"
            latitude in 35.5..35.9 && longitude in 139.5..140.0 -> "Tokyo"
            latitude in 40.5..41.0 && longitude in -74.2..-73.7 -> "New York"
            latitude in 51.3..51.7 && longitude in -0.5..0.3 -> "London"
            latitude in 48.7..49.0 && longitude in 2.1..2.6 -> "Paris"
            else -> "Current Location"
        }

        return LocationItem(
            id = "current_${latitude}_${longitude}",
            name = cityName,
            latitude = latitude,
            longitude = longitude,
            country = "Current Region",
            admin1 = null,
            dioramaTheme = resolveDioramaTheme(cityName, latitude, longitude)
        )
    }

    private fun resolveDioramaTheme(name: String, latitude: Double, longitude: Double): DioramaTheme {
        val lower = name.lowercase(Locale.ROOT)
        return when {
            lower.contains("mountain view") || lower.contains("palo alto") || lower.contains("sunnyvale") || lower.contains("san jose") -> DioramaTheme.MOUNTAIN_VIEW
            lower.contains("chennai") || lower.contains("madras") -> DioramaTheme.CHENNAI
            lower.contains("bengaluru") || lower.contains("bangalore") -> DioramaTheme.BENGALURU
            lower.contains("mumbai") || lower.contains("bombay") -> DioramaTheme.MUMBAI
            lower.contains("tokyo") || lower.contains("shinjuku") || lower.contains("shibuya") -> DioramaTheme.TOKYO
            lower.contains("new york") || lower.contains("manhattan") || lower.contains("brooklyn") -> DioramaTheme.NEW_YORK
            lower.contains("london") || lower.contains("westminster") -> DioramaTheme.LONDON
            lower.contains("paris") -> DioramaTheme.PARIS
            // Coastal inference
            lower.contains("beach") || lower.contains("coast") -> DioramaTheme.COASTAL
            else -> DioramaTheme.fromLocation(name)
        }
    }
}
