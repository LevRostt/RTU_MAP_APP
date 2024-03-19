package ru.levrost.rtu_map_app.ui.view.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.levrost.rtu_map_app.data.model.Place
import ru.levrost.rtu_map_app.databinding.MapPlaceCardBinding

class MapCardFragment() : Fragment() {

    private var _bindind: MapPlaceCardBinding? = null
    private val mBinding get() = _bindind!!
    private lateinit var _place : Place

    constructor(place : Place) : this() {
        _place = place
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindind = MapPlaceCardBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindind = null
    }
}