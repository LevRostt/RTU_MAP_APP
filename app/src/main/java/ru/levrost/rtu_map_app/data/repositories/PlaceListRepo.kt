package ru.levrost.rtu_map_app.data.repositories

import android.R.attr.description
import android.app.Application
import android.net.Uri
import android.util.Log
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
import java.io.File
import java.util.stream.Collectors


class PlaceListRepo(private val application: Application) : repository<List<Place>> {

    private var dataBaseSource : AppDataBase = AppDataBase.getDataBase(application)
    private val serverApi = ApiClient.getClient().create(ServerApi::class.java)

    init {

    }
    private fun getFromServer(){

        AppDataBase.databaseWriteExecutor.execute {
            dataBaseSource.placeListDao()?.clearData() //отключить, если нет интернета
        }

        serverApi.getPlaces().enqueue(object : Callback<List<PlaceFromServer>>{
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
                } else{
                    Log.d("LRDebugServer", "response " + response.code().toString())
                }
            }

            override fun onFailure(call: Call<List<PlaceFromServer>>, t: Throwable) {
                Log.d("LRDebugServer", "Failed $t")
            }

        })
    }

    private fun postToServer(place : Place){
        val placeToServer = PlaceToServer(place.latitude, place.longitude, place.name, place.image)
        Log.d("LRDebugServer", " $placeToServer ")

        val latitude = placeToServer.latitude.toString().toRequestBody("text/plain".toMediaType())
        val longitude = placeToServer.longitude.toString().toRequestBody("text/plain".toMediaType())
        val description = placeToServer.description.toRequestBody("text/plain".toMediaType())

        var image : MultipartBody.Part? = null
        if(place.isPicSaved()) {
            val inputStream =
                application.contentResolver.openInputStream(Uri.parse(placeToServer.image))

            val imageFile = File(application.cacheDir, "imageFile").apply { createNewFile() }

            inputStream.use { inputStream ->
                imageFile.outputStream().use { fileOut ->
                    inputStream?.copyTo(fileOut)
                }
            }
            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            image = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
        }

        serverApi.postPlace(latitude, longitude, description, image).enqueue(object : Callback<PlaceFromServer>{
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
        getFromServer()

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
                    false,
                    it.icon
                )
            }.collect(Collectors.toList())
        } ?: MutableLiveData(ArrayList<Place>() as List<Place>)
    }

    fun getPlaceByText(text: String) : LiveData<List<Place>>{
//        getFromServer()

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
        AppDataBase.databaseWriteExecutor.execute {
            dataBaseSource.placeListDao()?.pushPlace(
                data.map { PlaceEntity(it.name, it.idPlace, it.description, it.image, it.userName, it.userId, it.likes, it.latitude, it.longitude, it.isLiked) }
            )
        }
    }

    fun size() : LiveData<Int>? = dataBaseSource.placeListDao()?.count()

    fun deleteData() {

    }

}