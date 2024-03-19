package ru.levrost.rtu_map_app.data.repositories

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.levrost.rtu_map_app.data.dataSource.retrofit.ApiClient
import ru.levrost.rtu_map_app.data.dataSource.retrofit.ServerApi
import ru.levrost.rtu_map_app.data.dataSource.retrofit.model.UserFromServer
import ru.levrost.rtu_map_app.data.dataSource.retrofit.model.UserToServer
import ru.levrost.rtu_map_app.data.dataSource.retrofit.model.UserTokenFromServer
import ru.levrost.rtu_map_app.data.dataSource.room.entites.UserEntity
import ru.levrost.rtu_map_app.data.dataSource.room.root.AppDataBase
import ru.levrost.rtu_map_app.data.model.UserData
import ru.levrost.rtu_map_app.global.observeOnce

class UserDataRepo private constructor(private val context: Application) {
    private var dataBaseSource : AppDataBase = AppDataBase.getDataBase(context)
    private val serverApi = ApiClient.getClient().create(ServerApi::class.java)
    private var _cachedData : UserData? = null
    val cacheData get() = _cachedData

    companion object{
        private var _instance : UserDataRepo? = null
        fun getInstance(context: Application): UserDataRepo {
            if (_instance == null){
                _instance = UserDataRepo(context)
            }
            return _instance!!
        }

        fun getInstance(): UserDataRepo?{
            return _instance
        }
    }

    fun getData(): LiveData<UserData> {
        return dataBaseSource.userDao()?.getData()?.map { userEntity ->
            val data = UserData(userEntity.name, userEntity.userId, userEntity.latitude, userEntity.longitude,userEntity.subscribes, userEntity.likes)
            _cachedData = data //Кэшируем данные для последующего к ним обращения без запроса к серверу
            return@map data
        } ?: MutableLiveData(UserData("-1", "-1"))
    }

    fun register(username : String, password : String){
        serverApi.register(UserToServer(username, password)).enqueue(object :
            Callback<UserFromServer> {
            override fun onResponse(call: Call<UserFromServer>, response: Response<UserFromServer>) {
                if (response.isSuccessful){
                    login(username, password)
                } else{
                    Log.d("LRDebugServer", "response ${response.code()} ; ${response.errorBody()?.string()} ")
                }
            }

            override fun onFailure(call: Call<UserFromServer>, t: Throwable) {
                Log.d("LRDebugServer", "Failed $t")
            }

        })
    }

    fun login(username: String, password: String){
        serverApi.login(username, password).enqueue(object : Callback<UserTokenFromServer>{
            override fun onResponse(
                call: Call<UserTokenFromServer>,
                response: Response<UserTokenFromServer>
            ) {
                if (response.isSuccessful){
                    val user = response.body()!!
                    updateData(UserData(username, user.access_token))
                } else{
                    Log.d("LRDebugServer", "response ${response.code()} ; ${response.errorBody()?.string()} ")
                }
            }

            override fun onFailure(call: Call<UserTokenFromServer>, t: Throwable) {
                Log.d("LRDebugServer", "Failed $t")
            }

        })
    }

    fun updateData(data : UserData) {
        _cachedData = data

        context.getSharedPreferences(
            "UNAME",
            AppCompatActivity.MODE_PRIVATE
        )
            .edit()
            .putString("name", data.name)
            .putString("access_token", data.userId)
            .putString("token_type", "bearer")
            .apply()

        AppDataBase.databaseWriteExecutor.execute {
            dataBaseSource.userDao()?.editProfile(UserEntity(0, data.name, data.userId, data.likes, data.subUsers, data.latitude, data.longitude))
        }
    }

    fun deleteData() {
        context.getSharedPreferences("UNAME", AppCompatActivity.MODE_PRIVATE)
            .edit()
            .putString("access_token", null)
            .putString("token_type", null)
            .putString("name", "-1")
            .apply()
        updateData(UserData("-1", "-1"))
    }


}