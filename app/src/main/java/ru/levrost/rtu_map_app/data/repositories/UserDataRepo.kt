package ru.levrost.rtu_map_app.data.repositories

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
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
import ru.levrost.rtu_map_app.global.ResultStatus
import ru.levrost.rtu_map_app.global.debugLog
import ru.levrost.rtu_map_app.global.observeOnce
import java.lang.ref.WeakReference

class UserDataRepo private constructor(private var context: WeakReference<Context>?) {

    private var dataBaseSource : AppDataBase? = AppDataBase.getDataBase(context?.get()!!)
    private var serverApi : ServerApi? = ApiClient.getClient().create(ServerApi::class.java)
    private var _cachedData : UserData? = null
    val cacheData get() = _cachedData

    companion object{

        private var _instance : UserDataRepo? = null
        @Synchronized
        fun getInstance(context: WeakReference<Context>): UserDataRepo {
            if (_instance == null){
                _instance = UserDataRepo(context)
            }
            return _instance!!
        }

        @Synchronized
        fun getInstance(): UserDataRepo?{
            return _instance
        }

        @Synchronized
        fun detach(){
            _instance?.dataBaseSource?.close()
            _instance?.dataBaseSource = null
            ApiClient.close()
            _instance?.context = null
            _instance?.serverApi = null
            _instance?._cachedData = null
            _instance = null
        }
    }

    fun getData(): LiveData<UserData> {
        return dataBaseSource?.userDao()?.getData()?.map { userEntity ->
            val data = UserData(userEntity.name, userEntity.userId, userEntity.latitude, userEntity.longitude,userEntity.subscribes, userEntity.likes)
            _cachedData = data //Кэшируем данные для последующего к ним обращения без запроса к серверу
            return@map data
        } ?: MutableLiveData(UserData("-1", "-1"))
    }

    fun register(username : String, password : String): LiveData<ResultStatus>{
        val result = MutableLiveData<ResultStatus>()
        serverApi?.register(UserToServer(username, password))?.enqueue(object : Callback<UserFromServer> {
            override fun onResponse(call: Call<UserFromServer>, response: Response<UserFromServer>) {
                if (response.isSuccessful){
                    val op = login(username, password)
                    val observer = object : Observer<ResultStatus> {
                        override fun onChanged(value: ResultStatus) {
                            result.postValue(value)
                            op.removeObserver(this)
                        }
                    }
                    op.observeForever(observer)
                } else{
                    Log.d("LRDebugServer", "response ${response.code()} ; ${response.errorBody()?.string()} ")
                    result.postValue(ResultStatus.UserFail)
                }
            }

            override fun onFailure(call: Call<UserFromServer>, t: Throwable) {
                Log.d("LRDebugServer", "Failed $t")
                result.postValue(ResultStatus.ServerFail)
            }
        })
        return result
    }

    fun login(username: String, password: String): LiveData<ResultStatus>{
        val result = MutableLiveData<ResultStatus>()
        serverApi?.login(username, password)?.enqueue(object : Callback<UserTokenFromServer>{
            override fun onResponse(
                call: Call<UserTokenFromServer>,
                response: Response<UserTokenFromServer>
            ) {
                if (response.isSuccessful){
                    val user = response.body()!!
                    updateData(UserData(username, user.access_token))
                    result.postValue(ResultStatus.Success)
                } else{
                    Log.d("LRDebugServer", "response ${response.code()} ; ${response.errorBody()?.string()} ")
                    result.postValue(ResultStatus.UserFail)
                }
            }

            override fun onFailure(call: Call<UserTokenFromServer>, t: Throwable) {
                Log.d("LRDebugServer", "Failed $t")
                result.postValue(ResultStatus.ServerFail)
            }

        })
        return result
    }

    fun updateData(data : UserData) {
        _cachedData = data

        context!!.get()!!.getSharedPreferences(
            "UNAME",
            Context.MODE_PRIVATE
        )
            .edit()
            .putString("name", data.name)
            .putString("access_token", data.userId)
            .putString("token_type", "bearer")
            .apply()

        AppDataBase.databaseWriteExecutor.execute {
            dataBaseSource?.userDao()?.editProfile(UserEntity(0, data.name, data.userId, data.likes, data.subUsers, data.latitude, data.longitude))
        }
    }

    fun deleteData() {
        context!!.get()!!.getSharedPreferences("UNAME", Context.MODE_PRIVATE)
            .edit()
            .putString("access_token", null)
            .putString("token_type", null)
            .putString("name", "-1")
            .apply()
        updateData(UserData("-1", "-1"))
    }


}