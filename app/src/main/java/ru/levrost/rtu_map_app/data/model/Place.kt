package ru.levrost.rtu_map_app.data.model

import java.util.Base64

class Place(val name: String, val idPlace: String, val userName: String, val userId: String,  val latitude: Double, val longitude : Double) {

    private var _description : String = ""
    private var _likes : Int = 0
    private var _is_liked : Boolean = false
    private var _image : String = ""

    var description
        set(value) {}
        get() = _description
    var likes
        set(value) {}
        get() = _likes
    var isLiked
        set(value) {}
        get() = _is_liked
    var image
        set(value) {}
        get() = _image

    override fun toString(): String {
        val toOut = "name = $name ; idPlace = $idPlace ; userName = $userName ; userId = $userId ; latitude = $latitude " +
                "; longitude = $longitude ; description = $description ; likes = $likes ; isLiked = $isLiked ; image = $image"
        return toOut
    }

    fun stringToOut(): String{
        return if (userName != "") "$name by $userName" else "$name by guest"
    }

    constructor(name: String, idPlace: String, userName: String, userId: String, latitude: Double, longitude : Double,
                description: String, likes: Int, isLiked: Boolean, image: String?)
            : this(name, idPlace, userName, userId, latitude, longitude){
        _description = description
        _likes = likes
        _is_liked = isLiked
        if (image != null) {
            _image = image
        }
        else{
            _image = ""
        }
    }

    fun liked(){ //Функционал лайка места
        _is_liked = true
        likes++
    }
    fun unLiked(){ //Функционал лайка места
        _is_liked = false
        likes--
    }

    fun isPicSaved():Boolean { // проверка записи/доступности картинки
        return _image != ""
    }

}