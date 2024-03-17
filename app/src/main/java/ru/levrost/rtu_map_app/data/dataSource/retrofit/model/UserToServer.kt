package ru.levrost.rtu_map_app.data.dataSource.retrofit.model

data class UserToServer (
    val username : String,
    val password : String
){
    override fun toString(): String {
        return "{ username = $username ; pass = $password }"
    }
}