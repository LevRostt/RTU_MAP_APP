package ru.levrost.rtu_map_app.ui.view.Activity

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.yandex.mapkit.MapKitFactory
import ru.levrost.rtu_map_app.R


class MainActivity : AppCompatActivity() {

    private val MAPKIT_API_KEY = "da88c11a-ce91-46e7-bfa8-ab8a2c9d90a0"
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge() //edge to edge ломает возможность смены цвета в статус баре
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decor = window.decorView
//            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        window.statusBarColor = resources.getColor(R.color.base_brown_statusBar, theme)

        MapKitFactory.setApiKey(MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
    }
}