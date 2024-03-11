package ru.levrost.rtu_map_app.data.repositories

import androidx.lifecycle.LiveData

interface repository<T> {
    fun getData() : LiveData<T>?
    fun updateData(data : T) : Unit

}