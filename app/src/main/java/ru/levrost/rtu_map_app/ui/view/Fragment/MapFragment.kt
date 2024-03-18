package ru.levrost.rtu_map_app.ui.view.Fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.databinding.MapFragmentBinding
import ru.levrost.rtu_map_app.ui.viewModel.PlaceListViewModel
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel

class MapFragment: Fragment() {

    private var _binding: MapFragmentBinding? = null
    private val mBinding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels<UserViewModel> {
        UserViewModel.Factory
    }

    private val placeListViewModel: PlaceListViewModel by activityViewModels<PlaceListViewModel> {
        PlaceListViewModel.Factory
    }

    private var mapView: MapView? = null
    private var userPoint: Point = Point()
    private var userLocationLayer: UserLocationLayer? = null
    private var zoom: Float = 9.5F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.initialize(context)

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

        userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapView!!.mapWindow)

//        mapView.map.isNightModeEnabled = true

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        userLocationLayer!!.isVisible = true
        userLocationLayer!!.setObjectListener(locationObjectListener)

        mBinding.userLocationBtm.setOnClickListener {
            updateLocation()
            jumpToUser(2F)
        }

        setupMapStyle()

        if(!checkAvailableUserLocationAccess()){
            requestAvailableUserLocation()
            mapView!!.map
                .move(CameraPosition(Point(55.7515, 37.64), 4f, 0.0f, 0.0f))
        } else{
            jumpToUser(1.5F)
        }

        updateLocation()
        jumpToUser(2F)
        jumpToPlace()
    }
    private fun jumpToPlace(){
        if (placeListViewModel.selectedPlace() != null && placeListViewModel.selectedPlace()!!.latitude != 0.0){
            mapView!!.map.move(CameraPosition(
                Point(placeListViewModel.selectedPlace()!!.latitude, placeListViewModel.selectedPlace()!!.longitude), 13.5F, 0.0F, 45F),
                Animation(Animation.Type.SMOOTH, 2.0F),
                null)
            placeListViewModel.selectPlace(0.0,0.0)
        }
    }

    private fun jumpToUser(duration: Float){
        if (!checkAvailableUserLocationAccess()){
            requestAvailableUserLocation()
        } else {
            val locationManager =
                activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    // Если геолокация выключена, выводим диалоговое окно с запросом на включение
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Чтобы отслеживать своё местоположение в приложении, пожалуйста, включите геокацию.")
                    .setCancelable(false)
                    .setPositiveButton("Включить") { dialog, id -> // Открываем настройки геолокации
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                    }
                    .setNegativeButton(
                        "Отмена"
                    ) { dialog, id -> // Закрываем диалоговое окно
                        dialog.cancel()
                    }
                val alert = builder.create()
                alert.show()
            }
            userLocationLayer!!.isVisible = true
            mapView!!.map.move(
                CameraPosition(userPoint, zoom, 0.0f, 45f),
                Animation(Animation.Type.SMOOTH, duration),
                null
            )
        }
    }


    private fun updateLocation(){

        userPoint = userViewModel.userPoint(viewLifecycleOwner)
        if (userPoint.latitude != 55.7515){
            zoom = 16F
        }
        else{
            zoom = 4.5F
        }

    }


//    private fun createMapPlaces() { //Добавляет приблюжённые к пользователю места на карту
//        val objectTapListener =
//            MapObjectTapListener { mapObject: MapObject, point: Point? ->  // Создаём заранее, чтобы не программа не теряла указатель
//                // на это listener
//                val localObject = mapObject as PlacemarkMapObject
//                val userData = localObject.userData
//                if (userData is Place) {
//                    Toast.makeText(
//                        context,
//                        (localObject.userData as Place?).getName(),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                true
//            }
//        mUserViewModel.getData().observe(viewLifecycleOwner) { data ->
//            mPlaceViewModel.getPlaces().observe(viewLifecycleOwner) { places ->
//                for (place in places) {
//                    if (userPoint != null && Place.calculateDistance(
//                            userPoint.latitude,
//                            userPoint.longitude,
//                            place
//                        ) < Place.showDistance
//                    ) {
//                        val `object`: PlacemarkMapObject = mapObjects.addPlacemark(
//                            Point(
//                                place.getLatitude(),
//                                place.getLongitude()
//                            )
//                        )
//                        `object`.setIcon(
//                            ImageProvider.fromResource(
//                                context,
//                                R.drawable.pin
//                            ),
//                            IconStyle().setAnchor(PointF(0.5f, 0.7f))
//                                .setScale(0.04f)
//                        )
//                        `object`.isVisible = true
//                        `object`.userData = place
//                        `object`.addTapListener(objectTapListener)
//                    } // Настройка видимости места
//
//                    //Проверяем находится ли пользователь достаточно близко к месту, чтобы добавить его как посещённое
//                    if (userPoint != null && Place.calculateDistance(
//                            userPoint.latitude,
//                            userPoint.longitude,
//                            place
//                        ) < place.getRadius()
//                    ) {
//                        var include = false
//                        for (mPlaces in data.getIdOfVisitedPlaces()) {
//                            if (mPlaces === place.getId()) {
//                                include = true
//                                break
//                            }
//                        }
//                        if (!include) {
//                            mUserViewModel.refusedDataBase(viewLifecycleOwner)
//                            mUserViewModel.insertPlace(place.getId())
//                            mBinding.mapNotification.startAnimation(
//                                AnimationUtils.loadAnimation(
//                                    context,
//                                    R.anim.visible_on
//                                )
//                            )
//                            mBinding.mapNotification.setVisibility(View.VISIBLE)
//                            mBinding.mapNotification.setOnClickListener { v ->
//                                NavHostFragment.findNavController(
//                                    this
//                                )
//                                    .navigate(MapFragmentDirections.actionMapFragmentToVisitListFragmentOnMapList())
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

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

        mapView!!.map.setMapStyle(hideAllStyle)
    }


    override fun onStart() {
        mapView?.onStart()
        MapKitFactory.getInstance().onStart()
        super.onStart()
    }

    override fun onStop() {
        mapView?.onStop()
        MapKitFactory.getInstance().onStop()
        //Save last cam position
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