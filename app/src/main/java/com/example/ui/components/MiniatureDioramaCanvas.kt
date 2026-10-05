package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.data.model.DioramaTheme
import com.example.data.model.WeatherCondition
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Premium 3D Miniature Weather Diorama Canvas.
 * - Photographed from an elevated 3/4 tilt perspective.
 * - Complete living miniature landscape that seamlessly blends into the sky.
 * - Absolutely NO green oval platform.
 * - Detailed miniature buildings, houses, roads with lane markings, micro-cars,
 *   trees, mountains, and realistic directional miniature lighting.
 */
@Composable
fun MiniatureDioramaCanvas(
    theme: DioramaTheme,
    condition: WeatherCondition,
    isDay: Boolean = true,
    aiGeneratedBitmap: Bitmap? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val tiltX = remember { Animatable(0f) }
    val tiltY = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "miniatureWorldMotion")

    // Slow ambient micro-motion
    val vehicleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vehicleProgress"
    )

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waves"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            tiltX.animateTo(0f, spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        coroutineScope.launch {
                            tiltY.animateTo(0f, spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch { tiltX.animateTo(0f) }
                        coroutineScope.launch { tiltY.animateTo(0f) }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newX = (tiltX.value + dragAmount.x * 0.08f).coerceIn(-12f, 12f)
                        val newY = (tiltY.value - dragAmount.y * 0.08f).coerceIn(-8f, 8f)
                        coroutineScope.launch { tiltX.snapTo(newX) }
                        coroutineScope.launch { tiltY.snapTo(newY) }
                    }
                )
            }
            .graphicsLayer {
                rotationY = tiltX.value
                rotationX = tiltY.value
                cameraDistance = 18f * density
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        if (aiGeneratedBitmap != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .shadow(20.dp, RoundedCornerShape(32.dp))
            ) {
                Image(
                    bitmap = aiGeneratedBitmap.asImageBitmap(),
                    contentDescription = "3D Miniature Diorama of ${theme.label}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 1. Natural terraced terrain foundation (blends seamlessly into horizon)
                drawNaturalTerrainBase(w, h, theme, condition)

                // 2. Location-specific miniature world
                when (theme) {
                    DioramaTheme.MOUNTAIN_VIEW -> drawMountainViewWorld(w, h, condition, isDay, vehicleProgress)
                    DioramaTheme.CHENNAI -> drawChennaiWorld(w, h, condition, isDay, waveOffset, vehicleProgress)
                    DioramaTheme.BENGALURU -> drawBengaluruWorld(w, h, condition, isDay, vehicleProgress)
                    DioramaTheme.MUMBAI -> drawMumbaiWorld(w, h, condition, isDay, waveOffset, vehicleProgress)
                    DioramaTheme.TOKYO -> drawTokyoWorld(w, h, condition, isDay, vehicleProgress)
                    DioramaTheme.NEW_YORK -> drawNewYorkWorld(w, h, condition, isDay, vehicleProgress)
                    DioramaTheme.LONDON -> drawLondonWorld(w, h, condition, isDay, vehicleProgress)
                    DioramaTheme.PARIS -> drawParisWorld(w, h, condition, isDay, vehicleProgress)
                    else -> drawGenericWorld(w, h, condition, isDay, vehicleProgress)
                }

                // 3. Environmental weather overlay on terrain
                drawTerrainWeatherReaction(w, h, condition)

                // 4. Subtle tilt-shift atmospheric depth-of-field
                drawAtmosphericDepthGradation(w, h)
            }
        }

        // Ambient weather particle effects (soft clouds, fine rain, snow drift)
        WeatherParticleOverlay(
            condition = condition,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// -------------------------------------------------------------
// 1. NATURAL FULL-WIDTH BLENDED TERRAIN (NO GREEN OVAL)
// -------------------------------------------------------------
private fun DrawScope.drawNaturalTerrainBase(
    width: Float,
    height: Float,
    theme: DioramaTheme,
    condition: WeatherCondition
) {
    val horizonY = height * 0.42f
    val baseGrassColor = when (condition) {
        WeatherCondition.SNOW -> Color(0xFFF1F5F9)
        WeatherCondition.SUNSET_TWILIGHT -> Color(0xFFE2E8F0)
        else -> Color(0xFFDCFCE7) // Soft pale pastel grass
    }

    val foregroundColor = when (condition) {
        WeatherCondition.SNOW -> Color(0xFFE2E8F0)
        WeatherCondition.SUNSET_TWILIGHT -> Color(0xFFFDE68A)
        else -> Color(0xFFBBF7D0) // Rich natural green foreground
    }

    // Distant soft hill slope blending into the sky
    val hillPath = Path().apply {
        moveTo(0f, horizonY + 30f)
        cubicTo(width * 0.35f, horizonY - 15f, width * 0.65f, horizonY + 25f, width, horizonY)
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }

    drawPath(
        path = hillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                baseGrassColor.copy(alpha = 0.55f),
                baseGrassColor,
                foregroundColor
            ),
            startY = horizonY - 20f,
            endY = height
        )
    )
}

// -------------------------------------------------------------
// 2. LOCATION-SPECIFIC MINIATURE WORLDS
// -------------------------------------------------------------

// =================== MOUNTAIN VIEW ===================
private fun DrawScope.drawMountainViewWorld(
    w: Float,
    h: Float,
    condition: WeatherCondition,
    isDay: Boolean,
    progress: Float
) {
    val baseY = h * 0.48f

    // 1. Distant Santa Cruz Mountain range silhouettes in soft blue haze
    drawDistantMountain(0f, w * 0.6f, baseY - 35f, 55f, Color(0xFF94A3B8).copy(alpha = 0.45f))
    drawDistantMountain(w * 0.4f, w, baseY - 30f, 60f, Color(0xFF64748B).copy(alpha = 0.40f))

    // 2. Curved suburban roads with crisp markings
    val mainRoad = Path().apply {
        moveTo(0f, baseY + 60f)
        cubicTo(w * 0.35f, baseY + 45f, w * 0.65f, baseY + 85f, w, baseY + 65f)
    }
    drawPath(mainRoad, Color(0xFF475569), style = Stroke(width = 24f, cap = StrokeCap.Round))
    drawPath(mainRoad, Color(0xFFE2E8F0), style = Stroke(width = 2f, cap = StrokeCap.Round)) // White center line

    // Cross street
    val crossRoad = Path().apply {
        moveTo(w * 0.28f, baseY + 20f)
        lineTo(w * 0.32f, baseY + 120f)
    }
    drawPath(crossRoad, Color(0xFF475569), style = Stroke(width = 16f, cap = StrokeCap.Round))

    // 3. Tiny animated electric vehicle on road
    val carT = (progress * 1.1f) % 1.0f
    val carX = w * 0.08f + carT * (w * 0.84f)
    val carY = baseY + 58f + sin(carT * 3.14f) * 12f
    drawMicroCar(carX, carY, Color(0xFF38BDF8), isDay)

    // 4. Silicon Valley Modern Corporate Pavilions (Center-Left)
    drawModernBuilding(
        x = w * 0.42f,
        y = baseY + 42f,
        width = 58f,
        depth = 42f,
        height = 54f,
        roofColor = Color(0xFF0284C7), // Blue photovoltaic solar array
        facadeColor = Color(0xFFE0F2FE),
        sideColor = Color(0xFFBAE6FD),
        condition = condition,
        isDay = isDay
    )

    // Tech Studio 2
    drawModernBuilding(
        x = w * 0.64f,
        y = baseY + 52f,
        width = 52f,
        depth = 38f,
        height = 42f,
        roofColor = Color(0xFFF8FAFC),
        facadeColor = Color(0xFFF1F5F9),
        sideColor = Color(0xFFCBD5E1),
        condition = condition,
        isDay = isDay
    )

    // 5. Suburban Houses with pitched roofs (California Bungalows)
    drawSuburbanHouse(w * 0.12f, baseY + 36f, 32f, 26f, Color(0xFFF1F5F9), Color(0xFF64748B))
    drawSuburbanHouse(w * 0.22f, baseY + 28f, 30f, 24f, Color(0xFFFEF3C7), Color(0xFF9A3412))
    drawSuburbanHouse(w * 0.82f, baseY + 48f, 34f, 26f, Color(0xFFFFFFFF), Color(0xFF334155))

    // 6. Lush California Oak & Pine trees
    drawPuffyMiniTree(w * 0.06f, baseY + 20f, 15f, Color(0xFF16A34A))
    drawPuffyMiniTree(w * 0.18f, baseY + 14f, 14f, Color(0xFF15803D))
    drawPuffyMiniTree(w * 0.38f, baseY + 25f, 16f, Color(0xFF16A34A))
    drawPuffyMiniTree(w * 0.58f, baseY + 30f, 15f, Color(0xFF15803D))
    drawPuffyMiniTree(w * 0.78f, baseY + 35f, 17f, Color(0xFF16A34A))
    drawPuffyMiniTree(w * 0.92f, baseY + 40f, 13f, Color(0xFF22C55E))
}

// =================== CHENNAI ===================
private fun DrawScope.drawChennaiWorld(
    w: Float,
    h: Float,
    condition: WeatherCondition,
    isDay: Boolean,
    waveOffset: Float,
    progress: Float
) {
    val baseY = h * 0.48f

    // 1. Coastal Bay of Bengal turquoise waters along right side
    val oceanPath = Path().apply {
        moveTo(w * 0.62f, baseY - 15f)
        cubicTo(w * 0.72f, baseY + 40f + sin(waveOffset) * 4f, w * 0.68f, baseY + 100f, w, baseY + 90f)
        lineTo(w, h)
        lineTo(w * 0.62f, h)
        close()
    }
    drawPath(
        oceanPath,
        Brush.horizontalGradient(
            colors = listOf(Color(0xFF06B6D4), Color(0xFF0284C7)),
            startX = w * 0.6f,
            endX = w
        )
    )

    // Sandy Shoreline strip
    val beachPath = Path().apply {
        moveTo(w * 0.56f, baseY - 15f)
        lineTo(w * 0.64f, h)
    }
    drawPath(beachPath, Color(0xFFFDE047), style = Stroke(width = 18f, cap = StrokeCap.Round))

    // 2. Marina Beach coastal road
    val coastalRoad = Path().apply {
        moveTo(0f, baseY + 75f)
        lineTo(w * 0.58f, baseY + 75f)
    }
    drawPath(coastalRoad, Color(0xFF334155), style = Stroke(width = 24f, cap = StrokeCap.Round))
    drawPath(coastalRoad, Color(0xFFFACC15), style = Stroke(width = 2f, cap = StrokeCap.Round))

    // Micro auto-rickshaw / car moving along beach road
    val autoX = (progress * 1.3f % 1.0f) * (w * 0.52f)
    drawMicroCar(autoX, baseY + 73f, Color(0xFFFACC15), isDay)

    // 3. Chennai Urban Architecture (Pastel multi-story flats with balconies)
    drawModernBuilding(
        x = w * 0.10f,
        y = baseY + 45f,
        width = 48f,
        depth = 36f,
        height = 62f,
        roofColor = Color(0xFFEA580C),
        facadeColor = Color(0xFFFEF3C7),
        sideColor = Color(0xFFFDE68A),
        condition = condition,
        isDay = isDay
    )

    drawModernBuilding(
        x = w * 0.28f,
        y = baseY + 52f,
        width = 54f,
        depth = 38f,
        height = 70f,
        roofColor = Color(0xFF0284C7),
        facadeColor = Color(0xFFE0F2FE),
        sideColor = Color(0xFFBAE6FD),
        condition = condition,
        isDay = isDay
    )

    // 4. Subtle traditional Tamil temple gopuram silhouette in background
    val gopuramX = w * 0.44f
    val gopuramY = baseY + 20f
    val gopuramPath = Path().apply {
        moveTo(gopuramX - 16f, gopuramY)
        lineTo(gopuramX - 6f, gopuramY - 48f)
        lineTo(gopuramX + 6f, gopuramY - 48f)
        lineTo(gopuramX + 16f, gopuramY)
        close()
    }
    drawPath(gopuramPath, Color(0xFFF59E0B))
    drawCircle(Color(0xFFD97706), radius = 5f, center = Offset(gopuramX, gopuramY - 52f))

    // 5. Tropical Coconut Palms
    drawMiniaturePalm(w * 0.05f, baseY + 18f, 38f)
    drawMiniaturePalm(w * 0.22f, baseY + 12f, 34f)
    drawMiniaturePalm(w * 0.52f, baseY + 28f, 36f)
    drawMiniaturePalm(w * 0.55f, baseY + 80f, 32f)
}

// =================== BENGALURU ===================
private fun DrawScope.drawBengaluruWorld(
    w: Float,
    h: Float,
    condition: WeatherCondition,
    isDay: Boolean,
    progress: Float
) {
    val baseY = h * 0.48f

    // 1. City Ring Road & Elevated Metro Viaduct
    val road = Path().apply {
        moveTo(0f, baseY + 70f)
        cubicTo(w * 0.35f, baseY + 60f, w * 0.65f, baseY + 80f, w, baseY + 68f)
    }
    drawPath(road, Color(0xFF334155), style = Stroke(width = 24f, cap = StrokeCap.Round))
    drawPath(road, Color.White, style = Stroke(width = 2f, cap = StrokeCap.Round))

    // Elevated Metro Track
    val metroTrack = Path().apply {
        moveTo(0f, baseY + 25f)
        lineTo(w, baseY + 25f)
    }
    drawPath(metroTrack, Color(0xFF94A3B8), style = Stroke(width = 7f, cap = StrokeCap.Round))

    // Moving Namma Metro coach
    val trainX = (progress % 1f) * (w - 40f)
    drawRoundRect(
        Color(0xFF818CF8),
        Offset(trainX, baseY + 19f),
        Size(36f, 10f),
        CornerRadius(3f, 3f)
    )

    // Moving Car on Road
    val carX = (progress * 1.2f % 1f) * (w - 20f)
    drawMicroCar(carX, baseY + 68f, Color(0xFF38BDF8), isDay)

    // 2. Glass IT Park Towers (Electronic City / Whitefield style)
    drawModernBuilding(
        x = w * 0.12f,
        y = baseY + 48f,
        width = 52f,
        depth = 38f,
        height = 80f,
        roofColor = Color(0xFF0284C7),
        facadeColor = Color(0xFFBAE6FD),
        sideColor = Color(0xFF7DD3FC),
        condition = condition,
        isDay = isDay
    )

    // Modern Apartment Complex
    drawModernBuilding(
        x = w * 0.38f,
        y = baseY + 58f,
        width = 56f,
        depth = 40f,
        height = 68f,
        roofColor = Color(0xFF475569),
        facadeColor = Color(0xFFF1F5F9),
        sideColor = Color(0xFFCBD5E1),
        condition = condition,
        isDay = isDay
    )

    // Red Brick Heritage Building (Vidhana Soudha homage)
    drawModernBuilding(
        x = w * 0.66f,
        y = baseY + 46f,
        width = 62f,
        depth = 42f,
        height = 48f,
        roofColor = Color(0xFF991B1B),
        facadeColor = Color(0xFFDC2626),
        sideColor = Color(0xFFB91C1C),
        condition = condition,
        isDay = isDay
    )
    drawCircle(Color(0xFFFDE047), radius = 10f, center = Offset(w * 0.66f + 31f, baseY - 7f))

    // 3. Garden City Flowering Canopies (Jacaranda purple & Gulmohar red)
    drawPuffyMiniTree(w * 0.05f, baseY + 18f, 18f, Color(0xFF15803D))
    drawPuffyMiniTree(w * 0.30f, baseY + 14f, 17f, Color(0xFFA855F7)) // Jacaranda
    drawPuffyMiniTree(w * 0.60f, baseY + 20f, 16f, Color(0xFFEF4444)) // Gulmohar
    drawPuffyMiniTree(w * 0.88f, baseY + 28f, 18f, Color(0xFF15803D))
}

// =================== MUMBAI ===================
private fun DrawScope.drawMumbaiWorld(
    w: Float,
    h: Float,
    condition: WeatherCondition,
    isDay: Boolean,
    waveOffset: Float,
    progress: Float
) {
    val baseY = h * 0.48f

    // Coastal Arabian Sea water along bottom
    val seaPath = Path().apply {
        moveTo(0f, baseY + 80f)
        cubicTo(w * 0.4f, baseY + 95f + sin(waveOffset) * 4f, w * 0.7f, baseY + 75f, w, baseY + 85f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        seaPath,
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0284C7), Color(0xFF0C4A6E)),
            startY = baseY + 80f,
            endY = h
        )
    )

    // Marine Drive Coastal Promenade curve
    val marineDrive = Path().apply {
        moveTo(0f, baseY + 68f)
        cubicTo(w * 0.4f, baseY + 82f, w * 0.7f, baseY + 64f, w, baseY + 72f)
    }
    drawPath(marineDrive, Color(0xFF1E293B), style = Stroke(width = 24f, cap = StrokeCap.Round))
    drawPath(marineDrive, Color(0xFFF8FAFC), style = Stroke(width = 4f, cap = StrokeCap.Round))

    // Moving yellow/black Mumbai taxi
    val taxiX = (progress * 1.2f % 1f) * (w - 20f)
    val taxiY = baseY + 66f + sin((progress * 1.2f % 1f) * 3.14f) * 10f
    drawMicroCar(taxiX, taxiY, Color(0xFFFACC15), isDay)

    // Dense High-Rise Towers & Gateway Monument
    drawModernBuilding(
        x = w * 0.12f,
        y = baseY + 45f,
        width = 44f,
        depth = 34f,
        height = 88f,
        roofColor = Color(0xFF475569),
        facadeColor = Color(0xFFE2E8F0),
        sideColor = Color(0xFF94A3B8),
        condition = condition,
        isDay = isDay
    )

    drawModernBuilding(
        x = w * 0.32f,
        y = baseY + 52f,
        width = 48f,
        depth = 36f,
        height = 96f,
        roofColor = Color(0xFF0284C7),
        facadeColor = Color(0xFFBAE6FD),
        sideColor = Color(0xFF64748B),
        condition = condition,
        isDay = isDay
    )

    // Gateway Arch Monument (Center-Right)
    val gwX = w * 0.62f
    val gwY = baseY + 50f
    drawRoundRect(
        Color(0xFFF59E0B),
        Offset(gwX - 22f, gwY - 48f),
        Size(44f, 48f),
        CornerRadius(4f, 4f)
    )
    drawRoundRect(
        Color(0xFF78350F),
        Offset(gwX - 9f, gwY - 32f),
        Size(18f, 32f),
        CornerRadius(7f, 7f)
    )

    drawModernBuilding(
        x = w * 0.78f,
        y = baseY + 48f,
        width = 46f,
        depth = 36f,
        height = 76f,
        roofColor = Color(0xFF334155),
        facadeColor = Color(0xFFCBD5E1),
        sideColor = Color(0xFF94A3B8),
        condition = condition,
        isDay = isDay
    )
}

// =================== TOKYO ===================
private fun DrawScope.drawTokyoWorld(
    w: Float,
    h: Float,
    condition: WeatherCondition,
    isDay: Boolean,
    progress: Float
) {
    val baseY = h * 0.48f

    // Tokyo City Avenue Grid
    val road = Path().apply {
        moveTo(0f, baseY + 66f)
        lineTo(w, baseY + 66f)
    }
    drawPath(road, Color(0xFF1E293B), style = Stroke(width = 24f, cap = StrokeCap.Round))
    // Pedestrian crosswalk stripes
    for (i in 0 until 12) {
        drawLine(
            Color.White,
            Offset(w * 0.32f + i * 8f, baseY + 56f),
            Offset(w * 0.32f + i * 8f, baseY + 76f),
            strokeWidth = 3f
        )
    }

    // Moving Green Tokyo Taxi
    val carX = (progress * 1.3f % 1f) * (w - 20f)
    drawMicroCar(carX, baseY + 64f, Color(0xFF22C55E), isDay)

    // Tokyo Tower Miniature (Background Right)
    val ttX = w * 0.82f
    val ttY = baseY + 30f
    val ttPath = Path().apply {
        moveTo(ttX - 18f, ttY)
        lineTo(ttX - 3.5f, ttY - 110f)
        lineTo(ttX + 3.5f, ttY - 110f)
        lineTo(ttX + 18f, ttY)
        close()
    }
    drawPath(ttPath, Color(0xFFEF4444))
    drawRoundRect(Color.White, Offset(ttX - 12f, ttY - 60f), Size(24f, 10f), CornerRadius(2f, 2f))
    drawLine(Color.White, Offset(ttX, ttY - 110f), Offset(ttX, ttY - 134f), strokeWidth = 2.5f)

    // Dense Stepped Japanese High-Rises
    drawModernBuilding(
        x = w * 0.08f,
        y = baseY + 45f,
        width = 46f,
        depth = 36f,
        height = 84f,
        roofColor = Color(0xFF334155),
        facadeColor = Color(0xFF64748B),
        sideColor = Color(0xFF475569),
        condition = condition,
        isDay = isDay
    )

    drawModernBuilding(
        x = w * 0.30f,
        y = baseY + 50f,
        width = 52f,
        depth = 38f,
        height = 96f,
        roofColor = Color(0xFF38BDF8),
        facadeColor = Color(0xFFE2E8F0),
        sideColor = Color(0xFF94A3B8),
        condition = condition,
        isDay = isDay
    )

    drawModernBuilding(
        x = w * 0.56f,
        y = baseY + 48f,
        width = 48f,
        depth = 36f,
        height = 76f,
        roofColor = Color(0xFF0284C7),
        facadeColor = Color(0xFFBAE6FD),
        sideColor = Color(0xFF64748B),
        condition = condition,
        isDay = isDay
    )

    // Sakura / Cherry Blossom trees
    drawPuffyMiniTree(w * 0.03f, baseY + 18f, 15f, Color(0xFFF472B6))
    drawPuffyMiniTree(w * 0.25f, baseY + 14f, 14f, Color(0xFFFBCFE8))
    drawPuffyMiniTree(w * 0.50f, baseY + 20f, 16f, Color(0xFFF472B6))
    drawPuffyMiniTree(w * 0.74f, baseY + 26f, 15f, Color(0xFFFBCFE8))
}

// =================== NEW YORK ===================
private fun DrawScope.drawNewYorkWorld(
    w: Float,
    h: Float,
    condition: WeatherCondition,
    isDay: Boolean,
    progress: Float
) {
    val baseY = h * 0.48f

    // Fifth Avenue asphalt grid
    val road = Path().apply {
        moveTo(0f, baseY + 66f)
        lineTo(w, baseY + 66f)
    }
    drawPath(road, Color(0xFF1E293B), style = Stroke(width = 24f, cap = StrokeCap.Round))
    // Yellow dashed lane lines
    for (i in 0 until 10) {
        drawLine(
            Color(0xFFFACC15),
            Offset(i * (w / 9f), baseY + 66f),
            Offset(i * (w / 9f) + 14f, baseY + 66f),
            strokeWidth = 2.5f
        )
    }

    // Moving Yellow Cabs
    val cab1X = (progress % 1f) * (w - 20f)
    val cab2X = ((progress + 0.5f) % 1f) * (w - 20f)
    drawMicroCar(cab1X, baseY + 64f, Color(0xFFFACC15), isDay)
    drawMicroCar(cab2X, baseY + 64f, Color(0xFFFACC15), isDay)

    // Central Park green rectangle in mid-ground
    drawRoundRect(
        Color(0xFF16A34A),
        Offset(w * 0.04f, baseY + 10f),
        Size(w * 0.22f, 32f),
        CornerRadius(4f, 4f)
    )
    drawPuffyMiniTree(w * 0.10f, baseY + 22f, 12f, Color(0xFF15803D))
    drawPuffyMiniTree(w * 0.18f, baseY + 22f, 13f, Color(0xFF15803D))

    // Art Deco Empire Skyscraper (Centerpiece)
    drawModernBuilding(
        x = w * 0.40f,
        y = baseY + 50f,
        width = 54f,
        depth = 42f,
        height = 118f,
        roofColor = Color(0xFF94A3B8),
        facadeColor = Color(0xFFCBD5E1),
        sideColor = Color(0xFF64748B),
        condition = condition,
        isDay = isDay
    )
    // Art Deco Spire
    drawLine(Color(0xFFE2E8F0), Offset(w * 0.40f + 27f, baseY - 68f), Offset(w * 0.40f + 27f, baseY - 105f), strokeWidth = 3f)

    // Brick Brownstone (Left)
    drawModernBuilding(
        x = w * 0.24f,
        y = baseY + 46f,
        width = 46f,
        depth = 34f,
        height = 54f,
        roofColor = Color(0xFF78350F),
        facadeColor = Color(0xFFB45309), // Terracotta brick
        sideColor = Color(0xFF92400E),
        condition = condition,
        isDay = isDay
    )

    // Modern High-Rise (Right)
    drawModernBuilding(
        x = w * 0.65f,
        y = baseY + 54f,
        width = 56f,
        depth = 40f,
        height = 92f,
        roofColor = Color(0xFF0284C7),
        facadeColor = Color(0xFFE2E8F0),
        sideColor = Color(0xFF94A3B8),
        condition = condition,
        isDay = isDay
    )
}

// =================== LONDON ===================
private fun DrawScope.drawLondonWorld(
    w: Float,
    h: Float,
    condition: WeatherCondition,
    isDay: Boolean,
    progress: Float
) {
    val baseY = h * 0.48f

    // Thames River bend
    val river = Path().apply {
        moveTo(0f, baseY + 50f)
        cubicTo(w * 0.4f, baseY + 40f, w * 0.6f, baseY + 80f, w, baseY + 60f)
    }
    drawPath(river, Color(0xFF0284C7), style = Stroke(width = 32f, cap = StrokeCap.Round))

    // Westminster Arched Bridge
    val bridge = Path().apply {
        moveTo(w * 0.38f, baseY + 30f)
        lineTo(w * 0.52f, baseY + 90f)
    }
    drawPath(bridge, Color(0xFFCBD5E1), style = Stroke(width = 14f, cap = StrokeCap.Round))

    // Moving Red Double-Decker Bus on Bridge
    val busT = (progress * 1.1f) % 1f
    val busX = w * 0.40f + busT * 26f
    val busY = baseY + 38f + busT * 42f
    drawRoundRect(
        Color(0xFFDC2626),
        Offset(busX - 8f, busY - 12f),
        Size(26f, 15f),
        CornerRadius(3f, 3f)
    )

    // Big Ben Gothic Clock Tower (Left)
    val bbX = w * 0.22f
    val bbY = baseY + 45f
    drawRoundRect(Color(0xFFD97706), Offset(bbX - 14f, bbY - 96f), Size(28f, 96f), CornerRadius(2.5f, 2.5f))
    drawCircle(Color.White, radius = 7.5f, center = Offset(bbX, bbY - 76f))
    drawCircle(Color.Black, radius = 6.5f, center = Offset(bbX, bbY - 76f), style = Stroke(1.5f))
    val spire = Path().apply {
        moveTo(bbX - 12f, bbY - 96f)
        lineTo(bbX, bbY - 128f)
        lineTo(bbX + 12f, bbY - 96f)
        close()
    }
    drawPath(spire, Color(0xFF334155))

    // London Townhouses
    drawModernBuilding(
        x = w * 0.62f,
        y = baseY + 48f,
        width = 54f,
        depth = 38f,
        height = 56f,
        roofColor = Color(0xFF334155),
        facadeColor = Color(0xFFCBD5E1),
        sideColor = Color(0xFF94A3B8),
        condition = condition,
        isDay = isDay
    )
}

// =================== PARIS ===================
private fun DrawScope.drawParisWorld(
    w: Float,
    h: Float,
    condition: WeatherCondition,
    isDay: Boolean,
    progress: Float
) {
    val baseY = h * 0.48f

    // Eiffel Tower Miniature (Left)
    val etX = w * 0.24f
    val etY = baseY + 48f
    val eiffel = Path().apply {
        moveTo(etX - 24f, etY)
        lineTo(etX - 4f, etY - 105f)
        lineTo(etX + 4f, etY - 105f)
        lineTo(etX + 24f, etY)
        close()
    }
    drawPath(eiffel, Color(0xFF78716C))
    drawRect(Color(0xFF44403C), Offset(etX - 16f, etY - 55f), Size(32f, 7f))
    drawLine(Color(0xFF44403C), Offset(etX, etY - 105f), Offset(etX, etY - 132f), strokeWidth = 3f)

    // Boulevard road
    val road = Path().apply {
        moveTo(0f, baseY + 66f)
        lineTo(w, baseY + 66f)
    }
    drawPath(road, Color(0xFF334155), style = Stroke(width = 24f, cap = StrokeCap.Round))

    val carX = (progress * 1.2f % 1f) * (w - 20f)
    drawMicroCar(carX, baseY + 64f, Color(0xFF38BDF8), isDay)

    // Haussmannian Beige Apartments with Mansard roofs
    drawModernBuilding(
        x = w * 0.48f,
        y = baseY + 48f,
        width = 62f,
        depth = 40f,
        height = 62f,
        roofColor = Color(0xFF475569), // Slate grey mansard
        facadeColor = Color(0xFFFEF3C7), // Limestone beige
        sideColor = Color(0xFFFDE68A),
        condition = condition,
        isDay = isDay
    )
}

// =================== GENERIC METROPOLIS ===================
private fun DrawScope.drawGenericWorld(
    w: Float,
    h: Float,
    condition: WeatherCondition,
    isDay: Boolean,
    progress: Float
) {
    val baseY = h * 0.48f

    val road = Path().apply {
        moveTo(0f, baseY + 66f)
        lineTo(w, baseY + 66f)
    }
    drawPath(road, Color(0xFF334155), style = Stroke(width = 24f, cap = StrokeCap.Round))
    val carX = (progress * 1.2f % 1f) * (w - 20f)
    drawMicroCar(carX, baseY + 64f, Color(0xFF38BDF8), isDay)

    drawModernBuilding(
        x = w * 0.18f,
        y = baseY + 46f,
        width = 48f,
        depth = 36f,
        height = 75f,
        roofColor = Color(0xFF38BDF8),
        facadeColor = Color(0xFFE2E8F0),
        sideColor = Color(0xFF94A3B8),
        condition = condition,
        isDay = isDay
    )
    drawModernBuilding(
        x = w * 0.48f,
        y = baseY + 50f,
        width = 54f,
        depth = 40f,
        height = 64f,
        roofColor = Color(0xFF0284C7),
        facadeColor = Color(0xFFBAE6FD),
        sideColor = Color(0xFF64748B),
        condition = condition,
        isDay = isDay
    )
}

// -------------------------------------------------------------
// 3. ARCHITECTURAL BUILDING & MINIATURE PRIMITIVES
// -------------------------------------------------------------
private fun DrawScope.drawModernBuilding(
    x: Float,
    y: Float,
    width: Float,
    depth: Float,
    height: Float,
    roofColor: Color,
    facadeColor: Color,
    sideColor: Color,
    condition: WeatherCondition,
    isDay: Boolean
) {
    val skew = depth * 0.38f

    // Soft realistic cast shadow on ground
    val shadowLength = when (condition) {
        WeatherCondition.SUNSET_TWILIGHT -> height * 0.70f
        WeatherCondition.CLOUDY, WeatherCondition.RAIN -> height * 0.20f
        else -> height * 0.38f
    }
    val shadowPath = Path().apply {
        moveTo(x, y)
        lineTo(x + width, y)
        lineTo(x + width + skew + shadowLength * 0.6f, y - skew * 0.45f + shadowLength * 0.3f)
        lineTo(x + skew + shadowLength * 0.6f, y - skew * 0.45f + shadowLength * 0.3f)
        close()
    }
    drawPath(shadowPath, Color(0x180F172A))

    // Front facade
    drawRect(facadeColor, Offset(x, y - height), Size(width, height))

    // Side facade (isometric shadow)
    val sidePath = Path().apply {
        moveTo(x + width, y - height)
        lineTo(x + width + skew, y - height - skew * 0.5f)
        lineTo(x + width + skew, y - skew * 0.5f)
        lineTo(x + width, y)
        close()
    }
    drawPath(sidePath, sideColor)

    // Roof surface
    val roofPath = Path().apply {
        moveTo(x, y - height)
        lineTo(x + skew, y - height - skew * 0.5f)
        lineTo(x + width + skew, y - height - skew * 0.5f)
        lineTo(x + width, y - height)
        close()
    }
    drawPath(roofPath, roofColor)

    // Windows
    val rows = 4
    val cols = 3
    val winW = width / (cols + 1) * 0.52f
    val winH = height / (rows + 1) * 0.46f

    val windowColor = when {
        condition == WeatherCondition.SUNSET_TWILIGHT || !isDay -> Color(0xFFFEF08A)
        condition == WeatherCondition.RAIN || condition == WeatherCondition.STORM -> Color(0xFFFDE047)
        else -> Color.White.copy(alpha = 0.75f)
    }

    for (r in 1..rows) {
        for (c in 1..cols) {
            val wx = x + c * (width / (cols + 1)) - winW / 2f
            val wy = y - height + r * (height / (rows + 1)) - winH / 2f
            drawRoundRect(
                color = windowColor,
                topLeft = Offset(wx, wy),
                size = Size(winW, winH),
                cornerRadius = CornerRadius(1.5f, 1.5f)
            )
        }
    }
}

private fun DrawScope.drawSuburbanHouse(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    wallColor: Color,
    roofColor: Color
) {
    // House base
    drawRect(wallColor, Offset(x, y - height * 0.6f), Size(width, height * 0.6f))
    // Pitched roof
    val roof = Path().apply {
        moveTo(x - 3f, y - height * 0.6f)
        lineTo(x + width / 2f, y - height)
        lineTo(x + width + 3f, y - height * 0.6f)
        close()
    }
    drawPath(roof, roofColor)
    // Front door
    drawRect(Color(0xFF78350F), Offset(x + width * 0.4f, y - height * 0.35f), Size(width * 0.2f, height * 0.35f))
}

private fun DrawScope.drawPuffyMiniTree(x: Float, y: Float, radius: Float, foliageColor: Color) {
    // Soft shadow
    drawOval(Color(0x180F172A), Offset(x - radius * 0.8f, y + radius * 0.3f), Size(radius * 1.6f, radius * 0.6f))
    // Trunk
    drawLine(Color(0xFF78350F), Offset(x, y), Offset(x, y + radius * 0.8f), strokeWidth = 3f, cap = StrokeCap.Round)
    // Canopy
    drawCircle(foliageColor, radius = radius, center = Offset(x, y))
    drawCircle(Color.White.copy(alpha = 0.22f), radius = radius * 0.55f, center = Offset(x - radius * 0.2f, y - radius * 0.2f))
}

private fun DrawScope.drawMiniaturePalm(x: Float, y: Float, height: Float) {
    val trunk = Path().apply {
        moveTo(x, y + height)
        quadraticTo(x + 6f, y + height * 0.5f, x - 4f, y)
    }
    drawPath(trunk, Color(0xFF78350F), style = Stroke(width = 3.5f, cap = StrokeCap.Round))
    val frondColor = Color(0xFF15803D)
    drawLine(frondColor, Offset(x - 4f, y), Offset(x - 20f, y - 6f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(frondColor, Offset(x - 4f, y), Offset(x + 16f, y - 8f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(frondColor, Offset(x - 4f, y), Offset(x - 12f, y + 8f), strokeWidth = 2.5f, cap = StrokeCap.Round)
    drawLine(frondColor, Offset(x - 4f, y), Offset(x + 18f, y + 6f), strokeWidth = 2.5f, cap = StrokeCap.Round)
}

private fun DrawScope.drawDistantMountain(x1: Float, x2: Float, baseY: Float, height: Float, color: Color) {
    val peakX = (x1 + x2) / 2f
    val mtn = Path().apply {
        moveTo(x1, baseY + height)
        lineTo(peakX, baseY)
        lineTo(x2, baseY + height)
        close()
    }
    drawPath(mtn, color)
}

private fun DrawScope.drawMicroCar(x: Float, y: Float, bodyColor: Color, isDay: Boolean) {
    drawOval(Color(0x28000000), Offset(x - 1f, y + 5f), Size(18f, 5f))
    drawRoundRect(bodyColor, Offset(x, y), Size(16f, 7.5f), CornerRadius(2f, 2f))
    drawRect(Color.White.copy(alpha = 0.85f), Offset(x + 4f, y - 2f), Size(8f, 2.5f))
    drawCircle(Color(0xFFFEF08A), radius = 1.5f, center = Offset(x + 14f, y + 2.5f))
}

// -------------------------------------------------------------
// 4. WEATHER OVERLAY & ATMOSPHERIC BLEND
// -------------------------------------------------------------
private fun DrawScope.drawTerrainWeatherReaction(width: Float, height: Float, condition: WeatherCondition) {
    when (condition) {
        WeatherCondition.SNOW -> {
            // Soft snowcaps on building roofs
            drawCircle(Color.White.copy(alpha = 0.9f), radius = 8f, center = Offset(width * 0.44f, height * 0.42f))
            drawCircle(Color.White.copy(alpha = 0.9f), radius = 7f, center = Offset(width * 0.66f, height * 0.46f))
        }
        WeatherCondition.RAIN -> {
            // Subtle road specular puddle
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x6693C5FD), Color.Transparent),
                    center = Offset(width * 0.5f, height * 0.54f),
                    radius = 35f
                ),
                topLeft = Offset(width * 0.42f, height * 0.52f),
                size = Size(65f, 18f)
            )
        }
        else -> Unit
    }
}

private fun DrawScope.drawAtmosphericDepthGradation(width: Float, height: Float) {
    // Top horizon atmospheric haze blending into sky
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0x35FFFFFF), Color.Transparent),
            startY = 0f,
            endY = height * 0.24f
        ),
        topLeft = Offset(0f, 0f),
        size = Size(width, height * 0.24f)
    )

    // Bottom soft blend into screen floor
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0x22FFFFFF)),
            startY = height * 0.85f,
            endY = height
        ),
        topLeft = Offset(0f, height * 0.85f),
        size = Size(width, height * 0.15f)
    )
}
