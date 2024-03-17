package ru.levrost.rtu_map_app.data.dataSource.retrofit.model

data class UserFromServer (
    val id : String,
    val username : String
){
    override fun toString(): String {
        return "{ id = $id ; username = $username }"
    }
}