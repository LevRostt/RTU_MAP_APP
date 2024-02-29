package ru.levrost.rtu_map_app.ui.view.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.yandex.mapkit.MapKitFactory
import ru.levrost.rtu_map_app.R

class MainActivity : AppCompatActivity() {

    private val MAPKIT_API_KEY = "da88c11a-ce91-46e7-bfa8-ab8a2c9d90a0"
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MapKitFactory.setApiKey(MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
    }
}