package ru.levrost.rtu_map_app.data.model

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

    constructor(name: String, idPlace: String, userName: String, userId: String, latitude: Double, longitude : Double,
                description: String, likes: Int, isLiked: Boolean, image: String)
            : this(name, idPlace, userName, userId, latitude, longitude){
        _description = description
        _likes = likes
        _is_liked = isLiked
        _image = image
    }

    fun liked(){ //Функционал лайка места
        _is_liked = true
        likes++
    }

    fun isPicSaved():Boolean { // проверка записи картинки
        return _image != ""
    }

}