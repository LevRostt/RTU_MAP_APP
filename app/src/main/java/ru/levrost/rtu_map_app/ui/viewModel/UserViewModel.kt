package ru.levrost.rtu_map_app.ui.viewModel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.geometry.Point
import ru.levrost.rtu_map_app.data.model.UserData
import ru.levrost.rtu_map_app.data.repositories.UserDataRepo
import ru.levrost.rtu_map_app.global.ResultStatus
import ru.levrost.rtu_map_app.global.debugLog
import java.lang.NullPointerException
import java.lang.ref.WeakReference

class UserViewModel(private val application: Application) : AndroidViewModel(application) {
    private val repo: UserDataRepo = UserDataRepo.getInstance(WeakReference(application.applicationContext))
    private var _userData: LiveData<UserData> = repo.getData()
    val userData get() = _userData

    private var _cardProfileUserData = arrayOf("", "") //Данные пользователя фрагмент которого нужно открыть (имя, id)
    val cardProfileUserData get() = _cardProfileUserData

    private var userPoint: Point = Point(55.7515, 37.64)

    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)

    init{
        updateData()
    }

    fun register(username: String, password : String): LiveData<ResultStatus>{
        return repo.register(username, password)
    }

    fun login(username: String, password: String): LiveData<ResultStatus>{
        return repo.login(username, password)
    }

    private fun updateData() : Boolean{
        if ((ActivityCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) ||
            !((application.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
                .isProviderEnabled(
                LocationManager.GPS_PROVIDER))
        ){
            return false
        }

        fusedLocationProviderClient.lastLocation.addOnCompleteListener( application.mainExecutor ) {
            if (_userData.value != null) {
                val location = it.result
                val tempData = _userData.value!!

                try {
                    tempData.apply {
                        latitude = location.latitude
                        longitude = location.longitude
                    }
                } catch (_: NullPointerException){return@addOnCompleteListener}

                repo.updateData(tempData)
                _userData = repo.getData()
            }
        }
        return true
    }

    override fun onCleared() {
        super.onCleared()
        debugLog("cleared")
    }

    fun setCardProfileUserData(name: String, userId: String) {
        _cardProfileUserData = arrayOf(name, userId)
    }

    fun getUser(): LiveData<UserData> {
        updateData()
        return _userData
    }

    fun getCachedUser() : UserData? {
        return repo.cacheData
    }

    fun loginAsGuest(){
        val tempData = UserData("guest", "0")
        application.getSharedPreferences("UNAME", AppCompatActivity.MODE_PRIVATE)
            .edit()
            .putString("name", "0")
            .apply()
        repo.updateData(tempData)
    }

    fun deleteUser() {
        repo.deleteData()
    }


    @Deprecated("Now all logic only on server")
    fun likePlace(id : String) {
        if (_userData.value != null) {
            val tempData = _userData.value!!
            tempData.like(id)
            repo.updateData(tempData)
        }
    }

    @Deprecated("Now all logic only on server")
    fun unLikePlace(id : String) {
        if (_userData.value != null) {
            val tempData = _userData.value!!
            tempData.unLike(id)
            repo.updateData(tempData)
        }
    }

    fun subscribe(id : String) {
        if (_userData.value != null) {
            val tempData = _userData.value!!
            tempData.subscribe(id)
            repo.updateData(tempData)
        }
    }

    fun unscribe(id : String) {
        if (_userData.value != null) {
            val tempData = _userData.value!!
            tempData.unscribe(id)
            repo.updateData(tempData)
        }
    }

    private val observer = Observer<UserData> { user ->
        userPoint = if (user.latitude != 0.0 && user.longitude != 0.0) {
            Point(user.latitude, user.longitude)
        } else {
            Point(55.7515, 37.64)
        }
    }

    fun userPoint(viewLifecycleOwner: LifecycleOwner) : Point{
        getUser().observe(viewLifecycleOwner, observer) // LiveData не посылает изменения о своих обновлениях,
        getUser().removeObserver(observer)               // поэтому подписка нужна только для моментального получения данных
        return userPoint
    }

    companion object{
        val Factory: ViewModelProvider.Factory = object :  ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                return UserViewModel(application) as T
            }
        }
    }

}