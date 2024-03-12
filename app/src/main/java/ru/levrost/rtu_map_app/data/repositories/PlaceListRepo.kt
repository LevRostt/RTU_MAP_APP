package ru.levrost.rtu_map_app.data.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import ru.levrost.rtu_map_app.data.dataSource.entites.PlaceEntity
import ru.levrost.rtu_map_app.data.dataSource.root.AppDataBase
import ru.levrost.rtu_map_app.data.model.Place
import java.util.stream.Collectors

class PlaceListRepo(private val application: Application) : repository<List<Place>> {

    private var dataBaseSource : AppDataBase = AppDataBase.getDataBase(application)
    override fun getData(): LiveData<List<Place>> {


            return dataBaseSource.placeListDao()?.getAllPlaces()?.map { placeList ->

                    placeList.stream().map {
                        Place(
                            it.name,
                            it.idPlace,
                            it.userName,
                            it.userId,
                            it.latitude,
                            it.longitude,
                            it.description,
                            it.likes,
                            false,
                            it.icon
                        )
                    }.collect(Collectors.toList())

        } ?: MutableLiveData(ArrayList<Place>() as List<Place>)
    }

    fun addPlace(data : Place) {
        AppDataBase.databaseWriteExecutor.execute{
            dataBaseSource.placeListDao()?.addPlace(PlaceEntity(data.name,data.idPlace,data.description, data.image, data.userName, data.userId, data.likes, data.latitude, data.longitude))
        }
    }

    fun size() : LiveData<Int>? = dataBaseSource.placeListDao()?.count()

    fun deleteData() {

    }

}