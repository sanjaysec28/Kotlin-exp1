package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherData

/**
 * Minimalist, elegant weather display.
 * No cards. Crisp white typography hovering over the light sky.
 * Visual Hierarchy:
 * - Condition ("Clear Sky")
 * - Temperature ("75°") with compact °C | °F switcher
 * - Date ("Tuesday, May 19, 2026")
 */
@Composable
fun WeatherHeader(
    weather: WeatherData,
    temperatureUnit: TemperatureUnit,
    isUpdating: Boolean = false,
    onUnitToggle: (TemperatureUnit) -> Unit,
    onOpenAiSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top micro-indicator row (loading state or AI Diorama button)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isUpdating) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Updating...",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Minimal AI diorama icon button
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.28f),
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onOpenAiSheet)
                    .testTag("ai_diorama_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = "AI Diorama Options",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 1. Weather Condition Text (Clean, elegant, non-carded)
        AnimatedContent(
            targetState = weather.conditionText,
            transitionSpec = {
                (slideInVertically { it / 3 } + fadeIn(tween(350))) togetherWith
                        (slideOutVertically { -it / 3 } + fadeOut(tween(250)))
            },
            label = "conditionText"
        ) { conditionText ->
            Text(
                text = conditionText,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 24.sp,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.testTag("weather_condition_text")
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 2. Large Temperature Display with animated counter & adjacent compact unit toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = temperatureUnit.toDisplay(weather.currentTempC),
                transitionSpec = {
                    (slideInVertically { height -> height / 2 } + fadeIn(tween(350))) togetherWith
                            (slideOutVertically { height -> -height / 2 } + fadeOut(tween(250)))
                },
                label = "tempTransition"
            ) { displayTemp ->
                Text(
                    text = displayTemp,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 82.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        letterSpacing = (-2).sp
                    ),
                    modifier = Modifier.testTag("temperature_text")
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Minimal °C | °F switcher
            MinimalUnitToggle(
                currentUnit = temperatureUnit,
                onUnitSelected = onUnitToggle
            )
        }

        // 3. Current Date Text
        Text(
            text = weather.dateFormatted,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 14.sp
            ),
            modifier = Modifier.testTag("current_date_text")
        )
    }
}

@Composable
fun MinimalUnitToggle(
    currentUnit: TemperatureUnit,
    onUnitSelected: (TemperatureUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.25f),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .testTag("temperature_unit_toggle")
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UnitPillButton(
                label = "°C",
                isSelected = currentUnit == TemperatureUnit.CELSIUS,
                onClick = { onUnitSelected(TemperatureUnit.CELSIUS) }
            )
            UnitPillButton(
                label = "°F",
                isSelected = currentUnit == TemperatureUnit.FAHRENHEIT,
                onClick = { onUnitSelected(TemperatureUnit.FAHRENHEIT) }
            )
        }
    }
}

@Composable
private fun UnitPillButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) Color.White else Color.Transparent
    val textColor = if (isSelected) Color(0xFF1E3A8A) else Color.White

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                fontSize = 12.sp
            )
        )
    }
}
