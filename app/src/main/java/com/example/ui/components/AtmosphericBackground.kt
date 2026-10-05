package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.model.WeatherCondition
import com.example.ui.theme.ClearSkyDayBottom
import com.example.ui.theme.ClearSkyDayTop
import com.example.ui.theme.ClearSkyNightBottom
import com.example.ui.theme.ClearSkyNightTop
import com.example.ui.theme.CloudyDayBottom
import com.example.ui.theme.CloudyDayTop
import com.example.ui.theme.RainDayBottom
import com.example.ui.theme.RainDayTop
import com.example.ui.theme.SnowDayBottom
import com.example.ui.theme.SnowDayTop
import com.example.ui.theme.StormDayBottom
import com.example.ui.theme.StormDayTop
import com.example.ui.theme.SunsetBottom
import com.example.ui.theme.SunsetTop

@Composable
fun AtmosphericBackground(
    condition: WeatherCondition,
    isDay: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val (targetTop, targetBottom) = when (condition) {
        WeatherCondition.CLEAR -> {
            if (isDay) Pair(ClearSkyDayTop, ClearSkyDayBottom)
            else Pair(ClearSkyNightTop, ClearSkyNightBottom)
        }
        WeatherCondition.CLOUDY -> Pair(CloudyDayTop, CloudyDayBottom)
        WeatherCondition.RAIN -> Pair(RainDayTop, RainDayBottom)
        WeatherCondition.STORM -> Pair(StormDayTop, StormDayBottom)
        WeatherCondition.SNOW -> Pair(SnowDayTop, SnowDayBottom)
        WeatherCondition.SUNSET_TWILIGHT -> Pair(SunsetTop, SunsetBottom)
    }

    val animatedTop by animateColorAsState(
        targetValue = targetTop,
        animationSpec = tween(durationMillis = 800),
        label = "skyTop"
    )

    val animatedBottom by animateColorAsState(
        targetValue = targetBottom,
        animationSpec = tween(durationMillis = 800),
        label = "skyBottom"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(animatedTop, animatedBottom)
                )
            )
    ) {
        content()
    }
}
