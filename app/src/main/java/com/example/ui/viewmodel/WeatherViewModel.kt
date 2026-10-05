package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.MiniWeatherDatabase
import com.example.data.location.LocationService
import com.example.data.model.LocationItem
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherData
import com.example.data.network.GeminiDioramaService
import com.example.data.network.GoogleMapsGroundingService
import com.example.data.network.LocalLandmarkInsight
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeatherUiState(
    val weather: WeatherData,
    val currentLocation: LocationItem = WeatherRepository.DEFAULT_LOCATION,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.FAHRENHEIT, // 75° default
    val searchQuery: String = "",
    val searchResults: List<LocationItem> = emptyList(),
    val recentLocations: List<LocationItem> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val isDetectingLocation: Boolean = false,
    val isTransitioning: Boolean = false,
    val transitionMessage: String? = null,
    val errorMessage: String? = null,
    val landmarkInsight: LocalLandmarkInsight? = null,
    val aiGeneratedBitmap: Bitmap? = null,
    val isGeneratingAiDiorama: Boolean = false,
    val aiGenerationError: String? = null,
    val geminiApiKey: String = BuildConfig.GEMINI_API_KEY
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MiniWeatherDatabase.getInstance(application)
    private val repository = WeatherRepository(database.locationDao())
    val locationService = LocationService(application)

    // Initial weather state per requirements:
    // Location: Mountain View, Condition: Clear Sky, Temperature: 75°, Date: Tuesday, May 19, 2026
    private val initialWeather = repository.getFallbackWeather(WeatherRepository.DEFAULT_LOCATION)

    private val _uiState = MutableStateFlow(
        WeatherUiState(
            weather = initialWeather,
            currentLocation = WeatherRepository.DEFAULT_LOCATION
        )
    )
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Observe recent locations from Room
        repository.recentLocations.onEach { recents ->
            _uiState.update { it.copy(recentLocations = recents) }
        }.launchIn(viewModelScope)

        // Load live weather & Maps Grounding landmarks for initial location
        loadWeather(WeatherRepository.DEFAULT_LOCATION)
        loadMapsGroundingLandmarks(WeatherRepository.DEFAULT_LOCATION)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()

        if (query.trim().isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            delay(280) // Debounce typing
            val results = repository.searchLocations(query)
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
        }
    }

    fun selectLocation(location: LocationItem) {
        // Step 1: Immediately show the selected location name and start emergence transition
        _uiState.update {
            it.copy(
                currentLocation = location,
                searchQuery = "",
                searchResults = emptyList(),
                aiGeneratedBitmap = null,
                isTransitioning = true,
                transitionMessage = "Materializing ${location.name}..."
            )
        }
        viewModelScope.launch {
            repository.saveLocation(location)
        }
        // Step 2 & 3: Retrieve weather and smoothly morph the environment
        loadWeather(location)
        loadMapsGroundingLandmarks(location)
    }

    fun useCurrentLocation() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDetectingLocation = true,
                    isTransitioning = true,
                    transitionMessage = "Accessing current location...",
                    errorMessage = null
                )
            }

            val result = locationService.getCurrentLocation()
            result.fold(
                onSuccess = { loc ->
                    _uiState.update { it.copy(isDetectingLocation = false) }
                    selectLocation(loc)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isDetectingLocation = false,
                            isTransitioning = false,
                            transitionMessage = null,
                            errorMessage = err.message ?: "Could not detect GPS location"
                        )
                    }
                }
            )
        }
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        _uiState.update { it.copy(temperatureUnit = unit) }
    }

    fun refreshWeather() {
        loadWeather(_uiState.value.currentLocation)
        loadMapsGroundingLandmarks(_uiState.value.currentLocation)
    }

    fun setCustomGeminiApiKey(key: String) {
        _uiState.update { it.copy(geminiApiKey = key) }
    }

    private fun loadMapsGroundingLandmarks(location: LocationItem) {
        viewModelScope.launch {
            val key = _uiState.value.geminiApiKey
            val insightResult = GoogleMapsGroundingService.getLocalLandmarksWithMapsGrounding(
                locationName = location.name,
                latitude = location.latitude,
                longitude = location.longitude,
                apiKey = key
            )
            insightResult.onSuccess { insight ->
                _uiState.update { it.copy(landmarkInsight = insight) }
            }
        }
    }

    fun generateAiDiorama() {
        val state = _uiState.value
        val city = state.currentLocation.name
        val condition = state.weather.condition.displayName
        val key = state.geminiApiKey

        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingAiDiorama = true, aiGenerationError = null) }
            val result = GeminiDioramaService.generateDioramaImage(
                cityName = city,
                weatherCondition = condition,
                apiKey = key
            )
            result.fold(
                onSuccess = { bitmap ->
                    _uiState.update {
                        it.copy(
                            aiGeneratedBitmap = bitmap,
                            isGeneratingAiDiorama = false,
                            aiGenerationError = null
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isGeneratingAiDiorama = false,
                            aiGenerationError = err.message ?: "Failed to generate AI diorama"
                        )
                    }
                }
            )
        }
    }

    fun resetToProceduralDiorama() {
        _uiState.update { it.copy(aiGeneratedBitmap = null) }
    }

    private fun loadWeather(location: LocationItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.fetchWeather(location)
            result.fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            weather = data,
                            currentLocation = location,
                            isLoading = false,
                            isTransitioning = false,
                            transitionMessage = null,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isTransitioning = false,
                            transitionMessage = null,
                            errorMessage = err.message ?: "Unable to fetch live weather"
                        )
                    }
                }
            )
        }
    }
}
