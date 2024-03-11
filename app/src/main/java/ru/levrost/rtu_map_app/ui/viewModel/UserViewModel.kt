package ru.levrost.rtu_map_app.ui.viewModel

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.geometry.Point
import ru.levrost.rtu_map_app.data.model.UserData
import ru.levrost.rtu_map_app.data.repositories.UserDataRepo
import ru.levrost.rtu_map_app.ui.view.Activity.MainActivity

class UserViewModel(private val application: Application) : AndroidViewModel(application) {
    private val repo: UserDataRepo = UserDataRepo(application)
    private var userData: LiveData<UserData> = repo.getData()

    private var userPoint: Point = Point(55.7515, 37.64)

    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)

    init{
        updateData()
    }

    private fun updateData() : Boolean{
        if (ActivityCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        fusedLocationProviderClient.lastLocation.addOnCompleteListener( application.mainExecutor ) {
            if (userData.value != null) {
                val location = it.result
                val tempData = userData.value!!

                tempData.apply {
                    latitude = location.latitude
                    longitude = location.longitude
                }

                repo.updateData(tempData)
                userData = repo.getData()
            }
        }
        return true
    }

    fun getUser(): LiveData<UserData> {
        updateData()
        return userData
    }

    fun login(name : String, userId : String){
        val tempData = UserData(name, userId)
        repo.updateData(tempData)
    }

    fun deleteUser() {
        repo.deleteData()
    }

    fun likePlace(id : String) {
        if (userData.value != null) {
            val tempData = userData.value!!
            tempData.like(id)
            repo.updateData(tempData)
        }
    }

    fun subscribe(id : String) {
        if (userData.value != null) {
            val tempData = userData.value!!
            tempData.subscribe(id)
            repo.updateData(tempData)
        }
    }
    fun unLikePlace(id : String) {
        if (userData.value != null) {
            val tempData = userData.value!!
            tempData.unLike(id)
            repo.updateData(tempData)
        }
    }

    fun unscribe(id : String) {
        if (userData.value != null) {
            val tempData = userData.value!!
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