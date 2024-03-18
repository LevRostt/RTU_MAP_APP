package ru.levrost.rtu_map_app.data.repositories

import android.app.Application
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
import ru.levrost.rtu_map_app.global.debugLog
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

import java.util.stream.Collectors


class PlaceListRepo(private val application: Application) : repository<List<Place>> {

    private var dataBaseSource : AppDataBase = AppDataBase.getDataBase(application)
    private val serverApi = ApiClient.getClient().create(ServerApi::class.java)

    private val sharedPref = application.getSharedPreferences(
        "UNAME",
        AppCompatActivity.MODE_PRIVATE
    )

    init {
        getFromServer()
    }

    private fun getFromServer(){

        val accessToken = sharedPref.getString("token_type", "") + " " + sharedPref.getString("access_token", "") //!

        serverApi.getPlaces(accessToken).enqueue(object : Callback<List<PlaceFromServer>>{
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

                    Log.d("LRDebugServer", "${response.body()} ; ${response.errorBody()?.string()} ")
                } else{
                    Log.d("LRDebugServer", "response ${response.code()} ; ${response.errorBody()?.string()} ")
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
                application.contentResolver.openInputStream(Uri.parse(placeToServer.image))

            val imageFile = File(application.cacheDir, "imageFile").apply { createNewFile() }

            inputStream.use { stream ->
                imageFile.outputStream().use { fileOut ->
                    stream?.copyTo(fileOut)
                }
            }

            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            image = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
        }

        val accessToken = sharedPref.getString("token_type", "") + " " + sharedPref.getString("access_token", "")

        serverApi.postPlace(accessToken, latitude, longitude, description, image).enqueue(object : Callback<PlaceFromServer>{
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
                    Log.d("LRDebugServer", "response $placeToAdd ")
                } else{
                    Log.d("LRDebugServer", "response ${response.code()} ; ${response.errorBody()?.string()} ")
                }
            }

            override fun onFailure(call: Call<PlaceFromServer>, t: Throwable) {
                Log.d("LRDebugServer", "Failed: $t ;")
            }

        })
    }

    override fun getData(): LiveData<List<Place>> {
        return dataBaseSource.placeListDao()?.getAllPlaces()?.map { placeList ->
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

        return dataBaseSource.placeListDao()?.getPlacesByText(text)?.map { placeList ->
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

    fun addPlace(data : Place) {
        //post to server
        postToServer(data)
    }

    fun addPlaceToDataBase(data : Place){
        AppDataBase.databaseWriteExecutor.execute{
            dataBaseSource.placeListDao()?.addPlace(PlaceEntity(data.name,data.idPlace,data.description, data.image, data.userName, data.userId, data.likes, data.latitude, data.longitude, data.isLiked))
        }
    }

    fun pushPlaces(data : List<Place>) {
        if (data.isNotEmpty()) {

            AppDataBase.databaseWriteExecutor.execute {

                dataBaseSource.placeListDao()?.replacePlaces(
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