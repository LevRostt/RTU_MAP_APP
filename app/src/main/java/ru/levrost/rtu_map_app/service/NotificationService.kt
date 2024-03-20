package ru.levrost.rtu_map_app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.data.model.Place
import ru.levrost.rtu_map_app.data.repositories.PlaceListRepo
import ru.levrost.rtu_map_app.data.repositories.UserDataRepo
import ru.levrost.rtu_map_app.global.debugLog
import ru.levrost.rtu_map_app.ui.view.Activity.MainActivity

class NotificationService : Service() {

    private val NOTIFICATION_ID = 1337666
    private val CHANNEL_ID = "NotificationServiceChannel"
    private var isServiceRunning = false

    private var placeRepo : PlaceListRepo? = PlaceListRepo.getInstance()
    private var userRepo : UserDataRepo? = UserDataRepo.getInstance()
    private var oldList : List<Place>? = null
    private lateinit var notificationManager : NotificationManager
    private lateinit var job: Job

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isServiceRunning || (intent != null && intent.action == "STOP_SERVICE")) {
            if (intent != null && "STOP_SERVICE" == intent.action) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                job.cancel()
            }

            createNotificationChannel()

            startNotification()

            createNotificationGroup()

            job = CoroutineScope(Dispatchers.Main).launch {
                while (isActive){

                    placeRepo?.getFromServer()
                    delay(1000)
                    cheackUpdateData()
                    delay(15000)
                }
            }

            isServiceRunning = true
        }

        return START_STICKY
    }

    private fun cheackUpdateData() {

        if (oldList == null){
            oldList = placeRepo?.cacheData
        }

        val subscribe = userRepo?.cacheData?.subUsers
        val newList = placeRepo?.cacheData

        debugLog(subscribe.toString())

        if (oldList != null && subscribe != null && oldList!!.size != newList!!.size) {

            val uniquePlaceList = newList.filter {newPlace -> //Получаем уникальные места которых ранее не было*
                !oldList!!.any { oldPlace ->
                    oldPlace.idPlace == newPlace.idPlace
                } && subscribe.any {userId ->
                    newPlace.userId == userId
                }
            }

            debugLog(uniquePlaceList.toString())

            for (place in uniquePlaceList){
                createNotification(place.userName)
            }

            oldList = newList
        }
    }

    private fun createNotification(name: String) {

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.map_filled)
            .setContentTitle(name)
            .setContentText(ContextCompat.getString(applicationContext, R.string.user) + " $name " + ContextCompat.getString(applicationContext, R.string.add_new_place))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(NOTIFICATION_ID.toString())
            .build()

        // Отправка уведомления
        notificationManager.notify(name.hashCode(), notification)
    }

    private fun startNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, NotificationService::class.java)
        stopIntent.setAction("STOP_SERVICE")
        val pendingStopIntent =
            PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText(ContextCompat.getString(applicationContext, R.string.notification_no_place))
            .setSmallIcon(R.drawable.map_filled)
            .setGroup(NOTIFICATION_ID.toString())
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.delete_icon, ContextCompat.getString(applicationContext, R.string.dont_track), pendingStopIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "RTU_MAP_APP_", NotificationManager.IMPORTANCE_HIGH)
        channel.description = "Channel for My Foreground Service"

        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel) //channel
    }

    private fun createNotificationGroup() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.map_filled)
            .setContentText(ContextCompat.getString(applicationContext, R.string.project_name))
            .setGroup(NOTIFICATION_ID.toString())
            .setGroupSummary(true)
            .build()

        notificationManager.notify(-NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        PlaceListRepo.detach()
        UserDataRepo.detach()
        placeRepo = null
        userRepo = null
        isServiceRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}