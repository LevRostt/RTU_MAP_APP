package ru.levrost.rtu_map_app.ui.view.fragment

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.data.model.Place
import ru.levrost.rtu_map_app.data.model.UserData
import ru.levrost.rtu_map_app.databinding.MapPlaceCardBinding
import ru.levrost.rtu_map_app.global.isInternetAvailable
import ru.levrost.rtu_map_app.ui.view.activity.MainActivity
import ru.levrost.rtu_map_app.ui.view.fragment.controllers.MapCardFragmentController
import ru.levrost.rtu_map_app.ui.viewModel.PlaceListViewModel
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel

class MapCardFragment(val place : Place) : Fragment() {

    private var _bindind: MapPlaceCardBinding? = null
    private val mBinding get() = _bindind!!
    private var _controller: MapCardFragmentController? = null
    private val controller get() = _controller!!

    val placeListViewModel: PlaceListViewModel by activityViewModels<PlaceListViewModel> {
        PlaceListViewModel.Factory
    }

    val userViewModel: UserViewModel by activityViewModels<UserViewModel> {
        UserViewModel.Factory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindind = MapPlaceCardBinding.inflate(inflater, container, false)
        _controller = MapCardFragmentController(this, mBinding)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controller.draw()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindind = null
    }
}