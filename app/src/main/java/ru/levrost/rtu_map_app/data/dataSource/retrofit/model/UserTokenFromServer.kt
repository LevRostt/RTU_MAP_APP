package ru.levrost.rtu_map_app.data.dataSource.retrofit.model

class UserTokenFromServer(
    val access_token : String,
    val token_type : String
){
    override fun toString(): String {
        return "access_token = $access_token ; token_type = $token_type"
    }

}