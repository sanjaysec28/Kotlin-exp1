# MiniWeather 🌦️

MiniWeather is an Android weather app concept focused on presenting
everyday weather information through a playful **miniature-world visual
experience**. Instead of showing weather only as numbers and icons, the
app aims to pair weather conditions with an atmospheric, miniature-style
scene for the selected location.

## Contents

-   [Overview](#overview)
-   [Features](#features)
-   [Architecture](#architecture)
-   [Application workflow](#application-workflow)
-   [Technology stack](#technology-stack)
-   [Project structure](#project-structure)
-   [Configuration and secrets](#configuration-and-secrets)
-   [Build and run](#build-and-run)
-   [Testing and troubleshooting](#testing-and-troubleshooting)
-   [Security notes](#security-notes)
-   [Roadmap](#roadmap)

## Overview

**Platform:** Android\
**Project name:** MiniWeather\
**Primary experience:** Search for a location, view current weather, and
explore a weather-reactive miniature scene.

The intended implementation uses a native Android architecture with
Kotlin and Jetpack Compose. The UI, weather data retrieval, and optional
AI-generated scene workflow should remain separate so each part can be
tested and maintained independently.

## Features

The project concept includes the following features. Check the current
application before describing any item as fully implemented.

-   Search for a city or location.
-   Display current weather information such as temperature, conditions,
    and date/time.
-   Switch between Celsius and Fahrenheit.
-   Present weather conditions with a miniature 3D-inspired scene.
-   Adapt scene appearance to weather conditions, such as clear, cloudy,
    rainy, or snowy weather.
-   Use AI image generation for location-specific scenes when configured
    and available.
-   Show loading, empty, network-error, and API-error states.
-   Provide a responsive layout for supported Android screen sizes.

## Architecture

The recommended architecture separates presentation, business logic, and
data access. Keep API-specific code out of UI components.

``` text
┌───────────────────────────────────────┐
│              Compose UI               │
│ Search · Weather Card · Mini Scene    │
└───────────────────┬───────────────────┘
                    │ user actions / UI state
┌───────────────────▼───────────────────┐
│          ViewModel / StateFlow         │
│ Screen state · validation · events     │
└───────────────────┬───────────────────┘
                    │ calls
┌───────────────────▼───────────────────┐
│              Repository               │
│ Coordinates weather and scene data     │
└──────────────┬───────────────┬────────┘
               │               │
┌──────────────▼────────┐ ┌────▼────────────────┐
│   Weather data source │ │  Scene data source  │
│ Weather service/API   │ │ Optional AI service │
└──────────────┬────────┘ └────┬────────────────┘
               │               │
┌──────────────▼───────────────▼────────┐
│     Models, mapping and error types   │
└───────────────────────────────────────┘
```

### Main layers

**Presentation** - Jetpack Compose screens and reusable components
render the UI. - UI components observe screen state and send user
actions to the ViewModel. - Components should not make network requests
directly.

**ViewModel** - Coordinates user actions such as searching for a
location or changing temperature units. - Exposes observable UI state,
ideally through `StateFlow`. - Uses Kotlin coroutines for asynchronous
operations. - Reports loading, success, empty-result, and error states.

**Repository** - Provides a single interface for retrieving weather and
optional scene information. - Coordinates data sources and maps service
responses into app models. - Keeps UI logic independent of the weather
provider and AI provider.

**Weather data source** - Calls the configured weather API. - Parses the
response and maps it into application models. - Handles HTTP errors,
invalid responses, timeouts, and connectivity failures.

**Scene data source (optional)** - Requests or retrieves a miniature
scene based on the selected location and weather. - Should have loading
and failure states independent of the core weather result. - If image
generation is unavailable, the app should still be able to display
weather information.

**Models and utilities** - Define weather and screen-state models. -
Keep temperature conversion, date formatting, weather-condition mapping,
and prompt construction in testable utilities.

## Application workflow

### 1. Launch

1.  The app opens to the main weather screen.
2.  The ViewModel exposes an initial state.
3.  If a default location or last-selected location is supported, the
    app requests its weather; otherwise, the user can search for a
    location.

### 2. Search for a location

1.  The user enters a city or location and submits the search.
2.  The ViewModel validates the input and displays a loading state.
3.  The repository requests weather data from the configured weather
    service.
4.  The service response is mapped into the app's weather model.
5.  The UI displays the location, temperature, conditions, and other
    available details.
6.  If the request fails or the location is not found, the UI shows a
    useful error and allows retrying.

### 3. Create or load the miniature scene

1.  The app derives a scene description from the selected location and
    weather condition.
2.  If the optional AI scene service is configured, the scene data
    source requests an image or retrieves an existing result.
3.  The UI shows scene loading independently of weather loading.
4.  On success, the scene is displayed with the weather information.
5.  On failure, the app keeps the weather screen usable and shows a
    fallback or retry action.

### 4. Change temperature units

1.  The user selects °C or °F.
2.  The presentation layer updates the selected unit.
3.  The app converts the displayed temperature using a tested conversion
    utility.
4.  The underlying weather value should remain in a consistent base unit
    to avoid repeated-conversion errors.

### 5. Handle failures

-   Show a loading indicator while requests are in progress.
-   Distinguish invalid location, network failure, API failure, and
    optional scene-generation failure.
-   Avoid crashing on missing or malformed response fields.
-   Keep the last valid result where appropriate and provide a retry
    action.
-   Never display fabricated weather values as if they came from a live
    service.

## Technology stack

The intended/native implementation direction is:

  Technology                  Responsibility
  --------------------------- ---------------------------------------
  Kotlin                      Android application language
  Jetpack Compose             Declarative UI
  Material 3                  Android UI components and theming
  ViewModel                   Screen-level state and event handling
  Kotlin Coroutines           Asynchronous work
  StateFlow                   Observable state
  Repository pattern          Separation of UI and data sources
  Weather API                 Current weather and location data
  Optional AI image service   Weather-reactive miniature scenes
  Gradle Kotlin DSL           Android build configuration

**Verification:** The exact weather provider, AI provider, library
versions, and currently installed dependencies should be taken from the
repository's Gradle files and source code. Do not assume an API provider
is configured just because it is mentioned in this document.

## Project structure

A suggested structure for this architecture is shown below. Actual file
and package names may differ in the current repository.

``` text
MiniWeather/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/<package-name>/
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/
│   │   │   │   │   ├── remote/
│   │   │   │   │   └── repository/
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/
│   │   │   │   │   ├── screens/
│   │   │   │   │   └── theme/
│   │   │   │   ├── viewmodel/
│   │   │   │   ├── util/
│   │   │   │   └── MainActivity.kt
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── README.md
```

This is an architectural guide, not a claim that every listed folder
already exists. Prefer the repository's current structure and avoid
moving files unless there is a clear reason.

## Configuration and secrets

1.  Inspect the project's current configuration to identify required API
    keys and endpoints.
2.  Configure keys using the existing local configuration mechanism (for
    example, Gradle properties or a local, untracked environment file)
    and read them through the appropriate build configuration.
3.  Add local secret files to `.gitignore`.
4.  Commit a safe example configuration file with placeholder values
    only, if the project needs one.
5.  Never commit real API keys, passwords, access tokens, or private
    service credentials.

**Important:** A key embedded in an Android application can be extracted
from the packaged app. For production, proxy sensitive provider calls
through a trusted backend where appropriate, and restrict provider keys
by permissions, quotas, and application requirements.

## Build and run

### Requirements

-   Android Studio with a compatible Android SDK.
-   A compatible JDK/Gradle setup as specified by the project.
-   An Android emulator or physical Android device.
-   Valid provider configuration for features that require external
    APIs.

### Open the project

1.  Clone or download the repository.
2.  Open the **project root** in Android Studio---the directory
    containing `settings.gradle.kts` and the root `build.gradle.kts`.
3.  Allow Gradle sync to finish.
4.  Configure any required local API keys without committing them.
5.  Select an emulator or connected device.
6.  Run the `app` configuration.

### Command-line build

On macOS/Linux:

``` bash
./gradlew assembleDebug
```

On Windows:

``` bat
gradlew.bat assembleDebug
```

To run unit tests:

``` bash
./gradlew test
```

These commands assume the repository includes the Gradle wrapper and
corresponding tasks. Resolve any Android SDK or JDK requirements
reported by Gradle.

## Testing and troubleshooting

Recommended checks:

-   Search for a valid city and confirm the returned location is
    correct.
-   Test a location that does not exist.
-   Test with the network disabled and restore connectivity.
-   Test missing or invalid API configuration.
-   Confirm Celsius/Fahrenheit conversion in both directions.
-   Verify weather errors do not prevent the rest of the screen from
    working.
-   Verify optional scene-generation failures do not hide weather
    information.
-   Check loading indicators, retry actions, and screen-size behavior.
-   Ensure no secrets appear in Git history or application logs.

### Common issues

  -----------------------------------------------------------------------
  Symptom                             What to inspect
  ----------------------------------- -----------------------------------
  Gradle sync fails                   JDK, Android Gradle Plugin, Gradle
                                      wrapper, SDK and dependency
                                      versions

  Weather request fails               API key configuration, endpoint,
                                      connectivity, quota, and response
                                      parsing

  Location search returns no result   Input validation, provider
                                      response, and error mapping

  Scene does not appear               Scene provider configuration,
                                      network request, image response,
                                      and UI state

  App shows stale data                State handling, caching policy, and
                                      request lifecycle

  API key is missing                  Local configuration and build-time
                                      key wiring; never paste secrets
                                      into source control
  -----------------------------------------------------------------------

## Security notes

-   Do not commit API keys or credentials.
-   Treat weather and scene-generation services as external dependencies
    that can fail.
-   Validate user input and external API responses.
-   Use HTTPS for network requests.
-   Avoid logging secrets or sensitive location information.
-   Apply appropriate quotas and restrictions to provider keys.
-   If a backend is used, validate requests and keep private credentials
    on the server.

## Roadmap

Potential improvements, depending on the current implementation:

-   More detailed forecasts and weather alerts.
-   Saved locations and recent searches.
-   Cached last-known weather for offline viewing.
-   More miniature scene styles and weather-specific animations.
-   Accessibility improvements and additional screen-size testing.
-   Automated unit tests for temperature conversion, model mapping, and
    UI state transitions.

## Contributing

1.  Create a feature branch.
2.  Make a focused change.
3.  Run the relevant tests and build checks.
4.  Review the diff for accidental secrets and unrelated changes.
5.  Open a pull request with a concise summary and test results.

## License

Add the license selected for this project before distributing it
publicly. Until a license is added, do not assume that others have
permission to reuse the source code.
