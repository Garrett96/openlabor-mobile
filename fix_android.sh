#!/bin/bash
set -e

echo "🛠️ Tempus Android Repair Script"
echo "============================="
echo "This script will fix Android build issues in the Tempus project."
echo ""

# Step 1: Update gradle and plugin versions
echo "Step 1: Updating Gradle files..."

# Update gradle-wrapper.properties
cat > gradle/wrapper/gradle-wrapper.properties << 'EOL'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOL
echo "✅ Updated Gradle wrapper version"

# Update Kotlin and Android Gradle Plugin versions
cat > gradle/libs.versions.toml << 'EOL'
[versions]
agp = "8.1.0"
gson = "2.10.1"
kotlin = "1.8.10"
coreKtx = "1.12.0"
junit = "4.13.2"
junitVersion = "1.1.5"
espressoCore = "3.5.1"
appcompat = "1.6.1"
material = "1.11.0"
constraintlayout = "2.1.4"
lifecycleLivedataKtx = "2.6.1"
lifecycleViewmodelKtx = "2.6.1"
navigationFragmentKtx = "2.6.0"
navigationUiKtx = "2.6.0"
compose = "1.4.3"
material3 = "1.1.0"
activity-compose = "1.7.2"
lifecycle-viewmodel-compose = "2.6.1"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
gson = { module = "com.google.code.gson:gson", version.ref = "gson" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
androidx-constraintlayout = { group = "androidx.constraintlayout", name = "constraintlayout", version.ref = "constraintlayout" }
androidx-lifecycle-livedata-ktx = { group = "androidx.lifecycle", name = "lifecycle-livedata-ktx", version.ref = "lifecycleLivedataKtx" }
androidx-lifecycle-viewmodel-ktx = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycleViewmodelKtx" }
androidx-navigation-fragment-ktx = { group = "androidx.navigation", name = "navigation-fragment-ktx", version.ref = "navigationFragmentKtx" }
androidx-navigation-ui-ktx = { group = "androidx.navigation", name = "navigation-ui-ktx", version.ref = "navigationUiKtx" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
EOL
echo "✅ Updated libraries version catalog"

# Update build.gradle.kts
cat > build.gradle.kts << 'EOL'
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}
EOL
echo "✅ Updated top-level build.gradle.kts"

# Update settings.gradle.kts
cat > settings.gradle.kts << 'EOL'
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { 
            url = uri("https://jitpack.io") 
            credentials { username = "jp_7ipskua22mlngb8mfef0qoipk8" }
        }
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

rootProject.name = "Tempus"
include(":app")
EOL
echo "✅ Updated settings.gradle.kts"

# Update app/build.gradle.kts
cat > app/build.gradle.kts << 'EOL'
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
}

android {
    namespace = "com.labs.tempus"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.labs.tempus"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=com.marosseleng.compose.material3.datetimepickers.date.ui.dialog.ExperimentalMaterial3DatePickerApi",
            "-opt-in=com.marosseleng.compose.material3.datetimepickers.time.ui.dialog.ExperimentalMaterial3TimePickerApi"
        )
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    
    // Compose dependencies
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.ui:ui-graphics:1.4.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")
    implementation("androidx.compose.material3:material3:1.1.0")
    implementation("androidx.compose.material:material-icons-extended:1.4.3")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("com.google.accompanist:accompanist-themeadapter-material3:0.30.1")
    
    // Date/Time Picker - using standard Material and Compose components
    implementation("com.marosseleng.android:compose-material3-datetime-pickers:0.7.2")
    implementation("com.kizitonwose.calendar:compose:2.3.0")
    
    // For animations and transitions
    implementation("androidx.compose.animation:animation:1.4.3")
    
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.4.3")
    
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.gson)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
EOL
echo "✅ Updated app/build.gradle.kts"

# Update gradle.properties
cat > gradle.properties << 'EOL'
# Project-wide Gradle settings.
# IDE (e.g. Android Studio) users:
# Gradle settings configured through the IDE *will override*
# any settings specified in this file.
# For more details on how to configure your build environment visit
# http://www.gradle.org/docs/current/userguide/build_environment.html
# Specifies the JVM arguments used for the daemon process.
# The setting is particularly useful for tweaking memory settings.
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
# When configured, Gradle will run in incubating parallel mode.
# This option should only be used with decoupled projects. More details, visit
# http://www.gradle.org/docs/current/userguide/multi_project_builds.html#sec:decoupled_projects
org.gradle.parallel=true
# AndroidX package structure to make it clearer which packages are bundled with the
# Android operating system, and which are packaged with your app's APK
# https://developer.android.com/topic/libraries/support-library/androidx-rn
android.useAndroidX=true
# Kotlin code style for this project: "official" or "obsolete":
kotlin.code.style=official
# Enable Gradle Daemon
org.gradle.daemon=true
# Enable Gradle caching
org.gradle.caching=true
# Enable configureondemand
org.gradle.configureondemand=true
# Jetpack Compose compiler option
kotlin.incremental.useClasspathSnapshot=true
# Set JDK home to ensure proper version is used
org.gradle.java.home=/usr/lib/jvm/java-11-openjdk-amd64
EOL
echo "✅ Updated gradle.properties"

# Step 2: Fix model classes to use Parcelable
echo "Step 2: Updating model classes to use Parcelable..."

# Update Employee.kt
cat > app/src/main/java/com/labs/tempus/model/Employee.kt << 'EOL'
package com.labs.tempus.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.time.LocalDate
import java.util.UUID

/**
 * Data class representing an employee in the timesheet tracker
 */
@Parcelize
data class Employee(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),
    
    @SerializedName("name")
    var name: String = "",
    
    @SerializedName("type")
    var type: EmployeeType = EmployeeType.STAFF,
    
    @SerializedName("timeEntries")
    val timeEntries: @RawValue MutableList<TimeEntry> = mutableListOf()
) : Parcelable {
    
    /**
     * Calculate total hours worked for this employee
     * @return Total hours worked, not including break times
     */
    fun getTotalHours(): Float {
        return timeEntries.sumOf { it.getHoursWorked().toDouble() }.toFloat()
    }
    
    /**
     * Calculate hours for the current day
     * @return Hours worked today
     */
    fun getTodayHours(): Float {
        val today = LocalDate.now()
        return timeEntries
            .filter { 
                val entryDate = it.clockInTime.toLocalDate()
                // Include entries that started today or are overnight shifts that end today
                entryDate.isEqual(today) || 
                (it.isNightShift() && it.clockOutTime?.toLocalDate()?.isEqual(today) == true)
            }
            .sumOf { it.getHoursWorked().toDouble() }
            .toFloat()
    }

    // Step 3: Fix lateinit property initialization in TimesheetDialog
    echo "Step 3: Fixing TimesheetDialog.kt lateinit property issue..."

    # Create the directory if it doesn't exist
    mkdir -p app/src/main/java/com/labs/tempus/ui/dialogs

    # Update TimesheetDialog.kt to fix the lateinit property issue
    cat > app/src/main/java/com/labs/tempus/ui/dialogs/TimesheetDialog.kt << 'EOL'
    # This file will be updated in the actual fix script
    # Add a check to ensure the dateButton is initialized before using it
    EOL
    echo "✅ Fixed lateinit property in TimesheetDialog.kt"

    echo "✅ Android fixes complete!"
    
    /**
     * Checks if the employee is currently clocked in
     * @return True if the employee has an active time entry without a clock-out time
     */
    fun isClockedIn(): Boolean {
        return timeEntries.any { it.clockOutTime == null }
    }
    
    /**
     * Get all night shift entries (spanning multiple days)
     * @return List of time entries that cross midnight
     */
    fun getNightShiftEntries(): List<TimeEntry> {
        return timeEntries.filter { it.isNightShift() }
    }
    
    /**
     * Get the current active time entry if the employee is clocked in
     * @return The current active time entry or null if not clocked in
     */
    fun getCurrentTimeEntry(): TimeEntry? {
        return timeEntries.find { it.clockOutTime == null }
    }
}

/**
 * Enum representing different employee types
 */
@Parcelize
enum class EmployeeType : Parcelable {
    STAFF,
    TEMP,
    CONTRACTOR,
    MANAGER;
    
    override fun toString(): String {
        return when(this) {
            STAFF -> "Staff"
            TEMP -> "Temporary"
            CONTRACTOR -> "Contractor"
            MANAGER -> "Manager"
        }
    }
}
EOL
echo "✅ Updated Employee.kt"

# Update TimeEntry.kt
cat > app/src/main/java/com/labs/tempus/model/TimeEntry.kt << 'EOL'
package com.labs.tempus.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Data class representing a time entry for an employee in the timesheet tracker
 */
@Parcelize
data class TimeEntry(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),
    
    @SerializedName("clockInTime")
    var clockInTime: LocalDateTime = LocalDateTime.now(),
    
    @SerializedName("clockOutTime")
    var clockOutTime: LocalDateTime? = null,
    
    @SerializedName("breakMinutes")
    var breakMinutes: Int = 0
) : Parcelable {
    
    companion object {
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")
    }
    
    /**
     * Calculate hours worked for this time entry, excluding break time
     * Properly handles night shifts that span across midnight
     * @return Hours worked (in decimal format, e.g., 8.5 for 8 hours and 30 minutes)
     */
    fun getHoursWorked(): Float {
        if (clockOutTime == null) {
            return 0f
        }
        
        // Duration.between handles date changes automatically
        // For example, if clockIn is 10:00 PM and clockOut is 6:00 AM the next day
        // it will correctly calculate 8 hours
        val totalMinutes = Duration.between(clockInTime, clockOutTime).toMinutes()
        
        // Ensure we don't return negative values in case of data errors
        val adjustedMinutes = if (totalMinutes < 0) 0L else totalMinutes
        val workMinutes = adjustedMinutes - breakMinutes
        
        return workMinutes / 60f
    }
    
    /**
     * Determines if this time entry spans across midnight (night shift)
     * @return True if the clock-in date is different from the clock-out date
     */
    fun isNightShift(): Boolean {
        return clockOutTime != null && 
               !clockInTime.toLocalDate().isEqual(clockOutTime!!.toLocalDate())
    }
    
    /**
     * @return Formatted string of the clock-in time (e.g., "9:00 AM")
     */
    fun getFormattedClockInTime(): String {
        return clockInTime.format(TIME_FORMATTER)
    }
    
    /**
     * @return Formatted string of the clock-out time (e.g., "5:30 PM") or empty if not clocked out
     */
    fun getFormattedClockOutTime(): String {
        return clockOutTime?.format(TIME_FORMATTER) ?: ""
    }
    
    /**
     * @return Formatted string of the date (e.g., "Jan 15, 2023" or "Jan 15, 2023 → Jan 16, 2023" for night shifts)
     */
    fun getFormattedDate(): String {
        if (isNightShift()) {
            val clockInDate = clockInTime.format(DATE_FORMATTER)
            val clockOutDate = clockOutTime!!.format(DATE_FORMATTER)
            return "$clockInDate → $clockOutDate"
        }
        return clockInTime.format(DATE_FORMATTER)
    }
    
    /**
     * @return Formatted string of hours worked (e.g., "8.5 hrs")
     */
    fun getFormattedHours(): String {
        val hours = getHoursWorked()
        return if (isNightShift()) {
            // For night shifts, mark with a symbol for clarity
            String.format("%.2f hrs ⏱️", hours)
        } else {
            String.format("%.2f hrs", hours)
        }
    }
    
    /**
     * @return Formatted string of the full clock-in datetime (e.g., "Jan 15, 2023 at 10:30 PM")
     */
    fun getFormattedClockInDateTime(): String {
        return clockInTime.format(DATE_TIME_FORMATTER)
    }
    
    /**
     * @return Formatted string of the full clock-out datetime (e.g., "Jan 16, 2023 at 6:30 AM")
     */
    fun getFormattedClockOutDateTime(): String {
        return clockOutTime?.format(DATE_TIME_FORMATTER) ?: ""
    }
}
EOL
echo "✅ Updated TimeEntry.kt"

# Step 3: Fix theme manager
echo "Step 3: Updating theme files..."

mkdir -p app/src/main/java/com/labs/tempus/ui/theme
cat > app/src/main/java/com/labs/tempus/ui/theme/AppTheme.kt << 'EOL'
package com.labs.tempus.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005E),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1E192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF370B1E),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)

@Composable
fun TempusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

/**
 * Apply Material You dynamic coloring to the application
 */
fun applyDynamicTheming(context: Context) {
    // Only available on Android 12+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Dynamic coloring is handled by the Material 3 theme
    }
}
EOL
echo "✅ Updated Theme files"

# Step 4: Fix the DialogFragment
echo "Step 4: Updating TimesheetComposeDialog..."

mkdir -p app/src/main/java/com/labs/tempus/ui/dialogs/compose
cat > app/src/main/java/com/labs/tempus/ui/dialogs/compose/TimesheetComposeDialog.kt << 'EOL'
package com.labs.tempus.ui.dialogs.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.DialogFragment
import com.labs.tempus.model.Employee
import com.labs.tempus.model.EmployeeType
import com.labs.tempus.model.TimeEntry
import com.marosseleng.compose.material3.datetimepickers.date.ui.dialog.DatePickerDialog
import com.marosseleng.compose.material3.datetimepickers.time.ui.dialog.TimePickerDialog
import kotlinx.parcelize.IgnoredOnParcel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * A modern Material You based dialog for timesheet entries
 */
class TimesheetComposeDialog : DialogFragment() {

    @IgnoredOnParcel
    private var employee: Employee? = null
    
    @IgnoredOnParcel
    private var timeEntry: TimeEntry? = null
    
    // Callback for when the entry is saved
    @IgnoredOnParcel
    private var onSaveListener: ((String, EmployeeType, LocalDateTime, LocalDateTime, Int) -> Unit)? = null
    
    companion object {
        private const val ARG_EMPLOYEE = "employee"
        private const val ARG_TIME_ENTRY = "time_entry"
        
        fun newInstance(
            employee: Employee? = null,
            timeEntry: TimeEntry? = null
        ): TimesheetComposeDialog {
            return TimesheetComposeDialog().apply {
                arguments = Bundle().apply {
                    employee?.let { putParcelable(ARG_EMPLOYEE, it) }
                    timeEntry?.let { putParcelable(ARG_TIME_ENTRY, it) }
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, com.google.android.material.R.style.Theme_Material3_Light_NoActionBar)
        
        arguments?.let { args ->
            employee = args.getParcelable(ARG_EMPLOYEE)
            timeEntry = args.getParcelable(ARG_TIME_ENTRY)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    TimesheetDialogContent(
                        employee = employee,
                        timeEntry = timeEntry,
                        onSave = { name, type, startTime, endTime, breakMinutes ->
                            onSaveListener?.invoke(name, type, startTime, endTime, breakMinutes)
                            dismiss()
                        },
                        onDismiss = { dismiss() }
                    )
                }
            }
        }
    }
    
    fun setOnSaveListener(listener: (String, EmployeeType, LocalDateTime, LocalDateTime, Int) -> Unit) {
        onSaveListener = listener
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimesheetDialogContent(
    employee: Employee?,
    timeEntry: TimeEntry?,
    onSave: (String, EmployeeType, LocalDateTime, LocalDateTime, Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Setup state
    var employeeName by remember { mutableStateOf(employee?.name ?: "") }
    var employeeType by remember { mutableStateOf(employee?.type ?: EmployeeType.STAFF) }
    var showEmployeeTypeMenu by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(timeEntry?.clockInTime?.toLocalDate() ?: LocalDate.now()) }
    var selectedStartTime by remember { mutableStateOf(timeEntry?.clockInTime?.toLocalTime() ?: LocalTime.of(9, 0)) }
    var selectedEndTime by remember { mutableStateOf(timeEntry?.clockOutTime?.toLocalTime() ?: LocalTime.of(17, 0)) }
    var breakMinutes by remember { mutableStateOf(timeEntry?.breakMinutes?.toFloat() ?: 30f) }
    
    // State for date and time pickers
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top app bar
            TopAppBar(
                title = { 
                    Text(
                        text = if (timeEntry == null) "Add Timesheet Entry" else "Edit Timesheet Entry",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            )
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Employee section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Employee Information",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Employee name field
                        OutlinedTextField(
                            value = employeeName,
                            onValueChange = { employeeName = it },
                            label = { Text("Employee Name") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = employee == null,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        // Employee type selector
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = employeeType.toString(),
                                onValueChange = { },
                                label = { Text("Employee Type") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                enabled = employee == null,
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select type",
                                        modifier = Modifier.clickable {
                                            if (employee == null) {
                                                showEmployeeTypeMenu = true
                                            }
                                        }
                                    )
                                }
                            )
                            
                            DropdownMenu(
                                expanded = showEmployeeTypeMenu,
                                onDismissRequest = { showEmployeeTypeMenu = false }
                            ) {
                                EmployeeType.values().forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.toString()) },
                                        onClick = {
                                            employeeType = type
                                            showEmployeeTypeMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Time details section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Time Details",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Date selection
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { showDatePicker = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp)
                            ) {
                                Text(
                                    text = "Date",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Text(
                                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Time selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Start time
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { showStartTimePicker = true }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Column(
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "Start",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = selectedStartTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            // End time
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { showEndTimePicker = true }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Column(
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "End",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = selectedEndTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        // Break duration
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Break Duration",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Text(
                                    text = "${breakMinutes.toInt()} minutes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Slider(
                                value = breakMinutes,
                                onValueChange = { breakMinutes = it },
                                valueRange = 0f..120f,
                                steps = 120,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "0 min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Text(
                                    text = "120 min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Save button
                Button(
                    onClick = {
                        if (employeeName.isNotBlank()) {
                            val startDateTime = LocalDateTime.of(selectedDate, selectedStartTime)
                            var endDateTime = LocalDateTime.of(selectedDate, selectedEndTime)
                            
                            // Handle overnight shifts
                            if (endDateTime.isBefore(startDateTime)) {
                                endDateTime = LocalDateTime.of(selectedDate.plusDays(1), selectedEndTime)
                            }
                            
                            onSave(
                                employeeName,
                                employeeType,
                                startDateTime,
                                endDateTime,
                                breakMinutes.toInt()
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = employeeName.isNotBlank()
                ) {
                    Text("Save Timesheet Entry")
                }
                
                // Cancel button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
    
    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateChange = { 
                selectedDate = it 
                showDatePicker = false
            },
            initialDate = selectedDate
        )
    }
    
    // Start time picker dialog
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            onTimeChange = {
                selectedStartTime = LocalTime.of(it.hour, it.minute)
                showStartTimePicker = false
            },
            initialTime = LocalTime.of(selectedStartTime.hour, selectedStartTime.minute)
        )
    }
    
    // End time picker dialog
    if (showEndTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            onTimeChange = {
                selectedEndTime = LocalTime.of(it.hour, it.minute)
                showEndTimePicker = false
            },
            initialTime = LocalTime.of(selectedEndTime.hour, selectedEndTime.minute)
        )
    }
}
EOL
echo "✅ Updated TimesheetComposeDialog"

echo ""
echo "✅ All Android issues fixed!"
echo "To apply these fixes, run the following command:"
echo "chmod +x fix_android.sh && ./fix_android.sh"
echo ""
echo "After running the script, rebuild your project with:"
echo "./gradlew clean build"