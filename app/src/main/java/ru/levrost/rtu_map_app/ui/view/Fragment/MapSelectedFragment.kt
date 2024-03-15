package ru.levrost.rtu_map_app.ui.view.Fragment

import android.app.AlertDialog
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import ru.levrost.rtu_map_app.ui.viewModel.PlaceListViewModel

class MapSelectedFragment: Fragment() {
    private var _binding: FragmentMapSelectedBinding? = null
    private val mBinding get() = _binding!!
//    private val placeListViewModel: PlaceListViewModel by viewModels {
//        PlaceListViewModel.Factory
//    }

    private val placeListViewModel: PlaceListViewModel by activityViewModels<PlaceListViewModel> {
        PlaceListViewModel.Factory
    }


    private lateinit var mapView: MapView
    private lateinit var mapKit : MapKit
    private lateinit var mapObjects: MapObjectCollection
    private lateinit var icon : ImageProvider
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapSelectedBinding.inflate(inflater, container, false)

        mapView = mBinding.mapView
        mapKit = MapKitFactory.getInstance()
        mapObjects = mapView.map.mapObjects.addCollection()

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        icon = ImageProvider.fromResource(context, R.drawable.location_pin_filled)

        mapView.map.move(CameraPosition(Point(55.7515, 37.64), 5f, 0.0f, 0.0f))
        mapView.map.addInputListener(inputListener)
    }

    private val inputListener = object : InputListener{
        override fun onMapTap(map: Map, point: Point) {
//            Toast.makeText(getContext(), point.getLatitude() + String.valueOf(point.getLongitude()) , Toast.LENGTH_SHORT).show();

            // icon не устанавливается
            val obj = mapObjects.addPlacemark(
                Point(point.latitude, point.longitude)
            )

            //
            obj.setIcon(
                icon
            )

            obj.isVisible = true

            val builder = AlertDialog.Builder(context)
            builder.setMessage("Выбрать это место?")
                .setCancelable(false)
                .setPositiveButton("Да") { dialog, id -> //pop back stack
//                    val bundle = Bundle()
//                    bundle.putDoubleArray(
//                        ModerationMessegeFragment.key_to_data,
//                        doubleArrayOf(point.latitude, point.longitude)
//                    )
//                    parentFragmentManager.setFragmentResult(
//                        ModerationMessegeFragment.key_to_data,
//                        bundle
//                    )
                    placeListViewModel.selectPlace(point.latitude, point.longitude)
                    NavHostFragment.findNavController(parentFragment!!).popBackStack()
                    dialog.cancel()
                }
                .setNegativeButton("Нет") { dialog, id -> // Закрываем диалоговое окно
                    mapObjects.remove(obj)
                    dialog.cancel()
                }
            val alert = builder.create()
            alert.show()
        }

        override fun onMapLongTap(p0: Map, p1: Point) {}

    }


    override fun onStart() {
        super.onStart()
        mapView.onStart()
        mapKit.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        mapKit.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}