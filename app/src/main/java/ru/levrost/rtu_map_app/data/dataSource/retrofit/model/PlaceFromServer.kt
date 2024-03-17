package ru.levrost.rtu_map_app.data.dataSource.retrofit.model

data class PlaceFromServer (
    val id : String,
    val latitude : Double,
    val longitude : Double,
    val description : String,
    val image : String?,
    val likes : Int,
    val is_liked: Boolean,
    val user: UserFromServer?
){
    override fun toString(): String {
        return "{ id = $id ; latitude = $latitude ; longitude = $longitude ; description = $description ; image = $image ; likes = $likes ; is_liked = $is_liked ; user = $user"
    }

}


//"id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
//    "latitude": 0,
//    "longitude": 0,
//    "description": "string",
//    "image": "string",
//    "likes": 0,
//    "is_liked": false,
//    "user": {
//      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
//      "username": "string"
//    }