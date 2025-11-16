package com.weatherdesk.ui.components

import javafx.animation.*
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.util.Duration
import kotlin.math.*

/**
 * Interactive 3D-style globe for selecting locations
 * Users can drag to rotate and click to select coordinates
 */
class InteractiveGlobe : StackPane() {

    private val canvas = Canvas(400.0, 400.0)
    private val gc: GraphicsContext = canvas.graphicsContext2D

    // Globe properties
    private var rotationX = 0.0
    private var rotationY = 0.0
    private var targetRotationX = 0.0
    private var targetRotationY = 0.0
        private var isDirty = true  // Dirty flag for optimized rendering

    // Interaction
    private var isDragging = false
    private var lastMouseX = 0.0
    private var lastMouseY = 0.0

    // Animation
    private var animationTimer: AnimationTimer? = null
    private val markerPositions = mutableListOf<GlobeMarker>()

    // Callbacks
    var onLocationSelected: ((latitude: Double, longitude: Double) -> Unit)? = null

    data class GlobeMarker(
        val latitude: Double,
        val longitude: Double,
        val label: String,
        var pulsePhase: Double = 0.0
    )

    init {
        children.add(canvas)
        prefWidth = 400.0
        prefHeight = 400.0

        setupInteraction()
        startAnimation()

        // Add some popular city markers
        addMarker(51.5074, -0.1278, "London")
        addMarker(40.7128, -74.0060, "New York")
        addMarker(35.6762, 139.6503, "Tokyo")
        addMarker(-33.8688, 151.2093, "Sydney")
        addMarker(48.8566, 2.3522, "Paris")
    }

    /**
     * Add a marker to the globe
     */
    fun addMarker(latitude: Double, longitude: Double, label: String) {
        markerPositions.add(GlobeMarker(latitude, longitude, label))
    }

    /**
     * Clear all markers
     */
    fun clearMarkers() {
        markerPositions.clear()
    }

    /**
     * Setup mouse interaction
     */
    private fun setupInteraction() {
        canvas.setOnMousePressed { event ->
            isDragging = true
            lastMouseX = event.x
            lastMouseY = event.y
        }

        canvas.setOnMouseDragged { event ->
            if (isDragging) {
                val deltaX = event.x - lastMouseX
                val deltaY = event.y - lastMouseY

                targetRotationY += deltaX * 0.01
                targetRotationX -= deltaY * 0.01

                // Clamp X rotation
                targetRotationX = targetRotationX.coerceIn(-PI / 2, PI / 2)

                lastMouseX = event.x
                lastMouseY = event.y
            }
        }

        canvas.setOnMouseReleased {
            isDragging = false
        }

        canvas.setOnMouseClicked { event ->
            if (!isDragging) {
                handleClick(event)
            }
        }

        // Auto-rotate when not dragging
        canvas.setOnMouseExited {
            isDragging = false
        }
    }

    /**
     * Handle click to select location
     */
    private fun handleClick(event: MouseEvent) {
        val centerX = canvas.width / 2
        val centerY = canvas.height / 2
        val radius = min(centerX, centerY) * 0.8

        val dx = event.x - centerX
        val dy = event.y - centerY
        val distance = sqrt(dx * dx + dy * dy)

        if (distance <= radius) {
            // Convert click position to lat/lon
            val x = dx / radius
            val y = dy / radius

            // Approximate coordinate calculation
            val latitude = asin(-y) * 180.0 / PI
            val longitude = atan2(x, cos(rotationX)) * 180.0 / PI + rotationY * 180.0 / PI

            onLocationSelected?.invoke(
                latitude.coerceIn(-90.0, 90.0),
                ((longitude + 180) % 360 - 180).coerceIn(-180.0, 180.0)
            )

            // Visual feedback
            animateClickFeedback(event.x, event.y)
        }
    }

    /**
     * Animate click feedback
     */
    private fun animateClickFeedback(x: Double, y: Double) {
        val ripple = javafx.scene.shape.Circle(x, y, 5.0)
        ripple.fill = Color.TRANSPARENT
        ripple.stroke = Color.rgb(100, 200, 255, 0.8)
        ripple.strokeWidth = 2.0
        children.add(ripple)

        val timeline = Timeline(
            KeyFrame(
                Duration.millis(500.0),
                KeyValue(ripple.radiusProperty(), 50.0),
                KeyValue(ripple.opacityProperty(), 0.0)
            )
        )
        timeline.setOnFinished { children.remove(ripple) }
        timeline.play()
    }

    /**
     * Start animation loop
     */
    private fun startAnimation() {
        animationTimer = object : AnimationTimer() {
            private var lastTime = 0L

            override fun handle(now: Long) {
                if (lastTime == 0L) {
                    lastTime = now
                    return
                }

                val deltaTime = (now - lastTime) / 1_000_000_000.0
                lastTime = now

                update(deltaTime)
                render()
            }
        }
        animationTimer?.start()
    }

    /**
     * Update globe state
     */
    private fun update(deltaTime: Double) {
        // Smooth rotation interpolation
        rotationX += (targetRotationX - rotationX) * 5.0 * deltaTime
        rotationY += (targetRotationY - rotationY) * 5.0 * deltaTime
                isDirty = true  // Mark for redraw since rotation changed

        // Auto-rotate when not dragging
        if (!isDragging) {
            targetRotationY += 0.1 * deltaTime
                        isDirty = true  // Mark for redraw
        }

        // Update marker pulse animation
        markerPositions.forEach {
            it.pulsePhase += deltaTime * 2.0
        }
    }

    /**
     * Render the globe
     */
    private fun render() {
                if (!isDirty) return  // Skip render if nothing changed
        val width = canvas.width
        val height = canvas.height
        val centerX = width / 2
        val centerY = height / 2
        val radius = min(centerX, centerY) * 0.8

        // Clear canvas
        gc.clearRect(0.0, 0.0, width, height)

        // Draw space background
        gc.fill = Color.rgb(10, 15, 30)
        gc.fillRect(0.0, 0.0, width, height)

        // Draw stars
        for (i in 0..100) {
            val starX = (i * 73 % width.toInt()).toDouble()
            val starY = (i * 137 % height.toInt()).toDouble()
            val starSize = (i % 3 + 1).toDouble()
            val alpha = (sin(i + rotationY * 10) * 0.3 + 0.7)
            gc.fill = Color.rgb(255, 255, 255, alpha)
            gc.fillOval(starX, starY, starSize, starSize)
        }

        // Draw globe sphere
        drawGlobeSphere(centerX, centerY, radius)

        // Draw latitude/longitude lines
        drawGridLines(centerX, centerY, radius)

        // Draw markers
        drawMarkers(centerX, centerY, radius)

        // Draw glow effect
        drawGlowEffect(centerX, centerY, radius)
                isDirty = false  // Mark as rendered
    }

    /**
     * Draw the main globe sphere
     */
    private fun drawGlobeSphere(centerX: Double, centerY: Double, radius: Double) {
        // Shadow
        gc.fill = Color.rgb(0, 0, 0, 0.3)
        gc.fillOval(centerX - radius + 10, centerY - radius + 10, radius * 2, radius * 2)

        // Main sphere with gradient
        val gradient = javafx.scene.paint.RadialGradient(
            0.0, 0.0, 0.3, 0.3, 0.7, true,
            javafx.scene.paint.CycleMethod.NO_CYCLE,
            javafx.scene.paint.Stop(0.0, Color.rgb(40, 100, 180)),
            javafx.scene.paint.Stop(0.7, Color.rgb(20, 60, 120)),
            javafx.scene.paint.Stop(1.0, Color.rgb(10, 30, 60))
        )
        gc.fill = gradient
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2)
    }

    /**
     * Draw grid lines on globe
     */
    private fun drawGridLines(centerX: Double, centerY: Double, radius: Double) {
        gc.stroke = Color.rgb(100, 150, 200, 0.3)
        gc.lineWidth = 1.0

        // Draw latitude lines
        for (lat in -80..80 step 20) {
            drawLatitudeLine(centerX, centerY, radius, lat.toDouble())
        }

        // Draw longitude lines
        for (lon in 0..360 step 30) {
            drawLongitudeLine(centerX, centerY, radius, lon.toDouble())
        }
    }

    /**
     * Draw a latitude line
     */
    private fun drawLatitudeLine(centerX: Double, centerY: Double, radius: Double, latitude: Double) {
        val points = mutableListOf<Pair<Double, Double>>()

        for (lon in 0..360 step 5) {
            val point = project3DPoint(latitude, lon.toDouble(), radius)
            if (point != null) {
                points.add(Pair(centerX + point.first, centerY + point.second))
            }
        }

        if (points.size > 1) {
            for (i in 0 until points.size - 1) {
                gc.strokeLine(points[i].first, points[i].second, points[i + 1].first, points[i + 1].second)
            }
        }
    }

    /**
     * Draw a longitude line
     */
    private fun drawLongitudeLine(centerX: Double, centerY: Double, radius: Double, longitude: Double) {
        val points = mutableListOf<Pair<Double, Double>>()

        for (lat in -90..90 step 5) {
            val point = project3DPoint(lat.toDouble(), longitude, radius)
            if (point != null) {
                points.add(Pair(centerX + point.first, centerY + point.second))
            }
        }

        if (points.size > 1) {
            for (i in 0 until points.size - 1) {
                gc.strokeLine(points[i].first, points[i].second, points[i + 1].first, points[i + 1].second)
            }
        }
    }

    /**
     * Draw location markers
     */
    private fun drawMarkers(centerX: Double, centerY: Double, radius: Double) {
        markerPositions.forEach { marker ->
            val point = project3DPoint(marker.latitude, marker.longitude, radius)
            if (point != null) {
                val (x, y, z) = point

                // Only draw if visible (z > 0 means front-facing)
                if (z > 0) {
                    val screenX = centerX + x
                    val screenY = centerY + y

                    // Pulsing marker
                    val pulse = sin(marker.pulsePhase) * 0.3 + 0.7
                    val markerSize = 8.0 * pulse

                    // Draw marker glow
                    gc.fill = Color.rgb(255, 100, 100, 0.3 * pulse)
                    gc.fillOval(screenX - markerSize, screenY - markerSize, markerSize * 2, markerSize * 2)

                    // Draw marker
                    gc.fill = Color.rgb(255, 50, 50, 0.9)
                    gc.fillOval(screenX - markerSize / 2, screenY - markerSize / 2, markerSize, markerSize)

                    // Draw label
                    gc.fill = Color.WHITE
                    gc.fillText(marker.label, screenX + markerSize, screenY - markerSize)
                }
            }
        }
    }

    /**
     * Draw glow effect around globe
     */
    private fun drawGlowEffect(centerX: Double, centerY: Double, radius: Double) {
        val gradient = javafx.scene.paint.RadialGradient(
            0.0, 0.0, 0.5, 0.5, 0.6, true,
            javafx.scene.paint.CycleMethod.NO_CYCLE,
            javafx.scene.paint.Stop(0.8, Color.TRANSPARENT),
            javafx.scene.paint.Stop(1.0, Color.rgb(100, 150, 255, 0.2))
        )
        gc.fill = gradient
        gc.fillOval(centerX - radius * 1.1, centerY - radius * 1.1, radius * 2.2, radius * 2.2)
    }

    /**
     * Project 3D point to 2D with rotation
     * Returns (x, y, z) where z indicates depth (for culling)
     */
    private fun project3DPoint(latitude: Double, longitude: Double, radius: Double): Triple<Double, Double, Double>? {
        val lat = Math.toRadians(latitude)
        val lon = Math.toRadians(longitude) + rotationY

        // Convert to 3D coordinates
        var x = radius * cos(lat) * sin(lon)
        var y = -radius * sin(lat)
        var z = radius * cos(lat) * cos(lon)

        // Apply X rotation
        val newY = y * cos(rotationX) - z * sin(rotationX)
        val newZ = y * sin(rotationX) + z * cos(rotationX)
        y = newY
        z = newZ

        // Simple perspective projection
        val distance = 1000.0
        val scale = distance / (distance + z)

        return Triple(x * scale, y * scale, z)
    }

    /**
     * Stop animation and cleanup
     */
    fun dispose() {
        animationTimer?.stop()
    }
}
