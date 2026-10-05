package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.WeatherRepository
import com.example.ui.components.AtmosphericBackground
import com.example.ui.components.GeminiAiDioramaSheet
import com.example.ui.components.HourlyForecastStrip
import com.example.ui.components.LocationSearchBar
import com.example.ui.components.MiniatureDioramaCanvas
import com.example.ui.components.WeatherHeader
import com.example.ui.components.WeatherMetricsPills
import com.example.ui.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val weather = uiState.weather

    var showAiSheet by remember { mutableStateOf(false) }
    var showDetailsSheet by remember { mutableStateOf(false) }

    val aiSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val detailsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Location Permission Request Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.useCurrentLocation()
        }
    }

    val onUseCurrentLocationClick: () -> Unit = {
        if (viewModel.locationService.hasLocationPermission()) {
            viewModel.useCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    AtmosphericBackground(
        condition = weather.condition,
        isDay = weather.isDay,
        modifier = modifier
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBarPadding, bottom = navBarPadding)
        ) {
            val totalHeight = maxHeight
            // The 3D miniature world occupies 58% of the screen height
            val dioramaHeight = (totalHeight * 0.58f).coerceAtLeast(360.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ==========================================
                // TOP & CENTER (~42%): CLEAN, AIRY WEATHER
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Floating Search Pill with GPS current location option
                    LocationSearchBar(
                        query = uiState.searchQuery,
                        currentLocationName = uiState.currentLocation.name,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        searchResults = uiState.searchResults,
                        featuredLocations = WeatherRepository.FEATURED_LOCATIONS,
                        isSearching = uiState.isSearching,
                        isDetectingLocation = uiState.isDetectingLocation,
                        onLocationSelected = viewModel::selectLocation,
                        onUseCurrentLocation = onUseCurrentLocationClick
                    )

                    // Error banner if any offline issue
                    if (uiState.errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.88f),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(onClick = viewModel::refreshWeather)
                                .testTag("retry_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.errorMessage ?: "Notice • Tap to retry",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF1E293B),
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retry",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Weather Information: Condition -> Large Temperature + °C/°F -> Date
                    WeatherHeader(
                        weather = weather,
                        temperatureUnit = uiState.temperatureUnit,
                        isUpdating = uiState.isTransitioning || uiState.isLoading || uiState.isDetectingLocation,
                        onUnitToggle = viewModel::setTemperatureUnit,
                        onOpenAiSheet = { showAiSheet = true }
                    )
                }

                // ==========================================
                // LOWER 58%: THE 3D MINIATURE HERO ENVIRONMENT
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dioramaHeight)
                        .testTag("miniature_diorama_hero_container"),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Smooth Emergence Transition: Combined cross-fade and scale-up from background
                    AnimatedContent(
                        targetState = uiState.currentLocation,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)) +
                             scaleIn(
                                 initialScale = 0.84f,
                                 animationSpec = spring(
                                     dampingRatio = 0.82f,
                                     stiffness = 220f
                                 )
                             )) togetherWith
                            (fadeOut(animationSpec = tween(durationMillis = 400, easing = FastOutLinearInEasing)) +
                             scaleOut(
                                 targetScale = 0.95f,
                                 animationSpec = tween(durationMillis = 400, easing = FastOutLinearInEasing)
                             ))
                        },
                        label = "dioramaSceneEmergenceTransition",
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("miniature_diorama_canvas")
                    ) { targetLocation ->
                        MiniatureDioramaCanvas(
                            theme = targetLocation.dioramaTheme,
                            condition = weather.condition,
                            isDay = weather.isDay,
                            aiGeneratedBitmap = uiState.aiGeneratedBitmap,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Small elegant loading indicator (never blocks screen)
                    if (uiState.isTransitioning || uiState.isLoading || uiState.isDetectingLocation) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.92f),
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(13.dp),
                                    color = Color(0xFF0284C7),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.transitionMessage ?: "Materializing miniature world...",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF1E293B),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // Subtle Bottom Floating Glass Pill (Weather details, Maps Grounding & hourly on tap)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .clickable { showDetailsSheet = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💧 ${weather.humidityPercent}%  •  💨 ${weather.windSpeedKmh.toInt()} km/h  •  📍 Explore landmarks & forecast",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Minimal Bottom Sheet for Forecast & Maps Grounding Details
        if (showDetailsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showDetailsSheet = false },
                sheetState = detailsSheetState,
                containerColor = Color.White.copy(alpha = 0.98f),
                contentColor = Color(0xFF0F172A)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = uiState.currentLocation.name,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            )
                            Text(
                                text = "${uiState.currentLocation.country ?: ""} • ${weather.conditionText}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF64748B)
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE0F2FE),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showDetailsSheet = false
                                    onUseCurrentLocationClick()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Current GPS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF0284C7),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    WeatherMetricsPills(
                        weather = weather,
                        unit = uiState.temperatureUnit
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Hourly Outlook",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    HourlyForecastStrip(
                        hourlyList = weather.hourlyForecast,
                        unit = uiState.temperatureUnit
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Google Maps Grounding Card (gemini-3.5-flash with googleMaps tool)
                    uiState.landmarkInsight?.let { insight ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE0F2FE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Map,
                                                contentDescription = null,
                                                tint = Color(0xFF0284C7),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = insight.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFDCFCE7)
                                    ) {
                                        Text(
                                            text = insight.sourceTitle,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF15803D),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = insight.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF475569),
                                        lineHeight = 18.sp
                                    )
                                )

                                if (insight.landmarkHighlights.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Notable Geographical Landmarks:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF0F172A),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    insight.landmarkHighlights.forEach { landmark ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Place,
                                                contentDescription = null,
                                                tint = Color(0xFF0284C7),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = landmark,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFF334155),
                                                    fontSize = 12.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Gemini AI Diorama Modal Sheet
        if (showAiSheet) {
            GeminiAiDioramaSheet(
                sheetState = aiSheetState,
                currentLocation = uiState.currentLocation,
                isGenerating = uiState.isGeneratingAiDiorama,
                hasGeneratedImage = uiState.aiGeneratedBitmap != null,
                geminiApiKey = uiState.geminiApiKey,
                onApiKeyChange = viewModel::setCustomGeminiApiKey,
                onGenerateClicked = viewModel::generateAiDiorama,
                onResetToProcedural = {
                    viewModel.resetToProceduralDiorama()
                    showAiSheet = false
                },
                onDismiss = { showAiSheet = false }
            )
        }
    }
}
