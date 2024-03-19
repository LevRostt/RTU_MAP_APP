package ru.levrost.rtu_map_app.ui.adapters

import androidx.recyclerview.widget.DiffUtil

class PlaceDiffUtil<T>(val oldArray: List<T>, val newArray: List<T>,
                    private val areItemsTheSameImpl: (old: T, new: T)->Boolean ={oldItem, newItem -> oldItem==newItem},
                    private val areContentTheSameImpl: (old: T, new: T) -> Boolean = {oldItem, newItem -> oldItem==newItem}) : DiffUtil.Callback() {
    override fun getOldListSize() = oldArray.size

    override fun getNewListSize() = newArray.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldArray[oldItemPosition]
        val new = newArray[newItemPosition]
        return areItemsTheSameImpl(old, new)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldArray[oldItemPosition]
        val new = newArray[newItemPosition]
        return areContentTheSameImpl(old, new)
    }
}