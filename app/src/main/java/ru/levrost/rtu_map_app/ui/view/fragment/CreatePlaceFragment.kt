package ru.levrost.rtu_map_app.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.levrost.rtu_map_app.R
import ru.levrost.rtu_map_app.databinding.CreatePlaceFragmentBinding
import ru.levrost.rtu_map_app.ui.view.fragment.controllers.CreatePlaceFragmentController
import ru.levrost.rtu_map_app.ui.viewModel.PlaceListViewModel
import ru.levrost.rtu_map_app.ui.viewModel.UserViewModel

class CreatePlaceFragment: Fragment() {
    private var _binding: CreatePlaceFragmentBinding? = null
    private val mBinding get() = _binding!!

    private var _controller: CreatePlaceFragmentController? = null
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
        _binding = CreatePlaceFragmentBinding.inflate(inflater, container, false)
        _controller = CreatePlaceFragmentController(this, mBinding)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (placeListViewModel.getLastUri() != null){
            mBinding.placePic.setImageURI(placeListViewModel.getLastUri())
        }

        mBinding.placePic.setOnClickListener {
            controller.getContentFromGallery.launch("image/*")
        }

        mBinding.btnConfirm.setOnClickListener {
            controller.createPlace()
        }

        mBinding.selectOnMap.setOnClickListener {
            findNavController().navigate(R.id.action_createPlaceFragment_to_mapSelectedFragment)
        }

        mBinding.cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        placeListViewModel.selectPlace(0.0,0.0)
        _binding = null
        _controller = null
    }
}