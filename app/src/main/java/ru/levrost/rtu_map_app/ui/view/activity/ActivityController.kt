package ru.levrost.rtu_map_app.ui.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import ru.levrost.rtu_map_app.R

class ActivityController(val activity: MainActivity) {

    private var navTopestController: NavController? = null
    private var lastBackPress: Long = 0

    fun showCustomTopPanel(){
        activity.window.statusBarColor = activity.resources.getColor(R.color.base_brown_statusBar, activity.theme) // Рисую кастомную верхнюю полоску
    }

    val onBackPressCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            activity.apply {
                if (isStartDestination(navTopestController?.currentDestination)) {
                    if (lastBackPress + 2500 > System.currentTimeMillis())
                        finish()
                    else {
                        Log.d("MyDebugMess", "Need a toast")
                        Toast.makeText(
                            baseContext,
                            "Press again to exit",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    lastBackPress = System.currentTimeMillis()
                } else {
                    navTopestController?.popBackStack()
                }
            }
        }
    }

    val fragmentListener =
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

    fun showNotificationPermissionDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle(ContextCompat.getString(context, R.string.en_notification_request))
            setMessage(ContextCompat.getString(context, R.string.en_notification_mess))
            setPositiveButton(ContextCompat.getString(context, R.string.settings)) { _, _ ->
                openNotificationSettings(context)
            }
            setNegativeButton(ContextCompat.getString(context, R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun openNotificationSettings(context: Context) {
        val intent = Intent()
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        intent.putExtra("android.provider.extra.APP_PACKAGE", context.packageName)
        context.startActivity(intent)
    }


    fun navRestart(){
        activity.apply {


            userViewModel.getUser().observe(this) {}

            navTopestController = (supportFragmentManager
                .findFragmentById(R.id.mainGraphContainer) as NavHostFragment)
                .navController

            val navGraph: NavGraph =
                navTopestController!!.navInflater.inflate(R.navigation.auth_nav_graph)

            if (isLogin()) {
                navGraph.setStartDestination(R.id.mainFragment)
                navTopestController!!.graph = navGraph
            } else {
                userViewModel.deleteUser()
                navTopestController!!.popBackStack(R.id.loginFragment, false)
                navTopestController!!.graph = navGraph
            }
        }
    }

    private fun isLogin(): Boolean {
        activity.apply {
            val uName = getSharedPreferences("UNAME", AppCompatActivity.MODE_PRIVATE).getString(
                "name",
                "-1"
            )
            //debugLog("uName on activity = $uName")
            return uName != "-1"
        }
    }

    private fun isStartDestination(destination: NavDestination?) : Boolean{
        val startDestinations = listOf(R.id.mapFragment, R.id.loginFragment)
        return startDestinations.contains(destination?.id)
    }

}