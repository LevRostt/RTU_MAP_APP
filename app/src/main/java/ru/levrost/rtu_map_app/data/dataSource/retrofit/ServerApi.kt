package ru.levrost.rtu_map_app.data.dataSource.retrofit

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import ru.levrost.rtu_map_app.data.dataSource.retrofit.model.PlaceFromServer
import ru.levrost.rtu_map_app.data.dataSource.retrofit.model.PlaceToServer
import ru.levrost.rtu_map_app.data.dataSource.retrofit.model.UserFromServer
import ru.levrost.rtu_map_app.data.dataSource.retrofit.model.UserToServer
import ru.levrost.rtu_map_app.data.dataSource.retrofit.model.UserTokenFromServer

interface ServerApi {

    @GET("tags/")
    fun getPlaces(
        @Header("Authorization") accessToken : String
    ) : Call<List<PlaceFromServer>>

    @Multipart
    @POST("tags/")
    fun postPlace(
        @Header("Authorization") accessToken : String,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part?
    ) : Call<PlaceFromServer>


    @POST("/api/auth/register")
    fun register(
        @Body user: UserToServer
    ) : Call<UserFromServer>

    @FormUrlEncoded
    @POST("auth/jwt/login")
    fun login(
        @Field("username") name : String,
        @Field("password") password : String
    ) : Call<UserTokenFromServer>
}