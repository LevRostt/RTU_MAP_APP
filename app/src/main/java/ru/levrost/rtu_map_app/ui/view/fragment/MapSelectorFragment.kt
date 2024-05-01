package ru.levrost.rtu_map_app.ui.view.fragment

import android.app.AlertDialog
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
import ru.levrost.rtu_map_app.ui.view.fragment.controllers.MapSelectorController
import ru.levrost.rtu_map_app.ui.viewModel.PlaceListViewModel

class MapSelectorFragment: Fragment() {
    private var _binding: FragmentMapSelectedBinding? = null
    private val mBinding get() = _binding!!
    private var _controller: MapSelectorController? = null
    private val controller get() = _controller!!

    val placeListViewModel: PlaceListViewModel by activityViewModels<PlaceListViewModel> {
        PlaceListViewModel.Factory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapSelectedBinding.inflate(inflater, container, false)

        controller.mapBinding()

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller.mapSetup()
    }


    override fun onStart() {
        super.onStart()
        controller.mapStart()
    }

    override fun onStop() {
        super.onStop()
        controller.mapStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _controller = null
    }
}