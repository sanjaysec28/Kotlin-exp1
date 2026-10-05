package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.data.model.WeatherCondition
import kotlin.random.Random

data class WeatherParticle(
    val xRatio: Float,
    val yRatio: Float,
    val speed: Float,
    val length: Float,
    val alpha: Float,
    val size: Float = 2f
)

@Composable
fun WeatherParticleOverlay(
    condition: WeatherCondition,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "weatherParticles")

    val rainProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainProgress"
    )

    val snowProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snowProgress"
    )

    // Slow, serene atmospheric cloud drift
    val cloudsOffset by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 36000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloudsOffset"
    )

    // Gentle sun floating pulse
    val sunPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sunPulse"
    )

    // Lightning flash for storm
    val lightningAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lightningFlash"
    )

    val rainParticles = remember {
        val random = Random(42)
        List(60) {
            WeatherParticle(
                xRatio = random.nextFloat(),
                yRatio = random.nextFloat(),
                speed = 0.7f + random.nextFloat() * 0.6f,
                length = 14f + random.nextFloat() * 12f,
                alpha = 0.35f + random.nextFloat() * 0.35f,
                size = 1.4f + random.nextFloat() * 1.0f
            )
        }
    }

    val snowParticles = remember {
        val random = Random(88)
        List(45) {
            WeatherParticle(
                xRatio = random.nextFloat(),
                yRatio = random.nextFloat(),
                speed = 0.2f + random.nextFloat() * 0.25f,
                length = 0f,
                alpha = 0.4f + random.nextFloat() * 0.45f,
                size = 2.0f + random.nextFloat() * 2.5f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        when (condition) {
            WeatherCondition.CLEAR -> {
                // Subtle, elegant soft sun glow (NO giant spinning rays)
                drawSubtleSunGlow(w, h, sunPulse)
                drawSoftPuffyClouds(w, h, cloudsOffset, count = 2, opacity = 0.65f)
            }
            WeatherCondition.CLOUDY -> {
                drawSoftPuffyClouds(w, h, cloudsOffset, count = 5, opacity = 0.82f)
            }
            WeatherCondition.RAIN -> {
                drawSoftPuffyClouds(w, h, cloudsOffset * 0.6f, count = 3, opacity = 0.75f)
                drawSoftRain(w, h, rainParticles, rainProgress)
            }
            WeatherCondition.STORM -> {
                drawSoftPuffyClouds(w, h, cloudsOffset * 0.7f, count = 4, opacity = 0.85f, tint = Color(0xFF64748B))
                drawSoftRain(w, h, rainParticles, rainProgress * 1.3f)
                drawSubtleLightning(w, h, lightningAlpha)
            }
            WeatherCondition.SNOW -> {
                drawSoftPuffyClouds(w, h, cloudsOffset * 0.5f, count = 2, opacity = 0.6f)
                drawSoftSnow(w, h, snowParticles, snowProgress)
            }
            WeatherCondition.SUNSET_TWILIGHT -> {
                drawSunsetAtmosphere(w, h, sunPulse)
                drawSoftPuffyClouds(w, h, cloudsOffset * 0.5f, count = 2, opacity = 0.55f, tint = Color(0xFFFED7AA))
            }
        }
    }
}

private fun DrawScope.drawSubtleSunGlow(width: Float, height: Float, pulse: Float) {
    val sunX = width * 0.82f
    val sunY = height * 0.14f

    // Soft radiant aura
    drawCircle(
        color = Color(0x28FDE047),
        radius = 52f * pulse,
        center = Offset(sunX, sunY)
    )
    drawCircle(
        color = Color(0x44FBBF24),
        radius = 32f * pulse,
        center = Offset(sunX, sunY)
    )
    drawCircle(
        color = Color(0xFFFFFBEB),
        radius = 18f,
        center = Offset(sunX, sunY)
    )
}

private fun DrawScope.drawSoftRain(
    width: Float,
    height: Float,
    particles: List<WeatherParticle>,
    progress: Float
) {
    val slant = -4f
    particles.forEach { p ->
        val y = ((p.yRatio + progress * p.speed) % 1.0f) * height
        val x = p.xRatio * width + (y / height) * slant
        val start = Offset(x, y)
        val end = Offset(x + slant * 0.6f, y + p.length)

        drawLine(
            color = Color(0xFF93C5FD).copy(alpha = p.alpha),
            start = start,
            end = end,
            strokeWidth = p.size,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawSoftSnow(
    width: Float,
    height: Float,
    particles: List<WeatherParticle>,
    progress: Float
) {
    particles.forEach { p ->
        val y = ((p.yRatio + progress * p.speed) % 1.0f) * height
        val sway = kotlin.math.sin((progress * 6.28f + p.xRatio * 10f).toDouble()).toFloat() * 10f
        val x = p.xRatio * width + sway

        drawCircle(
            color = Color.White.copy(alpha = p.alpha),
            radius = p.size,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawSoftPuffyClouds(
    width: Float,
    height: Float,
    offsetNorm: Float,
    count: Int,
    opacity: Float,
    tint: Color = Color.White
) {
    for (i in 0 until count) {
        val baseX = ((offsetNorm + (i * 0.42f)) % 1.5f - 0.25f) * width
        val baseY = height * (0.04f + (i * 0.05f))
        val scale = 0.85f + (i % 3) * 0.25f

        val r1 = 20f * scale
        val r2 = 30f * scale
        val r3 = 18f * scale
        val cloudColor = tint.copy(alpha = opacity * 0.85f)

        drawCircle(color = cloudColor, radius = r1, center = Offset(baseX - 22f * scale, baseY))
        drawCircle(color = cloudColor, radius = r2, center = Offset(baseX, baseY - 10f * scale))
        drawCircle(color = cloudColor, radius = r3, center = Offset(baseX + 24f * scale, baseY))
        drawRoundRect(
            color = cloudColor,
            topLeft = Offset(baseX - 26f * scale, baseY - 4f * scale),
            size = Size(52f * scale, 18f * scale),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
        )
    }
}

private fun DrawScope.drawSubtleLightning(width: Float, height: Float, progress: Float) {
    val isFlashing = progress in 0.86f..0.91f
    if (isFlashing) {
        drawRect(
            color = Color.White.copy(alpha = 0.16f),
            size = size
        )
    }
}

private fun DrawScope.drawSunsetAtmosphere(width: Float, height: Float, pulse: Float) {
    val sunX = width * 0.5f
    val sunY = height * 0.24f

    drawCircle(
        color = Color(0x33F97316),
        radius = 80f * pulse,
        center = Offset(sunX, sunY)
    )
    drawCircle(
        color = Color(0x55FDBA74),
        radius = 45f * pulse,
        center = Offset(sunX, sunY)
    )
    drawCircle(
        color = Color(0xFFFFF7ED),
        radius = 24f,
        center = Offset(sunX, sunY)
    )
}
