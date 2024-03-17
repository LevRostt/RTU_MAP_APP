package ru.levrost.rtu_map_app.data.repositories

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import ru.levrost.rtu_map_app.data.dataSource.room.entites.UserEntity
import ru.levrost.rtu_map_app.data.dataSource.room.root.AppDataBase
import ru.levrost.rtu_map_app.data.model.UserData

class UserDataRepo(private val application: Application) : repository<UserData> {
    private var dataBaseSource : AppDataBase = AppDataBase.getDataBase(application)
    override fun getData(): LiveData<UserData> {
        Log.d("MyDebugMess", dataBaseSource.toString()+ " " + dataBaseSource.userDao().toString())
        return dataBaseSource.userDao()?.getData()?.map { userEntity ->
            UserData(userEntity.name, userEntity.userId, userEntity.latitude, userEntity.longitude,userEntity.subscribes, userEntity.likes)
        } ?: MutableLiveData(UserData("none", "-1"))
    }

    fun updateData(data : UserData) {
        AppDataBase.databaseWriteExecutor.execute {
            dataBaseSource.userDao()?.editProfile(UserEntity(0, data.name, data.userId, data.likes, data.subUsers, data.latitude, data.longitude))
        }
    }

    fun deleteData() {
        updateData(UserData("none", "-1"))
    }

}