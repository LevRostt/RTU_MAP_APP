package ru.levrost.rtu_map_app.ui.view.fragment.controllers

import android.app.AlertDialog
import android.graphics.PointF
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.databinding.FragmentMapSelectedBinding
import ru.levrost.rtu_map_app.ui.view.fragment.MapSelectorFragment

class MapSelectorController(val fragment : MapSelectorFragment, val binding: FragmentMapSelectedBinding) {
    private lateinit var mapView: MapView
    private lateinit var mapKit : MapKit
    private lateinit var mapObjects: MapObjectCollection

    fun mapBinding(){
        mapView = binding.mapView
        mapKit = MapKitFactory.getInstance()
        mapObjects = mapView.map.mapObjects.addCollection()
    }

    fun mapSetup(){
        mapView.map.move(CameraPosition(Point(55.7515, 37.64), 5f, 0.0f, 0.0f))
        mapView.map.addInputListener(inputListener)
    }

    fun mapStart(){
        mapView.onStart()
        mapKit.onStart()
    }

    fun mapStop(){
        mapView.onStop()
        mapKit.onStop()
    }

    private val inputListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) {

            // icon не устанавливается
            val obj = mapObjects.addPlacemark(
                Point(point.latitude, point.longitude)
            )

            //
            obj.setIcon(
                ImageProvider.fromResource(fragment.context, R.drawable.filled_pin_pic,),
                IconStyle().setAnchor(PointF(0.5f, 0.7f))
                    .setScale(0.04f)
            )

            obj.isVisible = true

            val builder = AlertDialog.Builder(fragment.context)
            builder.setMessage(ContextCompat.getString(fragment.requireContext(), R.string.choose_this_place))
                .setCancelable(false)
                .setPositiveButton(ContextCompat.getString(fragment.requireContext(), R.string.yes)) { dialog, id -> //pop back stack
                    fragment.placeListViewModel.selectPlace(point.latitude, point.longitude)
                    NavHostFragment.findNavController(fragment.requireParentFragment()).popBackStack()
                    dialog.cancel()
                }
                .setNegativeButton(ContextCompat.getString(fragment.requireContext(), R.string.no)) { dialog, id -> // Закрываем диалоговое окно
                    mapObjects.remove(obj)
                    dialog.cancel()
                }
            val alert = builder.create()
            alert.show()
        }

        override fun onMapLongTap(p0: Map, p1: Point) {}

    }


}