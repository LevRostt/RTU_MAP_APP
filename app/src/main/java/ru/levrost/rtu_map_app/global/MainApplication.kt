package ru.levrost.rtu_map_app.global

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class MainApplication : Application() {
    private val MAPKIT_API_KEY = "da88c11a-ce91-46e7-bfa8-ab8a2c9d90a0"
    override fun onCreate() {
        super.onCreate()

        try {
            MapKitFactory.setApiKey(MAPKIT_API_KEY)
        } catch (_: AssertionError){}
    }
}