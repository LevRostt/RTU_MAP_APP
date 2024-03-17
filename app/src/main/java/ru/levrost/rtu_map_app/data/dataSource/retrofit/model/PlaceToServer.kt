package ru.levrost.rtu_map_app.data.dataSource.retrofit.model

data class PlaceToServer (
    val latitude : Double,
    val longitude : Double,
    val description : String, //На сервере нет имён мест, есть только description
    val image : String?
){
    override fun toString(): String {
        return "latitude = $latitude ; longitude = $longitude ; description = $description ; image = $image"
    }
}