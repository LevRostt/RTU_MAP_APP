package ru.levrost.rtu_map_app.ui.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.service.NotificationService
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel


class MainActivity : AppCompatActivity() {

    private var _activityController : ActivityController? = null
    private val activityController get() = _activityController!!

    val userViewModel: UserViewModel by viewModels {
        UserViewModel.Factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _activityController = ActivityController(this)

        activityController.showCustomTopPanel()

        supportFragmentManager.registerFragmentLifecycleCallbacks(activityController.fragmentListener, true) // Вешаю слушатель на каждый фрагмент

        onBackPressedDispatcher.addCallback(this, activityController.onBackPressCallback)

        navRestart()
    }

    override fun onStart() {
        super.onStart()
        checkAndRequestNotificationPermission(this)
        val serviceIntent = Intent(application, NotificationService::class.java)
        application.startForegroundService(serviceIntent)
    }

    fun checkAndRequestNotificationPermission(context: Context) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            activityController.showNotificationPermissionDialog(context)
        }
    }

    fun navRestart(){
        activityController.navRestart()
    }

    override fun onDestroy() {
        super.onDestroy()
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(activityController.fragmentListener)
        _activityController = null
    }

}