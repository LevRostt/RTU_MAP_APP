package ru.levrost.rtu_map_app.ui.view.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.MapKitFactory
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.global.debugLog
import ru.levrost.rtu_map_app.global.observeOnce
import ru.levrost.rtu_map_app.service.NotificationService
import ru.levrost.rtu_map_app.ui.viewModel.PlaceListViewModel
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel


class MainActivity : AppCompatActivity() {

    private var navTopestController: NavController? = null
    private var lastBackPress: Long = 0

    private val userViewModel: UserViewModel by viewModels {
        UserViewModel.Factory
    }

    private val placeListViewModel: PlaceListViewModel by viewModels {
        PlaceListViewModel.Factory
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = resources.getColor(R.color.base_brown_statusBar, theme) // Рисую кастомную верхнюю полоску

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentListiner, true) // Вешаю слушатель на каждый фрагмент

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isStartDestination(navTopestController?.currentDestination)){
                    if (lastBackPress + 2500 > System.currentTimeMillis())
                        finish()
                    else{
                        Log.d("MyDebugMess", "Need a toast")
                        Toast.makeText(
                            baseContext,
                            "Press again to exit",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    lastBackPress = System.currentTimeMillis()
                }
                else {
                    navTopestController?.popBackStack()
                }
            }
        })

        navRestart()

        val serviceIntent = Intent(application, NotificationService::class.java)
        application.startForegroundService(serviceIntent)
    }

    private val fragmentListiner =
        object : FragmentManager.FragmentLifecycleCallbacks(){ // Слушатель получает для активити контроллер активного экрана
            override fun onFragmentViewCreated(
                fm: FragmentManager,
                f: Fragment,
                v: View,
                savedInstanceState: Bundle?
            ) {
                super.onFragmentViewCreated(fm, f, v, savedInstanceState)
                if (navTopestController != f.findNavController())
                    navTopestController = f.findNavController()
            }

        }

    fun navRestart(){

        navTopestController = (supportFragmentManager
            .findFragmentById(R.id.mainGraphContainer) as NavHostFragment)
            .navController

        val navGraph: NavGraph = navTopestController!!.navInflater.inflate(R.navigation.auth_nav_graph)

        if (isLogin()) {
            navGraph.setStartDestination(R.id.mainFragment)
            navTopestController!!.graph = navGraph
        } else {
            userViewModel.deleteUser()
            navTopestController!!.popBackStack(R.id.loginFragment, false)
            navTopestController!!.graph = navGraph
        }

    }

    private fun isLogin(): Boolean {
        val uName = getSharedPreferences("UNAME", MODE_PRIVATE).getString("name", "-1")
        debugLog("uName on activity = $uName")
        return uName != "-1"
    }

    private fun isStartDestination(destination: NavDestination?) : Boolean{
        val startDestinations = listOf(R.id.mapFragment, R.id.loginFragment)
        return startDestinations.contains(destination?.id)
    }


    override fun onDestroy() {
        super.onDestroy()
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentListiner)
    }

}