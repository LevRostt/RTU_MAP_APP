package ru.levrost.rtu_map_app.data.dataSource.retrofit

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import ru.levrost.rtu_map_app.data.dataSource.retrofit.model.PlaceFromServer
import ru.levrost.rtu_map_app.data.dataSource.retrofit.model.PlaceToServer
import ru.levrost.rtu_map_app.data.dataSource.retrofit.model.UserFromServer
import ru.levrost.rtu_map_app.data.dataSource.retrofit.model.UserToServer

interface ServerApi {

    @GET("tags/")
    fun getPlaces() : Call<List<PlaceFromServer>>

    @Multipart
    @POST("tags/")
    fun postPlace(
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part?
    ) : Call<PlaceFromServer>


    @POST("/api/auth/register")
    fun register(
        @Body user: UserToServer
    ) : Call<UserFromServer>

}