package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HourlyForecast
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherCondition

@Composable
fun HourlyForecastStrip(
    hourlyList: List<HourlyForecast>,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hourly_forecast_strip")
        ) {
            items(hourlyList) { hour ->
                HourlyCard(hour = hour, unit = unit)
            }
        }
    }
}

@Composable
private fun HourlyCard(
    hour: HourlyForecast,
    unit: TemperatureUnit
) {
    val bgColor = if (hour.isNow) Color(0xFFE0F2FE) else Color(0xFFF1F5F9)
    val borderColor = if (hour.isNow) Color(0xFF38BDF8) else Color(0xFFE2E8F0)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        modifier = Modifier
            .width(66.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = hour.timeFormatted,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF64748B),
                    fontWeight = if (hour.isNow) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Icon(
                imageVector = getConditionIcon(hour.condition),
                contentDescription = hour.condition.displayName,
                tint = if (hour.condition == WeatherCondition.CLEAR) Color(0xFFF59E0B) else Color(0xFF0284C7),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = unit.toDisplay(hour.tempC),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A),
                    fontSize = 14.sp
                )
            )
        }
    }
}

fun getConditionIcon(condition: WeatherCondition): ImageVector {
    return when (condition) {
        WeatherCondition.CLEAR -> Icons.Outlined.WbSunny
        WeatherCondition.CLOUDY -> Icons.Outlined.Cloud
        WeatherCondition.RAIN -> Icons.Outlined.Grain
        WeatherCondition.STORM -> Icons.Outlined.Thunderstorm
        WeatherCondition.SNOW -> Icons.Outlined.AcUnit
        WeatherCondition.SUNSET_TWILIGHT -> Icons.Outlined.WbTwilight
    }
}
