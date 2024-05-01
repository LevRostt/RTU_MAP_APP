package ru.levrost.rtu_map_app.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView
import ru.levrost.rtu_map_app.databinding.MapFragmentBinding
import ru.levrost.rtu_map_app.ui.view.fragment.controllers.MapFragmentController
import ru.levrost.rtu_map_app.ui.viewModel.PlaceListViewModel
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel


class MapFragment: Fragment() {
    private var _binding: MapFragmentBinding? = null
    private val mBinding get() = _binding!!
    private var _controller: MapFragmentController? = null
    private val controller get() = _controller!!

    val userViewModel: UserViewModel by activityViewModels<UserViewModel> {
        UserViewModel.Factory
    }

    val placeListViewModel: PlaceListViewModel by activityViewModels<PlaceListViewModel> {
        PlaceListViewModel.Factory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MapFragmentBinding.inflate(inflater, container, false)
        _controller = MapFragmentController(this, mBinding)

        controller.mapInit()

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controller.mapUiInit()

        mBinding.userLocationBtm.setOnClickListener {
            controller.showUser()
        }

        mBinding.cardCloseBtn.setOnClickListener {
            controller.closeCard()
        }

    }

    override fun onStart() {
        controller.mapStart()
        super.onStart()
    }

    override fun onStop() {
        controller.mapStop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _controller = null
    }

}