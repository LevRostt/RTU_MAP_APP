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
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import ru.levrost.rtu_map_app.databinding.MapPlaceCardBinding
import ru.levrost.rtu_map_app.ui.viewModel.PlaceListViewModel
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel


class MapFragment: Fragment() {

    private var _binding: MapFragmentBinding? = null
    private val MAPKIT_API_KEY = "da88c11a-ce91-46e7-bfa8-ab8a2c9d90a0"
    private val mBinding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels<UserViewModel> {
        UserViewModel.Factory
    }

    private val placeListViewModel: PlaceListViewModel by activityViewModels<PlaceListViewModel> {
        PlaceListViewModel.Factory
    }

    private var cardFragment : MapCardFragment? = null

    private var mapView: MapView? = null
    private var userPoint: Point = Point()
    private var userLocationLayer: UserLocationLayer? = null
    private var zoom: Float = 9.5F
    private var mapObjects: MapObjectCollection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            MapKitFactory.setApiKey(MAPKIT_API_KEY)
        } catch (e: AssertionError){}

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

        mapObjects = mapView!!.map.mapObjects.addCollection()

        createMapPlaces()

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
        zoom = if (userPoint.latitude != 55.7515){
            16F
        } else{
            4.5F
        }

    }

    private fun createMapPlaces() { //Добавляет приблюжённые к пользователю места на карту
        val objectTapListener =
            MapObjectTapListener { mapObject: MapObject, _: Point? ->  // Создаём заранее, чтобы не программа не теряла указатель
                // на это listener
                val localObject = mapObject as PlacemarkMapObject
                val userData = localObject.userData
//                cardFragment = MapCardFragment()
//                showSecondFragment()
                Toast.makeText(context, (userData as Place).stringToOut(), Toast.LENGTH_SHORT).show()
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

    private fun showSecondFragment() {
        childFragmentManager.beginTransaction()
            .replace(R.id.card_fragment, cardFragment!!)
            .commit()
        mBinding.cardFragment.visibility = View.VISIBLE
    }

    private fun hideSecondFragment() {
        childFragmentManager.beginTransaction()
            .remove(cardFragment!!)
            .commit()
        mBinding.cardFragment.visibility = View.GONE
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