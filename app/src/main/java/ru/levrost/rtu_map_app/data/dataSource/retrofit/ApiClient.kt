package ru.levrost.rtu_map_app.data.dataSource.retrofit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ApiClient {
    const val SHORT_URL = "https://maps.rtuitlab.dev"
    private const val BASE_URL = "https://maps.rtuitlab.dev/api/"
    private var retrofit: Retrofit? = null

    @Synchronized
    fun getClient(): Retrofit {
        if (retrofit == null) {
//            val loggingInterceptor = HttpLoggingInterceptor()
//            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
//
//            val client = OkHttpClient.Builder() //for debugging
//                .addInterceptor(loggingInterceptor)
//                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
//                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun close(){
        retrofit = null
    }

}