package com.antigravity.eframe.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class ProximitySensorManager(
    context: Context,
    private val onProximityNear: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val proximitySensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private var isListening = false
    private var lastNearTimestamp = 0L

    val isSupported: Boolean
        get() = proximitySensor != null

    fun startListening() {
        if (isListening || proximitySensor == null) return
        // Use SENSOR_DELAY_NORMAL (approx 5 Hz) instead of SENSOR_DELAY_UI (60 Hz)
        // to avoid flooding the CPU on low-end tablets
        sensorManager?.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        isListening = true
        Log.d(TAG, "ProximitySensorManager: listening started with low CPU delay")
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
        Log.d(TAG, "ProximitySensorManager: listening stopped")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_PROXIMITY) return

        val distance = event.values[0]
        val maxRange = proximitySensor?.maximumRange ?: 5f

        val isNear = distance < maxRange || distance < 4f

        if (isNear) {
            val now = System.currentTimeMillis()
            // Debounce to prevent multiple triggers within 800ms
            if (now - lastNearTimestamp > 800) {
                lastNearTimestamp = now
                Log.d(TAG, "Proximity detected: hand near ($distance cm)")
                onProximityNear()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        private const val TAG = "ProximitySensorManager"
    }
}
