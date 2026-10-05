package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.location.LocationService
import com.example.data.model.DioramaTheme
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MiniWeather", appName)
  }

  @Test
  fun `temperature unit conversion is accurate`() {
    val tempC = 23.8889 // 75°F
    val fahrenheitDisplay = TemperatureUnit.FAHRENHEIT.toDisplay(tempC)
    assertEquals("75°", fahrenheitDisplay)

    val celsiusDisplay = TemperatureUnit.CELSIUS.toDisplay(24.0)
    assertEquals("24°", celsiusDisplay)
  }

  @Test
  fun `diorama theme resolves correctly for cities`() {
    assertEquals(DioramaTheme.MOUNTAIN_VIEW, DioramaTheme.fromLocation("Mountain View"))
    assertEquals(DioramaTheme.TOKYO, DioramaTheme.fromLocation("Tokyo"))
    assertEquals(DioramaTheme.NEW_YORK, DioramaTheme.fromLocation("New York"))
    assertEquals(DioramaTheme.CHENNAI, DioramaTheme.fromLocation("Chennai"))
    assertEquals(DioramaTheme.BENGALURU, DioramaTheme.fromLocation("Bengaluru"))
    assertEquals(DioramaTheme.MUMBAI, DioramaTheme.fromLocation("Mumbai"))
    assertEquals(DioramaTheme.LONDON, DioramaTheme.fromLocation("London"))
  }

  @Test
  fun `location service initializes properly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val locationService = LocationService(context)
    assertNotNull(locationService)
  }
}
