package com.hridoy.arsceneviewcomposesample.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.Channel

class SensorDataManager(context: Context) : SensorEventListener {

    private val sensorManager by lazy { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    fun init() {
        // Choose sensor
        val mLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        // register listener
        sensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private var lightmeter: Float? = null

    // センサーデータを継続的に読み取る
    val data: Channel<SensorData> = Channel(Channel.BUFFERED)

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            lightmeter = event.values[0]
        }

        if(lightmeter != null){
            data.trySend(
                SensorData(
                    light = lightmeter!!
                )
            )

        }

    }
    // センサーサービスを停止
    fun cancel() {
        sensorManager.unregisterListener(this)
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

}