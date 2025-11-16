package com.weatherdesk.ui.theme

import com.weatherdesk.model.WeatherCondition
import javafx.animation.*
import javafx.scene.layout.Pane
import javafx.scene.paint.*
import javafx.util.Duration
import java.time.LocalTime

/**
 * Dynamic theme manager that changes backgrounds based on weather and time of day
 * Creates immersive, atmospheric experiences
 */
class ThemeManager {

    /**
     * Time-based theme periods
     */
    enum class TimeOfDay {
        DAWN,      // 5:00 - 7:00
        MORNING,   // 7:00 - 11:00
        MIDDAY,    // 11:00 - 15:00
        AFTERNOON, // 15:00 - 18:00
        DUSK,      // 18:00 - 20:00
        NIGHT      // 20:00 - 5:00
    }

    /**
     * Weather theme with dynamic colors
     */
    data class WeatherTheme(
        val primaryColor: Color,
        val secondaryColor: Color,
        val accentColor: Color,
        val backgroundColor: Color,
        val gradientStops: List<Stop>,
        val particleColor: Color,
        val textColor: Color,
        val glassOpacity: Double = 0.15
    )

    companion object {
        /**
         * Determine time of day from current time
         */
        fun getTimeOfDay(time: LocalTime = LocalTime.now()): TimeOfDay {
            return when (time.hour) {
                in 5..6 -> TimeOfDay.DAWN
                in 7..10 -> TimeOfDay.MORNING
                in 11..14 -> TimeOfDay.MIDDAY
                in 15..17 -> TimeOfDay.AFTERNOON
                in 18..19 -> TimeOfDay.DUSK
                else -> TimeOfDay.NIGHT
            }
        }

        /**
         * Get theme based on weather condition and time of day
         */
        fun getTheme(condition: WeatherCondition, timeOfDay: TimeOfDay = getTimeOfDay()): WeatherTheme {
            return when (condition) {
                WeatherCondition.CLEAR -> getClearTheme(timeOfDay)
                WeatherCondition.CLOUDS,
                WeatherCondition.BROKEN_CLOUDS -> getCloudyTheme(timeOfDay)
                WeatherCondition.FEW_CLOUDS,
                WeatherCondition.SCATTERED_CLOUDS -> getPartlyCloudyTheme(timeOfDay)
                WeatherCondition.RAIN,
                WeatherCondition.SHOWER_RAIN -> getRainyTheme(timeOfDay)
                WeatherCondition.THUNDERSTORM -> getThunderstormTheme(timeOfDay)
                WeatherCondition.SNOW -> getSnowyTheme(timeOfDay)
                WeatherCondition.MIST -> getMistyTheme(timeOfDay)
                WeatherCondition.UNKNOWN -> getDefaultTheme()
            }
        }

        /**
         * Clear sky themes - vibrant and bright
         */
        private fun getClearTheme(timeOfDay: TimeOfDay): WeatherTheme {
            return when (timeOfDay) {
                TimeOfDay.DAWN -> WeatherTheme(
                    primaryColor = Color.web("#FF6B6B"),
                    secondaryColor = Color.web("#FFE66D"),
                    accentColor = Color.web("#FF8B94"),
                    backgroundColor = Color.web("#1A1A2E"),
                    gradientStops = listOf(
                        Stop(0.0, Color.web("#0F2027")),
                        Stop(0.5, Color.web("#203A43")),
                        Stop(1.0, Color.web("#2C5364"))
                    ),
                    particleColor = Color.web("#FFE66D", 0.8),
                    textColor = Color.WHITE
                )
                TimeOfDay.MORNING -> WeatherTheme(
                    primaryColor = Color.web("#4ECDC4"),
                    secondaryColor = Color.web("#FFE66D"),
                    accentColor = Color.web("#95E1D3"),
                    backgroundColor = Color.web("#38A3A5"),
                    gradientStops = listOf(
                        Stop(0.0, Color.web("#56CCF2")),
                        Stop(0.5, Color.web("#2F80ED")),
                        Stop(1.0, Color.web("#1E3A8A"))
                    ),
                    particleColor = Color.web("#FFE66D", 0.6),
                    textColor = Color.WHITE
                )
                TimeOfDay.MIDDAY -> WeatherTheme(
                    primaryColor = Color.web("#00D4FF"),
                    secondaryColor = Color.web("#FFB75E"),
                    accentColor = Color.web("#0DCEDA"),
                    backgroundColor = Color.web("#0077B6"),
                    gradientStops = listOf(
                        Stop(0.0, Color.web("#0077B6")),
                        Stop(0.5, Color.web("#00B4D8")),
                        Stop(1.0, Color.web("#90E0EF"))
                    ),
                    particleColor = Color.web("#FFE66D", 0.7),
                    textColor = Color.WHITE
                )
                TimeOfDay.AFTERNOON -> WeatherTheme(
                    primaryColor = Color.web("#F77F00"),
                    secondaryColor = Color.web("#FCBF49"),
                    accentColor = Color.web("#EAE2B7"),
                    backgroundColor = Color.web("#003049"),
                    gradientStops = listOf(
                        Stop(0.0, Color.web("#F77F00")),
                        Stop(0.5, Color.web("#D62828")),
                        Stop(1.0, Color.web("#003049"))
                    ),
                    particleColor = Color.web("#FCBF49", 0.6),
                    textColor = Color.WHITE
                )
                TimeOfDay.DUSK -> WeatherTheme(
                    primaryColor = Color.web("#D6336C"),
                    secondaryColor = Color.web("#7209B7"),
                    accentColor = Color.web("#F72585"),
                    backgroundColor = Color.web("#240046"),
                    gradientStops = listOf(
                        Stop(0.0, Color.web("#240046")),
                        Stop(0.5, Color.web("#5A189A")),
                        Stop(1.0, Color.web("#D6336C"))
                    ),
                    particleColor = Color.web("#F72585", 0.8),
                    textColor = Color.WHITE
                )
                TimeOfDay.NIGHT -> WeatherTheme(
                    primaryColor = Color.web("#4361EE"),
                    secondaryColor = Color.web("#7209B7"),
                    accentColor = Color.web("#560BAD"),
                    backgroundColor = Color.web("#10002B"),
                    gradientStops = listOf(
                        Stop(0.0, Color.web("#000000")),
                        Stop(0.5, Color.web("#0A0E27")),
                        Stop(1.0, Color.web("#1A1A40"))
                    ),
                    particleColor = Color.web("#FFFFFF", 0.5),
                    textColor = Color.WHITE
                )
            }
        }

        /**
         * Rainy themes - cool blues and greys
         */
        private fun getRainyTheme(timeOfDay: TimeOfDay): WeatherTheme {
            val isDark = timeOfDay in listOf(TimeOfDay.DUSK, TimeOfDay.NIGHT)
            return WeatherTheme(
                primaryColor = Color.web("#4A5568"),
                secondaryColor = Color.web("#2D3748"),
                accentColor = Color.web("#4299E1"),
                backgroundColor = Color.web("#1A202C"),
                gradientStops = if (isDark) listOf(
                    Stop(0.0, Color.web("#1A202C")),
                    Stop(0.5, Color.web("#2D3748")),
                    Stop(1.0, Color.web("#4A5568"))
                ) else listOf(
                    Stop(0.0, Color.web("#667EEA")),
                    Stop(0.5, Color.web("#4A5568")),
                    Stop(1.0, Color.web("#2D3748"))
                ),
                particleColor = Color.web("#4299E1", 0.6),
                textColor = Color.WHITE,
                glassOpacity = 0.2
            )
        }

        /**
         * Thunderstorm themes - dramatic purples and dark blues
         */
        private fun getThunderstormTheme(timeOfDay: TimeOfDay): WeatherTheme {
            return WeatherTheme(
                primaryColor = Color.web("#7C3AED"),
                secondaryColor = Color.web("#1F2937"),
                accentColor = Color.web("#FBBF24"),
                backgroundColor = Color.web("#111827"),
                gradientStops = listOf(
                    Stop(0.0, Color.web("#111827")),
                    Stop(0.5, Color.web("#1F2937")),
                    Stop(1.0, Color.web("#374151"))
                ),
                particleColor = Color.web("#FBBF24", 0.8),
                textColor = Color.WHITE,
                glassOpacity = 0.25
            )
        }

        /**
         * Snowy themes - pristine whites and icy blues
         */
        private fun getSnowyTheme(timeOfDay: TimeOfDay): WeatherTheme {
            return WeatherTheme(
                primaryColor = Color.web("#E0F2FE"),
                secondaryColor = Color.web("#7DD3FC"),
                accentColor = Color.web("#38BDF8"),
                backgroundColor = Color.web("#0C4A6E"),
                gradientStops = listOf(
                    Stop(0.0, Color.web("#E0F2FE")),
                    Stop(0.5, Color.web("#7DD3FC")),
                    Stop(1.0, Color.web("#0C4A6E"))
                ),
                particleColor = Color.web("#FFFFFF", 0.9),
                textColor = Color.web("#0C4A6E")
            )
        }

        /**
         * Cloudy themes - soft greys
         */
        private fun getCloudyTheme(timeOfDay: TimeOfDay): WeatherTheme {
            return WeatherTheme(
                primaryColor = Color.web("#94A3B8"),
                secondaryColor = Color.web("#64748B"),
                accentColor = Color.web("#CBD5E1"),
                backgroundColor = Color.web("#334155"),
                gradientStops = listOf(
                    Stop(0.0, Color.web("#475569")),
                    Stop(0.5, Color.web("#64748B")),
                    Stop(1.0, Color.web("#94A3B8"))
                ),
                particleColor = Color.web("#E2E8F0", 0.5),
                textColor = Color.WHITE
            )
        }

        /**
         * Partly cloudy themes
         */
        private fun getPartlyCloudyTheme(timeOfDay: TimeOfDay): WeatherTheme {
            val clearTheme = getClearTheme(timeOfDay)
            val cloudyTheme = getCloudyTheme(timeOfDay)

            return WeatherTheme(
                primaryColor = clearTheme.primaryColor,
                secondaryColor = cloudyTheme.secondaryColor,
                accentColor = clearTheme.accentColor,
                backgroundColor = clearTheme.backgroundColor,
                gradientStops = listOf(
                    Stop(0.0, clearTheme.gradientStops[0].color),
                    Stop(0.5, cloudyTheme.gradientStops[1].color),
                    Stop(1.0, clearTheme.gradientStops[2].color)
                ),
                particleColor = clearTheme.particleColor,
                textColor = Color.WHITE
            )
        }

        /**
         * Misty themes - ethereal and soft
         */
        private fun getMistyTheme(timeOfDay: TimeOfDay): WeatherTheme {
            return WeatherTheme(
                primaryColor = Color.web("#D1D5DB"),
                secondaryColor = Color.web("#9CA3AF"),
                accentColor = Color.web("#E5E7EB"),
                backgroundColor = Color.web("#6B7280"),
                gradientStops = listOf(
                    Stop(0.0, Color.web("#9CA3AF")),
                    Stop(0.5, Color.web("#D1D5DB")),
                    Stop(1.0, Color.web("#E5E7EB"))
                ),
                particleColor = Color.web("#F3F4F6", 0.4),
                textColor = Color.web("#1F2937")
            )
        }

        /**
         * Default theme
         */
        private fun getDefaultTheme(): WeatherTheme {
            return WeatherTheme(
                primaryColor = Color.web("#6366F1"),
                secondaryColor = Color.web("#4F46E5"),
                accentColor = Color.web("#818CF8"),
                backgroundColor = Color.web("#1E1B4B"),
                gradientStops = listOf(
                    Stop(0.0, Color.web("#1E1B4B")),
                    Stop(0.5, Color.web("#312E81")),
                    Stop(1.0, Color.web("#4C1D95"))
                ),
                particleColor = Color.web("#C4B5FD", 0.6),
                textColor = Color.WHITE
            )
        }

        /**
         * Apply theme to a pane with smooth animation
         */
        fun applyThemeToPane(pane: Pane, theme: WeatherTheme, animate: Boolean = true) {
            val gradient = LinearGradient(
                0.0, 0.0, 0.0, 1.0, true, CycleMethod.NO_CYCLE,
                theme.gradientStops
            )

            if (animate) {
                // Animate background transition
                val timeline = Timeline()
                timeline.keyFrames.add(
                    KeyFrame(
                        Duration.millis(2000.0),
                        KeyValue(pane.opacityProperty(), 1.0, Interpolator.EASE_BOTH)
                    )
                )
                pane.background = javafx.scene.layout.Background(
                    javafx.scene.layout.BackgroundFill(gradient, null, null)
                )
                timeline.play()
            } else {
                pane.background = javafx.scene.layout.Background(
                    javafx.scene.layout.BackgroundFill(gradient, null, null)
                )
            }
        }
    }
}
