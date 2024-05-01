package ru.levrost.rtu_map_app.ui.view.fragment.controllers

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.data.model.Place
import ru.levrost.rtu_map_app.databinding.MapFragmentBinding
import ru.levrost.rtu_map_app.ui.view.fragment.MapCardFragment
import ru.levrost.rtu_map_app.ui.view.fragment.MapFragment

class MapFragmentController(val fragment : MapFragment, val binding: MapFragmentBinding) {

    private var cardFragment : MapCardFragment? = null

    private var userPoint: Point = Point()
    private var userLocationLayer: UserLocationLayer? = null
    private var zoom: Float = 9.5F
    private var mapObjects: MapObjectCollection? = null
    private var fragmentMarkObject : PlacemarkMapObject? = null

    private lateinit var mapView: MapView


    fun mapInit(){

        MapKitFactory.initialize(fragment.context)

        if (!checkAvailableUserLocationAccess()){
            requestAvailableUserLocation()
        }

        mapView = binding.mapView
        userLocationLayer =
            MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow)
        mapObjects = mapView.map.mapObjects.addCollection()

        //        mapView.map.isNightModeEnabled = true
    }

    fun mapStart(){
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    fun mapStop(){
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
    }

    fun mapUiInit(){
        fragment.apply {
            hideCardFragment()

            userLocationLayer!!.isVisible = true
            userLocationLayer!!.setObjectListener(locationObjectListener)

            if (!checkAvailableUserLocationAccess()) {
                requestAvailableUserLocation()
                mapView.map
                    .move(CameraPosition(Point(55.7515, 37.64), 4f, 0.0f, 0.0f))
            } else {
                jumpToUser(1.5F)
            }

            setupMapStyle()
            updateLocation()
            jumpToUser(2F)
            jumpToPlace()
        }

    }

    fun showUser(){
        updateLocation()
        jumpToUser(2F)
    }

    fun closeCard(){
        hideCardFragment()
        binding.cardCloseBtn.visibility = View.GONE
    }


    //Проверка выдачи пользователя разрешения на использование геолокации
    fun checkAvailableUserLocationAccess(): Boolean {
        fragment.apply {
            return (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    &&
                    ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED)
        }
    }

    //Запрос на выдачу разрешения на использование геолокации
    fun requestAvailableUserLocation() {
        fragment.apply {
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
    }

    private fun jumpToPlace(){
        fragment.apply {
            if (placeListViewModel.selectedPlace() != null && placeListViewModel.selectedPlace()!!.latitude != 0.0) {
                mapView.map.move(
                    CameraPosition(
                        Point(
                            placeListViewModel.selectedPlace()!!.latitude,
                            placeListViewModel.selectedPlace()!!.longitude
                        ), 13.5F, 0.0F, 45F
                    ),
                    Animation(Animation.Type.SMOOTH, 2.0F),
                    null
                )
                placeListViewModel.selectPlace(0.0, 0.0)
            }
        }
    }

    private fun jumpToUser(duration: Float){
        fragment.apply {
            if (!checkAvailableUserLocationAccess()) {
                requestAvailableUserLocation()
            } else {
                val locationManager =
                    activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    // Если геолокация выключена, выводим диалоговое окно с запросом на включение
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage(
                        ContextCompat.getString(
                            requireContext(),
                            R.string.track_location
                        )
                    )
                        .setCancelable(false)
                        .setPositiveButton(
                            ContextCompat.getString(
                                requireContext(),
                                R.string.enable
                            )
                        ) { dialog, id -> // Открываем настройки геолокации
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivity(intent)
                        }
                        .setNegativeButton(
                            ContextCompat.getString(requireContext(), R.string.cancel)
                        ) { dialog, id -> // Закрываем диалоговое окно
                            dialog.cancel()
                        }
                    val alert = builder.create()
                    alert.show()
                }
                userLocationLayer!!.isVisible = true
                mapView.map.move(
                    CameraPosition(userPoint, zoom, 0.0f, 45f),
                    Animation(Animation.Type.SMOOTH, duration),
                    null
                )
            }
        }
    }


    private fun updateLocation(){
        fragment.apply {
            userPoint = userViewModel.userPoint(viewLifecycleOwner)
            zoom = if (userPoint.latitude != 55.7515) {
                16F
            } else {
                4.5F
            }
        }
    }

    private fun createMapPlaces() { //Добавляет приблюжённые к пользователю места на карту
        fragment.apply {
            val objectTapListener =
                MapObjectTapListener { mapObject: MapObject, _: Point? ->  // Создаём заранее, чтобы mapkit не терял указатель на listener(написано в доках)
                    showCardFragment(mapObject.userData as Place)

                    fragmentMarkObject = mapObject as PlacemarkMapObject

                    binding.cardCloseBtn.visibility = View.VISIBLE

                    mapObject.setIcon(
                        ImageProvider.fromResource(
                            context,
                            R.drawable.pin_pic_picked
                        ),
                        IconStyle().setAnchor(PointF(0.5f, 0.7f))
                            .setScale(0.06f)
                    )


                    true
                }

            placeListViewModel.placeList.observe(viewLifecycleOwner) { places ->
                for (place in places) {

                    val mapObj: PlacemarkMapObject? = mapObjects?.addPlacemark(
                        Point(
                            place.latitude,
                            place.longitude
                        )
                    )
                    mapObj?.setIcon(
                        ImageProvider.fromResource(
                            context,
                            R.drawable.filled_pin_pic
                        ),
                        IconStyle().setAnchor(PointF(0.5f, 0.7f))
                            .setScale(0.035f)
                    )
                    mapObj?.isVisible = true
                    mapObj?.userData = place
                    mapObj?.addTapListener(objectTapListener)
                }
            }
        }
    }

    private fun showCardFragment(place : Place) {
        fragment.apply {
            hideCardFragment()

            cardFragment = MapCardFragment(place)
            binding.cardFragment.visibility = View.VISIBLE
            childFragmentManager.beginTransaction()
                .replace(R.id.card_fragment, cardFragment!!)
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down)
                .show(cardFragment!!)
                .commit()

//            mBinding.cardFragment.setOnClickListener {}
        }
    }

    private fun hideCardFragment() {
        fragment.apply {
            if (cardFragment != null) {
                childFragmentManager.beginTransaction()
                    .remove(cardFragment!!)
                    .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down)
                    .show(cardFragment!!)
                    .commit()

            }
            try {
                fragmentMarkObject?.setIcon(
                    ImageProvider.fromResource(
                        context,
                        R.drawable.filled_pin_pic
                    ),
                    IconStyle().setAnchor(PointF(0.5f, 0.7f))
                        .setScale(0.0f)
                )
            } catch (_: RuntimeException) {
            }
            binding.cardFragment.visibility = View.GONE

        }
    }



    //устанавливаем дизайн для иконки местоположения пользователя
    private val locationObjectListener: UserLocationObjectListener =
        object : UserLocationObjectListener {
            override fun onObjectAdded(userLocationView: UserLocationView) {
                userLocationView.accuracyCircle.fillColor = Color.argb(160,139,79,37)

                userLocationView.arrow.setIcon(
                    ImageProvider.fromResource(fragment.context, R.drawable.map_arrow_circle_red),
                    IconStyle().setAnchor(PointF(0.5f, 0.7f))
                        .setRotationType(RotationType.NO_ROTATION)
                        .setScale(0.065f))

                userLocationView.pin.setIcon(
                    ImageProvider.fromResource(fragment.context, R.drawable.map_arrow_circle_red),
                    IconStyle().setAnchor(PointF(0.5f, 0.7f))
                        .setRotationType(RotationType.NO_ROTATION)
                        .setScale(0.065f))

            }
            override fun onObjectRemoved(p0: UserLocationView) {}
            override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {}
        }

    private fun setupMapStyle(){
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
    }

    companion object {
        val REQUEST_LOCATION_PERMISSION: Int = 1
    }
}