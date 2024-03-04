package ru.levrost.rtu_map_app.ui.view.Fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.databinding.MapFragmentBinding

class MapFragment: Fragment() {

    private var _binding: MapFragmentBinding? = null
    private val mBinding get() = _binding!!

    private lateinit var mapView: MapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkAvailableUserLocationAccess()){
            requestAvailableUserLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MapFragmentBinding.inflate(inflater, container, false)
        mapView = mBinding.mapView

        val userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = false
        userLocationLayer.setObjectListener(locationObjectListener)

//        mapView.map.isNightModeEnabled = true

        updateUserLocation()

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hideAllStyle =
            "[" +  // Задание стиля через псевдо JSON для карты. По аналогии с примером из API
                    "        {" +
                    "            \"types\": \"point\"," +
                    "            \"tags\": {" +
                    "                \"all\": [" +
                    "                    \"poi\"" +
                    "                ]," +
                    "                \"none\": [" +
                    "                    \"outdoor\"," +
                    "                    \"major_landmark\"" +
                    "                ]" +
                    "            }," +
                    "            \"stylers\": {" +
                    "                \"visibility\": \"off\"" +
                    "            }" +
                    "        }" +
                    "    ]"

        mapView.map.setMapStyle(hideAllStyle)

        mapView.map
            .move(CameraPosition(Point(55.7515, 37.64), 4f, 0.0f, 0.0f))
    }

    private fun updateUserLocation(): Boolean{
        // Возвращает true - если обновлена, false - если нет возможности и установила по умолчанию
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        return true
    }

    //Проверка выдачи пользователя разрешения на использование геолокации
    private fun checkAvailableUserLocationAccess(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    //Запрос на выдачу разрешения на использование геолокации
    private fun requestAvailableUserLocation() {
        ActivityCompat
            .requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat
                .requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
        }
    }

//устанавливаем дизайн для иконки местоположения пользователя
    private val locationObjectListener: UserLocationObjectListener =
        object : UserLocationObjectListener {
            override fun onObjectAdded(userLocationView: UserLocationView) {
                userLocationView.accuracyCircle.fillColor = Color.argb(160,139,79,37)

                userLocationView.arrow.setIcon(
                    ImageProvider.fromResource(context, R.drawable.map_arrow_circle_red),
                    IconStyle().setAnchor(PointF(0.5f, 0.7f))
                        .setRotationType(RotationType.NO_ROTATION)
                        .setScale(0.065f))

                userLocationView.pin.setIcon(
                    ImageProvider.fromResource(context, R.drawable.map_arrow_circle_red),
                    IconStyle().setAnchor(PointF(0.5f, 0.7f))
                        .setRotationType(RotationType.NO_ROTATION)
                        .setScale(0.065f))

            }
            override fun onObjectRemoved(p0: UserLocationView) {}
            override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {}
        }


    override fun onStart() {
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
        super.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        requireActivity().window.statusBarColor = Color.TRANSPARENT
        _binding = null
    }

    companion object {
        val REQUEST_LOCATION_PERMISSION: Int = 1
    }

}