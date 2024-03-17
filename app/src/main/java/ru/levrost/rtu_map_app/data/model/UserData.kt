package ru.levrost.rtu_map_app.data.model

class UserData(val name: String, val userId: String) {
    var latitude = 0.0

    var longitude = 0.0

    private var _subUsers : ArrayList<String> = ArrayList()

    var subUsers: ArrayList<String>
        set(value) {}
        get() = _subUsers

    private var _likes : ArrayList<String> = ArrayList()

    var likes: ArrayList<String>
        set(value) {}
        get() = _likes

    constructor(_name: String, _userId: String, _latitude: Double, _longitude: Double, subUsers: List<String>, likes: List<String>)
            : this(_name, _userId){
        _likes = likes as ArrayList<String>
        _subUsers = subUsers as ArrayList<String>
        latitude = _latitude
        longitude = _longitude
    }

    override fun toString(): String {
        return "name = $name ; userId = $userId ; latitude = $latitude ; longitude = $longitude"
    }
    constructor(userData: UserData): this(userData.name, userData.userId, userData.latitude, userData.longitude, userData.subUsers, userData.likes)

    fun subscribe(id : String){
        if (id !in subUsers){
            _subUsers.add(id)
        }
    }

    fun unscribe(id : String){
        _subUsers.remove(id)
    }

    fun like(idOfPlace : String){
        if (idOfPlace !in likes) {
            _likes.add(idOfPlace)
        }
    }

    fun unLike(idOfPlace: String){
        _likes.remove(idOfPlace)
    }
}