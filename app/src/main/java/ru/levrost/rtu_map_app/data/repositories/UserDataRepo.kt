package ru.levrost.rtu_map_app.data.repositories

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import okhttp3.internal.userAgent
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

class UserDataRepo(private val application: Application) : repository<UserData> {
    private var dataBaseSource : AppDataBase = AppDataBase.getDataBase(application)
    private val serverApi = ApiClient.getClient().create(ServerApi::class.java)


    override fun getData(): LiveData<UserData> {
        return dataBaseSource.userDao()?.getData()?.map { userEntity ->
            UserData(userEntity.name, userEntity.userId, userEntity.latitude, userEntity.longitude,userEntity.subscribes, userEntity.likes)
        } ?: MutableLiveData(UserData("none", "-1"))
    }

    fun register(username : String, password : String){
        serverApi.register(UserToServer(username, password)).enqueue(object :
            Callback<UserFromServer> {
            override fun onResponse(call: Call<UserFromServer>, response: Response<UserFromServer>) {
                if (response.isSuccessful){
                    val user = response.body()!!
//                    application.getSharedPreferences(
//                        "UNAME",
//                        AppCompatActivity.MODE_PRIVATE
//                    )
//                        .edit()
//                        .putString("name", user.username)
//                        .apply()

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
                    application.getSharedPreferences(
                        "UNAME",
                        AppCompatActivity.MODE_PRIVATE
                    )
                        .edit()
                        .putString("name", username)
                        .putString("access_token", user.access_token)
                        .putString("token_type", user.token_type)
                        .apply()

                    updateData(UserData(username, user.access_token))
                    //add to sharedpref token?
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
        AppDataBase.databaseWriteExecutor.execute {
            dataBaseSource.userDao()?.editProfile(UserEntity(0, data.name, data.userId, data.likes, data.subUsers, data.latitude, data.longitude))
        }
    }

    fun updateData(data : UserFromServer) {
        AppDataBase.databaseWriteExecutor.execute {
            dataBaseSource.userDao()?.editProfile(UserEntity(0, data.username, data.id))
        }
    }

    fun deleteData() {
        application.getSharedPreferences("UNAME", AppCompatActivity.MODE_PRIVATE)
            .edit()
            .putString("access_token", null)
            .putString("token_type", null)
            .putString("name", "-1")
            .apply()
        updateData(UserData("none", "-1"))
    }


}