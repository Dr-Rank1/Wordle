package com.lexiguess.app.ui.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.lexiguess.app.ui.theme.LocalTiltParallaxEnabled

data class TiltAngles(
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
)

/**
 * Returns dynamic pitch (rotationX) and roll (rotationY) angles based on physical device sensors.
 * When disabled via LocalTiltParallaxEnabled, registers no listeners and returns (0, 0).
 */
@Composable
fun rememberDeviceTilt(): TiltAngles {
    val enabled = LocalTiltParallaxEnabled.current
    if (!enabled) return TiltAngles(0f, 0f)

    val context = LocalContext.current
    val tiltState = remember { mutableStateOf(TiltAngles(0f, 0f)) }

    DisposableEffect(enabled, context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (sensorManager == null || sensor == null) {
            return@DisposableEffect onDispose {}
        }

        var smoothedX = 0f
        var smoothedY = 0f
        val alpha = 0.15f // Low-pass filter smoothing coefficient

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val rawX = event.values[0] // Lateral tilt (roll): -10 to 10 m/s^2
                val rawY = event.values[1] // Longitudinal tilt (pitch): -10 to 10 m/s^2

                // Smooth raw sensor readings to remove jitter
                smoothedX += alpha * (rawX - smoothedX)
                smoothedY += alpha * (rawY - smoothedY)

                // Phone is typically held at ~45-55 deg pitch in hand (~5.5-6.5 m/s^2)
                val calibratedPitch = smoothedY - 6.0f
                val calibratedRoll = -smoothedX

                // Map to subtle, natural tilt rotation angles: max +/- 6 degrees
                val rotX = (calibratedPitch * 0.9f).coerceIn(-6f, 6f)
                val rotY = (calibratedRoll * 0.9f).coerceIn(-7f, 7f)

                tiltState.value = TiltAngles(rotationX = rotX, rotationY = rotY)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    return tiltState.value
}
