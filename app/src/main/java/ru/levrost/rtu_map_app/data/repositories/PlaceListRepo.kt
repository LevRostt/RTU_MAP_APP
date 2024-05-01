package ru.levrost.rtu_map_app.data.repositories

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.levrost.rtu_map_app.data.dataSource.retrofit.ApiClient
import ru.levrost.rtu_map_app.data.dataSource.retrofit.ServerApi
import ru.levrost.rtu_map_app.data.dataSource.retrofit.model.PlaceFromServer
import ru.levrost.rtu_map_app.data.dataSource.retrofit.model.PlaceToServer
import ru.levrost.rtu_map_app.data.dataSource.room.entites.PlaceEntity
import ru.levrost.rtu_map_app.data.dataSource.room.root.AppDataBase
import ru.levrost.rtu_map_app.data.model.Place
import ru.levrost.rtu_map_app.data.model.UserData
import ru.levrost.rtu_map_app.global.debugLog
import java.io.File
import java.lang.ref.WeakReference

import java.util.stream.Collectors


class PlaceListRepo private constructor(private var context: WeakReference<Context>?) {

    private var dataBaseSource : AppDataBase? = AppDataBase.getDataBase(context!!.get()!!)
    private var serverApi : ServerApi? = ApiClient.getClient().create(ServerApi::class.java)
    private var _cachedData : List<Place>? = null
    val cacheData get() = _cachedData

    private val sharedPref = context!!.get()!!.getSharedPreferences(
        "UNAME",
        AppCompatActivity.MODE_PRIVATE
    )

    companion object{
        private var _instance : PlaceListRepo? = null

        @Synchronized
        fun getInstance(context: WeakReference<Context>): PlaceListRepo {
            if (_instance == null){
                _instance = PlaceListRepo(context)
            }
            return _instance!!
        }

        @Synchronized
        fun getInstance(): PlaceListRepo?{
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

    init {
        getFromServer()
    }


    fun getUrl(link : String) : String{
        //debugLog(ApiClient.SHORT_URL + link)
        return ApiClient.SHORT_URL + link
    }
    fun getFromServer(){
        val accessToken = sharedPref.getString("token_type", "") + " " + sharedPref.getString("access_token", "")

        serverApi?.getPlaces(accessToken)?.enqueue(object : Callback<List<PlaceFromServer>>{
            override fun onResponse(call: Call<List<PlaceFromServer>>, response: Response<List<PlaceFromServer>>) {
                if (response.isSuccessful){

                    val list : ArrayList<Place> = ArrayList()
                    for (place in response.body()!!){
                        list.add(
                            Place(
                                place.description,
                                place.id,
                                place.user?.username ?: "",
                                place.user?.id ?: "",
                                place.latitude,
                                place.longitude,
                                place.description,
                                place.likes,
                                place.is_liked,
                                place.image
                            )
                        )
                    }
                    pushPlaces(list)

                    Log.d("LRDebugServer", "Get ${response.body()} ; ${response.errorBody()?.string()} ")
                } else{
                    Log.d("LRDebugServer", "Get response ${response.code()} ; ${response.errorBody()?.string()} ")
                }
            }

            override fun onFailure(call: Call<List<PlaceFromServer>>, t: Throwable) {
                Log.d("LRDebugServer", "Failed $t")
            }

        })
    }

    private fun postToServer(place : Place){
        val placeToServer = PlaceToServer(place.latitude, place.longitude, place.name, place.image)

        val latitude = placeToServer.latitude.toString().toRequestBody("text/plain".toMediaType())
        val longitude = placeToServer.longitude.toString().toRequestBody("text/plain".toMediaType())
        val description = placeToServer.description.toRequestBody("text/plain".toMediaType())

        var image : MultipartBody.Part? = null
        if(place.isPicSaved()) {
            val inputStream =
                context!!.get()!!.contentResolver.openInputStream(Uri.parse(placeToServer.image))

            val imageFile = File(context!!.get()!!.cacheDir, "imageFile").apply { createNewFile() }

            inputStream.use { stream ->
                imageFile.outputStream().use { fileOut ->
                    stream?.copyTo(fileOut)
                }
            }

            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            image = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
        }

        val accessToken = sharedPref.getString("token_type", "") + " " + sharedPref.getString("access_token", "")

        serverApi?.postPlace(accessToken, latitude, longitude, description, image)?.enqueue(object : Callback<PlaceFromServer>{
            override fun onResponse(call: Call<PlaceFromServer>, response: Response<PlaceFromServer>) {
                if (response.isSuccessful){
                    val responsePlace = response.body()!!
                    val placeToAdd = Place(
                        responsePlace.description,
                        responsePlace.id,
                        responsePlace.user?.username ?: "",
                        responsePlace.user?.id ?: "",
                        responsePlace.latitude,
                        responsePlace.longitude,
                        responsePlace.description,
                        responsePlace.likes,
                        responsePlace.is_liked,
                        responsePlace.image
                    )
                    addPlaceToDataBase(placeToAdd)
                    Log.d("LRDebugServer", "Post response $placeToAdd ")
                } else{
                    Log.d("LRDebugServer", "Post response ${response.code()} ; ${response.errorBody()?.string()} ")
                }
            }

            override fun onFailure(call: Call<PlaceFromServer>, t: Throwable) {
                Log.d("LRDebugServer", "Failed: $t ;")
            }

        })
    }

    private fun likeToServer(id : String){
        val accessToken = sharedPref.getString("token_type", "") + " " + sharedPref.getString("access_token", "")

        serverApi?.like(accessToken, id)?.enqueue(object : Callback<PlaceFromServer>{
            override fun onResponse(call: Call<PlaceFromServer>, response: Response<PlaceFromServer>) {
                if (response.isSuccessful){
                    Log.d("LRDebugServer", "Like response ${response.body()!!} ")
                } else{
                    Log.d("LRDebugServer", "Like response ${response.code()} ; ${response.errorBody()?.string()} ")
                }
            }

            override fun onFailure(call: Call<PlaceFromServer>, t: Throwable) {
                Log.d("LRDebugServer", "Failed: $t ;")
            }

        })
    }

    private fun unLikeToServer(id : String){
        val accessToken = sharedPref.getString("token_type", "") + " " + sharedPref.getString("access_token", "")

        serverApi?.unlike(accessToken, id)?.enqueue(object : Callback<PlaceFromServer>{
            override fun onResponse(call: Call<PlaceFromServer>, response: Response<PlaceFromServer>) {
                if (response.isSuccessful){
                    Log.d("LRDebugServer", "Unlike response ${response.code()} ")
                } else{
                    Log.d("LRDebugServer", "Unlike response ${response.code()} ; ${response.errorBody()?.string()} ")
                }
            }

            override fun onFailure(call: Call<PlaceFromServer>, t: Throwable) {
                Log.d("LRDebugServer", "Failed: $t ;")
            }
        })
    }

    private fun deleteToServer(id : String){
        val accessToken = sharedPref.getString("token_type", "") + " " + sharedPref.getString("access_token", "")

        serverApi?.delete(accessToken, id)?.enqueue(object : Callback<PlaceFromServer>{
            override fun onResponse(call: Call<PlaceFromServer>, response: Response<PlaceFromServer>) {
                if (response.isSuccessful){
                    Log.d("LRDebugServer", "Delete response ${response.code()} ")
                } else{
                    Log.d("LRDebugServer", "Delete response ${response.code()} ; ${response.errorBody()?.string()} ")
                }
            }

            override fun onFailure(call: Call<PlaceFromServer>, t: Throwable) {
                Log.d("LRDebugServer", "Failed: $t ;")
            }
        })
    }

    fun getData(): LiveData<List<Place>> {
        return dataBaseSource?.placeListDao()?.getAllPlaces()?.map { placeList ->
            placeList.stream().map {
                Place(
                    it.name,
                    it.idPlace,
                    it.userName,
                    it.userId,
                    it.latitude,
                    it.longitude,
                    it.description,
                    it.likes,
                    it.isLike,
                    it.icon
                )
            }.collect(Collectors.toList())
        } ?: MutableLiveData(ArrayList<Place>() as List<Place>)
    }

    fun getPlaceByText(text: String) : LiveData<List<Place>>{
        getFromServer()

        return dataBaseSource?.placeListDao()?.getPlacesByText(text)?.map { placeList ->
            placeList.stream().map {
                Place(
                    it.name,
                    it.idPlace,
                    it.userName,
                    it.userId,
                    it.latitude,
                    it.longitude,
                    it.description,
                    it.likes,
                    false,
                    it.icon
                )
            }.collect(Collectors.toList())

        } ?: MutableLiveData(ArrayList<Place>() as List<Place>)
    }


    fun likePlace(id : String){
        likeToServer(id)
    }

    fun unLikePlace(id : String){
        unLikeToServer(id)
    }

    fun deletePlace(id : String){
        deleteToServer(id)
    }

    fun addPlace(data : Place) {
        postToServer(data)
    }

    fun addPlaceToDataBase(data : Place){
        AppDataBase.databaseWriteExecutor.execute{
            dataBaseSource?.placeListDao()?.addPlace(PlaceEntity(data.name,data.idPlace,data.description, data.image, data.userName, data.userId, data.likes, data.latitude, data.longitude, data.isLiked))
        }
    }

    private fun pushPlaces(data : List<Place>) {
        if (data.isNotEmpty()) {
            _cachedData = data
            AppDataBase.databaseWriteExecutor.execute {
                dataBaseSource?.placeListDao()?.replacePlaces(
                    data.map {
                        PlaceEntity(
                            it.name,
                            it.idPlace,
                            it.description,
                            it.image,
                            it.userName,
                            it.userId,
                            it.likes,
                            it.latitude,
                            it.longitude,
                            it.isLiked
                        )
                    }
                )
            }
        }
    }

//    fun size() : LiveData<Int>? = dataBaseSource.placeListDao()?.count()

//    fun deleteData() {}

}