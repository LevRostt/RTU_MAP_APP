package ru.levrost.rtu_map_app.ui.viewModel

import android.app.Application
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
    val placeList get() = _placeList

    fun addPlace(place: Place){
        repo.addPlace(place)
    }

    fun addPlace(name: String, idPlace: String, userName: String, userId: String, latitude: Double, longitude : Double,
                 description: String, likes: Int, isLiked: Boolean, image: String){
        repo.addPlace(Place(name, idPlace, userName, userId, latitude, longitude, description, likes, isLiked, image))
    }

    fun selectPlace(latitude: Double, longitude: Double){
        _point.postValue(Point(latitude, longitude))
    }

    fun selectedPlace() = _point.value

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