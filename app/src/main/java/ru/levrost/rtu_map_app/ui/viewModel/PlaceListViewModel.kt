package ru.levrost.rtu_map_app.ui.viewModel

import android.app.Application
import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.yandex.mapkit.geometry.Point
import ru.levrost.rtu_map_app.data.model.Place
import ru.levrost.rtu_map_app.data.repositories.PlaceListRepo

class PlaceListViewModel (private val application: Application) : AndroidViewModel(application)  {

    private val repo = PlaceListRepo(application)
    private val _placeList = repo.getData()
    private val _point : MutableLiveData<Point> = MutableLiveData()
    private val _lastBitMap : MutableLiveData<Bitmap> = MutableLiveData()

    val placeList get() = _placeList

    private fun addPlace(place: Place){
        repo.addPlace(place)
    }

    fun addPlace(name: String, idPlace: String, userName: String, userId: String, latitude: Double, longitude : Double,
                 description: String, likes: Int, isLiked: Boolean, image: String){
        repo.addPlace(Place(name, idPlace, userName, userId, latitude, longitude, description, likes, isLiked, image))
    }

    fun likePlace(id : Int){
        val place = _placeList.value!!.get(id)
        place.likes++
        // отключить при отстутвии интернета
    }

    fun deletePlace(id : String){

    }

    fun setLastBitMap(bitmap: Bitmap) = _lastBitMap.postValue(bitmap)

    fun getLastBitMap() = _lastBitMap.value

    fun selectPlace(latitude: Double, longitude: Double){ //Переписать реализацию?
        _point.postValue(Point(latitude, longitude))
    }

    fun selectedPlace() = _point.value //Аналогично переписать?

    companion object{
        val Factory: ViewModelProvider.Factory = object :  ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return PlaceListViewModel(application) as T
            }
        }
    }
}